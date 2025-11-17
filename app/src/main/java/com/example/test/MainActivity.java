package com.example.test;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements MedicineAdapter.OnSetReminderClickListener, MedicineAdapter.OnDeleteClickListener {
    private List<Medicine> medicines = new ArrayList<>();
    private MedicineAdapter adapter;
    private static final String SHARED_PREFS = "pill_prefs";
    private static final String MEDICINES_KEY = "medicines_list";
    private RecyclerView recyclerView;
    private Gson gson = new Gson();

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) Log.d("Main", "Notifications OK");
                else Log.w("Main", "Notifications denied");
            });

    private final ActivityResultLauncher<Intent> addMedicineLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Medicine newMedicine = (Medicine) result.getData().getSerializableExtra("medicine");
                    if (newMedicine != null) {
                        int position = medicines.size();
                        medicines.add(newMedicine);
                        adapter.notifyItemInserted(position);  // Встроенная анимация RecyclerView (замена LayoutTransition)
                        recyclerView.smoothScrollToPosition(position);
                        saveMedicines();
                        Log.d("Main", "Added: " + newMedicine.getName());
                    } else {
                        Log.e("Main", "Received null medicine");
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // Анимация появления списка (встроенная RecyclerView, без LayoutTransition)
        recyclerView.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator());

        adapter = new MedicineAdapter(medicines, this, this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> addMedicineLauncher.launch(new Intent(this, AddMedicineActivity.class)));

        loadMedicines();
    }

    @Override
    public void onSetReminderClick(Medicine medicine) {
        long now = System.currentTimeMillis();
        long time = medicine.getReminderTime();
        if (time < now) {
            time += 24 * 60 * 60 * 1000L;
            medicine.setReminderTime(time);
            saveMedicines();
        }
        setAlarm(medicine);
        Toast.makeText(this, "Напоминание на " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(time)), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(Medicine medicine) {
        int position = medicines.indexOf(medicine);
        if (position != -1) {
            cancelAlarm(medicine);
            medicines.remove(position);
            adapter.notifyItemRemoved(position);  // Встроенная анимация удаления
            saveMedicines();
        }
    }

    private void setAlarm(Medicine medicine) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("medicine_name", medicine.getName());
        int requestCode = medicine.hashCode() % 10000;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long time = medicine.getReminderTime();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                    }
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                }
            }
            Log.d("Main", "Alarm set for " + medicine.getName());
        } catch (SecurityException e) {
            Log.e("Main", "Alarm error: " + e.getMessage());
        }
    }

    private void cancelAlarm(Medicine medicine) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("medicine_name", medicine.getName());
        int requestCode = medicine.hashCode() % 10000;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
        Log.d("Main", "Alarm cancelled for " + medicine.getName());
    }

    private void loadMedicines() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String json = prefs.getString(MEDICINES_KEY, null);
        if (json != null && !json.isEmpty()) {
            Type type = new TypeToken<List<Medicine>>(){}.getType();
            List<Medicine> loaded = gson.fromJson(json, type);
            medicines.clear();
            if (loaded != null) medicines.addAll(loaded);
        }
        adapter.notifyDataSetChanged();
        Log.d("Main", "Loaded " + medicines.size() + " medicines");
    }

    private void saveMedicines() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = gson.toJson(medicines);
        editor.putString(MEDICINES_KEY, json);
        editor.apply();
        Log.d("Main", "Saved " + medicines.size() + " medicines");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMedicines();
    }
}