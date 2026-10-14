package overload_api.controller.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * ユーザー情報更新時のリクエスト情報を保持するDTO。
 */
public class UserUpdateRequest {

    /**
     * ユーザー名。必須・50文字以内。空文字はDB仕様どおり許可する。
     */
    @NotNull
    @Size(max = 50)
    private String name;

    /**
     * 性別。API値はmale / female、表示名はフロントで変換する。
     */
    @NotNull(message = "性別は必須です")
    @Pattern(regexp = "male|female", message = "性別は男性または女性を選択してください")
    private String gender;

    /**
     * 年齢。
     *
     * {@code @NotNull} と {@code @Min} により、年齢が必須かつ0以上であることを検証する。
     */
    @NotNull(message = "年齢は必須です")
    @Min(
            value = 0,
            message = "年齢は0以上の値を入力してください")
    private Integer age;

    /**
     * 体重（kg）。
     *
     * {@code @NotNull}・{@code @Digits}で必須・DECIMAL(5,2)の範囲を検証し、
     * {@code @DecimalMin} により、
     * 体重が0より大きい値であることを検証する。
     */
    @NotNull(message = "体重は必須です")
    @Digits(integer = 3, fraction = 2, message = "体重は999.99kg以下、小数第2位まで入力してください")
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "体重は0より大きい値を入力してください")
    private BigDecimal bodyWeightKg;

    /**
     * ユーザー名を取得する。
     *
     * @return ユーザー名
     */
    public String getName() {
        return name;
    }

    /**
     * ユーザー名を設定する。
     *
     * @param name ユーザー名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 性別を取得する。
     *
     * @return 性別
     */
    public String getGender() {
        return gender;
    }

    /**
     * 性別を設定する。
     *
     * @param gender 性別
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * 年齢を取得する。
     *
     * @return 年齢
     */
    public Integer getAge() {
        return age;
    }

    /**
     * 年齢を設定する。
     *
     * @param age 年齢
     */
    public void setAge(Integer age) {
        this.age = age;
    }

    /**
     * 体重（kg）を取得する。
     *
     * @return 体重（kg）
     */
    public BigDecimal getBodyWeightKg() {
        return bodyWeightKg;
    }

    /**
     * 体重（kg）を設定する。
     *
     * @param bodyWeightKg 体重（kg）
     */
    public void setBodyWeightKg(BigDecimal bodyWeightKg) {
        this.bodyWeightKg = bodyWeightKg;
    }
}