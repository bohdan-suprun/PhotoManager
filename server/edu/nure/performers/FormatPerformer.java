package edu.nure.performers;

import edu.nure.db.Connector;
import edu.nure.db.RequestPreparing;
import edu.nure.db.entity.Format;
import edu.nure.db.entity.constraints.ValidationException;
import edu.nure.performers.exceptions.PerformException;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Created by bod on 21.09.15.
 */
public class FormatPerformer extends AbstractPerformer{
    public FormatPerformer(ResponseBuilder builder)throws SQLException{
        connection = Connector.getConnector().getConnection();
        setBuilder(builder);
    }

    @Override
    public void perform() throws PerformException, IOException, SQLException {
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
    protected void doGet() throws PerformException, IOException, SQLException {
        try {
            ResultSet rs;
            if(builder.getParameter("name") == null) {
                rs = getStatement().executeQuery(RequestPreparing.select("format", new String[]{"*"}, null));
            } else {
                String name = Objects.requireNonNull(builder.getParameter("name")).replace("'", "\"");
                 rs = getStatement().executeQuery(RequestPreparing.select("format", new String[]{"*"},
                        "WHERE Name LIKE '" + name + "%'"));
            }
            while ( rs != null && rs.next()) {
                Format format = new Format(rs);
                builder.add(format);
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
            Format format = new Format(builder);
            int n = getStatement().executeUpdate(RequestPreparing.insert("format", Format.getFields(),
                    new Object[]{format.getName(), format.getWidth(), format.getHeight(),
                            format.getPrice()}));
            if(n > 0){
                builder.add(format);
                builder.setStatus(ResponseBuilder.STATUS_OK);
                return;
            }
            builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
            builder.setText("Ошибка при добавлении нового формата");
        } catch (SQLException e) {
            String ms = e.getMessage();
            if(ms.contains("Duplicate")) {
                throw new PerformException("Такая запись уже существует");
            }else {
                throw new PerformException("Ошибка обработки запроса: " + e.getMessage());
            }
        } catch (ValidationException e) {
            throw new PerformException("Ошибка формата данных");
        }
    }

    @Override
    protected void doUpdate() throws PerformException, IOException, SQLException {
        try {
            String oldName;
            try {
                oldName = Objects.requireNonNull(builder.getParameter("oldName"));
            }catch (NullPointerException ex){throw new ValidationException();}
            Format format = new Format(builder);

                    String req = RequestPreparing.update("format",
                    Format.getFields(),
                    new Object[]{format.getName(), format.getWidth(), format.getHeight(), format.getPrice()},
                    "`Name`='" + oldName+"'");
            int n = getStatement().executeUpdate(req);
            if(n > 0) {
                builder.setStatus(builder.STATUS_OK);
                return;
            }
            builder.setStatus(builder.STATUS_ERROR_WRITE);
            builder.setText("Ошибка во время изменения формата");
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

    @Override
    protected void doDelete() throws PerformException, IOException, SQLException {
        try {
            Format fm = new Format(builder);
            int n = getStatement().executeUpdate("DELETE FROM `FORMAT` WHERE `Name`='" + fm.getName()+"'");
            if(n > 0) {
                builder.setStatus(ResponseBuilder.STATUS_OK);
                return;
            }
            builder.setStatus(builder.STATUS_ERROR_WRITE);
            builder.setText("Ошибка во время удаления формата");
        } catch (SQLException e) {
            throw new PerformException("Ошибка обработки запроса: "+e.getMessage());
        } catch (ValidationException e) {
            throw new PerformException("Ошибка формата данных");
        }

    }
}
