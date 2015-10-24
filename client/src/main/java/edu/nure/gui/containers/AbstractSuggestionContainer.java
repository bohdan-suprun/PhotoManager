package edu.nure.gui.containers;

import edu.nure.db.entity.Transmittable;

/**
 * Created by bod on 10.10.15.
 */
public abstract class AbstractSuggestionContainer extends AbstractContainer {

    public AbstractSuggestionContainer(Transmittable t) {
        super(t);
    }

    public abstract String getString();

    public String displayString(){
        return getString();
    }
}
