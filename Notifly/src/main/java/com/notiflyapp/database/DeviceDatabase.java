/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.notiflyapp.data.DeviceInfo;

/**
 * Created by Brennan on 6/1/2016.
 */
public class DeviceDatabase extends Database {

    public final static String TABLE_NAME = "devices";
    public final static String ID = "id";
    public final static String MAC_ADDRESS = "mac";
    public final static String NAME = "name";
    public final static String TYPE = "type";
    public final static String CONNECT = "connect";
    public final static String SMS = "sms";
    public final static String NOTIFICATION = "notifications";

    private final static String[] columnArray = new String[]{ ID, MAC_ADDRESS, NAME, TYPE, CONNECT, SMS, NOTIFICATION };

    public final static String CREATE_TABLE =  "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " " +
                                        "(" + ID + " INTEGER PRIMARY KEY," +
                                        MAC_ADDRESS + " TEXT," +
                                        NAME + " TEXT," +
                                        TYPE + " INTEGER," +
                                        CONNECT + " INTEGER," +
                                        SMS + " INTEGER," +
                                        NOTIFICATION + " INTEGER)";

    public DeviceDatabase(Context context, SQLiteOpenHelper sqLiteOpenHelper) {
        super(context, sqLiteOpenHelper);
    }

    public boolean hasDevice(DeviceInfo info) throws NullMacAddressException {
        if(info.getDeviceMac() == null) {
            throw NullMacAddressException.makeException();
        }

        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        Cursor cursor = null;

        cursor = database.query(TABLE_NAME, new String[]{MAC_ADDRESS}, null, null, null, null, null);

        if(cursor != null) {
            while (cursor.moveToNext()) {
                if(cursor.getString(cursor.getColumnIndex(MAC_ADDRESS)).equals(info.getDeviceMac())) {
                    return true;
                }
            }
        }

        return false;
    }

