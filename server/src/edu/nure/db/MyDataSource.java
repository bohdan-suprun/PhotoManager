package edu.nure.db;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by bod on 12.10.15.
 */
public class MyDataSource implements DataSource {
    private ArrayList<Connection> pool;
    private Connection c;
    private int index = 0;

    public MyDataSource(int initial){
        pool = new ArrayList<Connection>(initial);
        try {
            for(int i = 0;i<initial;i++) {
                Class.forName("com.mysql.jdbc.Driver");
                pool.add(get());
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Connection getConnection() throws SQLException {
        if(index >= pool.size())
            index = 0;

            return pool.get(index++);
    }

    private Connection get() throws SQLException{
       return DriverManager.getConnection("jdbc:mysql://localhost/pmanager?user=root&password=*****" +
                "&useUnicode=yes&characterEncoding=UTF-8");
    }

    @Override
    public Connection getConnection(String s, String s1) throws SQLException {
        return null;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter printWriter) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int i) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> aClass) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return false;
    }
}
