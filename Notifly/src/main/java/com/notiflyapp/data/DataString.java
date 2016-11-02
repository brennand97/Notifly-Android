/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.data;

/**
 * Created by Brennan on 10/30/2016.
 */

public class DataString extends DataObject<String, String> {

    public DataString(String body) {
        super();
        this.type = Type.DATA_STRING;
        this.body = body;
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public void putBody(String body) {
        this.body = body;
    }

    @Override
    public String getExtra() {
        return extra;
    }

    @Override
    public void putExtra(String file) {
        this.extra = file;
    }
}
