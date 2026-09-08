package overload_api.controller.dto;

import java.util.List;

public class WorkoutHistoryResponse {

    private Long sessionId;

    private java.time.LocalDateTime startedAt;

    private Long exerciseId;

    private String exerciseName;
    
    private String note;

    private List<WorkoutHistorySetResponse> sets;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public java.time.LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(java.time.LocalDateTime startedAt) {
        this.startedAt = startedAt;
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
    
    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<WorkoutHistorySetResponse> getSets() {
        return sets;
    }

    public void setSets(List<WorkoutHistorySetResponse> sets) {
        this.sets = sets;
    }
}
