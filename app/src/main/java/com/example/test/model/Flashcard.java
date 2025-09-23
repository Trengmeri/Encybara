package com.example.test.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Flashcard implements Parcelable {
    private int id;
    private String word;
    private String definitions;
    private String partOfSpeech;
    private String phoneticText;
    private String phoneticAudio;
    private String addedDate;
    private String lastReviewed;
    private String examples;
    private boolean learnedStatus;
    private String vietNameseMeaning;

    public Flashcard(int id, String word, List<Integer> definitionIndices, int partOfSpeechIndex) {
        this.id = id;
        this.word = word;
        this.definitions = convertDefinitionsToString(definitionIndices);
        this.partOfSpeech = convertPartOfSpeech(partOfSpeechIndex);
    }
    // Chuyển danh sách định nghĩa thành chuỗi, ngăn cách bằng dấu phẩy
    private String convertDefinitionsToString(List<Integer> definitionIndices) {
        return definitionIndices != null ? TextUtils.join(", ", definitionIndices) : "No definitions";
    }
    // Hàm chuyển đổi chỉ mục thành loại từ
    private String convertPartOfSpeech(int index) {
        String[] partsOfSpeech = {"Noun", "Verb", "Adjective", "Adverb"}; // Cập nhật danh sách theo app của bạn
        return (index >= 0 && index < partsOfSpeech.length) ? partsOfSpeech[index] : "Unknown";
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDefinitions() {
        return definitions;
    }

    public void setDefinitions(String definitions) {
        this.definitions = definitions;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public String getPhoneticText() {
        return phoneticText;
    }
    public void setPhoneticText(String phoneticText) {
        this.phoneticText = phoneticText;
    }

    public String getPhoneticAudio() {
        return phoneticAudio;
    }

    public void setPhoneticAudio(String phoneticAudio) {
        this.phoneticAudio = phoneticAudio;
    }

    public String getAddedDate() {
        return addedDate;
    }
    public static String extractDateTimeVietnam(String addedDate) {
        // Chuyển chuỗi ISO 8601 thành ZonedDateTime ở UTC
        ZonedDateTime utcTime = ZonedDateTime.parse(addedDate);

        // Chuyển sang múi giờ Việt Nam (UTC+7)
        ZonedDateTime vietnamTime = utcTime.withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"));

        // Format lại thành "YYYY-MM-DD HH:mm:ss"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return vietnamTime.format(formatter);
    }



    public void setAddedDate(String addedDate) {
        this.addedDate = addedDate;
    }

    public String getLastReviewed() {
        return lastReviewed;
    }
    public String timeAgo(String lastReviewed) {
        if (lastReviewed == null || lastReviewed.isEmpty()) {
            return "Just now";
        }
        Instant lastTime;
        try {
            lastTime = Instant.parse(lastReviewed);
            // Tiếp tục xử lý thời gian
        } catch (Exception e) {
            Log.e("DEBUG", "Lỗi parse timestamp: " + e.getMessage());
            return "Lỗi thời gian";
        }
        Instant now = Instant.now();
        Duration duration = Duration.between(lastTime, now);
        long minutesAgo = duration.toMinutes();
        long hoursAgo = duration.toHours();
        long daysAgo = duration.toDays();

        if (daysAgo >= 1) {
            return extractDateTimeVietnam(lastReviewed); // Hiển thị ngày + giờ
        } else if (hoursAgo >= 1) {
            return hoursAgo + " hours ago";
        } else if (minutesAgo > 0) {
            return minutesAgo + " minutes ago";
        } else {
            return "Just now";
        }
    }

    public void setLastReviewed(String lastReviewed) {
        this.lastReviewed = lastReviewed;
    }

    public String getExamples() {
        return examples;
    }

    public void setExamples(String examples) {
        this.examples = examples;
    }

    public boolean isLearnedStatus() {
        return learnedStatus;
    }

    public void setLearnedStatus(boolean learnedStatus) {
        this.learnedStatus = learnedStatus;
    }

    public String getVietNameseMeaning() {
        return vietNameseMeaning;
    }

    public void setVietNameseMeaning(String vietNameseMeaning) {
        this.vietNameseMeaning = vietNameseMeaning;
    }

    // ** Parcelable Implementation **
    protected Flashcard(Parcel in) {
        id = in.readInt();
        word = in.readString();
        definitions = in.readString();
        partOfSpeech = in.readString();
        phoneticText = in.readString();
        phoneticAudio = in.readString();
        addedDate = in.readString();
        lastReviewed = in.readString();
        examples = in.readString();
        learnedStatus = in.readByte() != 0;
        vietNameseMeaning = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(word);
        dest.writeString(definitions);
        dest.writeString(partOfSpeech);
        dest.writeString(phoneticText);
        dest.writeString(phoneticAudio);
        dest.writeString(addedDate);
        dest.writeString(lastReviewed);
        dest.writeString(examples);
        dest.writeByte((byte) (learnedStatus ? 1 : 0));
        dest.writeString(vietNameseMeaning);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Flashcard> CREATOR = new Creator<Flashcard>() {
        @Override
        public Flashcard createFromParcel(Parcel in) {
            return new Flashcard(in);
        }

        @Override
        public Flashcard[] newArray(int size) {
            return new Flashcard[size];
        }
    };
}