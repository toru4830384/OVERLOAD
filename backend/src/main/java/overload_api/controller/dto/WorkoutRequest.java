package overload_api.controller.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

public class WorkoutRequest {

	@NotNull
	private Long userId;
	
	@NotEmpty(message = "種目を1つ以上指定してください")
	@Valid
	private List<WorkoutExerciseRequest> exercises;
    
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<WorkoutExerciseRequest> getExercises() {
        return exercises;
    }

    public void setExercises(List<WorkoutExerciseRequest> exercises) {
        this.exercises = exercises;
    }
}