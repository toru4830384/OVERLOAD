package overload_api.model;

import java.math.BigDecimal;

public class User {

    private Long id;
    private String name;
    private String gender;
    private Integer age;
    private BigDecimal bodyWeightKg;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public BigDecimal getBodyWeightKg() {
        return bodyWeightKg;
    }

    public void setBodyWeightKg(BigDecimal bodyWeightKg) {
        this.bodyWeightKg = bodyWeightKg;
    }

}
