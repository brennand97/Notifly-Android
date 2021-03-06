/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.services.sms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.util.Log;

import com.notiflyapp.data.SMS;
import com.notiflyapp.data.requestframework.Request;
import com.notiflyapp.data.requestframework.RequestHandler;
import com.notiflyapp.data.requestframework.Response;

import java.io.IOException;

/**
 * Created by Brennan on 5/21/2016.
 */
public class SentReceiver extends BroadcastReceiver {

    private SMS sms;
    private Request request;
    private Response response;

    public SentReceiver(Request request, SMS sms) {
        this.request = request;
        this.sms = sms;
        response = Response.makeResponse(request);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String result = null;

        switch (getResultCode()) {

            case Activity.RESULT_OK:
                result = "Message successful";
                sms.setDate(System.currentTimeMillis());
                SMS sentSms = null;
                Cursor cursor = context.getContentResolver().query(Telephony.Sms.Sent.CONTENT_URI, null, null, null, null);
                if(cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    sentSms = MessageHandler.getSms(cursor);
                } else {
                    sentSms = sms;
                }
                response.putItem(RequestHandler.RequestCode.EXTRA_SEND_SMS_SMSOBJECT, sentSms);
                response.putItem(RequestHandler.RequestCode.CONFIRMATION_SEND_SMS_SENT, null);
                RequestHandler.getInstance(context).sendResponse(response);
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                response.putItem(RequestHandler.RequestCode.CONFIRMATION_SEND_SMS_FAILED, null);
                RequestHandler.getInstance(context).sendResponse(response);
                result = "Message failed";
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                result = "Radio off";
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                result = "No PDU defined";
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                result = "No service";
                break;
        }

        Log.i("Debug-SentReceiver", result);
    }
}