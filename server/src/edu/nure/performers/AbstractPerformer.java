package edu.nure.performers;

import edu.nure.db.Connector;
import edu.nure.db.RequestPreparing;
import edu.nure.db.entity.Transmittable;
import edu.nure.performers.exceptions.PerformException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by bod on 18.09.15.
 * Abstract class. Used for manipulation on entities
 */
abstract public class AbstractPerformer {
    protected ResponseBuilder builder;
    protected Connection connection;

    public AbstractPerformer() throws SQLException {

    }
    public Connection getConnection(){
        return connection;
    }

    protected ResponseBuilder getBuilder(){
        return builder;
    }

    protected void setBuilder(ResponseBuilder builder){
        this.builder = builder;
    }

    protected Statement getStatement() throws ConnectException{
        try {
            return connection.createStatement();
        }catch (SQLException e) {
            throw new ConnectException("SQL EXCEPTION");
        }

    }

    abstract public void perform() throws PerformException, IOException, SQLException;

    protected void doGet() throws PerformException, IOException, SQLException {
        // returns requested values
        //TODO
    }
    protected void doInsert() throws PerformException, IOException, SQLException {
        // inserts and returns value of last inserted item
        //TODO
    }
    protected void doUpdate() throws PerformException, IOException, SQLException {
        // updates value
        //TODO
    }
    protected void doDelete() throws PerformException, IOException, SQLException {
        // just deletes value if it is not restricted
        //TODO
    }

    // returns a value with max ID
    protected ResultSet getLastInserted(String table) throws ConnectException, SQLException{
        String s = "SELECT * FROM `pmanager`.`"+table.toUpperCase()+"`"
        +" Where Id=(Select Max(Id) From `pmanager`.`"+table.toUpperCase()+"`)";
        ResultSet rs = getStatement().executeQuery(s);

        rs.next();
        return rs;
    }
}
