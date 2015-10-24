package edu.nure.gui.containers;

import edu.nure.db.entity.Transmittable;

/**
 * Created by bod on 09.10.15.
 */
abstract public class AbstractContainer {
    protected Transmittable t;

    public AbstractContainer(Transmittable t){
        this.t = t;
    }

    @Override
    public String toString() {
        return wrap();
    }

    public Transmittable getEntity(){
        return t;
    }

    public void setEntity(Transmittable t){
        this.t = t;
    }

    abstract protected String wrap();
}
