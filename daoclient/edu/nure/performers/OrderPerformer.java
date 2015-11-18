package edu.nure.performers;

import edu.nure.db.Connector;
import edu.nure.db.RequestPreparing;
import edu.nure.db.dao.AbstractDAOFactory;
import edu.nure.db.dao.DBException;
import edu.nure.db.dao.domains.interfaces.OrderDAO;
import edu.nure.db.entity.Order;
import edu.nure.db.entity.User;
import edu.nure.db.entity.constraints.ValidationException;
import edu.nure.db.entity.primarykey.IntegerPrimaryKey;
import edu.nure.db.entity.primarykey.StringPrimaryKey;
import edu.nure.email.EmailSender;
import edu.nure.performers.exceptions.PerformException;

import javax.mail.MessagingException;
import java.io.IOException;
import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by bod on 21.09.15.
 */
public class OrderPerformer extends AbstractPerformer {
    private OrderDAO dao;
    public OrderPerformer(ResponseBuilder b) throws DBException {
        super(b);
        dao = AbstractDAOFactory.getDAO(AbstractDAOFactory.MYSQL).getOrderDAO();
    }

    @Override
    public void perform() throws PerformException, IOException {
        int action = Integer.valueOf(Objects.requireNonNull(builder.getParameter("action")));
        switch (action) {
            case Action.GET_ORDER:
                doGet();
                break;
            case Action.INSERT_ORDER:
                doInsert();
                break;
            case Action.UPDATE_ORDER:
                doUpdate();
                break;
            case Action.DELETE_ORDER:
                doDelete();
                break;
            default:
                builder.setStatus(ResponseBuilder.STATUS_PARAM_ERROR);
        }
    }

    private void putResult(List<Order> orders){
        for (Order order: orders){
            putResult(order);
        }
    }

    private void putResult(Order order){
        builder.add(order);
        builder.setStatus(ResponseBuilder.STATUS_OK);
    }

    @Override
    protected void doGet() throws PerformException, IOException{
        try {
            String customer =  builder.getParameter("customer");
            String responsible =  builder.getParameter("responsible");
            boolean isActive = builder.getParameter("active") != null;
            String id = builder.getParameter("id");
            if(customer != null) {
                int customerId = Integer.valueOf(customer);
                if (isActive) {
                    putResult(dao.getActiveByCustomer(customerId));
                }else {
                    putResult(dao.getByCustomer(customerId));
                }
            }
            if(responsible != null) {
                int responsibleId = Integer.valueOf(responsible);
                if(isActive){
                    putResult(dao.getActiveByResponsible(responsibleId));
                } else {
                    putResult(dao.getByResponsible(responsibleId));
                }
            }
            if(id != null) {
                int orderId = Integer.valueOf(id);
                if (isActive){
                    putResult(dao.getActiveById(orderId));
                } else {
                    putResult(dao.select(new IntegerPrimaryKey(orderId)));
                }
            }
        } catch (NumberFormatException ex){
            throw new PerformException("Неверный формат данных");
        }
    }

    @Override
    protected void doInsert() throws PerformException, IOException {
        try {
            Order order = new Order(builder);
            order = dao.insert(order);
            if(order != null) {
                builder.add(order);
                builder.setStatus(ResponseBuilder.STATUS_OK);
            } else {
                builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
                builder.setText("Неудалось добавить заказ");
            }
        } catch (DBException e) {
            String mes = e.getMessage();
            if(mes.contains("foreign key")) {
                throw new PerformException("Поля Покупатель, Ответственный и Срочность должны быть выбраны с поля");
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
            Order order = new Order(builder);
            if(dao.update(order)) {
                builder.setStatus(ResponseBuilder.STATUS_OK);
            } else {
                builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
                builder.setText("Неудалось изменить заказ");
            }
        } catch (DBException e) {
            String mes = e.getMessage();
            builder.setText("exception");
            if(mes.contains("foreign key"))
                throw new PerformException("Поля Покупатель, Ответственный и Срок должны быть выбраны с поля");
            if(mes.contains("deactivated"))
                throw new PerformException("Невозможно обновить неактивный заказ");
            throw new PerformException("Ошибка обработки запроса");
        } catch (ValidationException e) {
            throw new PerformException("Ошибка формата данных");
        }
    }

    @Override
    protected void doDelete() throws PerformException, IOException{
        try {
            Order order = new Order(builder);
            if(dao.delete(order)) {
                builder.setStatus(ResponseBuilder.STATUS_OK);
            }else {
                builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
                builder.setText("Неудалось удалить заказ");
            }
        }catch (DBException e) {
            String msg = e.getMessage();
            if(msg.contains("deactivated"))
                throw new PerformException("Невозможно удалить неактивный заказ");
            throw new PerformException("Ошибка обработки запроса");
        } catch (ValidationException e) {
            throw new PerformException("Ошибка формата данных");
        }catch (NullPointerException e) {
            throw new PerformException("Не найдены нужные параметры");
        }

    }
}
