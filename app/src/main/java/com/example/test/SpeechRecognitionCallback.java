package com.example.test;

import android.os.Bundle;

import java.util.ArrayList;

public interface SpeechRecognitionCallback {
    void onReadyForSpeech();
    void onBeginningOfSpeech();
    void onRmsChanged(float rmsdB);
    void onBufferReceived(byte[] buffer);
    void onEndOfSpeech();
    void onError(int error);
    void onResults(ArrayList<String> matches);
    void onPartialResults(Bundle partialResults);
    void onEvent(int eventType, Bundle params);
}