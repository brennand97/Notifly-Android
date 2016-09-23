/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.data;

import java.io.File;

/**
 * Created by Brennan on 4/17/2016.
 *
 * Used for sending Short Message Service (SMS) messages between devices
 */
public class SMS extends DataObject<String, File> {

    private static final long serialVersionUID = 3349238414148539467L;

    private int id;
    private String address;
    private String originatingAddress;
    private String creator;
    private long date;
    private long dateSent;
    private String person;
    private boolean read;
    private long subscriptionId;
    private int threadId;

    /**
     * Default constructor, degines DataObject.Type
     */
    public SMS() {
        super();
        type = Type.SMS;
    }

    /**
     * Constructor that sets sender and body on creation.
     *
     * @param address The phone number of the sender of the SMS as a String.
     * @param originatingAddress The phone number of the recipient of the message.
     * @param body The body of the SMS message as a string.
     */
    public SMS(String address, String originatingAddress, String body) {
        super();
        type = Type.SMS;
        this.address = address;
        this.originatingAddress = originatingAddress;
        this.body = body;
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

    public SMS clone() {
        SMS sms = new SMS();
        sms.setId(id);
        sms.setAddress(address);
        sms.setOriginatingAddress(originatingAddress);
        sms.setBody(body);
        sms.setCreator(creator);
        sms.setDate(date);
        sms.setDateSent(dateSent);
        sms.setPerson(person);
        sms.setRead(read);
        sms.setSubscriptionId(subscriptionId);
        sms.setThreadId(threadId);
        return sms;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOriginatingAddress() {
        return originatingAddress;
    }

    public void setOriginatingAddress(String originatingAddress) {
        this.originatingAddress = originatingAddress;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getDateSent() {
        return dateSent;
    }

    public void setDateSent(long dateSent) {
        this.dateSent = dateSent;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

}
