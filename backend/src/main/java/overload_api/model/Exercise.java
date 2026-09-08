package overload_api.model;

public class Exercise {

    private Long id;
    private String name;
    private String category;
    private Boolean barbell;
    private String equipment;
    private String pattern;

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getBarbell() {
        return barbell;
    }

    public void setBarbell(Boolean barbell) {
        this.barbell = barbell;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}