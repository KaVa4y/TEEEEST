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
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

    private final List<Medicine> medicines = new ArrayList<>();
    private MedicineAdapter adapter;
    private RecyclerView recyclerView;
    private final Gson gson = new Gson();
    private static final String PREFS = "pill_prefs";
    private static final String KEY = "medicines";

    private final ActivityResultLauncher<String> permLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {});

    private final ActivityResultLauncher<Intent> addLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Medicine m = (Medicine) result.getData().getSerializableExtra("medicine");
                    if (m != null) {
                        int pos = medicines.size();
                        medicines.add(m);
                        adapter.notifyItemInserted(pos);
                        recyclerView.smoothScrollToPosition(pos);
                        saveMedicines();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator());

        adapter = new MedicineAdapter(medicines, this, this);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.addButton).setOnClickListener(v ->
                addLauncher.launch(new Intent(this, AddMedicineActivity.class)));

        loadMedicines();  // Загрузка только один раз в onCreate
    }

    @Override
    public void onSetReminderClick(Medicine m) {
        long time = m.getReminderTime();
        if (time < System.currentTimeMillis()) {
            time += 24 * 60 * 60 * 1000L;
            m.setReminderTime(time);
            saveMedicines();
        }
        setAlarm(m);
        Toast.makeText(this, "Напоминание на " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(time)), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(Medicine m) {
        int pos = medicines.indexOf(m);
        if (pos != -1) {
            cancelAlarm(m);
            medicines.remove(pos);
            adapter.notifyItemRemoved(pos);
            saveMedicines();
        }
    }

    private int getRequestCode(Medicine m) {
        return (m.getName() + m.getDosage() + m.getReminderTime()).hashCode();
    }

    private void setAlarm(Medicine m) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("medicine_name", m.getName());
        int rc = getRequestCode(m);

        PendingIntent pi = PendingIntent.getBroadcast(this, rc, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        am.cancel(pi);  // Отмена старого

        long time = m.getReminderTime();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi);
                } else {
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi);
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, time, pi);
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Нет разрешения на точные напоминания", Toast.LENGTH_LONG).show();
        }
    }

    private void cancelAlarm(Medicine m) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        int rc = getRequestCode(m);
        PendingIntent pi = PendingIntent.getBroadcast(this, rc, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.cancel(pi);
    }

    private void loadMedicines() {
        medicines.clear();  // ← Ключевой фикс: очищаем перед загрузкой!
        SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
        String json = p.getString(KEY, null);
        if (json != null) {
            Type type = new TypeToken<List<Medicine>>(){}.getType();
            List<Medicine> list = gson.fromJson(json, type);
            if (list != null) medicines.addAll(list);
        }
        adapter.notifyDataSetChanged();
    }

    private void saveMedicines() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putString(KEY, gson.toJson(medicines))
                .apply();
    }

    // Убрал onResume — больше не нужен, дубли исчезли
}