package com.notiflyapp.data;

import java.io.File;

/**
 * Created by Brennan on 5/7/2016.
 */
public class Request extends DataObject<Object, File> {

    private Object requestObject;

    public Request() {
        super();
        type = Type.REQUEST;
    }

    /**
     * @return The body of the DataObject as a String
     */
    @Override
    public Object getBody() {
        return requestObject;
    }

    /**
     * @param body The body of the message being sent as a String
     */
    @Override
    public void putBody(Object body) {
        requestObject = body;
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
