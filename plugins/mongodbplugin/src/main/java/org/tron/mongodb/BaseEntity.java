package org.tron.mongodb;

import java.io.Serializable;

public class BaseEntity implements Serializable {

    private Object _id;

    public Object get_id() {
        return _id;
    }

    public void set_id(Object _id) {
        this._id = _id;
    }

}
