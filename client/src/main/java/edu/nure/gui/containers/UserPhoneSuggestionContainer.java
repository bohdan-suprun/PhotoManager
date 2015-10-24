package edu.nure.gui.containers;

import edu.nure.db.entity.Transmittable;
import edu.nure.db.entity.User;

/**
 * Created by bod on 10.10.15.
 */
public class UserPhoneSuggestionContainer extends UserNameSuggestionContainer {

    public UserPhoneSuggestionContainer(Transmittable t) {
        super(t);
    }

    @Override
    public String getString() {
        return ((User)t).getPhone().replaceAll("[^0-9]","");
    }

    @Override
    public String displayString() {
        return ((User)t).getPhone();
    }

    @Override
    protected String wrap() {
        return ((User)t).getPhone();
    }
}
