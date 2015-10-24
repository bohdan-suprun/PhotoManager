package edu.nure.listener;

import edu.nure.listener.results.DBResult;
import edu.nure.listener.results.DBSelectResult;

/**
 * Created by bod on 01.10.15.
 */
public interface ResponseListener {

    void doSelect(DBSelectResult result);
    void doInsert(DBSelectResult result);
    void doUpdate(DBResult result);
    void doDelete(DBResult result);
    void doError(DBResult result);
    void doBinaryImage(byte[] image);
}
