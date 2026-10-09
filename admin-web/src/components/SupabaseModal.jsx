import React, { useState } from 'react';
import { Database, Copy, Check, ExternalLink, X, RefreshCw, CheckCircle2, AlertTriangle } from 'lucide-react';
import { SUPABASE_PROJECT_ID, SUPABASE_URL, checkSupabaseStatus } from '../lib/supabase';

const SCHEMA_SQL = `-- ==========================================================
-- SMART COLLEGE MANAGEMENT SYSTEM (VVIT)
-- SUPABASE POSTGRESQL DATABASE SCHEMA
-- Project ID: easrebumarwkloibtemf
-- ==========================================================

-- 1. Students Table
CREATE TABLE IF NOT EXISTS public.students (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    roll_number TEXT UNIQUE NOT NULL,
    department TEXT NOT NULL,
    semester INTEGER DEFAULT 1,
    section TEXT DEFAULT 'A',
    email TEXT,
    phone TEXT,
    photo_url TEXT,
    attendance_rate INTEGER DEFAULT 85,
    cgpa NUMERIC DEFAULT 8.0,
    guardian_name TEXT,
    guardian_phone TEXT,
    address TEXT,
    blood_group TEXT DEFAULT 'O+',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. Faculty / Professor Table
CREATE TABLE IF NOT EXISTS public.faculty (
    id TEXT PRIMARY KEY,
    employee_id TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    designation TEXT NOT NULL,
    qualification TEXT NOT NULL,
    department TEXT NOT NULL,
    email TEXT,
    phone TEXT,
    linkedin TEXT,
    photo_url TEXT,
    cabin_room TEXT DEFAULT 'Room 101',
    experience_years INTEGER DEFAULT 5,
    subjects JSONB DEFAULT '[]'::jsonb,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. Student Timetable Table
CREATE TABLE IF NOT EXISTS public.timetable_student (
    id TEXT PRIMARY KEY,
    day TEXT NOT NULL,
    start_time TEXT NOT NULL,
    end_time TEXT NOT NULL,
    duration TEXT DEFAULT '1 Hour',
    subject_name TEXT NOT NULL,
    subject_code TEXT,
    teacher_name TEXT,
    teacher_id TEXT,
    room TEXT,
    batch_or_section TEXT DEFAULT 'CSE-5B',
    is_lab BOOLEAN DEFAULT FALSE,
    is_break BOOLEAN DEFAULT FALSE,
    is_ongoing BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. Professor / Faculty Timetable Table
CREATE TABLE IF NOT EXISTS public.timetable_faculty (
    id TEXT PRIMARY KEY,
    day TEXT NOT NULL,
    start_time TEXT NOT NULL,
    end_time TEXT NOT NULL,
    subject_name TEXT NOT NULL,
    subject_code TEXT,
    teacher_name TEXT NOT NULL,
    teacher_id TEXT,
    room TEXT,
    batch_or_section TEXT DEFAULT 'CSE-5B',
    lecture_type TEXT DEFAULT 'Lecture',
    is_lab BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. Student Attendance Table
CREATE TABLE IF NOT EXISTS public.attendance_student (
    id TEXT PRIMARY KEY,
    student_id TEXT,
    student_name TEXT NOT NULL,
    roll_number TEXT NOT NULL,
    subject TEXT NOT NULL,
    date TEXT NOT NULL,
    status TEXT NOT NULL,
    marked_by TEXT,
    timestamp TIMESTAMPTZ DEFAULT NOW()
);

-- 6. Faculty / Professor Attendance Table
CREATE TABLE IF NOT EXISTS public.attendance_faculty (
    id TEXT PRIMARY KEY,
    faculty_id TEXT,
    faculty_name TEXT NOT NULL,
    employee_id TEXT NOT NULL,
    department TEXT NOT NULL,
    date TEXT NOT NULL,
    status TEXT NOT NULL,
    check_in_time TEXT,
    timestamp TIMESTAMPTZ DEFAULT NOW()
);

-- 7. Circulars Table
CREATE TABLE IF NOT EXISTS public.circulars (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    date TEXT NOT NULL,
    category TEXT DEFAULT 'ACADEMICS',
    target TEXT DEFAULT 'ALL',
    is_urgent BOOLEAN DEFAULT FALSE,
    is_published BOOLEAN DEFAULT TRUE,
    published_by TEXT DEFAULT 'Campus Administrator',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 8. Assignments Table
CREATE TABLE IF NOT EXISTS public.assignments (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    instructions TEXT,
    subject TEXT NOT NULL,
    due_date TEXT NOT NULL,
    max_marks INTEGER DEFAULT 100,
    department TEXT DEFAULT 'Information Technology',
    section TEXT DEFAULT 'A',
    file_url TEXT,
    created_by TEXT DEFAULT 'Faculty',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 9. Study Materials Table
CREATE TABLE IF NOT EXISTS public.study_materials (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT,
    subject TEXT NOT NULL,
    shared_by TEXT NOT NULL,
    shared_date TEXT NOT NULL,
    file_size TEXT DEFAULT '2.5 MB',
    file_type TEXT DEFAULT 'PDF',
    download_url TEXT,
    department TEXT DEFAULT 'Information Technology',
    section TEXT DEFAULT 'A',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Disable Row Level Security (RLS) for seamless frontend read/write operations
ALTER TABLE public.students DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.faculty DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.timetable_student DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.timetable_faculty DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.attendance_student DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.attendance_faculty DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.circulars DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.assignments DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.study_materials DISABLE ROW LEVEL SECURITY;`;

