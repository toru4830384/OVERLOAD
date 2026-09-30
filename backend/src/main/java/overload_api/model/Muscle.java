package overload_api.model;

/**
 * 筋肉情報を保持するモデル。
 */
public class Muscle {

    /**
     * 筋肉のID。
     */
    private Long id;

    /**
     * 筋肉名。
     */
    private String name;

    /**
     * 筋肉のIDを取得する。
     *
     * @return 筋肉のID
     */
    public Long getId() {

        return id;

    }

    /**
     * 筋肉のIDを設定する。
     *
     * @param id 筋肉のID
     */
    public void setId(Long id) {

        this.id = id;

    }

    /**
     * 筋肉名を取得する。
     *
     * @return 筋肉名
     */
    public String getName() {

        return name;

    }

    /**
     * 筋肉名を設定する。
     *
     * @param name 筋肉名
     */
    public void setName(String name) {

        this.name = name;

    }

}