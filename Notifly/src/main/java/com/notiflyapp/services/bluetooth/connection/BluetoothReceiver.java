package com.notiflyapp.services.bluetooth.connection;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Brennan on 5/24/2016.
 */
public class BluetoothReceiver extends BroadcastReceiver {

    boolean discovering = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null) {
            String action = intent.getAction();
            if(action != null) {
                Intent bluetoothService = new Intent(context, BluetoothService.class);
                switch (action) {
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        int extra = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                        switch (extra) {
                            case BluetoothAdapter.STATE_ON:
                                bluetoothService.setAction(BluetoothService.CONNECT_ALL_DEVICES);
                                context.startService(bluetoothService);
                                Log.i(this.toString(), "Bluetooth turned on, connecting devices");
                                break;
                            case BluetoothAdapter.STATE_TURNING_OFF:
                                context.stopService(bluetoothService);
                                Log.i(this.toString(), "Bluetooth turned off, disconnecting from all devices");
                                break;

                        }
                        break;
                }
            }
        }
    }
}
