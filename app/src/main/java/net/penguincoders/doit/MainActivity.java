package net.penguincoders.doit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.penguincoders.doit.Adapters.MissionToDoAdapter;
import net.penguincoders.doit.Model.ToDoModel;
import net.penguincoders.doit.Utils.MissionDatabaseHandler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.Angle;
import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.Spread;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.core.models.Size;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class MainActivity extends AppCompatActivity implements DialogCloseListener{

    private MissionDatabaseHandler db;

    private RecyclerView tasksRecyclerView;
    private MissionToDoAdapter tasksAdapter;
    private FloatingActionButton floatingActionButton;
    private List<ToDoModel> taskList;


    private KonfettiView konfettiView;
    private Shape.DrawableShape drawableShape = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setKonfettiView();
        Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "", Toast.LENGTH_SHORT).show();        //make the app in english only
        Configuration configuration = new Configuration();
        configuration.locale = Locale.ENGLISH;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext()
                .getResources().getDisplayMetrics());

        //open the database for writing and reading
        db = new MissionDatabaseHandler(this);
        db.openDatabase();


        //set the tasksRecyclerView and set the tasksAdapter
        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tasksAdapter = new MissionToDoAdapter(db,MainActivity.this);
        tasksRecyclerView.setAdapter(tasksAdapter);

        taskList = db.getAllTasks();
        //i reverse the list so that the tasks will be from newest to oldest
        //i can had to the menu couple ways to sort the tasks
        Collections.reverse(taskList);
        tasksAdapter.setTasks(taskList);

        //add new task to the list
        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG);
            }
        });

        ItemTouchHelper itemTouchHelper =
                new ItemTouchHelper(new RecyclerItemTouchHelper(tasksAdapter));
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);

        //this intent is from the RecycleBin
        if(getIntent().getExtras() != null){
            addNewItem();
        }
    }
    public void addNewItem(){
        Intent intent = getIntent();
        ToDoModel task = new ToDoModel();
        task.setTask(intent.getExtras().getString("task"));
        task.setStatus(intent.getExtras().getInt("status"));
        db.insertTask(task);
        this.taskList = db.getAllTasks();
        Collections.reverse(this.taskList);
        tasksAdapter.setTasks(taskList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.profile){
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        }
        else if(item.getItemId() == R.id.shareProfile){
            Intent myIntent = new Intent (Intent.ACTION_SEND);
            myIntent. setType ("text/plain");
            String shareBody = printMission();
            String shareSubject = "Your Mission";
            myIntent. putExtra (Intent.EXTRA_SUBJECT, shareSubject) ;
            myIntent.putExtra (Intent. EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(myIntent, "Share using"));
        }
        else if(item.getItemId() == R.id.recycleBin) {
            Intent intent = new Intent(this, RecycleBin.class);
            startActivity(intent);
        }
        return true;
    }
    //this function will create the string with all the tasks
    public String printMission(){
        StringBuilder text = new StringBuilder();
        List<ToDoModel> listMission = this.taskList;
        for(int i = 1; i < listMission.size() + 1; i++){
            ToDoModel toDoModel = listMission.get(i - 1);
            String toDoMassage = i + ": " + toDoModel.getTask() + " Done: " + (toDoModel.getStatus() == 1) + "\n\n";
            text.append(toDoMassage);
        }
        return text.toString();
    }
    @Override
    public void handleDialogClose(DialogInterface dialog){
        this.taskList = db.getAllTasks();

        Collections.reverse(this.taskList);
        tasksAdapter.setTasks(taskList);
    }
    public void moveItemToRecycleBin(ToDoModel toDoModel){
        Intent intent = new Intent(this, RecycleBin.class);
        intent.putExtra("task", toDoModel.getTask());
        intent.putExtra("status", toDoModel.getStatus());
        startActivity(intent);
    }
    public void rain() {
        EmitterConfig emitterConfig = new Emitter(4, TimeUnit.SECONDS).perSecond(200);
        konfettiView.start(
                new PartyFactory(emitterConfig)
                        .angle(Angle.BOTTOM)
                        .spread(Spread.ROUND)
                        .shapes(Arrays.asList(Shape.Square.INSTANCE, Shape.Circle.INSTANCE, drawableShape))
                        .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                        .setSpeedBetween(0f, 15f)
                        .position(new Position.Relative(0.0, 0.0).between(new Position.Relative(1.0, 0.0)))
                        .build()
        );
    }
    public void setKonfettiView() {
        konfettiView = findViewById(R.id.konfettiView);
        final Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_heart);
        drawableShape = new Shape.DrawableShape(drawable, true);
        konfettiView = findViewById(R.id.konfettiView);
        EmitterConfig emitterConfig = new Emitter(5L, TimeUnit.SECONDS).perSecond(50);
        final Party party = new PartyFactory(emitterConfig)
                .angle(270)
                .spread(90)
                .setSpeedBetween(1f, 5f)
                .timeToLive(2000L)
                .shapes(new Shape.Rectangle(0.2f), drawableShape)
                .sizes(new Size(12, 5f, 0.2f))
                .position(0.0, 0.0, 1.0, 0.0)
                .build();
        konfettiView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                konfettiView.start(party);
            }
        });
    }
}