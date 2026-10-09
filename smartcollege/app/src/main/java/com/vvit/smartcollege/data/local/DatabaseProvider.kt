package com.vvit.smartcollege.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.vvit.smartcollege.data.local.dao.AdminDao
import com.vvit.smartcollege.data.local.dao.AuthDao
import com.vvit.smartcollege.data.local.dao.StudentDao
import com.vvit.smartcollege.data.local.dao.TeacherDao

object DatabaseProvider {
    private var dbHelper: SmartCollegeDbHelper? = null
    private var database: SQLiteDatabase? = null

    var appContext: Context? = null
        private set
    var authDao: AuthDao? = null
        private set
    var studentDao: StudentDao? = null
        private set
    var teacherDao: TeacherDao? = null
        private set
    var adminDao: AdminDao? = null
        private set
    var sessionManager: SessionManager? = null
        private set

    fun init(context: Context) {
        if (dbHelper == null) {
            val appCtx = context.applicationContext
            appContext = appCtx
            sessionManager = SessionManager.getInstance(appCtx)
            val helper = SmartCollegeDbHelper(appCtx)
            dbHelper = helper
            val db = helper.writableDatabase
            database = db
            authDao = AuthDao(db)
            studentDao = StudentDao(db)
            teacherDao = TeacherDao(db)
            adminDao = AdminDao(db)
        }
    }

    fun initForTesting(db: SQLiteDatabase) {
        database = db
        authDao = AuthDao(db)
        studentDao = StudentDao(db)
        teacherDao = TeacherDao(db)
        adminDao = AdminDao(db)
    }

    fun getDatabase(): SQLiteDatabase? = database
}
