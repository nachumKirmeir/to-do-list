package com.nachumToDoApp.vr2;

import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.nachumToDoApp.vr2.Adapters.MissionToDoAdapter;

public class RecyclerItemTouchHelperMainList extends ItemTouchHelper.SimpleCallback {

    private final MissionToDoAdapter adapter;

    public RecyclerItemTouchHelperMainList(MissionToDoAdapter adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
    }


    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    //on the user swipe the RecyclerView this function will trigger
    @Override
    public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();
        if (direction == ItemTouchHelper.LEFT) {//המשתמש רוצה למחוק את המשימה
            AlertDialog.Builder builder = new AlertDialog.Builder(adapter.getContext());//בניית דיאלוד לשאול את המשתמש אם הוא בטוח רוצה למחוק את המשימה
            builder.setTitle("Delete Task");
            builder.setMessage("Are you sure you want to delete this Task?");
            builder.setCancelable(false);
            builder.setPositiveButton("Confirm",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adapter.deleteItem(position);
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {//רוצה לערוך
            adapter.editItem(position);
        }
    }

    //כאשר המשתמש גורר את המשימה פעולה זו תצבע את הרקע מתחת למימה ותציב איקון של מחיקה או של עריכה
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
                icon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.ic_baseline_edit);
                background = new ColorDrawable(ContextCompat.getColor(adapter.getContext(), R.color.colorPrimaryDark));
            } else {//swipe left delete
                icon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.ic_baseline_delete);
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
