package com.notiflyapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Brennan on 6/1/2016.
 */
public abstract class Database {

    SQLiteOpenHelper databaseHelper;
    final Context context;

    public Database(Context context, SQLiteOpenHelper sqLiteOpenHelper) {
        this.context = context;
        this.databaseHelper = sqLiteOpenHelper;
    }

}
