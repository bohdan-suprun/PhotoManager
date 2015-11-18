package edu.nure.db.entity.constraints;

/**
 * Created by bod on 17.09.15.
 */
public class Between<T extends Comparable> implements Comparable<T> {
    private T lo, hi;

    public Between(T lo, T hi){
        this.lo = lo;
        this.hi = hi;
    }

    @Override
    public int compareTo(T t) {
        if(t.compareTo(lo) >= 0 && t.compareTo(hi) <= 0)
            return 0;
        return -1;
    }
}
