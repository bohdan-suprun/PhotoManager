package edu.nure.db.dao.domains.implementations;

import edu.nure.db.RequestPreparing;
import edu.nure.db.dao.DBException;
import edu.nure.db.dao.domains.interfaces.FormatDAO;
import edu.nure.db.entity.Format;
import edu.nure.db.entity.primarykey.PrimaryKey;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bod on 11.11.15.
 */
public class FormatDAOImpl extends GenericDAOImpl<Format> implements FormatDAO {

    public FormatDAOImpl(Connection connection) {
        super(connection);
    }

    @Override
    public List<Format> getLikeName(String like) {
        return getAll(Format.class, "WHERE `Name` LIKE '" + like + "%'");
    }

    @Override
    public Format select(PrimaryKey key) {
        return getAll(Format.class,
                "WHERE `"+key.getName()+"`=\'" + key.getValue()+ "\'").iterator().next();
    }

    @Override
    public List<Format> selectAll() {
        return getAll(Format.class, null);
    }

    @Override
    protected Format getLastInserted(Format tClass) {
        return tClass;
    }
}
