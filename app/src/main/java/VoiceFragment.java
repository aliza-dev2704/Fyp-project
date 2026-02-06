package com.example.blindnavigator;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Locale;

public class VoiceFragment extends Fragment {

    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private TextView statusText;
    private ProgressBar listeningIndicator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_voice, container, false);

        statusText = view.findViewById(R.id.statusText);
        listeningIndicator = view.findViewById(R.id.listeningIndicator);

        // Initialize TTS
        textToSpeech = new TextToSpeech(requireContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.getDefault());
                speak("Voice commands are ready. Say GPS, Object, or Emergency.");
                statusText.setText("Voice commands are ready.");
            }
        });

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext());
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {
                statusText.setText("Listening...");
                listeningIndicator.setVisibility(View.VISIBLE);
            }

            @Override public void onBeginningOfSpeech() {
                statusText.setText("Speak now...");
            }

            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {
                listeningIndicator.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError(int error) {
                statusText.setText("Error, retrying...");
                restartListening();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String command = matches.get(0).toLowerCase().trim();
                    statusText.setText("You said: " + command);
                    handleCommand(command);
                }
                restartListening();
            }

            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });

        // Start listening immediately
        startListening();

        return view;
    }

    private void startListening() {
        if (speechRecognizer != null) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            speechRecognizer.startListening(intent);
        }
    }

    private void restartListening() {
        statusText.postDelayed(this::startListening, 500);
    }

    private void stopListening() {
        if (speechRecognizer != null) {
            try {
                speechRecognizer.stopListening();
                speechRecognizer.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleCommand(String command) {
        MainActivity mainActivity = (MainActivity) requireActivity();

        if (command.contains("gps") || command.contains("navigation") || command.contains("map")) {
            speak("Opening GPS page.");
            stopListening(); // stop before switching
            mainActivity.loadFragment(new GpsFragment());

        } else if (command.contains("object") || command.contains("detect") || command.contains("camera")) {
            speak("Opening Object Detection page.");
            stopListening();
            mainActivity.loadFragment(new ObjectFragment());

        } else if (command.contains("emergency") || command.contains("help") || command.contains("sos")) {
            speak("Opening Emergency page.");
            stopListening();
            mainActivity.loadFragment(new EmergencyFragment());

        } else if (command.contains("voice")) {
            speak("You are already in the Voice Command section.");

        } else {
            speak("Sorry, I did not understand " + command);
            Toast.makeText(requireContext(), "Command not recognized: " + command, Toast.LENGTH_SHORT).show();
        }
    }

    private void speak(String message) {
        if (textToSpeech != null) {
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
