package edu.nure.gui.simpleviewer;

import edu.nure.db.entity.Order;
import edu.nure.db.entity.User;
import edu.nure.gui.MessagesManager;
import edu.nure.listener.Action;
import edu.nure.listener.SelectAdapter;
import edu.nure.listener.results.DBResult;
import edu.nure.listener.results.DBSelectResult;
import edu.nure.listener.results.ResultItem;
import edu.nure.net.HttpManager;
import edu.nure.net.Priority;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by bod on 15.10.15.
 */
public class ResponsibleOrderViewer extends OrderViewer {

    public ResponsibleOrderViewer(User resp) {
        super(resp);
        setReadOnly(true);
        titlePlayerFirst.setText("Ответственный");
        titlePlayerSecond.setText("Покупатель");
    }


    @Override
    protected void load() {
            HttpManager.getManager().sendGet("?action="+getAct+"&responsible="+ owner.getId()+"&active=",new Priority(Priority.MAX),
                    getDefaultAdapter());

    }

    @Override
    protected MouseAdapter taskClicked(){
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(!(curEntity instanceof NewItemContainer))
                    getCustomerAndShowWindow();
            }
        };
    }

    protected void getCustomerAndShowWindow(){
        final Order o = (Order)curEntity.getEntity();
        HttpManager.getManager().sendGet("?action="+ Action.GET_USER+"&id="+o.getCustomer(), new Priority(Priority.MAX),
                new SelectAdapter(){
                    @Override
                    public void onUser(DBSelectResult res) {
                        ResultItem[] items = res.getResult();
                        if(items.length > 0) {
                            StockViewer v = new StockViewer(o, (User) items[0].getEntity());
                            v.setReadOnly(true);
                            v.setVisible(true);
                        }

                    }
                    @Override
                    public void doError(DBResult result) {
                        MessagesManager.errorBox("Невозможно просмотреть задачи", "ERROR");
                    }
                });

    }
}
