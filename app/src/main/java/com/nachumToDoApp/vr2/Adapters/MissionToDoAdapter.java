package com.nachumToDoApp.vr2.Adapters;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nachumToDoApp.vr2.AddNewTaskFragment;
import com.nachumToDoApp.vr2.MainActivity;
import com.nachumToDoApp.vr2.Model.ToDoModel;
import com.nachumToDoApp.vr2.NotificationReceiver;
import com.nachumToDoApp.vr2.ServiceNotification;
import com.nachumToDoApp.vr2.R;
import com.nachumToDoApp.vr2.TimerData;
import com.nachumToDoApp.vr2.Utils.MissionDatabaseHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MissionToDoAdapter extends RecyclerView.Adapter<MissionToDoAdapter.ViewHolder> implements Filterable {

    private List<ToDoModel> todoList;
    private MissionDatabaseHandler db;
    private MainActivity activity;
    private CountDownTimer currentTimer;



    public MissionToDoAdapter(MissionDatabaseHandler db, MainActivity activity) {
        this.db = db;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        db.openDatabase();
        holder.setPosition(-1);
        holder.setRealPosition(position);
        final ToDoModel item = todoList.get(position);
        holder.task.setText(item.getTask());
        holder.task.setChecked(toBoolean(item.getStatus()));
        holder.setPosition(position);
    }
    private boolean toBoolean(int n) {
        return n != 0;
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public Context getContext() {
        return activity;
    }

    public void setTasks(List<ToDoModel> todoList) {
        this.todoList = todoList;
        //this line make the problem
        this.notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        ToDoModel item = todoList.get(position);
        db.deleteTask(item.getId());
        todoList.remove(position);
        notifyDataSetChanged();
        Intent intent = new Intent(getContext(), ServiceNotification.class);
        activity.startService(intent);
        activity.moveItemToRecycleBin(item);
    }

    public void editItem(int position) {
        ToDoModel item = todoList.get(position);
        Bundle bundle = new Bundle();
        bundle.putInt("id", item.getId());
        bundle.putString("task", item.getTask());
        AddNewTaskFragment fragment = new AddNewTaskFragment();
        fragment.setArguments(bundle);
        fragment.show(activity.getSupportFragmentManager(), AddNewTaskFragment.TAG);
    }
    public void update(){
        db.openDatabase();
        this.todoList = db.getAllTasks();
        Collections.reverse(this.todoList);
        notifyDataSetChanged();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        CheckBox task;
        int position;
        int realPosition;
        Button button;

        ViewHolder(View view) {
            super(view);
            position = -1;
            task = view.findViewById(R.id.todoCheckBox);
            task.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(position != -1){
                        ToDoModel item = todoList.get(position);
                        if (b) {
                            db.updateStatus(item.getId(), 1);
                            activity.rain();
                            item.setStatus(1);
                        } else {
                            db.updateStatus(item.getId(), 0);
                            item.setStatus(0);
                        }
                    }
                }
            });
            button = view.findViewById(R.id.btnTimer);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showTimerDialog(realPosition);
                }
            });
        }

        private void showTimerDialog(final int position) {
            // create and show time picker dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            // calculate timer duration in milliseconds
                            long timerDuration = ((hourOfDay * 60L) + minute) * 60 * 1000;
                            // store timer data in data structure
                            scheduleNotification(new TimerData(position, timerDuration));
                        }
                    }, 0, 0, true);
            timePickerDialog.show();
        }
        public void scheduleNotification(TimerData timerData) {

            if (currentTimer != null) {
                currentTimer.cancel();
                currentTimer = null;
            }

            final int seconds = (int) (timerData.getDuration() / 1000);

            Intent intent = new Intent(getContext(), NotificationReceiver.class);
            intent.putExtra("position", timerData.getPosition());
            intent.putExtra("taskMessage", todoList.get(realPosition).getTask());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE); // add FLAG_IMMUTABLE flag

            AlarmManager alarmManager = (AlarmManager)activity.getSystemService(Context.ALARM_SERVICE);

            long triggerTime = SystemClock.elapsedRealtime() + (seconds * 1000L);

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent);

            //set the text view
            final TextView textView = activity.findViewById(R.id.taskTimer);
            String toDisplay = "Task: " + todoList.get(realPosition).getTask() + " | Time: " + seconds/60 + "m";
            textView.setText(toDisplay);
            textView.setVisibility(View.VISIBLE);

            // Add a progress bar
            final ProgressBar progressBar = activity.findViewById(R.id.progressBar);
            progressBar.setMax(seconds);
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);


            currentTimer = new CountDownTimer(seconds * 1000L, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    int remainingSeconds = (int) millisUntilFinished / 1000;
                    progressBar.setProgress(seconds - remainingSeconds);
                }

                @Override
                public void onFinish() {
                    progressBar.setVisibility(View.GONE);
                    textView.setVisibility(View.GONE);
                }
            };
            currentTimer.start();
        }
        public void setPosition(int position) {
            this.position = position;
        }
        public void setRealPosition(int position) {
            this.realPosition = position;
        }
    }
    @Override
    public Filter getFilter() {
        return exampleFilter;
    }
    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<ToDoModel> arrayListAfterSearch = new ArrayList<>();
            ArrayList<ToDoModel> fullList = new ArrayList<ToDoModel>(db.getAllTasks());
            if(charSequence == null || charSequence.length() == 0)
                arrayListAfterSearch.addAll(fullList);
            else {
                String text = charSequence.toString().toString().toLowerCase().trim();
                for(ToDoModel item: fullList){
                    if(item.getTask().toLowerCase().trim().contains(text))
                        arrayListAfterSearch.add(item);
                }
            }
            FilterResults results = new FilterResults();
            results.values = arrayListAfterSearch;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            ArrayList<ToDoModel> newList = new ArrayList<>((List)filterResults.values);
            Collections.reverse(newList);
            todoList.clear();
            todoList.addAll(newList);
            notifyDataSetChanged();
        }
    };
}
