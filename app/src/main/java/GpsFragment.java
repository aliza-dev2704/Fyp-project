package com.example.blindnavigator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class GpsFragment extends Fragment {

    private static final int LOCATION_PERMISSION_CODE = 3001;
    private FusedLocationProviderClient locationClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_gps, container, false);

        // GPS client init
        locationClient =
                LocationServices.getFusedLocationProviderClient(requireContext());

        // Screen tap → navigation start
        view.setOnClickListener(v -> openGoogleMaps());

        return view;
    }

    private void openGoogleMaps() {

        // Permission check
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE
            );
            return;
        }

        // Get current location
        locationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {

                        double lat = location.getLatitude();
                        double lon = location.getLongitude();

                        // Destination = hospital (you can change later)
                        String uri =
                                "https://www.google.com/maps/dir/?api=1" +
                                "&origin=" + lat + "," + lon +
                                "&destination=hospital" +
                                "&travelmode=walking";

                        Intent intent =
                                new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        intent.setPackage("com.google.android.apps.maps");
                        startActivity(intent);
                    }
                });
    }
}
