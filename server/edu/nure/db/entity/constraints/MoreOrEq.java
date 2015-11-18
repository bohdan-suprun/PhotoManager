package edu.nure.db.entity.constraints;

/**
 * Created by bod on 17.09.15.
 */
public class MoreOrEq<T extends Comparable> extends MoreThan<T> {

    public MoreOrEq(T constraints) {
        super(constraints);
    }

    @Override
    public int compareTo(T t) {
        if(t.compareTo(constraints) >= 0)
            return 0;
        return -1;
    }
}
