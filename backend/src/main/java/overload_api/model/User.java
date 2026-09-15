package overload_api.model;

import java.math.BigDecimal;

/**
 * ユーザー情報を保持するモデル。
 */
public class User {

    /**
     * ユーザーのID。
     */
    private Long id;

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
     */
    private Integer age;

    /**
     * 体重（kg）。
     */
    private BigDecimal bodyWeightKg;

    /**
     * ユーザーのIDを取得する。
     *
     * @return ユーザーのID
     */
    public Long getId() {
        return id;
    }

    /**
     * ユーザーのIDを設定する。
     *
     * @param id ユーザーのID
     */
    public void setId(Long id) {
        this.id = id;
    }

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