package com.notiflyapp.services.sms;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.NotificationInd;
import com.google.android.mms.pdu.NotifyRespInd;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Brennan on 5/22/2016.
 */

public class MmsReceiver extends BroadcastReceiver {

    public final static String ACTION = "android.provider.Telephony.WAP_PUSH_RECEIVED";
    private final String DEBUG_TAG = getClass().getSimpleName();
    private static final String MMS_DATA_TYPE_1 = "application/vnd.wap.mms-message";
    private static final String MMS_DATA_TYPE_2 = "application/vnd.wap.sic";
    private static final String TAG = MmsReceiver.class.getSimpleName();

    private boolean perferWifi = false;
    private boolean wifiConnected = true;

    // Retrieve MMS
    public void onReceive(Context context, Intent intent) {

        byte[] data = intent.getByteArrayExtra("data");

        displayAll(data);

    }

    private void displayAll(byte[] data) {

        PduParser  parser = new PduParser(data);
        GenericPdu pdu    = null;

        try {
            pdu = parser.parse();
        } catch (RuntimeException e) {
            Log.w(TAG, e);
        }

        if(isNotification(pdu)) {
            NotificationInd notif = (NotificationInd) pdu;
            String address = new String(notif.getFrom().getTextString());
            int pduType = notif.getMessageType();
            String contentLocation = new String(notif.getContentLocation());
            String transactionId = new String(notif.getTransactionId());

            Log.v(TAG, "address : " + address);
            Log.v(TAG, "pduType : " + pduType);
            Log.v(TAG, "contentLocation : " + contentLocation);
            Log.v(TAG, "transactionId : " + transactionId);
        } else {
            Log.w(TAG, "Received MMS not notification");
        }

    }

    private String getMmsText(Context context, String id) {
        Uri partURI = Uri.parse("content://mms/part/" + id);
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try {
            is = context.getContentResolver().openInputStream(partURI);
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                BufferedReader reader = new BufferedReader(isr);
                String temp = reader.readLine();
                while (temp != null) {
                    sb.append(temp);
                    temp = reader.readLine();
                }
            }
        } catch (IOException e) {}
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {}
            }
        }
        return sb.toString();
    }

    private boolean isNotification(GenericPdu pdu) {
        return pdu != null && pdu.getMessageType() == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND;
    }

}
