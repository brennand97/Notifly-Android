package com.notiflyapp.services.bluetooth.connection;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;
import com.notiflyapp.data.DataObject;
import com.notiflyapp.data.DataObjectDeserializer;
import com.notiflyapp.data.requestframework.Request;
import com.notiflyapp.data.requestframework.RequestDeserializer;
import com.notiflyapp.data.requestframework.Response;
import com.notiflyapp.data.requestframework.ResponseDeserializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Brennan on 4/25/2016.
 */
public class ClientLoop {

    private static final String TAG = ClientLoop.class.getSimpleName();

    private BluetoothSocket mmSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private BluetoothClient client;
    private MessageHandler handler = new MessageHandler();

    private int BUFFER_SIZE = 512;
    private ArrayList<Byte> byteHolder = new ArrayList<>();
    private boolean multiBufferObject = false;

    private boolean connected = false;

    public ClientLoop(BluetoothClient client, BluetoothSocket socket) {
        this.client = client;
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;

        handler.start();
    }

    public void mainLoop() {

        connected = true;

        while (connected) {
            try {
                if(mmSocket.isConnected() && mmInStream != null) {
                    byte[] header = new byte[4];
                    int headerValue;
                    int bytes;
                    bytes = mmInStream.read(header);
                    if(bytes == -1) { // Catch for a client drop/disconnect
                        Log.i("Bluetooth Client", "Disconnected");
                        client.handleDisconnect();
                        break;
                    }
                    headerValue = retrieveHeader(header);
                    byte[] buffer = new byte[headerValue];
                    bytes = mmInStream.read(buffer);
                    //****************************************************************
                    //Crucial for successful packet retrieval
                    while(bytes < headerValue) {
                        byte[] newBuffer = new byte[headerValue - bytes];
                        bytes += mmInStream.read(newBuffer);
                        for(int i = 0; i < newBuffer.length; i++) {
                            buffer[headerValue - newBuffer.length + i] = newBuffer[i];
                        }
                    }
                    //***************************************************************
                    Log.v(TAG, "bytes in : " + String.valueOf(bytes));
                    if(bytes == -1) { // Catch for a client drop/disconnect
                        Log.i("Bluetooth Client", "Disconnected");
                        client.handleDisconnect();
                        break;
                    }
                    dataIn(buffer);
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }

    }

    private int retrieveHeader(byte[] header) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.put(header);
        buffer.flip();
        return buffer.getInt();
    }

    public void dataIn(byte[] data) throws MalformedJsonException, JsonSyntaxException {
        String str = new String(data);
        //Log.v(TAG, str);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Response.class, new ResponseDeserializer());
        gsonBuilder.registerTypeAdapter(Request.class, new RequestDeserializer());
        gsonBuilder.registerTypeAdapter(DataObject.class, new DataObjectDeserializer());
        gsonBuilder.serializeNulls();
        Gson gson = gsonBuilder.create();
        DataObject obj = gson.fromJson(str, DataObject.class);
        client.receivedMsg(obj);
    }

    public void send(DataObject dataObject) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeNulls();
        Gson gson = gsonBuilder.create();
        String object = gson.toJson(dataObject);
        if(object != null) {
            Log.v(TAG, object);
            handler.add(object);
        }
    }

    public void close() {
        connected = false;
        try {
            if(mmSocket.isConnected()) {
                mmInStream.close();
                mmOutStream.close();
                mmSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return mmSocket.isConnected();
    }

    protected class MessageHandler extends Thread {

        private final String TAG = MessageHandler.class.getSimpleName();

        private ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<String>(1024);
        private boolean running = false;

        final Object lock = new Object();

        public MessageHandler() {
        }

        public void add(String object) {
            if(object != null) {
                queue.add(object);
                if(running) {
                    try {
                        synchronized (lock) {
                            lock.notify();
                        }
                    } catch (IllegalMonitorStateException e) {
                        e.printStackTrace();
                        Log.v(TAG, "Most likely first addition call to new bluetooth device.");
                    }
                }
            }
        }

        private byte[] createHeader(int size) {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putInt(size);
            return buffer.array();
        }

        public void run() {
            Log.v(TAG, "Bluetooth MessageHandler started");
            running = true;
            synchronized (lock) {
                while (running) {
                    if(queue.isEmpty()) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        String object = queue.poll();
                        if(mmSocket != null) {
                            if(mmSocket.isConnected()) {
                                try {
                                    mmOutStream.write(createHeader(object.getBytes().length));
                                    mmOutStream.write(object.getBytes());
                                    Log.v(ClientLoop.TAG, "bytes out : " + String.valueOf(object.getBytes().length));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }

    }

}
