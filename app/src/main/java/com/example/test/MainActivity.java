package com.example.test;

import com.example.test.Medicine;  // Полный импорт (если в том же пакете — просто Medicine)
import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.test.Medicine;  // Импорт Medicine
import com.google.gson.Gson;  // Импорт Gson
import com.google.gson.reflect.TypeToken;  // Импорт TypeToken
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;  // Для locale в toasts, если нужно

public class MainActivity extends AppCompatActivity implements MedicineAdapter.OnSetReminderClickListener {
    private List<Medicine> medicines = new ArrayList<>();
    private MedicineAdapter adapter;
    private static final String SHARED_PREFS = "pill_prefs";
    private static final String MEDICINES_KEY = "medicines_list";
    private RecyclerView recyclerView;
    private Gson gson = new Gson();

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Уведомления разрешены", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Уведомления нужны для напоминаний!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Запрос разрешения для Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicineAdapter(medicines, this);
        recyclerView.setAdapter(adapter);

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddMedicineActivity.class);
            startActivityForResult(intent, 1);
        });

        loadMedicines();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Medicine newMedicine = (Medicine) data.getSerializableExtra("medicine");
            if (newMedicine != null) {
                medicines.add(newMedicine);
                adapter.notifyDataSetChanged();
                saveMedicines();
            }
        }
    }

    @Override
    public void onSetReminderClick(Medicine medicine) {
        // Если время в прошлом — сдвинь на завтра
        long now = System.currentTimeMillis();
        long time = medicine.getReminderTime();
        if (time < now) {
            time += 24 * 60 * 60 * 1000L;  // +1 день
            medicine.setReminderTime(time);
            saveMedicines();
        }
        setAlarm(medicine);
        Toast.makeText(this, String.format(Locale.getDefault(), "Напоминание установлено на %d", time), Toast.LENGTH_SHORT).show();
    }

    private void setAlarm(Medicine medicine) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("medicine_name", medicine.getName());
        int requestCode = medicine.hashCode() % 10000;  // Уникальный
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long time = medicine.getReminderTime();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Проверка разрешения для exact alarms (Android 12+)
                if (alarmManager.canScheduleExactAlarms()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                    }
                } else {
                    // Fallback: Inexact alarm или запроси разрешение в настройках
                    Toast.makeText(this, "Разрешите точные уведомления в настройках", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);  // Inexact
                    }
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                }
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Нет разрешения на точные alarm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMedicines() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String json = prefs.getString(MEDICINES_KEY, null);
        if (json != null) {
            Type type = new TypeToken<List<Medicine>>(){}.getType();
            List<Medicine> loaded = gson.fromJson(json, type);
            medicines.clear();
            if (loaded != null) {
                medicines.addAll(loaded);  // Фикс типов: addAll для List<Medicine>
            }
        } else {
            medicines = new ArrayList<>();
        }
        adapter.notifyDataSetChanged();
    }

    private void saveMedicines() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = gson.toJson(medicines);  // toJson(List<Medicine>)
        editor.putString(MEDICINES_KEY, json);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMedicines();
    }
}