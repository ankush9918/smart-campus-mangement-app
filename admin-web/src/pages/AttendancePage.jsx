import React, { useState } from 'react';
import { 
  ClipboardCheck, 
  Search, 
  Calendar, 
  CheckCircle2, 
  XCircle, 
  Clock, 
  UserCheck, 
  Users, 
  PlusCircle, 
  Filter,
  X,
  Check
} from 'lucide-react';
import { EmptyState } from '../components/LoadingState';
import { MASTER_SUBJECTS } from '../services/dataService';

const THEORY_SUBJECTS = MASTER_SUBJECTS.map(s => s.displayName);

export function AttendancePage({ 
  studentAttendance, 
  facultyAttendance, 
  onRecordStudentAttendance, 
  onRecordFacultyAttendance, 
  students, 
  faculty, 
  onShowToast 
}) {
  const [activeTab, setActiveTab] = useState('student'); // 'student' | 'faculty'
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedDate, setSelectedDate] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('ALL');

  // Modal State
  const [isRecordModalOpen, setIsRecordModalOpen] = useState(false);
  const [modalType, setModalType] = useState('student'); // 'student' | 'faculty'

  // Student Form
  const [stuForm, setStuForm] = useState({
    student_id: '',
    student_name: '',
    roll_number: '',
    subject: THEORY_SUBJECTS[0],
    date: new Date().toISOString().split('T')[0],
    status: 'PRESENT',
    marked_by: 'Dr. Rajesh Kumar'
  });

  // Faculty Form
  const [facForm, setFacForm] = useState({
    faculty_id: '',
    faculty_name: '',
    employee_id: '',
    department: 'Computer Science & Engineering',
    date: new Date().toISOString().split('T')[0],
    status: 'PRESENT',
    check_in_time: '08:45 AM'
  });

  // Open Record Modal
  const handleOpenRecord = (type) => {
    setModalType(type);
    if (type === 'student') {
      const defaultStu = students[0] || {};
      setStuForm({
        student_id: defaultStu.id || 'std_01',
        student_name: defaultStu.name || 'Aditya Sharma',
        roll_number: defaultStu.roll_number || 'VVIT/CS/2021/045',
        subject: THEORY_SUBJECTS[0],
        date: new Date().toISOString().split('T')[0],
        status: 'PRESENT',
        marked_by: 'Dr. Rajesh Kumar'
      });
    } else {
      const defaultFac = faculty[0] || {};
      setFacForm({
        faculty_id: defaultFac.id || 'fac_01',
        faculty_name: defaultFac.name || 'Dr. Rajesh Kumar',
        employee_id: defaultFac.employee_id || 'VVIT/CS/T-104',
        department: defaultFac.department || 'Computer Science & Engineering',
        date: new Date().toISOString().split('T')[0],
        status: 'PRESENT',
        check_in_time: '08:45 AM'
      });
    }
    setIsRecordModalOpen(true);
  };

  // Submit Record
  const handleSubmitRecord = async (e) => {
    e.preventDefault();
    try {
      if (modalType === 'student') {
        await onRecordStudentAttendance(stuForm);
        onShowToast('success', 'Attendance Logged', `Attendance recorded for ${stuForm.student_name}.`);
      } else {
        await onRecordFacultyAttendance(facForm);
        onShowToast('success', 'Faculty Attendance Logged', `Check-in recorded for ${facForm.faculty_name}.`);
      }
      setIsRecordModalOpen(false);
    } catch (err) {
      onShowToast('error', 'Operation Failed', err.message || 'Could not record attendance.');
    }
  };

  // Filter Student Records
  const filteredStudentRecords = studentAttendance.filter((r) => {
    const matchesSearch =
      (r.student_name && r.student_name.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (r.roll_number && r.roll_number.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (r.subject && r.subject.toLowerCase().includes(searchQuery.toLowerCase()));

    const matchesDate = !selectedDate || r.date === selectedDate;
    const matchesStatus = selectedStatus === 'ALL' || r.status === selectedStatus;

    return matchesSearch && matchesDate && matchesStatus;
  });

  // Filter Faculty Records
  const filteredFacultyRecords = facultyAttendance.filter((r) => {
    const matchesSearch =
      (r.faculty_name && r.faculty_name.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (r.employee_id && r.employee_id.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (r.department && r.department.toLowerCase().includes(searchQuery.toLowerCase()));

    const matchesDate = !selectedDate || r.date === selectedDate;
    const matchesStatus = selectedStatus === 'ALL' || r.status === selectedStatus;

    return matchesSearch && matchesDate && matchesStatus;
  });

  // Stats calculation
  const totalStudentRecords = studentAttendance.length;
  const presentStudentCount = studentAttendance.filter(r => r.status === 'PRESENT').length;
  const studentPresentRate = totalStudentRecords > 0 ? Math.round((presentStudentCount / totalStudentRecords) * 100) : 100;

  const totalFacultyRecords = facultyAttendance.length;
  const presentFacultyCount = facultyAttendance.filter(r => r.status === 'PRESENT').length;
  const facultyPresentRate = totalFacultyRecords > 0 ? Math.round((presentFacultyCount / totalFacultyRecords) * 100) : 100;

  return (
    <div className="space-y-6">
      {/* Top Segmented Tabs: Student Attendance vs Faculty Attendance */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-4 border-b border-slate-200 pb-4">
        <div className="flex items-center gap-2 p-1 bg-slate-100 rounded-xl max-w-sm">
          <button
            onClick={() => { setActiveTab('student'); setSelectedStatus('ALL'); }}
            className={`flex-1 flex items-center justify-center gap-2 py-2 px-4 rounded-lg text-xs font-bold transition-all ${
              activeTab === 'student'
                ? 'bg-white text-brand-700 shadow-xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <UserCheck className="w-4 h-4" />
            <span>Student Attendance</span>
            <span className="text-[10px] px-1.5 py-0.2 rounded-full bg-slate-200 font-bold">
              {studentAttendance.length}
            </span>
          </button>

          <button
            onClick={() => { setActiveTab('faculty'); setSelectedStatus('ALL'); }}
            className={`flex-1 flex items-center justify-center gap-2 py-2 px-4 rounded-lg text-xs font-bold transition-all ${
              activeTab === 'faculty'
                ? 'bg-white text-brand-700 shadow-xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <Users className="w-4 h-4" />
            <span>Faculty Attendance</span>
            <span className="text-[10px] px-1.5 py-0.2 rounded-full bg-slate-200 font-bold">
              {facultyAttendance.length}
            </span>
          </button>
        </div>

        <button
          onClick={() => handleOpenRecord(activeTab)}
          className="px-4 py-2 bg-brand-600 hover:bg-brand-700 text-white text-xs font-bold rounded-xl shadow-sm transition-all flex items-center gap-1.5 shrink-0 self-end sm:self-auto"
        >
          <PlusCircle className="w-4 h-4" />
          <span>{activeTab === 'student' ? 'Record Student Attendance' : 'Log Faculty Attendance'}</span>
        </button>
      </div>

      {/* Summary KPI Pills */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white p-4 rounded-xl border border-slate-100 shadow-2xs">
          <div className="text-[11px] font-bold text-slate-500 uppercase tracking-wider">
            Total {activeTab === 'student' ? 'Student' : 'Faculty'} Logs
          </div>
          <div className="text-xl font-extrabold text-slate-900 mt-1">
            {activeTab === 'student' ? totalStudentRecords : totalFacultyRecords}
          </div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-100 shadow-2xs">
          <div className="text-[11px] font-bold text-slate-500 uppercase tracking-wider">Present Count</div>
          <div className="text-xl font-extrabold text-emerald-600 mt-1">
            {activeTab === 'student' ? presentStudentCount : presentFacultyCount}
          </div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-100 shadow-2xs">
          <div className="text-[11px] font-bold text-slate-500 uppercase tracking-wider">Attendance Percentage</div>
          <div className="text-xl font-extrabold text-brand-600 mt-1">
            {activeTab === 'student' ? studentPresentRate : facultyPresentRate}%
          </div>
        </div>
      </div>

      {/* Search and Filters */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
        <div className="relative flex-1 max-w-md">
          <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder={activeTab === 'student' ? "Search student name, roll number, subject..." : "Search faculty name, employee ID, department..."}
            className="w-full pl-10 pr-4 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500 shadow-xs"
          />
        </div>

        <div className="flex items-center gap-2">
          {/* Date filter */}
          <input
            type="date"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            className="px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-700 focus:outline-none focus:border-brand-500 shadow-xs"
          />
          {selectedDate && (
            <button
              onClick={() => setSelectedDate('')}
              className="text-[11px] text-slate-500 hover:text-slate-800 underline"
            >
              Clear
            </button>
          )}

          {/* Status filter */}
          <select
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value)}
            className="px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-700 focus:outline-none focus:border-brand-500 shadow-xs font-medium"
          >
            <option value="ALL">All Statuses</option>
            <option value="PRESENT">Present</option>
            {activeTab === 'student' ? (
              <>
                <option value="ABSENT">Absent</option>
                <option value="LATE">Late</option>
              </>
            ) : (
              <>
                <option value="ON_LEAVE">On Leave</option>
                <option value="HALF_DAY">Half Day</option>
              </>
            )}
          </select>
        </div>
      </div>

      {/* Active Tab Table */}
      {activeTab === 'student' ? (
        // STUDENT ATTENDANCE TABLE
        filteredStudentRecords.length === 0 ? (
          <EmptyState
            icon={UserCheck}
            title="No Student Attendance Records"
            description={searchQuery ? "No records match your search." : "No student attendance has been recorded yet."}
            actionText="Record Attendance"
            onAction={() => handleOpenRecord('student')}
          />
        ) : (
          <div className="bg-white rounded-2xl border border-slate-200/80 overflow-hidden shadow-xs">
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-slate-50/70 border-b border-slate-100 text-[11px] font-bold text-slate-500 uppercase tracking-wider">
                    <th className="py-3.5 px-4">Date</th>
                    <th className="py-3.5 px-4">Roll Number</th>
                    <th className="py-3.5 px-4">Student Name</th>
                    <th className="py-3.5 px-4">Subject</th>
                    <th className="py-3.5 px-4">Status</th>
                    <th className="py-3.5 px-4">Marked By (Faculty)</th>
                    <th className="py-3.5 px-4 text-right">Logged Time</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 text-xs">
                  {filteredStudentRecords.map((r) => {
                    const isPresent = r.status === 'PRESENT';
                    const isAbsent = r.status === 'ABSENT';

                    return (
                      <tr key={r.id} className="hover:bg-slate-50/60 transition-colors">
                        <td className="py-3.5 px-4 font-mono text-slate-600 font-medium">
                          {r.date}
                        </td>
                        <td className="py-3.5 px-4 font-mono font-semibold text-slate-800">
                          {r.roll_number}
                        </td>
                        <td className="py-3.5 px-4 font-bold text-slate-900">
                          {r.student_name}
                        </td>
                        <td className="py-3.5 px-4 text-slate-700">
                          {r.subject}
                        </td>
                        <td className="py-3.5 px-4">
                          <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-bold uppercase tracking-wider ${
                            isPresent 
                              ? 'bg-emerald-100 text-emerald-800 border border-emerald-200' 
                              : isAbsent 
                              ? 'bg-rose-100 text-rose-800 border border-rose-200'
                              : 'bg-amber-100 text-amber-800 border border-amber-200'
                          }`}>
                            {isPresent && <CheckCircle2 className="w-3 h-3 text-emerald-600" />}
                            {isAbsent && <XCircle className="w-3 h-3 text-rose-600" />}
                            <span>{r.status}</span>
                          </span>
                        </td>
                        <td className="py-3.5 px-4 text-slate-600 font-medium">
                          {r.marked_by || 'Professor Desk'}
                        </td>
                        <td className="py-3.5 px-4 text-right text-[11px] text-slate-400">
                          {r.timestamp ? new Date(r.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '—'}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>

            <div className="px-5 py-3 border-t border-slate-100 bg-slate-50/50 flex items-center justify-between text-xs text-slate-500 font-medium">
              <span>Showing {filteredStudentRecords.length} records</span>
              <span className="font-mono text-[11px]">Synced via Supabase `attendance_student`</span>
            </div>
          </div>
        )
      ) : (
        // FACULTY ATTENDANCE TABLE
        filteredFacultyRecords.length === 0 ? (
          <EmptyState
            icon={Users}
            title="No Faculty Attendance Records"
            description={searchQuery ? "No records match your search." : "No faculty attendance logs recorded yet."}
            actionText="Log Faculty Attendance"
            onAction={() => handleOpenRecord('faculty')}
          />
        ) : (
          <div className="bg-white rounded-2xl border border-slate-200/80 overflow-hidden shadow-xs">
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-slate-50/70 border-b border-slate-100 text-[11px] font-bold text-slate-500 uppercase tracking-wider">
                    <th className="py-3.5 px-4">Date</th>
                    <th className="py-3.5 px-4">Employee ID</th>
                    <th className="py-3.5 px-4">Faculty Member</th>
                    <th className="py-3.5 px-4">Department</th>
                    <th className="py-3.5 px-4">Check-in Time</th>
                    <th className="py-3.5 px-4">Status</th>
                    <th className="py-3.5 px-4 text-right">Logged Time</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 text-xs">
                  {filteredFacultyRecords.map((r) => {
                    const isPresent = r.status === 'PRESENT';
                    const isLeave = r.status === 'ON_LEAVE';

                    return (
                      <tr key={r.id} className="hover:bg-slate-50/60 transition-colors">
                        <td className="py-3.5 px-4 font-mono text-slate-600 font-medium">
                          {r.date}
                        </td>
                        <td className="py-3.5 px-4 font-mono font-semibold text-slate-800">
                          {r.employee_id}
                        </td>
                        <td className="py-3.5 px-4 font-bold text-slate-900">
                          {r.faculty_name}
                        </td>
                        <td className="py-3.5 px-4 text-slate-700">
                          {r.department}
                        </td>
                        <td className="py-3.5 px-4 text-slate-800 font-mono font-semibold">
                          {r.check_in_time || '—'}
                        </td>
                        <td className="py-3.5 px-4">
                          <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-bold uppercase tracking-wider ${
                            isPresent 
                              ? 'bg-emerald-100 text-emerald-800 border border-emerald-200' 
                              : isLeave 
                              ? 'bg-amber-100 text-amber-800 border border-amber-200'
                              : 'bg-blue-100 text-blue-800 border border-blue-200'
                          }`}>
                            {isPresent && <CheckCircle2 className="w-3 h-3 text-emerald-600" />}
                            <span>{r.status.replace('_', ' ')}</span>
                          </span>
                        </td>
                        <td className="py-3.5 px-4 text-right text-[11px] text-slate-400">
                          {r.timestamp ? new Date(r.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '—'}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>

            <div className="px-5 py-3 border-t border-slate-100 bg-slate-50/50 flex items-center justify-between text-xs text-slate-500 font-medium">
              <span>Showing {filteredFacultyRecords.length} records</span>
              <span className="font-mono text-[11px]">Synced via Supabase `attendance_faculty`</span>
            </div>
          </div>
        )
      )}

      {/* Record Attendance Modal */}
      {isRecordModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-2xl max-w-lg w-full shadow-2xl border border-slate-100 overflow-hidden">
            <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-brand-50 border border-brand-100 flex items-center justify-center text-brand-600">
                  <ClipboardCheck className="w-5 h-5" />
                </div>
                <div>
                  <h2 className="text-base font-bold text-slate-900">
                    {modalType === 'student' ? 'Record Student Attendance' : 'Log Faculty Attendance'}
                  </h2>
                  <p className="text-xs text-slate-500">Record will be saved to Supabase</p>
                </div>
              </div>
              <button
                onClick={() => setIsRecordModalOpen(false)}
                className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSubmitRecord} className="p-6 space-y-4">
              {modalType === 'student' ? (
                <>
                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1">Select Student</label>
                    <select
                      value={stuForm.student_id}
                      onChange={(e) => {
                        const sel = students.find(s => s.id === e.target.value);
                        if (sel) {
                          setStuForm({
                            ...stuForm,
                            student_id: sel.id,
                            student_name: sel.name,
                            roll_number: sel.roll_number
                          });
                        }
                      }}
                      className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                    >
                      {students.map(s => (
                        <option key={s.id} value={s.id}>{s.name} ({s.roll_number})</option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1">Subject (Theory Only)</label>
                    <select
                      value={stuForm.subject}
                      onChange={(e) => setStuForm({ ...stuForm, subject: e.target.value })}
                      className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                    >
                      {THEORY_SUBJECTS.map(sub => (
                        <option key={sub} value={sub}>{sub}</option>
                      ))}
                    </select>
                  </div>

                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className="block text-xs font-bold text-slate-700 mb-1">Date</label>
                      <input
                        type="date"
                        value={stuForm.date}
                        onChange={(e) => setStuForm({ ...stuForm, date: e.target.value })}
                        className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-bold text-slate-700 mb-1">Status</label>
                      <select
                        value={stuForm.status}
                        onChange={(e) => setStuForm({ ...stuForm, status: e.target.value })}
                        className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                      >
                        <option value="PRESENT">Present</option>
                        <option value="ABSENT">Absent</option>
                        <option value="LATE">Late</option>
                      </select>
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1">Marked By Professor</label>
                    <input
                      type="text"
                      value={stuForm.marked_by}
                      onChange={(e) => setStuForm({ ...stuForm, marked_by: e.target.value })}
                      className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                    />
                  </div>
                </>
              ) : (
                <>
                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1">Select Faculty</label>
                    <select
                      value={facForm.faculty_id}
                      onChange={(e) => {
                        const sel = faculty.find(f => f.id === e.target.value);
                        if (sel) {
                          setFacForm({
                            ...facForm,
                            faculty_id: sel.id,
                            faculty_name: sel.name,
                            employee_id: sel.employee_id,
                            department: sel.department
                          });
                        }
                      }}
                      className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                    >
                      {faculty.map(f => (
                        <option key={f.id} value={f.id}>{f.name} ({f.employee_id})</option>
                      ))}
                    </select>
                  </div>

                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className="block text-xs font-bold text-slate-700 mb-1">Date</label>
                      <input
                        type="date"
                        value={facForm.date}
                        onChange={(e) => setFacForm({ ...facForm, date: e.target.value })}
                        className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-bold text-slate-700 mb-1">Status</label>
                      <select
                        value={facForm.status}
                        onChange={(e) => setFacForm({ ...facForm, status: e.target.value })}
                        className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                      >
                        <option value="PRESENT">Present</option>
                        <option value="ON_LEAVE">On Leave</option>
                        <option value="HALF_DAY">Half Day</option>
                      </select>
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1">Check-in Time</label>
                    <input
                      type="text"
                      value={facForm.check_in_time}
                      onChange={(e) => setFacForm({ ...facForm, check_in_time: e.target.value })}
                      className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                    />
                  </div>
                </>
              )}

              <div className="pt-4 border-t border-slate-100 flex items-center justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setIsRecordModalOpen(false)}
                  className="px-4 py-2 text-xs font-semibold text-slate-600 bg-slate-100 hover:bg-slate-200 rounded-xl transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 text-xs font-bold text-white bg-brand-600 hover:bg-brand-700 rounded-xl shadow-sm transition-colors flex items-center gap-1.5"
                >
                  <Check className="w-4 h-4" />
                  <span>Save Attendance</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
