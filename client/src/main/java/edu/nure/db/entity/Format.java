package edu.nure.db.entity;

import edu.nure.db.Connector;
import edu.nure.db.RequestPreparing;
import edu.nure.db.entity.constraints.MoreOrEq;
import edu.nure.db.entity.constraints.ValidationException;
import edu.nure.db.entity.constraints.Validator;

import javax.servlet.http.HttpServletRequest;
import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by bod on 17.09.15.
 */
public class Format implements Transmittable {
    private String name;
    private int width, height;
    private float price;

    public Format(String name, int width, int height, float price) throws ValidationException {
        try {
            setName(name);
            setHeight(height);
            setWidth(width);
            setPrice(price);
        } catch (Exception ex){
            throw new ValidationException(ex.getMessage());
        }
    }

    public Format(ResultSet rs) throws SQLException, ValidationException {
        setName(rs.getString("Name"));
        setHeight(rs.getInt("Height"));
        setWidth(rs.getInt("Width"));
        setPrice(rs.getFloat("Price"));
    }

    public Format(HttpServletRequest req) throws ValidationException {
        setName(req.getParameter("name"));
        try {
            setHeight(Integer.valueOf(req.getParameter("height")));
            setWidth(Integer.valueOf(req.getParameter("width")));
            setPrice(Float.valueOf(req.getParameter("price")));
        }catch (NumberFormatException ex){
            throw new ValidationException();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.replace('\'','"');
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) throws ValidationException {
        this.width = (Integer)Validator.validate(width, new MoreOrEq<Integer>(1));
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) throws ValidationException {
        this.height = this.width = (Integer)Validator.validate(height, new MoreOrEq<Integer>(1));
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) throws ValidationException {
        this.price = (Float)Validator.validate(price, new MoreOrEq<Float>(0.0f));
    }


    @Override
    public String toXML() {
        return "<format name=\""+name.replace('"','\'')+"\" "+
                "width=\""+width+"\" "+
                "height=\""+height+"\" "+
                "price=\""+price+"\"/>";
    }

    @Override
    public String toQuery() {
        return "name=" + name.replace('"','\'') +
                "&width=" + width +
                "&height=" + height +
                "&price=" + price;
    }

    public static Format getFormatByName(String name)throws ConnectException, SQLException, ValidationException{
        ResultSet rs = Connector.getConnector().getConnection().createStatement().
                executeQuery(RequestPreparing.select("format", new String[]{"*"}, "WHERE Name = '" + name+"'"));
        return new Format(rs);
    }
    public static String[] getFields() {
        return new String[]{"Name", "Width", "Height", "Price"};
    }
}
