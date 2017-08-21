package org.kite.movieindex.entity;

/**
 * Created by Mikhail_Miroliubov on 8/21/2017.
 */
public class Group {
    private String name;
    private Integer value;

    public Group() {
    }

    public Group(String name, Integer value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
