package edu.nure.db.entity.constraints;

/**
 * Created by bod on 17.09.15.
 */
public class MoreThan<T extends Comparable> implements Comparable<T>{
    protected T constraints;

    public MoreThan(T constraints){
        this.constraints = constraints;
    }

    @Override
    public int compareTo(T t) {
        if(t.compareTo(constraints) > 0)
            return 0;
        return -1;
    }
}
