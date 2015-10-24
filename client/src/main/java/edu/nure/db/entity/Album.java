package edu.nure.db.entity;

import edu.nure.db.Connector;
import edu.nure.db.RequestPreparing;
import edu.nure.db.entity.constraints.ValidationException;

import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by bod on 07.10.15.
 */
public class Album implements Transmittable{
    public static final int ID_NOT_SET = -1;
    private String name;
    private int id;
    private int userId;

    public Album(String name, int id, int userId) {
        this.name = name.replace("'", "\"");
        this.id = id;
        this.userId = userId;
    }

    public Album(ResultSet rs) throws SQLException{
        this.name = rs.getString("Name");
        this.id = rs.getInt("Id");
        this.userId = rs.getInt("UserId");;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toXML() {
        return "<album name=\""+name.replace("\"", "'")+"\" id = \""+id+"\" userId=\""+userId+"\"/>";
    }

    @Override
    public String toQuery() {
        return "name="+name.replace("\"", "'")+"&id="+id+"&userId="+userId;
    }

    public static String[] getFields(){
        return new String[]{"Name", "UserId"};
    }

    public static Album getAlbumById(int id)throws ConnectException, SQLException, ValidationException {
        ResultSet rs = Connector.getConnector().getConnection().createStatement().
                executeQuery(RequestPreparing.select("`album`", new String[]{"*"},
                        "WHERE Id = " + id));
        rs.next();
        return new Album(rs);
    }
}
