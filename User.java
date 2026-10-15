package com.focusflow.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Login Details
    private String name;
    @Column(unique = true)
    private String email;
    private String password;
    private String role = "USER";

    // Profile Details
    private Integer age;
    private String college;
    private String careerGoal;
    private String interests;

    // Productivity Details
    private Integer dailyTarget = 120; // minutes
    private Integer focusScore = 0;
    private Integer productivityScore = 0;
    private Integer wellnessScore = 0;
    private Integer streak = 0;
    private Integer xp = 0;
    private Integer level = 1;

    public User() {
    }

    // -------- Getters and Setters --------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getCareerGoal() {
        return careerGoal;
    }

    public void setCareerGoal(String careerGoal) {
        this.careerGoal = careerGoal;
    }

    public String getInterests() {
        return interests;
    }

    public void setInterests(String interests) {
        this.interests = interests;
    }

    public Integer getDailyTarget() {
        return dailyTarget;
    }

    public void setDailyTarget(Integer dailyTarget) {
        this.dailyTarget = dailyTarget;
    }

    public Integer getFocusScore() {
        return focusScore;
    }

    public void setFocusScore(Integer focusScore) {
        this.focusScore = focusScore;
    }

    public Integer getProductivityScore() {
        return productivityScore;
    }

    public void setProductivityScore(Integer productivityScore) {
        this.productivityScore = productivityScore;
    }

    public Integer getWellnessScore() {
        return wellnessScore;
    }

    public void setWellnessScore(Integer wellnessScore) {
        this.wellnessScore = wellnessScore;
    }

    public Integer getStreak() {
        return streak;
    }

    public void setStreak(Integer streak) {
        this.streak = streak;
    }

    public Integer getXp() {
        return xp;
    }

    public void setXp(Integer xp) {
        this.xp = xp;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }
}