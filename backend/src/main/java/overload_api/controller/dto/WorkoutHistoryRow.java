package overload_api.controller.dto;

import java.math.BigDecimal;

public class WorkoutHistoryRow {

    private Long sessionId;

    private java.time.LocalDateTime startedAt;

    private Long exerciseId;

    private String exerciseName;

    private Integer setNumber;

    private BigDecimal weightKg;

    private Integer reps;
    
    private String note;

    public Long getSessionId() {
        return sessionId;
    }

    public java.time.LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(java.time.LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(Long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public Integer getSetNumber() {
        return setNumber;
    }

    public void setSetNumber(Integer setNumber) {
        this.setNumber = setNumber;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public Integer getReps() {
        return reps;
    }

    public void setReps(Integer reps) {
        this.reps = reps;
    }
    
    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
