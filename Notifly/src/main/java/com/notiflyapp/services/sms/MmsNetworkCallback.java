/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.services.sms;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Brennan on 5/29/2016.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MmsNetworkCallback extends ConnectivityManager.NetworkCallback {

    ConnectivityManager connectivityManager;
    String url;
    private Context context;

    public MmsNetworkCallback(ConnectivityManager connectivityManager, String url, Context context) {
        this.connectivityManager = connectivityManager;
        this.url = url;
        this.context = context;
    }

    @Override
    public void onAvailable(Network network) {

        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
        Log.i("Debug", "Callback called");

        //TransactionSettings transactionSettings = new TransactionSettings(context, networkInfo.getExtraInfo());
        /*
        try {
            byte[] rawPdu = HttpUtils.httpConnection(context, 0, url, null, HttpUtils.HTTP_GET_METHOD, false, null, -1);
            Log.i("Debug", new String(rawPdu));
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
        ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).unregisterNetworkCallback(this);

        ContentResolver contentResolver = context.getContentResolver();
        final String[] projection = new String[]{"_id", "ct_t"};
        Uri uri = Uri.parse("content://mms/");
        Cursor query = contentResolver.query(uri, projection, null, null, null);

        int id =  -1;
        assert query != null;
        if (query.moveToFirst()) {
            //do {
                String string = query.getString(query.getColumnIndex("ct_t"));
                if ("application/vnd.wap.multipart.related".equals(string)) {
                    id = query.getInt(query.getColumnIndex("_id"));
                    Log.i("Debug-Content_Resolver", "id : " + id);

                    //String selectionPart = "_id=" + id;
                    Uri uri2 = Uri.parse("content://mms/part/" + id);
                    Cursor cursor = context.getContentResolver().query(uri, null,
                            null, null, null);
                    if (cursor.moveToFirst()) {
                        do {

                            for(int i = 0; i < cursor.getColumnCount(); i++) {
                                switch (cursor.getType(i)) {
                                    case Cursor.FIELD_TYPE_INTEGER:
                                        Log.i("Debug-Content_Resolver", cursor.getColumnName(i) + " : " + cursor.getInt(i));
                                        break;
                                    case Cursor.FIELD_TYPE_STRING:
                                        Log.i("Debug-Content_Resolver", cursor.getColumnName(i) + " : " + cursor.getString(i));
                                        break;
                                }

                            }
                            /*
                            String partId = cursor.getString(cursor.getColumnIndex("cid"));
                            Log.i("Debug-Content_Resolver", "cid : " + partId);
                            String type = cursor.getString(cursor.getColumnIndex("ct"));
                            if ("text/plain".equals(type)) {
                                String data = cursor.getString(cursor.getColumnIndex("_data"));
                                String body;
                                if (data != null) {
                                    // implementation of this method below
                                    body = getMmsText(partId);
                                } else {
                                    body = cursor.getString(cursor.getColumnIndex("text"));
                                }
                                Log.i("Debug-Content_Resolver", "Body : " + body);
                                String location = query.getString(query.getColumnIndex("ct_l"));
                                Log.i("Debug-Content_Resolver", "Location : " + location);
                            }
                            */
                        } while (cursor.moveToNext());
                    }
                } else {
                    // it's SMS
                }
            //} while (query.moveToNext());
        }

        super.onAvailable(network);
    }

    private String getMmsText(String id) {
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

}
