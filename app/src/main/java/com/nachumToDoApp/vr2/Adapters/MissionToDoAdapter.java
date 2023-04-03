package com.nachumToDoApp.vr2.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nachumToDoApp.vr2.AddNewTaskFragment;
import com.nachumToDoApp.vr2.MainActivity;
import com.nachumToDoApp.vr2.Model.ToDoModel;
import com.nachumToDoApp.vr2.ServiceNotification;
import com.nachumToDoApp.vr2.R;
import com.nachumToDoApp.vr2.Utils.MissionDatabaseHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MissionToDoAdapter extends RecyclerView.Adapter<MissionToDoAdapter.ViewHolder> implements Filterable {

    private List<ToDoModel> todoList;
    private MissionDatabaseHandler db;
    private MainActivity activity;


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
            
        }
        public void setPosition(int position) {
            this.position = position;
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