package edu.nure.db.entity;
import edu.nure.db.Connector;
import edu.nure.db.RequestPreparing;
import edu.nure.db.entity.constraints.MoreOrEq;
import edu.nure.db.entity.constraints.ValidationException;
import edu.nure.db.entity.constraints.Validator;

import javax.servlet.http.HttpServletRequest;
import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by bod on 17.09.15.
 */
public class Urgency implements Transmittable {
    int term;
    float factor;
    private static final int TO_MS = 60000;

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) throws ValidationException {
        Date now = new Date();
        Date inTerm = new Date(now.getTime()+term*TO_MS);
        if(inTerm.after(now)) {
            this.term = term;
        }else throw new ValidationException("Срочность должна быть больше нуля");
    }

    public float getFactor() {
        return factor;
    }

    public void setFactor(float factor) throws ValidationException {
        this.factor = (Float)Validator.validate(factor, new MoreOrEq<Float>(0.0f));
    }

    public Urgency(int term, float factor) throws ValidationException {
        setTerm(term);
        setFactor(factor);
    }

    public Urgency(ResultSet rs) throws ValidationException, SQLException {
        setTerm(rs.getInt("Term"));
        setFactor(rs.getFloat("Factor"));
    }

    public Urgency(HttpServletRequest req) throws ValidationException {
        try {
            setTerm(Integer.valueOf(req.getParameter("term")));
            setFactor(Float.valueOf(req.getParameter("factor")));
        }catch (NumberFormatException ex){
            throw new ValidationException();
        }
    }

    public static Urgency getUrgencyByTerm(int term) throws ConnectException, SQLException, ValidationException {
        ResultSet rs = Connector.getConnector().getConnection().createStatement().
                executeQuery(RequestPreparing.select("urgency", new String[]{"*"}, "WHERE Term = " + term));
        rs.next();
        return new Urgency(rs);
    }

    @Override
    public String toXML() {
        return "<urgency term=\""+term+"\" factor=\""+factor+"\"/>";
    }

    @Override
    public String toQuery() {
        return "term=" + term +
                "&factor=" + factor;
    }

    public static String[] getFields() {
        return new String[]{"Term", "Factor"};
    }
}
