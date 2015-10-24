package edu.nure.db.entity;

import javax.servlet.http.HttpServletRequest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Created by bod on 17.09.15.
 */
public class Right implements Transmittable{
    private String type, desc;

    public Right(String type, String desc){
        setType(type);
        setDesc(desc);
    }

    public Right(ResultSet rs) throws SQLException {
        setType(rs.getString("Type"));
        setDesc(rs.getString("Desc"));

    }

    public Right(HttpServletRequest req){
        setType(req.getParameter("type"));
        setDesc(req.getParameter("desc"));
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = Objects.requireNonNull(type);
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
