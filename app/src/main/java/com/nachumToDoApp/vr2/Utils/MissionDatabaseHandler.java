package com.nachumToDoApp.vr2.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nachumToDoApp.vr2.Mission.ToDoModel;

import java.util.ArrayList;
import java.util.List;

public class MissionDatabaseHandler extends SQLiteOpenHelper {

    private static final String NAME = "toDoListDatabase";//השם של הבסיס נתונים
    private static final String TODO_TABLE = "todo";//השם של הבסיס נתונים
    private static final String ID = "id";//עמודה שתיקרה id שתשמח לזיהוי של משימה
    private static final String TASK = "task";//עמודת טקסט שתכיל את תוכן המשימה
    private static final String STATUS = "status";//כאשר הערך 1 המשימה בוצעה 0 לא בוצעה
    private static final String CREATE_TODO_TABLE = "CREATE TABLE " + TODO_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TASK + " TEXT, "
            + STATUS + " INTEGER)";

    private SQLiteDatabase db;

    public MissionDatabaseHandler(Context context) {
        super(context, NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TODO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TODO_TABLE);
        // Create tables again
        onCreate(db);
    }

    public void openDatabase() {
        db = this.getWritableDatabase();
    }

    //add new task to the dataBase
    public void insertTask(ToDoModel task){
        ContentValues cv = new ContentValues();
        cv.put(TASK, task.getTask());
        cv.put(STATUS, task.getStatus());
        db.insert(TODO_TABLE, null, cv);
    }

    //this function return all the tasks in the data base
    public List<ToDoModel> getAllTasks(){
        List<ToDoModel> taskList = new ArrayList<>();
        Cursor cur = null;
        db.beginTransaction();
        try{
            cur = db.query(TODO_TABLE, null, null, null, null, null, null, null);
            if(cur != null){
                if(cur.moveToFirst()){//check if cur not empty
                    do{
                        ToDoModel task = new ToDoModel();
                        int id = cur.getColumnIndex(ID), taskString = cur.getColumnIndex(TASK), status = cur.getColumnIndex(STATUS);
                        task.setId(cur.getInt(id));
                        task.setTask(cur.getString(taskString));
                        task.setStatus(cur.getInt(status));
                        taskList.add(task);
                    }
                    while(cur.moveToNext());
                }
            }
        }
        finally {
            db.endTransaction();
            assert cur != null;
            cur.close();
        }
        return taskList;
    }

    //update the status after that the user change the status
    public void updateStatus(int id, int status){
        ContentValues cv = new ContentValues();
        cv.put(STATUS, status);
        db.update(TODO_TABLE, cv, ID + "= ?", new String[] {String.valueOf(id)});
    }

    //update the task in the database
    public void updateTask(int id, String task) {
        ContentValues cv = new ContentValues();
        cv.put(TASK, task);
        db.update(TODO_TABLE, cv, ID + "= ?", new String[] {String.valueOf(id)});
    }

    //delete the task with that id
    public void deleteTask(int id){
        db.delete(TODO_TABLE, ID + "= ?", new String[] {String.valueOf(id)});
    }
}
