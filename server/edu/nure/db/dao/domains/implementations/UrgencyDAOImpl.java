package edu.nure.db.dao.domains.implementations;

import edu.nure.db.entity.Urgency;
import edu.nure.db.entity.primarykey.PrimaryKey;

import java.sql.Connection;
import java.util.List;

/**
 * Created by bod on 11.11.15.
 */
public class UrgencyDAOImpl extends GenericDAOImpl<Urgency> {

    public UrgencyDAOImpl(Connection connection) {
        super(connection);
    }

    @Override
    public Urgency select(PrimaryKey key) {
        return getAll(Urgency.class, "WHERE `"+key.getName()+"` = "+key.getValue()).iterator().next();
    }

    @Override
    public List<Urgency> selectAll() {
        return getAll(Urgency.class, null);
    }
}
