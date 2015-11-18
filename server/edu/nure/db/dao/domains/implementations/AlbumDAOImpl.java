package edu.nure.db.dao.domains.implementations;

import edu.nure.db.RequestPreparing;
import edu.nure.db.dao.DBException;
import edu.nure.db.dao.domains.interfaces.AlbumDAO;
import edu.nure.db.dao.domains.interfaces.GenericDAO;
import edu.nure.db.entity.Album;
import edu.nure.db.entity.Image;
import edu.nure.db.entity.constraints.ValidationException;
import edu.nure.db.entity.primarykey.PrimaryKey;

import javax.naming.OperationNotSupportedException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by bod on 11.11.15.
 */
public class AlbumDAOImpl extends GenericDAOImpl<Album> implements AlbumDAO {

    public AlbumDAOImpl(Connection connection) {
        super(connection);
    }

    @Override
    public Album select(PrimaryKey key) {
        return getAll(Album.class, "WHERE `" + key.getName() + "` = "
                + key.getValue() + " LIMIT 1").iterator().next();
    }

    @Override
    public List<Album> selectAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Album> getUserAlbum(int userId) {
        return getAll(Album.class, "WHERE UserId = " + userId + " Order by Name");
    }

    /**
     *
     * @param userId
     * @return users albums and images in the album
     */
    @Override
    public Map<Album, List<Image>> getUserAlbums(int userId) {
        Map<Album, List<Image>> result = new HashMap<Album, List<Image>>();
        try {
            ResultSet rs = connection.createStatement().executeQuery(
                    "Select UserId, `Name`, a.Id as aId, i.Id, `Hash`, `Image`, `Album`, `CreatedIn` From `ALBUM` as a " +
                            "INNER JOIN `IMAGE` as i on i.Album = a.Id" +
                            " Where `UserId` = " + userId +
                            " Group by `Name`, i.`Id`, `Album`"
            );
            while (rs.next()){
                Album album = new Album(
                        rs.getString("Name"),
                        rs.getInt("aId"),
                        rs.getInt("UserId")
                );
                Image image = new Image();
                image.parseResultSet(rs);
                List<Image> albumImages = result.get(album);
                if (albumImages == null){
                    albumImages = new ArrayList<Image>();
                }
                albumImages.add(image);
                result.put(album, albumImages);
            }
            return result;
        } catch (SQLException ex){
            return result;
        } catch (DBException ex){
            return result;
        } catch (ValidationException ex){
            return result;
        }
    }
}
