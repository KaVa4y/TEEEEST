package com.example.test;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;
import java.util.Locale;

public class AddMedicineActivity extends AppCompatActivity {
    private TextInputEditText nameEdit, dosageEdit;
    private MaterialButton timeButton, saveButton;
    private Calendar calendar = Calendar.getInstance();

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

        updateTimeText();
    }

    private void showTimePicker() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long now = System.currentTimeMillis();
            long todayMidnight = now - (now % (24 * 60 * 60 * 1000L));
            long selectedTime = (hourOfDay * 60 * 60 * 1000L) + (minute * 60 * 1000L);
            calendar.setTimeInMillis(todayMidnight + selectedTime);
            updateTimeText();
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void updateTimeText() {
        String timeText = String.format(Locale.getDefault(), "%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        timeButton.setText(timeText);
    }

    private void saveMedicine() {
        String name = nameEdit.getText().toString().trim();
        String dosage = dosageEdit.getText().toString().trim();
        if (name.isEmpty() || dosage.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        long reminderTime = calendar.getTimeInMillis();
        Medicine medicine = new Medicine(name, dosage, reminderTime);
        Log.d("AddMedicine", "Created: " + name);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("medicine", medicine);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}