package edu.nure.db.dao.domains.interfaces;

import edu.nure.db.entity.Image;

import java.util.List;

/**
 * Created by bod on 11.11.15.
 */
public interface ImageDAO extends GenericDAO<Image> {

    List<Image> getLike(String hash, int limit);
    List<Image> getInAlbum(int albumId);

}
