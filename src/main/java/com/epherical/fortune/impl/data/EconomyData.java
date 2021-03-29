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
            .build(new CacheLoader<UUID, EconomyUser>() {
        @Override
        public EconomyUser load(UUID uuid) throws Exception {
            return loadUser(uuid);
        }
    });

    public void close() {
        saveSchedule.shutdown();
    }

    public abstract EconomyUser loadUser(UUID uuid) throws EconomyException;

    public abstract boolean userExists(UUID uuid);

    public abstract boolean saveUser(EconomyUser user) throws EconomyException;

    public abstract EconomyResponse userDeposit(UUID user, double amount);

    public abstract EconomyResponse userWithdraw(UUID user, double amount);

    public abstract List<EconomyUser> users();

    private Callable<EconomyUser> callUser(UUID uuid) {
        return () -> {
            EconomyUser user = loadUser(uuid);
            user.applyFuture(saveSchedule.scheduleAtFixedRate(user.scheduleSave(EconomyData.this), 10L, 10L, TimeUnit.SECONDS));
            return user;
        };
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
