/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.services.bluetooth.scan;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

/**
 * Created by Brennan on 5/28/2016.
 */
public class BluetoothScanService extends Service {

    public final IBinder mBinder = new LocalBinder();
    private DeviceFoundCallback mDeviceFoundCallback;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> availableDevices = new ArrayList<>();

    @Override
    public void onCreate() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public BluetoothScanService getService() {
            // Return this instance of LocalService so clients can call public methods
            return BluetoothScanService.this;
        }
    }

    public BluetoothDevice[] getPairedDevices() {
        if(mBluetoothAdapter != null) {
            if(mBluetoothAdapter.isEnabled()) {
                Set<BluetoothDevice> boundedDevices = mBluetoothAdapter.getBondedDevices();
                BluetoothDevice[] pairedDevices = new BluetoothDevice[boundedDevices.size()];
                boundedDevices.toArray(pairedDevices);
                return pairedDevices;
            }
        }
        return null;
    }

    public void startScan(DeviceFoundCallback dfc) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
        if(mBluetoothAdapter.isEnabled()) {
            mDeviceFoundCallback = dfc;
            mBluetoothAdapter.startDiscovery();
        }
    }

    public void stopScan() {
        if(mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        unregisterReceiver(mReceiver);
    }

    public interface DeviceFoundCallback {
        void deviceFound(BluetoothDevice device);
        void nameFound(BluetoothDevice device);
        void scanComplete();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if(mBluetoothAdapter.getBondedDevices().contains(device)) { return; }
            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    // Get the BluetoothDevice object from the Intent
                    if(device != null) {
                        if(!availableDevices.contains(device)) {
                            availableDevices.add(device);
                            if(mDeviceFoundCallback != null) {
                                mDeviceFoundCallback.deviceFound(device);
                            }
                            Log.i("Debug", device.getAddress());
                        }
                    }
                    break;
                case BluetoothDevice.ACTION_NAME_CHANGED:
                    // Get the BluetoothDevice object from the Intent
                    if(device != null) {
                        int position = -1;
                        for(BluetoothDevice d: availableDevices) {
                            if(Objects.equals(device.getAddress(), d.getAddress())) {
                                position = availableDevices.indexOf(d);
                                availableDevices.add(position, device);
                                break;
                            }
                        }
                        if(position == -1) {
                            availableDevices.add(device);
                        }
                        if(mDeviceFoundCallback != null) {
                            mDeviceFoundCallback.nameFound(device);
                        }
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.i("Debug", "Scan complete");
                    if(mDeviceFoundCallback != null) {
                        mDeviceFoundCallback.scanComplete();
                        mDeviceFoundCallback = null;
                    }
                    break;

            }
        }
    };
}
