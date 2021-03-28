package com.epherical.fortune.impl.data;



import com.epherical.fortune.impl.exception.EconomyException;
import com.epherical.fortune.impl.object.EconomyUser;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

public interface EconomyData {


    void close();

    Future<EconomyUser> loadUser(UUID uuid) throws EconomyException;

    boolean userExists(UUID uuid);

    Future<Boolean> saveUser(EconomyUser user) throws EconomyException;

    Future<EconomyQuery> userDeposit(EconomyUser user, double amount);

    Future<EconomyQuery> userWithdraw(EconomyUser user, double amount);

    List<EconomyUser> users();
}
