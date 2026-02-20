package com.example.todointerfaces;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import com.example.todointerfaces.Model.toDoModel;
import com.example.todointerfaces.Utils.AlarmReceiver;
import com.example.todointerfaces.Utils.DatabaseHandler;
import java.util.Calendar;
import java.util.Locale;
import com.example.todointerfaces.Utils.ThemeManager;
import com.example.todointerfaces.Utils.UserSessionManager;
public class AddingTask extends AppCompatActivity {
    private ActivityResultLauncher<String> notificationPermissionLauncher;
    Spinner CategoryList;
    Spinner priortyList;
    Button buttonPickTime;
    TextView textSelectedTime;
    int selectedHour;
    int selectedMinute;
    Button buttonPickDate;
    TextView textSelectedDate;
    int selectedYear;
    int selectedMonth;
    int selectedDay;
    EditText editTextTaskTitle;
    EditText editTextTaskNote;
    Button buttonSaveTask;
    private DatabaseHandler db;
    private boolean isEditMode = false;
    private int editTaskId = -1;
    private int editUserId = 0;
    private UserSessionManager session;
    private int currentUserId = -1;
    private TimePickerDialog pickerDialog;
    private AlarmManager alarmManger;
    Button cancelButton;
    Calendar now;
    private PendingIntent pendingIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.loadAndApplySavedTheme(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "alarm",
                    "Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getApplicationContext().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        saveTaskLogic();
                    } else {
                        Toast.makeText(this, "Notification permission denied. Cannot set reminder.", Toast.LENGTH_LONG).show();
                    }});
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_adding_task);
        db = new DatabaseHandler(this);
        db.openDataBase();
        session = new UserSessionManager(this);
        currentUserId = session.getUserId();
        cancelButton = findViewById(R.id.cancelBT);
        editTextTaskTitle = findViewById(R.id.TaskName);
        editTextTaskNote = findViewById(R.id.editTextText2);
        buttonSaveTask = findViewById(R.id.addTaskBtn);
        buttonSaveTask.setOnClickListener(v -> checkAndSaveTask());
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddingTask.this, AlarmReceiver.class);
                // Note: Request code '0' here will only cancel the alarm if the task ID was 0.
                pendingIntent = PendingIntent.getBroadcast(AddingTask.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
                if (alarmManger == null) {
                    alarmManger = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                }
                alarmManger.cancel(pendingIntent);
                Toast.makeText(AddingTask.this, "Alarm canceled", Toast.LENGTH_SHORT).show();
                Intent back= new Intent(AddingTask.this, HomePage.class);
                startActivity(back);
            }
        });
        CategoryList = findViewById(R.id.categoryList);
        String[] items = new String[]{"Personal", "Work", "Home"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        CategoryList.setAdapter(adapter);
        priortyList = findViewById(R.id.priortyList);
        String[] items2 = new String[]{"Low", "Medium", "High"};
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items2);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        priortyList.setAdapter(adapter2);
        buttonPickTime = findViewById(R.id.buttonPickTime);
        textSelectedTime = findViewById(R.id.textSelectedTime);

        now = Calendar.getInstance();
        selectedHour = now.get(Calendar.HOUR_OF_DAY);
        selectedMinute = now.get(Calendar.MINUTE);
        updateTimeText();
        buttonPickTime.setOnClickListener(v -> {
            pickerDialog = new TimePickerDialog(
                    AddingTask.this,
                    (view, hourOfDay, minute) -> {
                        selectedHour = hourOfDay;
                        selectedMinute = minute;
                        updateTimeText();
                    },
                    selectedHour,
                    selectedMinute,
                    true
            );
            pickerDialog.show();
        });
        buttonPickDate = findViewById(R.id.buttonPickDate);
        textSelectedDate = findViewById(R.id.textSelectedDate);
        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
        updateDateText();
        buttonPickDate.setOnClickListener(v -> {
            DatePickerDialog dateDialog = new DatePickerDialog(
                    AddingTask.this,
                    (DatePicker view, int year, int month, int dayOfMonth) -> {
                        selectedYear = year;
                        selectedMonth = month;
                        selectedDay = dayOfMonth;
                        updateDateText();
                    },
                    selectedYear,
                    selectedMonth,
                    selectedDay
            );
            dateDialog.show();
        });
        String mode = getIntent().getStringExtra("mode");
        if (mode != null && mode.equals("edit")) {
            isEditMode = true;
            editTaskId = getIntent().getIntExtra("task_id", -1);
            loadTaskForEdit();
        }
    }
    // Permission forAndroid 13/API 33+
    private void checkAndSaveTask() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                saveTaskLogic();
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                Toast.makeText(this, "We need notification permission to show reminders for your tasks.", Toast.LENGTH_LONG).show();
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            // Devices older
            saveTaskLogic();
        }
    }
    private void updateTimeText() {
        String time = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
        textSelectedTime.setText("Selected time: " + time);
    }

    private void updateDateText() {
        String date = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                selectedDay, (selectedMonth + 1), selectedYear);
        textSelectedDate.setText("Selected date: " + date);
    }
    private void loadTaskForEdit() {
        if (editTaskId == -1) return;
        toDoModel task = db.getTaskById(editTaskId);
        if (task == null) return;
        editUserId = task.getUserId();
        editTextTaskTitle.setText(task.getTask());
        editTextTaskNote.setText(task.getNote());
        String taskCategory = task.getCategory();
        if (taskCategory != null) {
            for (int i = 0; i < CategoryList.getCount(); i++) {
                if (taskCategory.equalsIgnoreCase(CategoryList.getItemAtPosition(i).toString())) {
                    CategoryList.setSelection(i);
                    break;
                }
            }
        }
        int prio = task.getPriority();
        int spinnerIndex;
        if (prio == 1) {
            spinnerIndex = 2;
        } else if (prio == 2) {
            spinnerIndex = 1;
        } else {
            spinnerIndex = 0;
        }
        priortyList.setSelection(spinnerIndex);
        String dateStr = task.getDate();
        if (dateStr != null && dateStr.contains("/")) {
            try {
                String[] parts = dateStr.split("/");
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]) - 1;
                int year = Integer.parseInt(parts[2]);
                selectedDay = day;
                selectedMonth = month;
                selectedYear = year;
            } catch (Exception e) {
                // ignore
            }
        }
        updateDateText();


        String timeStr = task.getTime();
        if (timeStr != null && timeStr.contains(":")) {
            try {
                String[] parts = timeStr.split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);
                selectedHour = hour;
                selectedMinute = minute;
            } catch (Exception e) {
                // ignore
            }
        }
        updateTimeText();
    }
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private void saveTaskLogic() {
        String title = editTextTaskTitle.getText().toString().trim();
        String note = editTextTaskNote.getText().toString().trim();
        if (title.isEmpty()) {
            editTextTaskTitle.setError("Please enter a task title");
            editTextTaskTitle.requestFocus();
            return;
        }

        String category = CategoryList.getSelectedItem().toString();
        String priorityStr = priortyList.getSelectedItem().toString();
        int priorityInt;
        switch (priorityStr) {
            case "High":
                priorityInt = 1;
                break;
            case "Medium":
                priorityInt = 2;
                break;
            default:
                priorityInt = 3;
        }
        String time = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
        String date = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                selectedDay, (selectedMonth + 1), selectedYear);
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.YEAR, selectedYear);
        alarmTime.set(Calendar.MONTH, selectedMonth);
        alarmTime.set(Calendar.DAY_OF_MONTH, selectedDay);
        alarmTime.set(Calendar.HOUR_OF_DAY, selectedHour);
        alarmTime.set(Calendar.MINUTE, selectedMinute);
        alarmTime.set(Calendar.SECOND, 0);
        alarmTime.set(Calendar.MILLISECOND, 0);
        if (alarmTime.before(Calendar.getInstance())) {
            Toast.makeText(this, "Cannot set alarm in the past!", Toast.LENGTH_SHORT).show();
            return;
        }
        toDoModel task = new toDoModel();
        task.setTask(title);
        task.setCategory(category);
        task.setTime(time);
        task.setDate(date);
        task.setNote(note);
        task.setPriority(priorityInt);
        task.setStatus(0);
        if (isEditMode && editTaskId != -1) {
            task.setUserId(editUserId);
            task.setId(editTaskId);
            db.updateTaskFull(task);
        } else {
            task.setUserId(currentUserId);
            long id = db.insertTask(task);
            task.setId((int) id);
        }

        Intent intent = new Intent(AddingTask.this, AlarmReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("note", note);
        intent.putExtra("taskId", task.getId());
        int alarmId = task.getId();
        pendingIntent = PendingIntent.getBroadcast(
                AddingTask.this,
                alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManger = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManger.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTime.getTimeInMillis(),
                pendingIntent
        );
        Toast.makeText(this, "Alarm set", Toast.LENGTH_SHORT).show();
        finish();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
}