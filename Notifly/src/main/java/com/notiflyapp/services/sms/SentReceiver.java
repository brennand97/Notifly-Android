package com.notiflyapp.services.sms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

import com.notiflyapp.data.SMS;

import java.io.IOException;

/**
 * Created by Brennan on 5/21/2016.
 */
public class SentReceiver extends BroadcastReceiver {

    private SMS sms;

    public SentReceiver(SMS sms) {
        this.sms = sms;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String result = null;

        switch (getResultCode()) {

            case Activity.RESULT_OK:
                result = "Message successful";
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                try {
                    Intent smsIntent = new Intent(context, SmsService.class);
                    smsIntent.setAction(SmsService.ACTION_SEND_SMS_VIA_INTENT);
                    smsIntent.putExtra(SmsService.EXTRA_SMS_MESSAGE, sms.serialize());
                    context.startService(smsIntent);
                } catch (IOException e) { e.printStackTrace(); }
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