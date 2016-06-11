package com.notiflyapp.services.sms;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.notiflyapp.data.MMS;
import com.notiflyapp.data.SMS;
import com.notiflyapp.data.Serial;
import com.notiflyapp.services.bluetooth.connection.BluetoothService;
import com.notiflyapp.services.bluetooth.scan.BluetoothScanService;
import com.notiflyapp.sms.SmsReceiver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Brennan on 5/18/2016.
 */
public class SmsService extends Service {

    private static final String TAG = SmsService.class.getSimpleName();

    public static final String EXTRA_SMS_MESSAGE = "com.notiflyapp.services.sms.SmsService.EXTRA_SMS_MESSAGE";
    public static final String ACTION_RECEIVE_SMS = "com.notiflyapp.services.sms.SmsService.ACTION_RECEIVE_SMS";
    public static final String EXTRA_MMS_MESSAGE = "com.notiflyapp.services.sms.SmsService.EXTRA_MMS_MESSAGE";
    public static final String ACTION_RECEIVE_MMS = "com.notiflyapp.services.sms.SmsService.ACTION_RECEIVE_MMS";
    public static final String ACTION_RETRIEVE_ALL_UNREAD_MESSAGES = "com.notiflyapp.services.sms.SmsService.ACTION_RETRIEVE_ALL_UNREAD_MESSAGES";
    public static final String ACTION_MESSAGE_AVAILABLE = "com.notiflyapp.services.sms.SmsService.ACTION_MESSAGE_AVAILABLE";
    public static final String READ_PHONE_STATE_PERMISSION_GRANTED = "com.notiflyapp.services.sms.SmsService.READ_PHONE_STATE_PERMISSION_GRANTED";
    public static final String ACTION_SEND_SMS = "com.notiflyapp.services.sms.SmsService.ACTION_SEND_SMS";
    public static final String ACTION_SEND_SMS_VIA_INTENT = "com.notiflyapp.services.sms.SmsService.ACTION_SEND_SMS_VIA_INTENT";

    private SmsManager smsManager;
    private SmsReceiver smsReceiver;
    private MmsReceiver mmsReceiver;
    private SentReceiver sentReceiver;
    private DeliveredReceiver deliveredReceiver;

    public ArrayBlockingQueue<Integer> queuedMessages = new ArrayBlockingQueue<>(10240);
    public HashMap<Integer, String> messageType = new HashMap<>();
    private final static String TYPE_SMS = "sms";
    private final static String TYPE_MMS = "mms";

