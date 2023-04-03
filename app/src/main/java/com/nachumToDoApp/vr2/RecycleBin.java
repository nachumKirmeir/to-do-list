package com.nachumToDoApp.vr2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.nachumToDoApp.vr2.Adapters.RecycleBinAdapter;
import com.nachumToDoApp.vr2.Model.ToDoModel;
import com.nachumToDoApp.vr2.Utils.RecycleBinDatabaseHandler;

import java.util.Collections;
import java.util.List;

public class RecycleBin extends AppCompatActivity{


    private RecycleBinDatabaseHandler db;
    private RecyclerView tasksRecyclerView;
    private RecycleBinAdapter recycleBinAdapter;
    private List<ToDoModel> taskList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle_bin);

        db = new RecycleBinDatabaseHandler(this);
        db.openDatabase();

        tasksRecyclerView = findViewById(R.id.recycleBinRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recycleBinAdapter = new RecycleBinAdapter(db, RecycleBin.this);
        tasksRecyclerView.setAdapter(recycleBinAdapter);

        taskList = db.getAllTasks();
        //i reverse the list so that the tasks will be from newest to oldest
        //i can had to the menu couple ways to sort the tasks
        Collections.reverse(taskList);
        recycleBinAdapter.setTasks(taskList);

        if(getIntent().getExtras() != null){
            addNewItem();
            finish();
        }
        ItemTouchHelper itemTouchHelper =
                new ItemTouchHelper(new RecyclerItemTouchHelperRecycleBin(recycleBinAdapter, taskList, RecycleBin.this));
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);
    }
    public void addNewItem(){
        Intent intent = getIntent();
        ToDoModel task = new ToDoModel();
        task.setTask(intent.getExtras().getString("task"));
        task.setStatus(intent.getExtras().getInt("status"));
        db.insertTask(task);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        if(item.getItemId() == R.id.returnHomePage) {
            finish();
        }
        return true;
    }
}