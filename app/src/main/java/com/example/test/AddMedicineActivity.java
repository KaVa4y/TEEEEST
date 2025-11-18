package com.example.test;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;
import java.util.Locale;

public class AddMedicineActivity extends AppCompatActivity {
    private final Calendar cal = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        TextInputEditText name = findViewById(R.id.nameEdit);
        TextInputEditText dosage = findViewById(R.id.dosageEdit);
        MaterialButton timeBtn = findViewById(R.id.timeButton);
        MaterialButton saveBtn = findViewById(R.id.saveButton);

        updateTimeButton(timeBtn);

        timeBtn.setOnClickListener(v -> new TimePickerDialog(this, (view, h, m) -> {
            cal.set(Calendar.HOUR_OF_DAY, h);
            cal.set(Calendar.MINUTE, m);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            // Устанавливаем на сегодня
            Calendar now = Calendar.getInstance();
            cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
            cal.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR));
            updateTimeButton(timeBtn);
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show());

        saveBtn.setOnClickListener(v -> {
            String n = name.getText().toString().trim();
            String d = dosage.getText().toString().trim();
            if (n.isEmpty() || d.isEmpty()) {
                Toast.makeText(this, "Заполните поля", Toast.LENGTH_SHORT).show();
                return;
            }
            Medicine m = new Medicine(n, d, cal.getTimeInMillis());
            setResult(RESULT_OK, new Intent().putExtra("medicine", m));
            finish();
        });
    }

    private void updateTimeButton(MaterialButton btn) {
        btn.setText(String.format(Locale.getDefault(), "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
    }
}