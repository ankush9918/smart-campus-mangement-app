import { supabase, authClient } from '../lib/supabase';

// Master Subject List as defined by curriculum requirements
export const MASTER_SUBJECTS = [
  {
    id: 'sub_bcs301',
    name: 'Data Structure',
    code: 'BCS-301',
    displayName: 'Data Structure (BCS-301)',
    status: 'Active'
  },
  {
    id: 'sub_bcc302',
    name: 'Python Programming',
    code: 'BCC-302',
    displayName: 'Python Programming (BCC-302)',
    status: 'Active'
  },
  {
    id: 'sub_boe310',
    name: 'Digital Electronics',
    code: 'BOE-310',
    displayName: 'Digital Electronics (BOE-310)',
    status: 'Active'
  },
  {
    id: 'sub_bcs303',
    name: 'Discrete Structure & Theory of Logic (T)',
    code: 'BCS-303',
    displayName: 'Discrete Structure & Theory of Logic (T) (BCS-303)',
    status: 'Active'
  },
  {
    id: 'sub_bcs302',
    name: 'Computer Organization & Architecture',
    code: 'BCS-302',
    displayName: 'Computer Organization & Architecture (BCS-302)',
    status: 'Active'
  },
  {
    id: 'sub_bas301',
    name: 'Technical Communication',
    code: 'BAS-301',
    displayName: 'Technical Communication (BAS-301)',
    status: 'Active'
  }
];

export function normalizeSubject(input) {
  if (!input) return null;
  const str = String(input).trim();
  const lower = str.toLowerCase();
  for (const s of MASTER_SUBJECTS) {
    if (s.id.toLowerCase() === lower || s.code.toLowerCase() === lower) return s;
    if (s.displayName.toLowerCase() === lower || s.name.toLowerCase() === lower) return s;
  }
  for (const s of MASTER_SUBJECTS) {
    if (lower.includes(s.code.toLowerCase())) return s;
    if (lower.includes(s.name.toLowerCase())) return s;
  }
  if (lower.includes('discrete') || lower.includes('dstl')) {
    return MASTER_SUBJECTS.find(s => s.code === 'BCS-303');
  }
  if (lower.includes('coa') || lower.includes('computer org')) {
    return MASTER_SUBJECTS.find(s => s.code === 'BCS-302');
  }
  if (lower.includes('python')) {
    return MASTER_SUBJECTS.find(s => s.code === 'BCC-302');
  }
  if (lower.includes('data struct') || lower.includes('ds')) {
    return MASTER_SUBJECTS.find(s => s.code === 'BCS-301');
  }
  if (lower.includes('digital elec')) {
    return MASTER_SUBJECTS.find(s => s.code === 'BOE-310');
  }
  if (lower.includes('tech') && lower.includes('comm')) {
    return MASTER_SUBJECTS.find(s => s.code === 'BAS-301');
  }
  return null;
}

// Initial fallback seed data (matching VVIT smart college system)
const INITIAL_STUDENTS = [
  {
    id: 'std_01',
    name: 'Aditya Sharma',
    roll_number: 'VVIT/CS/2021/045',
    department: 'Computer Science & Engineering',
    semester: 5,
    section: 'B',
    email: 'aditya.sharma@vvit.edu',
    phone: '+91 98765 43210',
    photo_url: 'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200',
    attendance_rate: 87,
    cgpa: 8.41,
    guardian_name: 'Ramesh Sharma',
    guardian_phone: '+91 98111 22233',
    address: 'Bangalore, Karnataka',
    blood_group: 'O+',
    created_at: new Date('2026-09-01').toISOString()
  },
  {
    id: 'std_02',
    name: 'Priya Patel',
    roll_number: 'VVIT/CS/2021/046',
    department: 'Computer Science & Engineering',
    semester: 5,
    section: 'B',
    email: 'priya.patel@vvit.edu',
    phone: '+91 98765 43211',
    photo_url: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200',
    attendance_rate: 92,
    cgpa: 9.15,
    guardian_name: 'Suresh Patel',
    guardian_phone: '+91 98111 22234',
    address: 'Mysore, Karnataka',
    blood_group: 'B+',
    created_at: new Date('2026-09-02').toISOString()
  },
  {
    id: 'std_03',
    name: 'Rohan Verma',
    roll_number: 'VVIT/CS/2021/047',
    department: 'Information Technology',
    semester: 5,
    section: 'A',
    email: 'rohan.verma@vvit.edu',
    phone: '+91 98765 43212',
    photo_url: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200',
    attendance_rate: 68,
    cgpa: 7.30,
    guardian_name: 'Anil Verma',
    guardian_phone: '+91 98111 22235',
    address: 'Hubli, Karnataka',
    blood_group: 'A+',
    created_at: new Date('2026-09-03').toISOString()
  },
  {
    id: 'std_04',
    name: 'Ananya Iyer',
    roll_number: 'VVIT/EC/2021/012',
    department: 'Electronics & Communication',
    semester: 5,
    section: 'A',
    email: 'ananya.iyer@vvit.edu',
    phone: '+91 98765 43213',
    photo_url: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200',
    attendance_rate: 94,
    cgpa: 8.90,
    guardian_name: 'Gopal Iyer',
    guardian_phone: '+91 98111 22236',
    address: 'Bangalore, Karnataka',
    blood_group: 'AB+',
    created_at: new Date('2026-09-04').toISOString()
  }
];

const INITIAL_FACULTY = [
  {
    id: 'fac_01',
    employee_id: 'VVIT/CS/T-104',
    name: 'Dr. Rajesh Kumar',
    designation: 'Associate Professor',
    qualification: 'Ph.D. in Computer Science',
    department: 'Computer Science & Engineering',
    email: 'rajesh.kumar@vvit.edu',
    phone: '+91 98123 45678',
    cabin_room: 'Academic Block A - Room 204',
    experience_years: 16,
    subjects: ['Data Structures & Algorithms', 'Theory of Computation', 'Computer Networks'],
    photo_url: 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=200',
    linkedin: 'https://linkedin.com/in/dr-rajesh-kumar',
    created_at: new Date('2026-08-01').toISOString()
  },
  {
    id: 'fac_02',
    employee_id: 'VVIT/CS/T-105',
    name: 'Prof. Sarah Jenkins',
    designation: 'Professor & HOD',
    qualification: 'Ph.D. in Distributed Systems',
    department: 'Computer Science & Engineering',
    email: 'sarah.jenkins@vvit.edu',
    phone: '+91 98111 22331',
    cabin_room: 'Sci-Tech Block - Room 301',
    experience_years: 14,
    subjects: ['Database Management', 'Distributed Systems', 'Cloud Computing'],
    photo_url: 'https://images.unsplash.com/photo-1560250097-0b93528c311a?w=200',
    linkedin: 'https://linkedin.com/in/prof-sarah-jenkins',
    created_at: new Date('2026-08-02').toISOString()
  },
  {
    id: 'fac_03',
    employee_id: 'VVIT/EC/T-201',
    name: 'Dr. Vikramaditya Rao',
    designation: 'Professor',
    qualification: 'Ph.D. in VLSI Design',
    department: 'Electronics & Communication',
    email: 'vikram.rao@vvit.edu',
    phone: '+91 98111 22332',
    cabin_room: 'VLSI Lab Complex Room 12',
    experience_years: 18,
    subjects: ['Analog Electronics', 'Digital Signal Processing'],
    photo_url: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200',
    linkedin: 'https://linkedin.com/in/dr-vikram-rao',
    created_at: new Date('2026-08-03').toISOString()
  }
];

