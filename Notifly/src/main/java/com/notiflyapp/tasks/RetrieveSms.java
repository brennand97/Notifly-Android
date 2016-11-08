/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.tasks;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.Telephony;
import android.util.Log;

import com.notiflyapp.data.DataObject;
import com.notiflyapp.data.SMS;
import com.notiflyapp.data.requestframework.RequestHandler;
import com.notiflyapp.data.requestframework.Response;
import com.notiflyapp.services.sms.MessageHandler;
import com.notiflyapp.services.sms.SmsService;
import com.notiflyapp.sms.SmsReceiver;

import java.io.IOException;

/**
 * Created by Brennan on 10/5/2016.
 */

public class RetrieveSms extends Thread {

    private static final String TAG = RetrieveSms.class.getSimpleName();
    private Context context;
    private Response response;
    private long startTime;
    private int messageCount;
    private String threadId;
    private String inequality;

    public RetrieveSms(Context context, Response response, long startTime, int messageCount, String threadId, String inequality) {
        this.context = context;
        this.response = response;
        this.startTime = startTime;
        this.messageCount = messageCount;
        this.threadId = threadId;
        this.inequality = inequality;
    }

    @Override
    public void run() {
        //TODO retrieve sms called for
        Cursor cursorThread = null;

        try {
            cursorThread = context.getContentResolver().query(Telephony.Sms.CONTENT_URI,
                    null, Telephony.Sms.THREAD_ID + " = ? AND " + Telephony.Sms.DATE + inequality + "?", new String[]{ threadId, String.valueOf(startTime) }, null);

            if(cursorThread == null) {
                Log.v(TAG, "cursor null");
                return;
            }

            if(cursorThread.getCount() <= 0) {
                Log.v(TAG, "cursor empty");
                return;
            }

            if(messageCount < 0) {
                messageCount = cursorThread.getCount();
            }
            Log.i(TAG, cursorThread.getCount() + " messages found for thread id " + threadId);
            cursorThread.moveToFirst();
            DataObject[] messages = new DataObject[messageCount];
            for(int i = 0; i < messageCount; i++) {
                messages[i] = MessageHandler.getSms(cursorThread);
                if(cursorThread.isLast()) {
                    break;
                } else {
                    cursorThread.moveToNext();
                }
            }

            for(int i = messages.length - 1; i >= 0; i--) {
                sendResponse((SMS) messages[i]);
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

    private void sendResponse(SMS sms) {
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
