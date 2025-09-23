package com.example.test.model;

import java.io.Serializable;
import java.util.List;

public class Question implements Serializable {
    private int id;
    private String quesContent;
    private String keyword;
    private String quesType;
    private String skillType;
    private int point;
    private List<Answer> answers;
    private List<Lesson> lessonQuestions;
    private List<QuestionChoice> questionChoices;
    private Object learningMaterial;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuesContent() {
        return quesContent;
    }

    public void setQuesContent(String quesContent) {
        this.quesContent = quesContent;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getQuesType() {
        return quesType;
    }

    public void setQuesType(String quesType) {
        this.quesType = quesType;
    }

    public String getSkillType() {
        return skillType;
    }

    public void setSkillType(String skillType) {
        this.skillType = skillType;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public List<Lesson> getLessonQuestions() {
        return lessonQuestions;
    }

    public void setLessonQuestions(List<Lesson> lessonQuestions) {
        this.lessonQuestions = lessonQuestions;
    }

    public List<QuestionChoice> getQuestionChoices() {
        return questionChoices;
    }

    public void setQuestionChoices(List<QuestionChoice> questionChoices) {
        this.questionChoices = questionChoices;
    }

    public Object getLearningMaterial() {
        return learningMaterial;
    }

    public void setLearningMaterial(Object learningMaterial) {
        this.learningMaterial = learningMaterial;
    }
    @Override
    public String toString() {
        return "Question{id=" + id +
                ", type='" + quesType + '\'' +
                ", content='" + quesContent + '\'' +
                '}';
    }
}