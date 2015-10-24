package edu.nure.performers;

import edu.nure.db.Connector;
import edu.nure.db.RequestPreparing;
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

    public LoginPerformer(ResponseBuilder b) throws SQLException{
        connection = Connector.getConnector().getConnection();
        setBuilder(b);
    }

    @Override
    public void perform() throws PerformException, IOException, SQLException {

        try {
            Statement st = getStatement();
            String phone = Validator.validate(Objects.requireNonNull(builder.getParameter("phone")), Validator.PHONE_VALIDATOR);
            String pass = Objects.requireNonNull(builder.getParameter("password"));

            String DB_REQ = RequestPreparing.select("user as U", new String[]{"*"}, RequestPreparing.join("user_right as UR",
                    "inner","U.Right = UR.Type")+"WHERE Phone='"+phone+"' and Password='"+pass+"' AND Password IS NOT NULL");
            ResultSet result = st.executeQuery(DB_REQ);
            if(!result.last()) {
                builder.setStatus(ResponseBuilder.STATUS_ERROR_READ);
                builder.setText("Пользователя с таким номером телефона не существует, или пароль не верен");
            }
            else{
                User user = new User(result);
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
        } catch (ConnectException e) {
            throw new PerformException("Some problems with database"+e.getMessage());
        } catch (NullPointerException ex){
            throw new PerformException("Parameter count doesn't match"+ex.getMessage());
        } catch (SQLException e) {
            throw new PerformException("Invalid request to Database");
        } catch (ValidationException e) {
            throw new PerformException("Validation error");
        }

    }
}
