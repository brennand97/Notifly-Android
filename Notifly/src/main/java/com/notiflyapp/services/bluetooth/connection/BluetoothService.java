/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.services.bluetooth.connection;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.notiflyapp.data.DataObject;
import com.notiflyapp.data.DeviceInfo;
import com.notiflyapp.data.SMS;
import com.notiflyapp.data.Serial;
import com.notiflyapp.data.requestframework.RequestHandler;
import com.notiflyapp.data.requestframework.Response;
import com.notiflyapp.database.DatabaseFactory;
import com.notiflyapp.database.DeviceDatabase;
import com.notiflyapp.database.DeviceNotFoundException;
import com.notiflyapp.database.NullCursorException;
import com.notiflyapp.database.NullMacAddressException;
import com.notiflyapp.services.sms.SmsService;
import com.notiflyapp.ui.activities.devices.DeviceActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Brennan on 5/6/2016.
 */
public class BluetoothService extends Service {

    private final static String TAG = BluetoothService.class.getSimpleName();

    private final static UUID uuid = UUID.fromString("85023189-23c1-410c-a7c7-29a91f49f764");
    public final static String DEVICE_INDEX = "deviceIndex";
    public final static String MAC_ADDRESS = "macAddress";

    public final static String CONNECT_TO_DEVICE = "com.notiflyapp.services.bluetooth.connection.BluetoothService.connection.CONNECT_TO_DEVICE";
    public final static String CONNECT_ALL_DEVICES = "com.notiflyapp.services.bluetooth.connection.BluetoothService.connection.CONNECT_ALL_DEVICES";
    public static final String DISCONNECT_DEVICE = "com.notiflyapp.services.bluetooth.connection.BluetoothService.connection.DISCONNECT_DEVICE";
    public static final String UPDATE_DEVICE = "com.notiflyapp.services.bluetooth.connection.BluetoothService.connection.UPDATE_DEVICE";

    public final static int CONNECT_DEVICE = 0;
    public final static int INCOMING_MESSAGE = 1;
    public final static int CLIENT_DISCONNECTED = 2;

    private BluetoothClient connectedClient = null;

    public static ServiceHandler mServiceHandler;
    private BluetoothAdapter mBluetoothAdapter;
    public DeviceDatabase deviceDatabase;

    private LocalBroadcastManager broadcaster;
    public static final String DEVICE_CONNECTED = "con.notiflyapp.services.bluetooth.connection.BluetoothService.DEVICE_CONNECTED";
    public static final String DEVICE_DISCONNECTED = "con.notiflyapp.services.bluetooth.connection.BluetoothService.DEVICE_DISCONNECTED";
    public static final String DEVICE_DATABASE_POSITION = "con.notiflyapp.services.bluetooth.connection.BluetoothService.DEVICE_DATABASE_POSITION";

    private static DeviceInfo thisDeviceInfo;

    private final class ServiceHandler extends Handler {

        private final String TAG = ServiceHandler.class.getSimpleName();

        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case CONNECT_DEVICE:
                    final String macAddress = msg.getData().getString(MAC_ADDRESS);
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            DeviceInfo deviceInfo;
                            try {
                                deviceInfo = deviceDatabase.getDeviceInfo(macAddress);
                            } catch (DeviceNotFoundException | NullCursorException e) {
                                e.printStackTrace();
                                return;
                            }

                            if(connectedClient != null) {
                                disconnectClient(connectedClient);
                            }

