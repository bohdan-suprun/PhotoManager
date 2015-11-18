package edu.nure.db.entity;

import java.io.Serializable;

/**
 * Created by bod on 17.09.15.
 */
public interface Transmittable extends Serializable, DBEntity{

    String toXML();
    String toQuery();
}
