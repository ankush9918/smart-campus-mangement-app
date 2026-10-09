# Smart College Management System 🎓

A comprehensive, full-stack college management solution featuring a modern **Android App** for students and professors, an **Admin Web Portal** for college administrators, and a cloud-synced **Supabase Backend**.

---

## 📁 Repository Structure

```
smartcollege-root/
├── smartcollege/           # Android Mobile App (Jetpack Compose, Kotlin)
│   ├── app/                # Main application module
│   │   ├── src/main/java/com/vvit/smartcollege/
│   │   │   ├── data/       # Room DB, Supabase Client, Repositories, Models
│   │   │   ├── ui/         # Jetpack Compose UI (Student, Teacher, Auth, Theme)
│   │   │   └── util/       # Helpers, Session Manager
│   └── build.gradle.kts
│
├── admin-web/              # Web Admin Dashboard (React, Vite, Tailwind CSS)
│   ├── src/
│   │   ├── components/     # UI Components, Modals, Forms
│   │   ├── pages/          # Faculty, Students, Subjects, Attendance Management
│   │   └── lib/            # Supabase API client
│   └── package.json
│
├── supabase_schema.sql     # Database schema, tables, policies, triggers
├── fix_auth_and_create_users.sql # User auth provisioning script
└── ADMIN_WEB_SETUP.md      # Admin web portal setup and deployment guide
```

---

## 🚀 Key Features

### 1. Student Android Dashboard
* **Dynamic Timetable & Attendance:** Real-time subject-wise attendance tracking with authorized faculty mappings.
* **Academic Hub:** View and download assignments and study materials directly from Supabase Cloud Storage.
* **Modern UI:** Built with Jetpack Compose and clean Inter typography.

### 2. Professor Android Dashboard
* **Subject-wise Attendance:** Attendance desk restricted to officially assigned subjects.
* **Assignments Hub:** Publish assignments with deadlines and attachments stored in Supabase cloud storage.
* **Study Material Hub:** Upload notes and reference materials (PDF, DOC, PPT).
* **Student Analytics:** Real-time roster and low-attendance alerts (< 75%).

### 3. Admin Web Dashboard
* **Faculty Management:** Register teachers and assign specific subjects from the master curriculum.
* **Student Management:** Enroll students, assign roll numbers, branches, and semesters.
* **System-wide Analytics:** High-level attendance reports and administrative controls.

---

## 🛠 Tech Stack

* **Mobile App:** Kotlin, Jetpack Compose, Material 3, Coroutines, StateFlow, Room DB, Retrofit / OkHttp / HttpURLConnection.
* **Admin Web:** React 18, Vite, Tailwind CSS, Lucide Icons.
* **Backend & Cloud:** Supabase (PostgreSQL, Auth, Storage Buckets, Row Level Security).

---

## ⚙️ Quick Start

### Android App
1. Open the `smartcollege/` folder in **Android Studio**.
2. Sync Gradle dependencies.
3. Run on an Android emulator or physical device (Min SDK 24, Target SDK 34).

### Admin Web Portal
1. Navigate to the `admin-web/` directory:
   ```bash
   cd admin-web
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the local development server:
   ```bash
   npm run dev
   ```

---

## 📄 License
This project is licensed under the MIT License.
