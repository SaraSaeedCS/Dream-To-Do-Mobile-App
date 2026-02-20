package com.example.todointerfaces;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todointerfaces.Adapter.tdDoAdapter;

public class RecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback {
    private tdDoAdapter adapter;
    public RecyclerItemTouchHelper(tdDoAdapter adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
    }
    @Override
    public boolean onMove(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder,
            @NonNull RecyclerView.ViewHolder target) {
        return false;
    }
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();
        if (direction == ItemTouchHelper.LEFT) {
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    viewHolder.itemView.getContext()
            );
            builder.setTitle("Delete Task");
            builder.setMessage("Are you sure you want to delete this Task?");
            builder.setPositiveButton("Confirm", (dialogInterface, i) -> {
                adapter.deleteItem(position);
            });
            builder.setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> {
                adapter.notifyItemChanged(viewHolder.getAdapterPosition());
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            adapter.editItem(position);
        }
    }
    @Override
    public void onChildDraw(
            @NonNull Canvas c,
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder,
            float dX, float dY,
            int actionState,
            boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20;
        final var context = itemView.getContext();
        Drawable icon;
        ColorDrawable background;
        if (dX > 0) {
            icon = ContextCompat.getDrawable(context, R.drawable.outline_brush_24);
            background = new ColorDrawable(ContextCompat.getColor(
                    context,
                    com.google.android.material.R.color.m3_ref_palette_primary80
            ));
        } else {
            icon = ContextCompat.getDrawable(context, R.drawable.outline_backspace_24);
            background = new ColorDrawable(Color.RED);
        }
        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() +
                (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + icon.getIntrinsicHeight();
        if (dX > 0) {
            int iconLeft = itemView.getLeft() + iconMargin;
            int iconRight = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            background.setBounds(
                    itemView.getLeft(),
                    itemView.getTop(),
                    itemView.getLeft() + ((int) dX) + backgroundCornerOffset,
                    itemView.getBottom()
            );
        } else if (dX < 0) {
            int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMargin;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            background.setBounds(
                    itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                    itemView.getTop(),
                    itemView.getRight(),
                    itemView.getBottom()
            );
        } else {
            background.setBounds(0, 0, 0, 0);
        }
        background.draw(c);
        icon.draw(c);
    }
}
