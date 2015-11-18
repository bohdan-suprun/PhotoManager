package edu.nure.performers;

import edu.nure.db.Connector;
import edu.nure.db.RequestPreparing;
import edu.nure.db.dao.AbstractDAOFactory;
import edu.nure.db.dao.DBException;
import edu.nure.db.dao.domains.interfaces.UserDAO;
import edu.nure.db.entity.User;
import edu.nure.db.entity.constraints.ValidationException;
import edu.nure.db.entity.constraints.Validator;
import edu.nure.performers.exceptions.PerformException;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/**
 * Created by bod on 18.09.15.
 */
public class LoginPerformer extends AbstractPerformer {
    private UserDAO dao;

    public LoginPerformer(ResponseBuilder b) throws DBException {
        super(b);
        dao = AbstractDAOFactory.getDAO(AbstractDAOFactory.MYSQL).getUserDAO();
    }

    @Override
    public void perform() throws PerformException, IOException{

        try {
            String phone = Validator.validate(Objects.requireNonNull(builder.getParameter("phone")), Validator.PHONE_VALIDATOR);
            String pass = Objects.requireNonNull(builder.getParameter("password"));
            User user = dao.login(phone, pass);
            if(user != null) {
                if((user.getRight().getType().equals("фотограф") ||
                        user.getRight().getType().equals("su"))) {

                    builder.add(user);
                    builder.getRequest().getSession().setAttribute("id", user.getId());
                    builder.getRequest().getSession().setAttribute("right", user.getRight().getType());
                    builder.setStatus(ResponseBuilder.STATUS_OK);
                }else{
                    throw new PerformException("Ошибка прав доступа.");
                }
            }
            else{
                builder.setStatus(ResponseBuilder.STATUS_ERROR_READ);
                builder.setText("Пользователя с таким номером телефона не существует, или не верен пароль");
            }
        } catch (NullPointerException ex){
            throw new PerformException("Parameter count doesn't match");
        } catch (ValidationException e) {
            throw new PerformException("Validation error");
        }
    }
}
