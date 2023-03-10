package net.penguincoders.doit.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.penguincoders.doit.Model.ToDoModel;
import net.penguincoders.doit.R;
import net.penguincoders.doit.RecycleBin;
import net.penguincoders.doit.Utils.RecycleBinDatabaseHandler;

import java.util.List;

public class RecycleBinAdapter extends RecyclerView.Adapter<RecycleBinAdapter.ViewHolder1>{

    private List<ToDoModel> todoList;
    private RecycleBinDatabaseHandler db;
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

    public void setTasks(List<ToDoModel> todoList) {
        this.todoList = todoList;
        //this line make the problem
        this.notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        ToDoModel item = todoList.get(position);
        db.deleteTask(item.getId());
        todoList.remove(position);
        notifyItemRemoved(position);
    }

    public class ViewHolder1 extends RecyclerView.ViewHolder {
        CheckBox task;
        ViewHolder1(View view) {
            super(view);
            task = view.findViewById(R.id.todoCheckBox);

        }

    }
}
