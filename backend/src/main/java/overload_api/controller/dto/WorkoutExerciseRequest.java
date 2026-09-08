package overload_api.controller.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class WorkoutExerciseRequest {

	@NotNull
	private Long exerciseId;

	@Valid
	private List<WorkoutSetRequest> sets;

    public Long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(Long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public List<WorkoutSetRequest> getSets() {
        return sets;
    }

    public void setSets(List<WorkoutSetRequest> sets) {
        this.sets = sets;
    }
}