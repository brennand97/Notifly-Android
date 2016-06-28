package com.notiflyapp.data;

import java.util.ArrayList;

/**
 * Created by Brennan on 6/27/2016.
 */
public class ConversationThread extends DataObject<ArrayList<Contact>, Integer> {

    /**
     *  Body ArrayList contains Contact objects for ConversationThread.
     *  Extra Integer contains threadId for ConversationThread.
     */

    private boolean archived = false;
    private String date;

    public ConversationThread() {
        super();
        this.type = Type.CONVERSATION_THREAD;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void addContact(Contact contact) {
        body.add(contact);
    }

    public Contact[] getContacts() {
        Contact[] contacts = new Contact[body.size()];
        body.toArray(contacts);
        return contacts;
    }

    @Override
    public ArrayList<Contact> getBody() {
        return body;
    }

    @Override
    public void putBody(ArrayList<Contact> body) {
        this.body = body;
    }

    @Override
    public Integer getExtra() {
        return extra;
    }

    @Override
    public void putExtra(Integer file) {
        this.extra = file;
    }
}
