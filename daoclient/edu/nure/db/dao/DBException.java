package edu.nure.db.dao;

/**
 * Created by bod on 11.11.15.
 */
public class DBException extends Exception {

    public DBException() {
    }

    public DBException(String message) {
        super(message);
    }

    public DBException(Exception another) {
        super(another.getMessage());
    }
}
