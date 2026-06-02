package com.edutest.test_system.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "tests")
public class TestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private Integer timeLimit; 

    @Column(name = "test_code", nullable = false, unique = true)
    private String testCode;

    private String authorEmail;
    private String subject;
    private String grade;
    private String category;
    private String targetAudience;

    private boolean hideAnswers; 
    private boolean published = false; 

   
    @Column(name = "status")
    private String status = "ACTIVE";

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, fetch = FetchType.EAGER) 
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "questions"})
    private List<QuestionEntity> questions;

    public TestEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; } 
    public Integer getTimeLimit() { return timeLimit; }
    public void setTimeLimit(Integer timeLimit) { this.timeLimit = timeLimit; }
    public String getTestCode() { return testCode; }
    public void setTestCode(String testCode) { this.testCode = testCode; }
    public String getAuthorEmail() { return authorEmail; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }
    public boolean isHideAnswers() { return hideAnswers; }
    public void setHideAnswers(boolean hideAnswers) { this.hideAnswers = hideAnswers; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<QuestionEntity> getQuestions() { return this.questions; }
    public void setQuestions(List<QuestionEntity> questions) { this.questions = questions; }
}