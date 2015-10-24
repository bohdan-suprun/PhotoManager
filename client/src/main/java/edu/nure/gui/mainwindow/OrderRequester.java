package edu.nure.gui.mainwindow;

import edu.nure.db.entity.Order;
import edu.nure.db.entity.User;
import edu.nure.gui.containers.OrderContainer;
import edu.nure.listener.Action;
import edu.nure.listener.ResponseListener;
import edu.nure.listener.SelectAdapter;
import edu.nure.listener.results.DBSelectResult;
import edu.nure.listener.results.ResultItem;
import edu.nure.net.HttpManager;
import edu.nure.net.Priority;

/**
 * Created by bod on 02.10.15.
 */
public class OrderRequester extends Thread {
    private User owner;

    public OrderRequester(User owner) {
        this.owner = owner;
        setDaemon(true);
        setPriority(Thread.MIN_PRIORITY);
        start();
    }

    @Override
    public void run() {
        try {
            while (true) {
                HttpManager.getManager().sendGet("?action=" + Action.GET_ORDER +
                                "&responsible=" +
                                MainForm.getMainForm().getWorker().getId() + "&active=", new Priority(Priority.MIN),
                        getListener());
                Thread.sleep(60000);

            }
        } catch (InterruptedException ex) {
            System.err.println("interrupted");
        }
    }

    private ResponseListener getListener() {
        return new SelectAdapter() {
            @Override
            public void onOrder(DBSelectResult result) {
                DBSelectResult res = result;
                MainForm.getMainForm().getOrderModel().clear();
                for (ResultItem item : res.getResult()) {
                    Order order = (Order) item.getEntity();
                    MainForm.getMainForm().getOrderModel().addElement(new OrderContainer(order));
                }
                MainForm.getMainForm().getListOrder().repaint();
            }
        };
    }
}