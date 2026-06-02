package com.edutest.test_system.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "questions")
public class QuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "option_a")
    private String optionA;

    @Column(name = "option_b")
    private String optionB;

    @Column(name = "option_c")
    private String optionC;

    @Column(name = "option_d")
    private String optionD;

    @Column(name = "correct_answer")
    private String correctAnswer;

    @Column(name = "points")
    private int points;

    @ManyToOne
    @JoinColumn(name = "test_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "questions"}) // ЦЕ РОЗРІВАЄ ЦИКЛ!
    private TestEntity test;

    public QuestionEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }
    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }
    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }
    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public TestEntity getTest() { return test; }
    public void setTest(TestEntity test) { this.test = test; }
}