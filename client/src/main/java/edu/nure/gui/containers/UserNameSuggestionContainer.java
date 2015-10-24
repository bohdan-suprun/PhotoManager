package edu.nure.gui.containers;

import edu.nure.db.entity.Transmittable;
import edu.nure.db.entity.User;

/**
 * Created by bod on 10.10.15.
 */
public class UserNameSuggestionContainer extends AbstractSuggestionContainer {

    public UserNameSuggestionContainer(Transmittable t) {
        super(t);
    }

    @Override
    public String getString() {
        return ((User)t).getName();
    }

    @Override
    protected String wrap() {
        User user = (User)t;
        return user.getName();
    }
}