const INITIAL_STUDENT_TIMETABLE = [
  // MONDAY
  { id: 'stt_mon_1', day: 'MONDAY', start_time: '10:00 AM', end_time: '10:50 AM', duration: '50 Mins', subject_name: 'Python Programming', subject_code: 'BCC-302', teacher_name: 'Dharamraj Yadav (DRY)', room: 'LH-1', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: false, is_ongoing: false },
  { id: 'stt_mon_2', day: 'MONDAY', start_time: '10:50 AM', end_time: '11:40 AM', duration: '50 Mins', subject_name: 'Data Structure', subject_code: 'BCS-301', teacher_name: 'Kalpana Suman (KS)', room: 'LH-1', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: false, is_ongoing: false },
  { id: 'stt_mon_3', day: 'MONDAY', start_time: '11:40 AM', end_time: '12:30 PM', duration: '50 Mins', subject_name: 'Digital Electronics', subject_code: 'BOE-310', teacher_name: 'Anant Sikarwar (AS)', room: 'LH-1', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: false, is_ongoing: false },
  { id: 'stt_mon_4', day: 'MONDAY', start_time: '12:30 PM', end_time: '01:20 PM', duration: '50 Mins', subject_name: 'Python Programming', subject_code: 'BCC-302', teacher_name: 'Dharamraj Yadav (DRY)', room: 'LH-1', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: false, is_ongoing: false },
  { id: 'stt_mon_lunch', day: 'MONDAY', start_time: '01:20 PM', end_time: '02:20 PM', duration: '1 Hour', subject_name: 'Lunch Break', subject_code: 'LUNCH', teacher_name: 'College Campus', room: 'Cafeteria', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: true, is_ongoing: false },
  { id: 'stt_mon_5', day: 'MONDAY', start_time: '02:20 PM', end_time: '03:10 PM', duration: '50 Mins', subject_name: 'Computer Organization & Architecture', subject_code: 'BCS-302', teacher_name: 'Prakash Pandey (PP)', room: 'LH-1', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: false, is_ongoing: false },
  { id: 'stt_mon_6', day: 'MONDAY', start_time: '03:10 PM', end_time: '04:00 PM', duration: '50 Mins', subject_name: 'Digital Electronics', subject_code: 'BOE-310', teacher_name: 'Anant Sikarwar (AS)', room: 'LH-1', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: false, is_ongoing: false },
  { id: 'stt_mon_7', day: 'MONDAY', start_time: '04:00 PM', end_time: '04:50 PM', duration: '50 Mins', subject_name: 'Discrete Structure & Theory of Logic (T)', subject_code: 'BCS-303', teacher_name: 'Rishav Kumar Singh (RKS)', room: 'LH-1', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: false, is_ongoing: false },

  // TUESDAY
  { id: 'stt_tue_1', day: 'TUESDAY', start_time: '10:00 AM', end_time: '10:50 AM', duration: '50 Mins', subject_name: 'Technical Communication', subject_code: 'BAS-301', teacher_name: 'Dr. Pinki Negi Bora (PNB)', room: 'LH-1', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: false, is_ongoing: false },
  { id: 'stt_tue_2', day: 'TUESDAY', start_time: '10:50 AM', end_time: '11:40 AM', duration: '50 Mins', subject_name: 'Discrete Structure & Theory of Logic', subject_code: 'BCS-303', teacher_name: 'Rishav Kumar Singh (RKS)', room: 'LH-1', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: false, is_ongoing: false },
  { id: 'stt_tue_3', day: 'TUESDAY', start_time: '11:40 AM', end_time: '12:30 PM', duration: '50 Mins', subject_name: 'Computer Organization & Architecture', subject_code: 'BCS-302', teacher_name: 'Prakash Pandey (PP)', room: 'LH-1', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: false, is_ongoing: false },
  { id: 'stt_tue_4', day: 'TUESDAY', start_time: '12:30 PM', end_time: '01:20 PM', duration: '50 Mins', subject_name: 'Data Structure', subject_code: 'BCS-301', teacher_name: 'Kalpana Suman (KS)', room: 'LH-1', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: false, is_ongoing: false },
  { id: 'stt_tue_lunch', day: 'TUESDAY', start_time: '01:20 PM', end_time: '02:20 PM', duration: '1 Hour', subject_name: 'Lunch Break', subject_code: 'LUNCH', teacher_name: 'College Campus', room: 'Cafeteria', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: true, is_ongoing: false },
  { id: 'stt_tue_5', day: 'TUESDAY', start_time: '02:20 PM', end_time: '03:10 PM', duration: '50 Mins', subject_name: 'Digital Electronics', subject_code: 'BOE-310', teacher_name: 'Anant Sikarwar (AS)', room: 'LH-1', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: false, is_ongoing: false },
  { id: 'stt_tue_6', day: 'TUESDAY', start_time: '03:10 PM', end_time: '04:50 PM', duration: '1 Hr 40 Mins', subject_name: 'COA Lab G1', subject_code: 'BCS-352', teacher_name: 'Kalpana Suman (KS) / Dr. Prakash Kumar Singh (PKS)', room: 'COA Lab', batch_or_section: 'IT-3rd Sem', is_lab: true, is_break: false, is_ongoing: false },

  // WEDNESDAY
  { id: 'stt_wed_1', day: 'WEDNESDAY', start_time: '10:00 AM', end_time: '10:50 AM', duration: '50 Mins', subject_name: 'Technical Communication', subject_code: 'BAS-301', teacher_name: 'Dr. Pinki Negi Bora (PNB)', room: 'LH-1', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: false, is_ongoing: false },
  { id: 'stt_wed_2', day: 'WEDNESDAY', start_time: '10:50 AM', end_time: '12:30 PM', duration: '1 Hr 40 Mins', subject_name: 'COA Lab G2', subject_code: 'BCS-352', teacher_name: 'Akash Garg (AG) / Dharamraj Yadav (DRY)', room: 'COA Lab', batch_or_section: 'IT-3rd Sem', is_lab: true, is_break: false, is_ongoing: false },
  { id: 'stt_wed_4', day: 'WEDNESDAY', start_time: '12:30 PM', end_time: '01:20 PM', duration: '50 Mins', subject_name: 'Data Structure', subject_code: 'BCS-301', teacher_name: 'Kalpana Suman (KS)', room: 'LH-1', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: false, is_ongoing: false },
  { id: 'stt_wed_lunch', day: 'WEDNESDAY', start_time: '01:20 PM', end_time: '02:20 PM', duration: '1 Hour', subject_name: 'Lunch Break', subject_code: 'LUNCH', teacher_name: 'College Campus', room: 'Cafeteria', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: true, is_ongoing: false },
  { id: 'stt_wed_5', day: 'WEDNESDAY', start_time: '02:20 PM', end_time: '04:00 PM', duration: '1 Hr 40 Mins', subject_name: 'Web Designing Workshop G2', subject_code: 'BCS-353', teacher_name: 'Akash Garg (AG) / Dr. Prakash Kumar Singh (PKS)', room: 'Web Tech Lab', batch_or_section: 'IT-3rd Sem', is_lab: true, is_break: false, is_ongoing: false },
  { id: 'stt_wed_7', day: 'WEDNESDAY', start_time: '04:00 PM', end_time: '04:50 PM', duration: '50 Mins', subject_name: 'Computer Organization & Architecture', subject_code: 'BCS-302', teacher_name: 'Prakash Pandey (PP)', room: 'LH-1', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: false, is_ongoing: false },
  { id: 'stt_wed_8', day: 'WEDNESDAY', start_time: '04:50 PM', end_time: '05:40 PM', duration: '50 Mins', subject_name: 'Sports & Yoga', subject_code: 'SPORTS', teacher_name: 'Sports Department', room: 'Sports Ground', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: false, is_ongoing: false },

  // THURSDAY
  { id: 'stt_thu_1', day: 'THURSDAY', start_time: '10:00 AM', end_time: '10:50 AM', duration: '50 Mins', subject_name: 'Technical Communication', subject_code: 'BAS-301', teacher_name: 'Dr. Pinki Negi Bora (PNB)', room: 'LH-1', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: false, is_ongoing: false },
  { id: 'stt_thu_2', day: 'THURSDAY', start_time: '10:50 AM', end_time: '11:40 AM', duration: '50 Mins', subject_name: 'Discrete Structure & Theory of Logic', subject_code: 'BCS-303', teacher_name: 'Rishav Kumar Singh (RKS)', room: 'LH-1', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: false, is_ongoing: false },
  { id: 'stt_thu_3', day: 'THURSDAY', start_time: '11:40 AM', end_time: '01:20 PM', duration: '1 Hr 40 Mins', subject_name: 'Web Designing Workshop G1', subject_code: 'BCS-353', teacher_name: 'Kalpana Suman (KS) / Anant Sikarwar (AS)', room: 'Web Tech Lab', batch_or_section: 'IT-3rd Sem', is_lab: true, is_break: false, is_ongoing: false },
  { id: 'stt_thu_lunch', day: 'THURSDAY', start_time: '01:20 PM', end_time: '02:20 PM', duration: '1 Hour', subject_name: 'Lunch Break', subject_code: 'LUNCH', teacher_name: 'College Campus', room: 'Cafeteria', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: true, is_ongoing: false },
  { id: 'stt_thu_5', day: 'THURSDAY', start_time: '02:20 PM', end_time: '04:00 PM', duration: '1 Hr 40 Mins', subject_name: 'Data Structure Lab G1', subject_code: 'BCS-351', teacher_name: 'Kalpana Suman (KS) / Dr. Prakash Kumar Singh (PKS)', room: 'DS Lab', batch_or_section: 'IT-3rd Sem', is_lab: true, is_break: false, is_ongoing: false },
  { id: 'stt_thu_7', day: 'THURSDAY', start_time: '04:00 PM', end_time: '04:50 PM', duration: '50 Mins', subject_name: 'Computer Organization & Architecture', subject_code: 'BCS-302', teacher_name: 'Prakash Pandey (PP)', room: 'LH-1', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: false, is_ongoing: false },
  { id: 'stt_thu_8', day: 'THURSDAY', start_time: '04:50 PM', end_time: '05:40 PM', duration: '50 Mins', subject_name: 'Sports & Yoga', subject_code: 'SPORTS', teacher_name: 'Sports Department', room: 'Sports Ground', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: false, is_ongoing: false },

  // FRIDAY
  { id: 'stt_fri_1', day: 'FRIDAY', start_time: '10:00 AM', end_time: '10:50 AM', duration: '50 Mins', subject_name: 'Data Structure', subject_code: 'BCS-301', teacher_name: 'Kalpana Suman (KS)', room: 'LH-1', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: false, is_ongoing: false },
  { id: 'stt_fri_2', day: 'FRIDAY', start_time: '10:50 AM', end_time: '12:30 PM', duration: '1 Hr 40 Mins', subject_name: 'Internship Assessment / Mini Project', subject_code: 'BCC-351', teacher_name: 'Dr. Prakash Kumar Singh (PKS)', room: 'Project Lab', batch_or_section: 'IT-3rd Sem', is_lab: true, is_break: false, is_ongoing: false },
  { id: 'stt_fri_4', day: 'FRIDAY', start_time: '12:30 PM', end_time: '01:20 PM', duration: '50 Mins', subject_name: 'Digital Electronics (Tutorial)', subject_code: 'BOE-310', teacher_name: 'Anant Sikarwar (AS)', room: 'LH-1', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: false, is_ongoing: false },
  { id: 'stt_fri_lunch', day: 'FRIDAY', start_time: '01:20 PM', end_time: '02:20 PM', duration: '1 Hour', subject_name: 'Lunch Break', subject_code: 'LUNCH', teacher_name: 'College Campus', room: 'Cafeteria', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: true, is_ongoing: false },
  { id: 'stt_fri_5', day: 'FRIDAY', start_time: '02:20 PM', end_time: '04:00 PM', duration: '1 Hr 40 Mins', subject_name: 'Data Structure Lab G2', subject_code: 'BCS-351', teacher_name: 'Kalpana Suman (KS) / Anant Sikarwar (AS)', room: 'DS Lab', batch_or_section: 'IT-3rd Sem', is_lab: true, is_break: false, is_ongoing: false },
  { id: 'stt_fri_7', day: 'FRIDAY', start_time: '04:00 PM', end_time: '04:50 PM', duration: '50 Mins', subject_name: 'Library / Self-Study', subject_code: 'LIB', teacher_name: 'Librarian', room: 'Central Library', batch_or_section: 'IT-3rd Sem', is_lab: false, is_break: false, is_ongoing: false }
];

