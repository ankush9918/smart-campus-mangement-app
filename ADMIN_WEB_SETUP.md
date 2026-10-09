# VVIT Smart Campus — Admin Web Dashboard & Architecture Guide

A dedicated, separate **Admin Web Dashboard** built for the **Vidya Vikas Institute of Technology (VVIT)** College Management System, connected with **Supabase PostgreSQL Cloud Backend**.

---

## 🌟 Architecture Overview

```text
                             ┌─────────────────────────────────┐
                             │       Admin Web Dashboard       │
                             │     (http://localhost:5173)     │
                             └────────────────┬────────────────┘
                                              │ Official @supabase/supabase-js
                                              ▼
                             ┌─────────────────────────────────┐
                             │    Supabase Cloud PostgreSQL    │
                             │  (Project: easrebumarwkloibtemf)│
                             └────────┬───────────────┬────────┘
                                      │ REST API      │ REST API
                                      ▼               ▼
                       ┌──────────────────────┐ ┌──────────────────────┐
                       │ Existing Android App │ │ Existing Android App │
                       │    (Student Portal)  │ │    (Faculty Portal)  │
                       └──────────────────────┘ └──────────────────────┘
```

---

## 🚀 1. Accessing the Admin Web Dashboard

The Admin Web Dashboard is currently running live on:

