/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.data.requestframework;

import com.notiflyapp.data.DataObject;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Brennan on 5/7/2016.
 */
public class Request extends DataObject<String, UUID> {

    private static final long serialVersionUID = 3349238414148539471L;

    //Body id the request string
    //Extra is UUID of specific message
    private String requestValue;
    private HashMap<String, DataObject> hashMap = new HashMap<>();

    public Request() {
        super();
        type = Type.REQUEST;
        extra = UUID.randomUUID();
    }

    /**
     * Returns data for the specified extra key string.
     *
     * @param key string associated with the request's key (body variable)
     * @return a general object that is defined by extra key
     */
    public Object getItem(String key) {
        return hashMap.get(key);
    }

    /**
     * Put's an object into response object with correct extra key associated.
     * This object can be called for with the same extra key.
     *
     * @param key defines what the object is and contains.
     * @param object the DataObject that contains data itself.
     */
    public void putItem(String key, DataObject object) {
        hashMap.put(key, object);
    }

    protected void putHashMap(HashMap<String, DataObject> map) {
        hashMap = map;
    }

    protected HashMap<String, DataObject> getHashMap() {
        return hashMap;
    }

    /**
     * Returns the value associated with the requestKey (found in body)
     *
     * @return the value corresponding to requested data
     */
    public String getRequestValue() {
        return requestValue;
    }

    /**
     * Sets the value that is associated with requestKey (found in body)
     *
     * @param requestValue that corresponds to the requested data
     */
    public void putRequestValue(String requestValue) {
        this.requestValue = requestValue;
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
    public UUID getExtra() {
        return extra;
    }

    /**
     * @param extra Extra data that goes along with the body as a File
     */
    @Override
    public void putExtra(UUID extra) {
        this.extra = extra;
    }

}
