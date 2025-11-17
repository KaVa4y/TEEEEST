package com.example.test;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MedicineAdapter.OnSetReminderClickListener {
    private List<Medicine> medicines = new ArrayList<>();
    private MedicineAdapter adapter;
    private static final String SHARED_PREFS = "pill_prefs";
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        setAlarm(medicine);
    }

    private void setAlarm(Medicine medicine) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("medicine_name", medicine.getName());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) medicine.getReminderTime() % 10000, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long time = medicine.getReminderTime();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        }
    }

    private void loadMedicines() {
        // Заглушка: в реальности используй SharedPreferences с Gson для сериализации списка
        // Для примера добавим тестовое лекарство
        if (medicines.isEmpty()) {
            long testTime = System.currentTimeMillis() + 3600000L; // Через час
            medicines.add(new Medicine("Аспирин", "1 таблетка", testTime));
            adapter.notifyDataSetChanged();
        }
    }

    private void saveMedicines() {
        // Заглушка: сохрани в SharedPreferences (добавь Gson для JSON)
    }
}