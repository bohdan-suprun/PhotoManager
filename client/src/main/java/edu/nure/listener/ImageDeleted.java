package edu.nure.listener;

import edu.nure.db.entity.Image;

/**
 * Created by bod on 16.10.15.
 */
public interface ImageDeleted {
    void imageDeleted(Image im);
}
