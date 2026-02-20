package com.example.todointerfaces.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.todointerfaces.Model.toDoModel;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "toDoListDataBase";
    private static final int DATABASE_VERSION = 3;
    public static final String TABLE_USER = "users";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_PHONE    = "phone";
    public static final String TABLE_TASK = "tasks";
    public static final String COLUMN_TASK_ID = "task_id";
    public static final String COLUMN_TASK_NAME = "name";
    public static final String COLUMN_TASK_CATEGORY = "category";
    public static final String COLUMN_TASK_TIME = "time";
    public static final String COLUMN_TASK_DATE = "date";
    public static final String COLUMN_TASK_NOTE = "note";
    public static final String COLUMN_TASK_PRIORITY = "priority";
    public static final String COLUMN_TASK_STATUS = "status";
    public static final String COLUMN_TASK_USER_ID = "user_id"; // FK → users.user_id


    private static final String CREATE_USER_TABLE =
            "CREATE TABLE " + TABLE_USER + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT, " +
                    COLUMN_EMAIL + " TEXT, " +
                    COLUMN_PHONE + " TEXT, " +   // NEW
                    COLUMN_PASSWORD + " TEXT" +
                    ");";
    private static final String CREATE_TASK_TABLE =
            "CREATE TABLE " + TABLE_TASK + " (" +
                    COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TASK_NAME + " TEXT, " +
                    COLUMN_TASK_CATEGORY + " TEXT, " +
                    COLUMN_TASK_TIME + " TEXT, " +
                    COLUMN_TASK_DATE + " TEXT, " +
                    COLUMN_TASK_NOTE + " TEXT, " +
                    COLUMN_TASK_PRIORITY + " INTEGER, " +
                    COLUMN_TASK_STATUS + " INTEGER, " +
                    COLUMN_TASK_USER_ID + " INTEGER, " +
                    "FOREIGN KEY (" + COLUMN_TASK_USER_ID + ") REFERENCES " +
                    TABLE_USER + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE" +
                    ");";

    private SQLiteDatabase db;
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_TASK_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For now: drop and recreate (OK for dev)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }
    public void openDataBase() {
        db = this.getWritableDatabase();
    }
    public void updateUserPasswordByEmail(String email, String newPassword) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, newPassword);

        db.update(
                TABLE_USER,
                values,
                "LOWER(" + COLUMN_EMAIL + ") = ?",
                new String[]{email.toLowerCase()}
        );
    }
    public long insertTask(toDoModel task) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TASK_NAME, task.getTask());
        cv.put(COLUMN_TASK_CATEGORY, task.getCategory());
        cv.put(COLUMN_TASK_TIME, task.getTime());
        cv.put(COLUMN_TASK_DATE, task.getDate());
        cv.put(COLUMN_TASK_NOTE, task.getNote());
        cv.put(COLUMN_TASK_PRIORITY, task.getPriority());
        cv.put(COLUMN_TASK_STATUS, task.getStatus());
        if (task.getUserId() != 0) {
            cv.put(COLUMN_TASK_USER_ID, task.getUserId());
        }
        return db.insert(TABLE_TASK, null, cv);
    }
    public List<toDoModel> getAllTasks() {
        List<toDoModel> taskList = new ArrayList<>();
        Cursor cur = null;
        try {
            cur = db.query(TABLE_TASK, null, null, null, null, null, null);
            if (cur != null && cur.moveToFirst()) {
                do {
                    toDoModel task = new toDoModel();
                    task.setId(cur.getInt(cur.getColumnIndexOrThrow(COLUMN_TASK_ID)));
                    task.setTask(cur.getString(cur.getColumnIndexOrThrow(COLUMN_TASK_NAME)));
                    task.setCategory(cur.getString(cur.getColumnIndexOrThrow(COLUMN_TASK_CATEGORY)));
                    task.setTime(cur.getString(cur.getColumnIndexOrThrow(COLUMN_TASK_TIME)));
                    task.setDate(cur.getString(cur.getColumnIndexOrThrow(COLUMN_TASK_DATE)));
                    task.setNote(cur.getString(cur.getColumnIndexOrThrow(COLUMN_TASK_NOTE)));
                    task.setPriority(cur.getInt(cur.getColumnIndexOrThrow(COLUMN_TASK_PRIORITY)));
                    task.setStatus(cur.getInt(cur.getColumnIndexOrThrow(COLUMN_TASK_STATUS)));
                    int userIdIndex = cur.getColumnIndex(COLUMN_TASK_USER_ID);
                    if (userIdIndex != -1 && !cur.isNull(userIdIndex)) {
                        task.setUserId(cur.getInt(userIdIndex));
                    }
                    taskList.add(task);
                } while (cur.moveToNext());
            }
        } finally {
            if (cur != null) cur.close();
        }
        return taskList;
    }
    public List<toDoModel> getTasksForUser(int userId) {
        List<toDoModel> taskList = new ArrayList<>();
        Cursor cur = null;
        try {
            cur = db.query(
                    TABLE_TASK,
                    null,
                    COLUMN_TASK_USER_ID + "=?",
                    new String[]{String.valueOf(userId)},
                    null, null, null
            );
            if (cur != null && cur.moveToFirst()) {
                do {
                    toDoModel task = new toDoModel();
                    task.setId(cur.getInt(cur.getColumnIndexOrThrow(COLUMN_TASK_ID)));
                    task.setTask(cur.getString(cur.getColumnIndexOrThrow(COLUMN_TASK_NAME)));
                    task.setCategory(cur.getString(cur.getColumnIndexOrThrow(COLUMN_TASK_CATEGORY)));
                    task.setTime(cur.getString(cur.getColumnIndexOrThrow(COLUMN_TASK_TIME)));
                    task.setDate(cur.getString(cur.getColumnIndexOrThrow(COLUMN_TASK_DATE)));
                    task.setNote(cur.getString(cur.getColumnIndexOrThrow(COLUMN_TASK_NOTE)));
                    task.setPriority(cur.getInt(cur.getColumnIndexOrThrow(COLUMN_TASK_PRIORITY)));
                    task.setStatus(cur.getInt(cur.getColumnIndexOrThrow(COLUMN_TASK_STATUS)));
                    task.setUserId(userId);
                    taskList.add(task);
                } while (cur.moveToNext());
            }
        } finally {
            if (cur != null) cur.close();
        }
        return taskList;
    }
    public void updateStatus(int id, int status) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TASK_STATUS, status);
        db.update(TABLE_TASK, cv, COLUMN_TASK_ID + "=?", new String[]{String.valueOf(id)});
    }
    public void deleteTask(int id) {
        db.delete(TABLE_TASK, COLUMN_TASK_ID + "=?", new String[]{String.valueOf(id)});
    }
    public toDoModel getTaskById(int id) {
        toDoModel task = null;
        Cursor cur = null;
        try {
            cur = db.query(
                    TABLE_TASK,
                    null,
                    COLUMN_TASK_ID + "=?",
                    new String[]{String.valueOf(id)},
                    null, null, null
            );
            if (cur != null && cur.moveToFirst()) {
                task = new toDoModel();
                task.setId(cur.getInt(cur.getColumnIndexOrThrow(COLUMN_TASK_ID)));
                task.setTask(cur.getString(cur.getColumnIndexOrThrow(COLUMN_TASK_NAME)));
                task.setCategory(cur.getString(cur.getColumnIndexOrThrow(COLUMN_TASK_CATEGORY)));
                task.setTime(cur.getString(cur.getColumnIndexOrThrow(COLUMN_TASK_TIME)));
                task.setDate(cur.getString(cur.getColumnIndexOrThrow(COLUMN_TASK_DATE)));
                task.setNote(cur.getString(cur.getColumnIndexOrThrow(COLUMN_TASK_NOTE)));
                task.setPriority(cur.getInt(cur.getColumnIndexOrThrow(COLUMN_TASK_PRIORITY)));
                task.setStatus(cur.getInt(cur.getColumnIndexOrThrow(COLUMN_TASK_STATUS)));
                int userIdIndex = cur.getColumnIndex(COLUMN_TASK_USER_ID);
                if (userIdIndex != -1 && !cur.isNull(userIdIndex)) {
                    task.setUserId(cur.getInt(userIdIndex));
                }
            }
        } finally {
            if (cur != null) cur.close();
        }
        return task;
    }
    public void updateTaskFull(toDoModel task) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TASK_NAME, task.getTask());
        cv.put(COLUMN_TASK_CATEGORY, task.getCategory());
        cv.put(COLUMN_TASK_TIME, task.getTime());
        cv.put(COLUMN_TASK_DATE, task.getDate());
        cv.put(COLUMN_TASK_NOTE, task.getNote());
        cv.put(COLUMN_TASK_PRIORITY, task.getPriority());
        cv.put(COLUMN_TASK_STATUS, task.getStatus());
        if (task.getUserId() > 0) {
            cv.put(COLUMN_TASK_USER_ID, task.getUserId());
        }
        db.update(
                TABLE_TASK,
                cv,
                COLUMN_TASK_ID + "=?",
                new String[]{String.valueOf(task.getId())}
        );
    }
    public long insertUser(String username, String email, String phone, String password) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_PASSWORD, password);
        return db.insert(TABLE_USER, null, values);
    }
    public boolean isEmailExists(String email) {
        Cursor cursor = null;
        try {
            cursor = db.query(
                    TABLE_USER,
                    new String[]{COLUMN_USER_ID},
                    COLUMN_EMAIL + "=?",
                    new String[]{email},
                    null, null, null
            );
            return cursor != null && cursor.moveToFirst();
        } finally {
            if (cursor != null) cursor.close();
        }
    }
    public String getUserEmailById(int userId) {
        String email = null;
        Cursor cursor = null;
        try {
            cursor = db.query(
                    TABLE_USER,
                    new String[]{COLUMN_EMAIL},
                    COLUMN_USER_ID + "=?",
                    new String[]{String.valueOf(userId)},
                    null, null, null
            );
            if (cursor != null && cursor.moveToFirst()) {
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return email;
    }
    // Update user's email
    public void updateUserEmail(int userId, String newEmail) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, newEmail);
        db.update(
                TABLE_USER,
                values,
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)}
        );
    }
    // Update user's password
    public void updateUserPassword(int userId, String newPassword) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, newPassword);
        db.update(
                TABLE_USER,
                values,
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)}
        );
    }


    public int getUserIdByEmailPassword(String email, String password) {
        int userId = -1;
        Cursor cursor = null;

        try {
            cursor = db.query(
                    TABLE_USER,
                    new String[]{COLUMN_USER_ID},
                    "LOWER(" + COLUMN_EMAIL + ") = ? AND " + COLUMN_PASSWORD + "=?",
                    new String[]{email.toLowerCase(), password},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return userId;
    }


    public void deleteUserAndTasks(int userId) {

        int rowsDeleted = db.delete(
                TABLE_USER,
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)}
        );
    }



}


