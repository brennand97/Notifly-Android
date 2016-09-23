/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.data;

import java.io.File;

/**
 * Created by Brennan on 4/17/2016.
 *
 * Used for sending general notifications between devices.
 */
public class Notification extends DataObject<String, File>{

    private static final long serialVersionUID = 3349238414148539470L;

    /**
     * Default constructor, defines DataObject.Type
     */
    public Notification() {
        super();
        type = Type.NOTIFICATION;
    }

    /**
     * @return The body of the DataObject as a String
     */
    @Override
    public String getBody() {
        return null;
    }

    /**
     * @param body The body of the message being sent as a String
     */
    @Override
    public void putBody(String body) {

    }

    /**
     * @return Extra data stored in the message as a File
     */
    @Override
    public File getExtra() {
        return null;
    }

    /**
     * @param file Extra data that goes along with the body as a File
     */
    @Override
    public void putExtra(File file) {

    }

}
