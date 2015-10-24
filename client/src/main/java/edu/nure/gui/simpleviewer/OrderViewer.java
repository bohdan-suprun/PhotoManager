package edu.nure.gui.simpleviewer;

import edu.nure.db.entity.Order;
import edu.nure.db.entity.Transmittable;
import edu.nure.db.entity.Urgency;
import edu.nure.db.entity.User;
import edu.nure.db.entity.constraints.ValidationException;
import edu.nure.db.entity.constraints.Validator;
import edu.nure.gui.MessagesManager;
import edu.nure.gui.SaveButtonObserver;
import edu.nure.gui.SuggestionComboBox;
import edu.nure.gui.SuggestionPerformer;
import edu.nure.gui.containers.*;
import edu.nure.listener.Action;
import edu.nure.listener.CurrentIndexChanged;
import edu.nure.listener.SelectAdapter;
import edu.nure.listener.results.DBResult;
import edu.nure.listener.results.DBSelectResult;
import edu.nure.listener.results.ResultItem;
import edu.nure.net.HttpManager;
import edu.nure.net.Priority;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by bod on 09.10.15.
 */
public class OrderViewer extends AbstractViewer {
    protected User owner;
    private JButton tasks;
    private static OrderViewer self;
    private JLabel customer;
    protected SuggestionComboBox responsible;
    private SuggestionComboBox urg;
    private JLabel term;
    protected JLabel titlePlayerFirst, titlePlayerSecond;
    private JLabel price;
    private JTextField descPane;
    private JCheckBox active;

    private HashMap<Integer,Urgency> urgencies;


    protected OrderViewer(User hesOrder) {
        super("order");
        this.owner = hesOrder;
        setContent(createPane());
        urgencies = new HashMap<Integer, Urgency>();
    }

    public static OrderViewer getWindow(User owner){
        if(self == null){
            self = new OrderViewer(owner);
        }else {
            self.owner = owner;
        }
        return self;
    }

    public static OrderViewer getWindow(){
        return self;
    }

    private JPanel createPane(){
        JPanel main = new JPanel();
        GridLayout g = new GridLayout(0,2);
        g.setVgap(10);
        g.setHgap(10);
        main.setLayout(g);
        main.add(titlePlayerFirst = new JLabel("Покупатель:"));
        main.add(customer = new JLabel("<html><font size=\"4\" color=\"blue\">"+ owner.getName()+"</font><br/>" +
                "<font color=\"green\" size=\"2\">"+ owner.getPhone()+"</font></html>"));
        main.add(titlePlayerSecond = new JLabel("Ответственный:"));
        responsible = new SuggestionComboBox(performerForResp());
        main.add(responsible);


        main.add(new JLabel("Срочность:"));
        main.add(urg = new SuggestionComboBox(performerForUrg()));

        main.add(new JLabel("Строк сдачи:"));
        main.add(term = new JLabel("<unknown>"));
        main.add(new JLabel("Заметка:"));
        descPane = new JTextField();
        main.add(descPane);

        main.add(new JLabel("Статус:"));
        main.add(active = new JCheckBox("Активный", false));

        main.add(new JLabel("К оплате:"));
        main.add(price = new JLabel("0"));
        main.add(tasks = new JButton("Задачи"));
        tasks.addMouseListener(taskClicked());

        urg.addItemChanged(new CurrentIndexChanged() {
            @Override
            public void currentIndexChanged(AbstractSuggestionContainer container) {
                Urgency u = (Urgency)container.getEntity();
                System.out.println(u.toXML());
                Date start;
                int delta = 0;
                if (curEntity instanceof NewItemContainer) {
                    start = new Date();
                    delta = u.getTerm() * 1000*60;
                } else {
                    Order or = (Order) curEntity.getEntity();
                    start = or.getTerm();
                    delta = (u.getTerm() - or.getUrgency())*1000*60;
                }
                Date deadLine = new Date(start.getTime() + delta);
                term.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(deadLine));

            }
        });

