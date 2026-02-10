package com.attendance.model;

public class StudentRiskDTO {
    private int studentId;
    private String name;
    private double currentAttendance;
    private String trend; // "UP", "STABLE", "DOWN"
    private int riskScore;
    private String riskLevel; // "SAFE", "WATCH", "AT-RISK"
    private double predictedFinalAttendance;
    private String suggestedAction;
    private int[] weeklyPresence; // For chart: [LastWeek, 2WeeksAgo, 3WeeksAgo, 4WeeksAgo]

    public StudentRiskDTO() {
    }

    public StudentRiskDTO(int studentId, String name, double currentAttendance, String trend, int riskScore,
            String riskLevel, double predictedFinalAttendance, String suggestedAction) {
        this.studentId = studentId;
        this.name = name;
        this.currentAttendance = currentAttendance;
        this.trend = trend;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.predictedFinalAttendance = predictedFinalAttendance;
        this.suggestedAction = suggestedAction;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCurrentAttendance() {
        return currentAttendance;
    }

    public void setCurrentAttendance(double currentAttendance) {
        this.currentAttendance = currentAttendance;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public double getPredictedFinalAttendance() {
        return predictedFinalAttendance;
    }

    public void setPredictedFinalAttendance(double predictedFinalAttendance) {
        this.predictedFinalAttendance = predictedFinalAttendance;
    }

    public String getSuggestedAction() {
        return suggestedAction;
    }

    public void setSuggestedAction(String suggestedAction) {
        this.suggestedAction = suggestedAction;
    }

    public int[] getWeeklyPresence() {
        return weeklyPresence;
    }

    public void setWeeklyPresence(int[] weeklyPresence) {
        this.weeklyPresence = weeklyPresence;
    }
}
