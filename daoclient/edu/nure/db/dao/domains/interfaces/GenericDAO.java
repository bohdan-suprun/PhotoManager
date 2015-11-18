package edu.nure.db.dao.domains.interfaces;

import edu.nure.db.dao.DBException;
import edu.nure.db.entity.DBEntity;
import edu.nure.db.entity.primarykey.PrimaryKey;

import java.util.List;

/**
 * Created by bod on 11.11.15.
 */
public interface GenericDAO<T extends DBEntity> {

    T insert(T ent) throws DBException;
    boolean update(T ent) throws DBException;
    boolean update(T ent, PrimaryKey pk) throws DBException;
    boolean delete(T ent) throws DBException;
    boolean delete(String entityName,PrimaryKey key) throws DBException;
    T select(PrimaryKey key);
    List<T> selectAll();

}
