package edu.nure.db.entity;

import edu.nure.db.entity.constraints.ValidationException;
import edu.nure.performers.ResponseBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by bod on 17.09.15.
 */
public class Stock implements Transmittable{
    public static final int ID_NOT_SET = -1;
    private int id = ID_NOT_SET;
    private int order;
    private int image;
    private String desc;
    private String format;

    public Stock(int id, int order, int image, String desc, String format){
        setId(id);
        setDesc(desc);
        setOrder(order);
        setImage(image);
        setFormat(format);
    }

    public Stock(ResultSet rs) throws SQLException, ValidationException, ConnectException{
        setId(rs.getInt("Id"));
        setDesc(rs.getString("Desc"));
        setOrder(rs.getInt("Id_order"));
        setImage(rs.getInt("Image"));
        setFormat(rs.getString("Format"));

    }

    public Stock(ResponseBuilder req) throws ValidationException, SQLException, ConnectException {
        try {
            String id = req.getParameter("id");
            if(id == null)
                setId(ID_NOT_SET);
            else    setId(Integer.valueOf(id));

            setDesc(req.getParameter("desc"));
            setOrder(req.getIntParameter("order"));
            setImage(req.getIntParameter("image"));
            setFormat(req.getParameter("format"));
        }catch (NumberFormatException ex){
            throw new ValidationException("Неверный формат данных");
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        if(desc != null)
            desc = desc.replace('\'', '"');
        this.desc = desc;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format.replace('\'', '"');
    }

    @Override
    public String toXML() {
        return "<stock id=\""+id+"\" order=\""+order+"\" image=\""+image+"\" "+((desc == null)?"":"desc=\""
                +desc.replace('"', '\'')+"\"")
                +" format=\"" + format.replace('"', '\'')+"\"/>";
    }

    @Override
    public String toQuery() {
        return "id=" + id +
                "&order=" + order +
                "&image=" + image +
                ((desc == null)?"":"&desc=\""
                        +desc.replace('"', '\'')+"\"")+
                "&format=" + format;
    }

    public static String[] getFields() {
        return new String[]{"Id_order", "Image", "Desc", "Format"};
    }
}
