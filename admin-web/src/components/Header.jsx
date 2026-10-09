import React from 'react';
import { Menu, Database, Bell, RefreshCw, Sparkles } from 'lucide-react';
import { SUPABASE_PROJECT_ID } from '../lib/supabase';

export function Header({ title, subtitle, onOpenMobile, supabaseStatus, onOpenSupabaseModal, onRefreshData, isRefreshing }) {
  const isReady = supabaseStatus?.connected && supabaseStatus?.tablesExist;

  return (
    <header className="h-16 px-6 lg:px-8 bg-white border-b border-slate-200/80 sticky top-0 z-30 flex items-center justify-between shadow-xs">
      {/* Left: mobile toggle + title */}
      <div className="flex items-center gap-4">
        <button
          onClick={onOpenMobile}
          className="p-2 rounded-xl text-slate-500 hover:text-slate-800 hover:bg-slate-100 lg:hidden"
        >
          <Menu className="w-5 h-5" />
        </button>

        <div>
          <h1 className="text-base font-extrabold text-slate-900 tracking-tight leading-tight">{title}</h1>
          {subtitle && <p className="text-xs text-slate-500 hidden sm:block">{subtitle}</p>}
        </div>
      </div>

      {/* Right: Actions, Supabase badge & profile */}
      <div className="flex items-center gap-3">
        {/* Supabase status badge */}
        <button
          onClick={onOpenSupabaseModal}
          className={`
            hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-full text-xs font-semibold border transition-all duration-200
            ${isReady
              ? 'bg-emerald-50 text-emerald-700 border-emerald-200 hover:bg-emerald-100'
              : supabaseStatus?.connected
              ? 'bg-amber-50 text-amber-700 border-amber-200 hover:bg-amber-100'
              : 'bg-rose-50 text-rose-700 border-rose-200 hover:bg-rose-100'}
          `}
          title="Click to view Supabase details and SQL setup"
        >
          <span className={`w-2 h-2 rounded-full ${isReady ? 'bg-emerald-500 animate-pulse' : supabaseStatus?.connected ? 'bg-amber-500' : 'bg-rose-500'}`} />
          <Database className="w-3.5 h-3.5" />
          <span>{isReady ? 'Supabase Live' : supabaseStatus?.connected ? 'Tables Setup Needed' : 'Offline Mode'}</span>
        </button>

        {/* Refresh button */}
        {onRefreshData && (
          <button
            onClick={onRefreshData}
            disabled={isRefreshing}
            className="p-2 rounded-xl text-slate-500 hover:text-brand-600 hover:bg-slate-100 transition-colors"
            title="Refresh latest data from Supabase"
          >
            <RefreshCw className={`w-4 h-4 ${isRefreshing ? 'animate-spin text-brand-600' : ''}`} />
          </button>
        )}

        {/* Notification bell */}
        <div className="relative">
          <button className="p-2 rounded-xl text-slate-500 hover:text-slate-800 hover:bg-slate-100 transition-colors">
            <Bell className="w-4 h-4" />
            <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-rose-500 rounded-full ring-2 ring-white"></span>
          </button>
        </div>

        {/* Divider */}
        <div className="h-6 w-px bg-slate-200 hidden sm:block"></div>

        {/* Admin profile chip */}
        <div className="flex items-center gap-2.5 pl-1">
          <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-brand-600 to-indigo-600 text-white flex items-center justify-center font-bold text-xs shadow-sm">
            RV
          </div>
          <div className="hidden md:block text-left">
            <div className="text-xs font-bold text-slate-800 leading-tight">Dr. Rajesh Varma</div>
            <div className="text-[10px] text-slate-500 font-medium leading-tight">Administrator</div>
          </div>
        </div>
      </div>
    </header>
  );
}
