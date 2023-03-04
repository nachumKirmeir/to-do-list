package net.penguincoders.doit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import net.penguincoders.doit.Adapters.RecycleBinAdapter;
import net.penguincoders.doit.Model.ToDoModel;
import net.penguincoders.doit.Utils.RecycleBinDatabaseHandler;

import java.util.Collections;
import java.util.List;

public class RecycleBin extends AppCompatActivity implements RecycleBinInterface{


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
        recycleBinAdapter = new RecycleBinAdapter(db, RecycleBin.this, this);
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
    }
    public void addNewItem(){
        Intent intent = getIntent();
        ToDoModel task = new ToDoModel();
        task.setTask(intent.getExtras().getString("task"));
        task.setStatus(intent.getExtras().getInt("status"));
        db.insertTask(task);
    }

    @Override
    public void onItemShortClick(final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(RecycleBin.this);
        builder.setTitle("Delete Task Forever");
        builder.setMessage("Are you sure you want to delete this Task Forever?");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        recycleBinAdapter.deleteItem(position);
                        Toast.makeText(RecycleBin.this, "The Item Deleted Forever", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onItemLongClick(final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(RecycleBin.this);
        builder.setTitle("Return Task");
        builder.setMessage("Are You Sure You Want To Return Task To The Main List?");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ToDoModel toDoModel = taskList.get(position);
                        Intent intent  = new Intent(RecycleBin.this, MainActivity.class);
                        intent.putExtra("task", toDoModel.getTask());
                        intent.putExtra("status", toDoModel.getStatus());
                        startActivity(intent);
                        recycleBinAdapter.deleteItem(position);
                        Toast.makeText(RecycleBin.this, "You Added The Task To Main List", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        if(item.getItemId() == R.id.returnHomePage)
            finish();
        return true;
    }
}