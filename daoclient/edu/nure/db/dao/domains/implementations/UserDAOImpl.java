package edu.nure.db.dao.domains.implementations;

import edu.nure.UserManager;
import edu.nure.db.RequestPreparing;
import edu.nure.db.dao.DBException;
import edu.nure.db.dao.domains.interfaces.UserDAO;
import edu.nure.db.entity.User;
import edu.nure.db.entity.constraints.ValidationException;
import edu.nure.db.entity.primarykey.PrimaryKey;

import javax.jws.soap.SOAPBinding;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Created by bod on 11.11.15.
 */
public class UserDAOImpl extends GenericDAOImpl<User> implements UserDAO {

    public UserDAOImpl(Connection connection) {
        super(connection);
    }

    @Override
    public User login(String login, String pass) {
        List<User> li = getAll(User.class, "WHERE `Phone`='"+login+"' and `Password`='"
                +pass+"' AND `Password` IS NOT NULL");

        return li.iterator().next();
    }

    @Override
    public List<User> getByName(String likeName) {
        return getByName(likeName, false);
    }

    @Override
    public List<User> getByName(String likeName, boolean withHiRights) {
        if(withHiRights) {
            return getAll(User.class, "WHERE `Name` LIKE '%" + likeName
                            + "%' AND `Right` = 'фотограф' ORDER BY `Name` LIMIT 15"
            );
        } else {
            return getAll(User.class,
                    "WHERE Name LIKE '%" + likeName + "%' Group BY `Name` ORDER BY Name LIMIT 15"
            );
        }
    }

    @Override
    public List<User> getAllNames(String likeName) {
        return getAll(User.class,
                "WHERE Name LIKE '%" + likeName + "%' ORDER BY Name LIMIT 15"
        );
    }

    @Override
    public List<User> getByPhone(String likePhone) {
        return getAll(User.class, "Where replace(replace(replace(replace(replace(`Phone`,'+','')"
                + ",'-',''),'(',''),')',''),' ','') "
                +"Like '%" + likePhone + "%' ORDER BY Phone LIMIT 15");
    }

    @Override
    public boolean setPassword(int id, String pass) throws DBException {
        try {
            String sql = RequestPreparing.update("USER", new String[]{"Password"}, new Object[]{pass},
                    "`Id`="+id+" AND `Password` is NULL"
            );
            int n = connection.createStatement().executeUpdate(sql);
            return n == 1;
        } catch (SQLException ex){
            throw new DBException(ex);
        }
    }

    @Override
    public User authenticate(String code) throws DBException {

        try {
            String sql = "Select * FROM `AUT` INNER JOIN `USER` USING(Id) WHERE `Code` = '" + code
                    + "' AND `Password` is null";

            ResultSet rs = connection.createStatement().executeQuery(sql);
            if (rs.next()) {
                User user = new User();
                user.parseResultSet(rs);
                // removing the auth code if the user exists
                connection.createStatement().executeUpdate(
                        "DELETE FROM `AUT` WHERE `Id` = '" + user.getId() + "'"
                );
                return user;
            }
            return null;
        } catch (SQLException ex){
            throw new DBException(ex);
        } catch (ValidationException ex) {
            return null;
        }
    }

    @Override
    public User select(PrimaryKey key) {
        return getAll(User.class, "WHERE `"+key.getName()+"` = "+key.getValue()).iterator().next();
    }

    @Override
    public String insertWithCode(User ent) throws DBException {
        String autCode = null;
        try {
            User user = super.insert(ent);
            if (user != null) {
                autCode = getCode(user);
                ent.setId(user.getId());
                String sql = RequestPreparing.insert("aut", new String[]{"Id", "Code"}, new Object[]{user.getId(), autCode});
                connection.createStatement().executeUpdate(sql);
            } else {
                ent.setId(User.ID_NOT_SET);
            }
            return autCode;
        } catch (SQLException ex){
            throw new DBException(ex);
        }
    }

    private String getCode(User user){
        try {
            return new BigInteger(MessageDigest.getInstance("MD5").digest((user.getName()+new Date().getTime()
                    + user.getId() + user.getPhone() + (1000000+new Random()
                            .nextLong())).getBytes())).toString(16);
        } catch (NoSuchAlgorithmException e) {
            return new BigInteger(user.getName() + new Date().getTime()+user.getPhone()+user.getId()+(1000000+
                    new Random().nextLong())).toString(16);
        }
    }
}
