package edu.nure.performers;

import edu.nure.db.Connector;
import edu.nure.db.RequestPreparing;
import edu.nure.db.entity.*;
import edu.nure.db.entity.constraints.ValidationException;
import edu.nure.performers.exceptions.PerformException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ConnectException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by bod on 21.09.15.
 */
public class ImagePerformer extends AbstractPerformer{
    private DiskFileItemFactory factory;

    public ImagePerformer(ResponseBuilder builder, DiskFileItemFactory factory) throws SQLException{
        connection = Connector.getConnector().getImageConnection();
        setBuilder(builder);
        this.factory = factory;
    }

    public void perform() throws PerformException, IOException, SQLException {
            int action = builder.getAction();

            switch (action) {
                case Action.GET_IMAGE:
                    doGet();
                    break;
                case Action.DELETE_IMAGE:
                    doDelete();
                    break;
                case Action.INSERT_IMAGE:
                    doInsert();
                    break;
                default:
                    builder.setStatus(ResponseBuilder.STATUS_PARAM_ERROR);

            }
    }

    @Override
    protected void doGet() throws PerformException, IOException, SQLException {
        try {
            Album owner;
            if (builder.getParameter("hash") != null) {
                String hash = builder.getParameter("hash");
                if(hash.matches("[0-9A-Fa-f]+")) {
                    getLookLikes(hash);
                }
                builder.setContentType(ResponseBuilder.XML_TYPE);
                return;
            }
            if (builder.getParameter("obj") != null) {
                try {
                    int id = builder.getIntParameter("id");
                    builder.add(edu.nure.db.entity.Image.getImageById(id));
                } catch (NumberFormatException ex){
                    throw new PerformException("Невозможно преобразовать id в число");
                }
                builder.setContentType(ResponseBuilder.XML_TYPE);
                return;
            }

            if (builder.getParameter("full") != null) {
                builder.add(getFull(builder.getIntParameter("id")));
                builder.setContentType(ResponseBuilder.IMAGE_TYPE);
                return;
            }
            if (builder.getParameter("preview") != null) {
                builder.add(preview(getFull(builder.getIntParameter("id"))));
                builder.setContentType(ResponseBuilder.IMAGE_TYPE);
                return;
            }
            try {
                owner = Album.getAlbumById(builder.getIntParameter("albumId"));
            }catch (NumberFormatException ex){
                throw new ValidationException();
            }
            if (builder.getParameter("next") != null) {
                getNext(builder.getIntParameter("next"), owner.getId());
                builder.setContentType(ResponseBuilder.XML_TYPE);
                return;
            }
            if (builder.getParameter("prev") != null) {
                getPrev(builder.getIntParameter("prev"),owner.getId());
                builder.setContentType(ResponseBuilder.XML_TYPE);
                return;
            }

            getAllImages(owner);
            builder.setContentType(ResponseBuilder.XML_TYPE);
        } catch (SQLException e) {
            throw new PerformException("Ошибка запроса к базе данных: "+e.getMessage());
        } catch (ValidationException e) {
            throw new PerformException("Неверный фомат данных");
        }
    }

    private void getLookLikes(String pHash) throws ConnectException, SQLException, ValidationException {
        Statement st = getConnection().createStatement();
        int li = builder.getIntParameter("limit");
        ResultSet resultSet = st.executeQuery("Select * From `pmanager`.`IMAGE`"+
                " Where BIT_COUNT(CONV(`Hash`, 16, 10) ^ CONV('"+pHash+"', 16, 10)) <=" +li+
                " ORDER BY "+"BIT_COUNT(CONV(`Hash`, 16, 10) ^ CONV('"+pHash+"', 16, 10))");
        while (resultSet.next()) {
            edu.nure.db.entity.Image image = new edu.nure.db.entity.Image(resultSet);
            builder.add(image);
        }
    }

    private void getNext(int id, int album) throws IOException, SQLException, ValidationException {
        ResultSet rs = getConnection().createStatement().executeQuery(
                RequestPreparing.select("`Image`", new String[]{"*"},
                        "Where Id >= "+id+" AND Album = "+album+" ORDER BY Id ASC")
        );
        while (rs.next()){
            if(rs.getInt("Id") > id){
                edu.nure.db.entity.Image image = new edu.nure.db.entity.Image(rs);
                builder.add(image);
                return;
            }
            if(rs.isLast()){
                edu.nure.db.entity.Image image = new edu.nure.db.entity.Image(rs);
                builder.add(image);
            }
        }


    }

    private void getPrev(int id, int album)throws IOException, SQLException, ValidationException{
        ResultSet rs = getConnection().createStatement().executeQuery(
                RequestPreparing.select("`Image`", new String[]{"*"},
                        "Where Id <= "+id+" AND Album = "+album+" ORDER BY Id DESC")
        );
        while (rs.next()){
            if(rs.getInt("Id") < id){
                edu.nure.db.entity.Image image = new edu.nure.db.entity.Image(rs);
                builder.add(image);
                return;
            }
            if(rs.isLast()) {
                edu.nure.db.entity.Image image = new edu.nure.db.entity.Image(rs);
                builder.add(image);
            }
        }
    }

