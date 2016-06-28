package com.notiflyapp.data;

/**
 * Created by Brennan on 6/27/2016.
 */
public class Contact extends DataObject<String, String> {

    /**
     *  Body String represents name of contact.
     *  Extra String represents phone number of contact
     */

    private int contactId;

    public Contact() {
        super();
        this.type = Type.CONTACT;
    }

    public void setContactId(int i) {
        contactId = i;
    }

    public int getContactId() {
        return contactId;
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
