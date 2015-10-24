package edu.nure.performers;

import edu.nure.db.Connector;
import edu.nure.db.entity.Right;
import edu.nure.performers.exceptions.PerformException;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by bod on 21.09.15.
 */
public class RightPerformer extends AbstractPerformer {

    public RightPerformer(ResponseBuilder b) throws SQLException{
        connection = Connector.getConnector().getConnection();
        setBuilder(b);
    }

    @Override
    public void perform() throws PerformException, IOException, SQLException {
        try {
            ResultSet rs = getStatement().executeQuery("Select * From `USER_RIGHT` WHERE Type != 'su';");
            while (rs.next()){
                Right right = new Right(rs);
                builder.add(right);
            }
            builder.setStatus(ResponseBuilder.STATUS_OK);
        } catch (SQLException e) {
            throw new PerformException("Ошибка работы с базой данных: "+e.getMessage());
        }
    }
}
