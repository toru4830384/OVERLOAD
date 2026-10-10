package overload_api.controller.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import overload_api.time.WorkoutTime;
import overload_api.controller.dto.WorkoutSessionResponse;
import overload_api.model.WorkoutSession;
import overload_api.controller.dto.WorkoutRequest;
import overload_api.controller.dto.WorkoutExerciseRequest;
import overload_api.controller.dto.WorkoutSetRequest;
import overload_api.controller.dto.WorkoutSetResponse;
import overload_api.controller.dto.WorkoutHistoryResponse;
import overload_api.controller.dto.WorkoutHistorySetResponse;
import overload_api.model.WorkoutSet;

/**
 * ワークアウトのAPI DTOとService DTO・永続化モデルを変換する。
 */
public final class WorkoutApiMapper {

    private WorkoutApiMapper() {
    }

    /**
     * Controller用のワークアウト登録リクエストをService用DTOに変換する。
     *
     * @param request Controller用のワークアウト登録リクエスト
     * @return Service用のワークアウト登録リクエスト
     */
    public static overload_api.service.dto.WorkoutRequest toServiceRequest(
            WorkoutRequest request) {

        Objects.requireNonNull(request, "Workout request is required");
        overload_api.service.dto.WorkoutRequest serviceRequest =
                new overload_api.service.dto.WorkoutRequest();

        serviceRequest.setUserId(request.getUserId());
        serviceRequest.setStartedAt(request.getStartedAt());
        serviceRequest.setFinishedAt(request.getFinishedAt());
        serviceRequest.setBodyWeightKg(request.getBodyWeightKg());
        serviceRequest.setClientRequestId(request.getClientRequestId());

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
                serviceExercise.setNote(
                        exerciseRequest.getNote());

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

    /**
     * Persistence ModelのWorkoutSetをController用レスポンスDTOに変換する。
     *
     * @param workoutSet ワークアウトセット情報
     * @return ワークアウトセットレスポンス
     */
    public static WorkoutSetResponse toResponse(WorkoutSet workoutSet) {

        Objects.requireNonNull(workoutSet, "Workout set is required");
        WorkoutSetResponse response = new WorkoutSetResponse();

        response.setId(workoutSet.getId());
        response.setSessionId(workoutSet.getSessionId());
        response.setExerciseId(workoutSet.getExerciseId());
        response.setExerciseOrder(workoutSet.getExerciseOrder());
        response.setSetNumber(workoutSet.getSetNumber());
        response.setWeightKg(workoutSet.getWeightKg());
        response.setReps(workoutSet.getReps());
        response.setNote(workoutSet.getNote());

        return response;
    }

    /**
     * サービスの履歴一覧をAPIレスポンスへ変換する。
     *
     * @param serviceResponses サービスの履歴一覧
     * @return API用履歴一覧（日時はUTCオフセット付き）
     */
    public static List<WorkoutHistoryResponse> toHistoryResponses(
            List<overload_api.service.dto.WorkoutHistoryResponse> serviceResponses) {
        List<WorkoutHistoryResponse> responses = new ArrayList<>();

        for (overload_api.service.dto.WorkoutHistoryResponse serviceResponse
                : serviceResponses) {

            WorkoutHistoryResponse response = new WorkoutHistoryResponse();
            response.setSessionId(serviceResponse.getSessionId());
            response.setStartedAt(WorkoutTime.toApiTime(serviceResponse.getStartedAt()));
            response.setFinishedAt(WorkoutTime.toApiTime(serviceResponse.getFinishedAt()));
            response.setBodyWeightKg(serviceResponse.getBodyWeightKg());
            response.setTotalCalories(serviceResponse.getTotalCalories());
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
     * 保存したセット一覧をAPIレスポンスへ変換する。
     *
     * @param sets 保存したセット一覧
     * @return API用セット一覧
     */
    public static List<WorkoutSetResponse> toResponses(List<WorkoutSet> sets) {
        return sets.stream().map(WorkoutApiMapper::toResponse).toList();
    }
    /**
     * ワークアウトセッションをAPIレスポンスDTOへ変換する。
     *
     * @param session 永続化モデル（必須）
     * @return API用セッション。未終了の場合はfinishedAtがnull
     */
    public static WorkoutSessionResponse toSessionResponse(WorkoutSession session) {
        Objects.requireNonNull(session, "Workout session is required");
        WorkoutSessionResponse response = new WorkoutSessionResponse();
        response.setId(session.getId());
        response.setUserId(session.getUserId());
        response.setStartedAt(WorkoutTime.toApiTime(session.getStartedAt()));
        response.setFinishedAt(WorkoutTime.toApiTime(session.getFinishedAt()));
        return response;
    }
}
