import React, { useState, useEffect } from 'react';
import { 
  BellRing, 
  Search, 
  PlusCircle, 
  Calendar, 
  Users, 
  AlertCircle, 
  Edit3, 
  Trash2, 
  Check, 
  X, 
  Send,
  Sparkles
} from 'lucide-react';
import { EmptyState } from '../components/LoadingState';

const CATEGORIES = ['ALL', 'ACADEMICS', 'EXAMINATION', 'EVENT', 'GENERAL'];
const TARGETS = ['ALL', 'STUDENTS', 'FACULTY'];

export function CircularsPage({ 
  circulars, 
  onAddCircular, 
  onUpdateCircular, 
  onDeleteCircular, 
  isAddModalOpen, 
  setIsAddModalOpen, 
  onShowToast, 
  onConfirmDelete 
}) {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [selectedTarget, setSelectedTarget] = useState('ALL');
  const [editingCircular, setEditingCircular] = useState(null);

  // Form State
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    date: new Date().toISOString().split('T')[0],
    category: 'ACADEMICS',
    target: 'ALL',
    is_urgent: false,
    published_by: 'Campus Administrator'
  });
  const [formErrors, setFormErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Keep form clean when Add modal opens
  useEffect(() => {
    if (isAddModalOpen && !editingCircular) {
      setFormData({
        title: '',
        description: '',
        date: new Date().toISOString().split('T')[0],
        category: 'ACADEMICS',
        target: 'ALL',
        is_urgent: false,
        published_by: 'Campus Administrator'
      });
      setFormErrors({});
    }
  }, [isAddModalOpen, editingCircular]);

  // Open Add Modal
  const handleOpenAdd = () => {
    setEditingCircular(null);
    setFormData({
      title: '',
      description: '',
      date: new Date().toISOString().split('T')[0],
      category: 'ACADEMICS',
      target: 'ALL',
      is_urgent: false,
      published_by: 'Campus Administrator'
    });
    setFormErrors({});
    setIsAddModalOpen(true);
  };

  // Open Edit Modal
  const handleOpenEdit = (cir) => {
    setEditingCircular(cir);
    setFormData({
      title: cir.title || '',
      description: cir.description || '',
      date: cir.date || new Date().toISOString().split('T')[0],
      category: cir.category || 'ACADEMICS',
      target: cir.target || 'ALL',
      is_urgent: Boolean(cir.is_urgent),
      published_by: cir.published_by || 'Campus Administrator'
    });
    setFormErrors({});
    setIsAddModalOpen(true);
  };

  // Validation
  const validateForm = () => {
    const errors = {};
    if (!formData.title.trim()) errors.title = 'Circular title is required.';
    if (!formData.description.trim()) errors.description = 'Notice content / description is required.';
    if (!formData.date) errors.date = 'Date is required.';

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Submit
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsSubmitting(true);
    try {
      if (editingCircular) {
        await onUpdateCircular(editingCircular.id, formData);
        onShowToast('success', 'Circular Updated', `"${formData.title}" updated in Supabase.`);
      } else {
        await onAddCircular(formData);
        onShowToast('success', 'Circular Published', `"${formData.title}" published to Student & Professor portals.`);
      }
      setIsAddModalOpen(false);
    } catch (err) {
      onShowToast('error', 'Operation Failed', err.message || 'Could not publish circular.');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Filter circulars
  const filteredCirculars = circulars.filter((c) => {
    const matchesSearch =
      (c.title && c.title.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (c.description && c.description.toLowerCase().includes(searchQuery.toLowerCase()));

    const matchesCategory = selectedCategory === 'ALL' || c.category === selectedCategory;
    const matchesTarget = selectedTarget === 'ALL' || c.target === selectedTarget;

    return matchesSearch && matchesCategory && matchesTarget;
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
            placeholder="Search circulars, title, contents..."
            className="w-full pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500 shadow-xs"
          />
        </div>

        {/* Filters and Publish Button */}
        <div className="flex items-center gap-2.5">
          <select
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
            className="px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-700 focus:outline-none focus:border-brand-500 shadow-xs font-medium"
          >
            {CATEGORIES.map((cat) => (
              <option key={cat} value={cat}>Category: {cat}</option>
            ))}
          </select>

          <select
            value={selectedTarget}
            onChange={(e) => setSelectedTarget(e.target.value)}
            className="px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-700 focus:outline-none focus:border-brand-500 shadow-xs font-medium"
          >
            <option value="ALL">Audience: ALL</option>
            <option value="STUDENTS">Students Only</option>
            <option value="FACULTY">Faculty Only</option>
          </select>

          <button
            onClick={handleOpenAdd}
            className="px-4 py-2.5 bg-brand-600 hover:bg-brand-700 text-white text-xs font-bold rounded-xl shadow-sm transition-all flex items-center gap-1.5 shrink-0"
          >
            <PlusCircle className="w-4 h-4" />
            <span>Publish Circular</span>
          </button>
        </div>
      </div>

      {/* Circulars List */}
      {filteredCirculars.length === 0 ? (
        <EmptyState
          icon={BellRing}
          title="No Circulars Found"
          description={searchQuery ? "No circulars matched your search criteria." : "There are no published circulars yet."}
          actionText="Publish First Circular"
          onAction={handleOpenAdd}
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {filteredCirculars.map((c) => (
            <div
              key={c.id}
              className={`bg-white rounded-2xl border p-5 shadow-xs hover:shadow-md transition-all flex flex-col justify-between group ${
                c.is_urgent ? 'border-amber-300 ring-1 ring-amber-200' : 'border-slate-200/80'
              }`}
            >
              <div>
                {/* Header tags */}
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-2">
                    <span className={`text-[10px] font-extrabold uppercase px-2.5 py-0.5 rounded-full ${
                      c.is_urgent 
                        ? 'bg-rose-100 text-rose-700 border border-rose-200' 
                        : 'bg-brand-50 text-brand-700 border border-brand-100'
                    }`}>
                      {c.category || 'NOTICE'}
                    </span>
                    <span className="text-[10px] font-bold text-slate-500 bg-slate-100 px-2 py-0.5 rounded-full">
                      Audience: {c.target || 'ALL'}
                    </span>
                  </div>
                  <div className="flex items-center gap-1.5 text-[11px] text-slate-400 font-medium">
                    <Calendar className="w-3.5 h-3.5" />
                    <span>{c.date}</span>
                  </div>
                </div>

                {/* Title */}
                <h3 className="text-base font-extrabold text-slate-900 group-hover:text-brand-600 transition-colors leading-snug">
                  {c.title}
                </h3>

                {/* Description */}
                <p className="mt-2 text-xs text-slate-600 leading-relaxed whitespace-pre-line">
                  {c.description}
                </p>
              </div>

              {/* Footer */}
              <div className="mt-5 pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
                <div className="text-[11px] text-slate-400">
                  By: <span className="font-semibold text-slate-600">{c.published_by || 'Admin Desk'}</span>
                </div>

                <div className="flex items-center gap-1.5">
                  <button
                    onClick={() => handleOpenEdit(c)}
                    className="p-1.5 rounded-lg text-slate-500 hover:text-brand-600 hover:bg-brand-50 transition-colors"
                    title="Edit Circular"
                  >
                    <Edit3 className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => {
                      onConfirmDelete(
                        'Delete Circular',
                        `Are you sure you want to remove the circular "${c.title}"?`,
                        () => onDeleteCircular(c.id)
                      );
                    }}
                    className="p-1.5 rounded-lg text-slate-500 hover:text-rose-600 hover:bg-rose-50 transition-colors"
                    title="Delete Circular"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Add / Edit Circular Modal */}
      {isAddModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-2xl max-w-xl w-full max-h-[90vh] flex flex-col shadow-2xl border border-slate-100 overflow-hidden">
            <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-amber-50 border border-amber-100 flex items-center justify-center text-amber-600">
                  <BellRing className="w-5 h-5" />
                </div>
                <div>
                  <h2 className="text-base font-bold text-slate-900">
                    {editingCircular ? 'Edit Circular / Notice' : 'Publish Campus Circular'}
                  </h2>
                  <p className="text-xs text-slate-500">
                    Will sync to Supabase and appear in Student & Professor apps
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

            <form onSubmit={handleSubmit} className="p-6 space-y-4 overflow-y-auto">
              {/* Title */}
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  Circular Title <span className="text-rose-500">*</span>
                </label>
                <input
                  type="text"
                  value={formData.title}
                  onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                  className={`w-full px-3 py-2 bg-white border rounded-xl text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20 ${
                    formErrors.title ? 'border-rose-400' : 'border-slate-200 focus:border-brand-500'
                  }`}
                />
                {formErrors.title && <p className="text-[11px] text-rose-500 mt-1">{formErrors.title}</p>}
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                {/* Category */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">Category</label>
                  <select
                    value={formData.category}
                    onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                    className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                  >
                    {CATEGORIES.filter(c => c !== 'ALL').map(c => (
                      <option key={c} value={c}>{c}</option>
                    ))}
                  </select>
                </div>

                {/* Target Audience */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">Target Audience</label>
                  <select
                    value={formData.target}
                    onChange={(e) => setFormData({ ...formData, target: e.target.value })}
                    className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                  >
                    <option value="ALL">All (Student & Faculty)</option>
                    <option value="STUDENTS">Students Only</option>
                    <option value="FACULTY">Faculty Only</option>
                  </select>
                </div>

                {/* Date */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">Date</label>
                  <input
                    type="date"
                    value={formData.date}
                    onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                    className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                  />
                </div>
              </div>

              {/* Published By */}
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Published By / Issuer</label>
                <input
                  type="text"
                  value={formData.published_by}
                  onChange={(e) => setFormData({ ...formData, published_by: e.target.value })}
                  className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-brand-500"
                />
              </div>

              {/* Content / Description */}
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  Notice Content / Description <span className="text-rose-500">*</span>
                </label>
                <textarea
                  rows={4}
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className={`w-full px-3 py-2 bg-white border rounded-xl text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20 leading-relaxed ${
                    formErrors.description ? 'border-rose-400' : 'border-slate-200 focus:border-brand-500'
                  }`}
                />
                {formErrors.description && <p className="text-[11px] text-rose-500 mt-1">{formErrors.description}</p>}
              </div>

              {/* Urgent Flag Toggle */}
              <div className="pt-2">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={formData.is_urgent}
                    onChange={(e) => setFormData({ ...formData, is_urgent: e.target.checked })}
                    className="w-4 h-4 text-amber-600 rounded border-slate-300 focus:ring-amber-500"
                  />
                  <span className="text-xs font-semibold text-slate-700">
                    Mark as High Priority / Urgent Notice
                  </span>
                </label>
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
                  <Send className="w-3.5 h-3.5" />
                  <span>{isSubmitting ? 'Publishing...' : editingCircular ? 'Update Circular' : 'Publish Circular'}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
