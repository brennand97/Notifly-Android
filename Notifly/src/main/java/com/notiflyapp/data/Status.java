package com.notiflyapp.data;

import java.util.UUID;

/**
 * Created by Brennan on 6/25/2016.
 */
public class Status extends DataObject<String, UUID> {

    public final static class StatusCode {
        public final static String SUCCESS = "success";
    }

    public Status(String code) {
        super();
        this.type = Type.STATUS;
        body = code;
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
    public UUID getExtra() {
        return extra;
    }

    @Override
    public void putExtra(UUID extra) {
        this.extra = extra;
    }
}
