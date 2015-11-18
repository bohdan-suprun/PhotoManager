package edu.nure.performers;

import edu.nure.db.Connector;
import edu.nure.db.dao.AbstractDAOFactory;
import edu.nure.db.dao.DBException;
import edu.nure.db.dao.domains.interfaces.GenericDAO;
import edu.nure.db.entity.Right;
import edu.nure.performers.exceptions.PerformException;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by bod on 21.09.15.
 */
public class RightPerformer extends AbstractPerformer {

    private GenericDAO<Right> dao;
    public RightPerformer(ResponseBuilder b) throws DBException {
        super(b);
        dao = AbstractDAOFactory.getDAO(AbstractDAOFactory.MYSQL).getRightDAO();
    }

    @Override
    public void perform() throws PerformException, IOException {

        for (Right right: dao.selectAll()){
            builder.add(right);
        }

        builder.setStatus(ResponseBuilder.STATUS_OK);
    }
}
