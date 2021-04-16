package com.epherical.fortune.impl.data;

import com.epherical.fortune.impl.exception.EconomyException;
import com.epherical.fortune.impl.object.EconomyUser;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.milkbowl.vault.economy.EconomyResponse;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public abstract class EconomyData {

    ScheduledExecutorService saveSchedule = Executors.newSingleThreadScheduledExecutor();

    LoadingCache<UUID, EconomyUser> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(20, TimeUnit.SECONDS)
            .expireAfterWrite(20, TimeUnit.SECONDS)
            .removalListener(notification -> {
                EconomyUser user = (EconomyUser) notification.getValue();
                user.cancelFuture();
            })
            .build(new CacheLoader<UUID, EconomyUser>() {
        @Override
        public EconomyUser load(UUID uuid) throws Exception {
            return loadUser(uuid);
        }
    });

    public EconomyData() {
        saveSchedule.scheduleAtFixedRate(() -> cache.cleanUp(), 1L, 1L, TimeUnit.MINUTES);
    }

    public void close() {
        saveSchedule.shutdown();
    }

    public abstract EconomyUser loadUser(UUID uuid) throws EconomyException;

    public abstract EconomyUser loadUser(String name) throws EconomyException;

    public abstract boolean userExists(String name) throws EconomyException;

    public abstract boolean userExists(UUID uuid) throws EconomyException;

    public abstract boolean saveUser(EconomyUser user) throws EconomyException;

    public abstract EconomyResponse userDeposit(UUID user, double amount);

    public EconomyResponse userDeposit(String user, double amount) throws EconomyException {
        return userDeposit(loadUser(user).uuid(), amount);
    }

    public abstract EconomyResponse userWithdraw(UUID user, double amount);

    public EconomyResponse userWithdraw(String user, double amount) throws EconomyException {
        return userWithdraw(loadUser(user).uuid(), amount);
    }

    public abstract List<EconomyUser> users();

    public abstract Callable<Boolean> logTransaction(EconomyResponse response, UUID uuid, String name);

    private Callable<EconomyUser> callUser(UUID uuid) {
        return () -> {
            EconomyUser user = loadUser(uuid);
            user.applyFuture(saveSchedule.scheduleAtFixedRate(user.scheduleSave(EconomyData.this), 1L, 1L, TimeUnit.SECONDS));
            return user;
        };
    }

    public void saveTransaction(EconomyResponse response, UUID uuid, String name) {
        saveSchedule.schedule(logTransaction(response, uuid, name), 0L, TimeUnit.SECONDS);
    }

    public EconomyUser getUser(UUID uuid) {
        try {
            return cache.get(uuid, callUser(uuid));
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
