package com.notiflyapp.tasks;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.Telephony;
import android.util.Log;

import com.notiflyapp.data.Contact;
import com.notiflyapp.data.ConversationThread;
import com.notiflyapp.data.requestframework.RequestHandler;
import com.notiflyapp.data.requestframework.Response;
import com.notiflyapp.data.requestframework.RequestHandler.RequestCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Brennan on 6/25/2016.
 */
public class ReceiveContactByThreadId extends Thread {

    private static final String TAG = ReceiveContactByThreadId.class.getSimpleName();
    private Context context;

    private Response response;

    public ReceiveContactByThreadId(Context context, Response response) {
        this.context = context;
        this.response = response;
    }

    @Override
    public void run() {
        ConversationThread t = new ConversationThread();
        t.putBody(new ArrayList<Contact>());
        t.putExtra(Integer.parseInt(response.getRequestValue()));
        Cursor cursorThread = null;
        Cursor cursorConversation = null;
        Cursor cursorContact = null;

        try {
            cursorThread = context.getContentResolver().query(Uri.parse( "content://mms-sms/conversations?simple=true"),
                    new String[] { Telephony.Threads.ARCHIVED, Telephony.Threads.DATE, Telephony.Threads.RECIPIENT_IDS },
                    Telephony.Threads._ID + " = ?", new String[]{ response.getRequestValue() }, null);
            String recipientString;

            if (cursorThread == null || cursorThread.getCount() == 0) {
                response.putItem(RequestCode.EXTRA_CONTACT_BY_THREAD_ID_THREAD, null);
                RequestHandler.getInstance(context).sendResponse(response);
                return;
            }

            cursorThread.moveToFirst();

            t.setArchived(cursorThread.getInt(cursorThread.getColumnIndex(Telephony.Threads.ARCHIVED)) == 1);
            t.setDate(cursorThread.getString(cursorThread.getColumnIndex(Telephony.Threads.DATE)));
            recipientString = cursorThread.getString(cursorThread.getColumnIndex(Telephony.Threads.RECIPIENT_IDS));
            String[] recipientIds = recipientString.split(" ");

            Log.v(TAG, "ThreadId: " + response.getRequestValue() + " yielded " + recipientIds.length + " recipientIds: " + Arrays.toString(recipientIds));

            cursorConversation = context.getContentResolver().query(Uri.parse("content://mms-sms/canonical-addresses"), new String[]{ "address" }, "_id = ?", recipientIds, null);

            if(cursorConversation == null || cursorConversation.getCount() == 0) {
                response.putItem(RequestCode.EXTRA_CONTACT_BY_THREAD_ID_THREAD, null);
                RequestHandler.getInstance(context).sendResponse(response);
                return;
            }

            cursorConversation.moveToFirst();
            HashSet<String> addresses = new HashSet<>();

            int address = cursorConversation.getColumnIndex("address");

            for(int i = 0; i < cursorConversation.getCount(); i++) {
                String a = cursorConversation.getString(address);
                Log.v(TAG, a);
                addresses.add(stripPhoneNumber(a));
                cursorConversation.moveToNext();
            }

            Log.v(TAG, "ThreadId: " + response.getRequestValue() + " yielded " + addresses.size() + " addresses: " + addresses);

            cursorContact = context.getContentResolver().query(Phone.CONTENT_URI, null, null, null, null);

            if (cursorContact == null || cursorContact.getCount() == 0) {
                for(String adr: addresses) {
                    Contact c = new Contact();
                    c.putExtra(adr);

                    t.addContact(c);
                }
                response.putItem(RequestCode.EXTRA_CONTACT_BY_THREAD_ID_THREAD, t);
                RequestHandler.getInstance(context).sendResponse(response);
                return;
            }

            cursorContact.moveToFirst();

            int contactIdIdx = cursorContact.getColumnIndex(Phone._ID);
            int nameIdx = cursorContact.getColumnIndex(Phone.DISPLAY_NAME);
            int phoneNumberIdx = cursorContact.getColumnIndex(Phone.NUMBER);
            int photoIdIdx = cursorContact.getColumnIndex(Phone.PHOTO_ID);

            do {
                String contactNumber = cursorContact.getString(phoneNumberIdx);
                if(addresses.contains(stripPhoneNumber(contactNumber))) {
                    Contact c = new Contact();

                    c.putBody(cursorContact.getString(nameIdx));
                    c.putExtra(contactNumber);
                    c.setContactId(cursorContact.getInt(contactIdIdx));

                    t.addContact(c);
                }
            } while (cursorContact.moveToNext());

            if(t.getContacts().length < addresses.size()) {
                for(String adr: addresses) {
                    for(int j = 0; j < t.getContacts().length; j++) {
                        if(t.getContacts()[j].getExtra().equals(adr)) {
                            continue;
                        }
                    }
                    Contact c = new Contact();
                    c.setContactId(-1);
                    c.putExtra(formatPhoneNumber(adr));

                    t.addContact(c);
                }
            }

            Log.v(TAG, "ThreadId: " + response.getRequestValue() + " yielded " + t.getContacts().length + " contacts: " + Arrays.toString(t.getContacts()));

        } catch (NullPointerException e) {
            e.printStackTrace();

            response.putItem(RequestCode.EXTRA_CONTACT_BY_THREAD_ID_THREAD, null);

        } finally {
            if(cursorThread != null) {
                cursorThread.close();
            }
            if (cursorContact != null) {
                cursorContact.close();
            }
        }

        response.putItem(RequestCode.EXTRA_CONTACT_BY_THREAD_ID_THREAD, t);
        RequestHandler.getInstance(context).sendResponse(response);
    }

    private String stripPhoneNumber(String s) {
        String raw =  s.replace(" ", "").replace("(", "").replace(")", "").replace("+", "").replace("-", "");
        if(raw.substring(0, 1).equals("1") && raw.length() == 11 && s.contains("+")) {
            raw = raw.substring(1);
        }
        return raw;
    }

    private String formatPhoneNumber(String s) {
        String raw = stripPhoneNumber(s);
        StringBuilder b = new StringBuilder();
        if(raw.length() == 10) {
            b.append("(");
            b.append(raw.substring(0, 3));
            b.append(")");
            b.append(" ");
            b.append(raw.substring(3, 6));
            b.append("-");
            b.append(raw.substring(6));
        } else if(raw.length() >= 11) {
            b.append(raw.substring(0, raw.length() - 10));
            b.append(" ");
            b.append(raw.substring(raw.length() - 10, raw.length() - 7));
            b.append("-");
            b.append(raw.substring(raw.length() - 7, raw.length() - 4));
            b.append("-");
            b.append(raw.substring(raw.length() - 4));
        }
        return b.toString();
    }

    private void displayAll() {
        Cursor cursorContact = context.getContentResolver().query(Phone.CONTENT_URI, null, null, null, null);
        if(cursorContact == null || cursorContact.getCount() == 0) {
            return;
        }
        cursorContact.moveToFirst();
        int nameIdx = cursorContact.getColumnIndex(Phone.DISPLAY_NAME);
        int phoneNumberIdx = cursorContact.getColumnIndex(Phone.NUMBER);
        do {
            Log.v(TAG, cursorContact.getString(nameIdx) + ", " + cursorContact.getString(phoneNumberIdx));
        } while (cursorContact.moveToNext());
    }
}