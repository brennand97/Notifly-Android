package com.notiflyapp.tasks;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;

import com.notiflyapp.data.requestframework.RequestHandler;
import com.notiflyapp.data.requestframework.Response;
import com.notiflyapp.data.requestframework.RequestHandler.RequestCode;

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
        Response r = response;
        Log.v(TAG, "Calling on all contacts");
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Phone.CONTENT_URI, null, null, null, null);
            int contactIdIdx = cursor.getColumnIndex(Phone._ID);
            int nameIdx = cursor.getColumnIndex(Phone.DISPLAY_NAME);
            int phoneNumberIdx = cursor.getColumnIndex(Phone.NUMBER);
            int photoIdIdx = cursor.getColumnIndex(Phone.PHOTO_ID);
            cursor.moveToFirst();

            String[] contactIds = new String[cursor.getCount()];
            String[] names = new String[cursor.getCount()];
            String[] phoneNumbers = new String[cursor.getCount()];

            do {
                contactIds[cursor.getPosition()] = cursor.getString(contactIdIdx);
                names[cursor.getPosition()] = cursor.getString(nameIdx);
                phoneNumbers[cursor.getPosition()] = cursor.getString(phoneNumberIdx);
                //...
            } while (cursor.moveToNext());

            r.putItem(RequestCode.EXTRA_CONTACT_BY_THREAD_ID_CONTACT_ID, contactIds);
            r.putItem(RequestCode.EXTRA_CONTACT_BY_THREAD_ID_NAME, names);
            r.putItem(RequestCode.EXTRA_CONTACT_BY_THREAD_ID_PHONE_NUMBER, phoneNumbers);

        } catch (Exception e) {
            e.printStackTrace();

            r.putItem(RequestCode.EXTRA_CONTACT_BY_THREAD_ID_CONTACT_ID, null);
            r.putItem(RequestCode.EXTRA_CONTACT_BY_THREAD_ID_NAME, null);
            r.putItem(RequestCode.EXTRA_CONTACT_BY_THREAD_ID_PHONE_NUMBER, null);

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        RequestHandler.getInstance(context).sendResponse(r);
    }

}