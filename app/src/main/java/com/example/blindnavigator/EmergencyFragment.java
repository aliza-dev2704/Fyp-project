package com.example.blindnavigator;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class EmergencyFragment extends Fragment implements SensorEventListener {

    private EditText etNumber;
    private String emergencyNumber = "";

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float lastX, lastY, lastZ;
    private long lastTime;
    private static final int SHAKE_THRESHOLD = 800;

    private long lastShakeTime = 0;
    private static final int SHAKE_COOLDOWN = 2000; // 2 seconds cooldown

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emergency, container, false);

        etNumber = view.findViewById(R.id.etNumber);

        // Sensor setup
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        checkPermissions();

        return view;
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.SEND_SMS, Manifest.permission.CALL_PHONE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void sendEmergencyMessage() {
        if (emergencyNumber.isEmpty()) {
            emergencyNumber = etNumber.getText().toString().trim();
        }
        if (emergencyNumber.isEmpty()) {
            Toast.makeText(getActivity(), "No emergency number saved", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(emergencyNumber, null, "I am in an emergency, please help!", null, null);
            Toast.makeText(getActivity(), "Emergency message sent", Toast.LENGTH_SHORT).show();
        }
    }

    private void callEmergencyNumber() {
        if (emergencyNumber.isEmpty()) {
            emergencyNumber = etNumber.getText().toString().trim();
        }
        if (emergencyNumber.isEmpty()) {
            Toast.makeText(getActivity(), "No emergency number saved", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + emergencyNumber));
            startActivity(callIntent);
        }
    }

    private void handleEmergencyTriggered() {
        // Send SMS
        sendEmergencyMessage();

        // Make call after 1.5 seconds
        etNumber.postDelayed(this::callEmergencyNumber, 1500);

        // Return to voice commands after 5 seconds
        etNumber.postDelayed(() -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new VoiceFragment())
                        .commitAllowingStateLoss();
            }
        }, 5000);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        long curTime = System.currentTimeMillis();

        if ((curTime - lastTime) > 100) {
            long diffTime = curTime - lastTime;
            lastTime = curTime;

            float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;

            if (speed > SHAKE_THRESHOLD && (curTime - lastShakeTime) > SHAKE_COOLDOWN) {
                lastShakeTime = curTime;
                handleEmergencyTriggered();
            }

            lastX = x;
            lastY = y;
            lastZ = z;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(),
                            "Permissions are required for emergency feature", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
