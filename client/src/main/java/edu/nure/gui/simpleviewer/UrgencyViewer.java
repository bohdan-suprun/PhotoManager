package edu.nure.gui.simpleviewer;

import edu.nure.db.entity.Transmittable;
import edu.nure.db.entity.Urgency;
import edu.nure.db.entity.constraints.ValidationException;
import edu.nure.gui.EditableJLabel;
import edu.nure.gui.SaveButtonObserver;
import edu.nure.gui.containers.UrgencyContainer;
import edu.nure.net.HttpManager;
import edu.nure.net.Priority;

import javax.swing.*;
import java.awt.*;

/**
 * Created by bod on 15.10.15.
 */
public class UrgencyViewer extends AbstractViewer {

    private EditableJLabel urg, factor;
    private String oldName;

    private static UrgencyViewer self;

    private UrgencyViewer() {
        super("URGENCY");
        setContent(createPanel());
    }

    public static UrgencyViewer getWindow(){
        return (self == null)? self = new UrgencyViewer(): self;
    }


    protected JPanel createPanel(){
        JPanel main = new JPanel(new GridLayout(0,2));
        main.add(new JLabel("Срочность, минут:"));
        main.add(urg = new EditableJLabel("1"));

        main.add(new JLabel("Наценка, %:"));
        main.add(factor = new EditableJLabel("0"));

        return main;
    }

    @Override
    protected void load() {
        HttpManager.getManager().sendGet("?action="+getAct,new Priority(Priority.MAX),
                getDefaultAdapter());
    }

    @Override
    protected void update() {
        HttpManager.getManager().sendGet("?action="+updAct+"&oldTerm="+oldName+"&"+curEntity.getEntity().toQuery(),new Priority(Priority.MAX),
                getDefaultAdapter());
    }

    @Override
    public void addItem(Transmittable t) {
        model.addElement(new UrgencyContainer(t));

    }

    @Override
    protected void observeSaveButton() {
        SaveButtonObserver.setListeners(new JComponent[]{urg,factor}, saveButton);
    }

    @Override
    protected Transmittable createEntity() throws ValidationException {
        try {
            return new Urgency(
                    Integer.valueOf(urg.getText()),
                    Float.valueOf(factor.getText())
            );
        } catch (NumberFormatException ex){
            throw new ValidationException("Все поля должны быть числами");
        }
    }

    @Override
    protected void entityChanged() {
        Urgency f = (Urgency)curEntity.getEntity();
        oldName = ""+f.getTerm();
        urg.setText(""+f.getTerm());
        factor.setText(""+f.getFactor());
    }

    @Override
    protected void setDefault() {
        oldName = null;
        urg.setText("1");
        factor.setText("0");
    }
}
