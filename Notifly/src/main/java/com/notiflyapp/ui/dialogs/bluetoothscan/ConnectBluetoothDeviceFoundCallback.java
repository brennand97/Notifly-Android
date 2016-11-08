/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.ui.dialogs.bluetoothscan;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;

import com.notiflyapp.R;
import com.notiflyapp.services.bluetooth.scan.BluetoothScanService;

/**
 * Created by Brennan on 5/28/2016.
 */
public class ConnectBluetoothDeviceFoundCallback implements BluetoothScanService.DeviceFoundCallback {

    private ConnectBluetoothAdapter connectBluetoothAdapter;
    private AlertDialog dialog;

    public ConnectBluetoothDeviceFoundCallback(AlertDialog dialog, ConnectBluetoothAdapter a) {
        this.dialog = dialog;
        connectBluetoothAdapter = a;
    }

    @Override
    public void deviceFound(BluetoothDevice device) {
        if(!connectBluetoothAdapter.contains(device)) {
            connectBluetoothAdapter.add(device);
        }
        connectBluetoothAdapter.notifyDataSetChanged();
    }

    @Override
    public void nameFound(BluetoothDevice device) {
        int position = connectBluetoothAdapter.indexOfMac(device);
        if(position == -1) {
            connectBluetoothAdapter.add(device);
        } else {
            connectBluetoothAdapter.add(position, device);
        }
        connectBluetoothAdapter.notifyDataSetChanged();
    }

    @Override
    public void scanComplete() {
        dialog.setTitle(R.string.dialog_title_complete);
    }

}
