package com.notiflyapp.data.requestframework;

import android.content.Context;
import android.util.Log;

import com.notiflyapp.tasks.ReceiveContactByThreadId;
import com.notiflyapp.services.bluetooth.connection.BluetoothClient;

import java.util.HashMap;

/**
 * Created by Brennan on 6/23/2016.
 */
public class RequestHandler {

    private final static String TAG = RequestHandler.class.getSimpleName();

    public final static class RequestCode {

        public final static String CONTACT_BY_THREAD_ID = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.CONTACT_BY_THREAD_ID";
            /**
             * @type String[]
             * This is a String array that will contain the contact ids of all contacts associated with given thread id
             */
            public final static String EXTRA_CONTACT_BY_THREAD_ID_CONTACT_ID = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.EXTRA_CONTACT_BY_THREAD_ID_CONTACT_ID";
            /**
             * @type String[]
             * This is a String array that will contain the names of all contacts associated with given thread id
             */
            public final static String EXTRA_CONTACT_BY_THREAD_ID_NAME = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.EXTRA_CONTACT_BY_THREAD_ID_NAME";
            /**
             * @type String[]
             * This is a String array that will contain the phone numbers of all contacts associated with given thread id
             */
            public final static String EXTRA_CONTACT_BY_THREAD_ID_PHONE_NUMBER = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.EXTRA_CONTACT_BY_THREAD_ID_PHONE_NUMBER";
            /**
             * @type Byte[][]
             * This is a 2-dimensional byte array that will contain the the images (int byte[] form) of all contacts associated with given thread id
             */
            public final static String EXTRA_CONTACT_BY_THREAD_ID_IMAGE = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.EXTRA_CONTACT_BY_THREAD_ID_IMAGE";

    }

    private HashMap<String, Request> requestHashMap = new HashMap<>();              //String is the UUID of the request in string form and the Request object is the request itself
    private HashMap<String, BluetoothClient> clientHashMap = new HashMap<>();       //String is the UUID of the request in string form and the BluetoothClient is the client related to the request
    private HashMap<String, ResponseCallback> callbackHashMap = new HashMap<>();    //String is the UUID of the request in string form and the ResponseCallback is the callback assigned with the request

    private static RequestHandler handler;
    private Context context;

    private RequestHandler(Context context) {
        this.context = context;
    }

    public interface ResponseCallback {
        void responseReceived(Request request, Response response);
    }

    public static RequestHandler getInstance(Context context) {
        if(handler == null) {
            handler = new RequestHandler(context);
        }
        return handler;
    }

    public void handleRequest(BluetoothClient client, Request request) {
        Log.v(TAG, "Received request : " + request.getExtra().toString() + " from : " + client.getDeviceMac() + " for : " + request.getBody());
        clientHashMap.put(request.getExtra().toString(), client);
        Response response = Response.makeResponse(request);
        switch (request.getBody()) {
            case RequestCode.CONTACT_BY_THREAD_ID:
                ReceiveContactByThreadId task = new ReceiveContactByThreadId(context, response);
                task.start();
            break;
            default:
                //TODO handle if the given key does not match any of the defined request codes
                break;
        }
    }

    public void handleResponse(Response response) {
        if(callbackHashMap.containsKey(response.getExtra().toString()) && requestHashMap.containsKey(response.getExtra().toString())) {
            //TODO handle the incoming of a requested response
            String uuid = response.getExtra().toString();
            callbackHashMap.get(uuid).responseReceived(requestHashMap.get(uuid), response);
            callbackHashMap.remove(response.getExtra().toString());
            requestHashMap.remove(response.getExtra().toString());
        } else {
            //The response doesn't have a matching request so for now just drop
        }
    }

    //TODO if the server framework is ever completely generalized on all OSes then change this function the the generalized client object
    public void sendRequest(BluetoothClient client, Request request, ResponseCallback callback) {
        requestHashMap.put(request.getExtra().toString(), request);
        callbackHashMap.put(request.getExtra().toString(), callback);
        client.sendMsg(request);
    }

    public void sendResponse(Response response) {
        String uuid = response.getExtra().toString();
        if(clientHashMap.containsKey(uuid)) {
            BluetoothClient client = clientHashMap.get(uuid);
            client.sendMsg(response);
            Log.v(TAG, "Response : " + response.getExtra().toString() + " sent");
            clientHashMap.remove(uuid);
        }
    }

}
