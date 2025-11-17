package com.example.test;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.ViewHolder> {
    private List<Medicine> medicines;
    private OnSetReminderClickListener listener;

    public interface OnSetReminderClickListener {
        void onSetReminderClick(Medicine medicine);
    }

    public MedicineAdapter(List<Medicine> medicines, OnSetReminderClickListener listener) {
        this.medicines = medicines;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicine medicine = medicines.get(position);
        holder.nameText.setText(medicine.getName());
        holder.dosageText.setText(medicine.getDosage());
        holder.setReminderButton.setOnClickListener(v -> listener.onSetReminderClick(medicine));
    }

    @Override
    public int getItemCount() {
        return medicines.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, dosageText;
        Button setReminderButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            dosageText = itemView.findViewById(R.id.dosageText);
            setReminderButton = itemView.findViewById(R.id.setReminderButton);
        }
    }
}