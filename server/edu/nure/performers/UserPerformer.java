package edu.nure.performers;

import edu.nure.db.Connector;
import edu.nure.db.RequestPreparing;
import edu.nure.db.entity.User;
import edu.nure.db.entity.constraints.ValidationException;
import edu.nure.email.EmailSender;
import edu.nure.performers.exceptions.PerformException;

import javax.mail.MessagingException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Random;

/**
 * Created by bod on 21.09.15.
 */
public class UserPerformer extends AbstractPerformer {

    public UserPerformer(ResponseBuilder b) throws SQLException{
        connection = Connector.getConnector().getConnection();
        setBuilder(b);
    }

    @Override
    public void perform() throws PerformException, IOException, SQLException {
        int action = builder.getAction();
        switch (action) {
            case Action.GET_USER:
                doGet();
                break;
            case Action.REGISTRY:
                doInsert();
                break;
            case Action.UPDATE_USER:
                doUpdate();
                break;
            default:
                builder.setStatus(ResponseBuilder.STATUS_PARAM_ERROR);
        }
    }

    @Override
    protected void doGet() throws PerformException, IOException, SQLException {
        try {
            String name =  builder.getParameter("name");
            String id =  builder.getParameter("id");
            String phone =  builder.getParameter("phone");
            String ajax = builder.getParameter("ajax");
            if(name != null) {
                ResultSet rs;
                if(ajax != null) {
                    if(builder.getParameter("hiRight") == null) {
                        rs = getStatement().executeQuery("Select * " +
                                "FROM `pmanager`.`USER` " +
                                "WHERE Name LIKE '%" + name + "%' Group BY `Name` ORDER BY Name LIMIT 15" );
                    }else{
                        rs = getStatement().executeQuery("Select * " +
                                "FROM `pmanager`.`USER` " +
                                "WHERE `Name` LIKE '%" + name + "%' AND `Right` = 'фотограф' ORDER BY `Name` LIMIT 15" );
                    }
                }else{
                    rs = getStatement().executeQuery(RequestPreparing.select("user", new String[]{"*"},
                            "WHERE Name = '" + name + "'"));
                }
                while (rs.next()){
                    User user = new User(rs);
                    builder.add(user);
                }
                builder.setStatus(ResponseBuilder.STATUS_OK);
                builder.setText(name);
                return;
            }
            if(phone != null) {
                ResultSet rs;
                if(ajax != null) {
                    phone = phone.replaceAll("[^0-9]","");
                    rs = getStatement().executeQuery(RequestPreparing.select("user", new String[]{"*"},
                            "Where replace(replace(replace(replace(replace(`Phone`,'+',''),'-',''),'(',''),')',''),' ','') " +
                            "Like '%" + phone + "%' ORDER BY Phone LIMIT 15"));
                }else{
                    rs = getStatement().executeQuery(RequestPreparing.select("user", new String[]{"*"},
                            "WHERE Phone = '" + phone + "'"));
                }
                while (rs.next()){
                    User user = new User(rs);
                    builder.add(user);
                }
                builder.setStatus(ResponseBuilder.STATUS_OK);
                return;
            }
            if(id != null) {
                ResultSet rs = getStatement().executeQuery(RequestPreparing.select("user", new String[]{"*"},
                            "WHERE Id = " + id + ""));
                while (rs.next()){
                    User user = new User(rs);
                    builder.add(user);
                }
                builder.setStatus(ResponseBuilder.STATUS_OK);
            }
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
            User user = new User(builder);
            String req = RequestPreparing.insert("user",
                    new String[]{"Name", "Phone","Email", "Right"},
                    new Object[]{user.getName(), user.getPhone(), user.getEmail(), user.getRight().getType().toLowerCase()}
            );
            int n = getStatement().executeUpdate(req);


            if(n > 0) {
                ResultSet rs = getStatement().executeQuery(RequestPreparing.select("`user` as U", new String[]{"*"},
                        RequestPreparing.join("`user_right` AS R", "INNER", "U.Right=R.Type")+"WHERE Phone = '" + user.getPhone() + "'"));
                rs.next();
                user = new User(rs);
                String aut_code = getCode(user);
                String s = RequestPreparing.insert("aut", new String[]{"Id", "Code"}, new Object[]{user.getId(), aut_code});
                getStatement().executeUpdate(s);
                if (user.getEmail() != null) {
                    EmailSender sender = new EmailSender();
                    sender.send("Photo Studio Registration", "Уважаемый " + user.getName() + "!\n" +
                                    "Вы воспользовались услугами нашей студии." +
                                    " Рекомендуем пройти по ссылке https://" + builder.getRequest().getServerName() + "/user?aut=" +
                                    aut_code +
                                    " для регистрацию и оценить все наши преимущества.\nХорошего дня!" +
                                    "Благодарим за доверие!!!",
                            user.getEmail());
                }

                builder.add(user);
                builder.setStatus(ResponseBuilder.STATUS_OK);
                return;
            }
            builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
            builder.setText("Неудалось добавить пользователя");
        } catch (SQLException e) {
            String msg = e.getMessage().toLowerCase();
            if(msg.contains("unique")) throw new PerformException("Пользователь с таким адрессом почты или телефоном" +
                    " уже зарегестрирован");
            else {
                if (msg.contains("foreign key"))
                    throw new PerformException("Права должны иметь значение Фотограф или Покупатель "+e.getMessage());

                else throw new PerformException("Ошибка обработки запроса: " + e.getMessage());
            }
        } catch (ValidationException e) {
            throw new PerformException("Ошибка формата данных: "+e.getMessage()+builder.getParameter("name"));
        }

    }

