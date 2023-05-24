package com.nachumToDoApp.vr2;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.nachumToDoApp.vr2.Adapters.RecycleBinAdapter;
import com.nachumToDoApp.vr2.Mission.ToDoModel;

import java.util.List;

public class RecyclerItemTouchHelperRecycleBin extends ItemTouchHelper.SimpleCallback{
    private final RecycleBinAdapter adapter;
    private final List<ToDoModel> taskList;
    //the main activity
    private final Activity activity;
    public RecyclerItemTouchHelperRecycleBin(RecycleBinAdapter adapter, List<ToDoModel> taskList, Activity activity) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
        this.taskList = taskList;
        this.activity = activity;
    }
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }
    //on the user swipe the RecyclerView this function will trigger
    @Override
    public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();
        AlertDialog.Builder builder = new AlertDialog.Builder(adapter.getContext());
        if(direction == ItemTouchHelper.LEFT) {
            builder.setTitle("Delete Task Forever");
            builder.setMessage("Are you sure you want to delete this Task Forever?");
            builder.setCancelable(false);
            builder.setPositiveButton("Confirm",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adapter.deleteItem(position);
                            Toast.makeText(adapter.getContext(), "The Item Deleted Forever", Toast.LENGTH_SHORT).show();
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        //swipe right
        else {
            //this code will return the task to  the main list
            builder.setTitle("Return Task");
            builder.setMessage("Are You Sure You Want To Return Task To The Main List?");
            builder.setCancelable(false);
            builder.setPositiveButton("Confirm",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ToDoModel toDoModel = taskList.get(position);
                            Intent intent  = new Intent(adapter.getContext(), MainActivity.class);
                            intent.putExtra("task", toDoModel.getTask());
                            intent.putExtra("status", toDoModel.getStatus());
                            adapter.getContext().startActivity(intent);
                            adapter.deleteItem(position);
                            Toast.makeText(adapter.getContext(), "You Added The Task To Main List", Toast.LENGTH_SHORT).show();
                            activity.finish();
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
    //this function will display under the recyclerView icons and put color when the item is swipe
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;

        final int MAX_DISPLACEMENT = itemView.getWidth() - 30;
        Drawable icon;
        ColorDrawable background;

        int backgroundCornerOffset = 20;

        //swipe right, edit
        if (dX > 0) {
            icon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.ic_baseline_add_24);
            background = new ColorDrawable(ContextCompat.getColor(adapter.getContext(), R.color.design_default_color_secondary));
        } else {//swipe left delete
            icon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.ic_baseline_delete_forever_24);
            background = new ColorDrawable(Color.RED);
        }

        assert icon != null;
        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + icon.getIntrinsicHeight();
        if (dX > 0) { // Move to the right
            int iconLeft = itemView.getLeft() + iconMargin;
            int iconRight = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            background.setBounds(itemView.getLeft(), itemView.getTop(),
                    itemView.getLeft() + (Math.min((int)dX, MAX_DISPLACEMENT)) + backgroundCornerOffset, itemView.getBottom());
        } else if (dX < 0) { // Move to the left
            int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMargin;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                    itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else { // view is unSwiped
            background.setBounds(0, 0, 0, 0);
        }

        background.draw(c);
        icon.draw(c);
    }
}
