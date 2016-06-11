package com.notiflyapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.provider.Telephony;

import com.notiflyapp.data.DeviceInfo;
import com.notiflyapp.data.MMS;
import com.notiflyapp.data.SMS;

/**
 * Created by Brennan on 6/7/2016.
 */
public class MessagesDatabase extends Database {

    public static final String TYPE_SMS = "sms";
    public static final String TYPE_MMS = "mms";

    public static final String ID = "id";
    public static final String TYPE = "type";

    public static final String TABLE_NAME = "messages";

    public final static String CREATE_TABLE =  "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " " +
                                            "(" + ID + " INTEGER PRIMARY KEY," +
                                            TYPE + " TEXT)";

    public MessagesDatabase(Context context, SQLiteOpenHelper sqLiteOpenHelper) {
        super(context, sqLiteOpenHelper);
    }

    public void add(int id, String type) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        database.insert(TABLE_NAME, null, getContentValues(id, type));
    }

    public SMS getSms(int id) {
        Cursor cursor = context.getContentResolver().query(Telephony.Sms.CONTENT_URI, null, ID + " = ?", new String[]{String.valueOf(id)}, null, null);
        if(cursor == null || cursor.getCount() < 1) {
            return null;
        }
        cursor.moveToFirst();

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

        cursor.close();

        return sms;
    }

    public MMS getMms(int id) {
        //TODO
        return null;
    }

    public Cursor getAll() {
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, null);
        return cursor;
    }

    public void removeAll() {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    private ContentValues getContentValues(int id, String type) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, id);
        contentValues.put(TYPE, type);

        return contentValues;
    }
}
