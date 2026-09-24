package overload_api.controller;

import java.util.ArrayList;
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

import overload_api.controller.dto.WorkoutExerciseRequest;
import overload_api.controller.dto.WorkoutHistoryResponse;
import overload_api.controller.dto.WorkoutHistorySetResponse;
import overload_api.controller.dto.WorkoutRequest;
import overload_api.controller.dto.WorkoutSetRequest;
import overload_api.model.WorkoutSet;
import overload_api.service.WorkoutService;

/**
 * ワークアウトに関するAPIを提供するコントローラー。
 */
@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    /**
     * WorkoutControllerを生成する。
     *
     * @param workoutService ワークアウト情報を扱うサービス
     */
    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    /**
     * ワークアウトを登録する。
     *
     * @param request 登録するワークアウト情報
     * @return 登録したワークアウトセット一覧
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<WorkoutSet> create(@Valid @RequestBody WorkoutRequest request) {
        return workoutService.create(toServiceRequest(request));
    }

    /**
     * 指定されたユーザーのワークアウト履歴を取得する。
     *
     * @param userId ユーザーID
     * @return ワークアウト履歴一覧
     */
    @GetMapping("/history")
    public List<WorkoutHistoryResponse> findHistory(
            @RequestParam("user_id") Long userId) {

        List<overload_api.service.dto.WorkoutHistoryResponse> serviceResponses =
                workoutService.findHistoryByUserId(userId);

        List<WorkoutHistoryResponse> responses = new ArrayList<>();

        for (overload_api.service.dto.WorkoutHistoryResponse serviceResponse
                : serviceResponses) {

            WorkoutHistoryResponse response = new WorkoutHistoryResponse();
            response.setSessionId(serviceResponse.getSessionId());
            response.setStartedAt(serviceResponse.getStartedAt());
            response.setExerciseId(serviceResponse.getExerciseId());
            response.setExerciseName(serviceResponse.getExerciseName());
            response.setNote(serviceResponse.getNote());

            List<WorkoutHistorySetResponse> sets = new ArrayList<>();

            if (serviceResponse.getSets() != null) {
                for (overload_api.service.dto.WorkoutHistorySetResponse serviceSet
                        : serviceResponse.getSets()) {

                    WorkoutHistorySetResponse set =
                            new WorkoutHistorySetResponse();
                    set.setSetNumber(serviceSet.getSetNumber());
                    set.setWeightKg(serviceSet.getWeightKg());
                    set.setReps(serviceSet.getReps());
                    sets.add(set);
                }
            }

            response.setSets(sets);
            responses.add(response);
        }

        return responses;
    }

    /**
     * Controller用のワークアウト登録リクエストをService用DTOに変換する。
     *
     * @param request Controller用のワークアウト登録リクエスト
     * @return Service用のワークアウト登録リクエスト
     */
    private overload_api.service.dto.WorkoutRequest toServiceRequest(
            WorkoutRequest request) {

        overload_api.service.dto.WorkoutRequest serviceRequest =
                new overload_api.service.dto.WorkoutRequest();

        serviceRequest.setUserId(request.getUserId());

        List<overload_api.service.dto.WorkoutExerciseRequest>
                serviceExercises = new ArrayList<>();

        if (request.getExercises() != null) {
            for (WorkoutExerciseRequest exerciseRequest
                    : request.getExercises()) {

                overload_api.service.dto.WorkoutExerciseRequest
                        serviceExercise =
                        new overload_api.service.dto.WorkoutExerciseRequest();

                serviceExercise.setExerciseId(
                        exerciseRequest.getExerciseId());

                List<overload_api.service.dto.WorkoutSetRequest>
                        serviceSets = new ArrayList<>();

                if (exerciseRequest.getSets() != null) {
                    for (WorkoutSetRequest setRequest
                            : exerciseRequest.getSets()) {

                        overload_api.service.dto.WorkoutSetRequest serviceSet =
                                new overload_api.service.dto.WorkoutSetRequest();

                        serviceSet.setSetNumber(setRequest.getSetNumber());
                        serviceSet.setWeightKg(setRequest.getWeightKg());
                        serviceSet.setReps(setRequest.getReps());
                        serviceSet.setNote(setRequest.getNote());

                        serviceSets.add(serviceSet);
                    }
                }

                serviceExercise.setSets(serviceSets);
                serviceExercises.add(serviceExercise);
            }
        }

        serviceRequest.setExercises(serviceExercises);
        return serviceRequest;
    }
}