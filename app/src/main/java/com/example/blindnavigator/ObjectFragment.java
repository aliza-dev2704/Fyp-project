package com.example.blindnavigator;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ObjectFragment extends Fragment {

    private static final int CAMERA_PERMISSION_REQUEST = 2001;

    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private TextToSpeech tts;
    private String lastSpoken = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_object, container, false);

        previewView = view.findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Init TTS
        tts = new TextToSpeech(requireContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

        // Check camera permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        }

        return view;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Image Analysis
                ImageAnalysis imageAnalysis =
                        new ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::processImageProxy);

                // Select back camera
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Bind lifecycle
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        getViewLifecycleOwner(), cameraSelector, preview, imageAnalysis);

            } catch (Exception e) {
                Log.e("CameraX", "Camera binding failed", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void processImageProxy(ImageProxy imageProxy) {
        try {
            if (imageProxy.getImage() == null) {
                imageProxy.close();
                return;
            }

            InputImage image =
                    InputImage.fromMediaImage(imageProxy.getImage(),
                            imageProxy.getImageInfo().getRotationDegrees());

            ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

            labeler.process(image)
                    .addOnSuccessListener(labels -> {
                        if (!labels.isEmpty()) {
                            ImageLabel label = labels.get(0);
                            String text = label.getText();

                            if (!text.equalsIgnoreCase(lastSpoken)) {
                                speak(text);
                                lastSpoken = text;
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("MLKit", "Labeling failed", e))
                    .addOnCompleteListener(task -> imageProxy.close());

        } catch (Exception e) {
            imageProxy.close();
        }
    }

    private void speak(String message) {
        if (tts != null) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Log.e("ObjectFragment", "Camera permission denied");
            }
        }
    }
}
