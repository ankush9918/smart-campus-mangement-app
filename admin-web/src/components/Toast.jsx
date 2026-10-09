import React from 'react';
import { CheckCircle2, AlertCircle, Info, X } from 'lucide-react';

export function Toast({ notification, onClose }) {
  if (!notification) return null;

  const { type = 'info', message, title } = notification;

  const bgStyles = {
    success: 'bg-emerald-50 border-emerald-200 text-emerald-900',
    error: 'bg-rose-50 border-rose-200 text-rose-900',
    info: 'bg-blue-50 border-blue-200 text-blue-900',
    warning: 'bg-amber-50 border-amber-200 text-amber-900'
  }[type] || 'bg-slate-50 border-slate-200 text-slate-900';

  const Icon = {
    success: CheckCircle2,
    error: AlertCircle,
    info: Info,
    warning: AlertCircle
  }[type] || Info;

  const iconColors = {
    success: 'text-emerald-600',
    error: 'text-rose-600',
    info: 'text-blue-600',
    warning: 'text-amber-600'
  }[type] || 'text-slate-600';

  return (
    <div className="fixed bottom-5 right-5 z-50 max-w-md w-full animate-fade-in shadow-xl rounded-xl border p-4 backdrop-blur-md transition-all duration-300">
      <div className={`rounded-lg p-3 border flex items-start gap-3 ${bgStyles}`}>
        <Icon className={`w-5 h-5 shrink-0 mt-0.5 ${iconColors}`} />
        <div className="flex-1 min-w-0">
          {title && <h4 className="font-semibold text-sm leading-snug">{title}</h4>}
          <p className="text-xs mt-0.5 opacity-90 leading-relaxed">{message}</p>
        </div>
        <button
          onClick={onClose}
          className="p-1 rounded hover:bg-black/5 text-slate-500 hover:text-slate-800 transition-colors"
        >
          <X className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}
