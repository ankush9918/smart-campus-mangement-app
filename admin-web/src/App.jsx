import React, { useState, useEffect, useCallback } from 'react';
import { Sidebar } from './components/Sidebar';
import { Header } from './components/Header';
import { Toast } from './components/Toast';
import { ConfirmDialog } from './components/ConfirmDialog';
import { SupabaseModal } from './components/SupabaseModal';
import { LoadingSpinner } from './components/LoadingState';

import { DashboardPage } from './pages/DashboardPage';
import { StudentsPage } from './pages/StudentsPage';
import { FacultyPage } from './pages/FacultyPage';
import { AttendancePage } from './pages/AttendancePage';
import { TimetablePage } from './pages/TimetablePage';
import { CircularsPage } from './pages/CircularsPage';
import { LoginPage } from './pages/LoginPage';

import { dataService } from './services/dataService';
import { checkSupabaseStatus, supabase } from './lib/supabase';

export function App() {
  const [currentUser, setCurrentUser] = useState(null);
  const [isAuthLoading, setIsAuthLoading] = useState(true);
  const [currentTab, setCurrentTab] = useState('dashboard');
  const [isMobileOpen, setIsMobileOpen] = useState(false);
  const [isSupabaseModalOpen, setIsSupabaseModalOpen] = useState(false);

  // Theme state (Dark / Light mode)
  const [isDarkMode, setIsDarkMode] = useState(() => {
    const saved = localStorage.getItem('theme');
    if (saved) return saved === 'dark';
    return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
  });

  useEffect(() => {
    if (isDarkMode) {
      document.documentElement.classList.add('dark');
      localStorage.setItem('theme', 'dark');
    } else {
      document.documentElement.classList.remove('dark');
      localStorage.setItem('theme', 'light');
    }
  }, [isDarkMode]);

  const toggleDarkMode = () => {
    setIsDarkMode(prev => !prev);
  };

  // Authenticate Admin session on mount directly from Supabase Auth
  useEffect(() => {
    supabase.auth.getSession().then(({ data: { session } }) => {
      const user = session?.user;
      const role = user?.user_metadata?.role?.toLowerCase() || '';
      if (user && role === 'admin') {
        setCurrentUser(user);
      } else {
        setCurrentUser(null);
      }
      setIsAuthLoading(false);
    });

    const { data: { subscription } } = supabase.auth.onAuthStateChange((_event, session) => {
      const user = session?.user;
      const role = user?.user_metadata?.role?.toLowerCase() || '';
      if (user && role === 'admin') {
        setCurrentUser(user);
      } else {
        setCurrentUser(null);
      }
      setIsAuthLoading(false);
    });

    return () => subscription.unsubscribe();
  }, []);

  const handleLogout = async () => {
    await supabase.auth.signOut();
    setCurrentUser(null);
    showToast('info', 'Signed Out', 'You have been securely signed out.');
  };

  // Data states
  const [students, setStudents] = useState([]);
  const [faculty, setFaculty] = useState([]);
  const [studentTimetable, setStudentTimetable] = useState([]);
  const [professorTimetable, setProfessorTimetable] = useState([]);
  const [studentAttendance, setStudentAttendance] = useState([]);
  const [facultyAttendance, setFacultyAttendance] = useState([]);
  const [circulars, setCirculars] = useState([]);

  // System states
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [supabaseStatus, setSupabaseStatus] = useState(null);

  // Shared Add Modals trigger
  const [isStudentAddOpen, setIsStudentAddOpen] = useState(false);
  const [isFacultyAddOpen, setIsFacultyAddOpen] = useState(false);
  const [isCircularAddOpen, setIsCircularAddOpen] = useState(false);

  // Toast & Confirm Dialog
  const [toast, setToast] = useState(null);
  const [confirmDialog, setConfirmDialog] = useState({
    isOpen: false,
    title: '',
    message: '',
    onConfirm: () => {}
  });

  const showToast = useCallback((type, title, message) => {
    setToast({ type, title, message });
    setTimeout(() => {
      setToast(null);
    }, 4000);
  }, []);

  const openConfirmDialog = useCallback((title, message, onConfirm) => {
    setConfirmDialog({
      isOpen: true,
      title,
      message,
      onConfirm: async () => {
        try {
          await onConfirm();
          setConfirmDialog(prev => ({ ...prev, isOpen: false }));
        } catch (e) {
          showToast('error', 'Action Failed', e.message);
        }
      }
    });
  }, [showToast]);

  // Check Supabase connection health
  const refreshSupabaseStatus = useCallback(async () => {
    try {
      const status = await checkSupabaseStatus();
      setSupabaseStatus(status);
      return status;
    } catch (e) {
      console.error('Failed to check Supabase status:', e);
    }
  }, []);

  // Fetch all data
  const loadAllData = useCallback(async (isRefresh = false) => {
    if (isRefresh) setIsRefreshing(true);
    else setIsLoading(true);

    try {
      const [
        studentsRes,
        facultyRes,
        sTimetableRes,
        pTimetableRes,
        sAttendanceRes,
        fAttendanceRes,
        circularsRes
      ] = await Promise.all([
        dataService.getStudents(),
        dataService.getFaculty(),
        dataService.getStudentTimetable(),
        dataService.getProfessorTimetable(),
        dataService.getStudentAttendance(),
        dataService.getFacultyAttendance(),
        dataService.getCirculars()
      ]);

      setStudents(studentsRes.data || []);
      setFaculty(facultyRes.data || []);
      setStudentTimetable(sTimetableRes.data || []);
      setProfessorTimetable(pTimetableRes.data || []);
      setStudentAttendance(sAttendanceRes.data || []);
      setFacultyAttendance(fAttendanceRes.data || []);
      setCirculars(circularsRes.data || []);

      await refreshSupabaseStatus();

      if (isRefresh) {
        showToast('success', 'Data Synchronized', 'All records refreshed with Supabase backend.');
      }
    } catch (err) {
      console.error('Error loading data:', err);
      showToast('error', 'Loading Error', 'Failed to fetch data from database.');
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  }, [refreshSupabaseStatus, showToast]);

  useEffect(() => {
    loadAllData();
  }, [loadAllData]);

  // Navigation helper
  const handleNavigate = (tab, options = {}) => {
    setCurrentTab(tab);
    if (options.openAddModal) {
      if (tab === 'students') setIsStudentAddOpen(true);
      if (tab === 'faculty') setIsFacultyAddOpen(true);
      if (tab === 'circulars') setIsCircularAddOpen(true);
    }
  };

  // Section titles
  const tabTitles = {
    dashboard: { title: 'Executive Overview', subtitle: 'Campus operations, live database sync & metrics' },
    students: { title: 'Student Management Desk', subtitle: 'Enrolled students roster, credentials & academic standing' },
    faculty: { title: 'Faculty & Professors Desk', subtitle: 'Professors directory, qualifications, workloads & cabin venues' },
    attendance: { title: 'Attendance Governance', subtitle: 'Real-time teacher classroom logs & faculty check-in registry' },
    timetable: { title: 'Timetable Scheduling Desk', subtitle: 'Weekly lecture timetable allocation for students & professors' },
    circulars: { title: 'Campus Circulars & Bulletins', subtitle: 'Official notices and announcements broadcasted to mobile users' }
  }[currentTab] || { title: 'Admin Portal', subtitle: '' };

  // ==============================================================
  // CRUD Handlers
  // ==============================================================

  // Students
  const handleAddStudent = async (newStudent) => {
    const res = await dataService.addStudent(newStudent);
    setStudents(prev => [res.data, ...prev.filter(s => s.id !== res.data.id)]);
    return res;
  };

  const handleUpdateStudent = async (id, updates) => {
    const res = await dataService.updateStudent(id, updates);
    setStudents(prev => prev.map(s => s.id === id ? res.data : s));
    return res;
  };

  const handleDeleteStudent = async (id) => {
    await dataService.deleteStudent(id);
    setStudents(prev => prev.filter(s => s.id !== id));
    showToast('info', 'Student Removed', 'The student record was deleted.');
  };

  // Faculty
  const handleAddFaculty = async (newFaculty) => {
    const res = await dataService.addFaculty(newFaculty);
    setFaculty(prev => [res.data, ...prev.filter(f => f.id !== res.data.id)]);
  };

  const handleUpdateFaculty = async (id, updates) => {
    const res = await dataService.updateFaculty(id, updates);
    setFaculty(prev => prev.map(f => f.id === id ? res.data : f));
  };

  const handleDeleteFaculty = async (id) => {
    await dataService.deleteFaculty(id);
    setFaculty(prev => prev.filter(f => f.id !== id));
    showToast('info', 'Faculty Removed', 'The faculty member was removed.');
  };

  // Student Timetable
  const handleAddStudentSlot = async (slot) => {
    const res = await dataService.addStudentTimetableSlot(slot);
    setStudentTimetable(prev => [...prev, res.data]);
  };

  const handleUpdateStudentSlot = async (id, updates) => {
    const res = await dataService.updateStudentTimetableSlot(id, updates);
    setStudentTimetable(prev => prev.map(s => s.id === id ? res.data : s));
  };

  const handleDeleteStudentSlot = async (id) => {
    await dataService.deleteStudentTimetableSlot(id);
    setStudentTimetable(prev => prev.filter(s => s.id !== id));
    showToast('info', 'Slot Deleted', 'Student timetable slot removed.');
  };

  // Professor Timetable
  const handleAddProfessorSlot = async (slot) => {
    const res = await dataService.addProfessorTimetableSlot(slot);
    setProfessorTimetable(prev => [...prev, res.data]);
  };

  const handleUpdateProfessorSlot = async (id, updates) => {
    const res = await dataService.updateProfessorTimetableSlot(id, updates);
    setProfessorTimetable(prev => prev.map(s => s.id === id ? res.data : s));
  };

  const handleDeleteProfessorSlot = async (id) => {
    await dataService.deleteProfessorTimetableSlot(id);
    setProfessorTimetable(prev => prev.filter(s => s.id !== id));
    showToast('info', 'Slot Deleted', 'Professor timetable slot removed.');
  };

  // Attendance
  const handleRecordStudentAttendance = async (record) => {
    const res = await dataService.recordStudentAttendance(record);
    setStudentAttendance(prev => [res.data, ...prev]);
  };

  const handleRecordFacultyAttendance = async (record) => {
    const res = await dataService.recordFacultyAttendance(record);
    setFacultyAttendance(prev => [res.data, ...prev]);
  };

  // Circulars
  const handleAddCircular = async (circular) => {
    const res = await dataService.addCircular(circular);
    setCirculars(prev => [res.data, ...prev.filter(c => c.id !== res.data.id)]);
  };

  const handleUpdateCircular = async (id, updates) => {
    const res = await dataService.updateCircular(id, updates);
    setCirculars(prev => prev.map(c => c.id === id ? res.data : c));
  };

  const handleDeleteCircular = async (id) => {
    await dataService.deleteCircular(id);
    setCirculars(prev => prev.filter(c => c.id !== id));
    showToast('info', 'Circular Removed', 'The notice was removed.');
  };

  if (isAuthLoading) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
        <LoadingSpinner message="Verifying administrative credentials..." />
      </div>
    );
  }

  if (!currentUser) {
    return (
      <LoginPage
        onLoginSuccess={({ user }) => {
          setCurrentUser(user);
          setCurrentTab('dashboard');
          showToast('success', 'Authenticated', 'Welcome to the Administrator Control Center.');
        }}
      />
    );
  }

  return (
    <div className="min-h-screen bg-slate-50 flex">
      {/* Sidebar Navigation */}
      <Sidebar
        currentTab={currentTab}
        onSelectTab={setCurrentTab}
        isMobileOpen={isMobileOpen}
        onCloseMobile={() => setIsMobileOpen(false)}
        supabaseStatus={supabaseStatus}
        onOpenSupabaseModal={() => setIsSupabaseModalOpen(true)}
        isDarkMode={isDarkMode}
        onToggleDarkMode={toggleDarkMode}
      />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0 lg:pl-72">
        <Header
          title={tabTitles.title}
          subtitle={tabTitles.subtitle}
          onOpenMobile={() => setIsMobileOpen(true)}
          supabaseStatus={supabaseStatus}
          onOpenSupabaseModal={() => setIsSupabaseModalOpen(true)}
          onRefreshData={() => loadAllData(true)}
          isRefreshing={isRefreshing}
          onLogout={handleLogout}
          isDarkMode={isDarkMode}
          onToggleDarkMode={toggleDarkMode}
        />

        <main className="flex-1 p-4 sm:p-6 lg:p-8 max-w-7xl w-full mx-auto">
          {isLoading ? (
            <LoadingSpinner message="Connecting to Supabase and loading records..." />
          ) : (
            <>
              {currentTab === 'dashboard' && (
                <DashboardPage
                  students={students}
                  faculty={faculty}
                  studentTimetable={studentTimetable}
                  studentAttendance={studentAttendance}
                  circulars={circulars}
                  supabaseStatus={supabaseStatus}
                  onNavigate={handleNavigate}
                  onOpenSupabaseModal={() => setIsSupabaseModalOpen(true)}
                />
              )}

              {currentTab === 'students' && (
                <StudentsPage
                  students={students}
                  onAddStudent={handleAddStudent}
                  onUpdateStudent={handleUpdateStudent}
                  onDeleteStudent={handleDeleteStudent}
                  isAddModalOpen={isStudentAddOpen}
                  setIsAddModalOpen={setIsStudentAddOpen}
                  onShowToast={showToast}
                  onConfirmDelete={openConfirmDialog}
                />
              )}

              {currentTab === 'faculty' && (
                <FacultyPage
                  faculty={faculty}
                  onAddFaculty={handleAddFaculty}
                  onUpdateFaculty={handleUpdateFaculty}
                  onDeleteFaculty={handleDeleteFaculty}
                  isAddModalOpen={isFacultyAddOpen}
                  setIsAddModalOpen={setIsFacultyAddOpen}
                  onShowToast={showToast}
                  onConfirmDelete={openConfirmDialog}
                />
              )}

              {currentTab === 'attendance' && (
                <AttendancePage
                  studentAttendance={studentAttendance}
                  facultyAttendance={facultyAttendance}
                  onRecordStudentAttendance={handleRecordStudentAttendance}
                  onRecordFacultyAttendance={handleRecordFacultyAttendance}
                  students={students}
                  faculty={faculty}
                  onShowToast={showToast}
                />
              )}

              {currentTab === 'timetable' && (
                <TimetablePage
                  studentTimetable={studentTimetable}
                  professorTimetable={professorTimetable}
                  onAddStudentSlot={handleAddStudentSlot}
                  onUpdateStudentSlot={handleUpdateStudentSlot}
                  onDeleteStudentSlot={handleDeleteStudentSlot}
                  onAddProfessorSlot={handleAddProfessorSlot}
                  onUpdateProfessorSlot={handleUpdateProfessorSlot}
                  onDeleteProfessorSlot={handleDeleteProfessorSlot}
                  faculty={faculty}
                  onShowToast={showToast}
                  onConfirmDelete={openConfirmDialog}
                />
              )}

              {currentTab === 'circulars' && (
                <CircularsPage
                  circulars={circulars}
                  onAddCircular={handleAddCircular}
                  onUpdateCircular={handleUpdateCircular}
                  onDeleteCircular={handleDeleteCircular}
                  isAddModalOpen={isCircularAddOpen}
                  setIsAddModalOpen={setIsCircularAddOpen}
                  onShowToast={showToast}
                  onConfirmDelete={openConfirmDialog}
                />
              )}

              {currentTab === 'login' && (
                <LoginPage
                  onLoginSuccess={() => {
                    setCurrentTab('dashboard');
                    showToast('success', 'Signed In', 'Welcome to the Smart Campus Admin Portal.');
                  }}
                />
              )}
            </>
          )}
        </main>
      </div>

      {/* Supabase Schema / Diagnostic Modal */}
      <SupabaseModal
        isOpen={isSupabaseModalOpen}
        onClose={() => setIsSupabaseModalOpen(false)}
        status={supabaseStatus}
        onRefreshStatus={refreshSupabaseStatus}
      />

      {/* Global Confirmation Dialog */}
      <ConfirmDialog
        isOpen={confirmDialog.isOpen}
        title={confirmDialog.title}
        message={confirmDialog.message}
        onConfirm={confirmDialog.onConfirm}
        onCancel={() => setConfirmDialog(prev => ({ ...prev, isOpen: false }))}
      />

      {/* Global Floating Toast */}
      <Toast notification={toast} onClose={() => setToast(null)} />
    </div>
  );
}
export default App;
