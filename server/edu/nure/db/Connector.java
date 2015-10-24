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
            ds = new MyDataSource(5);
            imageDs = new MyDataSource(5);
            imageUpload = new MyDataSource(1);
        } catch (Exception ex){
            throw new SQLException(ex.getMessage());

        }
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
