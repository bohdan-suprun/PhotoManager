package edu.nure.db.dao;

/**
 * Created by bod on 11.11.15.
 */
abstract public class AbstractDAOFactory {
    public static final int MYSQL = 1;

    public static DAOFactory getDAO(int type) throws DBException{
        switch (type){
            case MYSQL: return new MySQLDAOFactory();
            default: return null;
        }
    }
}
