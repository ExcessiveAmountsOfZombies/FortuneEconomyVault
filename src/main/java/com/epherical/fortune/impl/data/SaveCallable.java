package com.epherical.fortune.impl.data;

import com.epherical.fortune.impl.exception.EconomyException;
import com.epherical.fortune.impl.object.EconomyUser;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

public class SaveCallable implements Callable<Boolean> {

    private EconomyUser user;
    private Path userFolder;
    private Gson gson;

    public SaveCallable(EconomyUser user, Path userFolder, Gson gson) {
        this.user = user;
        this.userFolder = userFolder;
        this.gson = gson;
    }

    @Override
    public Boolean call() throws Exception {
        synchronized (user) {
            File file = new File(userFolder.resolve(user.uuid().toString()).toFile() + ".json");
            if (!file.exists()) {
                try {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(gson.toJson(user));
                return true;
            } catch (IOException e) {
                throw new EconomyException("Could not save user " + user.name() + " " + user.uuid());
            }
        }
    }
}