👉 **URL:** [http://localhost:5173](http://localhost:5173)

To start the dev server manually anytime:
```powershell
cd "c:\Users\Ankush\Life\smartcollege (2)\admin-web"
npm run dev
```

To build production bundle:
```powershell
cd "c:\Users\Ankush\Life\smartcollege (2)\admin-web"
npm run build
```

---

## 🛡️ 2. Existing Student & Professor Mobile Applications Protected

As strictly required:
* **Zero UI changes** were made to the Student or Professor/Faculty applications.
* Themes, colors, fonts, layouts, navigation, screens, and components remain **100% unchanged**.
* Only the **minimum backend/Supabase connection** was integrated via [SupabaseApiClient.kt](file:///c:/Users/Ankush/Life/smartcollege%20%282%29/smartcollege/app/src/main/java/com/vvit/smartcollege/data/remote/SupabaseApiClient.kt) into `StudentRepository` and `TeacherRepository` so that:
  - Updated timetables are received.
  - Published circulars are received.
  - Attendance logs are received and synced.

---

## 🗄️ 3. Supabase Backend Configuration

* **Project ID:** `easrebumarwkloibtemf`
* **Publishable Key:** `sb_publishable_CsQmrZ6ejrBeu6sQlmQX7Q_UoEoomC6`
* **Project URL:** `https://easrebumarwkloibtemf.supabase.co`

Stored securely in environment files:
- [admin-web/.env](file:///c:/Users/Ankush/Life/smartcollege%20%282%29/admin-web/.env)
- [admin-web/.env.example](file:///c:/Users/Ankush/Life/smartcollege%20%282%29/admin-web/.env.example)

### Running Database Schema in Supabase

The complete SQL schema is available in:
- [supabase_schema.sql](file:///c:/Users/Ankush/Life/smartcollege%20%282%29/supabase_schema.sql)
- In-App: Click the **Supabase DB** pill in the top bar / sidebar of the Admin Web Dashboard to open the interactive setup modal with 1-click **Copy SQL Schema**.

To initialize the 7 tables in your Supabase project:
1. Open the [Supabase SQL Editor](https://supabase.com/dashboard/project/easrebumarwkloibtemf/sql).
2. Copy and paste the contents of `supabase_schema.sql`.
3. Click **Run**.
4. The following 7 tables will be created with initial seed data:
   * `public.students`
   * `public.faculty`
   * `public.timetable_student`
   * `public.timetable_faculty`
   * `public.attendance_student`
   * `public.attendance_faculty`
   * `public.circulars`

*Note: The Admin Web Dashboard features a dual-layer data architecture — if tables are awaiting execution in Supabase, the web dashboard automatically uses a persistent local cache so data is never lost across browser refresh or restart, and syncs seamlessly once the schema is executed.*

---

## 📋 4. Features Implemented in Admin Web Dashboard

### 1. Dashboard
- Institutional KPIs: Students count, Faculty count, Today's Attendance %, Active Bulletins.
- Quick Management Action shortcuts (Enroll Student, Register Faculty, Timetable Desk, Publish Circular).
- Live Supabase connection status pill and latency indicator.
- Recent Circulars preview and Today's Schedule timeline.

### 2. Students Module
- **Add Student Modal** with complete validation:
  - Photo URL (with quick random generator)
  - Full Name
  - Enrollment / Roll Number (e.g. `VVIT/CS/2021/045`)
  - Department / Branch dropdown
  - Year / Semester and Section
  - College Email & Phone Number
  - Attendance % & CGPA
  - Guardian Name & Contact Details
  - Blood Group & Address
- Search by name, roll number, or email.
- Department & Semester filters.
- Edit Student and Delete Student with confirmation dialogs.
- Synced to Supabase `students` table.

### 3. Faculty / Professors Module
- **Add Faculty Modal** with complete validation:
  - Photo URL
  - Full Name
  - Employee ID (e.g. `VVIT/CS/T-104`)
  - Designation (Professor & HOD, Professor, Associate Professor, Assistant Professor, Lecturer)
  - Graduation / Education Details (e.g. `Ph.D. in Computer Science`)
  - Department
  - College Email & Phone Number
  - LinkedIn Profile
  - Cabin Room & Experience Years
  - Subjects Taught
- Search by name, designation, or employee ID.
- Department filter.
- Edit and Delete with confirmation dialogs.
- Synced to Supabase `faculty` table.

### 4. Attendance Module
Two dedicated sections:
- **Student Attendance**:
  - Displays attendance records marked by teachers.
  - Columns: Date, Roll Number, Student Name, Subject, Status (Present / Absent / Late chip), Marked By (Faculty Name), Timestamp.
  - Filter by date, status, and search query.
  - Quick "Record Student Attendance" action dialog.
- **Faculty / Professor Attendance**:
  - Displays faculty self check-in logs.
  - Columns: Date, Employee ID, Faculty Name, Department, Check-in Time, Status (Present / On Leave / Half Day), Timestamp.
  - Filter by date, status, and search query.
  - Quick "Log Faculty Attendance" action dialog.
- Synced to Supabase `attendance_student` and `attendance_faculty`.

### 5. Timetable Module
Two dedicated sections:
- **Student Timetable**:
  - Day selector (Monday - Saturday) & Section filter.
  - Cards showing Time, Duration, Subject, Code, Professor, Room, Batch, Lab/Break tags.
  - Prominent **Update** button on each card for inline editing.
  - **Add Student Class Slot** with validation.
  - Synced to Supabase `timetable_student` and reaches existing Student Dashboard.
- **Professor Timetable**:
  - Day selector & Professor filter.
  - Cards showing Time, Subject, Code, Professor Name, Room, Batch, Lecture Type.
  - Prominent **Update** button on each card for inline editing.
  - **Add Professor Lecture Slot** with validation.
  - Synced to Supabase `timetable_faculty` and reaches existing Professor Dashboard.

### 6. Circulars Module
- Feed of published campus bulletins.
- **Publish Circular Modal**:
  - Circular Title
  - Category (Academics, Examination, Event, General)
  - Target Audience (All Campus, Students Only, Faculty Only)
  - Date
  - High Priority / Urgent toggle
  - Notice Content / Description
  - Published By / Issuer
- Prominent Edit and Delete buttons.
- Synced to Supabase `circulars` and reaches both existing Student & Professor Dashboards.

### 7. Course Section Removed
- No Course menu item, page, card, route, button, mock data, or component exists anywhere in the Admin Web Dashboard.
