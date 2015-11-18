package edu.nure.db.entity;
import edu.nure.db.Connector;
import edu.nure.db.RequestPreparing;
import edu.nure.db.dao.DBException;
import edu.nure.db.entity.constraints.MoreOrEq;
import edu.nure.db.entity.constraints.ValidationException;
import edu.nure.db.entity.constraints.Validator;
import edu.nure.db.entity.primarykey.IntegerPrimaryKey;
import edu.nure.db.entity.primarykey.PrimaryKey;
import edu.nure.performers.ResponseBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by bod on 17.09.15.
 */
public class Urgency implements Transmittable {
    private int term;
    private double factor;
    private static final int TO_MS = 60000;

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) throws ValidationException {
        Date now = new Date();
        Date inTerm = new Date(now.getTime()+term*TO_MS);
        if(inTerm.after(now)) {
            this.term = term;
        }else throw new ValidationException("Bad term");
    }

    public double getFactor() {
        return factor;
    }

    public void setFactor(double factor) throws ValidationException {
        this.factor = (Double)Validator.validate(factor, new MoreOrEq<Double>(0.0));
    }

    public Urgency(){

    }

    public Urgency(int term, double factor) throws ValidationException {
        setTerm(term);
        setFactor(factor);
    }

    @Override
    public void parseResultSet(ResultSet rs) throws ValidationException, DBException {
        try {
            setTerm(rs.getInt("Term"));
            setFactor(rs.getFloat("Factor"));
        } catch (SQLException ex){
            throw new DBException(ex.getMessage());
        }
    }

    public Urgency(ResponseBuilder req) throws ValidationException {
        try {
            setTerm(req.getIntParameter("term"));
            setFactor(req.getDoubleParameter("factor"));
        }catch (NumberFormatException ex){
            throw new ValidationException();
        }
    }
/*
    public static Urgency getUrgencyByTerm(int term) throws ConnectException, SQLException, ValidationException {
        ResultSet rs = Connector.getConnector().getConnection().createStatement().
                executeQuery(RequestPreparing.select("urgency", new String[]{"*"}, "WHERE Term = " + term));
        rs.next();
        return new Urgency(rs);
    }
*/
    @Override
    public String toXML() {
        return "<urgency term=\""+term+"\" factor=\""+factor+"\"/>";
    }

    @Override
    public String toQuery() {
        return "term=" + term +
                "&factor=" + factor;
    }

    public String[] getFields() {
        return new String[]{"Term", "Factor"};
    }

    @Override
    public Object[] getValues() {
        return new Object[]{getTerm(), getFactor()};
    }

    @Override
    public String entityName() {
        return "URGENCY";
    }

    @Override
    public PrimaryKey getPrimaryKey() {
        return new IntegerPrimaryKey(getTerm());
    }
}
