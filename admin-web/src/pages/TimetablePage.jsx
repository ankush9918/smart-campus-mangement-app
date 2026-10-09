import React, { useState } from 'react';
import { 
  CalendarClock, 
  Calendar, 
  Clock, 
  Edit3, 
  Trash2, 
  PlusCircle, 
  GraduationCap, 
  Users, 
  Check, 
  X, 
  MapPin, 
  User, 
  BookOpen,
  Sparkles
} from 'lucide-react';
import { EmptyState } from '../components/LoadingState';

const DAYS = ['ALL', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];

export function TimetablePage({ 
  studentTimetable, 
  professorTimetable, 
  onAddStudentSlot, 
  onUpdateStudentSlot, 
  onDeleteStudentSlot, 
  onAddProfessorSlot, 
  onUpdateProfessorSlot, 
  onDeleteProfessorSlot, 
  faculty, 
  onShowToast, 
  onConfirmDelete 
}) {
  const [activeTab, setActiveTab] = useState('student'); // 'student' | 'professor'
  const [selectedDay, setSelectedDay] = useState('MONDAY');
  const [selectedProfessor, setSelectedProfessor] = useState('ALL');

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingSlot, setEditingSlot] = useState(null);

  // Form State for Student Slot
  const [slotForm, setSlotForm] = useState({
    day: 'MONDAY',
    start_time: '',
    end_time: '',
    duration: '',
    subject_name: '',
    subject_code: '',
    teacher_name: '',
    room: '',
    batch_or_section: '',
    is_lab: false,
    is_break: false,
    lecture_type: 'Lecture'
  });

  const [formErrors, setFormErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Open Add Modal
  const handleOpenAdd = () => {
    setEditingSlot(null);
    const dayToUse = selectedDay === 'ALL' ? 'MONDAY' : selectedDay;

    setSlotForm({
      day: dayToUse,
      start_time: '',
      end_time: '',
      duration: '',
      subject_name: '',
      subject_code: '',
      teacher_name: '',
      room: '',
      batch_or_section: '',
      is_lab: false,
      is_break: false,
      lecture_type: 'Lecture'
    });
    setFormErrors({});
    setIsModalOpen(true);
  };

  // Open Edit Modal
  const handleOpenEdit = (slot) => {
    setEditingSlot(slot);
    setSlotForm({
      day: slot.day || 'MONDAY',
      start_time: slot.start_time || '09:00 AM',
      end_time: slot.end_time || '10:00 AM',
      duration: slot.duration || '1 Hour',
      subject_name: slot.subject_name || '',
      subject_code: slot.subject_code || '',
      teacher_name: slot.teacher_name || '',
      room: slot.room || '',
      batch_or_section: slot.batch_or_section || 'CSE-5B',
      is_lab: Boolean(slot.is_lab),
      is_break: Boolean(slot.is_break),
      lecture_type: slot.lecture_type || 'Lecture'
    });
    setFormErrors({});
    setIsModalOpen(true);
  };

  // Validate
  const validateForm = () => {
    const errors = {};
    if (!slotForm.day) errors.day = 'Day of week is required.';
    if (!slotForm.start_time.trim()) errors.start_time = 'Start time is required.';
    if (!slotForm.end_time.trim()) errors.end_time = 'End time is required.';
    if (!slotForm.subject_name.trim()) errors.subject_name = 'Subject / Slot name is required.';
    if (!slotForm.room.trim()) errors.room = 'Room / Classroom is required.';
    if (!slotForm.teacher_name.trim() && !slotForm.is_break) errors.teacher_name = 'Instructor name is required.';

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Submit Slot
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsSubmitting(true);
    try {
      if (activeTab === 'student') {
        const payload = {
          day: slotForm.day.toUpperCase(),
          start_time: slotForm.start_time,
          end_time: slotForm.end_time,
          duration: slotForm.duration || '1 Hour',
          subject_name: slotForm.subject_name,
          subject_code: slotForm.subject_code,
          teacher_name: slotForm.teacher_name,
          room: slotForm.room,
          batch_or_section: slotForm.batch_or_section,
          is_lab: slotForm.is_lab,
          is_break: slotForm.is_break,
          is_ongoing: false
        };

        if (editingSlot) {
          await onUpdateStudentSlot(editingSlot.id, payload);
          onShowToast('success', 'Student Timetable Updated', `Slot for ${payload.subject_name} updated in Supabase.`);
        } else {
          await onAddStudentSlot(payload);
          onShowToast('success', 'Student Slot Added', `Slot for ${payload.subject_name} saved to Supabase timetable.`);
        }
      } else {
        const payload = {
          day: slotForm.day.toUpperCase(),
          start_time: slotForm.start_time,
          end_time: slotForm.end_time,
          subject_name: slotForm.subject_name,
          subject_code: slotForm.subject_code,
          teacher_name: slotForm.teacher_name,
          room: slotForm.room,
          batch_or_section: slotForm.batch_or_section,
          lecture_type: slotForm.lecture_type || 'Lecture',
          is_lab: slotForm.is_lab
        };

        if (editingSlot) {
          await onUpdateProfessorSlot(editingSlot.id, payload);
          onShowToast('success', 'Professor Timetable Updated', `Schedule for ${payload.teacher_name} updated in Supabase.`);
        } else {
          await onAddProfessorSlot(payload);
          onShowToast('success', 'Professor Slot Added', `Schedule saved to Supabase.`);
        }
      }
      setIsModalOpen(false);
    } catch (err) {
      onShowToast('error', 'Operation Failed', err.message || 'Could not save timetable slot.');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Filter student timetable
  const filteredStudentSlots = studentTimetable.filter((slot) => {
    const matchesDay = selectedDay === 'ALL' || slot.day.toUpperCase() === selectedDay;
    return matchesDay;
  });

  // Filter professor timetable
  const filteredProfessorSlots = professorTimetable.filter((slot) => {
    const matchesDay = selectedDay === 'ALL' || slot.day.toUpperCase() === selectedDay;
    const matchesProf = selectedProfessor === 'ALL' || slot.teacher_name === selectedProfessor;
    return matchesDay && matchesProf;
  });

  return (
    <div className="space-y-6">
      {/* Top Segmented Tab Switcher */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-4 border-b border-slate-200 pb-4">
        <div className="flex items-center gap-2 p-1 bg-slate-100 rounded-xl max-w-sm">
          <button
            onClick={() => setActiveTab('student')}
            className={`flex-1 flex items-center justify-center gap-2 py-2 px-4 rounded-lg text-xs font-bold transition-all ${
              activeTab === 'student'
                ? 'bg-white text-brand-700 shadow-xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <GraduationCap className="w-4 h-4" />
            <span>Student Timetable</span>
          </button>

          <button
            onClick={() => setActiveTab('professor')}
            className={`flex-1 flex items-center justify-center gap-2 py-2 px-4 rounded-lg text-xs font-bold transition-all ${
              activeTab === 'professor'
                ? 'bg-white text-brand-700 shadow-xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <Users className="w-4 h-4" />
            <span>Professor Timetable</span>
          </button>
        </div>

        <button
          onClick={handleOpenAdd}
          className="px-4 py-2 bg-brand-600 hover:bg-brand-700 text-white text-xs font-bold rounded-xl shadow-sm transition-all flex items-center gap-1.5 shrink-0 self-end sm:self-auto"
        >
          <PlusCircle className="w-4 h-4" />
          <span>{activeTab === 'student' ? 'Add Student Class Slot' : 'Add Professor Lecture Slot'}</span>
        </button>
      </div>

      {/* Day Selector Pills & Filters */}
      <div className="flex flex-col md:flex-row items-stretch md:items-center justify-between gap-4 bg-white p-4 rounded-2xl border border-slate-100 shadow-xs">
        {/* Day Pills */}
        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 md:pb-0">
          {DAYS.map((day) => (
            <button
              key={day}
              onClick={() => setSelectedDay(day)}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all shrink-0 ${
                selectedDay === day
                  ? 'bg-brand-600 text-white shadow-xs'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              {day}
            </button>
          ))}
        </div>

        {/* Professor filter if in professor tab */}
        {activeTab === 'professor' && (
          <div className="flex items-center gap-2">
            <span className="text-xs font-semibold text-slate-500 shrink-0">Filter Professor:</span>
            <select
              value={selectedProfessor}
              onChange={(e) => setSelectedProfessor(e.target.value)}
              className="px-3 py-1.5 bg-slate-50 border border-slate-200 rounded-lg text-xs text-slate-700 focus:outline-none focus:border-brand-500 font-medium"
            >
              <option value="ALL">All Professors</option>
              {faculty.map((f) => (
                <option key={f.id} value={f.name}>{f.name}</option>
              ))}
            </select>
          </div>
        )}
      </div>

      {/* Timetable Content */}
      {activeTab === 'student' ? (
        // STUDENT TIMETABLE SLOTS
        filteredStudentSlots.length === 0 ? (
          <EmptyState
            icon={CalendarClock}
            title="No Scheduled Classes"
            description={`No class slots found for ${selectedDay}. Click Add to schedule a lecture.`}
            actionText="Add Class Slot"
            onAction={handleOpenAdd}
          />
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {filteredStudentSlots.map((slot) => (
              <div
                key={slot.id}
                className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-xs hover:shadow-md transition-all flex flex-col justify-between group"
              >
                <div>
                  {/* Card top badges */}
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center gap-2">
                      <span className="text-[10px] font-extrabold uppercase px-2 py-0.5 rounded-md bg-brand-50 text-brand-700 border border-brand-100">
                        {slot.day}
                      </span>
                      {slot.is_lab && (
                        <span className="text-[10px] font-bold uppercase px-2 py-0.5 rounded-md bg-purple-50 text-purple-700 border border-purple-100">
                          LAB SESSION
                        </span>
                      )}
                      {slot.is_break && (
                        <span className="text-[10px] font-bold uppercase px-2 py-0.5 rounded-md bg-amber-50 text-amber-700 border border-amber-100">
                          BREAK
                        </span>
                      )}
                    </div>
                    <span className="text-[11px] font-bold text-slate-500 font-mono">
                      {slot.batch_or_section}
                    </span>
                  </div>

                  {/* Subject Name and Code */}
                  <h3 className="text-base font-extrabold text-slate-900 group-hover:text-brand-600 transition-colors">
                    {slot.subject_name}
                  </h3>
                  {slot.subject_code && (
                    <div className="text-xs font-mono font-semibold text-slate-500 mt-0.5">
                      Code: {slot.subject_code}
                    </div>
                  )}

                  {/* Details */}
                  <div className="mt-4 space-y-2 pt-3 border-t border-slate-100 text-xs text-slate-600">
                    <div className="flex items-center gap-2">
                      <Clock className="w-3.5 h-3.5 text-brand-600 shrink-0" />
                      <span className="font-semibold text-slate-800">{slot.start_time} - {slot.end_time}</span>
                      <span className="text-slate-400">({slot.duration})</span>
                    </div>

                    <div className="flex items-center gap-2">
                      <User className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                      <span>{slot.teacher_name || 'No instructor assigned'}</span>
                    </div>

                    <div className="flex items-center gap-2">
                      <MapPin className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                      <span>{slot.room || 'Classroom'}</span>
                    </div>
                  </div>
                </div>

                {/* Card footer: Update and Delete actions */}
                <div className="mt-5 pt-3 border-t border-slate-100 flex items-center justify-between">
                  <span className="text-[10px] text-slate-400 font-mono">ID: {slot.id}</span>
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => handleOpenEdit(slot)}
                      className="px-3 py-1.5 text-xs font-bold text-brand-700 bg-brand-50 hover:bg-brand-100 rounded-lg transition-colors flex items-center gap-1"
                    >
                      <Edit3 className="w-3.5 h-3.5" />
                      <span>Update</span>
                    </button>
                    <button
                      onClick={() => {
                        onConfirmDelete(
                          'Delete Class Slot',
                          `Are you sure you want to delete ${slot.subject_name} (${slot.start_time}) from the Student timetable?`,
                          () => onDeleteStudentSlot(slot.id)
                        );
                      }}
                      className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
                      title="Delete Slot"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )
      ) : (
        // PROFESSOR TIMETABLE SLOTS
        filteredProfessorSlots.length === 0 ? (
          <EmptyState
            icon={CalendarClock}
            title="No Professor Lectures Scheduled"
            description={`No scheduled lectures for ${selectedDay}. Click Add to assign a lecture.`}
            actionText="Add Lecture Slot"
            onAction={handleOpenAdd}
          />
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {filteredProfessorSlots.map((slot) => (
              <div
                key={slot.id}
                className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-xs hover:shadow-md transition-all flex flex-col justify-between group"
              >
                <div>
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center gap-2">
                      <span className="text-[10px] font-extrabold uppercase px-2 py-0.5 rounded-md bg-purple-50 text-purple-700 border border-purple-100">
                        {slot.day}
                      </span>
                      <span className="text-[10px] font-bold uppercase px-2 py-0.5 rounded-md bg-slate-100 text-slate-700">
                        {slot.lecture_type || 'Lecture'}
                      </span>
                    </div>
                    <span className="text-[11px] font-bold text-slate-500 font-mono">
                      {slot.batch_or_section}
                    </span>
                  </div>

                  <h3 className="text-base font-extrabold text-slate-900 group-hover:text-purple-600 transition-colors">
                    {slot.subject_name}
                  </h3>
                  {slot.subject_code && (
                    <div className="text-xs font-mono font-semibold text-slate-500 mt-0.5">
                      Code: {slot.subject_code}
                    </div>
                  )}

                  <div className="mt-4 space-y-2 pt-3 border-t border-slate-100 text-xs text-slate-600">
                    <div className="flex items-center gap-2">
                      <Clock className="w-3.5 h-3.5 text-purple-600 shrink-0" />
                      <span className="font-semibold text-slate-800">{slot.start_time} - {slot.end_time}</span>
                    </div>

                    <div className="flex items-center gap-2">
                      <User className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                      <span className="font-bold text-slate-800">{slot.teacher_name}</span>
                    </div>

                    <div className="flex items-center gap-2">
                      <MapPin className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                      <span>{slot.room || 'Room 402'}</span>
                    </div>
                  </div>
                </div>

                <div className="mt-5 pt-3 border-t border-slate-100 flex items-center justify-between">
                  <span className="text-[10px] text-slate-400 font-mono">ID: {slot.id}</span>
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => handleOpenEdit(slot)}
                      className="px-3 py-1.5 text-xs font-bold text-purple-700 bg-purple-50 hover:bg-purple-100 rounded-lg transition-colors flex items-center gap-1"
                    >
                      <Edit3 className="w-3.5 h-3.5" />
                      <span>Update</span>
                    </button>
                    <button
                      onClick={() => {
                        onConfirmDelete(
                          'Delete Lecture Slot',
                          `Are you sure you want to delete ${slot.subject_name} (${slot.teacher_name}) from the Professor timetable?`,
                          () => onDeleteProfessorSlot(slot.id)
                        );
                      }}
                      className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
                      title="Delete Slot"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )
      )}

      {/* Add / Edit Timetable Slot Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-2xl max-w-xl w-full max-h-[90vh] flex flex-col shadow-2xl border border-slate-100 overflow-hidden">
            <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-brand-50 border border-brand-100 flex items-center justify-center text-brand-600">
                  <CalendarClock className="w-5 h-5" />
                </div>
                <div>
                  <h2 className="text-base font-bold text-slate-900">
                    {editingSlot
                      ? `Update ${activeTab === 'student' ? 'Student' : 'Professor'} Timetable Slot`
                      : `Add ${activeTab === 'student' ? 'Student' : 'Professor'} Timetable Slot`}
                  </h2>
                  <p className="text-xs text-slate-500">Will update Supabase database and sync to mobile apps</p>
                </div>
              </div>
              <button
                onClick={() => setIsModalOpen(false)}
                className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="p-6 space-y-4 overflow-y-auto">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {/* Day of Week */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Day of Week <span className="text-rose-500">*</span>
                  </label>
                  <select
                    value={slotForm.day}
                    onChange={(e) => setSlotForm({ ...slotForm, day: e.target.value })}
                    className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500 font-semibold"
                  >
                    {DAYS.filter(d => d !== 'ALL').map(d => (
                      <option key={d} value={d}>{d}</option>
                    ))}
                  </select>
                </div>

                {/* Batch / Section */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">Batch / Section</label>
                  <input
                    type="text"
                    value={slotForm.batch_or_section}
                    onChange={(e) => setSlotForm({ ...slotForm, batch_or_section: e.target.value })}
                    className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                  />
                </div>

                {/* Start Time */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Start Time <span className="text-rose-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={slotForm.start_time}
                    onChange={(e) => setSlotForm({ ...slotForm, start_time: e.target.value })}
                    className={`w-full px-3 py-2 bg-white border rounded-xl text-xs text-slate-800 focus:outline-none ${
                      formErrors.start_time ? 'border-rose-400' : 'border-slate-200 focus:border-brand-500'
                    }`}
                  />
                </div>

                {/* End Time */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    End Time <span className="text-rose-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={slotForm.end_time}
                    onChange={(e) => setSlotForm({ ...slotForm, end_time: e.target.value })}
                    className={`w-full px-3 py-2 bg-white border rounded-xl text-xs text-slate-800 focus:outline-none ${
                      formErrors.end_time ? 'border-rose-400' : 'border-slate-200 focus:border-brand-500'
                    }`}
                  />
                </div>

                {/* Subject Name */}
                <div className="sm:col-span-2">
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Subject / Period Name <span className="text-rose-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={slotForm.subject_name}
                    onChange={(e) => setSlotForm({ ...slotForm, subject_name: e.target.value })}
                    className={`w-full px-3 py-2 bg-white border rounded-xl text-xs text-slate-800 focus:outline-none ${
                      formErrors.subject_name ? 'border-rose-400' : 'border-slate-200 focus:border-brand-500'
                    }`}
                  />
                  {formErrors.subject_name && <p className="text-[11px] text-rose-500 mt-1">{formErrors.subject_name}</p>}
                </div>

                {/* Subject Code */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">Subject Code</label>
                  <input
                    type="text"
                    value={slotForm.subject_code}
                    onChange={(e) => setSlotForm({ ...slotForm, subject_code: e.target.value })}
                    className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500 font-mono"
                  />
                </div>

                {/* Room / Lab Venue */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Room / Venue <span className="text-rose-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={slotForm.room}
                    onChange={(e) => setSlotForm({ ...slotForm, room: e.target.value })}
                    className={`w-full px-3 py-2 bg-white border rounded-xl text-xs text-slate-800 focus:outline-none ${
                      formErrors.room ? 'border-rose-400' : 'border-slate-200 focus:border-brand-500'
                    }`}
                  />
                </div>

                {/* Instructor */}
                <div className="sm:col-span-2">
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Faculty / Professor Name {!slotForm.is_break && <span className="text-rose-500">*</span>}
                  </label>
                  <div className="flex gap-2">
                    <input
                      type="text"
                      value={slotForm.teacher_name}
                      onChange={(e) => setSlotForm({ ...slotForm, teacher_name: e.target.value })}
                      className="flex-1 px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                    />
                    <select
                      onChange={(e) => {
                        if (e.target.value) setSlotForm({ ...slotForm, teacher_name: e.target.value });
                      }}
                      className="px-2 py-2 bg-slate-100 border border-slate-200 rounded-xl text-xs text-slate-700 focus:outline-none"
                    >
                      <option value="">Select Faculty</option>
                      {faculty.map(f => (
                        <option key={f.id} value={f.name}>{f.name}</option>
                      ))}
                    </select>
                  </div>
                </div>

                {/* Toggles */}
                <div className="sm:col-span-2 flex items-center gap-6 pt-2">
                  <label className="flex items-center gap-2 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={slotForm.is_lab}
                      onChange={(e) => setSlotForm({ ...slotForm, is_lab: e.target.checked })}
                      className="w-4 h-4 text-brand-600 rounded border-slate-300 focus:ring-brand-500"
                    />
                    <span className="text-xs font-semibold text-slate-700">Lab Session</span>
                  </label>

                  {activeTab === 'student' && (
                    <label className="flex items-center gap-2 cursor-pointer">
                      <input
                        type="checkbox"
                        checked={slotForm.is_break}
                        onChange={(e) => setSlotForm({ ...slotForm, is_break: e.target.checked })}
                        className="w-4 h-4 text-amber-600 rounded border-slate-300 focus:ring-amber-500"
                      />
                      <span className="text-xs font-semibold text-slate-700">Interval / Break</span>
                    </label>
                  )}
                </div>
              </div>

              {/* Actions */}
              <div className="pt-4 border-t border-slate-100 flex items-center justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
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
                  <span>{isSubmitting ? 'Saving to Supabase...' : editingSlot ? 'Update Timetable Slot' : 'Save Slot'}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
