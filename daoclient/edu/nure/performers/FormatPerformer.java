package edu.nure.performers;

import edu.nure.db.Connector;
import edu.nure.db.RequestPreparing;
import edu.nure.db.dao.AbstractDAOFactory;
import edu.nure.db.dao.DBException;
import edu.nure.db.dao.domains.interfaces.FormatDAO;
import edu.nure.db.entity.Format;
import edu.nure.db.entity.constraints.ValidationException;
import edu.nure.db.entity.primarykey.StringPrimaryKey;
import edu.nure.performers.exceptions.PerformException;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * Created by bod on 21.09.15.
 * FormatPerformer class is used for handling format entity manipulation requests.
 */
public class FormatPerformer extends AbstractPerformer{
    private FormatDAO dao;
    public FormatPerformer(ResponseBuilder builder)throws DBException {
        super(builder);
        dao = AbstractDAOFactory.getDAO(AbstractDAOFactory.MYSQL).getFormatDAO();
    }

    @Override
    public void perform() throws PerformException, IOException {
        int action = builder.getAction();
        switch (action) {
            case Action.GET_FORMAT:
                doGet();
                break;
            case Action.INSERT_FORMAT:
                doInsert();
                break;
            case Action.UPDATE_FORMAT:
                doUpdate();
                break;
            case Action.DELETE_FORMAT:
                doDelete();
                break;
            default:
                builder.setStatus(ResponseBuilder.STATUS_PARAM_ERROR);
        }

    }

    @Override
    protected void doGet() throws PerformException, IOException{
        try {
            List<Format> formats;
            if (builder.getParameter("name") == null) {
                // returns full list of format values
                formats = dao.selectAll();
            } else {
                // used for suggestions list
                String name = Objects.requireNonNull(builder.getParameter("name")).replace("'", "\"");
                formats = dao.getLikeName(name);
            }
            for (Format format : formats) {
                builder.add(format);
            }
            builder.setStatus(ResponseBuilder.STATUS_OK);
        } catch (NullPointerException ex){
            throw new PerformException("Не указан нужный параметер");
        }
    }

    @Override
    protected void doInsert() throws PerformException, IOException {
        try {
            Format format = new Format(builder);
            format = dao.insert(format);
            if(format != null){
                builder.add(format);
                builder.setStatus(ResponseBuilder.STATUS_OK);
            }else {
                builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
                builder.setText("Ошибка при добавлении нового формата");
            }
        } catch (DBException e) {
            String ms = e.getMessage();
            if(ms.contains("Duplicate")) {
                throw new PerformException("Такая запись уже существует");
            }else {
                throw new PerformException("Ошибка обработки запроса" );
            }
        } catch (ValidationException e) {
            throw new PerformException("Ошибка формата данных");
        }
    }

    @Override
    protected void doUpdate() throws PerformException, IOException {
        try {
            String oldName;
            try {
                oldName = Objects.requireNonNull(builder.getParameter("oldName"));
            }catch (NullPointerException ex){throw new ValidationException();}
            Format format = new Format(builder);
            if (dao.update(format, new StringPrimaryKey("Name", oldName))){
                builder.setStatus(ResponseBuilder.STATUS_OK);
            } else {
                builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
                builder.setText("Ошибка во время изменения формата");
            }
        } catch (DBException e) {
            String msg = e.getMessage().toLowerCase();
            if(msg.contains("foreign key"))
                throw new PerformException("Невозможно удалить запись: запись используется в заказе");
            else
                throw new PerformException("Ошибка при удалении записи ");
        } catch (ValidationException e) {
            throw new PerformException("Ошибка формата данных");
        }
    }

    @Override
    protected void doDelete() throws PerformException, IOException {
        try {
            Format fm = new Format(builder);
            if(dao.delete(fm)) {
                builder.setStatus(ResponseBuilder.STATUS_OK);
            } else {
                builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
                builder.setText("Ошибка во время удаления формата");
            }
        } catch (DBException e) {
            throw new PerformException("Ошибка обработки запроса. Невозможно удалить запись");
        } catch (ValidationException e) {
            throw new PerformException("Ошибка формата данных");
        }

    }
}
