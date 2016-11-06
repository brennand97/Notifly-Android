/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.notiflyapp.data.DataString;
import com.notiflyapp.data.requestframework.Request;
import com.notiflyapp.data.requestframework.RequestHandler;
import com.notiflyapp.data.requestframework.Response;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

/**
 * Created by Brennan on 9/21/2016.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class PreviousSMSTest {

    @Test()
    public void contactThread_correct() throws Exception {
        int threadId = 1;

        Request r = new Request();
        r.putBody(RequestHandler.RequestCode.RETRIEVE_PREVIOUS_SMS);
        r.putItem(RequestHandler.RequestCode.EXTRA_THREAD_ID, new DataString("42"));
        r.putItem(RequestHandler.RequestCode.EXTRA_RETRIEVE_PREVIOUS_SMS_START_TIME, new DataString(String.valueOf(System.currentTimeMillis())));
        r.putItem(RequestHandler.RequestCode.EXTRA_RETRIEVE_PREVIOUS_SMS_MESSAGE_COUNT, new DataString("20"));

        Context context = InstrumentationRegistry.getTargetContext();
        RequestHandler handler = RequestHandler.getInstance(context);
        assertNotNull(handler);

        handler.sendRequest(null, r, new RequestHandler.ResponseCallback() {
            @Override
            public void responseReceived(Request request, Response response) {



            }
        });

        Thread.sleep(200);
    }

}
