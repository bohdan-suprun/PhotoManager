package edu.nure.db;

import java.net.ConnectException;
import java.sql.*;

/**
 * Created by bod on 18.09.15.
 */
public class Connector {
    private static Connector self;
    private static Connection connection;
    private Connector(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost/photo_manager?user=root&password=boddec2494" +
                    "&useUnicode=yes&characterEncoding=UTF-8");
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM FORMAT");
            while(rs.next()) {
                System.out.println(rs.getString("Name")+"---"+rs.getInt("Width")+"---"+rs.getInt("Height")+"---"+rs.getFloat("Price"));

            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static Connector getConnector() {
        return ((self==null)? self = new Connector():self);
    }
    public Connection getConnection() throws ConnectException{
        try {
            if(connection.isClosed()) throw new ConnectException();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static void main(String[] arg){
        Connector c = new Connector();

    }
}
