package com.example.test.model;

import java.util.List;

public class Meaning {
    private String partOfSpeech;
    private List<Definition> definitions;

    public Meaning(String key, List<Definition> value) {
    }

    // Getters and Setters
    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public List<Definition> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(List<Definition> definitions) {
        this.definitions = definitions;
    }
}