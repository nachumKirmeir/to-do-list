package com.nachumToDoApp.vr2.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nachumToDoApp.vr2.Mission.ToDoModel;
import com.nachumToDoApp.vr2.R;
import com.nachumToDoApp.vr2.RecycleBin;
import com.nachumToDoApp.vr2.Utils.RecycleBinDatabaseHandler;

import java.util.List;

public class RecycleBinAdapter extends RecyclerView.Adapter<RecycleBinAdapter.ViewHolder1>{

    //the list of the items
    private List<ToDoModel> todoList;
    //the database
    private RecycleBinDatabaseHandler db;
    //main activity
    private RecycleBin activity;

    public RecycleBinAdapter(RecycleBinDatabaseHandler db, RecycleBin activity) {
        this.db = db;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder1 onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_layout, parent, false);
        return new ViewHolder1(itemView);
    }
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder1 holder, int position) {
        db.openDatabase();
        final ToDoModel item = todoList.get(position);
        holder.task.setText(item.getTask());
        holder.task.setChecked(toBoolean(item.getStatus()));
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

    //update the adapter
    public void setTasks(List<ToDoModel> todoList) {
        this.todoList = todoList;
        //this line make the problem
        this.notifyDataSetChanged();
    }
    //remove item from the list and from the database
    public void deleteItem(int position) {
        ToDoModel item = todoList.get(position);
        db.deleteTask(item.getId());
        todoList.remove(position);
        notifyItemRemoved(position);
    }

    public class ViewHolder1 extends RecyclerView.ViewHolder {
        CheckBox task;
        Button btnTimer;
        ViewHolder1(View view) {
            super(view);
            task = view.findViewById(R.id.todoCheckBox);
            //in the recycle the user should not be able to edit the item
            task.setClickable(false);
            task.setFocusable(false);
            btnTimer = view.findViewById(R.id.btnTimer);
            btnTimer.setVisibility(View.GONE);
        }
    }
}
