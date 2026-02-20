package com.example.todointerfaces.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todointerfaces.HomePage;
import com.example.todointerfaces.Model.toDoModel;
import com.example.todointerfaces.R;
import com.example.todointerfaces.Utils.DatabaseHandler;

import java.util.ArrayList;
import java.util.List;

public class tdDoAdapter extends RecyclerView.Adapter<tdDoAdapter.ViewHolder> {
    private List<toDoModel> todoList = new ArrayList<>();
    private final HomePage activity;
    private final DatabaseHandler db;
    public tdDoAdapter(DatabaseHandler db, HomePage activity) {
        this.db = db;
        this.activity = activity;
    }
    private Boolean toBoolean(int n) {
        return n != 0;
    }
    public void setTasks(List<toDoModel> todoList) {
        this.todoList = todoList;
        notifyDataSetChanged();
    }
    public Context getContext() {
        return activity;
    }
    public void deleteItem(int position) {
        toDoModel item = todoList.get(position);
        db.deleteTask(item.getId());
        todoList.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_layout, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        toDoModel item = todoList.get(position);
        holder.task.setOnCheckedChangeListener(null);
        holder.task.setText(item.getTask());
        holder.task.setChecked(toBoolean(item.getStatus()));
        String date = item.getDate();
        String time = item.getTime();
        StringBuilder dateTimeBuilder = new StringBuilder();
        if (date != null && !date.trim().isEmpty()) {
            dateTimeBuilder.append(date.trim());
        }
        if (time != null && !time.trim().isEmpty()) {
            if (dateTimeBuilder.length() > 0) {
                dateTimeBuilder.append(" • ");
            }
            dateTimeBuilder.append(time.trim());
        }
        if (dateTimeBuilder.length() == 0) {
            holder.textDateTime.setText("");
        } else {
            holder.textDateTime.setText(dateTimeBuilder.toString());
        }
        int priority = item.getPriority();
        String priorityLabel;
        int color;
        if (priority == 1) {
            priorityLabel = "High";
            color = 0xFFf94144;
        } else if (priority == 2) {
            priorityLabel = "Medium";
            color = 0xFFf8961e;
        } else {
            priorityLabel = "Low";
            color = 0xFF90be6d;
        }
        holder.textPriority.setText(priorityLabel);
        holder.textPriority.setTextColor(color);


        holder.task.setOnCheckedChangeListener((CompoundButton compoundButton, boolean isChecked) -> {
            db.updateStatus(item.getId(), isChecked ? 1 : 0);
        });
    }
    public void editItem(int position) {
        toDoModel item = todoList.get(position);
        android.content.Intent intent = new android.content.Intent(activity, com.example.todointerfaces.AddingTask.class);
        intent.putExtra("mode", "edit");
        intent.putExtra("task_id", item.getId());
        activity.startActivity(intent);
    }
    @Override
    public int getItemCount() {
        return todoList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox task;
        TextView textDateTime;
        TextView textPriority;
        ViewHolder(View view) {
            super(view);
            task = view.findViewById(R.id.checkBox);
            textDateTime = view.findViewById(R.id.textDateTime);
            textPriority = view.findViewById(R.id.textPriority);
        }
    }
}
