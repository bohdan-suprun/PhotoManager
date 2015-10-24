package edu.nure.performers;

/**
 * Created by bod on 18.09.15.
 */
public class Action {

    public static final int LOGIN = 1;

    public static final int INSERT_IMAGE = 100; //+
    public static final int INSERT_FORMAT = 101;//+
    public static final int INSERT_ORDER = 102;//+
    public static final int INSERT_STOCK = 103; //+
    public static final int INSERT_URGENCY = 104;//+
    public static final int REGISTRY = 105;//+
    public static final int INSERT_ALBUM = 106;//+

    public static final int GET_ORDER = 200;//+
    public static final int GET_IMAGE = 201;//+
    public static final int GET_USER = 202;//++
    public static final int GET_FORMAT = 203;//+
    public static final int GET_RIGHT = 204;//+
    public static final int GET_URGENCY = 205;//+
    public static final int GET_STOCK = 206;//+
    public static final int GET_ALBUM = 207;//+

    public static final int UPDATE_USER = 300; //+
    public static final int UPDATE_FORMAT = 301;//+
    public static final int UPDATE_ORDER = 302;//+
    public static final int UPDATE_URGENCY = 303;//+
    public static final int UPDATE_STOCK = 304;//+

    public static final int DELETE_FORMAT = 400;//+
    public static final int DELETE_ORDER = 401;//+
    public static final int DELETE_URGENCY = 402;//+
    public static final int DELETE_STOCK = 403;//
    public static final int DELETE_IMAGE = 404;//+
    public static final int DELETE_ALBUM = 405;//+

    public static final int CUSTOMER_SET_PASS = 501;
    public static final int CUSTOMER_LOGIN = 502;
    public static final int CUSTOMER_AJAX = 503;


    public static final int KILL_SESSION = 999;

}
