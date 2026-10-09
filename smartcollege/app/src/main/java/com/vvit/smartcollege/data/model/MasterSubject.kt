package com.vvit.smartcollege.data.model

data class MasterSubject(
    val id: String,
    val name: String,
    val code: String,
    val status: String = "Active"
) {
    val displayName: String get() = "$name ($code)"
}

object MasterSubjectRegistry {
    val subjects: List<MasterSubject> = listOf(
        MasterSubject("sub_bcs301", "Data Structure", "BCS-301"),
        MasterSubject("sub_bcc302", "Python Programming", "BCC-302"),
        MasterSubject("sub_boe310", "Digital Electronics", "BOE-310"),
        MasterSubject("sub_bcs303", "Discrete Structure & Theory of Logic (T)", "BCS-303"),
        MasterSubject("sub_bcs302", "Computer Organization & Architecture", "BCS-302"),
        MasterSubject("sub_bas301", "Technical Communication", "BAS-301")
    )

    fun getAll(): List<MasterSubject> = subjects

    fun find(query: String?): MasterSubject? {
        if (query.isNullOrBlank()) return null
        val clean = query.trim()
        val lower = clean.lowercase()

        // Exact ID, Code or Name
        subjects.firstOrNull { it.id.equals(clean, ignoreCase = true) }?.let { return it }
        subjects.firstOrNull { it.code.equals(clean, ignoreCase = true) }?.let { return it }
        subjects.firstOrNull { it.displayName.equals(clean, ignoreCase = true) }?.let { return it }
        subjects.firstOrNull { it.name.equals(clean, ignoreCase = true) }?.let { return it }

        // Code matching
        subjects.firstOrNull { lower.contains(it.code.lowercase()) }?.let { return it }

        // Keyword matching
        if (lower.contains("discrete") || lower.contains("dstl")) return subjects.find { it.code == "BCS-303" }
        if (lower.contains("coa") || lower.contains("computer org") || lower.contains("architecture")) return subjects.find { it.code == "BCS-302" }
        if (lower.contains("python")) return subjects.find { it.code == "BCC-302" }
        if (lower.contains("data struct") || lower.contains("ds lab") || lower.contains("data structure")) return subjects.find { it.code == "BCS-301" }
        if (lower.contains("digital elec") || lower.contains("electronics")) return subjects.find { it.code == "BOE-310" }
        if (lower.contains("technical comm") || lower.contains("communication")) return subjects.find { it.code == "BAS-301" }

        return null
    }

    fun normalize(query: String?): String {
        val s = find(query)
        return s?.displayName ?: query?.trim() ?: "Computer Organization & Architecture (BCS-302)"
    }

    /**
     * Verifies if a teacher assigned list grants access to a specific subject
     */
    fun isAuthorized(assignedList: List<String>, subject: String): Boolean {
        if (assignedList.isEmpty()) {
            return true
        }
        val target = find(subject) ?: return assignedList.any { it.contains(subject, ignoreCase = true) }
        return assignedList.any { assigned ->
            val mapped = find(assigned)
            mapped?.code == target.code || assigned.contains(target.code, ignoreCase = true) || assigned.contains(target.name, ignoreCase = true)
        }
    }
}
