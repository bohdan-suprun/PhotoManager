package edu.nure.performers;

import edu.nure.db.Connector;
import edu.nure.db.RequestPreparing;
import edu.nure.db.entity.Album;
import edu.nure.performers.exceptions.PerformException;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by bod on 07.10.15.
 */
public class AlbumPerformer extends AbstractPerformer {

    public AlbumPerformer(ResponseBuilder builder) throws SQLException {
        connection = Connector.getConnector().getConnection();
        this.builder = builder;
    }

    @Override
    public void perform() throws PerformException, IOException, SQLException {
        int action = builder.getAction();
        switch (action) {
            case Action.GET_ALBUM:
                doGet();
                break;
            case Action.INSERT_ALBUM:
                doInsert();
                break;
            case Action.DELETE_ALBUM:
                doDelete();
        }


    }

    @Override
    protected void doGet() throws PerformException, IOException, SQLException {
        try{
            int id = builder.getIntParameter("id");
            ResultSet s;
            if(builder.getParameter("albumId") != null){
                s = getStatement().executeQuery(RequestPreparing.select("album", new String[]{"*"},
                        "WHERE Id ="+builder.getParameter("albumId")+"  Order by Name;"));

            }else {
                s = getStatement().executeQuery(RequestPreparing.select("album", new String[]{"*"},
                        "WHERE UserId = " + id + " Order by Name;"));
            }
            while (s.next()){
                builder.add(new Album(s));
            }
            builder.setStatus(ResponseBuilder.STATUS_OK);

        }catch (NumberFormatException ex){
            throw new PerformException("Недостаточно параметров");
        } catch (SQLException ex){
            throw new PerformException("Ошибка обработки запроса");
        }

    }

    @Override
    protected void doInsert() throws PerformException, IOException, SQLException {
        try{
            Album a = new Album(builder);
            int n =  getStatement().executeUpdate(RequestPreparing.insert("album", Album.getFields(),
                    new Object[]{a.getName(), a.getUserId()}));

            if(n > 0) {
                Album album = new Album(getLastInserted("album"));
                builder.add(album);
                builder.setStatus(ResponseBuilder.STATUS_OK);
                return;
            }
            builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
            builder.setText("Ошибка создания нового альбома");
        }catch (NumberFormatException ex){
            throw new PerformException("Недостаточно параметров");
        } catch (SQLException ex){
            if(ex.getMessage().toLowerCase().contains("duplicate")) throw new PerformException("Такой альбом уже существует");
            throw new PerformException("Ошибка обработки запроса");
        }
    }

    @Override
    protected void doDelete() throws PerformException, IOException, SQLException {
        try{
            Album a = new Album(builder);
            int n =  getStatement().executeUpdate(
                    "DELETE FROM `ALBUM` WHERE Id=" + a.getId()
            );

            if(n > 0) {
                builder.setStatus(ResponseBuilder.STATUS_OK);
                return;
            }
            builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
            builder.setText("Ошибка удаления альбома");
        }catch (NumberFormatException ex){
            throw new PerformException("Недостаточно параметров");
        } catch (SQLException ex){
            throw new PerformException("Ошибка обработки запроса: альбом содержит фотографии");
        }
    }
}
