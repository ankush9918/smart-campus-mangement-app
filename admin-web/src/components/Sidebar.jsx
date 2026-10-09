import React from 'react';
import { 
  LayoutDashboard, 
  GraduationCap, 
  Users, 
  CalendarClock, 
  ClipboardCheck, 
  BellRing, 
  Database, 
  ChevronRight,
  ShieldAlert,
  Building2,
  X
} from 'lucide-react';

export function Sidebar({ currentTab, onSelectTab, isMobileOpen, onCloseMobile, supabaseStatus, onOpenSupabaseModal }) {
  const navItems = [
    {
      id: 'dashboard',
      label: 'Dashboard',
      icon: LayoutDashboard,
      badge: null
    },
    {
      id: 'students',
      label: 'Students',
      icon: GraduationCap,
      badge: null
    },
    {
      id: 'faculty',
      label: 'Faculty / Professors',
      icon: Users,
      badge: null
    },
    {
      id: 'attendance',
      label: 'Attendance',
      icon: ClipboardCheck,
      subItems: ['Student Attendance', 'Faculty Attendance']
    },
    {
      id: 'timetable',
      label: 'Timetable',
      icon: CalendarClock,
      subItems: ['Student Timetable', 'Professor Timetable']
    },
    {
      id: 'circulars',
      label: 'Circulars',
      icon: BellRing,
      badge: null
    }
  ];

  return (
    <>
      {/* Mobile Backdrop */}
      {isMobileOpen && (
        <div 
          onClick={onCloseMobile}
          className="fixed inset-0 z-40 bg-slate-900/60 backdrop-blur-sm lg:hidden"
        />
      )}

      {/* Sidebar container */}
      <aside className={`
        fixed top-0 bottom-0 left-0 z-50 w-72 bg-white border-r border-slate-200/80 flex flex-col transition-transform duration-300 ease-in-out
        lg:translate-x-0 ${isMobileOpen ? 'translate-x-0' : '-translate-x-full'}
      `}>
        {/* Brand Header */}
        <div className="h-16 px-6 border-b border-slate-100 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-brand-700 via-brand-600 to-brand-500 text-white flex items-center justify-center font-bold text-lg shadow-md shadow-brand-500/20">
              L
            </div>
            <div>
              <div className="font-extrabold text-sm text-slate-900 tracking-tight leading-tight">L.D.A.H REC Mainpuri</div>
              <div className="text-[11px] font-semibold text-brand-600 uppercase tracking-wider">Admin Portal</div>
            </div>
          </div>
          <button 
            onClick={onCloseMobile}
            className="p-1 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 lg:hidden"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Navigation list */}
        <div className="flex-1 overflow-y-auto px-4 py-5 space-y-1.5">
          <div className="px-3 pb-2 text-[10px] font-bold uppercase tracking-wider text-slate-400">
            Institutional Modules
          </div>

          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = currentTab === item.id;

            return (
              <div key={item.id}>
                <button
                  onClick={() => {
                    onSelectTab(item.id);
                    if (onCloseMobile) onCloseMobile();
                  }}
                  className={`
                    w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-semibold transition-all duration-200 group
                    ${isActive 
                      ? 'bg-brand-600 text-white shadow-md shadow-brand-600/25 font-bold' 
                      : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100/80'}
                  `}
                >
                  <Icon className={`w-4 h-4 shrink-0 transition-transform duration-200 group-hover:scale-110 ${isActive ? 'text-white' : 'text-slate-400 group-hover:text-slate-700'}`} />
                  <span className="flex-1 text-left">{item.label}</span>
                  {item.badge && (
                    <span className={`text-[10px] px-1.5 py-0.5 rounded-full font-bold ${isActive ? 'bg-white/20 text-white' : 'bg-slate-100 text-slate-600'}`}>
                      {item.badge}
                    </span>
                  )}
                  {item.subItems && (
                    <ChevronRight className={`w-3.5 h-3.5 shrink-0 opacity-60 ${isActive ? 'rotate-90' : ''}`} />
                  )}
                </button>

                {/* Sub-item pills when active */}
                {isActive && item.subItems && (
                  <div className="ml-7 mt-1.5 pl-2 border-l border-brand-200 space-y-1">
                    {item.subItems.map((sub) => (
                      <div key={sub} className="text-[11px] font-medium text-brand-700 py-0.5">
                        • {sub}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            );
          })}
        </div>

        {/* Supabase Status Pill & Setup Button */}
        <div className="p-4 border-t border-slate-100 bg-slate-50/50">
          <button
            onClick={onOpenSupabaseModal}
            className="w-full p-3 rounded-xl border border-slate-200 bg-white hover:border-brand-300 hover:shadow-sm transition-all duration-200 text-left group"
          >
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Database className="w-4 h-4 text-brand-600" />
                <span className="text-xs font-bold text-slate-800">Supabase DB</span>
              </div>
              <span className={`w-2 h-2 rounded-full ${
                supabaseStatus?.connected && supabaseStatus?.tablesExist
                  ? 'bg-emerald-500 animate-pulse'
                  : supabaseStatus?.connected
                  ? 'bg-amber-500'
                  : 'bg-rose-500'
              }`} />
            </div>
            <div className="text-[11px] text-slate-500 mt-1 truncate">
              {supabaseStatus?.connected && supabaseStatus?.tablesExist
                ? 'All 7 Tables Live'
                : supabaseStatus?.connected
                ? 'Tables Setup Needed'
                : 'Offline / Fallback'}
            </div>
            <div className="text-[10px] text-brand-600 font-semibold mt-1 group-hover:underline flex items-center gap-1">
              <span>View Schema & Status</span>
              <ChevronRight className="w-3 h-3" />
            </div>
          </button>
        </div>

        {/* Admin Executive Profile footer */}
        <div className="px-5 py-4 border-t border-slate-100 flex items-center gap-3">
          <div className="w-9 h-9 rounded-full bg-slate-800 text-white flex items-center justify-center font-bold text-xs shadow-sm">
            AD
          </div>
          <div className="flex-1 min-w-0">
            <div className="text-xs font-bold text-slate-800 truncate">Dr. Rajesh Varma</div>
            <div className="text-[11px] text-slate-500 truncate">Chief Administrator</div>
          </div>
        </div>
      </aside>
    </>
  );
}
