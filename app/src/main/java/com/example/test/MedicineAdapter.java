package com.example.test;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.ViewHolder> {
    private List<Medicine> medicines;
    private OnSetReminderClickListener reminderListener;
    private OnDeleteClickListener deleteListener;

    public interface OnSetReminderClickListener {
        void onSetReminderClick(Medicine medicine);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Medicine medicine);
    }

    public MedicineAdapter(List<Medicine> medicines, OnSetReminderClickListener reminderListener, OnDeleteClickListener deleteListener) {
        this.medicines = medicines;
        this.reminderListener = reminderListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine, parent, false);
        ViewCompat.setTransitionName(view, "card_transition_" + viewType);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicine medicine = medicines.get(position);
        holder.nameText.setText(medicine.getName());
        holder.dosageText.setText(medicine.getDosage());
        holder.setReminderButton.setOnClickListener(v -> reminderListener.onSetReminderClick(medicine));
        holder.deleteButton.setOnClickListener(v -> animateRemoval(holder, position, medicine));
    }

    private void animateRemoval(ViewHolder holder, int position, Medicine medicine) {
        holder.itemView.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        deleteListener.onDeleteClick(medicine);
                        super.onAnimationEnd(animation);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return medicines.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, dosageText;
        Button setReminderButton, deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            dosageText = itemView.findViewById(R.id.dosageText);
            setReminderButton = itemView.findViewById(R.id.setReminderButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}