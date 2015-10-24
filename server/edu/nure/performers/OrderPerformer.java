package edu.nure.performers;

import edu.nure.db.Connector;
import edu.nure.db.RequestPreparing;
import edu.nure.db.entity.Order;
import edu.nure.db.entity.User;
import edu.nure.db.entity.constraints.ValidationException;
import edu.nure.email.EmailSender;
import edu.nure.performers.exceptions.PerformException;

import javax.mail.MessagingException;
import java.io.IOException;
import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Objects;

/**
 * Created by bod on 21.09.15.
 */
public class OrderPerformer extends AbstractPerformer {

    public OrderPerformer(ResponseBuilder b) throws SQLException{
        connection = Connector.getConnector().getConnection();
        setBuilder(b);
    }

    @Override
    public void perform() throws PerformException, IOException, SQLException {
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
    private void exec(String name, String value, boolean active) throws ConnectException, SQLException, ValidationException {
        String s = RequestPreparing.select("`order` AS O", new String[]{"*"},
                "WHERE "+name+" = " + Integer.valueOf(value)+" " + ((active)?"AND Status=1":"")+" Order by O.Urg");
        ResultSet rs = getStatement().executeQuery(s);
        int i = 0;
        while (rs.next()){
            Order order = new Order(rs);
            builder.add(order);
            builder.setStatus(ResponseBuilder.STATUS_OK);
        }


    }

    @Override
    protected void doGet() throws PerformException, IOException, SQLException {
        try {
            String customer =  builder.getParameter("customer");
            String responsible =  builder.getParameter("responsible");
            String isActive = builder.getParameter("active");
            String id = builder.getParameter("id");
            builder.setText(customer+" "+responsible);
            if(customer != null) {
                exec("Customer", customer, isActive != null);
                return;
            }
            if(responsible != null) {
                exec("Responsible", responsible, isActive != null);
                return;
            }
            if(id != null) {
                exec("Id", id, isActive != null);
            }
        } catch (SQLException e) {
            throw new PerformException("Ошибка при работе с базой данных " + e.getMessage());
        } catch (ValidationException e) {
            throw new PerformException("Неверный формат данных");
        } catch (NumberFormatException ex){
            throw new PerformException("Неверный формат данных");
        }
    }

    @Override
    protected void doInsert() throws PerformException, IOException, SQLException {
        try {
            Order order = new Order(builder);

            String s = RequestPreparing.insert("order",Order.getFields(),
                    new Object[]{order.getCustomer(), order.getResponsible(), order.getDesc(),
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(order.getTerm()), order.getForPay(), order.getStatus(),
                    order.getUrgency()});
            int n = getStatement().executeUpdate(s);

            if(n > 0) {
                builder.add(new Order(getLastInserted("order")));
                builder.setStatus(ResponseBuilder.STATUS_OK);
                return;
            }
            builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
            builder.setText("Неудалось добавить заказ");
        } catch (SQLException e) {
            String mes = e.getMessage();
            if(mes.contains("foreign key")) {
                throw new PerformException("Поля Покупатель, Ответственный и Срочность должны быть выбраны с поля");
            }else{
                throw new PerformException("Ошибка обработки запроса: "+mes);
            }
        } catch (ValidationException e) {
            throw new PerformException("Ошибка формата данных");
        }
    }

    @Override
    protected void doUpdate() throws PerformException, IOException, SQLException {
        try {
            Order order = new Order(builder);
            int n = getStatement().executeUpdate(RequestPreparing.update("order", Order.getFields(),
                    new Object[]{order.getCustomer(), order.getResponsible(), order.getDesc(),
                            order.getTerm(), order.getForPay(), order.getStatus()}, "Id=" + order.getId()));
            if(n > 0) {
                builder.setStatus(ResponseBuilder.STATUS_OK);
                return;
            }
            builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
            builder.setText("Неудалось изменить заказ");
        } catch (SQLException e) {
            String mes = e.getMessage();
            if(mes.contains("foreign key")) {
                throw new PerformException("Поля Покупатель, Ответственный и Срок должны быть выбраны с поля");
            }else{
                throw new PerformException("Ошибка обработки запроса: "+mes);
            }
        } catch (ValidationException e) {
            throw new PerformException("Ошибка формата данных");
        }
    }

    @Override
    protected void doDelete() throws PerformException, IOException, SQLException {
        try {
            String confirm = builder.getParameter("confirm");
            final Order order = new Order(builder);
            int n = getStatement().executeUpdate("DELETE FROM `pmanager`.`ORDER` WHERE `Id`="+order.getId());
            if(n > 0) {
                builder.setStatus(ResponseBuilder.STATUS_OK);
                if (confirm != null){
                    final User user = User.getUserById(order.getCustomer());
                    if (order.getStatus() == 1 && user.getEmail() != null) {
                            EmailSender sender = new EmailSender();
                            sender.send("Photo Studio Order notify", "Уважаемый " + user.getName() + "\n" +
                                            "Ваш заказ был удален. Рекомендуем обратится в нашу студию для уточнения объстоятельств.",
                                    user.getEmail());
                    }
                }
            }else {
                builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
                builder.setText("Неудалось удалить заказ");
            }
        }catch (SQLException e) {
            throw new PerformException("Ошибка обработки запроса: "+e.getMessage());
        } catch (ValidationException e) {
            throw new PerformException("Ошибка формата данных");
        }catch (NullPointerException e) {
            throw new PerformException("Не найдены нужные параметры");
        }

    }
}
