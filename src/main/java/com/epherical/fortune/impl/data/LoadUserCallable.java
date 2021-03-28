package com.epherical.fortune.impl.data;

import com.epherical.fortune.impl.exception.EconomyException;
import com.epherical.fortune.impl.object.EconomyUser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.Callable;

public class LoadUserCallable implements Callable<EconomyUser> {

    private final UUID uuid;
    private final Path userFolder;
    private final Gson gson;

    public LoadUserCallable(UUID uuid, Path userFolder, Gson gson) {
        this.uuid = uuid;
        this.userFolder = userFolder;
        this.gson = gson;
    }

    @Override
    public EconomyUser call() throws Exception {
        try {
            File file = new File(userFolder.resolve(uuid.toString()).toFile() + ".json");
            try (FileReader reader = new FileReader(file)) {
                JsonObject object = gson.fromJson(reader, JsonObject.class);
                String userID = object.getAsJsonPrimitive("uuid").getAsString();
                String userName = object.getAsJsonPrimitive("name").getAsString();
                double amount = object.getAsJsonPrimitive("balance").getAsDouble();

                return new EconomyUser(UUID.fromString(userID), userName, amount);
            }
        } catch (IOException e) {
            throw new EconomyException("Could not load user with uuid " + uuid);
        }
    }
}
