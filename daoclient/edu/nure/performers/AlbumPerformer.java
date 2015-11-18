package edu.nure.performers;

import edu.nure.db.RequestPreparing;
import edu.nure.db.dao.AbstractDAOFactory;
import edu.nure.db.dao.DBException;
import edu.nure.db.dao.domains.interfaces.AlbumDAO;
import edu.nure.db.dao.domains.interfaces.GenericDAO;
import edu.nure.db.entity.Album;
import edu.nure.db.entity.primarykey.IntegerPrimaryKey;
import edu.nure.performers.exceptions.PerformException;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by bod on 07.10.15.
 */
public class AlbumPerformer extends AbstractPerformer {
    private AlbumDAO dao;

    public AlbumPerformer(ResponseBuilder builder) throws DBException{
        super(builder);
        dao = AbstractDAOFactory.getDAO(AbstractDAOFactory.MYSQL).getAlbumDAO();
    }

    @Override
    public void perform() throws PerformException, IOException {
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
    protected void doGet() throws PerformException, IOException {
        try{
            int userId = builder.getIntParameter("id");
            if(builder.getParameter("albumId") != null){
                Album album = dao.select(new IntegerPrimaryKey(Integer.valueOf(builder.getParameter("albumId"))));
                builder.add(album);
            }else {
                for (Album album: dao.getUserAlbum(userId)) {
                    builder.add(album);
                }
            }
            builder.setStatus(ResponseBuilder.STATUS_OK);
        }catch (NumberFormatException ex){
            throw new PerformException("Недостаточно параметров");
        }

    }

    @Override
    protected void doInsert() throws PerformException, IOException {
        try{
            Album a = new Album(builder);

            a = dao.insert(a);
            if (a != null){
                builder.add(a);
                builder.setStatus(ResponseBuilder.STATUS_OK);
            } else {
                builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
                builder.setText("Ошибка создания нового альбома");
            }
        }catch (NumberFormatException ex){
            throw new PerformException("Недостаточно параметров");
        } catch (DBException ex){
            if(ex.getMessage().toLowerCase().contains("duplicate"))
                throw new PerformException("Такой альбом уже существует");
            throw new PerformException("Ошибка обработки запроса");
        }
    }

    @Override
    protected void doDelete() throws PerformException, IOException{
        try{
            Album a = new Album(builder);
            if(!dao.delete(a)) {
                builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
                builder.setText("Ошибка удаления альбома");
            }
        }catch (NumberFormatException ex){
            throw new PerformException("Недостаточно параметров");
        } catch (DBException ex){
            throw new PerformException("Ошибка обработки запроса: альбом содержит фотографии");
        }
    }
}
