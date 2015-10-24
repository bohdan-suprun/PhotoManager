package edu.nure;

import edu.nure.performers.Action;
import edu.nure.performers.ImagePerformer;
import edu.nure.performers.ResponseBuilder;
import edu.nure.performers.exceptions.PerformException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by bod on 21.09.15.
 */
public class ImageManager extends HttpServlet {
    DiskFileItemFactory factory;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int action;
        Connection conn = null;
        if(ServletFileUpload.isMultipartContent(req))
            action = Action.INSERT_IMAGE;
        else action = Integer.valueOf(req.getParameter("action"));

        ResponseBuilder builder = new ResponseBuilder(req, action);
        try {
            if(action == Action.GET_IMAGE)
                if(!Manager.checkLowRight(req.getSession())) throw new AccessDeniedException("Ошибка прав доступа");
            if(action == Action.DELETE_IMAGE || action == Action.INSERT_IMAGE)
                if(!Manager.checkHiRight(req.getSession())) throw new AccessDeniedException("Ошибка прав доступа");

            ImagePerformer p = new ImagePerformer(builder, factory);
            conn = p.getConnection();
            p.perform();
        } catch (PerformException e) {
            builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
            builder.setText(e.getMessage());
        } catch (AccessDeniedException e) {
            builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
            builder.setText(e.getMessage());
        } catch (SQLException ex){
            builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
            builder.setText(ex.getMessage());
        } finally {
            resp.setContentType(builder.getContentType());
            builder.writeTo(resp.getOutputStream());
        }
    }

    @Override
    public void init() throws ServletException {
    
        factory = new DiskFileItemFactory();
        
    }
}
