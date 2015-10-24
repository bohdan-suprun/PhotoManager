package edu.nure.gui.containers;

import edu.nure.db.entity.Transmittable;
import edu.nure.db.entity.Urgency;

/**
 * Created by bod on 10.10.15.
 */
public class UrgencyContainer extends AbstractSuggestionContainer {

    public UrgencyContainer(Transmittable t) {
        super(t);
    }

    @Override
    protected String wrap() {
        Urgency u = (Urgency)t;
        if(u.getTerm() < 60)
            return u.getTerm()+" мин.";
        if(u.getTerm() >= 60 && u.getTerm() < 60*24)
            return u.getTerm()/60.0+" часов";
        if(u.getTerm() >= 60*24)
            return u.getTerm()/(60.0*24)+" дней";
        return null;
    }

    @Override
    public String getString() {
        return displayString();
    }

    @Override
    public String displayString() {
        return wrap();
    }
}
