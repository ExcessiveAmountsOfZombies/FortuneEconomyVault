package com.epherical.fortune.impl.data;

import com.epherical.fortune.impl.config.FortuneConfig;
import com.epherical.fortune.impl.exception.EconomyException;
import com.epherical.fortune.impl.object.EconomyUser;
import com.google.common.collect.Lists;
import net.milkbowl.vault.economy.EconomyResponse;

import java.sql.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

public class EconomyDataMySQL extends EconomyData {

    private final String TABLE_PREFIX = "fortune_";
    private final String BALANCES_TABLE = TABLE_PREFIX + "balances";
    private final String LOGGING_TABLE = TABLE_PREFIX + "log";

    private Connection connection;

    private final String hostIP;
    private final String username;
    private final String password;
    private final String dbName;
    private final int port;

    public EconomyDataMySQL(FortuneConfig config) {
        super(config);
        this.hostIP = config.hostIP();
        this.username = config.username();
        this.password = config.password();
        this.dbName = config.databaseName();
        this.port = config.port();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = openConnection(this.hostIP, this.username, this.password, this.dbName, this.port);

        } catch (ClassNotFoundException | SQLException e) {
            connection = null;
            e.printStackTrace();
        }
        validateTable();
    }

    @Override
    public void close() {
        super.close();
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection openConnection(String hostIP, String username, String password, String dbName, int port) throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://" + hostIP + ":" + port + "/" + dbName, username, password);
    }

    public boolean validateTable() {
        if (!isConnected()) {
            return false;
        }

        query("CREATE TABLE IF NOT EXISTS " + BALANCES_TABLE + " ("
            + "uuid varchar(36) NOT NULL PRIMARY KEY,"
            + "name varchar(40) NOT NULL,"
            + "balance double NOT NULL)");

        query("create table if not exists " + LOGGING_TABLE +
                "(id int auto_increment," +
                "uuid varchar(36) not null, " +
                "name varchar(40) null, " +
                "amount int not null, " +
                "balance int null, " +
                "error varchar(255) null, " +
                "success boolean not null," +
                "time timestamp not null, " +
                "constraint logging_pk " +
                "primary key (id));");

        return true;
    }

    public boolean isConnected() {
        boolean closed = false;
        boolean valid = false;
        boolean exists = connection != null;

        if (exists) {
            try {
                closed = connection.isClosed();
            } catch (SQLException e) {
                closed = true;
                e.printStackTrace();
            }

            if (!closed) {
                try {
                    valid = connection.isValid(2);
                } catch (SQLException ignored) {
                }
            }
        }

        if (exists && !closed && valid) {
            // still connected to the database.
            return true;
        }

        if (exists && !closed) {
            try {
                connection.close();
            } catch (SQLException ignored) {}
        }

        try {
            openConnection(hostIP, username, password, dbName, port);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            if (connection != null && !connection.isClosed()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // if we get down here it's not connected to the database
        return false;
    }


    @Override
    public EconomyUser loadUser(UUID uuid) throws EconomyException {
        if (isConnected()) {

            String query = "SELECT uuid, name, balance " +
                    "FROM " + BALANCES_TABLE +
                    " WHERE " + "uuid = ?;";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uuid.toString());
                ResultSet set = statement.executeQuery();
                if (set.next()) {
                    String id = set.getString(1);
                    String name = set.getString(2);
                    double balance = set.getDouble(3);
                    set.close();
                    return new EconomyUser(UUID.fromString(id), name, balance);
                }


                set.close();
            } catch (SQLException e) {
                throw new EconomyException(e.getMessage());
            }
        }
        return null;
    }

    @Override
    public boolean userExists(String name) throws EconomyException {
        if (isConnected()) {
            String query = "SELECT 1 " +
                    "FROM " + BALANCES_TABLE +
                    " WHERE " + "name = ?;";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, name);
                ResultSet set = statement.executeQuery();
                if (set.next()) {
                    boolean value = set.getInt(1) != 0;
                    set.close();
                    return value;
                }
                set.close();
            } catch (SQLException e) {
                throw new EconomyException(e.getMessage());
            }
        }
        return false;
    }

    @Override
    public boolean userExists(UUID uuid) throws EconomyException {
        if (isConnected()) {
            String query = "SELECT 1 " +
                    "FROM " + BALANCES_TABLE +
                    " WHERE " + "uuid = ?;";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uuid.toString());
                ResultSet set = statement.executeQuery();
                if (set.next()) {
                    boolean value = set.getInt(1) != 0;
                    set.close();
                    return value;
                }
                set.close();
            } catch (SQLException e) {
                throw new EconomyException(e.getMessage());
            }
        }
        return false;
    }

    @Override
    public boolean saveUser(EconomyUser user) throws EconomyException {
        if (isConnected()) {
            String query = "INSERT INTO " + BALANCES_TABLE + " (uuid, name, balance) VALUES (?, ?, ?)" +
                    "ON DUPLICATE KEY UPDATE " +
                    "name=?, balance=balance + ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, user.uuid().toString());
                statement.setString(2, user.name());
                statement.setDouble(3, user.currentBalance());
                statement.setString(4, user.name());
                statement.setDouble(5, user.transactionBalance());
                statement.executeUpdate();

                cache.invalidate(user.uuid());
            } catch (SQLException e) {
                throw new EconomyException(e.getMessage());
            }
        }

        return false;
    }

    @Override
    public EconomyResponse userDeposit(UUID user, double amount) {
        if (isConnected()) {
            String query = "UPDATE " + BALANCES_TABLE +
                    " SET balance = balance + ?" +
                    " WHERE uuid = ?";

            EconomyUser econUser = getUser(user);

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setDouble(1, amount);
                statement.setString(2, user.toString());
                statement.executeUpdate();
                cache.invalidate(user);
                EconomyResponse response = new EconomyResponse(amount, econUser.currentBalance() + amount, EconomyResponse.ResponseType.SUCCESS, "");
                saveTransaction(response, user, econUser.name());
                return response;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        EconomyResponse response = new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "");
        saveTransaction(response, user, "");
        return response;
    }

    @Override
    public EconomyResponse userWithdraw(UUID user, double amount) {
        if (isConnected()) {
            String query = "UPDATE " + BALANCES_TABLE +
                    " SET balance = balance - ?" +
                    " WHERE uuid = ?";

            EconomyUser econUser = getUser(user);

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setDouble(1, amount);
                statement.setString(2, user.toString());
                statement.executeUpdate();
                cache.invalidate(user);
                EconomyResponse response = new EconomyResponse(amount, econUser.currentBalance() - amount, EconomyResponse.ResponseType.SUCCESS, "");
                saveTransaction(response, user, econUser.name());
                return response;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        EconomyResponse response = new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "");
        saveTransaction(response, user, "");
        return response;
    }

    @Override
    public List<EconomyUser> users() {
        List<EconomyUser> users = Lists.newArrayList();
        if (isConnected()) {
            String query = "SELECT * FROM " + BALANCES_TABLE;

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                ResultSet set = statement.executeQuery();
                while (set.next()) {
                    String id = set.getString(1);
                    String name = set.getString(2);
                    double balance = set.getDouble(3);
                    users.add(new EconomyUser(UUID.fromString(id), name, balance));
                }
                set.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        return users;
    }

    @Override
    public Callable<Boolean> logTransaction(EconomyResponse response, UUID uuid, String name) {
        return () -> {
            if (!config.logTransactions()) {
                return false;
            }
            if (isConnected()) {
                String query = "INSERT INTO " + LOGGING_TABLE + " " +
                        "(uuid, name, amount, balance, error, success, time)" +
                        " VALUES (?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, uuid.toString());
                    statement.setString(2, name);
                    statement.setDouble(3, response.amount);
                    statement.setDouble(4, response.balance);
                    statement.setString(5, response.errorMessage);
                    statement.setBoolean(6, response.transactionSuccess());
                    statement.setTimestamp(7, Timestamp.from(Instant.now()));
                    statement.executeUpdate();
                    return true;
                } catch (SQLException e) {
                    throw new EconomyException(e.getMessage());
                }
            }
            return false;
        };
    }

    @Override
    public EconomyUser loadUser(String name) throws EconomyException {
        if (isConnected()) {

            String query = "SELECT uuid, name, balance " +
                    "FROM " + BALANCES_TABLE +
                    " WHERE " + "name = ?;";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, name);
                ResultSet set = statement.executeQuery();
                if (set.next()) {
                    String id = set.getString(1);
                    double balance = set.getDouble(3);
                    set.close();
                    return new EconomyUser(UUID.fromString(id), name, balance);
                }


                set.close();
            } catch (SQLException e) {
                throw new EconomyException(e.getMessage());
            }
        }
        return null;
    }


    private void query(String query) {
        if (!isConnected()) {
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
