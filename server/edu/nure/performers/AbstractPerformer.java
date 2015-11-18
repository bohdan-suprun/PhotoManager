package edu.nure.performers;

import edu.nure.db.Connector;
import edu.nure.db.RequestPreparing;
import edu.nure.db.dao.DBException;
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

    public AbstractPerformer(ResponseBuilder builder) throws DBException {
        setBuilder(builder);

    }

    protected ResponseBuilder getBuilder(){
        return builder;
    }

    protected void setBuilder(ResponseBuilder builder){
        this.builder = builder;
    }

    abstract public void perform() throws PerformException, IOException;

    protected void doGet() throws PerformException, IOException {
        // returns requested values
        //TODO
    }
    protected void doInsert() throws PerformException, IOException{
        // inserts and returns value of last inserted item
        //TODO
    }
    protected void doUpdate() throws PerformException, IOException{
        // updates value
        //TODO
    }
    protected void doDelete() throws PerformException, IOException {
        // just deletes value if it is not restricted
        //TODO
    }
}
