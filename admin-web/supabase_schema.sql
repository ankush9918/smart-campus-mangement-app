-- ==========================================================
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

-- Disable Row Level Security (RLS) so the frontend client can perform read/write operations seamlessly
ALTER TABLE public.students DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.faculty DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.timetable_student DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.timetable_faculty DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.attendance_student DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.attendance_faculty DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.circulars DISABLE ROW LEVEL SECURITY;

-- Initial Seed Data
INSERT INTO public.students (id, name, roll_number, department, semester, section, email, phone, attendance_rate, cgpa, photo_url, guardian_name, guardian_phone, address, blood_group)
VALUES
('std_01', 'Aditya Sharma', 'VVIT/CS/2021/045', 'Computer Science & Engineering', 5, 'B', 'aditya.sharma@vvit.edu', '+91 98765 43210', 87, 8.41, 'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200', 'Ramesh Sharma', '+91 98111 22233', 'Bangalore, Karnataka', 'O+'),
('std_02', 'Priya Patel', 'VVIT/CS/2021/046', 'Computer Science & Engineering', 5, 'B', 'priya.patel@vvit.edu', '+91 98765 43211', 92, 9.15, 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200', 'Suresh Patel', '+91 98111 22234', 'Mysore, Karnataka', 'B+'),
('std_03', 'Rohan Verma', 'VVIT/CS/2021/047', 'Information Technology', 5, 'A', 'rohan.verma@vvit.edu', '+91 98765 43212', 68, 7.30, 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200', 'Anil Verma', '+91 98111 22235', 'Hubli, Karnataka', 'A+'),
('std_04', 'Ananya Iyer', 'VVIT/EC/2021/012', 'Electronics & Communication', 5, 'A', 'ananya.iyer@vvit.edu', '+91 98765 43213', 94, 8.90, 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200', 'Gopal Iyer', '+91 98111 22236', 'Bangalore, Karnataka', 'AB+')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.faculty (id, employee_id, name, designation, qualification, department, email, phone, cabin_room, experience_years, subjects, photo_url, linkedin)
VALUES
('fac_01', 'VVIT/CS/T-104', 'Dr. Rajesh Kumar', 'Associate Professor', 'Ph.D. in Computer Science', 'Computer Science & Engineering', 'rajesh.kumar@vvit.edu', '+91 98123 45678', 'Academic Block A - Room 204', 16, '["Data Structures & Algorithms", "Theory of Computation", "Computer Networks"]'::jsonb, 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=200', 'https://linkedin.com/in/dr-rajesh-kumar'),
('fac_02', 'VVIT/CS/T-105', 'Prof. Sarah Jenkins', 'Professor & HOD', 'Ph.D. in Distributed Systems', 'Computer Science & Engineering', 'sarah.jenkins@vvit.edu', '+91 98111 22331', 'Sci-Tech Block - Room 301', 14, '["Database Management", "Distributed Systems", "Cloud Computing"]'::jsonb, 'https://images.unsplash.com/photo-1560250097-0b93528c311a?w=200', 'https://linkedin.com/in/prof-sarah-jenkins'),
('fac_03', 'VVIT/EC/T-201', 'Dr. Vikramaditya Rao', 'Professor', 'Ph.D. in VLSI Design', 'Electronics & Communication', 'vikram.rao@vvit.edu', '+91 98111 22332', 'VLSI Lab Complex Room 12', 18, '["Analog Electronics", "Digital Signal Processing"]'::jsonb, 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200', 'https://linkedin.com/in/dr-vikram-rao')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.timetable_student (id, day, start_time, end_time, duration, subject_name, subject_code, teacher_name, room, batch_or_section, is_lab, is_break, is_ongoing)
VALUES
('stt_01', 'MONDAY', '09:00 AM', '10:00 AM', '1 Hour', 'Theory of Computation', 'CS-501', 'Dr. Rajesh Kumar', 'Room 402', 'CSE-5B', FALSE, FALSE, FALSE),
('stt_02', 'MONDAY', '10:00 AM', '11:00 AM', '1 Hour', 'Computer Networks Lab', 'CS-502L', 'Dr. Rajesh Kumar', 'Network Lab 2', 'CSE-5B', TRUE, FALSE, TRUE),
('stt_03', 'MONDAY', '11:00 AM', '11:30 AM', '30 Mins', 'Short Tea Break', '', 'College Campus', 'Cafeteria', 'CSE-5B', FALSE, TRUE, FALSE),
('stt_04', 'MONDAY', '11:30 AM', '12:30 PM', '1 Hour', 'Database Management', 'CS-503', 'Prof. Sarah Jenkins', 'Room 402', 'CSE-5B', FALSE, FALSE, FALSE),
('stt_05', 'MONDAY', '01:30 PM', '02:30 PM', '1 Hour', 'Software Engineering', 'CS-504', 'Dr. Alan Turing', 'Room 402', 'CSE-5B', FALSE, FALSE, FALSE),
('stt_06', 'TUESDAY', '09:00 AM', '10:00 AM', '1 Hour', 'Database Management', 'CS-503', 'Prof. Sarah Jenkins', 'Room 402', 'CSE-5B', FALSE, FALSE, FALSE),
('stt_07', 'TUESDAY', '10:00 AM', '11:00 AM', '1 Hour', 'Operating Systems', 'CS-505', 'Dr. Rajesh Kumar', 'Room 402', 'CSE-5B', FALSE, FALSE, FALSE),
('stt_08', 'WEDNESDAY', '09:00 AM', '11:00 AM', '2 Hours', 'DBMS Project Lab', 'CS-503L', 'Prof. Sarah Jenkins', 'Software Lab 1', 'CSE-5B', TRUE, FALSE, FALSE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.timetable_faculty (id, day, start_time, end_time, subject_name, subject_code, teacher_name, room, batch_or_section, lecture_type, is_lab)
VALUES
('ftt_01', 'MONDAY', '09:00 AM', '10:00 AM', 'Theory of Computation', 'CS-501', 'Dr. Rajesh Kumar', 'Room 402', 'CSE-5B', 'Lecture', FALSE),
('ftt_02', 'MONDAY', '10:00 AM', '11:00 AM', 'Computer Networks Lab', 'CS-502L', 'Dr. Rajesh Kumar', 'Network Lab 2', 'CSE-5B', 'Lab Session', TRUE),
('ftt_03', 'MONDAY', '11:30 AM', '12:30 PM', 'Database Management', 'CS-503', 'Prof. Sarah Jenkins', 'Room 402', 'CSE-5B', 'Lecture', FALSE),
('ftt_04', 'TUESDAY', '10:00 AM', '11:00 AM', 'Operating Systems', 'CS-505', 'Dr. Rajesh Kumar', 'Room 402', 'CSE-5B', 'Lecture', FALSE),
('ftt_05', 'WEDNESDAY', '09:00 AM', '11:00 AM', 'DBMS Project Lab', 'CS-503L', 'Prof. Sarah Jenkins', 'Software Lab 1', 'CSE-5B', 'Lab Session', TRUE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.attendance_student (id, student_id, student_name, roll_number, subject, date, status, marked_by)
VALUES
('att_s_01', 'std_01', 'Aditya Sharma', 'VVIT/CS/2021/045', 'Theory of Computation', '2026-10-03', 'PRESENT', 'Dr. Rajesh Kumar'),
('att_s_02', 'std_02', 'Priya Patel', 'VVIT/CS/2021/046', 'Theory of Computation', '2026-10-03', 'PRESENT', 'Dr. Rajesh Kumar'),
('att_s_03', 'std_03', 'Rohan Verma', 'VVIT/CS/2021/047', 'Database Management', '2026-10-03', 'ABSENT', 'Prof. Sarah Jenkins'),
('att_s_04', 'std_04', 'Ananya Iyer', 'VVIT/EC/2021/012', 'Analog Electronics', '2026-10-03', 'PRESENT', 'Dr. Vikramaditya Rao')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.attendance_faculty (id, faculty_id, faculty_name, employee_id, department, date, status, check_in_time)
VALUES
('att_f_01', 'fac_01', 'Dr. Rajesh Kumar', 'VVIT/CS/T-104', 'Computer Science & Engineering', '2026-10-03', 'PRESENT', '08:45 AM'),
('att_f_02', 'fac_02', 'Prof. Sarah Jenkins', 'VVIT/CS/T-105', 'Computer Science & Engineering', '2026-10-03', 'PRESENT', '08:52 AM'),
('att_f_03', 'fac_03', 'Dr. Vikramaditya Rao', 'VVIT/EC/T-201', 'Electronics & Communication', '2026-10-03', 'ON_LEAVE', '-')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.circulars (id, title, description, date, category, target, is_urgent, is_published, published_by)
VALUES
('cir_01', 'Mid-Term Examinations Schedule Announced', 'All students and faculty members are hereby notified that Mid-Semester Examinations for Odd Semester 2026 will commence from October 20th. Detailed hall tickets will be issued shortly.', '2026-10-02', 'EXAMINATION', 'ALL', TRUE, TRUE, 'Controller of Examinations'),
('cir_02', 'Annual Technical Symposium - TechNova 2026', 'Department of Computer Science is hosting TechNova 2026 on October 28-29. Registrations are open for coding contests, hackathons, and paper presentations.', '2026-10-01', 'EVENT', 'ALL', FALSE, TRUE, 'Dean Student Affairs'),
('cir_03', 'Faculty Academic Council Meeting', 'A mandatory meeting for all faculty members will be conducted in Senate Hall on Monday at 03:30 PM regarding curriculum revision and NBA accreditation metrics.', '2026-10-03', 'ACADEMICS', 'FACULTY', TRUE, TRUE, 'Principal Desk')
ON CONFLICT (id) DO NOTHING;
