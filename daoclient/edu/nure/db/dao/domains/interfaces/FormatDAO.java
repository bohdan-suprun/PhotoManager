package edu.nure.db.dao.domains.interfaces;

import edu.nure.db.entity.Format;

import java.util.List;

/**
 * Created by bod on 11.11.15.
 */
public interface FormatDAO extends GenericDAO<Format> {

    List<Format> getLikeName(String like);


}