    @Override
    public void onCreate() {

        smsManager = SmsManager.getDefault();

        IntentFilter intentFilter;

        smsReceiver = new SmsReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(SmsReceiver.ACTION_2);
        intentFilter.addAction(SmsReceiver.ACTION_2);
        registerReceiver(smsReceiver, intentFilter);

        mmsReceiver = new MmsReceiver();
        intentFilter = new IntentFilter(MmsReceiver.ACTION);
        registerReceiver(mmsReceiver, intentFilter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent != null) {
            String action = intent.getAction();
            if(action != null) {
                switch (action) {
                    case ACTION_RECEIVE_SMS:
                        try {
                            forwardMessage((SMS) Serial.deserialize(intent.getByteArrayExtra(SmsReceiver.MESSAGE)));
                            Log.i("Debug-SmsService", "Message forwarded");
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        Intent inform = new Intent(this, BluetoothService.class);
                        inform.setAction(ACTION_MESSAGE_AVAILABLE);
                        startService(inform);
                        break;
                    case ACTION_RETRIEVE_ALL_UNREAD_MESSAGES:
                        forwardUnreadMessages();
                        Log.i("Debug-SmsService", "All unread messages in threads called for");
                        break;
                    case READ_PHONE_STATE_PERMISSION_GRANTED:
                        TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                        break;
                    case ACTION_SEND_SMS:
                        try {
                            SMS toSend = (SMS) Serial.deserialize(intent.getByteArrayExtra(EXTRA_SMS_MESSAGE));
                            sendSMS(toSend);
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        break;
                    case ACTION_SEND_SMS_VIA_INTENT:
                        try {
                            SMS toSend = (SMS) Serial.deserialize(intent.getByteArrayExtra(EXTRA_SMS_MESSAGE));
                            sendSMSViaIntent(toSend);
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        unregisterReceiver(smsReceiver);
        unregisterReceiver(mmsReceiver);
        unregisterReceiver(sentReceiver);
        unregisterReceiver(deliveredReceiver);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public synchronized void forwardMessage(MMS message) {
        try {
            Intent bluetoothService = new Intent(this, BluetoothService.class);
            bluetoothService.putExtra(EXTRA_MMS_MESSAGE, message.serialize());
            bluetoothService.setAction(ACTION_RECEIVE_MMS);
            startService(bluetoothService);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void forwardMessage(SMS message) {
        try {
            Intent bluetoothService = new Intent(this, BluetoothService.class);
            bluetoothService.putExtra(EXTRA_SMS_MESSAGE, message.serialize());
            bluetoothService.setAction(ACTION_RECEIVE_SMS);
            startService(bluetoothService);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void forwardUnreadMessages() {
        Cursor cursor = getContentResolver().query(Telephony.Sms.Inbox.CONTENT_URI, null, Telephony.Sms.READ + " = ?", new String[]{"0"}, null, null);
        if(cursor != null) {
            if(cursor.getCount() > 0) {
                cursor.moveToLast();
                do {
                    SMS sms = MessageHandler.getSms(cursor);
                    forwardMessage(sms);
                } while (cursor.moveToPrevious());
            }
            Log.v(TAG, "Forwarded " + cursor.getCount() + " total sms");
            cursor.close();
        }

        cursor = getContentResolver().query(Telephony.Mms.Inbox.CONTENT_URI, null, Telephony.Mms.READ + " = ?", new String[]{"0"}, null, null);
        if(cursor != null) {
            if(cursor.getCount() > 0) {
                cursor.moveToLast();
                do {
                    //MMS mms = MessageHandler.getMms(cursor);
                    //forwardMessage(mms);
                } while (cursor.moveToPrevious());
            }
            Log.v(TAG, "Forwarded " + cursor.getCount() + " total mms");
            cursor.close();
        }
    }

    private void sendSMS(SMS toSend) {

        String SENT = "sent";
        String DELIVERED = "delivered";

        ArrayList<String> bodyParts = smsManager.divideMessage(toSend.getBody());
        ArrayList<SMS> brokenSms = new ArrayList<>();
        for(String part: bodyParts) {
            brokenSms.add(new SMS(toSend.getAddress(), toSend.getOriginatingAddress(), part));
        }

        ArrayList<PendingIntent> sentPIs = new ArrayList<>();
        for(int i = 0; i < bodyParts.size(); i++) {
            Intent intent = new Intent(SENT);
            sentPIs.add(PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));

            registerReceiver(new SentReceiver(brokenSms.get(i)), new IntentFilter(SENT));
        }

        ArrayList<PendingIntent> deliveryPIs = new ArrayList<>();
        for(int i = 0; i < bodyParts.size(); i++) {
            Intent intent = new Intent(SENT);
            deliveryPIs.add(PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));

            registerReceiver(new DeliveredReceiver(), new IntentFilter(DELIVERED));
        }

        Log.i("Debug-SmsService", "Message sent to " + toSend.getAddress());

        smsManager.sendMultipartTextMessage(toSend.getAddress(), toSend.getOriginatingAddress(), bodyParts, sentPIs, deliveryPIs);
    }

    private void sendSMSViaIntent(SMS toSend) {
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + toSend.getAddress()));
        smsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(smsIntent);
    }

}

/*  EXAMPLE SMS SENT/DELIVERED INTENT
            String SENT = "SMS_SENT";
            String DELIVERED = "SMS_DELIVERED";

                PendingIntent sentPI = PendingIntent.getBroadcast(
                        ClipResultActivity.this, 0, new Intent(SENT), 0);

                PendingIntent deliveredPI = PendingIntent.getBroadcast(
                        ClipResultActivity.this, 0, new Intent(DELIVERED),
                        0);

                // ---when the SMS has been sent---
                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context arg0, Intent arg1) {
                        if (getResultCode() == Activity.RESULT_OK)
                            Toast.makeText(getBaseContext(), "SMS sent",
                                    Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getBaseContext(),
                                    "SMS not sent", Toast.LENGTH_SHORT)
                                    .show();
                    }
                }, new IntentFilter(SENT));

                // ---when the SMS has been delivered---
                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context arg0, Intent arg1) {
                        if (getResultCode() == Activity.RESULT_OK)
                            Toast.makeText(getBaseContext(),
                                    "SMS delivered", Toast.LENGTH_SHORT)
                                    .show();
                        else
                            Toast.makeText(getBaseContext(),
                                    "SMS not delivered", Toast.LENGTH_SHORT)
                                    .show();
                    }
                }, new IntentFilter(DELIVERED));

                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(PHONE_NO, null, SMS_BODY, sentPI,
                        deliveredPI);
 */
