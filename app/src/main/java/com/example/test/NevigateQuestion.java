package com.example.test;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test.api.ApiCallback;
import com.example.test.api.LessonManager;
import com.example.test.api.QuestionManager;
import com.example.test.model.Lesson;
import com.example.test.model.Question;
import com.example.test.ui.question_data.GrammarPick1QuestionActivity;
import com.example.test.ui.question_data.GrammarPickManyActivity;
import com.example.test.ui.question_data.ListeningChoiceActivity;
import com.example.test.ui.question_data.ListeningQuestionActivity;
import com.example.test.ui.question_data.ReadingTextActivity;
import com.example.test.ui.question_data.RecordQuestionActivity;
import com.example.test.ui.question_data.WrittingActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NevigateQuestion extends AppCompatActivity {
    private String skill;
    private int lessonID,courseID,enrollmentId;
    private int currentQuestionIndex; // Vị trí câu hỏi hiện tại
    private List<Integer> questionIds = new ArrayList<>();
    private List<Question> questions = new ArrayList<>();
    private QuestionManager quesManager;
    private LessonManager lessonManager = new LessonManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            courseID = intent.getIntExtra("courseId",1);
            lessonID = intent.getIntExtra("lessonId",1);
            enrollmentId = intent.getIntExtra("enrollmentId",1);
            questionIds = (List<Integer>) intent.getSerializableExtra("questionIds");
        }

        lessonManager.fetchLessonById(lessonID, new ApiCallback<Lesson>() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onSuccess(Lesson lesson) {
                if(lesson != null){
                    skill = lesson.getSkillType();
                    fetchQuestionsFromAPI(skill);
                }
            }


            @Override
            public void onFailure(String errorMessage) {

            }
        });

        quesManager = new QuestionManager(this);
        currentQuestionIndex = 0; // Bắt đầu từ câu hỏi đầu tiên

    }

    private void fetchQuestionsFromAPI(String skill) {
        if(questionIds != null){
            for (Integer id : questionIds) {
                quesManager.fetchQuestionContentFromApi(id, new ApiCallback<Question>() {
                    @Override
                    public void onSuccess(Question question) {
                        if (question != null) {
                            questions.add(question);
                            // Khi đã lấy đủ tất cả câu hỏi, chuyển sang Activity tiếp theo
                            if (questions.size() == questionIds.size()) {
                                navigateToActivity(questions.get(currentQuestionIndex) , skill);
                            }
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        finish(); // Nếu có lỗi, đóng Activity
                    }

                    @Override
                    public void onSuccess() {}
                });
            }
        }
    }

    private void navigateToActivity(Question question, String skill) {
        Intent intent = null;

        if ("READING".equals(skill)) {
            String quesType = question.getQuesType().trim().toUpperCase();
            if ("MULTIPLE".equals(quesType)) {
                intent = new Intent(this, GrammarPickManyActivity.class);
            } else if ("TEXT".equals(quesType)) {
                intent = new Intent(this, ReadingTextActivity.class);
            } else {
                intent = new Intent(this, GrammarPick1QuestionActivity.class);
            }
    } else if ("LISTENING".equals(skill)) {
            String quesType = question.getQuesType().trim().toUpperCase();
            intent = new Intent(this, "CHOICE".equals(quesType)
                    ? ListeningChoiceActivity.class
                    : ListeningQuestionActivity.class);
        } else if ("SPEAKING".equals(skill)) {
            intent = new Intent(this, RecordQuestionActivity.class);
        }else if ("WRITING".equals(skill)) {
            intent = new Intent(this, WrittingActivity.class);
        }

        if (intent != null) {
            intent.putExtra("currentQuestionIndex", currentQuestionIndex);
            intent.putExtra("courseID",courseID);
            intent.putExtra("lessonID",lessonID);
            intent.putExtra("enrollmentId", enrollmentId);
            Log.e("nevigate","Lesson ID: "+ lessonID + "courseID: "+ courseID);
            if (questions == null || questions.isEmpty()) {
                Log.e("NevigateQuestion", "Danh sách câu hỏi bị null hoặc rỗng trước khi gửi!");
            } else {
                Log.d("NevigateQuestion", "Danh sách câu hỏi trước khi gửi: " + questions);
            }
            Collections.sort(questions, Comparator.comparingInt(Question::getId));
            intent.putExtra("questions", (Serializable) questions);

            startActivity(intent);
        }
        finish();
    }
}
