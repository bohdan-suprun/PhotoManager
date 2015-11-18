package edu.nure.performers;

import edu.nure.db.Connector;
import edu.nure.db.RequestPreparing;
import edu.nure.db.dao.AbstractDAOFactory;
import edu.nure.db.dao.DBException;
import edu.nure.db.dao.domains.interfaces.GenericDAO;
import edu.nure.db.entity.Urgency;
import edu.nure.db.entity.constraints.ValidationException;
import edu.nure.db.entity.primarykey.IntegerPrimaryKey;
import edu.nure.performers.exceptions.PerformException;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Created by bod on 21.09.15.
 */
public class UrgencyPerformer extends AbstractPerformer {

    private GenericDAO<Urgency> dao;

    public UrgencyPerformer(ResponseBuilder b) throws DBException {
        super(b);
        dao = AbstractDAOFactory.getDAO(AbstractDAOFactory.MYSQL).getUrgencyDAO();
    }

    public void perform() throws PerformException, IOException {
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
    protected void doGet() throws PerformException, IOException {
        for (Urgency urgency: dao.selectAll()) {
            builder.add(urgency);
        }
        builder.setStatus(ResponseBuilder.STATUS_OK);
    }

    @Override
    protected void doInsert() throws PerformException, IOException {
        try {
            Urgency urgency = new Urgency(builder);
            urgency = dao.insert(urgency);
            if(urgency != null) {
                builder.add(urgency);
                builder.setStatus(ResponseBuilder.STATUS_OK);
            } else {
                builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
                builder.setText("Неудалось добавить срок");
            }
        } catch (DBException e) {
            String msg = e.getMessage();
            if(msg.contains("Duplicate")) {
                throw new PerformException("Такая запись уже существует");
            }else{
                throw new PerformException("Ошибка обработки запроса");
            }
        } catch (ValidationException e) {
            throw new PerformException("Ошибка формата данных");
        }
    }

    @Override
    protected void doUpdate() throws PerformException, IOException {
        try {
            int oldTerm = Integer.valueOf(Objects.requireNonNull(builder.getParameter("oldTerm")));
            Urgency urgency = new Urgency(builder);
            if(dao.update(urgency, new IntegerPrimaryKey("Term", oldTerm))) {
                builder.setStatus(ResponseBuilder.STATUS_OK);
            } else {
                builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
                builder.setText("Неудалось обновить срок");
            }
        } catch (DBException e) {
            throw new PerformException("Ошибка обработки запроса");
        } catch (ValidationException e) {
            throw new PerformException("Ошибка формата данных");
        }
    }

    @Override
    protected void doDelete() throws PerformException, IOException {
        try {
            Urgency urgency = new Urgency(builder);
            if(dao.delete(urgency)) {
                builder.setStatus(ResponseBuilder.STATUS_OK);
            } else {
                builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
                builder.setText("Неудалось удалить срок");
            }
        } catch (DBException e) {
            String msg = e.getMessage().toLowerCase();
            if(msg.contains("foreign key"))
                throw new PerformException("Невозможно удалить запись: запись используется в заказе");
            else
                throw new PerformException("Ошибка при удалении записи");
        } catch (ValidationException e) {
            throw new PerformException("Ошибка формата данных");
        }

    }
}
