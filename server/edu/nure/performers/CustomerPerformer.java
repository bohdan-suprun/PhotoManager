package edu.nure.performers;

import edu.nure.Manager;
import edu.nure.UserManager;
import edu.nure.db.Connector;
import edu.nure.db.entity.User;
import edu.nure.db.entity.constraints.ValidationException;
import edu.nure.db.entity.constraints.Validator;
import edu.nure.performers.exceptions.PerformException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.AccessDeniedException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Created by bod on 26.09.15.
 */
public class CustomerPerformer extends AbstractPerformer{
    private final HashMap<String, String> ACCESS = new HashMap<String, String>();
    {
        ACCESS.put("registry.html", "aut");
        ACCESS.put("gallery.html", "id");
    }
    private HttpServletResponse response;


    public CustomerPerformer(ResponseBuilder b, HttpServletResponse response) throws SQLException{
        connection = Connector.getConnector().getConnection();
        builder = b;
        this.response = response;
    }

    @Override
    public void perform() throws PerformException, IOException, SQLException {
        String ac = builder.getParameter("action");
        if (ac != null) {
            int action = Integer.valueOf(ac);
            switch (action) {
                case Action.CUSTOMER_SET_PASS:
                    if (builder.getRequest().getSession().getAttribute("aut") != null) {
                        setPass();
                        response.sendRedirect("https://" + UserManager.hostName + "/user/login.html");
                    } else throw new AccessDeniedException("Access denied");
                    break;
                case Action.CUSTOMER_LOGIN:
                    if (login()) {
                        response.sendRedirect("https://" + UserManager.hostName + "/user/gallery/gallery.html");
                    } else {
                        response.sendRedirect("https://" + UserManager.hostName + "/user/login_fail.html");
                    }
                    break;
                case Action.CUSTOMER_AJAX:
                    if (!Manager.checkLowRight(builder.getRequest().getSession()))
                        throw new AccessDeniedException("Access Denied");
                    ajax();
                    break;
                case Action.KILL_SESSION:
                    builder.getRequest().getSession().invalidate();
                    response.sendRedirect("https://" + UserManager.hostName);
            }
        } else {
            if (builder.getParameter("aut") != null) {
                aut();
                return;
            } //otherwise is loading a file

            String filePath = builder.getRequest().getPathTranslated();
            try {
                if (!checkForFileAccess(builder.getRequest().getSession(), filePath))
                    throw new AccessDeniedException("Access Denied");
                String requiredParam = ACCESS.get(new File(filePath).getName());

                if (requiredParam != null) {
                    User user = User.getUserById(Integer.valueOf(builder.getRequest().getSession().getAttribute(requiredParam).toString()));
                    builder.add(getFile(filePath, user));
                } else {
                    builder.add(getFile(filePath));
                }
            } catch (SQLException e) {
                throw new PerformException();
            } catch (ValidationException e) {
                throw new PerformException();
            }
        }
    }

    private byte[] getFile(String filename) throws PerformException {
        try {
            FileInputStream in = new FileInputStream(new File(filename));
            byte[] buf = new byte[in.available()];
            in.read(buf);
            if (filename.contains(".html") || filename.contains(".js")) {
                buf = new String(buf).replace("{hostname}", UserManager.hostName).getBytes();
            }
            return buf;
        }catch (IOException ex){
            throw new PerformException("File not found");
        }
    }

    private byte[] getFile(String filename, User user) throws PerformException {

        try {
            FileInputStream in =  new FileInputStream(new File(filename));
            byte[] buf = new byte[in.available()];
            in.read(buf);
            String s = new String(buf);
            if(filename.contains(".html") || filename.contains(".js")) {
                String replace[] = {"{phone}", "{hostname}", "{username}"};
                String to[] = {user.getPhone(), UserManager.hostName, user.getName()};
                for (int i = 0; i < replace.length; i++) {
                    s = s.replace(replace[i], to[i]);
                }
            }
            return s.getBytes();
        } catch (IOException e) {
            throw new PerformException("File not found");
        }

    }

