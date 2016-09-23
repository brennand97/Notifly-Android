/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.data;

import java.io.*;

/**
 * Created by Brennan on 4/17/2016.
 *
 * Abstract class for all data to be sent between devices.  Specific instances of data should extend this class.
 */
public abstract class DataObject< A, B > implements Serializable {

    private static final long serialVersionUID = 3349238414148539466L;
    protected A body;
    protected B extra;


    /**
     * Type provides each possible instance's type of DataObject
     */
    public final static class Type {
        public final static String SMS = "sms";
        public final static String MMS = "mms";
        public final static String NOTIFICATION = "notification";
        public final static String DEVICE_INFO = "device_info";
        public final static String REQUEST = "request";
        public final static String RESPONSE = "response";
        public final static String CONTACT = "contact";
        public final static String CONVERSATION_THREAD = "conversation_thread";
    }


    /**
     * Stores the instance's type
     */
    protected String type;


    /**
     * Default Conductor
     */
    public DataObject() {}


    /**
     *
     * @return DataObject.Type, the type of DataObject for the current instance
     */
    public String getType() {
        return  type;
    }


    /**
     *
     * @return The body of the DataObject as a String
     */
    public abstract A getBody();

    /**
     *
     * @param body The body of the message being sent as a String
     */
    public abstract void putBody(A body);

    /**
     *
     * @return Extra data stored in the message as a File
     */
    public abstract B getExtra();

    /**
     *
     * @param file Extra data that goes along with the body as a File
     */
    public abstract void putExtra(B file);


    /**
     *
     * @return  The serialized DataObject in a byte array
     * @throws IOException
     */
    public byte[] serialize() throws IOException {
        return Serial.serialize(this);
    }


    /**
     *
     * @param data  Serialized for of a DataObject as a byte array
     * @return  The dematerialized DataObject
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static DataObject deserialize(byte[] data) throws  IOException, ClassNotFoundException {
        return (DataObject) Serial.deserialize(data);
    }

}
