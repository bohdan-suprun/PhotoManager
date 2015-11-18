package edu.nure.db.dao.domains.implementations;

import edu.nure.db.RequestPreparing;
import edu.nure.db.dao.DBException;
import edu.nure.db.dao.domains.interfaces.ImageDAO;
import edu.nure.db.entity.Image;
import edu.nure.db.entity.primarykey.PrimaryKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by bod on 11.11.15.
 */
public class ImageDAOImpl extends GenericDAOImpl<Image> implements ImageDAO {

    public ImageDAOImpl(Connection connection) {
        super(connection);
    }

    @Override
    public List<Image> getLike(String hash, int limit) {
        return getAll(Image.class,
                "Where BIT_COUNT(CONV(`Hash`, 16, 10) ^ CONV('" + hash + "', 16, 10)) <=" + limit +
                        " ORDER BY " + "BIT_COUNT(CONV(`Hash`, 16, 10) ^ CONV('" + hash + "', 16, 10))"
        );
    }

    @Override
    public Image select(PrimaryKey key) {
        return getAll(Image.class, "Where `" + key.getName() + "`=" + key.getValue()).iterator().next();
    }

    @Override
    public List<Image> getInAlbum(int albumId) {
        return getAll(Image.class, "WHERE `Album` = " + albumId +
                " ORDER by CreatedIn desc");
    }

    @Override
    public Image insert(Image ent) throws DBException {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO `IMAGE` " +
                            "(`Hash`, `Album`,`CreatedIn`,`Image`)" +
                            "VALUES(?, ?, ?, ?)"
            );

            ps.setString(1, ent.getHash());
            ps.setInt(2, ent.getAlbum());
            ps.setString(3, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            ps.setBytes(4, ent.getImage());
            connection.setAutoCommit(false);
            int n = ps.executeUpdate();
            if (n != 1){
                throw new DBException("Ошибка при добавлении нового элемента: ничего не добавлено");
            }
            Image lastIm = getLastInserted(ent);
            connection.commit();
            return lastIm;
        } catch (Exception ex){
            throw new DBException(ex);
        } finally {
            try{
                connection.setAutoCommit(true);
            } catch (SQLException ex){
                throw new DBException(ex);
            }
        }
    }

}
