package com.notiflyapp.services.bluetooth.connection;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.notiflyapp.data.DataObject;
import com.notiflyapp.data.DeviceInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Brennan on 4/17/2016.
 *
 * Holds connection loop for the bluetooth client and the information gathered on the client
 */
public class BluetoothClient {

    private ClientLoop loop;    //Reference to ClientThread object that holds device's connection socket + in and out streams

    private ArrayList<DataObject> received = new ArrayList<>(); //ArrayList of all DataObjects received from the device
    private ArrayList<DataObject<String, java.io.File>> sent = new ArrayList<DataObject<String, java.io.File>>();     //ArrayList of all DataObjects sent to the device

    private String deviceName;  //Device's name obtained from received DataInfo DataObject on connect
    private String deviceMac;   //Device's Mac Address obtained from received DataInfo DataObject on connect
    private int deviceType;  //Device's Type obtained from received DataInfo DataObject on connect (Ex. Phone, Tablet, Laptop)

    public static class Type {
        public static final int MISC              = 0x0000;
        public static final int COMPUTER          = 0x0100;
        public static final int PHONE             = 0x0200;
        public static final int NETWORKING        = 0x0300;
        public static final int AUDIO_VIDEO       = 0x0400;
        public static final int PERIPHERAL        = 0x0500;
        public static final int IMAGING           = 0x0600;
        public static final int WEARABLE          = 0x0700;
        public static final int TOY               = 0x0800;
        public static final int HEALTH            = 0x0900;
        public static final int UNCATEGORIZED     = 0x1F00;
    }

    private Handler mHandler = BluetoothService.mServiceHandler;

    /**
     *
     * @param macAddress    BluetoothDevice Mac Address that is being connected
     * @param conn      The uninitialized connection received by the service
     */
    public BluetoothClient(String macAddress, BluetoothSocket conn) {
        loop = new ClientLoop(this, conn);
        deviceMac = macAddress;
    }

    protected void startLoop() {
        loop.mainLoop();
    }

    public boolean isConnected() {
        return loop.isConnected();
    }

    /**
     * Will disconnect client device from service and remove itself from connected devices list
     */
    void close() {
        loop.close();     //Called receiving threads close() method to disconnect it from device
        serverOut("Client disconnected.");
    }


    /**
     *Will take a DeviceInfo DataObject and extract the device's information
     * from it.
     *
     * @param di DeviceInfo DataObject containing the device's Name, Mac Address, Type
     */
    protected void setDeviceData(DeviceInfo di) {
        deviceName = di.getDeviceName();    //Retrieves the device name provided by the device
        deviceMac = di.getDeviceMac();      //Retrieves the device Mac Address provided by the device
        deviceType = di.getDeviceType();    //Retrieves the device Type provided by the device
    }


    /**
     * Handles the DataObjects received from the client device and
     * will pass the extracted data, or whole DataObject, to the
     * appropriate functions.
     *
     * @param object DataObject received from client
     */
    protected void receivedMsg(DataObject object) {
        Message msg = mHandler.obtainMessage();
        msg.what = BluetoothService.INCOMING_MESSAGE;
        msg.obj = object;
        msg.sendToTarget();
        received.add(object);
    }


    /**
     *
     * @return Array of all DataObjects received from the client device
     */
    public ArrayList<DataObject> getReceived() {
        return received;
    }


    /**
     * Sends a DataObject to client device and stores the sent object
     * in the sent array.
     *
     * @param msg DataObject to be sent to client device
     */
    public void sendMsg(DataObject msg) {
        loop.send(msg);
        sent.add(msg);
    }


    /**
     *
     * @return Array of all DataObjects sent to the client device
     */
    public ArrayList<DataObject<String, java.io.File>> getSent() {
        return sent;
    }


    /**
     * Prints a string to the service log with identifying TAG of "BluetoothClient: 'deviceName'"
     * and time at which it was sent.
     *
     * @param out String to print to log
     */
    protected void serverOut(String out) {
        Log.i("Debug", out);
    }


    /**
     * @return Connected bluetooth device's name
     */
    public String getDeviceName() {
        return deviceName;
    }


    /**
     * @param deviceName Set the connected bluetooth device's name
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }


    /**
     * @return Connected bluetooth device's Mac Address
     */
    public String getDeviceMac() {
        return deviceMac;
    }


    /**
     * @param deviceMac Set the connected bluetooth device's name
     */
    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }


    /**
     * @return Connected bluetooth device's Type
     */
    public int getDeviceType() {
        return deviceType;
    }


    /**
     * @param deviceType Set the connected bluetooth device's Type
     */
    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

}

