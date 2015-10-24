package edu.nure.performers;

import edu.nure.db.Connector;
import edu.nure.db.RequestPreparing;
import edu.nure.db.entity.Stock;
import edu.nure.db.entity.constraints.ValidationException;
import edu.nure.performers.exceptions.PerformException;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by bod on 24.09.15.
 */
public class StockPerformer extends AbstractPerformer {

    public StockPerformer(ResponseBuilder b) throws SQLException{
        connection = Connector.getConnector().getConnection();
        setBuilder(b);
    }

    @Override
    public void perform() throws PerformException, IOException, SQLException {
        int action = builder.getIntParameter("action");
        switch (action) {
            case Action.GET_STOCK:
                doGet();
                break;
            case Action.INSERT_STOCK:
                doInsert();
                break;
            case Action.UPDATE_STOCK:
                doUpdate();
                break;
            case Action.DELETE_STOCK:
                doDelete();
                break;
            default:
                builder.setStatus(ResponseBuilder.STATUS_PARAM_ERROR);
        }
    }

    @Override
    protected void doGet() throws PerformException, IOException, SQLException {
        try {
            String order =  builder.getParameter("order");
            String id = builder.getParameter("id");
            if(order != null) {
                String req = RequestPreparing.select("`stock`", new String[]{"*"},
                        "WHERE `Id_order` = " + Integer.valueOf(order));
                ResultSet rs = getStatement().executeQuery(req);
                while (rs.next()){
                    Stock stock = new Stock(rs);
                    builder.add(stock);
                }
                builder.setStatus(ResponseBuilder.STATUS_OK);
                return;
            }
            if(id != null){
                ResultSet rs = getStatement().executeQuery(RequestPreparing.select("stock", new String[]{"*"},
                    "WHERE Id = " + Integer.valueOf(id)));
                while (rs.next()){
                    Stock stock = new Stock(rs);
                    builder.add(stock);
                }
                builder.setStatus(ResponseBuilder.STATUS_OK);
            }
        } catch (SQLException e) {
            throw new PerformException("Ошибка при работе с базой данных");
        } catch (ValidationException e) {
            throw new PerformException("Неверный формат данных");
        } catch (NumberFormatException ex){
            throw new PerformException("Неверный формат данных");
        }
    }

    @Override
    protected void doInsert() throws PerformException, IOException, SQLException {
        try {
            Stock stock = new Stock(builder);
            int n = getStatement().executeUpdate(RequestPreparing.insert("stock", Stock.getFields(),
                    new Object[]{stock.getOrder(), stock.getImage(), stock.getDesc(),
                            stock.getFormat()}));
            if(n > 0) {
                builder.add(new Stock(getLastInserted("stock")));
                builder.setStatus(ResponseBuilder.STATUS_OK);
                return;
            }
            builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
            builder.setText("Неудалось добавить задание");
        } catch (SQLException e) {
            String mes = e.getMessage();
            if(mes.contains("foreign key")) {
                throw new PerformException("Поля Заказ, Изображение и Формат должны быть выбраны с поля");
            }else{
                throw new PerformException("Ошибка обработки запроса");
            }
        } catch (ValidationException e) {
            throw new PerformException("Ошибка формата данных");
        }
    }

    @Override
    protected void doUpdate() throws PerformException, IOException, SQLException {
        try {
            Stock stock = new Stock(builder);
            int n = getStatement().executeUpdate(RequestPreparing.update("stock", Stock.getFields(),
                    new Object[]{stock.getOrder(), stock.getImage(), stock.getDesc(),
                            stock.getFormat()}, "Id=" + stock.getId()));
            if(n > 0) {
                builder.setStatus(ResponseBuilder.STATUS_OK);
                return;
            }
            builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
            builder.setText("Неудалось изменить задание");
        } catch (SQLException e) {
            String mes = e.getMessage();
            if(mes.contains("foreign key")) {
                throw new PerformException("Поля Заказ, Изображение и Формат должны быть выбраны с поля");
            }else{
                throw new PerformException("Ошибка обработки запроса");
            }
        } catch (ValidationException e) {
            throw new PerformException("Ошибка формата данных");
        }
    }

    @Override
    protected void doDelete() throws PerformException, IOException, SQLException {
        try {
            Stock stock = new Stock(builder);
            int n = getStatement().executeUpdate("DELETE FROM `pmanager`.`STOCK` WHERE `Id`=" + stock.getId());
            if(n > 0) {
                builder.setStatus(ResponseBuilder.STATUS_OK);
                return;
            }
            builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
            builder.setText("Неудалось удалить задание");
        } catch (SQLException e) {
            throw new PerformException("Ошибка обработки запроса");
        } catch (ValidationException e) {
            throw new PerformException("Ошибка формата данных");
        }

    }
}
