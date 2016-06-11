package com.notiflyapp.sms;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Message;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.notiflyapp.data.SMS;
import com.notiflyapp.services.sms.MessageHandler;
import com.notiflyapp.services.sms.SmsService;

import java.io.IOException;

/**
 * Created by Brennan on 5/22/2016.
 */
public class SmsReceiver extends BroadcastReceiver {

    private final static String TAG = SmsReceiver.class.getSimpleName();
    public final static String ACTION_1 = "android.provider.Telephony.SMS_RECEIVED";
    public final static String ACTION_2 = "android.intent.action.DATA_SMS_RECEIVED";

    public final static String MESSAGE = "message";

    private int count = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Debug", "SMS Received");
        String action = intent.getAction();
        switch (action) {
            case ACTION_1:
                //Text Sms
                handleIncomingMessage(context, intent);
                break;
            case ACTION_2:
                //Binary Sms
                handleIncomingMessage(context, intent);
                break;
            default:

                break;
        }
    }

    public void handleIncomingMessage(Context context, Intent intent) {

        SmsMessage[] messages;
        messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        ContentResolver resolver = context.getContentResolver();
        Log.v(TAG, "Total messages received : " + messages.length);

        String message = "";
        if(messages.length > 1) {
            for(SmsMessage m: messages) {
                message = message + m.getMessageBody();
            }
        } else {
            message = messages[0].getMessageBody();
        }


        Cursor cursor = getMessageCursor(message, resolver);
        if (cursor == null) {
            Log.v(TAG, "message : " + message);
            count++;
            if(count >= 3) {
                Log.v(TAG, "Cursor failed to load 3 times, aborting");
            } else {
                Log.v(TAG, "Cursor failed to load, trying again in 1s");
                final SmsReceiver instance = this;
                final Context fcontext = context;
                final Intent fintent = intent;
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        instance.handleIncomingMessage(fcontext, fintent);
                    }
                };
                (new Thread(runnable)).start();
            }
            return;
        }

        cursor.moveToFirst();

        SMS sms = MessageHandler.getSms(cursor);

        cursor.close();

        Intent received = new Intent(context, SmsService.class);
        received.setAction(SmsService.ACTION_RECEIVE_SMS);
        try {
            received.putExtra(MESSAGE, sms.serialize());
        } catch (IOException e) {
            e.printStackTrace();
        }
        context.startService(received);


    }

    private Cursor getMessageCursor(String message, ContentResolver resolver) {
        Cursor cursor = resolver.query(Telephony.Sms.Inbox.CONTENT_URI, new String[]{ Telephony.Sms.Inbox._ID, Telephony.Sms.Inbox.BODY }, null, null, null);
        if(cursor == null) {
            return null;
        }
        int id = -1;
        while (cursor.moveToNext()) {
            if(cursor.getString(cursor.getColumnIndex(Telephony.Sms.Inbox.BODY)).trim().equals(message.trim())) {
                id = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.Inbox._ID));
                break;
            }
        }
        if(id == -1) {
            return null;
        } else {
            Cursor result = resolver.query(Telephony.Sms.Inbox.CONTENT_URI, null, null, null, null);
            return result;
        }
    }

}
