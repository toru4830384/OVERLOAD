package overload_api.controller.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

/**
 * ユーザー情報更新時のリクエスト情報を保持するDTO。
 */
public class UserUpdateRequest {

    /**
     * ユーザー名。
     */
    private String name;

    /**
     * 性別。
     */
    private String gender;

    /**
     * 年齢。
     *
     * @Minにより、年齢が0以上であることを検証する。
     */
    @Min(
            value = 0,
            message = "年齢は0以上の値を入力してください")
    private Integer age;

    /**
     * 体重（kg）。
     *
     * @DecimalMinにより、
     * 体重が0より大きい値であることを検証する。
     */
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