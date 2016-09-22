package com.notiflyapp;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.notiflyapp.data.Contact;
import com.notiflyapp.data.ConversationThread;
import com.notiflyapp.data.requestframework.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 * Created by Brennan on 9/21/2016.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ContactByThreadIdTest {

    @Test()
    public void contactThread_correct() throws Exception {
        int threadId = 1;

        Request r = new Request();
        r.putBody(RequestHandler.RequestCode.CONTACT_BY_THREAD_ID);

        Context context = InstrumentationRegistry.getTargetContext();
        RequestHandler handler = RequestHandler.getInstance(context);
        assertNotNull(handler);

        r.putRequestValue(String.valueOf(threadId));
        handler.sendRequest(null, r, new RequestHandler.ResponseCallback() {
            @Override
            public void responseReceived(Request request, Response response) {
                ConversationThread thread = (ConversationThread) response.getItem(RequestHandler.RequestCode.EXTRA_CONTACT_BY_THREAD_ID_THREAD);
                assertNotNull(thread);

                Contact[] contacts = thread.getContacts();
                assertNotNull(contacts);

            }
        });

        Log.v(ContactByThreadIdTest.class.getSimpleName(), r.getRequestValue());
        Thread.sleep(200);
    }

}
