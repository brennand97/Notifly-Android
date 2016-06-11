package com.notiflyapp.database;

import android.content.Context;

/**
 * Created by Brennan on 6/1/2016.
 */
public class DatabaseFactory {

    //Current and past database versions
    protected static final class Version {
        protected static final int DATABASE_VERSION = 1;
    }

    //Database name
    private static final String DATABASE_NAME = "notifly_data.db";

    //All databases
    private final DeviceDatabase device;

    private DatabaseHelper databaseHelper;

    private static DatabaseFactory instance;

    //Constructor
    private DatabaseFactory(Context context) {
        this.databaseHelper = new DatabaseHelper(context, DATABASE_NAME, null, Version.DATABASE_VERSION);
        this.device = new DeviceDatabase(context, databaseHelper);
    }

    public synchronized static DatabaseFactory getInstance(Context context) {
        if (instance == null)
            instance = new DatabaseFactory(context.getApplicationContext());
        return instance;
    }

    public static DeviceDatabase getDeviceDatabase(Context context) {
        return getInstance(context).device;
    }

}
