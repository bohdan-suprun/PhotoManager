package edu.nure.performers;

import edu.nure.db.Connector;
import edu.nure.db.RequestPreparing;
import edu.nure.db.entity.Urgency;
import edu.nure.db.entity.constraints.ValidationException;
import edu.nure.performers.exceptions.PerformException;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Created by bod on 21.09.15.
 */
public class UrgencyPerformer extends AbstractPerformer {

    public UrgencyPerformer(ResponseBuilder b) throws SQLException{
        connection = Connector.getConnector().getConnection();
        setBuilder(b);
    }

    public void perform() throws PerformException, IOException, SQLException {
        int action = builder.getAction();
        switch (action) {
            case Action.GET_URGENCY:
                doGet();
                break;
            case Action.INSERT_URGENCY:
                doInsert();
                break;
            case Action.UPDATE_URGENCY:
                doUpdate();
                break;
            case Action.DELETE_URGENCY:
                doDelete();
                break;
            default:
                builder.setStatus(ResponseBuilder.STATUS_PARAM_ERROR);
        }
    }

    @Override
    protected void doGet() throws PerformException, IOException, SQLException {
        try {
            ResultSet rs = getStatement().executeQuery(RequestPreparing.select("urgency", new String[]{"*"}, null));
            while (rs.next()) {
                Urgency urgency = new Urgency(rs);
                builder.add(urgency);
            }
            builder.setStatus(ResponseBuilder.STATUS_OK);
        } catch (SQLException e) {
            throw new PerformException("Ошибка при работе с базой данных " + e.getMessage());
        } catch (ValidationException e) {
            throw new PerformException("Неверный формат данных");
        } catch (NullPointerException ex){
            throw new PerformException("Не указан нужный параметер");
        }
    }

    @Override
    protected void doInsert() throws PerformException, IOException, SQLException {
        try {
            Urgency urgency = new Urgency(builder);
            int n = getStatement().executeUpdate(RequestPreparing.insert("urgency", Urgency.getFields(),
                    new Object[]{urgency.getTerm(), urgency.getFactor()}));
            if(n > 0) {
                builder.add(urgency);
                builder.setStatus(ResponseBuilder.STATUS_OK);
                return;
            }
            builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
            builder.setText("Неудалось добавить термин");
        } catch (SQLException e) {
            String msg = e.getMessage();
            if(msg.contains("Duplicate")) {
                throw new PerformException("Такая запись уже существует");
            }else{
                throw new PerformException("Ошибка обработки запроса: " + e.getMessage());
            }
        } catch (ValidationException e) {
            throw new PerformException("Ошибка формата данных");
        }
    }

    @Override
    protected void doUpdate() throws PerformException, IOException, SQLException {
        try {
            int oldTerm;
            try {
                oldTerm = Integer.valueOf(Objects.requireNonNull(builder.getParameter("oldTerm")));
            }catch (NumberFormatException ex){
                throw new ValidationException();
            }
            Urgency urgency = new Urgency(builder);
            int n = getStatement().executeUpdate(RequestPreparing.update("urgency", Urgency.getFields(),
                    new Object[]{urgency.getTerm(), urgency.getFactor()},
                    "Term=" + oldTerm));
            if(n > 0) {
                builder.setStatus(ResponseBuilder.STATUS_OK);
                return;
            }
            builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
            builder.setText("Неудалось обновить термин");
        } catch (SQLException e) {
            throw new PerformException("Ошибка обработки запроса: "+e.getMessage());
        } catch (ValidationException e) {
            throw new PerformException("Ошибка формата данных");
        }
    }

    @Override
    protected void doDelete() throws PerformException, IOException, SQLException {
        try {
            Urgency urgency = new Urgency(builder);
            int n = getStatement().executeUpdate("DELETE FROM URGENCY WHERE Term=" + urgency.getTerm());
            if(n > 0) {
                builder.setStatus(ResponseBuilder.STATUS_OK);
                return;
            }
            builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
            builder.setText("Неудалось удалить термин");
        } catch (SQLException e) {
            String msg = e.getMessage().toLowerCase();
            if(msg.contains("foreign key"))
                throw new PerformException("Невозможно удалить запись: запись используется в заказе");
            else
                throw new PerformException("Ошибка при удалении записи ");
        } catch (ValidationException e) {
            throw new PerformException("Ошибка формата данных");
        }

    }
}
