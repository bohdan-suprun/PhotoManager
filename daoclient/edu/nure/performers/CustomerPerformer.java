package edu.nure.performers;

import edu.nure.Manager;
import edu.nure.UserManager;
import edu.nure.db.Connector;
import edu.nure.db.dao.AbstractDAOFactory;
import edu.nure.db.dao.DAOFactory;
import edu.nure.db.dao.DBException;
import edu.nure.db.dao.MySQLDAOFactory;
import edu.nure.db.dao.domains.interfaces.AlbumDAO;
import edu.nure.db.dao.domains.interfaces.UserDAO;
import edu.nure.db.entity.Album;
import edu.nure.db.entity.Image;
import edu.nure.db.entity.User;
import edu.nure.db.entity.constraints.ValidationException;
import edu.nure.db.entity.constraints.Validator;
import edu.nure.db.entity.primarykey.IntegerPrimaryKey;
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
import java.util.*;

/**
 * Created by bod on 26.09.15.
 */
public class CustomerPerformer extends AbstractPerformer{
    private final HashMap<String, String> ACCESS = new HashMap<String, String>();
    {
        // requested page and required parameter in url
        // used for input user name, phone, etc.
        // replaces {hostname}, {username}, {userphone} actual values
        ACCESS.put("registry.html", "aut");
        ACCESS.put("gallery.html", "id");
    }
    private HttpServletResponse response;
    private DAOFactory dao;

    public CustomerPerformer(ResponseBuilder b, HttpServletResponse response) throws DBException {
        super(b);
        this.response = response;
        dao = AbstractDAOFactory.getDAO(AbstractDAOFactory.MYSQL);
    }

    @Override
    public void perform() throws PerformException, IOException {
        String ac = builder.getParameter("action");
        // if it is a handle request
        if (ac != null) {
            int action = Integer.valueOf(ac);
            switch (action) {
                // customer going to sign up
                case Action.CUSTOMER_SET_PASS:
                    if (builder.getRequest().getSession().getAttribute("aut") != null) {
                        setPass();
                        response.sendRedirect("https://" + UserManager.hostName + "/user/login.html");
                    } else throw new AccessDeniedException("Access denied");
                    break;
                // customer log in
                case Action.CUSTOMER_LOGIN:
                    if (login()) {
                        response.sendRedirect("https://" + UserManager.hostName + "/user/gallery/gallery.html");
                    } else {
                        response.sendRedirect("https://" + UserManager.hostName + "/user/login_fail.html");
                    }
                    break;
                // returns JSON file with albums and images
                case Action.CUSTOMER_AJAX:
                    if (!Manager.checkLowRight(builder.getRequest().getSession())) {
                        throw new AccessDeniedException("Access Denied");
                    }
                    ajax();
                    break;
                // log out
                case Action.KILL_SESSION:
                    builder.getRequest().getSession().invalidate();
                    response.sendRedirect("https://" + UserManager.hostName);
            }
            // if action is not exist in url
        } else {
            // user follow the email link
            if (builder.getParameter("aut") != null) {
                aut();
                return;
            }
            //otherwise is loading a file
            String filePath = builder.getRequest().getPathTranslated();

            try {
                if (!checkForFileAccess(builder.getRequest().getSession(), filePath))
                    throw new AccessDeniedException("Access Denied");

                String requiredParam = ACCESS.get(new File(filePath).getName());

                // if need to replace {username}, {userphone} actual info
                if (requiredParam != null) {
                    User user = dao.getUserDAO().select(new IntegerPrimaryKey(Integer.valueOf(builder.getRequest().getSession()
                            .getAttribute(requiredParam).toString())));
                    builder.add(getFile(filePath, user));
                } else {
                    builder.add(getFile(filePath));
                }
            } catch (NullPointerException e) {
                throw new PerformException("Такого пользователя не существут");
            } catch (DBException ex){
                throw new PerformException("Ошибка при обработке запроса");
            }
        }
    }

    private byte[] getFile(String filename) throws PerformException {
        // load file and replace {hostname} only
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
        // load file and insert actual user info
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
            // checks authentication code
            String regCode = Objects.requireNonNull(builder.getParameter("aut"));
            User user = dao.getUserDAO().authenticate(regCode);
            builder.getRequest().getSession().setAttribute("aut", String.valueOf(user.getId()));
            response.sendRedirect("https://"+UserManager.hostName+"/user/registry.html");

        } catch (NullPointerException ex){
            response.setStatus(505);
            response.getWriter().print(UserManager.ACCESS_DENIED_HTML);
        } catch (DBException e) {
            response.setStatus(500);
            response.getWriter().print(UserManager.INNER_ERROR_HTML);
        }
    }

    private void setPass() throws AccessDeniedException {
        try {
            String password = builder.getParameter("password");
            int id = Integer.valueOf(builder.getRequest().getSession().getAttribute("aut").toString());

            if (!dao.getUserDAO().setPassword(id, password)) {
                throw new AccessDeniedException("Access Denied");
            }
        } catch (DBException e) {
            throw new AccessDeniedException("Access Denied");
        }

    }

    private boolean login(){
        try {
            String password = builder.getParameter("password");
            String phone = builder.getParameter("phone");

            phone = Validator.validate(phone, Validator.PHONE_VALIDATOR);
            User user = dao.getUserDAO().login(phone, password);
            if (user != null) {
                // add attributes into session
                builder.getRequest().getSession().setAttribute("id", String.valueOf(user.getId()));
                builder.getRequest().getSession().setAttribute("right", user.getRight().getType());
                return true;
            } else {
                return false;
            }
        } catch (ValidationException e) {
            return false;
        } catch (DBException ex){
            return false;
        }

    }

    private void ajax() throws PerformException {
        // entries templates
        final String TEMPLATE_IMG = "{\"alt\":\"/a/\", \"src\":\"/s/\"}";
        final String TEMPLATE_SRC = "https://"+UserManager.hostName+"/image/?action=201&albumId=/album/"+
                "&id=/id/&preview";
        try {
            AlbumDAO albumDAO = dao.getAlbumDAO();
            int userId = Integer.valueOf(builder.getRequest().getSession().getAttribute("id").toString());
            Map<Album, List<Image>> albums = albumDAO.getUserAlbums(userId);
            HashMap<String, List<String>> json = new HashMap<String, List<String>>();
            for(Album album: albums.keySet()){
                json.put(album.getName(), new ArrayList<String>());
                String albumId = String.valueOf(album.getId());
                for (Image im: albums.get(album)) {
                    String imageId = String.valueOf(im.getId());
                    List<String> container = json.get(album.getName());
                    container.add(
                            TEMPLATE_IMG.replace("/a/", album.getName()).replace(
                                    "/s/", TEMPLATE_SRC.replace("/album/", albumId).replace("/id/", imageId)
                            )
                    );
                }            }
            builder.add(prepareJson(json));
        } catch (DBException e) {
            throw new PerformException();
        }
    }

    private byte[] prepareJson(HashMap<String, List<String>> pJson){
        // create JSON file using hash map
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
