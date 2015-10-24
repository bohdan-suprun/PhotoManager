package edu.nure;


import edu.nure.performers.*;
import edu.nure.performers.exceptions.PerformException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.util.Objects;


/**
 * Created by bod on 15.09.15.
 */
public class Manager extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ResponseBuilder builder = new ResponseBuilder(null, -1);
        try {
            resp.setCharacterEncoding("UTF-8");
            req.setCharacterEncoding("UTF-8");

            int action = Integer.valueOf(Objects.requireNonNull(req.getParameter("action")));
            builder = new ResponseBuilder(req, action);
            builder.setContentType(ResponseBuilder.XML_TYPE);
            if(action == Action.LOGIN){
                new LoginPerformer(builder).perform();
            }else {
                if(!checkHiRight(req.getSession())) throw new AccessDeniedException("Ошибка прав доступа");
                switch (action) {
                    case Action.REGISTRY:
                        new UserPerformer(builder).perform();
                        break;
                    case Action.GET_USER:
                        new UserPerformer(builder).perform();
                        break;
                    case Action.UPDATE_USER:
                        new UserPerformer(builder).perform();
                        break;
                    case Action.KILL_SESSION:
                        req.getSession().invalidate();
                        builder.setStatus(ResponseBuilder.STATUS_OK);
                        break;

                        //Format
                    case Action.INSERT_FORMAT:
                        new FormatPerformer(builder).perform();
                        break;
                    case Action.GET_FORMAT:
                        new FormatPerformer(builder).perform();
                        break;
                    case Action.UPDATE_FORMAT:
                        new FormatPerformer(builder).perform();
                        break;
                    case Action.DELETE_FORMAT:
                        new FormatPerformer(builder).perform();
                        break;
                    //Format END

                    case Action.INSERT_ALBUM:
                        new AlbumPerformer(builder).perform();
                        break;
                    case Action.GET_ALBUM:
                        new AlbumPerformer(builder).perform();
                        break;
                    case Action.DELETE_ALBUM:
                        new AlbumPerformer(builder).perform();
                        break;

                    //Right
                    case Action.GET_RIGHT:
                        new RightPerformer(builder).perform();
                        break;
                    //Urgency
                    case Action.INSERT_URGENCY:
                        new UrgencyPerformer(builder).perform();
                        break;
                    case Action.GET_URGENCY:
                        new UrgencyPerformer(builder).perform();
                        break;
                    case Action.UPDATE_URGENCY:
                        new UrgencyPerformer(builder).perform();
                        break;
                    case Action.DELETE_URGENCY:
                        new UrgencyPerformer(builder).perform();
                        break;
                    //Urgency END

                    //Order
                    case Action.INSERT_ORDER:
                        new OrderPerformer(builder).perform();
                        break;
                    case Action.GET_ORDER:
                        new OrderPerformer(builder).perform();
                        break;
                    case Action.UPDATE_ORDER:
                        new OrderPerformer(builder).perform();
                        break;
                    case Action.DELETE_ORDER:
                        new OrderPerformer(builder).perform();
                        break;
                    //Order END

                    //Stock
                    case Action.INSERT_STOCK:
                        new StockPerformer(builder).perform();
                        break;
                    case Action.GET_STOCK:
                        new StockPerformer(builder).perform();
                        break;
                    case Action.UPDATE_STOCK:
                        new StockPerformer(builder).perform();
                        break;
                    case Action.DELETE_STOCK:
                        new StockPerformer(builder).perform();
                        break;
                    //Stock END
                    default:
                        builder.setStatus(ResponseBuilder.STATUS_PARAM_ERROR);

                }
            }
        } catch (PerformException ex){
            builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
            builder.setText(ex.getMessage());
        } catch (NullPointerException ex){
            builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
            builder.setText(ex.getMessage());
        } catch (AccessDeniedException ex){
            builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
            builder.setText(ex.getMessage());
        } catch (NumberFormatException ex) {
            builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
            builder.setText(ex.getMessage());
        } catch (SQLException ex){
            builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
            builder.setText(ex.getMessage());
        } finally {
            resp.setContentType(builder.getContentType());
            builder.writeTo(resp.getOutputStream());
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    public static boolean checkHiRight(HttpSession session){
        if(session.getAttribute("id") != null){
            if(session.getAttribute("right").equals("фотограф") ||
                    session.getAttribute("right").equals("su"))
                return true;
        }
        return false;
    }
    public static boolean checkLowRight(HttpSession session){
        return session.getAttribute("id") != null;
    }
}