    private void aut() throws IOException {
        try {
            String reg = Objects.requireNonNull(builder.getParameter("aut"));
            ResultSet rs = getConnection().createStatement().executeQuery(
                    "Select * FROM `AUT` INNER JOIN `USER` USING(Id) WHERE `Code` = '"+reg+"' AND `Password` is null;"
            );
            if(rs.next()){
                User user = new User(rs);
                getConnection().createStatement().executeUpdate(
                        "DELETE FROM `AUT` WHERE `Id` = '" + user.getId() + "';"
                );
                builder.getRequest().getSession().setAttribute("aut", String.valueOf(user.getId()));
                response.sendRedirect("https://"+UserManager.hostName+"/user/registry.html");
            }else
                throw new NullPointerException();
        } catch (NullPointerException ex){
            response.setStatus(505);
            response.getWriter().print(UserManager.ACCESS_DENIED_HTML);
        } catch (SQLException e) {
            response.setStatus(500);
            response.getWriter().print(UserManager.INNER_ERROR_HTML);
        } catch (ConnectException e) {
            response.setStatus(500);
        } catch (ValidationException e) {
            response.setStatus(500);
            response.getWriter().print(UserManager.INNER_ERROR_HTML);
        } catch (IOException e) {
            response.setStatus(500);
            response.getWriter().print(UserManager.INNER_ERROR_HTML);
        }

    }

    private void setPass() throws AccessDeniedException {
        try {
            String phone = builder.getParameter("phone");
            String password = builder.getParameter("password");
            int id = Integer.valueOf(builder.getRequest().getSession().getAttribute("aut").toString());
            phone = Validator.validate(phone, Validator.PHONE_VALIDATOR);
            int n = getConnection().createStatement().executeUpdate(
                    "UPDATE `pmanager`.`USER` SET `Password` = '" + password+"' " +
                            "Where `Id`="+id+" AND `Password` is NULL AND `Phone` = '"+phone+"';"
            );
            if(n < 1) throw new AccessDeniedException("Access Denied");
        } catch (SQLException e) {
            throw new AccessDeniedException("Access Denied");
        }  catch (ValidationException e) {
            throw new AccessDeniedException("Неверный номер телефона");
        }

    }

    private boolean login(){
        try {
            String password = builder.getParameter("password");
            String phone = builder.getParameter("phone");

            phone = Validator.validate(phone, Validator.PHONE_VALIDATOR);

            ResultSet rs = getConnection().createStatement().executeQuery(
                    "Select * FROM `pmanager`.`USER` WHERE `Password` = '" + password+"' AND " +
                            "`Phone` = '"+phone+"' AND Password IS NOT NULL;"
            );
            getStatement().execute("CALL delete_root();");
            if(rs.next()){
                User user = new User(rs);
                builder.getRequest().getSession().setAttribute("id", String.valueOf(user.getId()));
                builder.getRequest().getSession().setAttribute("right", user.getRight().getType());
                return true;
            }else return false;
        } catch (SQLException e) {
            return false;
        } catch (ConnectException e) {
            return false;
        } catch (ValidationException e) {
            return false;
        }

    }

    private void ajax() throws PerformException {
        // Выбрать айдишники альбома
        final String TEMPLATE_IMG = "{\"alt\":\"/a/\", \"src\":\"/s/\"}";
        final String TEMPLATE_SRC = "https://"+UserManager.hostName+"/image/?action=201&albumId=/album/"+
                "&id=/id/&preview";
        try {
            ResultSet rs = getConnection().createStatement().executeQuery(
                    "Select `Name`, i.`Id`, `Album` From `ALBUM` as a " +
                            "INNER JOIN `IMAGE` as i on i.Album = a.Id" +
                            " Where `UserId` = " +builder.getRequest().getSession().getAttribute("id")+
                            " Group by `Name`, i.`Id`, `Album`;"
            );
            HashMap<String, List<String>> json = new HashMap<String, List<String>>();
            while (rs.next()){
                String album = rs.getString("Name");
                String id = rs.getString("Id");
                String aId = rs.getString("Album");
                List<String> images = json.get(album);
                if(images == null)
                    images = new LinkedList<String>();
                images.add(
                        TEMPLATE_IMG.replace("/a/", album).replace(
                                "/s/", TEMPLATE_SRC.replace("/album/", aId).replace("/id/", id)
                        )
                );
                json.put(album, images);
            }
            builder.add(prepareJson(json));
        } catch (SQLException e) {
            throw new PerformException();
        }
    }

    private byte[] prepareJson(HashMap<String, List<String>> pJson){
        StringBuilder result = new StringBuilder("{\n");
        for(String k: pJson.keySet()){
            result.append("\"" + k + "\":[");
            for(String item: pJson.get(k)){
                result.append(item+ ", ");
            }
            result.append("],");
            result = new StringBuilder(result.toString().replace("}, ]", "}]"));
        }
        return result.append("}").toString().replace("],}","]}").getBytes();
    }

    private boolean checkForFileAccess(HttpSession session, String fileName) {
        for(String k: ACCESS.keySet()){
            if(fileName.contains(k)){
                return session.getAttribute(ACCESS.get(k)) != null;
            }
        }
        return true;
    }
}