export function SupabaseModal({ isOpen, onClose, status, onRefreshStatus }) {
  const [copied, setCopied] = useState(false);
  const [testing, setTesting] = useState(false);

  if (!isOpen) return null;

  const handleCopy = () => {
    navigator.clipboard.writeText(SCHEMA_SQL);
    setCopied(true);
    setTimeout(() => setCopied(false), 2500);
  };

  const handleTest = async () => {
    setTesting(true);
    if (onRefreshStatus) {
      await onRefreshStatus();
    }
    setTesting(false);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
      <div className="bg-white rounded-2xl max-w-3xl w-full max-h-[90vh] flex flex-col shadow-2xl border border-slate-100 overflow-hidden">
        {/* Header */}
        <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-brand-50 border border-brand-100 flex items-center justify-center text-brand-600">
              <Database className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-slate-900">Supabase Backend & Database Setup</h2>
              <p className="text-xs text-slate-500">Project ID: <code className="font-mono text-brand-700 bg-brand-50 px-1 py-0.5 rounded">{SUPABASE_PROJECT_ID}</code></p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Status card */}
        <div className="p-6 space-y-5 overflow-y-auto">
          <div className="p-4 rounded-xl border border-slate-200 bg-slate-50/70 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              {status?.connected && status?.tablesExist ? (
                <div className="w-9 h-9 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center shrink-0">
                  <CheckCircle2 className="w-5 h-5" />
                </div>
              ) : status?.connected ? (
                <div className="w-9 h-9 rounded-full bg-amber-100 text-amber-600 flex items-center justify-center shrink-0">
                  <AlertTriangle className="w-5 h-5" />
                </div>
              ) : (
                <div className="w-9 h-9 rounded-full bg-rose-100 text-rose-600 flex items-center justify-center shrink-0">
                  <AlertTriangle className="w-5 h-5" />
                </div>
              )}
              <div>
                <div className="flex items-center gap-2">
                  <span className="text-sm font-bold text-slate-800">
                    {status?.connected && status?.tablesExist
                      ? 'Connected & All Tables Operational'
                      : status?.connected
                      ? 'Connected to Supabase — Awaiting SQL Schema Execution'
                      : 'Supabase Offline / Fallback Active'}
                  </span>
                  {status?.latency ? (
                    <span className="text-[11px] font-medium text-slate-500 bg-white px-2 py-0.5 rounded-full border border-slate-200">
                      {status.latency} ms
                    </span>
                  ) : null}
                </div>
                <p className="text-xs text-slate-500 mt-0.5">{status?.message || 'Checking status...'}</p>
              </div>
            </div>

            <div className="flex items-center gap-2 w-full md:w-auto">
              <button
                onClick={handleTest}
                disabled={testing}
                className="flex-1 md:flex-none px-3 py-1.5 text-xs font-semibold text-slate-700 bg-white hover:bg-slate-100 border border-slate-200 rounded-lg transition-colors flex items-center justify-center gap-1.5 shadow-sm"
              >
                <RefreshCw className={`w-3.5 h-3.5 ${testing ? 'animate-spin' : ''}`} />
                Test Connection
              </button>
              <a
                href={`https://supabase.com/dashboard/project/${SUPABASE_PROJECT_ID}/sql`}
                target="_blank"
                rel="noreferrer"
                className="flex-1 md:flex-none px-3 py-1.5 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-lg transition-colors flex items-center justify-center gap-1.5 shadow-sm"
              >
                <span>Open SQL Editor</span>
                <ExternalLink className="w-3.5 h-3.5" />
              </a>
            </div>
          </div>

          {/* Quick instructions */}
          <div className="bg-brand-50/60 border border-brand-100 rounded-xl p-4 text-xs text-slate-700 space-y-2">
            <h4 className="font-bold text-brand-900 flex items-center gap-1.5">
              <span>Setup Instructions for Project {SUPABASE_PROJECT_ID}:</span>
            </h4>
            <ol className="list-decimal list-inside space-y-1 text-slate-600">
              <li>Click the <strong>Copy SQL Schema</strong> button below.</li>
              <li>Open your Supabase Project dashboard at <a href={`https://supabase.com/dashboard/project/${SUPABASE_PROJECT_ID}/sql`} target="_blank" rel="noreferrer" className="text-brand-600 underline font-medium">Supabase SQL Editor</a>.</li>
              <li>Paste the SQL and click <strong>Run</strong>. All 7 tables and initial seeds will be ready immediately.</li>
              <li>Click <strong>Test Connection</strong> above to confirm live synchronization!</li>
            </ol>
          </div>

          {/* Code preview & copy */}
          <div>
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-bold text-slate-700 tracking-wide uppercase">PostgreSQL DDL Schema (7 Tables)</span>
              <button
                onClick={handleCopy}
                className="px-3 py-1 text-xs font-semibold text-brand-700 bg-brand-50 hover:bg-brand-100 border border-brand-200 rounded-lg transition-colors flex items-center gap-1.5"
              >
                {copied ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5" />}
                {copied ? 'Copied to Clipboard!' : 'Copy SQL Schema'}
              </button>
            </div>
            <pre className="p-4 bg-slate-900 text-slate-100 rounded-xl text-xs font-mono max-h-72 overflow-y-auto leading-relaxed border border-slate-800">
              <code>{SCHEMA_SQL}</code>
            </pre>
          </div>
        </div>

        {/* Footer */}
        <div className="px-6 py-3 border-t border-slate-100 bg-slate-50 flex items-center justify-between text-xs text-slate-500">
          <span>Backend URL: <code className="font-mono text-slate-700">{SUPABASE_URL}</code></span>
          <button
            onClick={onClose}
            className="px-4 py-1.5 bg-slate-200 hover:bg-slate-300 font-semibold text-slate-700 rounded-lg transition-colors"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
