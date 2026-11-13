package overload_api.controller.dto;

import java.math.BigDecimal;


import overload_api.domain.validation.BodyWeight;
import overload_api.domain.validation.ProfileAge;
import overload_api.domain.validation.ProfileGender;
import overload_api.domain.validation.ProfileName;

/**
 * ユーザー情報更新時のリクエスト情報を保持するDTO。
 */
public class UserUpdateRequest {

    /**
     * ユーザー名。必須・50文字以内。DB仕様と同じく空文字は許可する。
     */
    @ProfileName
    private String name;

    /**
     * 性別。API値はmale / female、表示名はフロントで変換する。
     */
    @ProfileGender
    private String gender;

    /**
     * 年齢。
     *
     * 共通の{@code @ProfileAge}で必須かつ0以上を検証する。
     */
    @ProfileAge
    private Integer age;

    /**
     * 体重（kg）。
     *
     * 共通の{@code @BodyWeight}で必須・正数・DECIMAL(5,2)の保存範囲を検証する。
     */
    @BodyWeight
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