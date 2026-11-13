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
import com.vvit.smartcollege.data.model.MasterSubject
import com.vvit.smartcollege.data.model.MasterSubjectRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object SupabaseApiClient {
    private const val TAG = "SupabaseApiClient"

    const val PROJECT_ID = "easrebumarwkloibtemf"
    const val BASE_URL = "https://$PROJECT_ID.supabase.co/rest/v1"
    const val AUTH_URL = "https://$PROJECT_ID.supabase.co/auth/v1"
    const val PUBLISHABLE_KEY = "sb_publishable_CsQmrZ6ejrBeu6sQlmQX7Q_UoEoomC6"
    const val FUNCTIONS_URL = "https://$PROJECT_ID.supabase.co/functions/v1"

    @Volatile
    private var currentAuthToken: String? = null

    fun setAuthToken(token: String?) {
        currentAuthToken = token
    }

    fun getAuthToken(): String {
        return currentAuthToken
            ?: com.vvit.smartcollege.data.local.DatabaseProvider.sessionManager?.getAccessToken()
            ?: com.vvit.smartcollege.data.local.SessionManager.get()?.getAccessToken()
            ?: PUBLISHABLE_KEY
    }

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
                val err = conn.errorStream?.bufferedReader()?.use { it.readText() }
                Log.w(TAG, "Request to $endpoint returned HTTP $code: $err")
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

                    val fn = if (obj.has("file_name") && !obj.isNull("file_name")) obj.optString("file_name", "").ifBlank { null } else null
                    val fu = if (obj.has("file_url") && !obj.isNull("file_url")) obj.optString("file_url", "").ifBlank { null } else null
                    val rawProvider = obj.optString("storage_provider", "").ifBlank { null }
                    val rawR2Key = obj.optString("r2_key", "").ifBlank { null }
                    val isR2 = (rawProvider?.equals("r2", ignoreCase = true) == true) ||
                            !rawR2Key.isNullOrBlank() ||
                            (fu?.startsWith("r2://") == true) ||
                            (fu?.startsWith("assignments/") == true)
                    val provider = if (isR2) "r2" else "supabase"
                    val r2Key = rawR2Key ?: if (fu?.startsWith("r2://") == true) {
                        fu.removePrefix("r2://").removePrefix("college-erp-files/")
                    } else if (fu?.startsWith("assignments/") == true) {
                        fu
                    } else null

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
                            fileName = fn,
                            fileUri = fu,
                            storageProvider = provider,
                            r2Key = r2Key
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
                    var storageProvider = "supabase"
                    var r2Key: String? = null

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
                        val sp = meta.optString("storage_provider", "")
                        val rk = meta.optString("r2_key", "")
                        if (sp.equals("r2", ignoreCase = true) || rk.isNotBlank() || fileUri?.startsWith("r2://") == true || fileUri?.startsWith("assignments/") == true) {
                            storageProvider = "r2"
                            r2Key = if (rk.isNotBlank()) rk else fileUri?.removePrefix("r2://")?.removePrefix("college-erp-files/")
                        }
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
                            fileUri = fileUri,
                            storageProvider = storageProvider,
                            r2Key = r2Key
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
                val isR2 = assignment.isR2Storage ||
                        assignment.storageProvider.equals("r2", ignoreCase = true) ||
                        !assignment.r2Key.isNullOrBlank() ||
                        assignment.fileUri?.startsWith("r2://") == true ||
                        assignment.fileUri?.startsWith("assignments/") == true
                val provider = if (isR2) "r2" else assignment.storageProvider
                put("storage_provider", provider)
                val key = assignment.r2Key ?: if (assignment.fileUri?.startsWith("r2://") == true) {
                    assignment.fileUri?.removePrefix("r2://")?.removePrefix("college-erp-files/")
                } else if (assignment.fileUri?.startsWith("assignments/") == true) {
                    assignment.fileUri
                } else null
                if (!key.isNullOrBlank()) {
                    put("r2_key", key)
                }
            }
            val res = executeRequest("/assignments", "POST", postJson.toString())
            if (res != null) success = true
        } catch (e: Exception) {
            Log.d(TAG, "Note: remote post assignment error: ${e.message}")
        }

        // 2. Also mirror into public.circulars table for real-time cloud accessibility
        try {
            val isR2 = assignment.isR2Storage ||
                    assignment.storageProvider.equals("r2", ignoreCase = true) ||
                    !assignment.r2Key.isNullOrBlank() ||
                    assignment.fileUri?.startsWith("r2://") == true ||
                    assignment.fileUri?.startsWith("assignments/") == true
            val provider = if (isR2) "r2" else assignment.storageProvider
            val key = assignment.r2Key ?: if (assignment.fileUri?.startsWith("r2://") == true) {
                assignment.fileUri?.removePrefix("r2://")?.removePrefix("college-erp-files/")
            } else if (assignment.fileUri?.startsWith("assignments/") == true) {
                assignment.fileUri
            } else null

            val descJson = JSONObject().apply {
                put("instructions", assignment.instructions)
                put("subject", assignment.subjectName)
                put("due_date", assignment.dueDate)
                put("max_marks", assignment.maxMarks)
                put("file_name", assignment.fileName ?: "")
                put("file_url", assignment.fileUri ?: "")
                put("storage_provider", provider)
                if (!key.isNullOrBlank()) {
                    put("r2_key", key)
                }
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
                    val rawProvider = obj.optString("storage_provider", "").ifBlank { null }
                    val rawR2Key = obj.optString("r2_key", "").ifBlank { null }
                    val isR2 = (rawProvider?.equals("r2", ignoreCase = true) == true) ||
                            !rawR2Key.isNullOrBlank() ||
                            (dUrl.startsWith("r2://")) ||
                            (fu?.startsWith("r2://") == true) ||
                            (dUrl.startsWith("study-materials/")) ||
                            (fu?.startsWith("study-materials/") == true)
                    val provider = if (isR2) "r2" else "supabase"
                    val r2Key = rawR2Key ?: if (dUrl.startsWith("r2://")) {
                        dUrl.removePrefix("r2://").removePrefix("college-erp-files/")
                    } else if (fu?.startsWith("r2://") == true) {
                        fu.removePrefix("r2://").removePrefix("college-erp-files/")
                    } else if (dUrl.startsWith("study-materials/")) {
                        dUrl
                    } else null

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
                            fileUri = fu ?: dUrl.ifBlank { null },
                            storageProvider = provider,
                            r2Key = r2Key
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
                    var storageProvider = "supabase"
                    var r2Key: String? = null

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
                        val sp = meta.optString("storage_provider", "")
                        val rk = meta.optString("r2_key", "")
                        if (sp.equals("r2", ignoreCase = true) || rk.isNotBlank() || fileUri?.startsWith("r2://") == true || fileUri?.startsWith("study-materials/") == true) {
                            storageProvider = "r2"
                            r2Key = if (rk.isNotBlank()) rk else fileUri?.removePrefix("r2://")?.removePrefix("college-erp-files/")
                        }
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
                            fileUri = fileUri,
                            storageProvider = storageProvider,
                            r2Key = r2Key
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

        val isR2 = material.isR2Storage ||
                material.storageProvider.equals("r2", ignoreCase = true) ||
                !material.r2Key.isNullOrBlank() ||
                material.fileUri?.startsWith("r2://") == true ||
                material.downloadUrl.startsWith("r2://") ||
                material.fileUri?.startsWith("study-materials/") == true ||
                material.downloadUrl.startsWith("study-materials/")

        val providerToSave = if (isR2) "r2" else material.storageProvider
        val keyToSave = material.r2Key ?: if (material.fileUri?.startsWith("r2://") == true) {
            material.fileUri?.removePrefix("r2://")?.removePrefix("college-erp-files/")
        } else if (material.downloadUrl.startsWith("r2://")) {
            material.downloadUrl.removePrefix("r2://").removePrefix("college-erp-files/")
        } else if (material.fileUri?.startsWith("study-materials/") == true) {
            material.fileUri
        } else if (material.downloadUrl.startsWith("study-materials/")) {
            material.downloadUrl
        } else null

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
                put("storage_provider", providerToSave)
                if (!keyToSave.isNullOrBlank()) {
                    put("r2_key", keyToSave)
                }
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
                put("storage_provider", providerToSave)
                if (!keyToSave.isNullOrBlank()) {
                    put("r2_key", keyToSave)
                }
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

    data class R2UploadPresignedResponse(
        val uploadUrl: String,
        val objectKey: String,
        val bucket: String,
        val expiresIn: Int = 3600
    )

    data class R2CompleteUploadResponse(
        val success: Boolean,
        val verified: Boolean,
        val objectKey: String,
        val contentLength: Long? = null,
        val etag: String? = null
    )

    /**
     * Request a secure presigned PUT URL from Supabase Edge Function to upload directly to Cloudflare R2.
     * Verifies user session and ensures only faculty can request upload URLs.
     */
    suspend fun getR2UploadPresignedUrl(
        fileName: String,
        fileType: String = "assignment",
        mimeType: String = "application/pdf"
    ): R2UploadPresignedResponse? = withContext(Dispatchers.IO) {
        val endpoints = listOf(
            "$FUNCTIONS_URL/r2-upload-url",
            "$FUNCTIONS_URL/r2-storage"
        )
        val bodyJson = JSONObject().apply {
            put("action", "upload")
            put("fileName", fileName)
            put("fileType", fileType)
            put("contentType", mimeType)
        }.toString()

        for (endpoint in endpoints) {
            var conn: HttpURLConnection? = null
            try {
                val url = URL(endpoint)
                conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 10000
                conn.readTimeout = 10000
                val token = getAuthToken()
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.setRequestProperty("apikey", PUBLISHABLE_KEY)
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Accept", "application/json")
                conn.doOutput = true

                OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                    writer.write(bodyJson)
                    writer.flush()
                }

                val code = conn.responseCode
                if (code in 200..299) {
                    val resp = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(resp)
                    val uploadUrl = json.optString("uploadUrl")
                    val objectKey = json.optString("objectKey")
                    val bucket = json.optString("bucket", "college-erp-files")
                    val expiresIn = json.optInt("expiresIn", 3600)
                    if (uploadUrl.isNotBlank() && objectKey.isNotBlank()) {
                        Log.d(TAG, "R2 presigned upload URL generated successfully for key: $objectKey")
                        return@withContext R2UploadPresignedResponse(
                            uploadUrl = uploadUrl,
                            objectKey = objectKey,
                            bucket = bucket,
                            expiresIn = expiresIn
                        )
                    }
                } else {
                    val err = conn.errorStream?.bufferedReader()?.use { it.readText() }
                    Log.w(TAG, "R2 upload-url request to $endpoint failed with code $code: $err")
                }
            } catch (e: Exception) {
                Log.w(TAG, "R2 upload-url request exception at $endpoint: ${e.message}")
            } finally {
                conn?.disconnect()
            }
        }
        null
    }

    /**
     * Upload binary bytes directly to Cloudflare R2 bucket via HTTP PUT using the presigned URL.
     */
    suspend fun uploadFileToR2(
        uploadUrl: String,
        fileBytes: ByteArray,
        mimeType: String = "application/pdf"
    ): Boolean = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL(uploadUrl)
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PUT"
            conn.connectTimeout = 30000
            conn.readTimeout = 30000
            conn.setRequestProperty("Content-Type", mimeType.ifBlank { "application/pdf" })
            conn.setFixedLengthStreamingMode(fileBytes.size)
            conn.doOutput = true

            conn.outputStream.use { os ->
                os.write(fileBytes)
                os.flush()
            }

            val code = conn.responseCode
            if (code in 200..299) {
                Log.d(TAG, "File uploaded successfully directly to Cloudflare R2 (HTTP $code)")
                true
            } else {
                val err = conn.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e(TAG, "Cloudflare R2 direct PUT upload failed ($code): $err")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Cloudflare R2 direct PUT upload exception: ${e.message}", e)
            false
        } finally {
            conn?.disconnect()
        }
    }

    /**
     * Call Supabase Edge Function to verify that the file was successfully written to Cloudflare R2 bucket.
     * Must be called after HTTP PUT upload succeeds and before saving metadata in the database.
     */
    suspend fun completeR2Upload(objectKey: String): R2CompleteUploadResponse? = withContext(Dispatchers.IO) {
        val cleanKey = objectKey.trim()
            .removePrefix("r2://")
            .removePrefix("college-erp-files/")

        val endpoints = listOf(
            "$FUNCTIONS_URL/complete-upload",
            "$FUNCTIONS_URL/r2-storage"
        )
        val bodyJson = JSONObject().apply {
            put("action", "complete")
            put("objectKey", cleanKey)
        }.toString()

        for (endpoint in endpoints) {
            var conn: HttpURLConnection? = null
            try {
                val url = URL(endpoint)
                conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 10000
                conn.readTimeout = 10000
                val token = getAuthToken()
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.setRequestProperty("apikey", PUBLISHABLE_KEY)
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Accept", "application/json")
                conn.doOutput = true

                OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                    writer.write(bodyJson)
                    writer.flush()
                }

                val code = conn.responseCode
                if (code in 200..299) {
                    val resp = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(resp)
                    val success = json.optBoolean("success", false)
                    val verified = json.optBoolean("verified", false)
                    if (success || verified) {
                        Log.d(TAG, "R2 upload verified successfully in bucket for key: $cleanKey")
                        return@withContext R2CompleteUploadResponse(
                            success = true,
                            verified = true,
                            objectKey = json.optString("objectKey", cleanKey),
                            contentLength = if (json.has("contentLength")) json.optLong("contentLength") else null,
                            etag = json.optString("etag")
                        )
                    }
                } else {
                    val err = conn.errorStream?.bufferedReader()?.use { it.readText() }
                    Log.w(TAG, "R2 complete-upload verification request to $endpoint failed with code $code: $err")
                }
            } catch (e: Exception) {
                Log.w(TAG, "R2 complete-upload request exception at $endpoint: ${e.message}")
            } finally {
                conn?.disconnect()
            }
        }
        null
    }

    /**
     * Request a secure presigned GET URL from Supabase Edge Function to download/view private Cloudflare R2 files.
     */
    suspend fun getR2DownloadUrl(objectKey: String): String? = withContext(Dispatchers.IO) {
        val cleanKey = objectKey.trim()
            .removePrefix("r2://")
            .removePrefix("college-erp-files/")

        val endpoints = listOf(
            "$FUNCTIONS_URL/r2-download-url",
            "$FUNCTIONS_URL/r2-storage"
        )
        val bodyJson = JSONObject().apply {
            put("action", "download")
            put("objectKey", cleanKey)
        }.toString()

        for (endpoint in endpoints) {
            var conn: HttpURLConnection? = null
            try {
                val url = URL(endpoint)
                conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 10000
                conn.readTimeout = 10000
                val token = getAuthToken()
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.setRequestProperty("apikey", PUBLISHABLE_KEY)
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Accept", "application/json")
                conn.doOutput = true

                OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                    writer.write(bodyJson)
                    writer.flush()
                }

                val code = conn.responseCode
                if (code in 200..299) {
                    val resp = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(resp)
                    val downloadUrl = json.optString("downloadUrl")
                    if (downloadUrl.isNotBlank()) {
                        Log.d(TAG, "R2 presigned download URL obtained successfully for key: $cleanKey")
                        return@withContext downloadUrl
                    }
                } else {
                    val err = conn.errorStream?.bufferedReader()?.use { it.readText() }
                    Log.w(TAG, "R2 download-url request to $endpoint failed with code $code: $err")
                }
            } catch (e: Exception) {
                Log.w(TAG, "R2 download-url request exception at $endpoint: ${e.message}")
            } finally {
                conn?.disconnect()
            }
        }
        null
    }

    /**
     * Resolves a download URL for any assignment or study material:
     * - If it's a legacy Supabase storage URL, returns it directly (backward compatibility).
     * - If it's stored in Cloudflare R2 (r2://, r2Key, or private path), requests a presigned download URL from Edge Functions.
     */
    suspend fun resolveDownloadUrl(rawUrl: String?, r2Key: String? = null): String? = withContext(Dispatchers.IO) {
        val candidateKey = r2Key?.ifBlank { null }
            ?: if (rawUrl?.startsWith("r2://") == true) rawUrl.removePrefix("r2://").removePrefix("college-erp-files/")
            else if (rawUrl?.startsWith("assignments/") == true || rawUrl?.startsWith("study-materials/") == true) rawUrl
            else null

        if (!candidateKey.isNullOrBlank()) {
            val r2Signed = getR2DownloadUrl(candidateKey)
            if (!r2Signed.isNullOrBlank()) return@withContext r2Signed
        }

        // Backward compatibility: If it's a legacy Supabase storage URL or any standard https link, serve directly
        if (!rawUrl.isNullOrBlank()) {
            if (rawUrl.contains(".supabase.co/storage/") || (rawUrl.startsWith("http://") || rawUrl.startsWith("https://"))) {
                return@withContext rawUrl
            }
        }

        null
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
