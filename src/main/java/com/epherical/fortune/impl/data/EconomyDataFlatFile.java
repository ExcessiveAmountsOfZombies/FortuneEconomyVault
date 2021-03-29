package com.epherical.fortune.impl.data;

import com.epherical.fortune.impl.data.serializer.EconomyUserSerializer;
import com.epherical.fortune.impl.exception.EconomyException;
import com.epherical.fortune.impl.object.EconomyUser;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.milkbowl.vault.economy.EconomyResponse;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class EconomyDataFlatFile extends EconomyData {

    private final Path userFolder;

    private final Gson gson;

    public EconomyDataFlatFile(Path dataFolder, String pluginName) {
        this.userFolder = dataFolder.resolve(pluginName + File.separator + "balances");
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(EconomyUser.class, new EconomyUserSerializer())
                .create();

    }

    @Override
    public EconomyUser loadUser(UUID uuid) throws EconomyException {
        try {
            File file = new File(userFolder.resolve(uuid.toString()).toFile() + ".json");
            try (FileReader reader = new FileReader(file)) {
                return gson.fromJson(reader, EconomyUser.class);
            }
        } catch (IOException e) {
            throw new EconomyException("Could not load user with uuid " + uuid);
        }
    }

    @Override
    public boolean userExists(UUID uuid) {
        return Files.exists(userFolder.resolve(uuid.toString() + ".json"));
    }

    @Override
    public boolean saveUser(EconomyUser user) throws EconomyException {
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
            writer.write(gson.toJson(user, EconomyUser.class));
            user.cancelFuture();
            cache.invalidate(user.uuid());
            return true;
        } catch (IOException e) {
            throw new EconomyException("Could not save user " + user.name() + " " + user.uuid());
        }
    }

    @Override
    public EconomyResponse userDeposit(UUID user, double amount) {
        try {
            EconomyUser econUser = getUser(user);
            econUser.add(amount);
            return new EconomyResponse(amount, econUser.currentBalance(), EconomyResponse.ResponseType.SUCCESS, "");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, e.getMessage());
        }
    }

    @Override
    public EconomyResponse userWithdraw(UUID user, double amount) {
        try {
            EconomyUser econUser = getUser(user);
            econUser.subtract(amount);
            return new EconomyResponse(amount, econUser.currentBalance(), EconomyResponse.ResponseType.SUCCESS, "");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, e.getMessage());
        }
    }

    @Override
    public List<EconomyUser> users() {
        List<EconomyUser> users = Lists.newArrayList();
        try {
            users.addAll(Files.walk(userFolder).filter(path -> !path.toFile().isDirectory()).map(path -> {
                try (FileReader reader = new FileReader(path.toFile())) {
                    JsonObject object = gson.fromJson(reader, JsonObject.class);
                    String userID = object.getAsJsonPrimitive("uuid").getAsString();
                    String userName = object.getAsJsonPrimitive("name").getAsString();
                    double amount = object.getAsJsonPrimitive("balance").getAsDouble();
                    return new EconomyUser(UUID.fromString(userID), userName, amount);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }).map(economyUser -> {
                // we map it back to any users that are already in the cache so their balances are pulled instead.
                EconomyUser user = cache.getIfPresent(economyUser.uuid());
                return user != null ? user : economyUser;
            }).collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }
}
