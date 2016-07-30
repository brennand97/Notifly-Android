package com.notiflyapp.data.requestframework;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.notiflyapp.data.DataObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Brennan on 6/28/2016.
 */
public class RequestDeserializer implements JsonDeserializer {
    @Override
    public Request deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        final JsonObject jsonObject = jsonElement.getAsJsonObject();

        String body = null;
        try {
            body = jsonObject.get("body").getAsString();
        } catch (NullPointerException e) {
            //DO nothing
        }
        UUID extra = null;
        try {
            extra = UUID.fromString(jsonObject.get("extra").getAsString());
        } catch (NullPointerException e) {
            //DO nothing
        }
        String requestValue = null;
        try {
            requestValue = jsonObject.get("requestValue").getAsString();
        } catch (NullPointerException e) {
            //DO nothing
        }
        HashMap<String, DataObject> hashMap = null;
        try {
            hashMap = jsonDeserializationContext.deserialize(jsonObject.get("hashMap"), new TypeToken<HashMap<String, DataObject>>(){}.getType());
        } catch (NullPointerException e) {
            //DO nothing
        }

        final Request request = new Request();
        request.putBody(body);
        request.putExtra(extra);
        request.putRequestValue(requestValue);
        request.putHashMap(hashMap);

        return request;
    }
}
