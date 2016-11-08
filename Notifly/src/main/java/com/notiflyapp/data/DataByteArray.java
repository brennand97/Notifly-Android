/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.data;

import java.util.ArrayList;

/**
 * Created by Brennan on 10/30/2016.
 */

public class DataByteArray extends DataObject<ArrayList<Byte>, String> {

    public DataByteArray(byte[] body) {
        super();
        this.type = Type.DATA_BYTE_ARRAY;
        this.body = new ArrayList<>();
        for(byte b: body) {
            this.body.add(b);
        }
    }

    public byte[] getByteArray() {
        byte[] out = new byte[this.body.size()];
        for(int i = 0; i < this.body.size(); i++) {
            out[i] = this.body.get(i);
        }
        return out;
    }

    @Override
    public ArrayList<Byte> getBody() {
        return body;
    }

    @Override
    public void putBody(ArrayList<Byte> body) {
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
