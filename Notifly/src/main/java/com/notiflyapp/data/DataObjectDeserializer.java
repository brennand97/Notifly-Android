package com.notiflyapp.data;

import com.google.gson.*;
import com.notiflyapp.data.requestframework.Request;
import com.notiflyapp.data.requestframework.Response;

import java.lang.reflect.Type;

/**
 * Created by Brennan on 6/28/2016.
 */
public class DataObjectDeserializer implements JsonDeserializer {

    @Override
    public DataObject deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        final JsonObject jsonObject = jsonElement.getAsJsonObject();
        final String dataType = jsonObject.get("type").getAsString();
        switch (dataType) {
            case DataObject.Type.SMS:
                return jsonDeserializationContext.deserialize(jsonElement, SMS.class);
            case DataObject.Type.MMS:
                return jsonDeserializationContext.deserialize(jsonElement, MMS.class);
            case DataObject.Type.NOTIFICATION:
                return jsonDeserializationContext.deserialize(jsonElement, Notification.class);
            case DataObject.Type.DEVICE_INFO:
                return jsonDeserializationContext.deserialize(jsonElement, DeviceInfo.class);
            case DataObject.Type.REQUEST:
                return jsonDeserializationContext.deserialize(jsonElement, Request.class);
            case DataObject.Type.RESPONSE:
                return jsonDeserializationContext.deserialize(jsonElement, Response.class);
            case DataObject.Type.CONTACT:
                return jsonDeserializationContext.deserialize(jsonElement, Contact.class);
            case DataObject.Type.CONVERSATION_THREAD:
                return jsonDeserializationContext.deserialize(jsonElement, ConversationThread.class);
        }

        return null;
    }

}
