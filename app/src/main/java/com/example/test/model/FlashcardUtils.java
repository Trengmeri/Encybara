package com.example.test.model;
import android.util.Log;

import com.google.gson.Gson;

import java.util.*;
public class FlashcardUtils {

    public static List<WordData> mergeWordData(List<WordData> wordDataList) {
        WordData mergedWordData = new WordData();
        mergedWordData.setMeanings(new ArrayList<>());

        // Dùng HashMap để gộp các meanings có cùng Part of Speech
        Map<String, Meaning> meaningMap = new LinkedHashMap<>();

        // Dùng Set để loại bỏ phonetics trùng
        Set<String> phoneticSet = new LinkedHashSet<>();

        for (WordData wordData : wordDataList) {
            // Merge meanings
            if (wordData.getMeanings() != null) {
                for (Meaning meaning : wordData.getMeanings()) {
                    if (meaning.getPartOfSpeech() != null && meaning.getDefinitions() != null) {
                        if (meaningMap.containsKey(meaning.getPartOfSpeech())) {
                            meaningMap.get(meaning.getPartOfSpeech()).getDefinitions().addAll(meaning.getDefinitions());
                        } else {
                            meaningMap.put(meaning.getPartOfSpeech(), meaning);
                        }
                    }
                }
            }

            // Merge phonetics (loại bỏ trùng)
            if (wordData.getPhonetics() != null) {
                for (Phonetic phonetic : wordData.getPhonetics()) {
                    phoneticSet.add(phonetic.getText()); // Chỉ thêm nếu chưa tồn tại
                }
            }
        }

        // Chuyển danh sách meanings về dạng list
        mergedWordData.getMeanings().addAll(meaningMap.values());

        // Chuyển phonetic set thành danh sách
        List<Phonetic> uniquePhonetics = new ArrayList<>();
        for (String phoneticText : phoneticSet) {
            Phonetic phonetic = new Phonetic();
            phonetic.setText(phoneticText);
            uniquePhonetics.add(phonetic);
        }
        mergedWordData.setPhonetics(uniquePhonetics);

        Log.d("DEBUG", "Meanings sau merge: " + new Gson().toJson(mergedWordData.getMeanings()));
        Log.d("DEBUG", "Phonetics sau merge: " + new Gson().toJson(mergedWordData.getPhonetics()));

        return Collections.singletonList(mergedWordData);
    }




}
