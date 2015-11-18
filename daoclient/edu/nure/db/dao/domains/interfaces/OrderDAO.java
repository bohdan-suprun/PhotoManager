package edu.nure.db.dao.domains.interfaces;

import edu.nure.db.entity.Order;

import java.util.List;

/**
 * Created by bod on 11.11.15.
 */
public interface OrderDAO extends GenericDAO<Order> {

    List<Order> getByResponsible(int respId);
    List<Order> getByCustomer(int customerId);
    List<Order> getActiveByResponsible(int respId);
    List<Order> getActiveByCustomer(int customerId);
    List<Order> getActiveById(int id);

}
