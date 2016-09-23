/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.ui.dialogs.bluetoothscan;

import android.animation.Animator;
import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.notiflyapp.services.bluetooth.scan.BluetoothScanService;

/**
 * Created by Brennan on 5/28/2016.
 */
public class ConnectBluetoothDeviceFoundCallback implements BluetoothScanService.DeviceFoundCallback {

    private ConnectBluetoothAdapter connectBluetoothAdapter;
    private ProgressBar progressBar;
    private TextView scanComplete;

    public ConnectBluetoothDeviceFoundCallback(ConnectBluetoothAdapter a, ProgressBar b, TextView c) {
        connectBluetoothAdapter = a;
        progressBar = b;
        scanComplete = c;
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
        if(progressBar != null ) {
            progressBar.animate().alpha(0).setDuration(1000).scaleX(0).scaleY(0).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    scanComplete.setVisibility(View.VISIBLE);
                    scanComplete.animate().alpha(255).setDuration(500);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
    }

}
