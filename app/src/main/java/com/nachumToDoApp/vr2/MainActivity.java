package com.nachumToDoApp.vr2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.android.material.navigation.NavigationView;
import com.nachumToDoApp.vr2.Adapters.MissionToDoAdapter;
import com.nachumToDoApp.vr2.Model.ToDoModel;
import com.nachumToDoApp.vr2.Utils.MissionDatabaseHandler;

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

public class MainActivity extends AppCompatActivity implements DialogCloseListener, NavigationView.OnNavigationItemSelectedListener {

    private MissionDatabaseHandler db;

    private RecyclerView tasksRecyclerView;
    private MissionToDoAdapter tasksAdapter;
    private FloatingActionButton floatingActionButton;
    private List<ToDoModel> taskList;


    private KonfettiView konfettiView;
    private Shape.DrawableShape drawableShape = null;
    private String firstName, lastName, email;
    private Bitmap imageUser;
    private DrawerLayout drawerLayout;
    private ImageView imageMenu;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setKonfettiView();

        //make the app in english only
        Configuration configuration = new Configuration();
        configuration.locale = Locale.ENGLISH;
        getSupportActionBar().hide();
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext()
                .getResources().getDisplayMetrics());

        //create the drawer Layout
        drawerLayout = findViewById(R.id.drawerLayout);
        imageMenu = findViewById(R.id.imageMenu);
        imageMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
                //in the navigationView there is an image view and text view for the user name
                setNavigationViewProfile();
                setSearchMenuOnNavigation();
            }
        });
        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);



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
                AddNewTaskFragment.newInstance().show(getSupportFragmentManager(), AddNewTaskFragment.TAG);
            }
        });

        ItemTouchHelper itemTouchHelper =
                new ItemTouchHelper(new RecyclerItemTouchHelperMainList(tasksAdapter));
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);

        //this intent is from the RecycleBin class
        if(getIntent().getExtras() != null){
            addNewItem();
        }
        setDataUser();
    }

    //this function will set the search menu
    public void setSearchMenuOnNavigation(){
        Menu menuView = navigationView.getMenu();

        MenuItem searchItem = menuView.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setTextDirection(View.TEXT_DIRECTION_LTR);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                tasksAdapter.getFilter().filter(s);
                return false;
            }
        });
    }
    //this function change the header of the NavigationView
    private void setNavigationViewProfile() {
        View headerView = navigationView.getHeaderView(0);
        if(imageUser != null){
            ImageView profileImage = headerView.findViewById(R.id.imageProfile);
            profileImage.setImageBitmap(imageUser);
        }
        if(firstName != null){
            TextView firstNameProfile = headerView.findViewById(R.id.FirstName);
            firstNameProfile.setText(firstName);
        }
        if(lastName != null){
            TextView lastNameProfile = headerView.findViewById(R.id.LastName);
            lastNameProfile.setText(lastName);
        }
        if(email != null){
            TextView email1 = headerView.findViewById(R.id.email);
            email1.setText(email);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.profile){
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivityForResult(intent, 0);
        }
        else if(item.getItemId() == R.id.shareProfile){
            //this convert put the bitmap in storage and get the uri
            String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), imageUser, "Image Of TheUser", null);
            Uri bitmapUri = Uri.parse(bitmapPath);

            Intent myIntent = new Intent(Intent.ACTION_SEND);
            myIntent.setType("image/*");
            String shareBody = printMission();
            myIntent.putExtra(Intent.EXTRA_SUBJECT, "Missions");
            myIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            myIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
            startActivity(Intent.createChooser(myIntent, "Share image using"));
        }
        else if(item.getItemId() == R.id.recycleBin) {
            Intent intent = new Intent(this, RecycleBin.class);
            startActivity(intent);
        }
        else if (item.getItemId() == R.id.home){
            drawerLayout.closeDrawer(navigationView);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Call the closeDrawer() method to close the drawer
                drawerLayout.closeDrawer(navigationView);
            }
        }, 500);
        return true;
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == RESULT_OK){
            setDataUser();
        }
    }
    //this function will create the string with all the tasks
    public String printMission(){
        StringBuilder text = new StringBuilder();
        text.append("User Name: ").append(firstName).append(" ").append(lastName).append("\n");
        text.append("Email: ").append(email).append("\n");
        List<ToDoModel> listMission = this.taskList;
        for(int i = 1; i < listMission.size() + 1; i++){
            ToDoModel toDoModel = listMission.get(i - 1);
            String toDoMassage = i + ": " + toDoModel.getTask() + " Done: " + (toDoModel.getStatus() == 1) + "\n\n";
            text.append(toDoMassage);
        }
        return text.toString();
    }
    @Override
    public void handleDialogClose(){
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
    //this function will save the data of the user from the profile and set the variables with this Values
    public void setDataUser(){
        SharedPreferences sharedPreferencesProfile = getSharedPreferences("profile", 0);
        firstName = sharedPreferencesProfile.getString("firstName", null);
        lastName = sharedPreferencesProfile.getString("lastName", null);
        email = sharedPreferencesProfile.getString("email", null);
        String encoded = sharedPreferencesProfile.getString("picture", null);
        if(encoded != null) {
            byte[] imageAsBytes = Base64.decode(encoded.getBytes(), Base64.DEFAULT);
            imageUser = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
        }
    }
    public void setKonfettiView() {
        konfettiView = findViewById(R.id.konfettiView);
        final Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_heart);
        drawableShape = new Shape.DrawableShape(drawable, true);
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
    }
    public void rain() {
        EmitterConfig emitterConfig = new Emitter(3, TimeUnit.SECONDS).perSecond(200);
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

}