                            BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(macAddress);
                            bluetoothDevice.fetchUuidsWithSdp();
                            BluetoothSocket bluetoothSocket;
                            try {
                                bluetoothSocket = connectDevice(bluetoothDevice);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.w(TAG, "Device with MAC : " + deviceInfo.getDeviceMac() + " did not connect");
                                return;
                            }
                            if (bluetoothSocket != null) {
                                BluetoothClient bluetoothClient = new BluetoothClient(getApplicationContext(), bluetoothSocket, bluetoothDevice, deviceInfo);
                                connectedClient = bluetoothClient;

                                AsyncDevice backgroundThread = new AsyncDevice();
                                backgroundThread.execute(bluetoothClient);

                                bluetoothClient.sendMsg(thisDeviceInfo);

                                if(bluetoothClient.isConnected()) {
                                    if(deviceInfo.getOptionSMS()) {
                                        Intent pollMessages = new Intent(BluetoothService.this, SmsService.class);
                                        pollMessages.setAction(SmsService.ACTION_RETRIEVE_ALL_UNREAD_MESSAGES);
                                        startService(pollMessages);
                                    }

                                    //Send broadcast informing all listeners that this device has connected
                                    Intent broadcast = new Intent(DEVICE_CONNECTED);
                                    try {
                                        int pos = deviceDatabase.getId(deviceInfo);
                                        broadcast.putExtra(DEVICE_DATABASE_POSITION, pos);
                                    } catch (NullMacAddressException | DeviceNotFoundException | NullCursorException e) {
                                        e.printStackTrace();
                                    }
                                    broadcaster.sendBroadcast(broadcast);

                                    Log.v(TAG, "Device connected.");
                                }
                            }
                        }
                    };
                    (new Thread(runnable)).start();

                    break;
                case INCOMING_MESSAGE:
                    Object obj = msg.obj;
                    if(obj instanceof DataObject)
                    switch (((DataObject) obj).getType()) {
                        case DataObject.Type.DEVICE_INFO:

                            break;
                        case DataObject.Type.MMS:

                            break;
                        case DataObject.Type.SMS:
                            try{
                                Intent smsService = new Intent(BluetoothService.this, SmsService.class);
                                smsService.setAction(SmsService.ACTION_SEND_SMS);
                                smsService.putExtra(SmsService.EXTRA_SMS_MESSAGE, ((SMS) obj).serialize());
                                startService(smsService);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        case DataObject.Type.NOTIFICATION:

                            break;
                        case DataObject.Type.REQUEST:
                            //Look in BluetoothClient because due to nature of object must be handled in the BluetoothLClient class itself
                            break;
                        case DataObject.Type.RESPONSE:
                            RequestHandler.getInstance(getApplicationContext()).handleResponse((Response) obj);
                            break;
                    }

                    break;
                case CLIENT_DISCONNECTED:
                    BluetoothClient client = ((BluetoothClient) msg.obj);
                    disconnectClient(client);
                    break;
            }

        }
    }

    @Override
    public void onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter != null) {

            HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
            thread.start();

            Looper mServiceLooper = thread.getLooper();
            mServiceHandler = new ServiceHandler(mServiceLooper);

            deviceDatabase = DatabaseFactory.getDeviceDatabase(this);

            thisDeviceInfo = new DeviceInfo();
            thisDeviceInfo.setDeviceName(mBluetoothAdapter.getName());
            thisDeviceInfo.setDeviceMac(mBluetoothAdapter.getAddress());
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = null;
        if(intent != null) {
            action = intent.getAction();
        } else {
            connectAllDBDevices();
        }
        if(action != null) {
            switch (action) {
                case SmsService.ACTION_RECEIVE_SMS:
                    if (intent.hasExtra(SmsService.EXTRA_SMS_MESSAGE)) {
                        try {
                            sendSms((SMS) Serial.deserialize(intent.getByteArrayExtra(SmsService.EXTRA_SMS_MESSAGE)));
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case SmsService.ACTION_RECEIVE_MMS:
                    //TODO
                    break;
                case CONNECT_ALL_DEVICES:
                    connectAllDBDevices();
                    Log.i(this.toString(), "Connected all devices");
                    break;
                case DISCONNECT_DEVICE:
                    //int index = intent.getIntExtra(DEVICE_INDEX, -1);
                    //DeviceInfo DDdeviceInfo;
                    if(connectedClient != null) {
                        disconnectClient(connectedClient);
                    }
                    break;
                case CONNECT_TO_DEVICE:
                    if (mBluetoothAdapter != null) {
                        if (mBluetoothAdapter.isEnabled()) {
                            DeviceInfo deviceInfo;
                            if (intent.hasExtra(DEVICE_INDEX)) {

                                int position = intent.getIntExtra(DEVICE_INDEX, 0);
                                try {
                                    deviceInfo = deviceDatabase.getDeviceInfo(position);
                                } catch (DeviceNotFoundException e) {
                                    e.printStackTrace();
                                    break;
                                } catch (NullCursorException e) {
                                    e.printStackTrace();
                                    break;
                                }

                                boolean connect = deviceInfo.getOptionConnect();

                                if(connect) {
                                    if(connectedClient == null || !connectedClient.getDeviceMac().equals(deviceInfo.getDeviceMac())) {
                                        startBluetoothDevice(deviceInfo.getDeviceMac());
                                    }
                                }
                            }
                        } else {
                            Log.i(this.toString(), "Bluetooth not enabled");
                        }
                    } else {
                        Log.i(this.toString(), "Bluetooth not found");
                    }
                    break;
                case UPDATE_DEVICE:
                    if (mBluetoothAdapter != null) {
                        if (mBluetoothAdapter.isEnabled()) {
                            DeviceInfo deviceInfo;
                            if (intent.hasExtra(DEVICE_INDEX)) {

                                int index1 = intent.getIntExtra(DEVICE_INDEX, 0);
                                try {
                                    deviceInfo = deviceDatabase.getDeviceInfo(index1);
                                } catch (DeviceNotFoundException e) {
                                    e.printStackTrace();
                                    break;
                                } catch (NullCursorException e) {
                                    e.printStackTrace();
                                    break;
                                }

                                boolean connect = deviceInfo.getOptionConnect();

                                if(connectedClient != null && connectedClient.getDeviceMac().equals(deviceInfo.getDeviceMac())) {
                                    if(!connect) {
                                        disconnectClient(connectedClient);
                                    }
                                }
                            }
                        } else {
                            Log.i(this.toString(), "Bluetooth not enabled");
                        }
                    } else {
                        Log.i(this.toString(), "Bluetooth not found");
                    }
                    break;
                default:
                    connectAllDBDevices();
                    break;
            }
        }

        return START_STICKY;

    }

    private void sendSms(SMS sms) {

        if(connectedClient != null) {
            try {
                DeviceInfo di = deviceDatabase.getDeviceInfo(connectedClient.getDeviceMac());
                if(di.getOptionConnect()) {
                    connectedClient.sendMsg(sms);
                }
            } catch (DeviceNotFoundException | NullCursorException e) {
                e.printStackTrace();
            }
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mBluetoothAdapter.cancelDiscovery();
        disconnectClient(connectedClient);
    }

    private synchronized BluetoothSocket connectDevice(BluetoothDevice bluetoothDevice) throws IOException {
        BluetoothSocket bluetoothSocket;
        bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
        if(!bluetoothSocket.isConnected()) {
            bluetoothSocket.connect();
        }
        return bluetoothSocket;
    }

    private void startBluetoothDevice(String macAddress) {
        Bundle extras = new Bundle();
        extras.putString(MAC_ADDRESS, macAddress);

        Message msg = mServiceHandler.obtainMessage();
        msg.setData(extras);
        msg.what = CONNECT_DEVICE;

        mServiceHandler.sendMessage(msg);
    }

    private void connectAllDBDevices() {
        //Start all devices that should be connected if in range from the database
        DeviceInfo[] infos;
        try {
            infos = deviceDatabase.getAll();
        } catch (NullCursorException | DeviceNotFoundException e) {
            e.printStackTrace();
            return;
        }
        for(DeviceInfo info: infos) {
            if(info.getOptionConnect()) {
                startBluetoothDevice(info.getDeviceMac());
            }
        }
    }

    public void disconnectClient(BluetoothClient client) {
        if(client != null) {
            client.close();
            connectedClient = null;

            //Inform all listeners that this client has disconnected
            Intent broadcast = new Intent(DEVICE_DISCONNECTED);
            try {
                int pos = deviceDatabase.getId(deviceDatabase.getDeviceInfo(client.getDeviceMac()));
                broadcast.putExtra(DEVICE_DATABASE_POSITION, pos);
            } catch (NullMacAddressException | DeviceNotFoundException | NullCursorException e) {
                e.printStackTrace();
            }
            broadcaster.sendBroadcast(broadcast);
        }
    }

}