const INITIAL_PROFESSOR_TIMETABLE = [
  {
    id: 'ftt_01',
    day: 'MONDAY',
    start_time: '09:00 AM',
    end_time: '10:00 AM',
    subject_name: 'Theory of Computation',
    subject_code: 'CS-501',
    teacher_name: 'Dr. Rajesh Kumar',
    teacher_id: 'fac_01',
    room: 'Room 402',
    batch_or_section: 'CSE-5B',
    lecture_type: 'Lecture',
    is_lab: false
  },
  {
    id: 'ftt_02',
    day: 'MONDAY',
    start_time: '10:00 AM',
    end_time: '11:00 AM',
    subject_name: 'Computer Networks Lab',
    subject_code: 'CS-502L',
    teacher_name: 'Dr. Rajesh Kumar',
    teacher_id: 'fac_01',
    room: 'Network Lab 2',
    batch_or_section: 'CSE-5B',
    lecture_type: 'Lab Session',
    is_lab: true
  },
  {
    id: 'ftt_03',
    day: 'MONDAY',
    start_time: '11:30 AM',
    end_time: '12:30 PM',
    subject_name: 'Database Management',
    subject_code: 'CS-503',
    teacher_name: 'Prof. Sarah Jenkins',
    teacher_id: 'fac_02',
    room: 'Room 402',
    batch_or_section: 'CSE-5B',
    lecture_type: 'Lecture',
    is_lab: false
  },
  {
    id: 'ftt_04',
    day: 'TUESDAY',
    start_time: '10:00 AM',
    end_time: '11:00 AM',
    subject_name: 'Operating Systems',
    subject_code: 'CS-505',
    teacher_name: 'Dr. Rajesh Kumar',
    teacher_id: 'fac_01',
    room: 'Room 402',
    batch_or_section: 'CSE-5B',
    lecture_type: 'Lecture',
    is_lab: false
  },
  {
    id: 'ftt_05',
    day: 'WEDNESDAY',
    start_time: '09:00 AM',
    end_time: '11:00 AM',
    subject_name: 'DBMS Project Lab',
    subject_code: 'CS-503L',
    teacher_name: 'Prof. Sarah Jenkins',
    teacher_id: 'fac_02',
    room: 'Software Lab 1',
    batch_or_section: 'CSE-5B',
    lecture_type: 'Lab Session',
    is_lab: true
  }
];

