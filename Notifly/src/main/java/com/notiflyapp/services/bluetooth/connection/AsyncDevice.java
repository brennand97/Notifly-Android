/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.services.bluetooth.connection;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

/**
 * Created by Brennan on 5/7/2016.
 */
public class AsyncDevice extends AsyncTask<BluetoothClient, BluetoothClient, BluetoothClient> {

    @Override
    protected BluetoothClient doInBackground(BluetoothClient... params) {

        params[0].startLoop();

        return params[0];

    }

    @Override
    protected void onPostExecute(BluetoothClient s) {
        super.onPostExecute(s);

        /*
        Message msg = ((Handler) BluetoothService.mServiceHandler).obtainMessage();
        msg.what = BluetoothService.CLIENT_DISCONNECTED;
        msg.obj = s;
        msg.sendToTarget();
        */
    }
}
