package edu.nure.db.dao.domains.implementations;

import edu.nure.db.dao.domains.interfaces.OrderDAO;
import edu.nure.db.entity.Order;
import edu.nure.db.entity.primarykey.PrimaryKey;

import java.sql.Connection;
import java.util.List;

/**
 * Created by bod on 11.11.15.
 */
public class OrderDAOImpl extends GenericDAOImpl<Order> implements OrderDAO{

    public OrderDAOImpl(Connection connection) {
        super(connection);
    }

    @Override
    public List<Order> getByResponsible(int respId) {
        return getAll(Order.class,
                "WHERE `Responsible` = " + respId + " ORDER BY `Urg`"
        );
    }

    @Override
    public List<Order> getByCustomer(int customerId) {

        return getAll(Order.class,
                "WHERE `Customer` = "+customerId+" ORDER BY `Urg`"
        );
    }

    @Override
    public Order select(PrimaryKey key) {
        return getAll(Order.class,
                "WHERE `"+key.getName()+"` = " + key.getValue() + " ORDER BY `Urg`"
        ).iterator().next();
    }

    @Override
    public List<Order> getActiveByResponsible(int respId) {
        return getAll(Order.class,
                "WHERE `Responsible` = " + respId + " AND `Status` = 1 ORDER BY `Urg`"
        );
    }

    @Override
    public List<Order> getActiveByCustomer(int customerId) {
        return getAll(Order.class,
                "WHERE `Customer` = " + customerId + " AND `Status` = 1 ORDER BY `Urg`"
        );
    }

    @Override
    public List<Order> getActiveById(int id) {
        return getAll(Order.class,
                "WHERE `Id` = " + id + " AND `Status` = 1 ORDER BY `Urg`"
        );
    }
}
