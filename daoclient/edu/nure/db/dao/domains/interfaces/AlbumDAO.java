package edu.nure.db.dao.domains.interfaces;

import edu.nure.db.dao.domains.implementations.GenericDAOImpl;
import edu.nure.db.entity.Album;
import edu.nure.db.entity.Image;

import java.util.List;
import java.util.Map;

/**
 * Created by bod on 11.11.15.
 */
public interface AlbumDAO extends GenericDAO<Album> {

    List<Album> getUserAlbum(int userId);
    Map<Album, List<Image>> getUserAlbums(int userId);

}
