package com.epherical.fortune.impl.data;

import com.epherical.fortune.impl.exception.EconomyException;
import com.epherical.fortune.impl.object.EconomyUser;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.milkbowl.vault.economy.EconomyResponse;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class EconomyDataFlatFile implements EconomyData {

    private final Path userFolder;

    private final Gson gson;

    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    public EconomyDataFlatFile(Path dataFolder, String pluginName) {
        this.userFolder = dataFolder.resolve(pluginName + File.separator + "balances");
        this.gson = new GsonBuilder().setPrettyPrinting().create();

    }

    public void close() {
        executor.shutdown();
    }

    @Override
    public Future<EconomyUser> loadUser(UUID uuid) {
        LoadUserCallable user = new LoadUserCallable(uuid, userFolder, gson);
        return executor.submit(user);
    }

    @Override
    public boolean userExists(UUID uuid) {
        return Files.exists(userFolder.resolve(uuid.toString() + ".json"));
    }

    @Override
    public Future<Boolean> saveUser(EconomyUser user) throws EconomyException {
        SaveCallable save = new SaveCallable(user, userFolder, gson);
        return executor.submit(save);
    }

    @Override
    public EconomyQuery userDeposit(EconomyUser user, double amount) {
        try {
            double bal = user.currentBalance() + amount;
            user.add(amount);
            saveUser(user);
            return new EconomyQuery(new EconomyResponse(true, ""), amount, bal);
        } catch (EconomyException e) {
            e.printStackTrace();
            return new EconomyQuery(new EconomyResponse(false, ""), 0, 0);
        }
    }

    @Override
    public EconomyQuery userWithdraw(EconomyUser user, double amount) {
        try {
            double bal = user.currentBalance() - amount;
            user.subtract(amount);
            saveUser(user);
            return new EconomyQuery(new EconomyResponse(true, ""), amount, bal);
        } catch (EconomyException e) {
            e.printStackTrace();
            return new EconomyQuery(new EconomyResponse(false, ""), 0, 0);
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
            }).collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }
}
