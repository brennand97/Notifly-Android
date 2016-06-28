package com.notiflyapp.ui.activities.devices.buttons;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.notiflyapp.data.DeviceInfo;
import com.notiflyapp.database.DatabaseFactory;
import com.notiflyapp.database.DeviceDatabase;
import com.notiflyapp.database.DeviceNotFoundException;
import com.notiflyapp.database.NullCursorException;
import com.notiflyapp.database.NullMacAddressException;
import com.notiflyapp.services.bluetooth.connection.BluetoothService;
import com.notiflyapp.ui.activities.devices.DeviceActivity;

/**
 * Created by Brennan on 5/16/2016.
 */
public abstract class OptionButton implements View.OnClickListener {

    private static final String TAG = OptionButton.class.getSimpleName();

    protected Activity activity;
    protected DeviceInfo deviceInfo, deviceInfoOld;
    private DeviceDatabase deviceDatabase;

    public OptionButton(Activity activity, DeviceInfo di) {
        this.activity = activity;
        deviceInfo = DeviceInfo.replicate(di);
        deviceInfoOld = di;
        deviceDatabase = DatabaseFactory.getDeviceDatabase(activity);
    }

    protected void updateDevice() {
        int index = -1;
        try {
            deviceDatabase.updateDevice(deviceInfo);
            index = deviceDatabase.getId(deviceInfo);
        } catch (NullMacAddressException e) {
            e.printStackTrace();
            return;
        } catch (DeviceNotFoundException e) {
            e.printStackTrace();
            Log.w(TAG, "Device, mac address: " + deviceInfo.getDeviceMac() + ", not found in device database.");
            return;
        } catch (NullCursorException e) {
            e.printStackTrace();
            return;
        }


        Handler mainHandler = ((DeviceActivity) activity).mainHandler;
        Message msg = mainHandler.obtainMessage();
        msg.what = DeviceActivity.ADD_DEVICE;
        msg.arg1 = index;
        msg.sendToTarget();

        Intent intent = new Intent(activity, BluetoothService.class);
        if(deviceInfo.getOptionConnect() != deviceInfoOld.getOptionConnect()) {
            if(deviceInfo.getOptionConnect() == true) {
                intent.setAction(BluetoothService.CONNECT_TO_DEVICE);
                intent.putExtra(BluetoothService.DEVICE_INDEX, index);
                activity.startService(intent);
            } else {
                intent.setAction(BluetoothService.DISCONNECT_DEVICE);
                intent.putExtra(BluetoothService.DEVICE_INDEX, index);
                activity.startService(intent);
            }
        }
        if(deviceInfo.getOptionNotification() != deviceInfoOld.getOptionNotification()) {
            //TODO Maybe update a notification service in the future
        }
        if(deviceInfo.getOptionSMS() != deviceInfoOld.getOptionSMS()) {
            //Not sure if anything needs to be done here...
        }

        deviceInfoOld = deviceInfo;
        deviceInfo = DeviceInfo.replicate(deviceInfoOld);
    }

}
