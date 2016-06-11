package com.notiflyapp.services.bluetooth.connection;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.notiflyapp.data.DataObject;
import com.notiflyapp.data.Serial;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Brennan on 4/25/2016.
 */
public class ClientLoop {

    private BluetoothSocket mmSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private BluetoothClient client;
    private MessageHandler handler = new MessageHandler();

    private int BUFFER_SIZE = 1024;
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

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytes = 0;

        while (connected) {
            try {
                if(mmSocket.isConnected() && mmInStream != null) {
                    bytes = mmInStream.read(buffer);
                }

                if(bytes == -1) { // Catch for a client drop/disconnect
                    Log.i("Bluetooth Client", "Disconnected");
                    break;
                }

                handleData(buffer);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }

        /*  For Testing Purposes
        for(int i = 0; i < 100; i++) {

            try {
                Thread.sleep(1000);
                send(new SMS("Testing","1, 2, 3"));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        */

    }

    private void handleData(byte[] buffer) {
        if (!multiBufferObject) {
            try {
                dataIn(buffer);
            } catch (EOFException e1) {
                for (Byte b : buffer) {
                    byteHolder.add(b);
                }
                multiBufferObject = true;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            for (Byte b : buffer) {
                byteHolder.add(b);
            }
            byte[] bBuffer = new byte[byteHolder.size()];
            for (int i = 0; i < byteHolder.size(); i++) {
                bBuffer[i] = byteHolder.get(i);
            }
            try {
                dataIn(bBuffer);
                byteHolder.clear();
                multiBufferObject = false;
            } catch (EOFException e2) {
                //Still isn't a complete object
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void dataIn(byte[] data) throws IOException, ClassNotFoundException {
        Object object = Serial.deserialize(data);
        if(object instanceof DataObject) {
            client.receivedMsg((DataObject) object);
        }
    }

    public void send(DataObject object) {
        if(object != null) {
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

        private ArrayBlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(1024);
        private boolean running = false;

        private final Object lock = new Object();

        public MessageHandler() {
        }

        public void add(DataObject object) {
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
                        Object object = queue.poll();
                        if(mmSocket != null) {
                            if(mmSocket.isConnected()) {
                                try {
                                    mmOutStream.write(Serial.serialize(object));
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
