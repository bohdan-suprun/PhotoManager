package edu.nure.listener.exception;

/**
 * Created by bod on 01.10.15.
 */
public class UnknownTagNameException extends Exception{
    public UnknownTagNameException() {
    }

    public UnknownTagNameException(String message) {
        super(message);
    }
}
