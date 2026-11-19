package com.vvit.smartcollege.data.remote

import android.util.Log
import com.vvit.smartcollege.data.model.NoticeItem
import com.vvit.smartcollege.data.model.StudentProfile
import com.vvit.smartcollege.data.model.StudentRosterItem
import com.vvit.smartcollege.data.model.StudentAttendanceStatus
import com.vvit.smartcollege.data.model.StudentTimetableEntry
import com.vvit.smartcollege.data.model.SubjectAttendance
import com.vvit.smartcollege.data.model.TeacherProfile
import com.vvit.smartcollege.data.model.TeacherScheduleItem
import com.vvit.smartcollege.data.model.StudentAssignment
import com.vvit.smartcollege.data.model.AssignmentStatus
import com.vvit.smartcollege.data.model.StudentStudyMaterial
import com.vvit.smartcollege.data.model.FacultyInfo
import com.vvit.smartcollege.data.model.MasterSubject
import com.vvit.smartcollege.data.model.MasterSubjectRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import com.vvit.smartcollege.data.local.SessionManager
import java.net.HttpURLConnection
import java.net.URL

object SupabaseApiClient {
    private const val TAG = "SupabaseApiClient"

    const val PROJECT_ID = "easrebumarwkloibtemf"
    const val BASE_URL = "https://$PROJECT_ID.supabase.co/rest/v1"
    const val AUTH_URL = "https://$PROJECT_ID.supabase.co/auth/v1"
    const val PUBLISHABLE_KEY = "sb_publishable_CsQmrZ6ejrBeu6sQlmQX7Q_UoEoomC6"

    @Volatile
    var activeAccessToken: String? = null

    private fun openConnection(endpoint: String, method: String = "GET", timeoutMs: Int = 4000): HttpURLConnection {
        val fullUrl = "$BASE_URL$endpoint"
        val url = URL(fullUrl)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = method
        conn.connectTimeout = timeoutMs
        conn.readTimeout = timeoutMs
        conn.setRequestProperty("apikey", PUBLISHABLE_KEY)
        conn.setRequestProperty("Authorization", "Bearer $PUBLISHABLE_KEY")
        conn.setRequestProperty("Accept", "application/json")

        if (method in listOf("POST", "PUT", "PATCH")) {
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Prefer", "return=minimal")
            conn.doOutput = true
        }
        return conn
    }

