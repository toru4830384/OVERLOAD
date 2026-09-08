package overload_api.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import overload_api.controller.dto.WorkoutHistoryResponse;
import overload_api.controller.dto.WorkoutRequest;
import overload_api.model.WorkoutSet;
import overload_api.service.WorkoutService;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<WorkoutSet> create(@Valid @RequestBody WorkoutRequest request) {
        try {
            return workoutService.create(request);
        } catch (org.springframework.jdbc.UncategorizedSQLException e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "入力値が不正です",
                    e
            );
        }
    }

    @GetMapping("/history")
    public List<WorkoutHistoryResponse> findHistory(
            @RequestParam("user_id") Long userId) {
        return workoutService.findHistoryByUserId(userId);
    }
}