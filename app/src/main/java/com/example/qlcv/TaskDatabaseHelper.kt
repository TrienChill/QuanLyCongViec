package com.example.qlcv

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.math.log


class TaskDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "tasks.db"
        private const val DATABASE_VERSION = 1
    }

    private val mContext = context

    private val dbPath: String = mContext.getDatabasePath(DATABASE_NAME).path

    override fun onCreate(db: SQLiteDatabase) {
        // Nếu sử dụng cơ sở dữ liệu có sẵn, không cần tạo bảng ở đây
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Xử lý nâng cấp cơ sở dữ liệu nếu cần
    }

    fun createDatabase() {
        val databaseFile = File(dbPath)
        if (!databaseFile.exists()) {
            // Tạo thư mục nếu chưa tồn tại
            databaseFile.parentFile?.mkdirs()

            try {
                copyDatabase(dbPath)
            } catch (e: IOException) {
                throw RuntimeException("Error copying database", e)
            }
        } else {
            Log.d("DatabaseHelper", "Database already exists")
        }
    }

    @Throws(IOException::class)
    private fun copyDatabase(dbPath: String) {
        val inputStream: InputStream = mContext.assets.open(DATABASE_NAME)
        val outputStream: FileOutputStream = FileOutputStream(dbPath)
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }
        outputStream.flush()
        outputStream.close()
        inputStream.close()
    }

    // CRUD Operations

    fun addTask(task: Task): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("name", task.name)
            put("description", task.description)
        }
        val id = db.insert("tasks", null, values)
        db.close()
        return id
    }

    fun getAllTasks(): List<Task> {
        val tasks = mutableListOf<Task>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM tasks", null)

        Log.d("DatabaseHelper", "Cursor count: ${cursor.count}")

        if (cursor.moveToFirst()) {
            do {
                val task = Task(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                )
                tasks.add(task)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return tasks
    }

    fun updateTask(task: Task): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("name", task.name)
            put("description", task.description)
        }
        val rowsAffected = db.update("tasks", values, "id = ?", arrayOf(task.id.toString()))
        db.close()
        return rowsAffected
    }

    fun deleteTask(id: Int): Int {
        val db = this.writableDatabase
        val rowsDeleted = db.delete("tasks", "id = ?", arrayOf(id.toString()))
        db.close()
        return rowsDeleted
    }

}