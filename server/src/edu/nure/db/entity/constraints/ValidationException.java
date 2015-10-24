package edu.nure.db.entity.constraints;

/**
 * Created by bod on 17.09.15.
 */
public class ValidationException extends Exception {
    public ValidationException(){
        super();
    }

    public ValidationException(String msg){
        super(msg);
    }
}
