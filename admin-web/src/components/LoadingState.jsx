import React from 'react';
import { Loader2 } from 'lucide-react';

export function LoadingSpinner({ message = 'Loading data...' }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 px-4">
      <Loader2 className="w-8 h-8 text-brand-600 animate-spin" />
      <p className="mt-3 text-sm text-slate-500 font-medium">{message}</p>
    </div>
  );
}

export function TableSkeleton({ rows = 5, cols = 5 }) {
  return (
    <div className="animate-pulse space-y-3 p-4">
      <div className="h-10 bg-slate-100 rounded-lg w-full"></div>
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="h-12 bg-slate-50 rounded-lg w-full flex items-center gap-4 px-4">
          {Array.from({ length: cols }).map((_, j) => (
            <div key={j} className="h-4 bg-slate-200/60 rounded flex-1"></div>
          ))}
        </div>
      ))}
    </div>
  );
}

export function EmptyState({ icon: Icon, title, description, actionText, onAction }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 px-4 text-center bg-white rounded-2xl border border-dashed border-slate-200">
      {Icon && (
        <div className="w-14 h-14 rounded-2xl bg-slate-50 border border-slate-100 flex items-center justify-center text-slate-400 mb-3 shadow-inner">
          <Icon className="w-7 h-7" />
        </div>
      )}
      <h3 className="text-base font-bold text-slate-800">{title}</h3>
      <p className="text-sm text-slate-500 max-w-sm mt-1 mb-4">{description}</p>
      {actionText && onAction && (
        <button
          onClick={onAction}
          className="px-4 py-2 bg-brand-600 hover:bg-brand-700 text-white text-xs font-semibold rounded-xl shadow-sm transition-colors"
        >
          {actionText}
        </button>
      )}
    </div>
  );
}
