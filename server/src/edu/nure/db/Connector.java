package edu.nure.db;

import edu.nure.performers.exceptions.PerformException;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.net.ConnectException;
import java.sql.*;

/**
 * Created by bod on 18.09.15.
 */
public class Connector {
    private static Connector self;
    private DataSource ds;
    private DataSource imageDs;
    private DataSource imageUpload;

    private Connector() throws SQLException{
        try {
            //Context ctx = new InitialContext();
            ds = new MyDataSource(5);//(DataSource)ctx.lookup("java:comp/env/jdbc/db");
            imageDs = new MyDataSource(5);//(DataSource)ctx.lookup("java:comp/env/jdbc/imagedb");
            imageUpload = new MyDataSource(1);
        } catch (Exception ex){
            throw new SQLException(ex.getMessage());

        }
        /*
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost/pmanager?user=root&password=boddec2494" +
                    "&useUnicode=yes&characterEncoding=UTF-8");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }*/

    }

    public static Connector getConnector() throws SQLException{
        return ((self==null)? self = new Connector():self);
    }
    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public Connection getImageConnection() throws SQLException{
        return imageDs.getConnection();
    }

    public Connection getImageUploadConnection() throws SQLException{
        return imageUpload.getConnection();
    }
}
