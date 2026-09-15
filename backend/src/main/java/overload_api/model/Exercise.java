package overload_api.model;

/**
 * トレーニング種目の情報を保持するモデル。
 */
public class Exercise {

    /**
     * 種目のID。
     */
    private Long id;

    /**
     * 種目名。
     */
    private String name;

    /**
     * 種目のカテゴリ。
     */
    private String category;

    /**
     * バーベルを使用する種目かどうか。
     */
    private Boolean barbell;

    /**
     * 使用する器具。
     */
    private String equipment;

    /**
     * 動作パターン。
     */
    private String pattern;

    /**
     * 種目のIDを取得する。
     *
     * @return 種目のID
     */
    public Long getId() {
        return id;
    }

    /**
     * 種目のIDを設定する。
     *
     * @param id 種目のID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 種目名を取得する。
     *
     * @return 種目名
     */
    public String getName() {
        return name;
    }

    /**
     * 種目名を設定する。
     *
     * @param name 種目名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 種目のカテゴリを取得する。
     *
     * @return 種目のカテゴリ
     */
    public String getCategory() {
        return category;
    }

    /**
     * 種目のカテゴリを設定する。
     *
     * @param category 種目のカテゴリ
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * バーベルを使用する種目かどうかを取得する。
     *
     * @return バーベルを使用する場合はtrue、それ以外はfalse
     */
    public Boolean getBarbell() {
        return barbell;
    }

    /**
     * バーベルを使用する種目かどうかを設定する。
     *
     * @param barbell バーベルを使用する場合はtrue、それ以外はfalse
     */
    public void setBarbell(Boolean barbell) {
        this.barbell = barbell;
    }

    /**
     * 使用する器具を取得する。
     *
     * @return 使用する器具
     */
    public String getEquipment() {
        return equipment;
    }

    /**
     * 使用する器具を設定する。
     *
     * @param equipment 使用する器具
     */
    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    /**
     * 動作パターンを取得する。
     *
     * @return 動作パターン
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * 動作パターンを設定する。
     *
     * @param pattern 動作パターン
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

}