        return main;
    }

    protected MouseAdapter taskClicked(){
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(!(curEntity instanceof NewItemContainer))
                    new StockViewer((Order)curEntity.getEntity(), owner).setVisible(true);
            }
        };
    }

    private SuggestionPerformer performerForUrg(){
        return new SuggestionPerformer() {

            @Override
            public void ajax(SuggestionComboBox box, String text, CountDownLatch latch) {
                    HttpManager.getManager().sendGet("?action="+ Action.GET_URGENCY, new Priority(Priority.MAX),
                            urgReceived(box, latch));
            }
            @Override
            public void search(String text) {

            }
        };
    }

    private SuggestionPerformer performerForResp(){
        return new SuggestionPerformer() {
            @Override
            public void ajax(final SuggestionComboBox box, String text, final CountDownLatch latch) {
                text = text.trim();
                String uri = "?action="+ edu.nure.listener.Action.GET_USER+"&";
                boolean byPhone = false;
                if(text.matches("[\\+\\(\\)\\s\\-0-9]+")) {
                    uri += "phone=" + text + "&ajax=";
                    byPhone = true;
                }
                if(text.matches(Validator.NAME_VALIDATOR))
                    uri += "name="+text+"&ajax=&hiRight=";

                final boolean flag = byPhone;
                HttpManager.getManager().sendGet(uri,new Priority(Priority.MAX), new SelectAdapter(){
                    @Override
                    public void onUser(DBSelectResult res) {

                        ResultItem[] items = res.getResult();
                        for(ResultItem item: items){
                            if(flag) {
                                box.addElement(new UserPhoneSuggestionContainer(item.getEntity()));
                            } else{
                                box.addElement(new UserNameSuggestionContainer(item.getEntity()));
                            }
                        }
                        latch.countDown();
                    }

            });
            }

            @Override
            public void search(String text) {

            }
        };
    }

    private SelectAdapter urgReceived(final SuggestionComboBox box, final CountDownLatch latch){
        return new SelectAdapter(){
            @Override
            public void onUrgency(DBSelectResult res) {
                ResultItem[] items = res.getResult();
                for(ResultItem item: items) {
                    System.out.println("----"+item.getEntity());
                    box.addElement(new UrgencyContainer(item.getEntity()));
                    Urgency u = (Urgency)item.getEntity();
                    urgencies.put(u.getTerm(), u);
                }
                latch.countDown();
            }

            @Override
            public void doError(DBResult result) {
                latch.countDown();
                MessagesManager.errorBox("Ошибка во время загрузки срочности"+
                "Код("+result.getStatus()+")","Ошибка");
            }
        };
    }

    @Override
    protected void load() {
        if(owner != null) {
            HttpManager.getManager().sendGet("?action=" + getAct + "&customer=" + owner.getId(), new Priority(Priority.MAX),
                    getDefaultAdapter());
        }

    }

    @Override
    protected void update() {
        HttpManager.getManager().sendGet(curEntity.getEntity(),updAct,new Priority(Priority.MAX),
                getDefaultAdapter());

    }

    @Override
    public void addItem(Transmittable t) {
        model.addElement(new OrderContainer((Order)t));
    }

    @Override
    protected void observeSaveButton() {

        SaveButtonObserver.setListeners(new JComponent[]{responsible, urg, active, descPane},saveButton);
    }

    @Override
    protected Transmittable createEntity() throws ValidationException {
        User resp;
        Urgency u;
        AbstractSuggestionContainer item = responsible.getCurrentItem();
        if(item == null) throw new ValidationException("Нужно указать ответственное лицо");
        resp = (User)item.getEntity();
        item = urg.getCurrentItem();
        if(item == null) throw new ValidationException("Нужно указать срочность");
        u  = (Urgency)item.getEntity();
        String desc = descPane.getText();
        if(desc.isEmpty()) desc = null;
        int status = (active.isSelected())?1:0;

        if(curEntity instanceof NewItemContainer) {
            return new Order(
                    Order.ID_NOT_SET,
                    owner.getId(),
                    resp.getId(),
                    desc,
                    term.getText(),
                    Float.valueOf(price.getText()),
                    status,
                    u.getTerm()
            );
        } else {
            return new Order(
                    ((Order)curEntity.getEntity()).getId(),
                    owner.getId(),
                    resp.getId(),
                    desc,
                    term.getText(),
                    Float.valueOf(price.getText()),
                    status,
                    u.getTerm()
            );
        }
    }

    @Override
    protected void entityChanged() {
        Order o = (Order)curEntity.getEntity();
        loadUser(o.getResponsible());
        loadUrgency(o.getUrgency());
        System.out.println(o.getUrgency());
        price.setText(""+o.getForPay());
        active.setSelected(o.getStatus() == 1);
        descPane.setText((o.getDesc() == null)?"":o.getDesc());
        term.setText( new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(o.getTerm()));
        saveButton.setEnabled(false);

    }

    protected void loadUser(int id){
        HttpManager.getManager().sendGet("?action="+Action.GET_USER+"&id="+id, new Priority(Priority.MAX),
                new SelectAdapter(){
                    @Override
                    public void onUser(DBSelectResult res) {
                        ResultItem[] items = res.getResult();
                        for(ResultItem item: items){
                            UserNameSuggestionContainer container = new UserNameSuggestionContainer(item.getEntity());
                            responsible.addElement(container);
                            int index = responsible.indexOf(container);
                            responsible.setSelectedIndex(index);
                            saveButton.setEnabled(false);
                        }
                    }
                    @Override
                    public void doError(DBResult result) {
                        responsible.setSelectedIndex(-1);
                        super.doError(result);
                        saveButton.setEnabled(false);
                    }
                });

    }

    private void loadUrgency(final int term){
        HttpManager.getManager().sendGet("?action="+Action.GET_URGENCY,new Priority(Priority.MAX),
       new SelectAdapter(){
           @Override
           public void doError(DBResult result) {
               urg.setSelectedIndex(-1);
               super.doError(result);
               saveButton.setEnabled(false);
           }

           @Override
           public void onUrgency(DBSelectResult res) {
               ResultItem[] items = res.getResult();
               for(ResultItem item: items){
                   UrgencyContainer container = new UrgencyContainer(item.getEntity());
                   urg.addElement(container);
                   if(((Urgency)item.getEntity()).getTerm() == term) {
                       int index = urg.indexOf(container);
                       urg.setSelectedIndex(index);
                   }
                   saveButton.setEnabled(false);
               }
           }
       });
    }

    @Override
    protected void setDefault() {
        responsible.resetCurrentEntity();
        urg.resetCurrentEntity();
        price.setText("0");
        active.setSelected(false);
        descPane.setText("");
        term.setText("<unknown>");
    }
}
