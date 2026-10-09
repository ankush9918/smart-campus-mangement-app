import React, { useState, useRef, useEffect } from 'react';
import { 
  Users, 
  Search, 
  Filter, 
  Edit3, 
  Trash2, 
  Phone, 
  Mail, 
  Linkedin, 
  Award, 
  BookOpen, 
  MapPin, 
  X, 
  Check, 
  Building,
  UserPlus,
  Lock,
  Eye,
  EyeOff,
  UploadCloud
} from 'lucide-react';
import { EmptyState } from '../components/LoadingState';
import { dataService, MASTER_SUBJECTS, normalizeSubject } from '../services/dataService';

const DEPARTMENTS = [
  'All Departments',
  'Information Technology',
  'Computer Science & Engineering',
  'Electronics & Communication',
  'Mechanical Engineering',
  'Civil Engineering'
];

const DESIGNATIONS = [
  'Professor & HOD',
  'Professor',
  'Associate Professor',
  'Assistant Professor',
  'Adjunct Professor',
  'Lecturer'
];

export function FacultyPage({ 
  faculty, 
  onAddFaculty, 
  onUpdateFaculty, 
  onDeleteFaculty, 
  isAddModalOpen, 
  setIsAddModalOpen, 
  onShowToast, 
  onConfirmDelete 
}) {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedDept, setSelectedDept] = useState('All Departments');
  const [editingFaculty, setEditingFaculty] = useState(null);
  const [showPassword, setShowPassword] = useState(false);

  // Photo Upload State
  const [selectedPhotoFile, setSelectedPhotoFile] = useState(null);
  const [photoPreview, setPhotoPreview] = useState(null);
  const [photoError, setPhotoError] = useState(null);
  const fileInputRef = useRef(null);

  // Form State
  const [formData, setFormData] = useState({
    name: '',
    employee_id: '',
    designation: 'Assistant Professor',
    qualification: '',
    department: 'Information Technology',
    email: '',
    password: '',
    phone: '',
    linkedin: '',
    photo_url: '',
    cabin_room: '',
    experience_years: '',
    subjects: ''
  });
  const [formErrors, setFormErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Keep form clean and blank when Add modal opens
  useEffect(() => {
    if (isAddModalOpen && !editingFaculty) {
      setFormData({
        name: '',
        employee_id: '',
        designation: 'Assistant Professor',
        qualification: '',
        department: 'Information Technology',
        email: '',
        password: '',
        phone: '',
        linkedin: '',
        photo_url: '',
        cabin_room: '',
        experience_years: '',
        subjects: ''
      });
      setFormErrors({});
      setShowPassword(false);
      setSelectedPhotoFile(null);
      setPhotoPreview(null);
      setPhotoError(null);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  }, [isAddModalOpen, editingFaculty]);

  // Open Add Modal
  const handleOpenAdd = () => {
    setEditingFaculty(null);
    setShowPassword(false);
    setSelectedPhotoFile(null);
    setPhotoPreview(null);
    setPhotoError(null);
    if (fileInputRef.current) fileInputRef.current.value = '';
    setFormData({
      name: '',
      employee_id: '',
      designation: 'Assistant Professor',
      qualification: '',
      department: 'Information Technology',
      email: '',
      password: '',
      phone: '',
      linkedin: '',
      photo_url: '',
      cabin_room: '',
      experience_years: '',
      subjects: ''
    });
    setFormErrors({});
    setIsAddModalOpen(true);
  };

  // Open Edit Modal
  const handleOpenEdit = (f) => {
    setEditingFaculty(f);
    setShowPassword(false);
    setSelectedPhotoFile(null);
    setPhotoPreview(f.photo_url || null);
    setPhotoError(null);
    if (fileInputRef.current) fileInputRef.current.value = '';

    let subjsStr = '';
    if (Array.isArray(f.subjects)) {
      subjsStr = f.subjects.join(', ');
    } else if (typeof f.subjects === 'string') {
      try {
        const parsed = JSON.parse(f.subjects);
        subjsStr = Array.isArray(parsed) ? parsed.join(', ') : f.subjects;
      } catch (e) {
        subjsStr = f.subjects;
      }
    }

    setFormData({
      name: f.name || '',
      employee_id: f.employee_id || '',
      designation: f.designation || 'Associate Professor',
      qualification: f.qualification || '',
      department: f.department || 'Information Technology',
      email: f.email || '',
      password: '',
      phone: f.phone || '',
      linkedin: f.linkedin || '',
      photo_url: f.photo_url || '',
      cabin_room: f.cabin_room || '',
      experience_years: f.experience_years !== undefined && f.experience_years !== null ? f.experience_years : '',
      subjects: subjsStr
    });
    setFormErrors({});
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
    setFormData((prev) => ({ ...prev, photo_url: '' }));
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  // Helper to check if a Master Subject is selected
  const isSubjectSelected = (masterSubj) => {
    if (!formData.subjects) return false;
    const parts = formData.subjects.split(',').map(s => s.trim().toLowerCase());
    return parts.some(p => 
      p === masterSubj.code.toLowerCase() ||
      p === masterSubj.name.toLowerCase() ||
      p === masterSubj.displayName.toLowerCase() ||
      p.includes(masterSubj.code.toLowerCase()) ||
      p.includes(masterSubj.name.toLowerCase())
    );
  };

  // Toggle selection of a Master Subject in the form
  const toggleSubject = (masterSubj) => {
    const isSelected = isSubjectSelected(masterSubj);
    let parts = formData.subjects ? formData.subjects.split(',').map(s => s.trim()).filter(Boolean) : [];
    if (isSelected) {
      parts = parts.filter(p => {
        const lower = p.toLowerCase();
        return (
          lower !== masterSubj.code.toLowerCase() &&
          lower !== masterSubj.name.toLowerCase() &&
          lower !== masterSubj.displayName.toLowerCase() &&
          !lower.includes(masterSubj.code.toLowerCase()) &&
          !lower.includes(masterSubj.name.toLowerCase())
        );
      });
    } else {
      parts.push(masterSubj.displayName);
    }
    setFormData(prev => ({ ...prev, subjects: parts.join(', ') }));
  };

  // Validation
  const validateForm = () => {
    const errors = {};
    if (!formData.name.trim()) errors.name = 'Faculty Name is required.';
    if (!formData.employee_id.trim()) errors.employee_id = 'Employee ID is required.';
    if (!formData.designation.trim()) errors.designation = 'Designation is required.';
    if (!formData.qualification.trim()) errors.qualification = 'Graduation / Education details are required.';
    if (!formData.department) errors.department = 'Department is required.';
    if (!formData.email.trim()) {
      errors.email = 'Email address is required.';
    } else if (!formData.email.includes('@')) {
      errors.email = 'Valid email is required.';
    }

    // Require password for new accounts (min 6 characters)
    if (!editingFaculty) {
      if (!formData.password || !formData.password.trim()) {
        errors.password = 'Professor login password is required.';
      } else if (formData.password.length < 6) {
        errors.password = 'Password must be at least 6 characters.';
      }
    }

    if (!formData.phone.trim()) errors.phone = 'Phone number is required.';

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Submit Add / Edit
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsSubmitting(true);
    try {
      let finalPhotoUrl = formData.photo_url || '';

      // Upload photo to Supabase Storage if a new file was chosen
      if (selectedPhotoFile) {
        const uploadRes = await dataService.uploadFacultyPhoto(selectedPhotoFile);
        if (uploadRes.url) {
          finalPhotoUrl = uploadRes.url;
        } else {
          console.warn('Supabase storage faculty notice:', uploadRes.error);
        }
      }

      // Map each subject to its canonical Master Subject name (or keep trimmed string)
      const subjectsArray = formData.subjects
        ? formData.subjects.split(',').map((s) => {
            const trimmed = s.trim();
            const normalized = normalizeSubject(trimmed);
            return normalized ? normalized.displayName : trimmed;
          }).filter(Boolean)
        : [];

      const payload = {
        name: formData.name,
        employee_id: formData.employee_id,
        designation: formData.designation,
        qualification: formData.qualification,
        department: formData.department,
        email: formData.email,
        password: formData.password,
        phone: formData.phone,
        linkedin: formData.linkedin,
        photo_url: finalPhotoUrl,
        cabin_room: formData.cabin_room,
        experience_years: formData.experience_years !== '' && !isNaN(parseInt(formData.experience_years)) ? parseInt(formData.experience_years) : 0,
        subjects: subjectsArray
      };

      if (editingFaculty) {
        await onUpdateFaculty(editingFaculty.id, payload);
        onShowToast('success', 'Faculty Updated', `${formData.name} updated successfully in Supabase.`);
      } else {
        const result = await onAddFaculty(payload);
        if (result?.authCreated) {
          onShowToast('success', 'Faculty Added & Auth Created', `${formData.name} registered in Supabase Auth (role: professor) and profile saved.`);
        } else if (result?.authError) {
          onShowToast('warning', 'Auth Warning: ' + result.authError, `${formData.name} saved in table, but Supabase Auth failed: "${result.authError}". Disable 'Confirm email' in Supabase Dashboard or run SQL RPC.`);
        } else {
          onShowToast('success', 'Faculty Added', `${formData.name} added to Supabase faculty table.`);
        }
      }
      setIsAddModalOpen(false);
    } catch (err) {
      onShowToast('error', 'Operation Failed', err.message || 'Could not save faculty.');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Filter
  const filteredFaculty = faculty.filter((f) => {
    const matchesSearch =
      (f.name && f.name.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (f.employee_id && f.employee_id.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (f.designation && f.designation.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (f.email && f.email.toLowerCase().includes(searchQuery.toLowerCase()));

    const matchesDept = selectedDept === 'All Departments' || f.department === selectedDept;

    return matchesSearch && matchesDept;
  });

  return (
    <div className="space-y-6">
      {/* Top action row */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-4">
        {/* Search */}
        <div className="relative flex-1 max-w-md">
          <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search by faculty name, designation, employee ID..."
            className="w-full pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 shadow-xs placeholder:text-slate-400"
          />
        </div>

        {/* Filter and Add */}
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

          <button
            onClick={handleOpenAdd}
            className="px-4 py-2.5 bg-brand-600 hover:bg-brand-700 text-white text-xs font-bold rounded-xl shadow-sm transition-all flex items-center gap-1.5 shrink-0"
          >
            <UserPlus className="w-4 h-4" />
            <span>Add Faculty</span>
          </button>
        </div>
      </div>

      {/* Faculty Table */}
      {filteredFaculty.length === 0 ? (
        <EmptyState
          icon={Users}
          title="No Faculty Members Found"
          description={searchQuery ? "No faculty members match your search criteria." : "There are no faculty members registered yet."}
          actionText="Add New Faculty"
          onAction={handleOpenAdd}
        />
      ) : (
        <div className="bg-white rounded-2xl border border-slate-200/80 overflow-hidden shadow-xs">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50/70 border-b border-slate-100 text-[11px] font-bold text-slate-500 uppercase tracking-wider">
                  <th className="py-3.5 px-4">Faculty Member</th>
                  <th className="py-3.5 px-4">Designation & Education</th>
                  <th className="py-3.5 px-4">Department & Cabin</th>
                  <th className="py-3.5 px-4">Contact & Social</th>
                  <th className="py-3.5 px-4">Teaching Subjects</th>
                  <th className="py-3.5 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-xs">
                {filteredFaculty.map((f) => {
                  let subjsList = [];
                  if (Array.isArray(f.subjects)) {
                    subjsList = f.subjects;
                  } else if (typeof f.subjects === 'string') {
                    try {
                      subjsList = JSON.parse(f.subjects);
                    } catch (e) {
                      subjsList = [f.subjects];
                    }
                  }

                  return (
                    <tr key={f.id} className="hover:bg-slate-50/60 transition-colors group">
                      {/* Avatar and name */}
                      <td className="py-3.5 px-4">
                        <div className="flex items-center gap-3">
                          <img
                            src={f.photo_url || `https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=200`}
                            alt={f.name}
                            className="w-10 h-10 rounded-full object-cover border border-slate-200 shadow-2xs shrink-0"
                            onError={(e) => {
                              e.target.src = 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=200';
                            }}
                          />
                          <div>
                            <div className="font-bold text-slate-900 group-hover:text-brand-600 transition-colors">
                              {f.name}
                            </div>
                            <div className="font-mono text-[11px] text-slate-500 mt-0.5">
                              {f.employee_id}
                            </div>
                          </div>
                        </div>
                      </td>

                      {/* Designation & Qualification */}
                      <td className="py-3.5 px-4">
                        <div className="font-bold text-slate-800">{f.designation}</div>
                        <div className="text-[11px] text-slate-500 mt-0.5 flex items-center gap-1">
                          <Award className="w-3 h-3 text-brand-600 shrink-0" />
                          <span className="truncate max-w-[170px]">{f.qualification}</span>
                        </div>
                      </td>

                      {/* Department & Cabin */}
                      <td className="py-3.5 px-4">
                        <div className="font-medium text-slate-800">{f.department}</div>
                        <div className="text-[11px] text-slate-500 mt-0.5 flex items-center gap-1">
                          <MapPin className="w-3 h-3 text-slate-400 shrink-0" />
                          <span>{f.cabin_room || 'Academic Block'}</span>
                        </div>
                      </td>

                      {/* Contact & Social */}
                      <td className="py-3.5 px-4 space-y-1">
                        <div className="flex items-center gap-1.5 text-slate-600">
                          <Mail className="w-3 h-3 text-slate-400" />
                          <span className="truncate max-w-[150px]">{f.email || '—'}</span>
                        </div>
                        <div className="flex items-center gap-1.5 text-slate-500 text-[11px]">
                          <Phone className="w-3 h-3 text-slate-400" />
                          <span>{f.phone || '—'}</span>
                        </div>
                        {f.linkedin && (
                          <div className="flex items-center gap-1.5 text-blue-600 text-[11px]">
                            <Linkedin className="w-3 h-3 text-blue-500" />
                            <a href={f.linkedin} target="_blank" rel="noreferrer" className="hover:underline truncate max-w-[140px]">
                              LinkedIn Profile
                            </a>
                          </div>
                        )}
                      </td>

                      {/* Subjects */}
                      <td className="py-3.5 px-4">
                        <div className="flex flex-wrap gap-1 max-w-[200px]">
                          {subjsList.slice(0, 2).map((s, idx) => (
                            <span key={idx} className="text-[10px] bg-slate-100 text-slate-700 px-2 py-0.5 rounded font-medium">
                              {s}
                            </span>
                          ))}
                          {subjsList.length > 2 && (
                            <span className="text-[10px] bg-slate-100 text-slate-500 px-1.5 py-0.5 rounded font-medium">
                              +{subjsList.length - 2} more
                            </span>
                          )}
                        </div>
                      </td>

                      {/* Actions */}
                      <td className="py-3.5 px-4 text-right">
                        <div className="flex items-center justify-end gap-1.5">
                          <button
                            onClick={() => handleOpenEdit(f)}
                            className="p-1.5 rounded-lg text-slate-500 hover:text-brand-600 hover:bg-brand-50 transition-colors"
                            title="Edit Faculty Member"
                          >
                            <Edit3 className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => {
                              onConfirmDelete(
                                'Delete Faculty Member',
                                `Are you sure you want to remove ${f.name} (${f.employee_id}) from the faculty directory?`,
                                () => onDeleteFaculty(f.id)
                              );
                            }}
                            className="p-1.5 rounded-lg text-slate-500 hover:text-rose-600 hover:bg-rose-50 transition-colors"
                            title="Delete Faculty Member"
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
            <span>Showing {filteredFaculty.length} of {faculty.length} faculty members</span>
            <span className="font-mono text-[11px]">Stored in Supabase `faculty`</span>
          </div>
        </div>
      )}

      {/* Add / Edit Faculty Modal */}
      {isAddModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-2xl max-w-2xl w-full max-h-[90vh] flex flex-col shadow-2xl border border-slate-100 overflow-hidden">
            {/* Modal Header */}
            <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-purple-50 border border-purple-100 flex items-center justify-center text-purple-600">
                  <UserPlus className="w-5 h-5" />
                </div>
                <div>
                  <h2 className="text-base font-bold text-slate-900">
                    {editingFaculty ? 'Edit Faculty Member' : 'Register New Faculty'}
                  </h2>
                  <p className="text-xs text-slate-500">
                    {editingFaculty ? 'Update record in Supabase' : 'Add record to Supabase faculty table'}
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

            {/* Modal Form */}
            <form onSubmit={handleSubmit} className="p-6 space-y-4 overflow-y-auto">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {/* Full Name */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Faculty Full Name <span className="text-rose-500">*</span>
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

                {/* Employee ID */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Employee ID <span className="text-rose-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={formData.employee_id}
                    onChange={(e) => setFormData({ ...formData, employee_id: e.target.value })}
                    className={`w-full px-3 py-2 bg-white border rounded-xl text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20 font-mono ${
                      formErrors.employee_id ? 'border-rose-400' : 'border-slate-200 focus:border-brand-500'
                    }`}
                  />
                  {formErrors.employee_id && <p className="text-[11px] text-rose-500 mt-1">{formErrors.employee_id}</p>}
                </div>

                {/* Designation */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Designation <span className="text-rose-500">*</span>
                  </label>
                  <select
                    value={formData.designation}
                    onChange={(e) => setFormData({ ...formData, designation: e.target.value })}
                    className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                  >
                    {DESIGNATIONS.map(d => (
                      <option key={d} value={d}>{d}</option>
                    ))}
                  </select>
                </div>

                {/* Qualification / Education */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Graduation / Education Details <span className="text-rose-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={formData.qualification}
                    onChange={(e) => setFormData({ ...formData, qualification: e.target.value })}
                    className={`w-full px-3 py-2 bg-white border rounded-xl text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20 ${
                      formErrors.qualification ? 'border-rose-400' : 'border-slate-200 focus:border-brand-500'
                    }`}
                  />
                  {formErrors.qualification && <p className="text-[11px] text-rose-500 mt-1">{formErrors.qualification}</p>}
                </div>

                {/* Department */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Department <span className="text-rose-500">*</span>
                  </label>
                  <select
                    value={formData.department}
                    onChange={(e) => setFormData({ ...formData, department: e.target.value })}
                    className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                  >
                    {DEPARTMENTS.filter(d => d !== 'All Departments').map(d => (
                      <option key={d} value={d}>{d}</option>
                    ))}
                  </select>
                </div>

                {/* Cabin Room */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">Cabin Room / Office</label>
                  <input
                    type="text"
                    value={formData.cabin_room}
                    onChange={(e) => setFormData({ ...formData, cabin_room: e.target.value })}
                    className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                  />
                </div>

                {/* Email */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    College Email <span className="text-rose-500">*</span>
                  </label>
                  <input
                    type="email"
                    value={formData.email}
                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    className={`w-full px-3 py-2 bg-white border rounded-xl text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20 ${
                      formErrors.email ? 'border-rose-400' : 'border-slate-200 focus:border-brand-500'
                    }`}
                  />
                  {formErrors.email && <p className="text-[11px] text-rose-500 mt-1">{formErrors.email}</p>}
                </div>

                {/* Password (for Add Faculty / Supabase Auth) */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Professor Login Password {editingFaculty ? '(Leave blank to keep unchanged)' : <span className="text-rose-500">*</span>}
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
                  {!formErrors.password && !editingFaculty && (
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

                {/* LinkedIn Profile */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">LinkedIn Profile</label>
                  <input
                    type="url"
                    value={formData.linkedin}
                    onChange={(e) => setFormData({ ...formData, linkedin: e.target.value })}
                    className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                  />
                </div>

                {/* Experience Years */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">Experience (Years)</label>
                  <input
                    type="number"
                    min="0"
                    max="50"
                    value={formData.experience_years}
                    onChange={(e) => setFormData({ ...formData, experience_years: e.target.value === '' ? '' : (parseInt(e.target.value) || 0) })}
                    className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                  />
                </div>

                {/* Upload Faculty Photo (sm:col-span-2) */}
                <div className="sm:col-span-2">
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Upload Faculty Photo
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
                        alt="Faculty Preview"
                        className="w-14 h-14 rounded-xl object-cover border border-slate-200 shadow-xs shrink-0"
                        onError={(e) => {
                          e.target.src = 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200';
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
                        Click to upload faculty photo
                      </div>
                      <p className="text-[11px] text-slate-400 mt-0.5">
                        Supports JPG, PNG, or WEBP (Max 5MB)
                      </p>
                    </div>
                  )}

                  {photoError && <p className="text-[11px] text-rose-500 mt-1">{photoError}</p>}
                </div>

                {/* Subjects Taught */}
                <div className="sm:col-span-2 space-y-2">
                  <div className="flex items-center justify-between">
                    <label className="block text-xs font-bold text-slate-700">Subjects Taught (Comma separated)</label>
                    <span className="text-[11px] text-slate-500 font-medium">Select subjects below or type</span>
                  </div>
                  <input
                    type="text"
                    value={formData.subjects}
                    onChange={(e) => setFormData({ ...formData, subjects: e.target.value })}
                    placeholder="e.g. Data Structure (BCS-301), Python Programming (BCC-302)"
                    className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                  />

                  {/* Assign Master Subjects Checkboxes */}
                  <div className="p-3 bg-slate-50 border border-slate-200/80 rounded-xl space-y-2 mt-2">
                    <div className="flex items-center justify-between">
                      <span className="text-[11px] font-bold text-slate-700 uppercase tracking-wider">
                        Assign Subjects to Teacher
                      </span>
                      <span className="text-[10px] text-slate-500 font-medium">Master Curriculum List</span>
                    </div>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 pt-1">
                      {MASTER_SUBJECTS.map((s) => {
                        const checked = isSubjectSelected(s);
                        return (
                          <label
                            key={s.id}
                            className={`flex items-start gap-2.5 p-2 rounded-lg border text-xs cursor-pointer transition-all ${
                              checked
                                ? 'bg-brand-50/80 border-brand-400 text-brand-950 font-medium shadow-xs'
                                : 'bg-white border-slate-200 text-slate-600 hover:border-slate-300'
                            }`}
                          >
                            <input
                              type="checkbox"
                              checked={checked}
                              onChange={() => toggleSubject(s)}
                              className="mt-0.5 rounded border-slate-300 text-brand-600 focus:ring-brand-500 w-3.5 h-3.5"
                            />
                            <div className="min-w-0 flex-1 leading-snug">
                              <span className="block truncate font-semibold">{s.name}</span>
                              <span className="text-[10px] text-slate-500 font-mono">{s.code}</span>
                            </div>
                          </label>
                        );
                      })}
                    </div>
                  </div>
                </div>
              </div>

              {/* Actions */}
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
                  <span>{isSubmitting ? 'Saving to Supabase...' : editingFaculty ? 'Update Faculty' : 'Save Faculty'}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
