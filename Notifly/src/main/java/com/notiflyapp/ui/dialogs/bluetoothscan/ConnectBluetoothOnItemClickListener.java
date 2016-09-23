/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.ui.dialogs.bluetoothscan;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.notiflyapp.R;
import com.notiflyapp.database.DatabaseFactory;
import com.notiflyapp.database.DeviceDatabase;
import com.notiflyapp.database.DeviceNotFoundException;
import com.notiflyapp.database.NullCursorException;
import com.notiflyapp.database.NullMacAddressException;
import com.notiflyapp.ui.activities.devices.DeviceActivity;
import com.notiflyapp.services.bluetooth.connection.BluetoothService;
import com.notiflyapp.data.DeviceInfo;

import java.util.ArrayList;

/**
 * Created by Brennan on 5/8/2016.
 */
public class ConnectBluetoothOnItemClickListener implements AdapterView.OnItemClickListener {

    private static final String TAG = ConnectBluetoothOnItemClickListener.class.getSimpleName();

    private ConnectBluetoothDialogFragment holder;

    public ConnectBluetoothOnItemClickListener(ConnectBluetoothDialogFragment holder) {
        this.holder = holder;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        ArrayList<BluetoothDevice> devices = holder.getDeviceList();
        BluetoothDevice device = devices.get(position);

        holder.dismiss();

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceName(device.getName())
                .setDeviceMac(device.getAddress())
                .setDeviceType(device.getBluetoothClass().getMajorDeviceClass())
                .setOptionConnect(true)
                .setOptionSMS(true)
                .setOptionNotification(true);

        int index = -1;
        DeviceDatabase database = DatabaseFactory.getDeviceDatabase(holder.getActivity());
        try {
            if (!database.hasDevice(deviceInfo)) {
                database.addDevice(deviceInfo);
            }
            index = database.getId(deviceInfo);
        } catch (NullMacAddressException | DeviceNotFoundException | NullCursorException e) {
            e.printStackTrace();
            Log.w(TAG, "Failed to add device.");
            Toast.makeText(holder.getActivity(), R.string.dialog_device_add_failed, Toast.LENGTH_LONG).show();
        }

        Handler mainHandler = ((DeviceActivity) holder.getActivity()).mainHandler;
        Message msg2 = mainHandler.obtainMessage();
        msg2.what = DeviceActivity.ADD_DEVICE;
        msg2.arg1 = index;
        msg2.sendToTarget();

        Intent intent = new Intent(holder.getActivity(), BluetoothService.class);
        intent.putExtra(BluetoothService.DEVICE_INDEX, index);
        intent.setAction(BluetoothService.CONNECT_TO_DEVICE);
        holder.getActivity().startService(intent);
    }

}
