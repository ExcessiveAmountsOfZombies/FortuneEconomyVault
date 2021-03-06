package com.epherical.fortune.impl.data;

import com.epherical.fortune.impl.config.FortuneConfig;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class EconomyDataFlatFile extends EconomyData {

    private final Path userFolder;
    private final Path logFolder;

    private final Gson gson;

    private Map<String, UUID> userCache = new HashMap<>();
    private final Object logLock = new Object();

    public EconomyDataFlatFile(FortuneConfig config, Path dataFolder) {
        super(config);
        this.userFolder = dataFolder.resolve("balances");
        this.logFolder = dataFolder.resolve("logs");
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(EconomyUser.class, new EconomyUserSerializer())
                .create();
        users().forEach(user -> userCache.putIfAbsent(user.name().toLowerCase(), user.uuid()));
    }

    @Override
    public EconomyUser loadUser(UUID uuid) throws EconomyException {
        try {
            File file = new File(userFolder.resolve(uuid.toString()).toFile() + ".json");
            try (FileReader reader = new FileReader(file)) {
                EconomyUser user = gson.fromJson(reader, EconomyUser.class);
                userCache.putIfAbsent(user.name().toLowerCase(), user.uuid());
                return user;
            }
        } catch (IOException e) {
            throw new EconomyException("Could not load user with uuid " + uuid);
        }
    }

    @Override
    public EconomyUser loadUser(String name) {
        UUID value = userCache.getOrDefault(name.toLowerCase(), null);
        return value != null ? getUser(value) : null;
    }

    @Override
    public boolean userExists(String name) {
        return userCache.containsKey(name.toLowerCase());
    }

    @Override
    public boolean userExists(UUID uuid) {
        return Files.exists(userFolder.resolve(uuid.toString() + ".json"));
    }

    @Override
    public boolean saveUser(EconomyUser user) throws EconomyException {
        File file = new File(userFolder.resolve(user.uuid().toString()).toFile() + ".json");
        EconomyUser freshUser = null;
        if (!file.exists()) {
            try {
                userCache.putIfAbsent(user.name().toLowerCase(), user.uuid());
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // we re-grab the user to make sure we are only applying the transactions from the user and not changes in their balance.
            freshUser = loadUser(user.uuid());
        }
        try (FileWriter writer = new FileWriter(file)) {
            user.addRefreshedUser(freshUser);
            writer.write(gson.toJson(user, EconomyUser.class));
            user.cancelFuture();
            cache.invalidate(user.uuid());
        } catch (IOException e) {
            throw new EconomyException("Could not save user " + user.name() + " " + user.uuid());
        }
        // the balance needs to be immutable, so we don't diverge between servers.
        return getUser(user.uuid()) != null;
    }

    @Override
    public EconomyResponse userDeposit(UUID user, double amount) {
        try {
            EconomyUser econUser = getUser(user);
            econUser.add(amount);
            EconomyResponse response = new EconomyResponse(amount, econUser.currentBalance(), EconomyResponse.ResponseType.SUCCESS, "");
            saveTransaction(response, user, econUser.name());
            return response;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            EconomyResponse response = new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, e.getMessage());
            saveTransaction(response, user, "");
            return response;
        }
    }

    @Override
    public EconomyResponse userWithdraw(UUID user, double amount) {
        try {
            EconomyUser econUser = getUser(user);
            econUser.subtract(amount);
            EconomyResponse response = new EconomyResponse(amount, econUser.currentBalance(), EconomyResponse.ResponseType.SUCCESS, "");
            saveTransaction(response, user, econUser.name());
            return response;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            EconomyResponse response = new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, e.getMessage());
            saveTransaction(response, user, "");
            return response;
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

    @Override
    public Callable<Boolean> logTransaction(EconomyResponse response, UUID uuid, String name) {
        return () -> {
            if (!config.logTransactions()) {
                return false;
            }
            String fileName = LocalDateTime.now().toLocalDate().toString() + ".json";
            File file = logFolder.resolve(fileName).toFile();
            if (!file.exists()) {
                try {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                synchronized (logLock) {
                    JsonObject object = new JsonObject();
                    object.addProperty("amount", response.amount);
                    object.addProperty("balance", response.balance);
                    object.addProperty("success", response.transactionSuccess());
                    object.addProperty("opt_error", response.errorMessage);
                    object.addProperty("uuid", uuid.toString());
                    object.addProperty("name", name);
                    try (FileWriter writer = new FileWriter(file, true)) {
                        writer.write(gson.toJson(object) + ",\n");
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return false;
        };
    }
}
