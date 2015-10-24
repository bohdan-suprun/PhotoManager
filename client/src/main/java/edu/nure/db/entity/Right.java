package edu.nure.db.entity;

import edu.nure.db.entity.constraints.ValidationException;

import javax.servlet.http.HttpServletRequest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Created by bod on 17.09.15.
 */
public class Right implements Transmittable{
    private String type, desc;

    public Right(String type, String desc)throws ValidationException{
        setType(type);
        setDesc(desc);
    }

    public Right(ResultSet rs) throws SQLException, ValidationException {
        setType(rs.getString("Type"));
        setDesc(rs.getString("Desc"));

    }

    public Right(HttpServletRequest req)throws ValidationException{
        setType(req.getParameter("type"));
        setDesc(req.getParameter("desc"));
    }

    public String getType() {
        return type;
    }

    public void setType(String type)throws ValidationException{
        try {
            this.type = Objects.requireNonNull(type);
        } catch (NullPointerException ex){
            throw new ValidationException("Вы не указали тип учетной записи пользователя");
        }
    }

    public String getDesc() {
        if(desc != null)
            desc = desc.replace('"', '\'');
        return desc;
    }

    public void setDesc(String desc) {
        if(desc != null)
            desc = desc.replace('\'', '"');
        this.desc = desc;
    }

    @Override
    public String toXML() {
        return "<right type=\""+type +"\""+((desc == null)?"":" desc=\""+desc+"\"")+"/>";
    }

    @Override
    public String toQuery() {
        return "type=" + type + "&desc=" + desc;
    }

    public static String[] getFields() {
        return new String[]{"Type", "Desc"};
    }
}
