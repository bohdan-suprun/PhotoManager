package edu.nure.db.entity.primarykey;

/**
 * Created by bod on 11.11.15.
 */
public class PrimaryKey<T> {

    protected T value;
    protected String name;

    public PrimaryKey(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public String getName() {
        return name;
    }
}
