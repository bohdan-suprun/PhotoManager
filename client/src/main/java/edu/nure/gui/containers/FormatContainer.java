package edu.nure.gui.containers;

import edu.nure.db.entity.Format;
import edu.nure.db.entity.Transmittable;

/**
 * Created by bod on 13.10.15.
 */
public class FormatContainer extends AbstractSuggestionContainer {

    public FormatContainer(Transmittable t) {
        super(t);
    }

    @Override
    public String getString() {
        return ((Format)t).getName();
    }

    @Override
    protected String wrap() {
        return getString();
    }
}
