package com.notiflyapp.services.sms;

import android.database.Cursor;
import android.os.Build;
import android.provider.Telephony;

import com.notiflyapp.data.SMS;

/**
 * Created by Brennan on 6/7/2016.
 */
public class MessageHandler {

    public static synchronized SMS getSms(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex(Telephony.Sms._ID));
        String address = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
        String body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
        String creator = cursor.getString(cursor.getColumnIndex(Telephony.Sms.CREATOR));
        long date = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE));
        long dateSent = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE_SENT));
        String person = cursor.getString(cursor.getColumnIndex(Telephony.Sms.PERSON));
        boolean read = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.READ)) == 1;
        int threadId = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.THREAD_ID));

        SMS sms = new SMS();
        sms.setId(id);
        sms.setOriginatingAddress(address);
        sms.setBody(body);
        sms.setCreator(creator);
        sms.setDate(date);
        sms.setDateSent(dateSent);
        sms.setPerson(person);
        sms.setRead(read);
        sms.setThreadId(threadId);

        if(Build.VERSION.SDK_INT >= 22) {
            long subscriptionId = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID));
            sms.setSubscriptionId(subscriptionId);
        } else {
            sms.setSubscriptionId(-1);
        }

        return sms;
    }

}