    private void getAllImages(Album owner) throws SQLException, ConnectException, ValidationException {
            Statement st = getConnection().createStatement();
            ResultSet resultSet = st.executeQuery("SELECT * FROM IMAGE Where Album=" + owner.getId() +
                    " ORDER by CreatedIn desc;");
            while (resultSet.next()) {
                edu.nure.db.entity.Image image = new edu.nure.db.entity.Image(resultSet);
                builder.add(image);
            }
    }

    private byte[] getFull(int id) throws IOException, SQLException {
        Statement st = getConnection().createStatement();
        ResultSet resultSet = st.executeQuery(RequestPreparing.select("image", new String[]{"Image"}
                , "Where Id=" + id));
        if(resultSet.last())
            return resultSet.getBytes("Image");
        else
        return null;
    }

    @Override
    public void doInsert() throws PerformException, IOException, SQLException {
        HashMap<String, String> params = new HashMap<String, String>();
        if (ServletFileUpload.isMultipartContent(builder.getRequest())) {
            ServletFileUpload upload = new ServletFileUpload(factory);
            byte[] buffer = null;
            try {
                List items = upload.parseRequest(builder.getRequest());
                for (Object it : items) {
                    FileItem item = (FileItem)it;
                    if (item.isFormField())
                        params.put(item.getFieldName(), item.getString("utf-8"));
                    else buffer = item.get();
                }
                edu.nure.db.entity.Image image = new edu.nure.db.entity.Image(
                        params.get("hash"),
                        edu.nure.db.entity.Image.ID_NOT_SET,
                        buffer,
                        Integer.valueOf(params.get("album")),
                        new Date()
                );

                PreparedStatement ps  = getConnection().prepareStatement("INSERT INTO `IMAGE` " +
                        "(`Hash`, `Album`,`CreatedIn`,`Image`)" +
                        "VALUES(?, ?, ?, ?)");

                ps.setString(1, image.getHash());
                ps.setInt(2, image.getAlbum());
                ps.setString(3, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                ps.setBytes(4, buffer);
                getConnection().setAutoCommit(false);
                int n = ps.executeUpdate();

                if(n > 0) {
                    builder.add(new edu.nure.db.entity.Image(getLastInserted("image")));
                    getConnection().commit();
                    builder.setStatus(ResponseBuilder.STATUS_OK);
                    return;
                }

                builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
                builder.setText("Неудалось добавить изображение");
            } catch (FileUploadException e) {
                throw new PerformException("Ошибка загрузки файла");
            } catch (SQLException e) {
                throw new PerformException("Ошибка обработки запроса");
            } catch (ValidationException ex){
                throw new PerformException("Неверный формат данных");
            } finally {
                getConnection().setAutoCommit(true);
            }
        } else{
            throw new PerformException("Неверный формат входного пакета");
        }

    }

    @Override
    protected void doDelete() throws PerformException, IOException, SQLException {
        try {
            int id;
            try {
                id = builder.getIntParameter("id");
            }catch (NumberFormatException e){
                throw new ValidationException();
            }
            int n = getConnection().createStatement().executeUpdate("DELETE FROM `pmanager`." +
                    "`IMAGE` WHERE `Id`=" + id);
            if(n > 0){
                builder.setStatus(ResponseBuilder.STATUS_OK);
                return;
            }
            builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
            builder.setText("Неудалось удалить изображение");
        } catch (SQLException e) {
            throw new PerformException("Ошибка обработки запроса");
        } catch (ValidationException e) {
            throw new PerformException("Ошибка формата данных");
        }
    }

    public byte[] preview(byte[] imgBytes) throws IOException, SQLException {

        Image img = ImageIO.read(new ByteArrayInputStream(imgBytes));
        int width = img.getWidth(null);
        int height = img.getHeight(null);
        double scale = 75.0/(double)Math.min(width, height);
        int miniWidth = (int)(scale * (double)width);
        int miniHeight = (int)(scale * (double)height);

        AffineTransform transform = new AffineTransform(
                ((double) miniWidth) / width, 0, 0,
                ((double) miniHeight) / height, 0, 0);
        AffineTransformOp transformer = new AffineTransformOp(transform, new RenderingHints(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC));
        BufferedImage fullImage = ImageIO.read(new ByteArrayInputStream(imgBytes));
        BufferedImage miniImage = new BufferedImage(miniWidth, miniHeight, BufferedImage.TYPE_3BYTE_BGR);
        transformer.filter(fullImage, miniImage);

        if(miniHeight > miniWidth) {
            miniImage = miniImage.getSubimage(0, (miniHeight - 75) / 2, 75, 75 + (miniHeight-75)/2);
        }
        else if(miniHeight < miniWidth) {
            miniImage = miniImage.getSubimage((miniWidth - 75) / 2, 0, 75 + (miniHeight-75)/2, 75);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(miniImage, "jpg", out);
        return out.toByteArray();
    }
}
