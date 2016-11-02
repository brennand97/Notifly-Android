/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.tasks;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.util.Log;

import com.notiflyapp.data.Contact;
import com.notiflyapp.data.DataString;
import com.notiflyapp.data.SMS;
import com.notiflyapp.data.requestframework.RequestHandler;
import com.notiflyapp.data.requestframework.Response;
import com.notiflyapp.services.sms.MessageHandler;
import com.notiflyapp.services.sms.SmsService;
import com.notiflyapp.sms.SmsReceiver;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Brennan on 10/5/2016.
 */

public class RetrievePreviousSms extends Thread {

    private static final String TAG = RetrievePreviousSms.class.getSimpleName();
    private Context context;
    private Response response;
    private long startTime;
    private int messageCount;
    private String threadId;

    public RetrievePreviousSms(Context context, Response response, long startTime, int messageCount, String threadId) {
        this.context = context;
        this.response = response;
        this.startTime = startTime;
        this.messageCount = messageCount;
        this.threadId = threadId;
    }

    @Override
    public void run() {
        //TODO retrieve sms called for
        Cursor cursorThread = null;

        try {
            cursorThread = context.getContentResolver().query(Telephony.Sms.CONTENT_URI,
                    null, Telephony.Sms._ID + " = ? AND " + Telephony.Sms.DATE + " < ?", new String[]{ threadId, String.valueOf(startTime) }, null);

            if(cursorThread == null) {
                Log.v(TAG, "cursor null");
                return;
            }

            if(cursorThread.getCount() <= 0) {
                Log.v(TAG, "cursor empty");
                return;
            }

            cursorThread.moveToFirst();

            for(int i = 0; i < messageCount; i++) {
                sendResponse(cursorThread);
                if(cursorThread.isLast()) {
                    break;
                } else {
                    cursorThread.moveToNext();
                }
            }

        } catch (NullPointerException e) {
            e.printStackTrace();

            response.putItem(RequestHandler.RequestCode.EXTRA_CONTACT_BY_THREAD_ID_THREAD, null);

        } finally {
            if(cursorThread != null) {
                cursorThread.close();
            }
        }
    }

    private void sendResponse(Cursor cursor) {
        SMS sms = MessageHandler.getSms(cursor);

        Intent received = new Intent(context, SmsService.class);
        received.setAction(SmsService.ACTION_RECEIVE_SMS);
        try {
            received.putExtra(SmsReceiver.MESSAGE, sms.serialize());
        } catch (IOException e) {
            e.printStackTrace();
        }
        context.startService(received);
    }

}
