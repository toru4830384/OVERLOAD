package overload_api.controller.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import overload_api.domain.validation.BodyWeight;
import overload_api.domain.validation.WorkoutRequestIdentifier;

/**
 * ワークアウト登録時のリクエスト情報を保持するDTO。
 */
public class WorkoutRequest {

    /**
     * 実際に開始操作を行った日時（UTCオフセット付き）。
     */
    @NotNull(message = "開始日時は必須です")
    private OffsetDateTime startedAt;

    /**
     * 実開始日時を取得する。
     *
     * @return 実開始日時
     */
    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    /**
     * 実開始日時を設定する。
     *
     * @param startedAt 実開始日時
     */
    public void setStartedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }

	/**
	 * ワークアウトを実施したユーザーのID。
	 *
	 * {@code @NotNull} により、ユーザーIDが未入力でないことを検証する。
	 */
	@NotNull
	private Long userId;

	/**
	 * ワークアウトで実施した種目の一覧。
	 *
	 * {@code @NotEmpty} により、種目一覧自体がnullまたは空にならないことを検証する。
	 * {@code @Valid} により、種目一覧内の各種目の入力値を検証する。
	 */
	@NotEmpty(message = "種目を1つ以上指定してください")
	@Valid
	private List<@NotNull WorkoutExerciseRequest> exercises;

    /**
     * ユーザーIDを取得する。
     *
     * @return ユーザーID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * ユーザーIDを設定する。
     *
     * @param userId ユーザーID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * ワークアウトで実施した種目の一覧を取得する。
     *
     * @return 種目一覧
     */
    public List<WorkoutExerciseRequest> getExercises() {
        return exercises;
    }

    /**
     * ワークアウトで実施した種目の一覧を設定する。
     *
     * @param exercises 種目一覧
     */
    public void setExercises(List<WorkoutExerciseRequest> exercises) {
        this.exercises = exercises;
    }


    /** 終了操作の日時。再試行でも同じ値を使う。 */
    @NotNull
    private OffsetDateTime finishedAt;

    /** @return 終了操作の日時。再試行でも同じ値を使う。 */
    public OffsetDateTime getFinishedAt() {
        return finishedAt;
    }

    /** @param finishedAt 終了操作の日時。再試行でも同じ値を使う。 */
    public void setFinishedAt(OffsetDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    /** 開始時点の体重（kg）。 */
    @BodyWeight
    private BigDecimal bodyWeightKg;

    /** @return 開始時点の体重（kg）。 */
    public BigDecimal getBodyWeightKg() {
        return bodyWeightKg;
    }

    /** @param bodyWeightKg 開始時点の体重（kg）。 */
    public void setBodyWeightKg(BigDecimal bodyWeightKg) {
        this.bodyWeightKg = bodyWeightKg;
    }

    /** 再送を識別するUUID。 */
    @WorkoutRequestIdentifier
    private String clientRequestId;

    /** @return 再送を識別するUUID。 */
    public String getClientRequestId() {
        return clientRequestId;
    }

    /** @param clientRequestId 再送を識別するUUID。 */
    public void setClientRequestId(String clientRequestId) {
        this.clientRequestId = clientRequestId;
    }
}
