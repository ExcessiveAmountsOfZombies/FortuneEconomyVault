package com.epherical.fortune.impl.data.serializer;

import com.epherical.fortune.impl.object.EconomyUser;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.UUID;

public class EconomyUserSerializer implements JsonSerializer<EconomyUser>, JsonDeserializer<EconomyUser> {


    @Override
    public EconomyUser deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        String userID = object.getAsJsonPrimitive("uuid").getAsString();
        String userName = object.getAsJsonPrimitive("name").getAsString();
        double amount = object.getAsJsonPrimitive("balance").getAsDouble();
        return new EconomyUser(UUID.fromString(userID), userName, amount);
    }

    @Override
    public JsonElement serialize(EconomyUser src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("uuid", src.uuid().toString());
        object.addProperty("name", src.name());
        object.addProperty("balance", src.currentBalance());
        return object;
    }
}
