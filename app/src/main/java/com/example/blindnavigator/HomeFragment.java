package com.example.blindnavigator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Simple TextView for Home Screen
        TextView textView = new TextView(getContext());
        textView.setText("Welcome to VisionMate.\nSay Navigation, Object Detection, or Emergency.");
        textView.setTextSize(18);
        textView.setPadding(40, 200, 40, 40);

        return textView;
    }
}
