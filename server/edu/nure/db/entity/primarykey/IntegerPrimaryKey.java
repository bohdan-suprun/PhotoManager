package edu.nure.db.entity.primarykey;

import edu.nure.db.entity.primarykey.PrimaryKey;

/**
 * Created by bod on 11.11.15.
 */
public class IntegerPrimaryKey extends PrimaryKey<Integer> {

    public IntegerPrimaryKey(Integer value) {
        super("Id", value);
    }
    public IntegerPrimaryKey(String name, Integer value) {
        super(name, value);
    }
}