    @Override
    protected void doUpdate() throws PerformException, IOException, SQLException {
        try {
            String setpass = builder.getParameter("setpass");
            User user;
            try {
                user = User.getUserById(builder.getIntParameter("id"));
            }catch (NumberFormatException ex ){throw new PerformException("Ошибка формата данных");}

            int n = 0;
            if(setpass != null) {
                user.setPassword(builder.getParameter("password"));
                n = getStatement().executeUpdate(RequestPreparing.update("user",
                        new String[]{"Password"},
                        new Object[]{user.getPassword()},
                        "Id="+user.getId()
                ));
            }else{
                user = new User(builder);
                n = getStatement().executeUpdate(RequestPreparing.update("user",
                        new String[]{"Name", "Phone", "Email", "Right"},
                                new Object[]{user.getName(), user.getPhone(), user.getEmail(), user.getRight().getType().toLowerCase()},
                        "Id="+user.getId()
                ));
            }
            if(n > 0){
                builder.setStatus(ResponseBuilder.STATUS_OK);
                return;
            }
            builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
            builder.setText("Неудалось изменить пользователя");
        } catch (SQLException e) {
            String msg = e.getMessage().toLowerCase();
            if(msg.contains("unique")) throw new PerformException("Пользователь с таким адрессом почты или телефоном" +
                    " уже зарегестрирован");
            else {
                if (msg.contains("foreign key"))
                    throw new PerformException("Права должны иметь значения Фотограф или Покупатель"+e.getMessage());

                else throw new PerformException("Ошибка обработки запроса: " + e.getMessage());
            }
        } catch (ValidationException e) {
            throw new PerformException("Ошибка формата данных");
        }
    }

    private String getCode(User user){
        try {
            String code = new BigInteger(MessageDigest.getInstance("MD5").digest(
                    (new Date().getTime()+""+user.getId()+user.getPhone()+user.getName()+(1000000+new Random().nextLong())).getBytes()
            )).toString(16);
            return code;
        } catch (NoSuchAlgorithmException e) {
            return new BigInteger(new Date().getTime()+""+user.getName()+user.getPhone()+user.getId()+(1000000+
                    new Random().nextLong())).toString(16);
        }

    }
}