    public DeviceInfo getDeviceInfo(int id) throws DeviceNotFoundException, NullCursorException {
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        Cursor cursor;

        cursor = database.query(TABLE_NAME, new String[]{NAME, MAC_ADDRESS, TYPE, CONNECT, SMS, NOTIFICATION}
                , ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndex(NAME));
                String mac_address = cursor.getString(cursor.getColumnIndex(MAC_ADDRESS));
                int type = cursor.getInt(cursor.getColumnIndex(TYPE));
                boolean connect = cursor.getInt(cursor.getColumnIndex(CONNECT)) == 1;
                boolean sms = cursor.getInt(cursor.getColumnIndex(SMS)) == 1;
                boolean notification = cursor.getInt(cursor.getColumnIndex(NOTIFICATION)) == 1;

                DeviceInfo info = new DeviceInfo();
                info.setDeviceName(name)
                        .setDeviceMac(mac_address)
                        .setDeviceType(type)
                        .setOptionConnect(connect)
                        .setOptionSMS(sms)
                        .setOptionNotification(notification);
                return info;
            } else {
                throw DeviceNotFoundException.makeException(TABLE_NAME, ID, String.valueOf(id));
            }
        } else {
            throw NullCursorException.makeException(TABLE_NAME, new String[]{NAME, MAC_ADDRESS, TYPE, CONNECT, SMS, NOTIFICATION});
        }
    }

    public DeviceInfo getDeviceInfo(String macAddress) throws DeviceNotFoundException, NullCursorException {
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        Cursor cursor;

        cursor = database.query(TABLE_NAME, new String[]{ID}, MAC_ADDRESS + " = ?", new String[]{macAddress}, null, null, null);

        if(cursor != null) {
            if(cursor.moveToFirst()) {
                return getDeviceInfo(cursor.getInt(cursor.getColumnIndex(ID)));
            } else {
                throw DeviceNotFoundException.makeException(TABLE_NAME, MAC_ADDRESS, macAddress);
            }
        } else {
            throw NullCursorException.makeException(TABLE_NAME, new String[]{ID}, new String[]{MAC_ADDRESS}, new String[]{macAddress});
        }
    }

    public DeviceInfo[] getAll() throws NullCursorException, DeviceNotFoundException {
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        Cursor cursor;

        cursor = database.query(TABLE_NAME, null, null, null, null, null, null);

        if(cursor != null) {
            if(cursor.moveToFirst()) {
                int numNull = 0;
                DeviceInfo[] infos = new DeviceInfo[cursor.getCount()];
                for(int i = 0; i < cursor.getCount(); i++) {
                    try {
                        infos[i] = getDeviceInfo(cursor.getInt(cursor.getColumnIndex(ID)));
                    } catch (DeviceNotFoundException e) {
                        e.printStackTrace();
                        numNull++;
                    }
                    cursor.moveToNext();
                }
                if(numNull > 0) {
                    DeviceInfo[] finalInfos = new DeviceInfo[cursor.getCount() - numNull];
                    finalInfos = repopulateArray(infos, finalInfos);
                    return finalInfos;
                } else {
                    return infos;
                }
            } else {
                throw new DeviceNotFoundException("No devices found in table.");
            }
        } else {
            throw NullCursorException.makeException(TABLE_NAME, columnArray);
        }
    }

    public void addDevice(DeviceInfo info) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        database.insert(TABLE_NAME, null, getContentValues(info));
    }

    public void updateDevice(DeviceInfo info) throws NullMacAddressException, DeviceNotFoundException, NullCursorException {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        int id = getId(info);

        database.update(TABLE_NAME, getContentValues(info), ID + " = ?", new String[] { String.valueOf(id) } );
    }

    public Integer deleteDevice (DeviceInfo info) throws NullMacAddressException, DeviceNotFoundException, NullCursorException {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        int id = getId(info);

        return database.delete(TABLE_NAME, ID + " = ?", new String[] { String.valueOf(id) });
    }

    public void clearDatabase() {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        databaseHelper.onCreate(database);
    }

    public int getId(DeviceInfo info) throws NullMacAddressException, DeviceNotFoundException, NullCursorException {
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        String macAddress = info.getDeviceMac();
        if(macAddress == null) {
            throw NullMacAddressException.makeException();
        }
        Cursor cursor = database.query(TABLE_NAME, new String[]{ ID }, MAC_ADDRESS + " = ?", new String[]{ macAddress }, null, null, null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndex(ID));
            } else {
                throw DeviceNotFoundException.makeException(TABLE_NAME, MAC_ADDRESS, info.getDeviceMac());
            }
        } else {
            throw NullCursorException.makeException(TABLE_NAME, new String[]{ ID }, new String[]{ MAC_ADDRESS }, new String[]{ macAddress });
        }
    }

    public int getCount() throws DeviceNotFoundException, NullCursorException {
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, null);

        if(cursor != null) {
            if(cursor.moveToFirst()) {
                return cursor.getCount();
            } else {
                throw new DeviceNotFoundException("No devices found in table.");
            }
        } else {
            throw NullCursorException.makeException(TABLE_NAME, columnArray);
        }
    }

    private ContentValues getContentValues(DeviceInfo info) {
        String mac = info.getDeviceMac();
        String name = info.getDeviceName();
        int type = info.getDeviceType();
        boolean connect = info.getOptionConnect();
        boolean sms = info.getOptionSMS();
        boolean notifications = info.getOptionNotification();

        ContentValues contentValues = new ContentValues();
        contentValues.put(MAC_ADDRESS, mac);
        contentValues.put(NAME, name);
        contentValues.put(TYPE, type);
        contentValues.put(CONNECT, connect ? 1 : 0);
        contentValues.put(SMS, sms ? 1 : 0);
        contentValues.put(NOTIFICATION, notifications ? 1 : 0);

        return contentValues;
    }

    private < T > T[] repopulateArray(T[] oldArray, T[] newArray) {
        int newCount = 0;
        for(int i = 0; i < (oldArray.length < newArray.length ? oldArray.length : newArray.length); i++) {
            if(oldArray[i] != null) {
                newArray[newCount] = oldArray[i];
                newCount++;
            }
        }
        return newArray;
    }

}
