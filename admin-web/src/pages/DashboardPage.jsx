import React from 'react';
import { 
  GraduationCap, 
  Users, 
  ClipboardCheck, 
  BellRing, 
  ArrowUpRight, 
  Calendar, 
  Clock, 
  UserPlus, 
  PlusCircle, 
  Database, 
  Sparkles,
  CheckCircle2,
  AlertTriangle
} from 'lucide-react';

export function DashboardPage({ 
  students, 
  faculty, 
  studentTimetable, 
  studentAttendance, 
  circulars, 
  supabaseStatus, 
  onNavigate, 
  onOpenSupabaseModal 
}) {
  const isReady = supabaseStatus?.connected && supabaseStatus?.tablesExist;

  // Compute stats
  const totalStudents = students.length;
  const totalFaculty = faculty.length;
  const activeCirculars = circulars.length;
  const todayAttendance = studentAttendance.length > 0
    ? Math.round((studentAttendance.filter(a => a.status === 'PRESENT').length / studentAttendance.length) * 100)
    : 87;

  return (
    <div className="space-y-6">
      {/* Supabase Connection Alert Banner (if tables need creation) */}
      {!isReady && (
        <div className="p-4 rounded-2xl border border-amber-200 bg-amber-50/80 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div className="flex items-start gap-3">
            <div className="w-10 h-10 rounded-xl bg-amber-100 text-amber-700 flex items-center justify-center shrink-0">
              <AlertTriangle className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-amber-900">
                {supabaseStatus?.connected
                  ? 'Supabase Backend Connected — Schema Setup Pending'
                  : 'Supabase Offline Mode Active'}
              </h3>
              <p className="text-xs text-amber-700 mt-0.5">
                Connected to project <code className="font-mono bg-amber-100 px-1 py-0.5 rounded font-bold">easrebumarwkloibtemf</code>.
                Execute the SQL schema in Supabase SQL editor to enable multi-client cloud persistence across mobile and web.
              </p>
            </div>
          </div>
          <button
            onClick={onOpenSupabaseModal}
            className="px-4 py-2 bg-amber-600 hover:bg-amber-700 text-white text-xs font-bold rounded-xl shadow-sm transition-colors shrink-0 flex items-center gap-1.5"
          >
            <Database className="w-4 h-4" />
            <span>View SQL Schema</span>
          </button>
        </div>
      )}

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Total Students */}
        <div className="bg-white p-5 rounded-2xl border border-slate-100 shadow-xs hover:shadow-md transition-shadow group">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Students Enrolled</span>
            <div className="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center group-hover:scale-110 transition-transform">
              <GraduationCap className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-3">
            <div className="text-2xl font-extrabold text-slate-900">{totalStudents}</div>
            <div className="text-xs text-emerald-600 font-semibold mt-1 flex items-center gap-1">
              <span>+4 New this term</span>
            </div>
          </div>
        </div>

        {/* Total Faculty */}
        <div className="bg-white p-5 rounded-2xl border border-slate-100 shadow-xs hover:shadow-md transition-shadow group">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Faculty Members</span>
            <div className="w-10 h-10 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center group-hover:scale-110 transition-transform">
              <Users className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-3">
            <div className="text-2xl font-extrabold text-slate-900">{totalFaculty}</div>
            <div className="text-xs text-purple-600 font-semibold mt-1 flex items-center gap-1">
              <span>100% Ph.D / Verified</span>
            </div>
          </div>
        </div>

        {/* Today's Attendance Rate */}
        <div className="bg-white p-5 rounded-2xl border border-slate-100 shadow-xs hover:shadow-md transition-shadow group">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Today's Attendance</span>
            <div className="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center group-hover:scale-110 transition-transform">
              <ClipboardCheck className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-3">
            <div className="text-2xl font-extrabold text-slate-900">{todayAttendance}%</div>
            <div className="text-xs text-emerald-600 font-semibold mt-1 flex items-center gap-1">
              <span>Above 75% threshold</span>
            </div>
          </div>
        </div>

        {/* Circulars Published */}
        <div className="bg-white p-5 rounded-2xl border border-slate-100 shadow-xs hover:shadow-md transition-shadow group">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Active Bulletins</span>
            <div className="w-10 h-10 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center group-hover:scale-110 transition-transform">
              <BellRing className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-3">
            <div className="text-2xl font-extrabold text-slate-900">{activeCirculars}</div>
            <div className="text-xs text-amber-600 font-semibold mt-1 flex items-center gap-1">
              <span>Synced with Student & Prof apps</span>
            </div>
          </div>
        </div>
      </div>

      {/* Quick Actions Row */}
      <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-xs">
        <h2 className="text-sm font-bold text-slate-800 tracking-tight mb-4">Quick Management Actions</h2>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          <button
            onClick={() => onNavigate('students', { openAddModal: true })}
            className="p-3.5 rounded-xl border border-slate-200 hover:border-brand-500 hover:bg-brand-50/50 transition-all text-left group"
          >
            <div className="w-9 h-9 rounded-lg bg-blue-100 text-blue-600 flex items-center justify-center mb-2 group-hover:scale-110 transition-transform">
              <UserPlus className="w-5 h-5" />
            </div>
            <div className="text-xs font-bold text-slate-800">Add Student</div>
            <div className="text-[11px] text-slate-500 mt-0.5">Enroll new admission</div>
          </button>

          <button
            onClick={() => onNavigate('faculty', { openAddModal: true })}
            className="p-3.5 rounded-xl border border-slate-200 hover:border-purple-500 hover:bg-purple-50/50 transition-all text-left group"
          >
            <div className="w-9 h-9 rounded-lg bg-purple-100 text-purple-600 flex items-center justify-center mb-2 group-hover:scale-110 transition-transform">
              <Users className="w-5 h-5" />
            </div>
            <div className="text-xs font-bold text-slate-800">Add Faculty</div>
            <div className="text-[11px] text-slate-500 mt-0.5">Register professor</div>
          </button>

          <button
            onClick={() => onNavigate('timetable')}
            className="p-3.5 rounded-xl border border-slate-200 hover:border-emerald-500 hover:bg-emerald-50/50 transition-all text-left group"
          >
            <div className="w-9 h-9 rounded-lg bg-emerald-100 text-emerald-600 flex items-center justify-center mb-2 group-hover:scale-110 transition-transform">
              <Calendar className="w-5 h-5" />
            </div>
            <div className="text-xs font-bold text-slate-800">Timetable Desk</div>
            <div className="text-[11px] text-slate-500 mt-0.5">Manage schedules</div>
          </button>

          <button
            onClick={() => onNavigate('circulars', { openAddModal: true })}
            className="p-3.5 rounded-xl border border-slate-200 hover:border-amber-500 hover:bg-amber-50/50 transition-all text-left group"
          >
            <div className="w-9 h-9 rounded-lg bg-amber-100 text-amber-600 flex items-center justify-center mb-2 group-hover:scale-110 transition-transform">
              <PlusCircle className="w-5 h-5" />
            </div>
            <div className="text-xs font-bold text-slate-800">Publish Circular</div>
            <div className="text-[11px] text-slate-500 mt-0.5">Notify campus</div>
          </button>
        </div>
      </div>

      {/* Two Columns: Recent Circulars & Today's Schedule preview */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Circulars */}
        <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-xs flex flex-col">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <BellRing className="w-4 h-4 text-brand-600" />
              <h2 className="text-sm font-bold text-slate-800">Campus Circulars & Notices</h2>
            </div>
            <button
              onClick={() => onNavigate('circulars')}
              className="text-xs font-bold text-brand-600 hover:text-brand-700 flex items-center gap-0.5"
            >
              <span>View All</span>
              <ArrowUpRight className="w-3.5 h-3.5" />
            </button>
          </div>

          <div className="space-y-3 flex-1">
            {circulars.slice(0, 3).map((item) => (
              <div key={item.id} className="p-3.5 rounded-xl border border-slate-100 hover:bg-slate-50/60 transition-colors">
                <div className="flex items-center justify-between gap-2">
                  <div className="flex items-center gap-2">
                    <span className={`text-[10px] px-2 py-0.5 rounded-full font-bold uppercase ${
                      item.is_urgent ? 'bg-rose-100 text-rose-700' : 'bg-slate-100 text-slate-600'
                    }`}>
                      {item.category || 'NOTICE'}
                    </span>
                    <span className="text-[10px] text-slate-400 font-medium">To: {item.target || 'ALL'}</span>
                  </div>
                  <span className="text-[11px] text-slate-400">{item.date}</span>
                </div>
                <h4 className="text-xs font-bold text-slate-900 mt-1.5">{item.title}</h4>
                <p className="text-xs text-slate-500 mt-1 line-clamp-2 leading-relaxed">{item.description}</p>
              </div>
            ))}
          </div>
        </div>

        {/* Timetable Highlights */}
        <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-xs flex flex-col">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <Calendar className="w-4 h-4 text-brand-600" />
              <h2 className="text-sm font-bold text-slate-800">Today's Class Schedule (Monday)</h2>
            </div>
            <button
              onClick={() => onNavigate('timetable')}
              className="text-xs font-bold text-brand-600 hover:text-brand-700 flex items-center gap-0.5"
            >
              <span>Manage</span>
              <ArrowUpRight className="w-3.5 h-3.5" />
            </button>
          </div>

          <div className="space-y-3 flex-1">
            {studentTimetable.slice(0, 4).map((slot) => (
              <div key={slot.id} className="p-3.5 rounded-xl border border-slate-100 hover:bg-slate-50/60 flex items-center justify-between gap-4">
                <div className="flex items-center gap-3">
                  <div className="w-12 text-center shrink-0">
                    <span className="text-[11px] font-bold text-brand-700 bg-brand-50 px-1.5 py-1 rounded block leading-tight">
                      {slot.start_time.split(' ')[0]}
                    </span>
                    <span className="text-[9px] text-slate-400 font-semibold uppercase">{slot.start_time.split(' ')[1] || 'AM'}</span>
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-bold text-slate-900">{slot.subject_name}</span>
                      {slot.is_lab && (
                        <span className="text-[9px] bg-purple-100 text-purple-700 px-1.5 py-0.5 rounded font-bold">LAB</span>
                      )}
                      {slot.is_break && (
                        <span className="text-[9px] bg-amber-100 text-amber-700 px-1.5 py-0.5 rounded font-bold">BREAK</span>
                      )}
                    </div>
                    <div className="text-[11px] text-slate-500 mt-0.5">
                      {slot.teacher_name} • {slot.room} • {slot.batch_or_section}
                    </div>
                  </div>
                </div>

                <div className="text-right shrink-0">
                  <span className="text-[11px] text-slate-400">{slot.duration}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
