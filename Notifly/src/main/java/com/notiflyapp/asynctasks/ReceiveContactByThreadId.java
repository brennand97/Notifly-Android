package com.notiflyapp.asynctasks;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import com.notiflyapp.data.requestframework.RequestHandler;
import com.notiflyapp.data.requestframework.Response;
import com.notiflyapp.data.requestframework.RequestHandler.RequestCode;

import java.util.ArrayList;

/**
 * Created by Brennan on 6/25/2016.
 */
public class ReceiveContactByThreadId extends AsyncTask<Response, Response, Response> {

    private Context context;

    public ReceiveContactByThreadId(Context context) {
        this.context = context;
    }

    @Override
    protected Response doInBackground(Response... params) {
        Response r = params[0];

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

        return r;
    }

    @Override
    protected void onPostExecute(Response r) {
        RequestHandler.getInstance(context).sendResponse(r);
    }
}