package edu.nure;

import edu.nure.db.dao.DBException;
import edu.nure.performers.AbstractPerformer;
import edu.nure.performers.CustomerPerformer;
import edu.nure.performers.ResponseBuilder;
import edu.nure.performers.exceptions.PerformException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.sql.SQLException;

/**
 * Created by bod on 26.09.15.
 */
public class UserManager extends HttpServlet {
    public static String hostName = "127.0.0.1";
    public static final String ACCESS_DENIED_HTML="<html><head><title>Access Denied</title></head><body>" +
            "<h1><b>505 Access denied</b></h1></body></html>";
    public static final String INNER_ERROR_HTML="<html><head><title>Inner error</title></head><body>" +
            "<h1><b>500 Inner server error</b></h1></body></html>";
    public static final String FILE_NOT_FOUND_HTML="<html><head><title>File not found</title></head><body>" +
            "<h1><b>404<br/> Requested file not found</b></h1></body></html>";
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ResponseBuilder builder = new ResponseBuilder(req, -1);
        hostName = req.getServerName();
        AbstractPerformer p = null;
        try {
            builder.setContentType(req.getContentType());
            p = new CustomerPerformer(builder, resp);
            p.perform();
        }catch (PerformException ex){
            if(ex.getMessage()!= null && ex.getMessage().contains("File not")){
                resp.setStatus(404);
                builder.add(INNER_ERROR_HTML);
            }else {
                resp.setStatus(500);
                builder.add(INNER_ERROR_HTML);
            }
        } catch (AccessDeniedException ex) {
            resp.setStatus(505);
            builder.add(ACCESS_DENIED_HTML);
        } catch (DBException ex){
            resp.setStatus(500);
            builder.add(INNER_ERROR_HTML);

        } finally {
            resp.setContentType(builder.getContentType());
            builder.writeTo(resp.getOutputStream());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       doGet(req, resp);

    }

}
