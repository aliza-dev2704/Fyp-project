package com.example.blindnavigator;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Locale;

public class GpsFragment extends Fragment {

    private TextView tvInstructions;
    private FusedLocationProviderClient fusedLocationClient;
    private GestureDetector gestureDetector;

    private static final int REQUEST_CODE_VOICE = 1001;
    private static final int REQUEST_LOCATION_PERMISSION = 2001;
    private static final int REQUEST_CODE_MAPS = 3001;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gps, container, false);

        tvInstructions = view.findViewById(R.id.tvInstructions);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Gesture: double tap → voice input
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                startVoiceInput();
                return true;
            }
        });

        // Attach touch listener
        view.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        return view;
    }

    // Start voice recognition
    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say destination address");

        startActivityForResult(intent, REQUEST_CODE_VOICE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_VOICE && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String destination = result.get(0);
                navigateToDestination(destination);
            }
        }

        if (requestCode == REQUEST_CODE_MAPS) {
            // ✅ After returning from Google Maps → go back to VoiceFragment
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new VoiceFragment());
            }
        }
    }

    // Navigate safely with permission check
    private void navigateToDestination(String destination) {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                String uri = "http://maps.google.com/maps?saddr=" +
                        location.getLatitude() + "," + location.getLongitude() +
                        "&daddr=" + destination;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");

                // Open Google Maps and expect a result when returning
                startActivityForResult(intent, REQUEST_CODE_MAPS);

            } else {
                tvInstructions.setText("Unable to get current location. Make sure GPS is ON.");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            tvInstructions.setText("Permission granted. Double-tap again to start navigation.");
        }
    }
}
