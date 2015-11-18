package edu.nure.db.dao.domains.implementations;

import edu.nure.db.RequestPreparing;
import edu.nure.db.dao.DBException;
import edu.nure.db.dao.domains.interfaces.GenericDAO;
import edu.nure.db.entity.DBEntity;
import edu.nure.db.entity.Image;
import edu.nure.db.entity.primarykey.PrimaryKey;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bod on 11.11.15.
 */
abstract public class GenericDAOImpl<T extends DBEntity> implements GenericDAO<T> {

    protected Connection connection;

    public GenericDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public T insert(T ent) throws DBException {
        try {
            String sql = RequestPreparing.insert(ent.entityName(), ent.getFields(), ent.getValues());
            connection.setAutoCommit(false);
            int n = connection.createStatement().executeUpdate(sql);
            if (n != 1 ){
                throw new DBException("Произошла ошибка во время добавления данных: ничего не добавлено");
            }
            T last = getLastInserted(ent);
            connection.commit();
            return last;
        } catch (SQLException ex){
            throw new DBException(ex);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ex){
                throw new DBException(ex);
            }
        }
    }

    protected T getLastInserted(T tClass){
        try {
            T last = (T) tClass.getClass().newInstance();
            PrimaryKey pk = last.getPrimaryKey();
            String sql = RequestPreparing.select(last.entityName(), new String[]{"*"},
                    "WHERE `"+pk.getName()+"`= (Select Max("+ pk.getName() +") From `"+RequestPreparing.DB_NAME+"`.`"
                            +last.entityName()+"`)"
            );
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()){
                last.parseResultSet(rs);
            }
            return last;
        } catch (Exception ex){
            return null;
        }
    }

    @Override
    public boolean update(T ent, PrimaryKey key) throws DBException {
        try {
            String sql = RequestPreparing.update(ent.entityName(), ent.getFields(), ent.getValues(),
                    key.getName() + " = "+key.getValue());
            int n = connection.createStatement().executeUpdate(sql);
            return n > 0;
        } catch (SQLException ex){
            throw new DBException(ex);
        }
    }

    @Override
    public boolean update(T ent) throws DBException {
        return update(ent, ent.getPrimaryKey());
    }

    @Override
    public boolean delete(T ent) throws DBException {
        return delete(ent.entityName(), ent.getPrimaryKey());
    }

    @Override
    public boolean delete(String entityName,PrimaryKey key) throws DBException {
        try {
            if (entityName == null || entityName.isEmpty() || entityName.contains("'")){
                throw new SQLException("Unreachable entity name");
            }
            String sql = "DELETE FROM `"+RequestPreparing.DB_NAME+"`.`"+entityName+"` WHERE "+key.getName()+"="+key.getValue();
            int n = connection.createStatement().executeUpdate(sql);
            return n > 0;
        } catch (SQLException ex){
            throw new DBException(ex);
        }
    }

    @Override
    public List<T> selectAll() {
        throw new UnsupportedOperationException();
    }

    protected List<T> getAll(Class<T> tClass, String cond){
        List<T> list = new ArrayList<T>();
        try {
            T inst = tClass.newInstance();
            String sql = RequestPreparing.select(inst.entityName(), new String[]{"*"}, cond);
            ResultSet rs = connection.createStatement().executeQuery(sql);

            while (rs.next()){
                inst = tClass.newInstance();
                inst.parseResultSet(rs);
                list.add(inst);
            }

            return list;
        } catch (Exception ex){
            return list;
        }
    }
}
