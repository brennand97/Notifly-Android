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
        Cursor cursorThread = null;
        Cursor cursorConversation = null;
        Cursor cursorContact = null;

        try {
            cursorThread = context.getContentResolver().query(Uri.parse( "content://mms-sms/conversations?simple=true"), new String[] { Telephony.Threads.ARCHIVED, Telephony.Threads.DATE, Telephony.Threads.RECIPIENT_IDS },
                    Telephony.Threads._ID + " = ?", new String[]{ response.getRequestValue() }, null);
            String recipientString;

            if (cursorThread == null) {
                response.setData(null);
                return;
            }

            int archived = cursorThread.getColumnIndex(Telephony.Threads.ARCHIVED);
            int date = cursorThread.getColumnIndex(Telephony.Threads.DATE);
            int recipientIdsX = cursorThread.getColumnIndex(Telephony.Threads.RECIPIENT_IDS);

            cursorThread.moveToFirst();

            t.setArchived(cursorThread.getInt(archived) == 1);
            t.setDate(cursorThread.getString(date));
            recipientString = cursorThread.getString(recipientIdsX);
            String[] recipientIds = recipientString.split(" ");

            cursorConversation = context.getContentResolver().query(Uri.parse("content://mms-sms/canonical-addresses"), new String[]{ "address" }, "_id = ?", recipientIds, null);

            if(cursorConversation == null) {
                response.setData(null);
                return;
            }
            cursorConversation.moveToFirst();
            String[] addresses = new String[cursorConversation.getCount()];

            int address = cursorConversation.getColumnIndex("address");

            for(int i = 0; i < cursorConversation.getCount(); i++) {
                addresses[i] = cursorConversation.getString(address);
            }

            cursorContact = context.getContentResolver().query(Phone.CONTENT_URI, null, Phone.NUMBER + " = ?", addresses, null);

            if (cursorContact == null || cursorContact.getCount() == 0) {
                response.setData(null);
                return;
            }
            cursorContact.moveToFirst();

            int contactIdIdx = cursorContact.getColumnIndex(Phone._ID);
            int nameIdx = cursorContact.getColumnIndex(Phone.DISPLAY_NAME);
            int phoneNumberIdx = cursorContact.getColumnIndex(Phone.NUMBER);
            int photoIdIdx = cursorContact.getColumnIndex(Phone.PHOTO_ID);

            do {
                Contact c = new Contact();

                c.putBody(cursorContact.getString(nameIdx));
                c.putExtra(cursorContact.getString(phoneNumberIdx));
                c.setContactId(cursorContact.getInt(contactIdIdx));

                t.addContact(c);
            } while (cursorContact.moveToNext());

        } catch (NullPointerException e) {
            e.printStackTrace();

            response.setData(null);

        } finally {
            if(cursorThread != null) {
                cursorThread.close();
            }
            if (cursorContact != null) {
                cursorContact.close();
            }
        }

        response.setData(t);
        RequestHandler.getInstance(context).sendResponse(response);
    }

}