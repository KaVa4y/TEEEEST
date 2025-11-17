package com.example.test;  // Твой пакет

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.test.Medicine;  // Импорт Medicine
import java.util.Calendar;
import java.util.Locale;

public class AddMedicineActivity extends AppCompatActivity {
    private EditText nameEdit, dosageEdit;
    private Button timeButton, saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        nameEdit = findViewById(R.id.nameEdit);
        dosageEdit = findViewById(R.id.dosageEdit);
        timeButton = findViewById(R.id.timeButton);
        saveButton = findViewById(R.id.saveButton);

        timeButton.setOnClickListener(v -> showTimePicker());
        saveButton.setOnClickListener(v -> saveMedicine());

        Calendar calendar = Calendar.getInstance();  // Локальная переменная (фикс warning)
        updateTimeText(calendar);  // Инициализация текущим временем
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();  // Локальная
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            // Установить на сегодня
            long now = System.currentTimeMillis();
            long todayMidnight = now - (now % (24 * 60 * 60 * 1000L));
            long selectedTime = (hourOfDay * 60 * 60 * 1000L) + (minute * 60 * 1000L);
            calendar.setTimeInMillis(todayMidnight + selectedTime);
            updateTimeText(calendar);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void updateTimeText(Calendar calendar) {
        String timeText = String.format(Locale.getDefault(), "%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));  // Locale fix
        timeButton.setText(timeText);
    }

    private void saveMedicine() {
        String name = nameEdit.getText().toString().trim();
        String dosage = dosageEdit.getText().toString().trim();
        if (name.isEmpty() || dosage.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar calendar = (Calendar) timeButton.getTag();  // Восстанови из tag, если нужно; иначе пересоздай
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        long reminderTime = calendar.getTimeInMillis();
        Medicine medicine = new Medicine(name, dosage, reminderTime);  // Создание Medicine

        Intent resultIntent = new Intent();
        resultIntent.putExtra("medicine", medicine);  // putExtra с Serializable
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}