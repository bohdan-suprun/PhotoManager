package edu.nure.db.entity;

import edu.nure.db.dao.DBException;
import edu.nure.db.entity.constraints.ValidationException;
import edu.nure.db.entity.primarykey.PrimaryKey;

import java.sql.ResultSet;

/**
 * Created by bod on 11.11.15.
 */
public interface DBEntity {
    String[] getFields();
    Object[] getValues();
    void parseResultSet(ResultSet rs) throws DBException, ValidationException;
    String entityName();
    PrimaryKey getPrimaryKey();
}
