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
public class ResponseDeserializer implements JsonDeserializer {
    @Override
    public Response deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        final JsonObject jsonObject = jsonElement.getAsJsonObject();

        final String body = jsonObject.get("body").getAsString();
        final UUID extra = UUID.fromString(jsonObject.get("extra").getAsString());
        final String requestValue = jsonObject.get("requestValue").getAsString();

        final HashMap<String, DataObject> hashMap = jsonDeserializationContext.deserialize(jsonObject.get("hashMap"), new TypeToken<HashMap<String, DataObject>>(){}.getType());

        final Response response = new Response();
        response.putBody(body);
        response.putExtra(extra);
        response.putRequestValue(requestValue);
        response.putHashMap(hashMap);

        return response;
    }
}
