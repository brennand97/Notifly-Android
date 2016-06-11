package com.notiflyapp.data;

/**
 * Created by Brennan on 4/17/2016.
 *
 * Used for sending Multimedia Messaging Service (MMS) messages between devices.
 */
public class MMS extends DataObject<String, Byte[]>{

    private static final long serialVersionUID = 3349238414148539468L;

    /**
     * Default constructor, defines DataObject.Type
     */
    public MMS() {
        super();
        type = Type.MMS;
    }

    /**
     * @return The body of the DataObject as a String
     */
    @Override
    public String getBody() {
        return body;
    }

    /**
     * @param body The body of the message being sent as a String
     */
    @Override
    public void putBody(String body) {
        this.body = body;
    }

    /**
     * @return Extra data stored in the message as a File
     */
    @Override
    public Byte[] getExtra() {
        return extra;
    }

    /**
     * @param extra Extra data that goes along with the body as a File
     */
    @Override
    public void putExtra(Byte[] extra) {
        this.extra = extra;
    }

}
