package com.edutest.test_system.models;

import jakarta.persistence.*;

@Entity
@Table(name = "results")
public class ResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private UserEntity student; 

    @ManyToOne
    @JoinColumn(name = "test_id")
    private TestEntity test;

   
    @Column(name = "student_first_name")
    private String studentFirstName;

    @Column(name = "student_last_name")
    private String studentLastName;

    private double percentage;

    public ResultEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UserEntity getStudent() { return student; } 
    public void setStudent(UserEntity student) { this.student = student; }
    public TestEntity getTest() { return test; }
    public void setTest(TestEntity test) { this.test = test; }
    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public String getStudentFirstName() { return studentFirstName; }
    public void setStudentFirstName(String studentFirstName) { this.studentFirstName = studentFirstName; }
    public String getStudentLastName() { return studentLastName; }
    public void setStudentLastName(String studentLastName) { this.studentLastName = studentLastName; }
}