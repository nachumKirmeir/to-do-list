package com.nachumToDoApp.vr2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.nachumToDoApp.vr2.Adapters.RecycleBinAdapter;
import com.nachumToDoApp.vr2.Mission.ToDoModel;
import com.nachumToDoApp.vr2.Utils.RecycleBinDatabaseHandler;

import java.util.Collections;
import java.util.List;

public class RecycleBin extends AppCompatActivity{


    private RecycleBinDatabaseHandler db;//גישה לבסיס הנתונים של סל המחזור

    //RecyclerView
    private RecyclerView tasksRecyclerView;
    private RecycleBinAdapter recycleBinAdapter;//מה שהכראי לעדכון ושמירה של המשימות
    private List<ToDoModel> taskList;//רשימת המשימות בסל המחזור

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle_bin);

        //get excess to the dataBase
        db = new RecycleBinDatabaseHandler(this);
        db.openDatabase();


        tasksRecyclerView = findViewById(R.id.recycleBinRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recycleBinAdapter = new RecycleBinAdapter(db, RecycleBin.this);
        tasksRecyclerView.setAdapter(recycleBinAdapter);
        taskList = db.getAllTasks();
        //i reverse the list so that the tasks will be from newest to oldest
        Collections.reverse(taskList);
        recycleBinAdapter.setTasks(taskList);

        //when the user remove an item from the main list the item will be added to the recycle bin
        if(getIntent().getExtras() != null){
            addNewItem();
            finish();
        }
        ItemTouchHelper itemTouchHelper =
                new ItemTouchHelper(new RecyclerItemTouchHelperRecycleBin(recycleBinAdapter, taskList, RecycleBin.this));
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);
    }
    //this will add the new item to the list
    public void addNewItem(){
        Intent intent = getIntent();
        ToDoModel task = new ToDoModel();
        task.setTask(intent.getExtras().getString("task"));
        task.setStatus(intent.getExtras().getInt("status"));
        db.insertTask(task);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recycle_bin_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    //when the user click on an item in the menu
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        if(item.getItemId() == R.id.returnHomePage) {
            finish();
        }
        //this will remove the items from the database and from the activity forever
        if(item.getItemId() == R.id.cleanAll){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete All Task");
            builder.setMessage("Are you sure you want to delete all the tasks in the recycle bin?");
            builder.setCancelable(false);
            builder.setPositiveButton("Confirm",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for(ToDoModel toDoModel: taskList){
                                db.deleteTask(toDoModel.getId());
                            }
                            taskList.clear();
                            recycleBinAdapter.setTasks(taskList);
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

        }
        return true;
    }
}