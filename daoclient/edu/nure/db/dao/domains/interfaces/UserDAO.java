package edu.nure.db.dao.domains.interfaces;

import edu.nure.db.dao.DBException;
import edu.nure.db.entity.User;

import java.util.List;

/**
 * Created by bod on 11.11.15.
 */
public interface UserDAO extends GenericDAO<User> {

    User login(String login, String pass);
    List<User> getByName(String likeName);
    List<User> getAllNames(String likeName);
    List<User> getByName(String likeName, boolean withHiRights);
    List<User> getByPhone(String likePhone);
    boolean setPassword(int id, String pass) throws DBException;
    User authenticate(String code) throws DBException;
    public String insertWithCode(User ent) throws DBException;

}