    private fun executeRequest(endpoint: String, method: String = "GET", bodyJson: String? = null): String? {
        var conn: HttpURLConnection? = null
        return try {
            conn = openConnection(endpoint, method)
            if (bodyJson != null && method in listOf("POST", "PUT", "PATCH")) {
                OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                    writer.write(bodyJson)
                    writer.flush()
                }
            }

            val code = conn.responseCode
            if (code in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                val response = reader.readText()
                reader.close()
                response
            } else {
                Log.d(TAG, "Request to $endpoint returned HTTP $code")
                null
            }
        } catch (e: Exception) {
            Log.d(TAG, "Supabase connection info: ${e.message}")
            null
        } finally {
            conn?.disconnect()
        }
    }

    /**
     * Fetch published circulars / notices from Supabase, filtered by target audience
     */
    suspend fun fetchCirculars(targetAudience: String? = null): List<NoticeItem>? = withContext(Dispatchers.IO) {
        val response = executeRequest("/circulars?select=*&is_published=eq.true&order=date.desc") ?: return@withContext null
        try {
            val arr = JSONArray(response)
            if (arr.length() == 0) return@withContext emptyList()

            val list = mutableListOf<NoticeItem>()
            val aud = targetAudience?.trim()?.uppercase()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val category = obj.optString("category", "ACADEMICS")
                if (category.equals("STUDY_MATERIAL", ignoreCase = true)) {
                    continue
                }
                val target = obj.optString("target", "ALL").trim().uppercase()

                // Filter by target audience if specified (ALL is always delivered to everyone)
                if (aud != null && target != "ALL") {
                    val isMatch = when (aud) {
                        "STUDENTS", "STUDENT" -> target == "STUDENTS" || target == "STUDENT"
                        "FACULTY", "PROFESSOR", "PROFESSORS", "TEACHER" -> target == "FACULTY" || target == "PROFESSORS" || target == "PROFESSOR"
                        else -> true
                    }
                    if (!isMatch) continue
                }

                val rawDesc = obj.optString("description", "")
                var cleanDesc = rawDesc
                if (rawDesc.trim().startsWith("{")) {
                    try {
                        val meta = JSONObject(rawDesc)
                        if (meta.has("subject")) {
                            val sub = meta.optString("subject", "").trim()
                            if (sub.isNotBlank()) cleanDesc = sub
                        } else if (meta.has("instructions")) {
                            val inst = meta.optString("instructions", "").trim()
                            if (inst.isNotBlank()) cleanDesc = inst
                        }
                    } catch (_: Exception) {}
                }

                list.add(
                    NoticeItem(
                        id = obj.optString("id", "cir_$i"),
                        title = obj.optString("title", "Notice"),
                        category = obj.optString("category", "ACADEMICS"),
                        timeAgo = obj.optString("date", "Today"),
                        description = cleanDesc,
                        isUrgent = obj.optBoolean("is_urgent", false),
                        date = obj.optString("date", "2026-10-03")
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing circulars", e)
            null
        }
    }

    /**
     * Fetch student timetable from Supabase
     */
    suspend fun fetchStudentTimetable(): List<StudentTimetableEntry>? = withContext(Dispatchers.IO) {
        val response = executeRequest("/timetable_student?select=*&order=id.asc") ?: return@withContext null
        try {
            val arr = JSONArray(response)
            if (arr.length() == 0) return@withContext null

            val list = mutableListOf<StudentTimetableEntry>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val sTime = obj.optString("start_time", "10:00 AM")
                val eTime = obj.optString("end_time", "10:50 AM")
                val timeFormatted = "$sTime - $eTime"
                val rawDay = obj.optString("day", "MONDAY").trim().uppercase()
                val normalizedDay = when {
                    rawDay.startsWith("MON") -> "Mon"
                    rawDay.startsWith("TUE") -> "Tue"
                    rawDay.startsWith("WED") -> "Wed"
                    rawDay.startsWith("THU") -> "Thu"
                    rawDay.startsWith("FRI") -> "Fri"
                    rawDay.startsWith("SAT") -> "Sat"
                    else -> "Mon"
                }

                list.add(
                    StudentTimetableEntry(
                        id = obj.optString("id", "stt_$i"),
                        dayOfWeek = normalizedDay,
                        time = timeFormatted,
                        duration = obj.optString("duration", "50 Mins"),
                        subjectName = obj.optString("subject_name", "Subject"),
                        facultyName = obj.optString("teacher_name", "Professor"),
                        room = obj.optString("room", "LH-1"),
                        isOngoing = obj.optBoolean("is_ongoing", false),
                        isBreak = obj.optBoolean("is_break", false)
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing student timetable", e)
            null
        }
    }

    /**
     * Fetch professor schedule from Supabase
     */
    suspend fun fetchTeacherSchedule(): List<TeacherScheduleItem>? = withContext(Dispatchers.IO) {
        val response = executeRequest("/timetable_faculty?select=*&order=start_time.asc") ?: return@withContext null
        try {
            val arr = JSONArray(response)
            if (arr.length() == 0) return@withContext null

            val list = mutableListOf<TeacherScheduleItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val sTime = obj.optString("start_time", "09:00 AM")
                val eTime = obj.optString("end_time", "10:00 AM")
                val timeFormatted = "$sTime - $eTime"

                list.add(
                    TeacherScheduleItem(
                        id = obj.optString("id", "ftt_$i"),
                        time = timeFormatted,
                        subjectName = obj.optString("subject_name", "Subject"),
                        room = obj.optString("room", "Room 402"),
                        section = obj.optString("batch_or_section", "CSE-5B"),
                        lectureType = obj.optString("lecture_type", "Lecture"),
                        dayOfWeek = obj.optString("day", "MONDAY").uppercase()
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing professor schedule", e)
            null
        }
    }

    /**
     * Post student attendance record to Supabase.
     * Allows multiple lectures/sessions for the same subject on the same day.
     * Each submission records a new lecture entry and recalculates overall student attendance percentage.
     */
    suspend fun postStudentAttendance(
        studentId: String,
        studentName: String,
        rollNumber: String,
        subject: String,
        date: String,
        status: String,
        teacherId: String = "",
        markedBy: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val cleanRoll = rollNumber.trim()
            val masterSub = MasterSubjectRegistry.find(subject)
            val canonicalSub = masterSub?.displayName ?: MasterSubjectRegistry.normalize(subject)
            val cleanDate = date.trim()
            val cleanId = studentId.trim()
            val isoTimestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.format(java.util.Date())

            // Insert new attendance session record (allowing multiple classes/lectures per day)
            val postJson = JSONObject().apply {
                put("id", "att_s_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(6)}")
                put("student_id", cleanId)
                put("student_name", studentName)
                put("roll_number", cleanRoll)
                put("subject", canonicalSub)
                put("date", cleanDate)
                put("status", status)
                put("marked_by", markedBy)
                put("timestamp", isoTimestamp)
            }
            val postRes = executeRequest("/attendance_student", "POST", postJson.toString())
            val isSaved = postRes != null

            // Recalculate and sync student's overall attendance percentage in Supabase students table
            if (isSaved) {
                try {
                    val encodedRoll = java.net.URLEncoder.encode(cleanRoll, "UTF-8")
                    var allRecordsRes = executeRequest("/attendance_student?roll_number=eq.$encodedRoll&select=status")
                    if ((allRecordsRes.isNullOrBlank() || allRecordsRes.trim() == "[]") && cleanId.isNotBlank()) {
                        val encodedId = java.net.URLEncoder.encode(cleanId, "UTF-8")
                        allRecordsRes = executeRequest("/attendance_student?student_id=eq.$encodedId&select=status")
                    }
                    if (!allRecordsRes.isNullOrBlank() && allRecordsRes.trim() != "[]") {
                        val allArr = JSONArray(allRecordsRes)
                        val total = allArr.length()
                        var presentCount = 0
                        for (i in 0 until total) {
                            if (allArr.getJSONObject(i).optString("status").equals("PRESENT", ignoreCase = true)) {
                                presentCount++
                            }
                        }
                        val newPercentage = if (total > 0) (presentCount * 100) / total else 100
                        val updateStudentJson = JSONObject().apply {
                            put("attendance_rate", newPercentage)
                        }
                        if (cleanRoll.isNotBlank()) {
                            executeRequest("/students?roll_number=eq.$encodedRoll", "PATCH", updateStudentJson.toString())
                        }
                        if (cleanId.isNotBlank()) {
                            val encodedId = java.net.URLEncoder.encode(cleanId, "UTF-8")
                            executeRequest("/students?id=eq.$encodedId", "PATCH", updateStudentJson.toString())
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Non-fatal error updating student overall percentage", e)
                }
            }

            isSaved
        } catch (e: Exception) {
            Log.e(TAG, "Error posting student attendance to Supabase", e)
            false
        }
    }

    /**
     * Post faculty attendance record to Supabase
     */
    suspend fun postFacultyAttendance(
        facultyId: String,
        facultyName: String,
        employeeId: String,
        department: String,
        date: String,
        status: String,
        checkInTime: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("id", "att_f_${System.currentTimeMillis()}")
                put("faculty_id", facultyId)
                put("faculty_name", facultyName)
                put("employee_id", employeeId)
                put("department", department)
                put("date", date)
                put("status", status)
                put("check_in_time", checkInTime)
            }
            val res = executeRequest("/attendance_faculty", "POST", json.toString())
            res != null
        } catch (e: Exception) {
            Log.e(TAG, "Error posting faculty attendance to Supabase", e)
            false
        }
    }

    /**
     * Authenticate user with Supabase Auth using email and password
     */
    suspend fun signInWithPassword(email: String, pass: String): SupabaseAuthResult = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL("$AUTH_URL/token?grant_type=password")
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 6000
            conn.readTimeout = 6000
            conn.setRequestProperty("apikey", PUBLISHABLE_KEY)
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            conn.doOutput = true

            val requestBody = JSONObject().apply {
                put("email", email)
                put("password", pass)
            }

            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }

            val code = conn.responseCode
            if (code in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                val responseText = reader.readText()
                reader.close()

                val json = JSONObject(responseText)
                val accessToken = if (json.has("access_token")) json.optString("access_token") else null
                if (!accessToken.isNullOrBlank()) {
                    activeAccessToken = accessToken
                }
                val userObj = json.optJSONObject("user")
                val userId = userObj?.optString("id")
                val userEmail = userObj?.optString("email", email)
                val metaObj = userObj?.optJSONObject("user_metadata")
                var role = metaObj?.optString("role")
                var name = metaObj?.optString("name")
                var identifier: String? = metaObj?.optString("roll_number") ?: metaObj?.optString("employee_id")

                // If metadata is missing role or profile details, query students or faculty tables
                if (role.isNullOrBlank() || identifier.isNullOrBlank()) {
                    val studentProfile = fetchStudentByEmailOrAuthId(userEmail, userId)
                    if (studentProfile != null) {
                        role = "student"
                        if (name.isNullOrBlank()) name = studentProfile.optString("name")
                        if (identifier.isNullOrBlank()) identifier = studentProfile.optString("roll_number")
                    } else {
                        val facultyProfile = fetchFacultyByEmailOrAuthId(userEmail, userId)
                        if (facultyProfile != null) {
                            role = "professor"
                            if (name.isNullOrBlank()) name = facultyProfile.optString("name")
                            if (identifier.isNullOrBlank()) identifier = facultyProfile.optString("employee_id")
                        }
                    }
                }

                SupabaseAuthResult(
                    isSuccess = true,
                    userId = userId,
                    email = userEmail,
                    role = role,
                    name = name,
                    identifier = identifier,
                    accessToken = accessToken
                )
            } else {
                val stream = conn.errorStream ?: conn.inputStream
                val errorResponse = if (stream != null) {
                    BufferedReader(InputStreamReader(stream, "UTF-8")).use { it.readText() }
                } else ""

                var errorMsg = "Invalid email or password. Please verify your credentials."
                try {
                    if (errorResponse.isNotBlank()) {
                        val errJson = JSONObject(errorResponse)
                        val parsed = errJson.optString("error_description",
                            errJson.optString("msg",
                                errJson.optString("message", "")))
                        if (parsed.isNotBlank()) {
                            errorMsg = parsed
                        }
                    }
                } catch (e: Exception) {
                    // Ignore parse errors
                }

                SupabaseAuthResult(
                    isSuccess = false,
                    errorMessage = errorMsg
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "signInWithPassword exception: ${e.message}")
            SupabaseAuthResult(
                isSuccess = false,
                errorMessage = e.message ?: "Network error connecting to Supabase Auth"
            )
        } finally {
            conn?.disconnect()
        }
    }

    private fun fetchStudentByEmailOrAuthId(email: String?, authId: String?): JSONObject? {
        try {
            val cleanEmail = email?.trim()
            if (!cleanEmail.isNullOrBlank()) {
                val encodedEmail = java.net.URLEncoder.encode(cleanEmail, "UTF-8")
                val res = executeRequest("/students?email=ilike.$encodedEmail&select=*")
                if (res != null) {
                    val arr = JSONArray(res)
                    if (arr.length() > 0) return arr.getJSONObject(0)
                }
            }
            if (!authId.isNullOrBlank()) {
                val res = executeRequest("/students?auth_user_id=eq.$authId&select=*")
                if (res != null) {
                    val arr = JSONArray(res)
                    if (arr.length() > 0) return arr.getJSONObject(0)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Student fetch lookup failed: ${e.message}")
        }
        return null
    }

    private fun fetchFacultyByEmailOrAuthId(email: String?, authId: String?): JSONObject? {
        try {
            val cleanEmail = email?.trim()
            if (!cleanEmail.isNullOrBlank()) {
                val encodedEmail = java.net.URLEncoder.encode(cleanEmail, "UTF-8")
                val res = executeRequest("/faculty?email=ilike.$encodedEmail&select=*")
                if (res != null) {
                    val arr = JSONArray(res)
                    if (arr.length() > 0) return arr.getJSONObject(0)
                }
            }
            if (!authId.isNullOrBlank()) {
                val res = executeRequest("/faculty?auth_user_id=eq.$authId&select=*")
                if (res != null) {
                    val arr = JSONArray(res)
                    if (arr.length() > 0) return arr.getJSONObject(0)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Faculty fetch lookup failed: ${e.message}")
        }
        return null
    }

    /**
     * Fetch full StudentProfile from Supabase
     */
    suspend fun fetchStudentProfile(email: String?, authId: String?): StudentProfile? = withContext(Dispatchers.IO) {
        val obj = fetchStudentByEmailOrAuthId(email, authId) ?: return@withContext null
        try {
            val attRate = obj.optInt("attendance_rate", 85)
            val cgpaVal = obj.optDouble("cgpa", 8.0)
            val semInt = obj.optInt("semester", 5)
            val secStr = obj.optString("section", "B")
            val photoUrlVal = if (obj.isNull("photo_url")) null else {
                val raw = obj.optString("photo_url", "").trim()
                if (raw.isNotBlank() && !raw.equals("null", ignoreCase = true)) raw else null
            }

            StudentProfile(
                id = obj.optString("id", "std_${System.currentTimeMillis()}"),
                name = obj.optString("name", "Student User"),
                rollNo = obj.optString("roll_number", "VVIT/CS/2021/045"),
                email = obj.optString("email", email ?: ""),
                phone = obj.optString("phone", "+91 98765 00000"),
                dateOfBirth = obj.optString("date_of_birth", "14th August 2003"),
                bloodGroup = obj.optString("blood_group", "O+"),
                department = "Information Technology",
                currentSemester = "Semester $semInt (Year 3)",
                section = "Section $secStr",
                admissionYear = obj.optString("admission_year").ifBlank { "2021" },
                guardianName = obj.optString("guardian_name", "Guardian"),
                guardianRelationship = obj.optString("guardian_relationship", "Father"),
                guardianContact = obj.optString("guardian_phone", "+91 98111 00000"),
                cgpa = cgpaVal,
                attendancePercentage = attRate,
                photoUrl = photoUrlVal
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error building StudentProfile", e)
            null
        }
    }

    /**
     * Fetch SubjectAttendance list and calculated overall attendance rate for a student from actual attendance records.
     * Strictly displays ONLY curriculum Theory subjects (no Labs, Workshops, or Projects) matching the official timetable.
     * Formula: (Present Classes / Total Classes) * 100
     */
    suspend fun fetchStudentAttendance(
        studentId: String?,
        rollNumber: String?,
        overallPercentage: Int
    ): AttendanceCalculationResult = withContext(Dispatchers.IO) {
        val rate = overallPercentage.coerceIn(0, 100)

        // 6 Official Master Theory Subjects from MasterSubjectRegistry
        val officialTheorySubjects = MasterSubjectRegistry.getAll().map { 
            val totalHours = when (it.code) {
                "BCS-301" -> 42
                "BCS-302" -> 40
                "BCS-303" -> 38
                "BOE-310" -> 36
                "BAS-301" -> 32
                "BCC-302" -> 35
                else -> 40
            }
            Triple(it.name, it.code, totalHours)
        }

        try {
            var recordsJson: String? = null
            if (!rollNumber.isNullOrBlank()) {
                val encodedRoll = java.net.URLEncoder.encode(rollNumber.trim(), "UTF-8")
                recordsJson = executeRequest("/attendance_student?roll_number=eq.$encodedRoll&select=*")
            }
            if ((recordsJson.isNullOrBlank() || recordsJson.trim() == "[]") && !studentId.isNullOrBlank()) {
                val encodedId = java.net.URLEncoder.encode(studentId.trim(), "UTF-8")
                recordsJson = executeRequest("/attendance_student?student_id=eq.$encodedId&select=*")
            }

            val subjectMap = mutableMapOf<String, Pair<Int, Int>>()
            var totalPresentRecords = 0
            var totalConductedRecords = 0

            if (!recordsJson.isNullOrBlank()) {
                val arr = JSONArray(recordsJson)
                for (i in 0 until arr.length()) {
                    val row = arr.getJSONObject(i)
                    var sub = row.optString("subject", "").trim()

                    // Filter out any labs, workshops, projects, practicals, non-academics
                    if (sub.contains("Lab", ignoreCase = true) ||
                        sub.contains("Workshop", ignoreCase = true) ||
                        sub.contains("Project", ignoreCase = true) ||
                        sub.contains("Practical", ignoreCase = true) ||
                        sub.contains("Sports", ignoreCase = true) ||
                        sub.contains("Library", ignoreCase = true)
                    ) {
                        continue
                    }

                    // Normalize to official theory subjects using MasterSubjectRegistry
                    val matchedMaster = MasterSubjectRegistry.find(sub)
                    if (matchedMaster != null) {
                        sub = matchedMaster.name
                    }

                    if (sub.isNotBlank()) {
                        val isPresent = row.optString("status").equals("PRESENT", ignoreCase = true)
                        val curr = subjectMap.getOrDefault(sub, Pair(0, 0))
                        subjectMap[sub] = Pair(
                            curr.first + (if (isPresent) 1 else 0),
                            curr.second + 1
                        )
                        if (isPresent) totalPresentRecords++
                        totalConductedRecords++
                    }
                }
            }

            val calculatedRate = if (totalConductedRecords > 0) {
                ((totalPresentRecords * 100) / totalConductedRecords).coerceIn(0, 100)
            } else {
                if (overallPercentage in 1..100) overallPercentage else 100
            }

            // Build list for all 6 official theory subjects
            val result = officialTheorySubjects.mapIndexed { index, (name, code, _) ->
                val master = MasterSubjectRegistry.find(code) ?: MasterSubjectRegistry.find(name)
                val stats = subjectMap[name]
                    ?: (if (master != null) subjectMap[master.name] else null)
                    ?: subjectMap.entries.firstOrNull { 
                        it.key.contains(name, ignoreCase = true) || name.contains(it.key, ignoreCase = true) ||
                        it.key.contains(code, ignoreCase = true)
                    }?.value

                if (stats != null && stats.second > 0) {
                    val pct = (stats.first * 100) / stats.second
                    SubjectAttendance(
                        id = "att_sub_${index + 1}",
                        subjectName = master?.displayName ?: "$name ($code)",
                        subjectCode = code,
                        attendedClasses = stats.first,
                        totalClasses = stats.second,
                        percentage = pct,
                        isAlert = pct < 75
                    )
                } else {
                    SubjectAttendance(
                        id = "att_sub_${index + 1}",
                        subjectName = master?.displayName ?: "$name ($code)",
                        subjectCode = code,
                        attendedClasses = 0,
                        totalClasses = 0,
                        percentage = 100,
                        isAlert = false
                    )
                }
            }
            return@withContext AttendanceCalculationResult(
                subjects = result,
                calculatedPercentage = calculatedRate,
                totalClasses = totalConductedRecords,
                presentClasses = totalPresentRecords
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating attendance from records", e)
        }

        // Fallback default: all 6 official theory subjects
        val fallbackList = officialTheorySubjects.mapIndexed { index, (name, code, _) ->
            val master = MasterSubjectRegistry.find(code) ?: MasterSubjectRegistry.find(name)
            SubjectAttendance(
                id = "att_sub_${index + 1}",
                subjectName = master?.displayName ?: "$name ($code)",
                subjectCode = code,
                attendedClasses = 0,
                totalClasses = 0,
                percentage = 100,
                isAlert = false
            )
        }
        return@withContext AttendanceCalculationResult(
            subjects = fallbackList,
            calculatedPercentage = if (rate in 1..100) rate else 100,
            totalClasses = 0,
            presentClasses = 0
        )
    }

    /**
     * Backward-compatible helper to get SubjectAttendance list directly
     */
    suspend fun fetchStudentAttendanceList(
        studentId: String?,
        rollNumber: String?,
        overallPercentage: Int
    ): List<SubjectAttendance> = fetchStudentAttendance(studentId, rollNumber, overallPercentage).subjects

    /**
     * Fetch assignments from Supabase, filtered by department/section
     */
    suspend fun fetchAssignments(
        department: String? = null,
        section: String? = null
    ): List<StudentAssignment>? = withContext(Dispatchers.IO) {
        val list = mutableListOf<StudentAssignment>()

        // 1. Try public.assignments table first
        try {
            val response = executeRequest("/assignments?select=*&order=created_at.desc")
            if (!response.isNullOrBlank() && response.startsWith("[")) {
                val arr = JSONArray(response)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val dept = obj.optString("department", "").trim()
                    if (!department.isNullOrBlank() && dept.isNotBlank() &&
                        !dept.equals("ALL", ignoreCase = true) &&
                        !dept.equals("General", ignoreCase = true) &&
                        !dept.equals(department.trim(), ignoreCase = true)
                    ) {
                        continue
                    }

                    list.add(
                        StudentAssignment(
                            id = obj.optString("id", "asg_${System.currentTimeMillis()}_$i"),
                            subjectName = obj.optString("subject", "Computer Science"),
                            title = obj.optString("title", "Assignment"),
                            instructions = obj.optString("instructions", obj.optString("description", "")),
                            dueDate = obj.optString("due_date", "Upcoming"),
                            status = AssignmentStatus.PENDING,
                            maxMarks = obj.optInt("max_marks", 100),
                            professorName = obj.optString("created_by", ""),
                            fileName = if (obj.has("file_name") && !obj.isNull("file_name")) obj.optString("file_name", "").ifBlank { null } else null,
                            fileUri = if (obj.has("file_url") && !obj.isNull("file_url")) obj.optString("file_url", "").ifBlank { null } else null
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Note: remote assignments query fallback: ${e.message}")
        }

        // 2. Also check circulars table with category=ASSIGNMENT (unified cross-table sync)
        try {
            val circResponse = executeRequest("/circulars?category=eq.ASSIGNMENT&order=created_at.desc")
            if (!circResponse.isNullOrBlank() && circResponse.startsWith("[")) {
                val arr = JSONArray(circResponse)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val id = obj.optString("id", "")
                    if (list.any { it.id == id }) continue // avoid duplicates

                    val descRaw = obj.optString("description", "")
                    var instructions = descRaw
                    var subject = "Computer Science"
                    var dueDate = obj.optString("date", "Upcoming")
                    var maxMarks = 100
                    var fileName: String? = null
                    var fileUri: String? = null
                    var prof = obj.optString("published_by", "")

                    try {
                        val meta = JSONObject(descRaw)
                        instructions = meta.optString("instructions", instructions)
                        subject = meta.optString("subject", subject)
                        dueDate = meta.optString("due_date", dueDate)
                        maxMarks = meta.optInt("max_marks", 100)
                        val fn = meta.optString("file_name", "")
                        if (fn.isNotBlank()) fileName = fn
                        val fu = meta.optString("file_url", "")
                        if (fu.isNotBlank()) fileUri = fu
                        val p = meta.optString("professor_name", "")
                        if (p.isNotBlank()) prof = p
                    } catch (_: Exception) {}

                    list.add(
                        StudentAssignment(
                            id = id,
                            subjectName = subject,
                            title = obj.optString("title", "Assignment"),
                            instructions = instructions,
                            dueDate = dueDate,
                            status = AssignmentStatus.PENDING,
                            maxMarks = maxMarks,
                            professorName = prof,
                            fileName = fileName,
                            fileUri = fileUri
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Note: circulars assignment query: ${e.message}")
        }

        if (list.isEmpty()) null else list
    }

    /**
     * Post assignment to Supabase
     */
    suspend fun postAssignment(
        assignment: StudentAssignment,
        department: String? = null,
        section: String? = null,
        createdBy: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        var success = false

        // 1. Try public.assignments table
        try {
            val postJson = JSONObject().apply {
                put("id", assignment.id)
                put("title", assignment.title)
                put("instructions", assignment.instructions)
                put("subject", assignment.subjectName)
                put("due_date", assignment.dueDate)
                put("max_marks", assignment.maxMarks)
                put("department", department ?: "Information Technology")
                put("section", section ?: "Section A")
                put("created_by", createdBy ?: assignment.professorName.ifBlank { "Faculty Member" })
                if (!assignment.fileName.isNullOrBlank()) {
                    put("file_name", assignment.fileName)
                }
                if (!assignment.fileUri.isNullOrBlank()) {
                    put("file_url", assignment.fileUri)
                }
            }
            val res = executeRequest("/assignments", "POST", postJson.toString())
            if (res != null) success = true
        } catch (e: Exception) {
            Log.d(TAG, "Note: remote post assignment error: ${e.message}")
        }

        // 2. Also mirror into public.circulars table for real-time cloud accessibility
        try {
            val descJson = JSONObject().apply {
                put("instructions", assignment.instructions)
                put("subject", assignment.subjectName)
                put("due_date", assignment.dueDate)
                put("max_marks", assignment.maxMarks)
                put("file_name", assignment.fileName ?: "")
                put("file_url", assignment.fileUri ?: "")
                put("professor_name", createdBy ?: assignment.professorName.ifBlank { "Faculty Member" })
            }
            val circJson = JSONObject().apply {
                put("id", assignment.id)
                put("title", assignment.title)
                put("description", descJson.toString())
                put("date", assignment.dueDate)
                put("category", "ASSIGNMENT")
                put("target", "STUDENT")
                put("is_urgent", false)
                put("is_published", true)
                put("published_by", createdBy ?: assignment.professorName.ifBlank { "Faculty Member" })
            }
            val circRes = executeRequest("/circulars", "POST", circJson.toString())
            if (circRes != null) success = true
        } catch (e: Exception) {
            Log.d(TAG, "Note: remote post circular assignment error: ${e.message}")
        }

        success
    }

    /**
     * Fetch study materials from Supabase, filtered by department/section
     */
    suspend fun fetchStudyMaterials(
        department: String? = null,
        section: String? = null
    ): List<StudentStudyMaterial>? = withContext(Dispatchers.IO) {
        val list = mutableListOf<StudentStudyMaterial>()

        // 1. Try public.study_materials table
        try {
            val response = executeRequest("/study_materials?select=*&order=id.desc")
            if (!response.isNullOrBlank() && response.startsWith("[")) {
                val arr = JSONArray(response)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val dept = obj.optString("department", "").trim()
                    if (!department.isNullOrBlank() && dept.isNotBlank() &&
                        !dept.equals("ALL", ignoreCase = true) &&
                        !dept.equals("General", ignoreCase = true) &&
                        !dept.equals(department.trim(), ignoreCase = true)
                    ) {
                        continue
                    }

                    val fn = if (obj.has("file_name") && !obj.isNull("file_name")) obj.optString("file_name", "").ifBlank { null } else null
                    val fu = if (obj.has("file_url") && !obj.isNull("file_url")) obj.optString("file_url", "").ifBlank { null } else null
                    val dUrl = obj.optString("download_url", fu ?: "")

                    list.add(
                        StudentStudyMaterial(
                            id = obj.optString("id", "mat_${System.currentTimeMillis()}_$i"),
                            title = obj.optString("title", "Study Material"),
                            subjectName = obj.optString("subject", "General"),
                            sharedBy = obj.optString("shared_by", "Faculty Member"),
                            sharedDate = obj.optString("shared_date", "Today"),
                            fileSize = obj.optString("file_size", "2.5 MB"),
                            fileType = obj.optString("file_type", "PDF"),
                            downloadUrl = dUrl,
                            fileName = fn,
                            fileUri = fu ?: dUrl.ifBlank { null }
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Note: remote study materials query: ${e.message}")
        }

        // 2. Also check circulars table with category=STUDY_MATERIAL (unified cross-table sync)
        try {
            val circResponse = executeRequest("/circulars?category=eq.STUDY_MATERIAL&order=created_at.desc")
            if (!circResponse.isNullOrBlank() && circResponse.startsWith("[")) {
                val arr = JSONArray(circResponse)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val id = obj.optString("id", "")
                    if (list.any { it.id == id }) continue

                    val descRaw = obj.optString("description", "")
                    var title = obj.optString("title", "Study Material")
                    var subject = "General"
                    var sharedBy = obj.optString("published_by", "Faculty Member")
                    var sharedDate = obj.optString("date", "Today")
                    var fileSize = "2.5 MB"
                    var fileType = "PDF"
                    var fileName: String? = null
                    var fileUri: String? = null

                    try {
                        val meta = JSONObject(descRaw)
                        title = meta.optString("title", title)
                        subject = meta.optString("subject", subject)
                        sharedBy = meta.optString("shared_by", sharedBy)
                        sharedDate = meta.optString("shared_date", sharedDate)
                        fileSize = meta.optString("file_size", fileSize)
                        fileType = meta.optString("file_type", fileType)
                        val fn = meta.optString("file_name", "")
                        if (fn.isNotBlank()) fileName = fn
                        val fu = meta.optString("file_url", "")
                        if (fu.isNotBlank()) fileUri = fu
                    } catch (_: Exception) {}

                    list.add(
                        StudentStudyMaterial(
                            id = id,
                            title = title,
                            subjectName = subject,
                            sharedBy = sharedBy,
                            sharedDate = sharedDate,
                            fileSize = fileSize,
                            fileType = fileType,
                            downloadUrl = fileUri ?: "",
                            fileName = fileName,
                            fileUri = fileUri
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Note: circulars study material query: ${e.message}")
        }

        if (list.isEmpty()) null else list
    }

    /**
     * Post study material to Supabase
     */
    suspend fun postStudyMaterial(
        material: StudentStudyMaterial,
        department: String? = null,
        section: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        var success = false

        // 1. Try public.study_materials table
        try {
            val postJson = JSONObject().apply {
                put("id", material.id)
                put("title", material.title)
                put("description", material.title)
                put("subject", material.subjectName)
                put("shared_by", material.sharedBy)
                put("shared_date", material.sharedDate)
                put("file_size", material.fileSize)
                put("file_type", material.fileType)
                put("download_url", material.downloadUrl)
                put("department", department ?: "Information Technology")
                put("section", section ?: "Section A")
                if (!material.fileName.isNullOrBlank()) {
                    put("file_name", material.fileName)
                }
                if (!material.fileUri.isNullOrBlank()) {
                    put("file_url", material.fileUri)
                }
            }
            val res = executeRequest("/study_materials", "POST", postJson.toString())
            if (res != null) success = true
        } catch (e: Exception) {
            Log.d(TAG, "Note: remote post study material error: ${e.message}")
        }

        // 2. Also mirror into circulars table with category=STUDY_MATERIAL
        try {
            val descJson = JSONObject().apply {
                put("title", material.title)
                put("subject", material.subjectName)
                put("shared_by", material.sharedBy)
                put("shared_date", material.sharedDate)
                put("file_size", material.fileSize)
                put("file_type", material.fileType)
                put("file_name", material.fileName ?: "")
                put("file_url", material.downloadUrl.ifBlank { material.fileUri ?: "" })
            }
            val circJson = JSONObject().apply {
                put("id", material.id)
                put("title", material.title)
                put("description", descJson.toString())
                put("date", material.sharedDate)
                put("category", "STUDY_MATERIAL")
                put("target", "STUDENT")
                put("is_urgent", false)
                put("is_published", true)
                put("published_by", material.sharedBy)
            }
            val circRes = executeRequest("/circulars", "POST", circJson.toString())
            if (circRes != null) success = true
        } catch (e: Exception) {
            Log.d(TAG, "Note: remote post circular study material error: ${e.message}")
        }

        success
    }

    /**
     * Upload a binary file (PDF, DOCX, PPTX, Image) to Supabase Storage bucket.
     * Returns the public HTTPS URL if successful, or null on failure.
     */
    suspend fun uploadFileToStorage(
        bucket: String,
        fileName: String,
        fileBytes: ByteArray,
        mimeType: String = "application/octet-stream"
    ): String? = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val cleanName = fileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val storageUrl = "https://$PROJECT_ID.supabase.co/storage/v1/object/$bucket/$cleanName"
            val url = URL(storageUrl)
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 20000
            conn.readTimeout = 20000
            conn.setRequestProperty("apikey", PUBLISHABLE_KEY)
            conn.setRequestProperty("Authorization", "Bearer $PUBLISHABLE_KEY")
            conn.setRequestProperty("Content-Type", mimeType.ifBlank { "application/octet-stream" })
            conn.setRequestProperty("x-upsert", "true")
            conn.doOutput = true

            conn.outputStream.use { os ->
                os.write(fileBytes)
                os.flush()
            }

            val code = conn.responseCode
            if (code in 200..299) {
                val publicUrl = "https://$PROJECT_ID.supabase.co/storage/v1/object/public/$bucket/$cleanName"
                Log.d(TAG, "File uploaded successfully to Supabase Storage: $publicUrl")
                publicUrl
            } else {
                val errorMsg = conn.errorStream?.bufferedReader()?.readText()
                Log.e(TAG, "Storage upload failed ($code): $errorMsg")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Storage upload exception: ${e.message}", e)
            null
        } finally {
            conn?.disconnect()
        }
    }

    /**
     * Post a notice/announcement to Supabase circulars table
     */
    suspend fun postNotice(
        notice: NoticeItem,
        targetAudience: String = "ALL",
        publishedBy: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        var success = false
        try {
            val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val circJson = JSONObject().apply {
                put("id", notice.id)
                put("title", notice.title)
                put("description", notice.description)
                put("date", dateStr)
                put("category", notice.category)
                put("target", targetAudience)
                put("is_urgent", notice.isUrgent)
                put("is_published", true)
                if (!publishedBy.isNullOrBlank()) {
                    put("published_by", publishedBy)
                }
            }
            val circRes = executeRequest("/circulars", "POST", circJson.toString())
            if (circRes != null) success = true
        } catch (e: Exception) {
            Log.d(TAG, "Note: remote post circular error: ${e.message}")
        }
        success
    }

    /**
     * Fetch TeacherProfile from Supabase
     */
    suspend fun fetchTeacherProfile(email: String?, authId: String?): TeacherProfile? = withContext(Dispatchers.IO) {
        val obj = fetchFacultyByEmailOrAuthId(email, authId) ?: return@withContext null
        try {
            val photoUrlVal = if (obj.isNull("photo_url")) null else {
                val raw = obj.optString("photo_url", "").trim()
                if (raw.isNotBlank() && !raw.equals("null", ignoreCase = true)) raw else null
            }
            val rawDept = obj.optString("department", "Information Technology").trim()
            val dept = if (rawDept.isBlank() || rawDept.equals("General", ignoreCase = true)) "Information Technology" else rawDept

            val subjectsList = mutableListOf<String>()
            val subjsArr = obj.optJSONArray("subjects")
            if (subjsArr != null) {
                for (si in 0 until subjsArr.length()) {
                    val rawS = subjsArr.optString(si).trim()
                    if (rawS.isNotBlank()) {
                        subjectsList.add(MasterSubjectRegistry.normalize(rawS))
                    }
                }
            } else {
                val sStr = obj.optString("subjects", "")
                if (sStr.isNotBlank()) {
                    sStr.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { rawS ->
                        subjectsList.add(MasterSubjectRegistry.normalize(rawS))
                    }
                }
            }
            if (subjectsList.isEmpty()) {
                subjectsList.add("Computer Organization & Architecture (BCS-302)")
                subjectsList.add("Data Structure (BCS-301)")
            }

            TeacherProfile(
                id = obj.optString("id", "fac_${System.currentTimeMillis()}"),
                name = obj.optString("name", "Faculty Member"),
                employeeId = obj.optString("employee_id", "EMP-001"),
                designation = obj.optString("designation", "Assistant Professor"),
                department = dept,
                email = obj.optString("email", email ?: ""),
                phone = obj.optString("phone", "+91 98123 00000"),
                weeklyWorkloadHours = obj.optInt("experience_years", 5) * 3,
                activeSubjectsCount = subjectsList.size,
                totalStudents = 180,
                classesToday = 4,
                pendingTasks = 5,
                photoUrl = photoUrlVal,
                subjects = subjectsList
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error building TeacherProfile", e)
            null
        }
    }

    private fun executeGetWithBearer(endpoint: String, bearerToken: String, timeoutMs: Int = 8000): Pair<Int, String?> {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL("$BASE_URL$endpoint")
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.useCaches = false
            conn.defaultUseCaches = false
            conn.connectTimeout = timeoutMs
            conn.readTimeout = timeoutMs
            conn.setRequestProperty("apikey", PUBLISHABLE_KEY)
            conn.setRequestProperty("Authorization", "Bearer $bearerToken")
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate")
            conn.setRequestProperty("Pragma", "no-cache")

            val code = conn.responseCode
            if (code in 200..299) {
                val body = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use { it.readText() }
                code to body
            } else {
                Log.d(TAG, "GET $endpoint returned HTTP $code")
                code to null
            }
        } catch (e: Exception) {
            Log.d(TAG, "GET $endpoint failed: ${e.message}")
            -1 to null
        } finally {
            conn?.disconnect()
        }
    }

    val AUTHENTIC_DEFAULT_FACULTY = listOf(
        FacultyInfo(
            id = "fac_1795020868260",
            name = "Dr. Pinki Negi Bora",
            employeeId = "3",
            designation = "Assistant Professor",
            department = "Information Technology",
            email = "pinki@recm.in",
            phone = "1111111111",
            qualification = "PHD",
            experienceYears = 15,
            cabinRoom = "",
            linkedin = "",
            photoUrl = null,
            subjects = listOf("Technical Communication (BAS-301)")
        ),
        FacultyInfo(
            id = "fac_1795020757156",
            name = "Rishav Kumar Singh",
            employeeId = "7",
            designation = "Assistant Professor",
            department = "Information Technology",
            email = "rishav@recm.in",
            phone = "8686515118",
            qualification = "M Tech",
            experienceYears = 1,
            cabinRoom = "",
            linkedin = "",
            photoUrl = null,
            subjects = listOf("Discrete Structure & Theory of Logic (T) (BCS-303)")
        ),
        FacultyInfo(
            id = "fac_1795020633812",
            name = "Prakash Pandey",
            employeeId = "6",
            designation = "Assistant Professor",
            department = "Information Technology",
            email = "prakash@recm.in",
            phone = "9621023012",
            qualification = "M Tech",
            experienceYears = 4,
            cabinRoom = "",
            linkedin = "",
            photoUrl = null,
            subjects = listOf("Computer Organization & Architecture (BCS-302)")
        ),
        FacultyInfo(
            id = "fac_1795020529612",
            name = "Anant Sikarwar",
            employeeId = "5",
            designation = "Assistant Professor",
            department = "Information Technology",
            email = "anant@recm.in",
            phone = "7505626917",
            qualification = "M Tech",
            experienceYears = 1,
            cabinRoom = "",
            linkedin = "",
            photoUrl = null,
            subjects = listOf("Digital Electronics (BOE-310)")
        ),
        FacultyInfo(
            id = "fac_1795020415073",
            name = "Kalpana Suman",
            employeeId = "8",
            designation = "Assistant Professor",
            department = "Information Technology",
            email = "kalpana@recm.in",
            phone = "6395451372",
            qualification = "M Tech",
            experienceYears = 10,
            cabinRoom = "",
            linkedin = "",
            photoUrl = null,
            subjects = listOf("Data Structure (BCS-301)")
        ),
        FacultyInfo(
            id = "fac_1795020277360",
            name = "Dharamraj Yadav",
            employeeId = "4",
            designation = "Assistant Professor",
            department = "Information Technology",
            email = "dharamaraj@recm.in",
            phone = "9005255041",
            qualification = "M Tech",
            experienceYears = 10,
            cabinRoom = "",
            linkedin = "",
            photoUrl = null,
            subjects = listOf("Python Programming (BCC-302)")
        )
    )

    /**
     * Fetch all authentic faculty directory profiles directly from Supabase `faculty` table,
     * filtering out removed legacy faculty and ensuring authentic active directory profiles.
     */
    suspend fun fetchAllFaculty(): List<FacultyInfo> = withContext(Dispatchers.IO) {
        val userToken = activeAccessToken ?: SessionManager.get()?.getAccessToken()

        fun queryEndpoint(endpoint: String): String? {
            if (!userToken.isNullOrBlank()) {
                val (code, body) = executeGetWithBearer(endpoint, userToken)
                if (body != null) return body
                // If user JWT failed or expired, fall back to publishable key
                if (code == 401 || code == 403 || code !in 200..299) {
                    val (_, fallbackBody) = executeGetWithBearer(endpoint, PUBLISHABLE_KEY)
                    if (fallbackBody != null) return fallbackBody
                }
            } else {
                val (_, body) = executeGetWithBearer(endpoint, PUBLISHABLE_KEY)
                if (body != null) return body
            }
            return null
        }

        val response = queryEndpoint("/faculty?select=*&order=created_at.desc")
            ?: queryEndpoint("/faculty?select=*")

        if (response.isNullOrBlank()) {
            return@withContext AUTHENTIC_DEFAULT_FACULTY
        }

        try {
            val arr = JSONArray(response)
            val list = mutableListOf<FacultyInfo>()
            val removedFacultyIds = setOf("fac_059c8dfd", "fac_eb98ee3c", "fac_433d3585")
            val removedEmployeeIds = setOf("25", "22", "00")

            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)

                // Skip any soft-deleted or inactive faculty records
                if (!obj.isNull("deleted_at") && obj.optString("deleted_at", "").trim().let { it.isNotBlank() && !it.equals("null", ignoreCase = true) }) {
                    continue
                }
                if (obj.has("is_deleted") && !obj.isNull("is_deleted") && obj.optBoolean("is_deleted", false)) {
                    continue
                }
                if (obj.has("is_active") && !obj.isNull("is_active") && !obj.optBoolean("is_active", true)) {
                    continue
                }
                if (obj.has("status") && !obj.isNull("status")) {
                    val statusVal = obj.optString("status", "").trim().lowercase()
                    if (statusVal in setOf("deleted", "removed", "inactive", "archived")) {
                        continue
                    }
                }

                fun firstString(vararg keys: String): String {
                    for (k in keys) {
                        if (!obj.isNull(k)) {
                            val v = obj.optString(k, "").trim()
                            if (v.isNotBlank() && !v.equals("null", ignoreCase = true)) return v
                        }
                    }
                    return ""
                }

                val facId = firstString("id")
                val name = firstString("name", "full_name", "faculty_name")
                val empId = firstString("employee_id", "faculty_id")

                if (name.isBlank()) continue

                // Strictly filter out the old removed records (nitin #25, raj #22, kalpana #00)
                val cleanNameLower = name.trim().lowercase()
                if (facId in removedFacultyIds ||
                    empId in removedEmployeeIds ||
                    (cleanNameLower == "nitin" && empId == "25") ||
                    (cleanNameLower == "raj" && empId == "22") ||
                    (cleanNameLower == "kalpana" && empId == "00")
                ) {
                    continue
                }

                val photoUrlVal = firstString("photo_url", "avatar_url").ifBlank { null }
                val dept = firstString("department")
                val designation = firstString("designation", "role")
                val qualification = firstString("qualification", "education")
                val email = firstString("email")
                val phone = firstString("phone", "mobile", "mobile_number", "contact_number")
                val cabin = firstString("cabin_room", "office_room", "cabin")
                val linkedin = firstString("linkedin", "linkedin_url")

                val expYears = if (obj.isNull("experience_years")) {
                    0
                } else {
                    val rawExp = obj.opt("experience_years")
                    when (rawExp) {
                        is Number -> rawExp.toInt()
                        is String -> Regex("""\d+""").find(rawExp)?.value?.toIntOrNull() ?: 0
                        else -> 0
                    }
                }

                val subjectsList = mutableListOf<String>()
                val subjsArr = obj.optJSONArray("subjects")
                if (subjsArr != null) {
                    for (si in 0 until subjsArr.length()) {
                        val rawS = subjsArr.optString(si, "").trim()
                        if (rawS.isNotBlank() && !rawS.equals("null", ignoreCase = true)) {
                            subjectsList.add(rawS)
                        }
                    }
                } else {
                    val sStr = firstString("subjects", "subject")
                    if (sStr.isNotBlank()) {
                        if (sStr.startsWith("[")) {
                            try {
                                val parsed = JSONArray(sStr)
                                for (si in 0 until parsed.length()) {
                                    val rawS = parsed.optString(si, "").trim()
                                    if (rawS.isNotBlank() && !rawS.equals("null", ignoreCase = true)) {
                                        subjectsList.add(rawS)
                                    }
                                }
                            } catch (_: Exception) {
                                sStr.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach {
                                    subjectsList.add(it)
                                }
                            }
                        } else {
                            sStr.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach {
                                subjectsList.add(it)
                            }
                        }
                    }
                }

                list.add(
                    FacultyInfo(
                        id = facId.ifBlank { "fac_$i" },
                        name = name,
                        employeeId = empId,
                        designation = designation,
                        department = dept,
                        email = email,
                        phone = phone,
                        qualification = qualification,
                        experienceYears = expYears,
                        cabinRoom = cabin,
                        linkedin = linkedin,
                        photoUrl = photoUrlVal,
                        subjects = subjectsList.distinct()
                    )
                )
            }

            // Merge with authentic active faculty directory to guarantee new data is always shown
            val merged = mutableListOf<FacultyInfo>()
            merged.addAll(list)

            for (defaultFac in AUTHENTIC_DEFAULT_FACULTY) {
                val exists = merged.any {
                    it.employeeId.isNotBlank() && it.employeeId.equals(defaultFac.employeeId, ignoreCase = true) ||
                    it.name.trim().equals(defaultFac.name.trim(), ignoreCase = true)
                }
                if (!exists) {
                    merged.add(defaultFac)
                }
            }

            merged
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all faculty", e)
            AUTHENTIC_DEFAULT_FACULTY
        }
    }

    /**
     * Fetch all registered students from Supabase (including photos, roll number, contacts, attendance),
     * with optional department-based filtering.
     */
    suspend fun fetchAllStudents(departmentFilter: String? = null): List<StudentRosterItem>? = withContext(Dispatchers.IO) {
        val response = executeRequest("/students?select=*&order=created_at.desc") ?: return@withContext null
        try {
            val arr = JSONArray(response)
            if (arr.length() == 0) return@withContext null

            val list = mutableListOf<StudentRosterItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val id = obj.optString("id", "std_${System.currentTimeMillis()}_$i")
                val name = obj.optString("name", "Student").trim()
                val roll = obj.optString("roll_number", "Roll-$i").trim()
                val email = obj.optString("email", "").trim()
                val phone = obj.optString("phone", "").trim()
                val rawDept = obj.optString("department", "Information Technology").trim()
                val dept = if (rawDept.isBlank() || rawDept.equals("General", ignoreCase = true)) "Information Technology" else rawDept
                val semInt = obj.optInt("semester", 3)
                val secStr = obj.optString("section", "A").trim()
                val section = if (secStr.startsWith("IT") || secStr.contains("Sem")) secStr else "IT-Sec $secStr"
                val attRate = obj.optInt("attendance_rate", 85)
                val cgpaVal = obj.optDouble("cgpa", 8.0)
                val guardianName = obj.optString("guardian_name", "Guardian / Parent")
                val guardianPhone = obj.optString("guardian_phone", "+91 98765 43210")
                val photoUrlVal = if (obj.isNull("photo_url")) null else {
                    val raw = obj.optString("photo_url", "").trim()
                    if (raw.isNotBlank() && !raw.equals("null", ignoreCase = true)) raw else null
                }

                list.add(
                    StudentRosterItem(
                        id = id,
                        rollNo = roll,
                        studentName = name,
                        section = section,
                        status = StudentAttendanceStatus.UNMARKED,
                        email = email,
                        phone = phone,
                        department = dept,
                        currentSemester = "Semester $semInt (Year 2)",
                        attendancePercentage = attRate,
                        cgpa = cgpaVal,
                        guardianName = guardianName,
                        guardianContact = guardianPhone,
                        photoUrl = photoUrlVal
                    )
                )
            }

            // Department-based assignment filtering
            if (!departmentFilter.isNullOrBlank() &&
                !departmentFilter.equals("ALL", ignoreCase = true) &&
                !departmentFilter.equals("General", ignoreCase = true)
            ) {
                val filtered = list.filter {
                    it.department.equals(departmentFilter.trim(), ignoreCase = true) ||
                    it.department.equals("Information Technology", ignoreCase = true) ||
                    it.department.equals("General", ignoreCase = true)
                }
                if (filtered.isNotEmpty()) return@withContext filtered
            }

            list
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing all students from Supabase", e)
            null
        }
    }
}

data class AttendanceCalculationResult(
    val subjects: List<SubjectAttendance>,
    val calculatedPercentage: Int,
    val totalClasses: Int,
    val presentClasses: Int
)

data class SupabaseAuthResult(
    val isSuccess: Boolean,
    val userId: String? = null,
    val email: String? = null,
    val role: String? = null,
    val name: String? = null,
    val identifier: String? = null,
    val accessToken: String? = null,
    val errorMessage: String? = null
)
