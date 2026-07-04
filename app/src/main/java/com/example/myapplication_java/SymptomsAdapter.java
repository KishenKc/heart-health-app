package com.example.myapplication_java;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SymptomsAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> symptomsList;
    private List<Boolean> checkedItems;

    public SymptomsAdapter(@NonNull Context context, List<String> symptomsList) {
        super(context, 0, symptomsList);
        this.context = context;
        this.symptomsList = symptomsList;
        this.checkedItems = new ArrayList<>();
        for (int i = 0; i < symptomsList.size(); i++) {
            checkedItems.add(false);
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
        }

        CheckBox checkBox = (CheckBox) convertView;
        checkBox.setText(symptomsList.get(position));
        checkBox.setChecked(checkedItems.get(position));

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkedItems.set(position, isChecked);
        });

        return convertView;
    }

    public List<String> getSelectedSymptoms() {
        List<String> selected = new ArrayList<>();
        for (int i = 0; i < symptomsList.size(); i++) {
            if (checkedItems.get(i)) {
                selected.add(symptomsList.get(i));
            }
        }
        return selected;
    }

    public void setCheckedItems(List<Boolean> checkedItems) {
        this.checkedItems = checkedItems;
        notifyDataSetChanged();
    }
}