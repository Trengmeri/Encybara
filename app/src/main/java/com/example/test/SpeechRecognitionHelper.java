package com.example.test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

public class SpeechRecognitionHelper {

    private SpeechRecognizer speechRecognizer;
    private Context context;
    private SpeechRecognitionCallback callback;

    public SpeechRecognitionHelper(Context context, SpeechRecognitionCallback callback) {
        this.context = context;
        this.callback = callback;
        initializeSpeechRecognizer();
    }

    private void initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                callback.onReadyForSpeech();
            }

            @Override
            public void onBeginningOfSpeech() {
                callback.onBeginningOfSpeech();
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                callback.onRmsChanged(rmsdB);
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                callback.onBufferReceived(buffer);
            }

            @Override
            public void onEndOfSpeech() {
                callback.onEndOfSpeech();
            }

            @Override
            public void onError(int error) {
                Log.e("SpeechRecognizerError", "Error code: " + error);
                callback.onError(error);
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                callback.onResults(matches);
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                callback.onPartialResults(partialResults);
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                callback.onEvent(eventType, params);
            }
        });
    }

    public void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizer.startListening(intent);
    }

    public void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
    }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }
}