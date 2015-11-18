package edu.nure.performers;

import edu.nure.db.Connector;
import edu.nure.db.RequestPreparing;
import edu.nure.db.dao.AbstractDAOFactory;
import edu.nure.db.dao.DBException;
import edu.nure.db.dao.domains.interfaces.ImageDAO;
import edu.nure.db.entity.*;
import edu.nure.db.entity.constraints.ValidationException;
import edu.nure.db.entity.primarykey.IntegerPrimaryKey;
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
    private ImageDAO dao;

    public ImagePerformer(ResponseBuilder builder, DiskFileItemFactory factory) throws DBException {
        super(builder);
        this.factory = factory;
        dao = AbstractDAOFactory.getDAO(AbstractDAOFactory.MYSQL).getImageDAO();
    }

    public void perform() throws PerformException, IOException {
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
    protected void doGet() throws PerformException, IOException {
        try {
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
                    builder.add(dao.select(new IntegerPrimaryKey(id)));
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
                int albumId = builder.getIntParameter("albumId");
                getAllImages(albumId);
                builder.setContentType(ResponseBuilder.XML_TYPE);
            }catch (NumberFormatException ex){
                throw new ValidationException();
            }


        } catch (ValidationException e) {
            throw new PerformException("Неверный фомат данных");
        }
    }

    private void getLookLikes(String pHash){
        int li = builder.getIntParameter("limit");

        for (edu.nure.db.entity.Image im: dao.getLike(pHash, li)) {
            builder.add(im);
        }
    }

    private void getAllImages(int albumId){
        for (edu.nure.db.entity.Image image:  dao.getInAlbum(albumId)) {
            builder.add(image);
        }
    }

    private byte[] getFull(int id) {
        return dao.select(new IntegerPrimaryKey(id)).getImage();
    }

    @Override
    public void doInsert() throws PerformException, IOException{
        try {
            if (ServletFileUpload.isMultipartContent(builder.getRequest())) {
                edu.nure.db.entity.Image image = parseRequest(new ServletFileUpload(factory));
                image = dao.insert(image);

                if(image != null) {
                    builder.add(image);
                    builder.setStatus(ResponseBuilder.STATUS_OK);
                } else {
                    builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
                    builder.setText("Неудалось добавить изображение");
                }
            } else{
                throw new PerformException("Неверный формат входного пакета");
            }
        } catch (FileUploadException e) {
            throw new PerformException("Ошибка загрузки файла");
        } catch (DBException ex){
            throw new PerformException("Ошибка обработки запроса");
        }

    }

    private edu.nure.db.entity.Image parseRequest(ServletFileUpload upload) throws FileUploadException, IOException{
        List items = upload.parseRequest(builder.getRequest());
        HashMap<String, String> parameters = new HashMap<String, String>();
        byte[] buffer = null;
        for (Object it : items) {
            FileItem item = (FileItem)it;
            if (item.isFormField())
                parameters.put(item.getFieldName(), item.getString("utf-8"));
            else buffer = item.get();
        }
        return new edu.nure.db.entity.Image(
                parameters.get("hash"),
                edu.nure.db.entity.Image.ID_NOT_SET,
                buffer,
                Integer.valueOf(parameters.get("album")),
                new Date()
        );
    }

    @Override
    protected void doDelete() throws PerformException, IOException {
        try {
            int id = builder.getIntParameter("id");
            if(dao.delete("IMAGE", new IntegerPrimaryKey(id))){
                builder.setStatus(ResponseBuilder.STATUS_OK);
            } else {
                builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
                builder.setText("Неудалось удалить изображение");
            }
        } catch (DBException e) {
            throw new PerformException("Ошибка обработки запроса");
        }
    }

    public byte[] preview(byte[] imgBytes) throws IOException {

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