const INITIAL_STUDENT_ATTENDANCE = [];

const INITIAL_FACULTY_ATTENDANCE = [
  {
    id: 'att_f_01',
    faculty_id: 'fac_01',
    faculty_name: 'Dr. Rajesh Kumar',
    employee_id: 'VVIT/CS/T-104',
    department: 'Computer Science & Engineering',
    date: '2026-10-03',
    status: 'PRESENT',
    check_in_time: '08:45 AM',
    timestamp: new Date().toISOString()
  },
  {
    id: 'att_f_02',
    faculty_id: 'fac_02',
    faculty_name: 'Prof. Sarah Jenkins',
    employee_id: 'VVIT/CS/T-105',
    department: 'Computer Science & Engineering',
    date: '2026-10-03',
    status: 'PRESENT',
    check_in_time: '08:52 AM',
    timestamp: new Date().toISOString()
  },
  {
    id: 'att_f_03',
    faculty_id: 'fac_03',
    faculty_name: 'Dr. Vikramaditya Rao',
    employee_id: 'VVIT/EC/T-201',
    department: 'Electronics & Communication',
    date: '2026-10-03',
    status: 'ON_LEAVE',
    check_in_time: '-',
    timestamp: new Date().toISOString()
  }
];

const INITIAL_CIRCULARS = [
  {
    id: 'cir_01',
    title: 'Mid-Term Examinations Schedule Announced',
    description: 'All students and faculty members are hereby notified that Mid-Semester Examinations for Odd Semester 2026 will commence from October 20th. Detailed hall tickets will be issued shortly.',
    date: '2026-10-02',
    category: 'EXAMINATION',
    target: 'ALL',
    is_urgent: true,
    is_published: true,
    published_by: 'Controller of Examinations',
    created_at: new Date('2026-10-02').toISOString()
  },
  {
    id: 'cir_02',
    title: 'Annual Technical Symposium - TechNova 2026',
    description: 'Department of Computer Science is hosting TechNova 2026 on October 28-29. Registrations are open for coding contests, hackathons, and paper presentations.',
    date: '2026-10-01',
    category: 'EVENT',
    target: 'ALL',
    is_urgent: false,
    is_published: true,
    published_by: 'Dean Student Affairs',
    created_at: new Date('2026-10-01').toISOString()
  },
  {
    id: 'cir_03',
    title: 'Faculty Academic Council Meeting',
    description: 'A mandatory meeting for all faculty members will be conducted in Senate Hall on Monday at 03:30 PM regarding curriculum revision and NBA accreditation metrics.',
    date: '2026-10-03',
    category: 'ACADEMICS',
    target: 'FACULTY',
    is_urgent: true,
    is_published: true,
    published_by: 'Principal Desk',
    created_at: new Date('2026-10-03').toISOString()
  }
];

// Helper to get/set local storage with initial fallback
function getLocal(key, initialVal) {
  try {
    const raw = localStorage.getItem(`vvit_${key}`);
    if (!raw) {
      localStorage.setItem(`vvit_${key}`, JSON.stringify(initialVal));
      return initialVal;
    }
    return JSON.parse(raw);
  } catch (e) {
    return initialVal;
  }
}

function setLocal(key, data) {
  try {
    localStorage.setItem(`vvit_${key}`, JSON.stringify(data));
  } catch (e) {
    console.error('LocalStorage error:', e);
  }
}

