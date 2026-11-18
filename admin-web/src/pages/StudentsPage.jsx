import React, { useState, useRef, useEffect } from 'react';
import { 
  UserPlus, 
  Search, 
  Filter, 
  MoreVertical, 
  Edit3, 
  Trash2, 
  Phone, 
  Mail, 
  GraduationCap, 
  AlertCircle,
  X,
  Check,
  Building,
  Calendar,
  Sparkles,
  Lock,
  Eye,
  EyeOff,
  ShieldCheck,
  UploadCloud
} from 'lucide-react';
import { EmptyState } from '../components/LoadingState';
import { dataService } from '../services/dataService';
import { isValidEmail } from '../lib/validation';

const DEPARTMENTS = [
  'All Departments',
  'Computer Science & Engineering',
  'Information Technology',
  'Electronics & Communication',
  'Mechanical Engineering',
  'Civil Engineering'
];

export function StudentsPage({ 
  students, 
  onAddStudent, 
  onUpdateStudent, 
  onDeleteStudent, 
  isAddModalOpen, 
  setIsAddModalOpen, 
  onShowToast, 
  onConfirmDelete 
}) {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedDept, setSelectedDept] = useState('All Departments');
  const [selectedSem, setSelectedSem] = useState('ALL');
  const [editingStudent, setEditingStudent] = useState(null);
  const [showPassword, setShowPassword] = useState(false);

  // Photo Upload State
  const [selectedPhotoFile, setSelectedPhotoFile] = useState(null);
  const [photoPreview, setPhotoPreview] = useState(null);
  const [photoError, setPhotoError] = useState(null);
  const fileInputRef = useRef(null);

  // Form State for Add / Edit
  const [formData, setFormData] = useState({
    name: '',
    roll_number: '',
    semester: 1,
    admission_year: '',
    section: 'A',
    email: '',
    password: '',
    phone: '',
    photo_url: '',
    attendance_rate: '',
    cgpa: '',
    guardian_name: '',
    guardian_phone: '',
    address: '',
    blood_group: 'O+'
  });
  const [formErrors, setFormErrors] = useState({});
  const [emailTouched, setEmailTouched] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Keep form clean and blank when Add modal opens
  useEffect(() => {
    if (isAddModalOpen && !editingStudent) {
      setFormData({
        name: '',
        roll_number: '',
        semester: 1,
        admission_year: '',
        section: 'A',
        email: '',
        password: '',
        phone: '',
        photo_url: '',
        attendance_rate: '',
        cgpa: '',
        guardian_name: '',
        guardian_phone: '',
        address: '',
        blood_group: 'O+'
      });
      setFormErrors({});
      setEmailTouched(false);
      setShowPassword(false);
      setSelectedPhotoFile(null);
      setPhotoPreview(null);
      setPhotoError(null);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  }, [isAddModalOpen, editingStudent]);

  // Open Add Modal
  const handleOpenAdd = () => {
    setEditingStudent(null);
    setShowPassword(false);
    setSelectedPhotoFile(null);
    setPhotoPreview(null);
    setPhotoError(null);
    if (fileInputRef.current) fileInputRef.current.value = '';
    setFormData({
      name: '',
      roll_number: '',
      semester: 1,
      admission_year: '',
      section: 'A',
      email: '',
      password: '',
      phone: '',
      photo_url: '',
      attendance_rate: '',
      cgpa: '',
      guardian_name: '',
      guardian_phone: '',
      address: '',
      blood_group: 'O+'
    });
    setFormErrors({});
    setEmailTouched(false);
    setIsAddModalOpen(true);
  };

  // Open Edit Modal
  const handleOpenEdit = (student) => {
    setEditingStudent(student);
    setShowPassword(false);
    setSelectedPhotoFile(null);
    setPhotoPreview(student.photo_url || null);
    setPhotoError(null);
    if (fileInputRef.current) fileInputRef.current.value = '';
    setFormData({
      name: student.name || '',
      roll_number: student.roll_number || '',
      semester: student.semester || 1,
      admission_year: student.admission_year || '',
      section: student.section || 'A',
      email: student.email || '',
      password: '',
      phone: student.phone || '',
      photo_url: student.photo_url || '',
      attendance_rate: student.attendance_rate !== undefined && student.attendance_rate !== null ? String(student.attendance_rate) : '',
      cgpa: student.cgpa !== undefined && student.cgpa !== null ? String(student.cgpa) : '',
      guardian_name: student.guardian_name || '',
      guardian_phone: student.guardian_phone || '',
      address: student.address || '',
      blood_group: student.blood_group || 'O+'
    });
    setFormErrors({});
    setEmailTouched(false);
    setIsAddModalOpen(true);
  };

  // Photo Selection
  const handlePhotoSelect = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const validTypes = ['image/jpeg', 'image/png', 'image/webp'];
    if (!validTypes.includes(file.type)) {
      setPhotoError('Please select a JPG, PNG, or WEBP image.');
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      setPhotoError('Photo size must be less than 5MB.');
      return;
    }

    setPhotoError(null);
    setSelectedPhotoFile(file);
    const objectUrl = URL.createObjectURL(file);
    setPhotoPreview(objectUrl);
  };

  // Remove Photo
  const handleRemovePhoto = () => {
    setSelectedPhotoFile(null);
    setPhotoPreview(null);
    setPhotoError(null);
    setFormData(prev => ({ ...prev, photo_url: '' }));
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  // Validation
  const validateForm = () => {
    const errors = {};
    if (!formData.name.trim()) errors.name = 'Student Name is required.';
    if (!formData.roll_number.trim()) errors.roll_number = 'Roll / Enrollment number is required.';
    // Department / Branch removed completely - no validation

    if (!formData.email.trim() || !isValidEmail(formData.email)) {
      errors.email = 'Please enter a valid email address.';
    }

    // Require password for new accounts (min 8 characters, letters and numbers)
    if (!editingStudent) {
      if (!formData.password || !formData.password.trim()) {
        errors.password = 'Student login password is required.';
      } else if (formData.password.length < 8) {
        errors.password = 'Password must be at least 8 characters long.';
      } else if (!/[A-Za-z]/.test(formData.password) || !/[0-9]/.test(formData.password)) {
        errors.password = 'Password must contain both letters and numbers.';
      }
    }

    if (!formData.phone.trim()) errors.phone = 'Phone number is required.';

    // Attendance validation (0 to 100)
    if (formData.attendance_rate !== '' && formData.attendance_rate !== null) {
      const att = parseFloat(formData.attendance_rate);
      if (isNaN(att) || att < 0 || att > 100) {
        errors.attendance_rate = 'Attendance % must be between 0 and 100.';
      }
    }

    // CGPA validation (0.0 to 10.0)
    if (formData.cgpa !== '' && formData.cgpa !== null) {
      const cg = parseFloat(formData.cgpa);
      if (isNaN(cg) || cg < 0 || cg > 10) {
        errors.cgpa = 'CGPA must be between 0.0 and 10.0.';
      }
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Submit Add / Edit
  const handleSubmit = async (e) => {
    e.preventDefault();
    setEmailTouched(true);
    if (!validateForm()) return;

    setIsSubmitting(true);
    try {
      let finalPhotoUrl = formData.photo_url || '';

      // Upload photo to Supabase Storage if a new file was chosen
      if (selectedPhotoFile) {
        const uploadRes = await dataService.uploadStudentPhoto(selectedPhotoFile);
        if (uploadRes.url) {
          finalPhotoUrl = uploadRes.url;
        } else {
          console.warn('Supabase storage notice:', uploadRes.error);
        }
      }

      // Convert attendance and CGPA to proper numeric values
      const numericAttendance = formData.attendance_rate !== '' && !isNaN(parseFloat(formData.attendance_rate))
        ? parseFloat(formData.attendance_rate)
        : (editingStudent ? (editingStudent.attendance_rate ?? 85) : 85);

      const numericCgpa = formData.cgpa !== '' && !isNaN(parseFloat(formData.cgpa))
        ? parseFloat(formData.cgpa)
        : (editingStudent ? (editingStudent.cgpa ?? 8.0) : 8.0);

      const studentPayload = {
        ...formData,
        photo_url: finalPhotoUrl,
        attendance_rate: numericAttendance,
        cgpa: numericCgpa
      };

      if (editingStudent) {
        await onUpdateStudent(editingStudent.id, studentPayload);
        onShowToast('success', 'Student Updated', `${formData.name} was successfully updated in Supabase.`);
      } else {
        const result = await onAddStudent(studentPayload);
        if (result?.authCreated) {
          onShowToast('success', 'Student Enrolled & Auth Created', `${formData.name} registered in Supabase Auth (role: student) and profile saved.`);
        } else if (result?.authError) {
          onShowToast('warning', 'Auth Warning: ' + result.authError, `${formData.name} saved in table, but Supabase Auth failed: "${result.authError}". Disable 'Confirm email' in Supabase Dashboard or run SQL RPC.`);
        } else {
          onShowToast('success', 'Student Added', `${formData.name} has been enrolled and saved to Supabase.`);
        }
      }
      setIsAddModalOpen(false);
    } catch (err) {
      onShowToast('error', 'Operation Failed', err.message || 'Could not save student.');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Filter students
  const filteredStudents = students.filter((s) => {
    const matchesSearch = 
      (s.name && s.name.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (s.roll_number && s.roll_number.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (s.email && s.email.toLowerCase().includes(searchQuery.toLowerCase()));

    const matchesDept = selectedDept === 'All Departments' || s.department === selectedDept;
    const matchesSem = selectedSem === 'ALL' || String(s.semester) === String(selectedSem);

    return matchesSearch && matchesDept && matchesSem;
  });

  return (
    <div className="space-y-6">
      {/* Top action row */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-4">
        {/* Search Bar */}
        <div className="relative flex-1 max-w-md">
          <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search by student name, roll no, email..."
            className="w-full pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 shadow-xs placeholder:text-slate-400"
          />
        </div>

        {/* Filters and Add button */}
        <div className="flex items-center gap-2.5">
          <select
            value={selectedDept}
            onChange={(e) => setSelectedDept(e.target.value)}
            className="px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-700 focus:outline-none focus:border-brand-500 shadow-xs font-medium"
          >
            {DEPARTMENTS.map((dept) => (
              <option key={dept} value={dept}>{dept}</option>
            ))}
          </select>

          <select
            value={selectedSem}
            onChange={(e) => setSelectedSem(e.target.value)}
            className="px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-700 focus:outline-none focus:border-brand-500 shadow-xs font-medium"
          >
            <option value="ALL">All Semesters</option>
            {[1, 2, 3, 4, 5, 6, 7, 8].map((sem) => (
              <option key={sem} value={sem}>Semester {sem}</option>
            ))}
          </select>

          <button
            onClick={handleOpenAdd}
            className="px-4 py-2.5 bg-brand-600 hover:bg-brand-700 text-white text-xs font-bold rounded-xl shadow-sm transition-all flex items-center gap-1.5 shrink-0"
          >
            <UserPlus className="w-4 h-4" />
            <span>Add Student</span>
          </button>
        </div>
      </div>

      {/* Students Table */}
      {filteredStudents.length === 0 ? (
        <EmptyState
          icon={GraduationCap}
          title="No Students Found"
          description={searchQuery ? "No enrolled students matched your search criteria." : "There are no students registered yet."}
          actionText="Add New Student"
          onAction={handleOpenAdd}
        />
      ) : (
        <div className="bg-white rounded-2xl border border-slate-200/80 overflow-hidden shadow-xs">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50/70 border-b border-slate-100 text-[11px] font-bold text-slate-500 uppercase tracking-wider">
                  <th className="py-3.5 px-4">Student Details</th>
                  <th className="py-3.5 px-4">Department & Section</th>
                  <th className="py-3.5 px-4">Sem / Year</th>
                  <th className="py-3.5 px-4">Attendance Standing</th>
                  <th className="py-3.5 px-4">Contact Info</th>
                  <th className="py-3.5 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-xs">
                {filteredStudents.map((s) => {
                  const isLowAttendance = s.attendance_rate < 75;
                  return (
                    <tr key={s.id} className="hover:bg-slate-50/60 transition-colors group">
                      {/* Student info with avatar */}
                      <td className="py-3.5 px-4">
                        <div className="flex items-center gap-3">
                          <img
                            src={s.photo_url || `https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200`}
                            alt={s.name}
                            className="w-10 h-10 rounded-full object-cover border border-slate-200 shadow-2xs shrink-0"
                            onError={(e) => {
                              e.target.src = 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200';
                            }}
                          />
                          <div>
                            <div className="font-bold text-slate-900 group-hover:text-brand-600 transition-colors">
                              {s.name}
                            </div>
                            <div className="font-mono text-[11px] text-slate-500 mt-0.5">
                              {s.roll_number}
                            </div>
                          </div>
                        </div>
                      </td>

                      {/* Department */}
                      <td className="py-3.5 px-4">
                        <div className="font-medium text-slate-800">{s.department}</div>
                        <div className="text-[11px] text-slate-500 mt-0.5">Section {s.section || 'A'}</div>
                      </td>

                      {/* Semester */}
                      <td className="py-3.5 px-4">
                        <span className="inline-block px-2 py-0.5 rounded-full text-[11px] font-semibold bg-slate-100 text-slate-700">
                          Sem {s.semester} (Yr {Math.ceil((s.semester || 1) / 2)})
                        </span>
                        {s.cgpa && (
                          <div className="text-[11px] font-semibold text-slate-600 mt-1">
                            CGPA: <span className="text-slate-900">{s.cgpa}</span>
                          </div>
                        )}
                      </td>

                      {/* Attendance */}
                      <td className="py-3.5 px-4">
                        <div className="flex items-center gap-2">
                          <div className="w-16 bg-slate-100 rounded-full h-2 overflow-hidden">
                            <div
                              className={`h-full rounded-full ${
                                isLowAttendance ? 'bg-rose-500' : 'bg-emerald-500'
                              }`}
                              style={{ width: `${Math.min(s.attendance_rate || 0, 100)}%` }}
                            />
                          </div>
                          <span className={`text-xs font-bold ${
                            isLowAttendance ? 'text-rose-600' : 'text-emerald-700'
                          }`}>
                            {s.attendance_rate || 0}%
                          </span>
                        </div>
                        {isLowAttendance && (
                          <span className="text-[10px] text-rose-600 font-semibold flex items-center gap-0.5 mt-0.5">
                            <AlertCircle className="w-3 h-3" />
                            <span>Defaulter (&lt;75%)</span>
                          </span>
                        )}
                      </td>

                      {/* Contact */}
                      <td className="py-3.5 px-4 space-y-0.5">
                        <div className="flex items-center gap-1.5 text-slate-600">
                          <Mail className="w-3 h-3 text-slate-400" />
                          <span className="truncate max-w-[150px]">{s.email || '—'}</span>
                        </div>
                        <div className="flex items-center gap-1.5 text-slate-500 text-[11px]">
                          <Phone className="w-3 h-3 text-slate-400" />
                          <span>{s.phone || '—'}</span>
                        </div>
                      </td>

                      {/* Actions */}
                      <td className="py-3.5 px-4 text-right">
                        <div className="flex items-center justify-end gap-1.5">
                          <button
                            onClick={() => handleOpenEdit(s)}
                            className="p-1.5 rounded-lg text-slate-500 hover:text-brand-600 hover:bg-brand-50 transition-colors"
                            title="Edit Student"
                          >
                            <Edit3 className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => {
                              onConfirmDelete(
                                'Delete Student',
                                `Are you sure you want to remove ${s.name} (${s.roll_number}) from the system?`,
                                () => onDeleteStudent(s.id)
                              );
                            }}
                            className="p-1.5 rounded-lg text-slate-500 hover:text-rose-600 hover:bg-rose-50 transition-colors"
                            title="Delete Student"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          <div className="px-5 py-3 border-t border-slate-100 bg-slate-50/50 flex items-center justify-between text-xs text-slate-500 font-medium">
            <span>Showing {filteredStudents.length} of {students.length} students</span>
            <span className="font-mono text-[11px]">Stored in Supabase `students`</span>
          </div>
        </div>
      )}

      {/* Add / Edit Student Modal */}
      {isAddModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-2xl max-w-2xl w-full max-h-[90vh] flex flex-col shadow-2xl border border-slate-100 overflow-hidden">
            {/* Modal Header */}
            <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-brand-50 border border-brand-100 flex items-center justify-center text-brand-600">
                  <UserPlus className="w-5 h-5" />
                </div>
                <div>
                  <h2 className="text-base font-bold text-slate-900">
                    {editingStudent ? 'Edit Student Details' : 'Enroll New Student'}
                  </h2>
                  <p className="text-xs text-slate-500">
                    {editingStudent ? 'Update details in Supabase' : 'Add record to Supabase students table'}
                  </p>
                </div>
              </div>
              <button
                onClick={() => setIsAddModalOpen(false)}
                className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Modal Form Body */}
            <form onSubmit={handleSubmit} className="p-6 space-y-4 overflow-y-auto">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {/* Full Name */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Full Name <span className="text-rose-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    className={`w-full px-3 py-2 bg-white border rounded-xl text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20 ${
                      formErrors.name ? 'border-rose-400' : 'border-slate-200 focus:border-brand-500'
                    }`}
                  />
                  {formErrors.name && <p className="text-[11px] text-rose-500 mt-1">{formErrors.name}</p>}
                </div>

                {/* Roll Number */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Roll / Enrollment Number <span className="text-rose-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={formData.roll_number}
                    onChange={(e) => setFormData({ ...formData, roll_number: e.target.value })}
                    className={`w-full px-3 py-2 bg-white border rounded-xl text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20 font-mono ${
                      formErrors.roll_number ? 'border-rose-400' : 'border-slate-200 focus:border-brand-500'
                    }`}
                  />
                  {formErrors.roll_number && <p className="text-[11px] text-rose-500 mt-1">{formErrors.roll_number}</p>}
                </div>

                {/* Semester */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">Semester</label>
                  <select
                    value={formData.semester}
                    onChange={(e) => setFormData({ ...formData, semester: parseInt(e.target.value) || 1 })}
                    className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                  >
                    {[1, 2, 3, 4, 5, 6, 7, 8].map(sem => (
                      <option key={sem} value={sem}>Sem {sem}</option>
                    ))}
                  </select>
                </div>

                {/* Admission Year */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">Admission Year</label>
                  <input
                    type="text"
                    value={formData.admission_year}
                    onChange={(e) => setFormData({ ...formData, admission_year: e.target.value })}
                    className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                  />
                </div>

                {/* Email */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Email Address <span className="text-rose-500">*</span>
                  </label>
                  <input
                    type="email"
                    value={formData.email}
                    onChange={(e) => {
                      const val = e.target.value;
                      setEmailTouched(true);
                      setFormData(prev => ({ ...prev, email: val }));
                      if (!isValidEmail(val)) {
                        setFormErrors(prev => ({ ...prev, email: 'Please enter a valid email address.' }));
                      } else {
                        setFormErrors(prev => {
                          const next = { ...prev };
                          delete next.email;
                          return next;
                        });
                      }
                    }}
                    onBlur={() => {
                      setEmailTouched(true);
                      if (!isValidEmail(formData.email)) {
                        setFormErrors(prev => ({ ...prev, email: 'Please enter a valid email address.' }));
                      }
                    }}
                    className={`w-full px-3 py-2 bg-white border rounded-xl text-xs text-slate-800 focus:outline-none focus:ring-2 ${
                      (emailTouched || formErrors.email) && !isValidEmail(formData.email)
                        ? 'border-rose-500 ring-1 ring-rose-500 focus:border-rose-500'
                        : 'border-slate-200 focus:border-brand-500 focus:ring-brand-500/20'
                    }`}
                  />
                  {(emailTouched || formErrors.email) && !isValidEmail(formData.email) && (
                    <p className="text-[11px] text-rose-500 mt-1">Please enter a valid email address.</p>
                  )}
                </div>

                {/* Password (for Add Student / Supabase Auth) */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Student Login Password {editingStudent ? '(Leave blank to keep unchanged)' : <span className="text-rose-500">*</span>}
                  </label>
                  <div className="relative">
                    <input
                      type={showPassword ? 'text' : 'password'}
                      value={formData.password}
                      onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                      className={`w-full pl-3 pr-10 py-2 bg-white border rounded-xl text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20 ${
                        formErrors.password ? 'border-rose-400' : 'border-slate-200 focus:border-brand-500'
                      }`}
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 p-1"
                    >
                      {showPassword ? <EyeOff className="w-3.5 h-3.5" /> : <Eye className="w-3.5 h-3.5" />}
                    </button>
                  </div>
                  {formErrors.password && <p className="text-[11px] text-rose-500 mt-1">{formErrors.password}</p>}
                  {!formErrors.password && !editingStudent && (
                    <p className="text-[10px] text-slate-400 mt-1">Min 6 characters. Configures Supabase Auth login.</p>
                  )}
                </div>

                {/* Phone */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Phone Number <span className="text-rose-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={formData.phone}
                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                    className={`w-full px-3 py-2 bg-white border rounded-xl text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20 ${
                      formErrors.phone ? 'border-rose-400' : 'border-slate-200 focus:border-brand-500'
                    }`}
                  />
                  {formErrors.phone && <p className="text-[11px] text-rose-500 mt-1">{formErrors.phone}</p>}
                </div>

                {/* Upload Student Photo (sm:col-span-2) */}
                <div className="sm:col-span-2">
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Upload Student Photo
                  </label>
                  <input
                    type="file"
                    ref={fileInputRef}
                    onChange={handlePhotoSelect}
                    accept="image/jpeg,image/png,image/webp"
                    className="hidden"
                  />

                  {photoPreview ? (
                    <div className="flex items-center gap-4 p-3 bg-slate-50 border border-slate-200 rounded-xl">
                      <img
                        src={photoPreview}
                        alt="Student Preview"
                        className="w-14 h-14 rounded-xl object-cover border border-slate-200 shadow-xs shrink-0"
                        onError={(e) => {
                          e.target.src = 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200';
                        }}
                      />
                      <div className="flex-1 min-w-0">
                        <div className="text-xs font-bold text-slate-800 truncate">
                          {selectedPhotoFile ? selectedPhotoFile.name : 'Current Photo'}
                        </div>
                        <div className="text-[11px] text-slate-500 mt-0.5">
                          {selectedPhotoFile ? `${(selectedPhotoFile.size / 1024).toFixed(1)} KB` : 'Stored in Supabase Storage'}
                        </div>
                        <div className="flex items-center gap-3 mt-2">
                          <button
                            type="button"
                            onClick={() => fileInputRef.current?.click()}
                            className="text-xs font-semibold text-brand-600 hover:text-brand-700 hover:underline"
                          >
                            Change Photo
                          </button>
                          <button
                            type="button"
                            onClick={handleRemovePhoto}
                            className="text-xs font-semibold text-rose-600 hover:text-rose-700 hover:underline flex items-center gap-1"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                            <span>Remove</span>
                          </button>
                        </div>
                      </div>
                    </div>
                  ) : (
                    <div
                      onClick={() => fileInputRef.current?.click()}
                      className="border-2 border-dashed border-slate-200 hover:border-brand-400 bg-slate-50/50 hover:bg-slate-50 rounded-xl p-4 text-center cursor-pointer transition-colors"
                    >
                      <div className="w-10 h-10 rounded-full bg-brand-50 text-brand-600 flex items-center justify-center mx-auto mb-2">
                        <UploadCloud className="w-5 h-5" />
                      </div>
                      <div className="text-xs font-bold text-slate-700">
                        Click to upload student photo
                      </div>
                      <p className="text-[11px] text-slate-400 mt-0.5">
                        Supports JPG, PNG, or WEBP (Max 5MB)
                      </p>
                    </div>
                  )}

                  {photoError && <p className="text-[11px] text-rose-500 mt-1">{photoError}</p>}
                </div>

                {/* Current Attendance % */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Current Attendance %
                  </label>
                  <input
                    type="text"
                    inputMode="decimal"
                    value={formData.attendance_rate}
                    onChange={(e) => {
                      const val = e.target.value;
                      if (val === '' || /^\d*\.?\d*$/.test(val)) {
                        setFormData({ ...formData, attendance_rate: val });
                        if (formErrors.attendance_rate) {
                          setFormErrors({ ...formErrors, attendance_rate: null });
                        }
                      }
                    }}
                    className={`w-full px-3 py-2 bg-white border rounded-xl text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20 ${
                      formErrors.attendance_rate ? 'border-rose-400' : 'border-slate-200 focus:border-brand-500'
                    }`}
                  />
                  {formErrors.attendance_rate && (
                    <p className="text-[11px] text-rose-500 mt-1">{formErrors.attendance_rate}</p>
                  )}
                </div>

                {/* CGPA Standing */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    CGPA Standing
                  </label>
                  <input
                    type="text"
                    inputMode="decimal"
                    value={formData.cgpa}
                    onChange={(e) => {
                      const val = e.target.value;
                      if (val === '' || /^\d*\.?\d*$/.test(val)) {
                        setFormData({ ...formData, cgpa: val });
                        if (formErrors.cgpa) {
                          setFormErrors({ ...formErrors, cgpa: null });
                        }
                      }
                    }}
                    className={`w-full px-3 py-2 bg-white border rounded-xl text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20 ${
                      formErrors.cgpa ? 'border-rose-400' : 'border-slate-200 focus:border-brand-500'
                    }`}
                  />
                  {formErrors.cgpa && (
                    <p className="text-[11px] text-rose-500 mt-1">{formErrors.cgpa}</p>
                  )}
                </div>

                {/* Guardian Details */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">Guardian Name</label>
                  <input
                    type="text"
                    value={formData.guardian_name}
                    onChange={(e) => setFormData({ ...formData, guardian_name: e.target.value })}
                    className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">Guardian Contact</label>
                  <input
                    type="text"
                    value={formData.guardian_phone}
                    onChange={(e) => setFormData({ ...formData, guardian_phone: e.target.value })}
                    className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                  />
                </div>
              </div>

              {/* Modal Actions */}
              <div className="pt-4 border-t border-slate-100 flex items-center justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setIsAddModalOpen(false)}
                  className="px-4 py-2 text-xs font-semibold text-slate-600 bg-slate-100 hover:bg-slate-200 rounded-xl transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="px-5 py-2 text-xs font-bold text-white bg-brand-600 hover:bg-brand-700 rounded-xl shadow-sm transition-colors flex items-center gap-1.5"
                >
                  <Check className="w-4 h-4" />
                  <span>{isSubmitting ? 'Saving to Supabase...' : editingStudent ? 'Update Student' : 'Save Student'}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
