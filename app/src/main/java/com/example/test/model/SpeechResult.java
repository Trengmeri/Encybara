package com.example.test.model;

public class SpeechResult {
        private String transcript;
        private double confidence;

        public SpeechResult(String transcript, double confidence) {
            this.transcript = transcript;
            this.confidence = confidence;
        }

        public String getTranscript() {
            return transcript;
        }

        public double getConfidence() {
            return confidence;
        }

        @Override
        public String toString() {
            return "Transcript: " + transcript + ", Confidence: " + confidence;
        }

}
