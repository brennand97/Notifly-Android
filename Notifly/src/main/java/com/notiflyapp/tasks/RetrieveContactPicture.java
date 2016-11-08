/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.tasks;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.notiflyapp.data.DataByteArray;
import com.notiflyapp.data.requestframework.RequestHandler;
import com.notiflyapp.data.requestframework.Response;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Brennan on 11/7/2016.
 */

public class RetrieveContactPicture extends Thread {

    private static final String TAG = RetrieveSms.class.getSimpleName();
    private Context context;
    private Response response;
    private long contactId;

    public RetrieveContactPicture(Context context, Response response, long contactId) {
        this.context = context;
        this.response = response;
        this.contactId = contactId;
    }

    @Override
    public void run() {
        Log.v(TAG, "Contact ID: " + contactId);
        InputStream photoInputStream = openPhoto(contactId);
        if(photoInputStream != null) {
            try {
                byte[] photoBytes = getBytes(photoInputStream);
                response.putItem(RequestHandler.RequestCode.EXTRA_PICTURE_BYTE_ARRAY, new DataByteArray(photoBytes));
                RequestHandler.getInstance(context).sendResponse(response);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.v(TAG, "photoInputStream was null.");
        }
        response.putItem(RequestHandler.RequestCode.EXTRA_PICTURE_BYTE_ARRAY, null);
        RequestHandler.getInstance(context).sendResponse(response);

    }

    public InputStream openPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] {ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            return null;
        }
        Log.v(TAG, "cursor length: " + cursor.getCount());
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return new ByteArrayInputStream(data);
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

}