export const dataService = {
  // -------------------------------------------------------------
  // STORAGE & STUDENTS CRUD
  // -------------------------------------------------------------
  async uploadStudentPhoto(file) {
    if (!file) return { url: null, error: null };
    try {
      const fileExt = file.name.split('.').pop()?.toLowerCase() || 'jpg';
      const cleanFileName = `std_${Date.now()}_${Math.random().toString(36).substring(2, 8)}.${fileExt}`;

      const { data, error } = await supabase.storage
        .from('student-photos')
        .upload(cleanFileName, file, {
          cacheControl: '3600',
          upsert: true
        });

      if (error) {
        console.warn('Supabase storage upload error:', error);
        return { url: null, error: error.message };
      }

      const { data: publicUrlData } = supabase.storage
        .from('student-photos')
        .getPublicUrl(cleanFileName);

      return { url: publicUrlData?.publicUrl || null, error: null };
    } catch (e) {
      console.warn('uploadStudentPhoto exception:', e);
      return { url: null, error: e.message };
    }
  },

  async uploadFacultyPhoto(file) {
    if (!file) return { url: null, error: null };
    try {
      const fileExt = file.name.split('.').pop()?.toLowerCase() || 'jpg';
      const cleanFileName = `fac_${Date.now()}_${Math.random().toString(36).substring(2, 8)}.${fileExt}`;

      let bucket = 'faculty-photos';
      let { data, error } = await supabase.storage
        .from(bucket)
        .upload(cleanFileName, file, {
          cacheControl: '3600',
          upsert: true
        });

      if (error && (error.message?.includes('bucket') || error.statusCode === '404' || error.error === 'Bucket not found')) {
        bucket = 'student-photos';
        const fallbackRes = await supabase.storage
          .from(bucket)
          .upload(cleanFileName, file, {
            cacheControl: '3600',
            upsert: true
          });
        data = fallbackRes.data;
        error = fallbackRes.error;
      }

      if (error) {
        console.warn('Supabase storage faculty upload error:', error);
        return { url: null, error: error.message };
      }

      const { data: publicUrlData } = supabase.storage
        .from(bucket)
        .getPublicUrl(cleanFileName);

      return { url: publicUrlData?.publicUrl || null, error: null };
    } catch (e) {
      console.warn('uploadFacultyPhoto exception:', e);
      return { url: null, error: e.message };
    }
  },

  async getStudents() {
    try {
      const { data, error } = await supabase
        .from('students')
        .select('*')
        .order('created_at', { ascending: false });

      if (!error && Array.isArray(data) && data.length > 0) {
        setLocal('students', data);
        return { data, source: 'supabase' };
      }
    } catch (e) {
      console.warn('Supabase getStudents exception:', e);
    }
    return { data: getLocal('students', INITIAL_STUDENTS), source: 'local' };
  },

  // -------------------------------------------------------------
  // HELPER: Create Supabase Auth Account (RPC first, then signUp fallback)
  // -------------------------------------------------------------
  async createAuthAccount({ email, password, role, name, extraData = {} }) {
    if (!email || !password) return { userId: null, error: null };
    const cleanEmail = email.trim();

    // 1. Try PostgreSQL RPC create_user_account (Bypasses email rate limits & confirmed instantly)
    try {
      const { data: rpcRes, error: rpcErr } = await supabase.rpc('create_user_account', {
        new_email: cleanEmail,
        new_password: password,
        user_role: role,
        full_name: name
      });

      if (!rpcErr && rpcRes && rpcRes.success) {
        console.log(`[Supabase Auth] Created ${role} via RPC successfully:`, rpcRes.user_id);
        return { userId: rpcRes.user_id, error: null, method: 'rpc' };
      }
      if (rpcErr && rpcErr.code !== 'PGRST202') {
        console.warn('RPC create_user_account error:', rpcErr);
      }
    } catch (rpcEx) {
      console.warn('RPC create_user_account exception:', rpcEx);
    }

    // 2. Fallback to standard Supabase Auth signUp (Works if "Confirm email" is turned OFF in Supabase Dashboard)
    try {
      const { data: authData, error: authError } = await authClient.auth.signUp({
        email: cleanEmail,
        password: password,
        options: {
          data: {
            role,
            name,
            ...extraData
          }
        }
      });

      if (authError) {
        console.warn(`Supabase Auth ${role} signUp error:`, authError);
        return { userId: null, error: authError.message, method: 'signUp' };
      }

      if (authData?.user?.id) {
        console.log(`[Supabase Auth] Created ${role} via signUp successfully:`, authData.user.id);
        return { userId: authData.user.id, error: null, method: 'signUp' };
      }
    } catch (authEx) {
      console.warn(`Supabase Auth ${role} signUp exception:`, authEx);
      return { userId: null, error: authEx.message, method: 'signUp' };
    }

    return { userId: null, error: 'Could not create account in Supabase Auth.', method: 'none' };
  },

  async addStudent(student) {
    let authUserId = null;
    let authErrorMsg = null;

    // 1. Create authentication account in Supabase Auth if email and password provided
    if (student.email && student.password) {
      const authResult = await this.createAuthAccount({
        email: student.email,
        password: student.password,
        role: 'student',
        name: student.name,
        extraData: {
          roll_number: student.roll_number,
          department: student.department || 'General'
        }
      });

      authUserId = authResult.userId;
      authErrorMsg = authResult.error;
    }

    // Strictly DO NOT store password in public database table
    const { password, ...studentProfile } = student;

    const newStudent = {
      ...studentProfile,
      department: studentProfile.department || 'General',
      id: student.id || (authUserId ? `std_${authUserId.substring(0, 8)}` : `std_${Date.now()}`),
      ...(authUserId ? { auth_user_id: authUserId } : {}),
      created_at: new Date().toISOString()
    };

    // Update local cache immediately (WITHOUT password)
    const current = getLocal('students', INITIAL_STUDENTS);
    const updated = [newStudent, ...current.filter(s => s.id !== newStudent.id)];
    setLocal('students', updated);

    // Try Supabase sync
    try {
      let insertPayload = { ...newStudent };
      let { data, error } = await supabase.from('students').insert(insertPayload).select();

      // Fallback: If auth_user_id or admission_year column is not yet in PostgreSQL schema, retry without it
      if (error && (error.message?.includes('auth_user_id') || error.message?.includes('admission_year') || error.code === 'PGRST204')) {
        let retryPayload = { ...insertPayload };
        if (error.message?.includes('admission_year') || error.code === 'PGRST204') delete retryPayload.admission_year;
        if (error.message?.includes('auth_user_id')) delete retryPayload.auth_user_id;
        const retry = await supabase.from('students').insert(retryPayload).select();
        if (!retry.error && retry.data && retry.data.length > 0) {
          data = retry.data;
          error = null;
        }
      }

      if (!error && data && data.length > 0) {
        return { 
          data: data[0], 
          source: 'supabase', 
          authCreated: !!authUserId, 
          authUserId, 
          authError: authErrorMsg 
        };
      }
    } catch (e) {
      console.warn('Supabase addStudent exception:', e);
    }

    return { 
      data: newStudent, 
      source: 'local', 
      authCreated: !!authUserId, 
      authUserId, 
      authError: authErrorMsg 
    };
  },

  async updateStudent(id, updates) {
    const { password, ...cleanedUpdates } = updates;
    const current = getLocal('students', INITIAL_STUDENTS);
    const updated = current.map(s => s.id === id ? { ...s, ...cleanedUpdates } : s);
    setLocal('students', updated);

    try {
      let updatePayload = { ...cleanedUpdates };
      let { data, error } = await supabase
        .from('students')
        .update(updatePayload)
        .eq('id', id)
        .select();

      if (error && (error.message?.includes('admission_year') || error.code === 'PGRST204')) {
        delete updatePayload.admission_year;
        const retry = await supabase
          .from('students')
          .update(updatePayload)
          .eq('id', id)
          .select();
        if (!retry.error && retry.data && retry.data.length > 0) {
          data = retry.data;
          error = null;
        }
      }

      if (!error && data && data.length > 0) {
        return { data: data[0], source: 'supabase' };
      }
    } catch (e) {
      console.warn('Supabase updateStudent exception:', e);
    }

    return { data: updated.find(s => s.id === id), source: 'local' };
  },

  async deleteStudent(id) {
    const current = getLocal('students', INITIAL_STUDENTS);
    const updated = current.filter(s => s.id !== id);
    setLocal('students', updated);

    try {
      await supabase.from('students').delete().eq('id', id);
    } catch (e) {
      console.warn('Supabase deleteStudent exception:', e);
    }
    return true;
  },

  // -------------------------------------------------------------
  // FACULTY CRUD
  // -------------------------------------------------------------
  async getFaculty() {
    try {
      const { data, error } = await supabase
        .from('faculty')
        .select('*')
        .order('created_at', { ascending: false });

      if (!error && Array.isArray(data) && data.length > 0) {
        setLocal('faculty', data);
        return { data, source: 'supabase' };
      }
    } catch (e) {
      console.warn('Supabase getFaculty exception:', e);
    }
    return { data: getLocal('faculty', INITIAL_FACULTY), source: 'local' };
  },

  async addFaculty(faculty) {
    let authUserId = null;
    let authErrorMsg = null;

    // 1. Create authentication account in Supabase Auth if email and password provided
    if (faculty.email && faculty.password) {
      const authResult = await this.createAuthAccount({
        email: faculty.email,
        password: faculty.password,
        role: 'professor',
        name: faculty.name,
        extraData: {
          employee_id: faculty.employee_id,
          department: faculty.department
        }
      });

      authUserId = authResult.userId;
      authErrorMsg = authResult.error;
    }

    // Strictly DO NOT store password in public database table
    const { password, ...facultyProfile } = faculty;

    const newFaculty = {
      ...facultyProfile,
      id: faculty.id || (authUserId ? `fac_${authUserId.substring(0, 8)}` : `fac_${Date.now()}`),
      ...(authUserId ? { auth_user_id: authUserId } : {}),
      created_at: new Date().toISOString()
    };

    const current = getLocal('faculty', INITIAL_FACULTY);
    const updated = [newFaculty, ...current.filter(f => f.id !== newFaculty.id)];
    setLocal('faculty', updated);

    try {
      let insertPayload = { ...newFaculty };
      let { data, error } = await supabase.from('faculty').insert(insertPayload).select();

      // Fallback: If auth_user_id column is not yet in PostgreSQL schema, retry without it
      if (error && (error.message?.includes('auth_user_id') || error.code === 'PGRST204')) {
        delete insertPayload.auth_user_id;
        const retry = await supabase.from('faculty').insert(insertPayload).select();
        if (!retry.error && retry.data && retry.data.length > 0) {
          data = retry.data;
          error = null;
        }
      }

      if (!error && data && data.length > 0) {
        return { 
          data: data[0], 
          source: 'supabase', 
          authCreated: !!authUserId, 
          authUserId, 
          authError: authErrorMsg 
        };
      }
    } catch (e) {
      console.warn('Supabase addFaculty exception:', e);
    }

    return { 
      data: newFaculty, 
      source: 'local', 
      authCreated: !!authUserId, 
      authUserId, 
      authError: authErrorMsg 
    };
  },

  async updateFaculty(id, updates) {
    const current = getLocal('faculty', INITIAL_FACULTY);
    const updated = current.map(f => f.id === id ? { ...f, ...updates } : f);
    setLocal('faculty', updated);

    try {
      const { data, error } = await supabase
        .from('faculty')
        .update(updates)
        .eq('id', id)
        .select();

      if (!error && data && data.length > 0) {
        return { data: data[0], source: 'supabase' };
      }
    } catch (e) {
      console.warn('Supabase updateFaculty exception:', e);
    }

    return { data: updated.find(f => f.id === id), source: 'local' };
  },

  async deleteFaculty(id) {
    const current = getLocal('faculty', INITIAL_FACULTY);
    const updated = current.filter(f => f.id !== id);
    setLocal('faculty', updated);

    try {
      await supabase.from('faculty').delete().eq('id', id);
    } catch (e) {
      console.warn('Supabase deleteFaculty exception:', e);
    }
    return true;
  },

  // -------------------------------------------------------------
  // TIMETABLE — STUDENT
  // -------------------------------------------------------------
  async getStudentTimetable(dayFilter = null) {
    try {
      let query = supabase.from('timetable_student').select('*').order('start_time', { ascending: true });
      if (dayFilter && dayFilter !== 'ALL') {
        query = query.eq('day', dayFilter);
      }
      const { data, error } = await query;
      if (!error && Array.isArray(data) && data.length > 0) {
        setLocal('student_timetable', data);
        return { data, source: 'supabase' };
      }
    } catch (e) {
      console.warn('Supabase getStudentTimetable exception:', e);
    }
    const all = getLocal('student_timetable', INITIAL_STUDENT_TIMETABLE);
    const filtered = (dayFilter && dayFilter !== 'ALL')
      ? all.filter(t => t.day.toUpperCase() === dayFilter.toUpperCase())
      : all;
    return { data: filtered, source: 'local' };
  },

  async addStudentTimetableSlot(slot) {
    const newSlot = {
      ...slot,
      id: slot.id || `stt_${Date.now()}`
    };

    const current = getLocal('student_timetable', INITIAL_STUDENT_TIMETABLE);
    const updated = [...current, newSlot];
    setLocal('student_timetable', updated);

    try {
      const { data, error } = await supabase.from('timetable_student').insert(newSlot).select();
      if (!error && data && data.length > 0) {
        return { data: data[0], source: 'supabase' };
      }
    } catch (e) {
      console.warn('Supabase addStudentTimetableSlot exception:', e);
    }
    return { data: newSlot, source: 'local' };
  },

  async updateStudentTimetableSlot(id, updates) {
    const current = getLocal('student_timetable', INITIAL_STUDENT_TIMETABLE);
    const updated = current.map(t => t.id === id ? { ...t, ...updates } : t);
    setLocal('student_timetable', updated);

    try {
      const { data, error } = await supabase
        .from('timetable_student')
        .update(updates)
        .eq('id', id)
        .select();

      if (!error && data && data.length > 0) {
        return { data: data[0], source: 'supabase' };
      }
    } catch (e) {
      console.warn('Supabase updateStudentTimetableSlot exception:', e);
    }
    return { data: updated.find(t => t.id === id), source: 'local' };
  },

  async deleteStudentTimetableSlot(id) {
    const current = getLocal('student_timetable', INITIAL_STUDENT_TIMETABLE);
    const updated = current.filter(t => t.id !== id);
    setLocal('student_timetable', updated);

    try {
      await supabase.from('timetable_student').delete().eq('id', id);
    } catch (e) {
      console.warn('Supabase deleteStudentTimetableSlot exception:', e);
    }
    return true;
  },

  // -------------------------------------------------------------
  // TIMETABLE — PROFESSOR / FACULTY
  // -------------------------------------------------------------
  async getProfessorTimetable(teacherFilter = null, dayFilter = null) {
    try {
      let query = supabase.from('timetable_faculty').select('*').order('start_time', { ascending: true });
      if (dayFilter && dayFilter !== 'ALL') {
        query = query.eq('day', dayFilter);
      }
      if (teacherFilter && teacherFilter !== 'ALL') {
        query = query.eq('teacher_name', teacherFilter);
      }
      const { data, error } = await query;
      if (!error && Array.isArray(data) && data.length > 0) {
        setLocal('professor_timetable', data);
        return { data, source: 'supabase' };
      }
    } catch (e) {
      console.warn('Supabase getProfessorTimetable exception:', e);
    }

    let all = getLocal('professor_timetable', INITIAL_PROFESSOR_TIMETABLE);
    if (dayFilter && dayFilter !== 'ALL') {
      all = all.filter(t => t.day.toUpperCase() === dayFilter.toUpperCase());
    }
    if (teacherFilter && teacherFilter !== 'ALL') {
      all = all.filter(t => t.teacher_name.toLowerCase().includes(teacherFilter.toLowerCase()));
    }
    return { data: all, source: 'local' };
  },

  async addProfessorTimetableSlot(slot) {
    const newSlot = {
      ...slot,
      id: slot.id || `ftt_${Date.now()}`
    };

    const current = getLocal('professor_timetable', INITIAL_PROFESSOR_TIMETABLE);
    const updated = [...current, newSlot];
    setLocal('professor_timetable', updated);

    try {
      const { data, error } = await supabase.from('timetable_faculty').insert(newSlot).select();
      if (!error && data && data.length > 0) {
        return { data: data[0], source: 'supabase' };
      }
    } catch (e) {
      console.warn('Supabase addProfessorTimetableSlot exception:', e);
    }
    return { data: newSlot, source: 'local' };
  },

  async updateProfessorTimetableSlot(id, updates) {
    const current = getLocal('professor_timetable', INITIAL_PROFESSOR_TIMETABLE);
    const updated = current.map(t => t.id === id ? { ...t, ...updates } : t);
    setLocal('professor_timetable', updated);

    try {
      const { data, error } = await supabase
        .from('timetable_faculty')
        .update(updates)
        .eq('id', id)
        .select();

      if (!error && data && data.length > 0) {
        return { data: data[0], source: 'supabase' };
      }
    } catch (e) {
      console.warn('Supabase updateProfessorTimetableSlot exception:', e);
    }
    return { data: updated.find(t => t.id === id), source: 'local' };
  },

  async deleteProfessorTimetableSlot(id) {
    const current = getLocal('professor_timetable', INITIAL_PROFESSOR_TIMETABLE);
    const updated = current.filter(t => t.id !== id);
    setLocal('professor_timetable', updated);

    try {
      await supabase.from('timetable_faculty').delete().eq('id', id);
    } catch (e) {
      console.warn('Supabase deleteProfessorTimetableSlot exception:', e);
    }
    return true;
  },

  // -------------------------------------------------------------
  // ATTENDANCE — STUDENT
  // -------------------------------------------------------------
  async getStudentAttendance(dateFilter = null) {
    try {
      let query = supabase.from('attendance_student').select('*').order('timestamp', { ascending: false });
      if (dateFilter) {
        query = query.eq('date', dateFilter);
      }
      const { data, error } = await query;
      if (!error && Array.isArray(data)) {
        setLocal('student_attendance', data);
        return { data, source: 'supabase' };
      }
      if (error) {
        console.warn('Supabase getStudentAttendance error:', error);
      }
    } catch (e) {
      console.warn('Supabase getStudentAttendance exception:', e);
    }
    const all = getLocal('student_attendance', []);
    const filtered = dateFilter ? all.filter(a => a.date === dateFilter) : all;
    return { data: filtered, source: 'local' };
  },

  async recordStudentAttendance(record) {
    const newRecord = {
      ...record,
      id: record.id || `att_s_${Date.now()}`,
      timestamp: new Date().toISOString()
    };

    const current = getLocal('student_attendance', []);
    const updated = [newRecord, ...current];
    setLocal('student_attendance', updated);

    try {
      const { data, error } = await supabase.from('attendance_student').insert(newRecord).select();
      if (!error && data && data.length > 0) {
        // Automatically recalculate and sync student attendance percentage
        try {
          const roll = record.roll_number;
          const sId = record.student_id;
          let query = supabase.from('attendance_student').select('status');
          if (roll) {
            query = query.eq('roll_number', roll);
          } else if (sId) {
            query = query.eq('student_id', sId);
          }
          const { data: allRecords } = await query;
          if (allRecords && allRecords.length > 0) {
            const present = allRecords.filter(r => r.status?.toUpperCase() === 'PRESENT').length;
            const rate = Math.round((present / allRecords.length) * 100);
            if (roll) {
              await supabase.from('students').update({ attendance_rate: rate }).eq('roll_number', roll);
            }
            if (sId) {
              await supabase.from('students').update({ attendance_rate: rate }).eq('id', sId);
            }
          }
        } catch (calcErr) {
          console.warn('Recalculate error:', calcErr);
        }
        return { data: data[0], source: 'supabase' };
      }
    } catch (e) {
      console.warn('Supabase recordStudentAttendance exception:', e);
    }
    return { data: newRecord, source: 'local' };
  },

  // -------------------------------------------------------------
  // ATTENDANCE — FACULTY
  // -------------------------------------------------------------
  async getFacultyAttendance(dateFilter = null) {
    try {
      let query = supabase.from('attendance_faculty').select('*').order('timestamp', { ascending: false });
      if (dateFilter) {
        query = query.eq('date', dateFilter);
      }
      const { data, error } = await query;
      if (!error && Array.isArray(data) && data.length > 0) {
        setLocal('faculty_attendance', data);
        return { data, source: 'supabase' };
      }
    } catch (e) {
      console.warn('Supabase getFacultyAttendance exception:', e);
    }
    const all = getLocal('faculty_attendance', INITIAL_FACULTY_ATTENDANCE);
    const filtered = dateFilter ? all.filter(a => a.date === dateFilter) : all;
    return { data: filtered, source: 'local' };
  },

  async recordFacultyAttendance(record) {
    const newRecord = {
      ...record,
      id: record.id || `att_f_${Date.now()}`,
      timestamp: new Date().toISOString()
    };

    const current = getLocal('faculty_attendance', INITIAL_FACULTY_ATTENDANCE);
    const updated = [newRecord, ...current];
    setLocal('faculty_attendance', updated);

    try {
      const { data, error } = await supabase.from('attendance_faculty').insert(newRecord).select();
      if (!error && data && data.length > 0) {
        return { data: data[0], source: 'supabase' };
      }
    } catch (e) {
      console.warn('Supabase recordFacultyAttendance exception:', e);
    }
    return { data: newRecord, source: 'local' };
  },

  // -------------------------------------------------------------
  // CIRCULARS CRUD
  // -------------------------------------------------------------
  async getCirculars() {
    try {
      const { data, error } = await supabase
        .from('circulars')
        .select('*')
        .order('date', { ascending: false });

      if (!error && Array.isArray(data) && data.length > 0) {
        setLocal('circulars', data);
        return { data, source: 'supabase' };
      }
    } catch (e) {
      console.warn('Supabase getCirculars exception:', e);
    }
    return { data: getLocal('circulars', INITIAL_CIRCULARS), source: 'local' };
  },

  async addCircular(circular) {
    const newCircular = {
      ...circular,
      id: circular.id || `cir_${Date.now()}`,
      created_at: new Date().toISOString(),
      is_published: circular.is_published !== undefined ? circular.is_published : true
    };

    const current = getLocal('circulars', INITIAL_CIRCULARS);
    const updated = [newCircular, ...current.filter(c => c.id !== newCircular.id)];
    setLocal('circulars', updated);

    try {
      const { data, error } = await supabase.from('circulars').insert(newCircular).select();
      if (!error && data && data.length > 0) {
        return { data: data[0], source: 'supabase' };
      }
    } catch (e) {
      console.warn('Supabase addCircular exception:', e);
    }
    return { data: newCircular, source: 'local' };
  },

  async updateCircular(id, updates) {
    const current = getLocal('circulars', INITIAL_CIRCULARS);
    const updated = current.map(c => c.id === id ? { ...c, ...updates } : c);
    setLocal('circulars', updated);

    try {
      const { data, error } = await supabase
        .from('circulars')
        .update(updates)
        .eq('id', id)
        .select();

      if (!error && data && data.length > 0) {
        return { data: data[0], source: 'supabase' };
      }
    } catch (e) {
      console.warn('Supabase updateCircular exception:', e);
    }
    return { data: updated.find(c => c.id === id), source: 'local' };
  },

  async deleteCircular(id) {
    const current = getLocal('circulars', INITIAL_CIRCULARS);
    const updated = current.filter(c => c.id !== id);
    setLocal('circulars', updated);

    try {
      await supabase.from('circulars').delete().eq('id', id);
    } catch (e) {
      console.warn('Supabase deleteCircular exception:', e);
    }
    return true;
  },

  // -------------------------------------------------------------
  // MASTER SUBJECTS CRUD
  // -------------------------------------------------------------
  async getSubjects() {
    try {
      const { data, error } = await supabase
        .from('subjects')
        .select('*')
        .order('code', { ascending: true });

      if (!error && Array.isArray(data) && data.length > 0) {
        setLocal('subjects', data);
        return { data, source: 'supabase' };
      }
    } catch (e) {
      console.warn('Supabase getSubjects note:', e);
    }
    return { data: getLocal('subjects', MASTER_SUBJECTS), source: 'local' };
  },

  async addSubject(subj) {
    const code = (subj.code || '').trim().toUpperCase();
    const name = (subj.name || '').trim();
    if (!name || !code) {
      throw new Error('Subject name and subject code are required.');
    }
    const current = getLocal('subjects', MASTER_SUBJECTS);
    const existing = current.find(s => s.code.toUpperCase() === code || s.name.toLowerCase() === name.toLowerCase());
    if (existing) {
      throw new Error(`Subject with code "${code}" or name "${name}" already exists.`);
    }

    const newSubj = {
      id: subj.id || `sub_${code.toLowerCase().replace(/[^a-z0-9]/g, '')}`,
      name,
      code,
      displayName: `${name} (${code})`,
      status: subj.status || 'Active'
    };
    const updated = [...current, newSubj];
    setLocal('subjects', updated);

    try {
      const { data, error } = await supabase.from('subjects').insert(newSubj).select();
      if (!error && data && data.length > 0) {
        return { data: data[0], source: 'supabase' };
      }
    } catch (e) {
      console.warn('Supabase addSubject note:', e);
    }
    return { data: newSubj, source: 'local' };
  },

  async updateSubject(id, updates) {
    const current = getLocal('subjects', MASTER_SUBJECTS);
    const updated = current.map(s => s.id === id ? { ...s, ...updates } : s);
    setLocal('subjects', updated);

    try {
      const { data, error } = await supabase.from('subjects').update(updates).eq('id', id).select();
      if (!error && data && data.length > 0) {
        return { data: data[0], source: 'supabase' };
      }
    } catch (e) {
      console.warn('Supabase updateSubject note:', e);
    }
    return { data: updated.find(s => s.id === id), source: 'local' };
  },

  async deleteSubject(id) {
    const current = getLocal('subjects', MASTER_SUBJECTS);
    const updated = current.filter(s => s.id !== id);
    setLocal('subjects', updated);

    try {
      await supabase.from('subjects').delete().eq('id', id);
    } catch (e) {
      console.warn('Supabase deleteSubject note:', e);
    }
    return true;
  }
};
