package com.epherical.fortune.impl.config;

import java.io.File;
import java.util.Locale;

public class FortuneConfig extends DefaultConfig {

    private StorageType type;
    private String dataPath;

    private String hostIP;
    private String username;
    private String password;
    private String databaseName;
    private int port;

    public FortuneConfig(File dataFolder, String configName) {
        super(dataFolder, configName);
    }

    @Override
    public boolean loadConfig() {
        if (!super.loadConfig()) {
            return false;
        }

        try {
            String typeString = conf.getString("storage-type", "JSON");
            type = StorageType.valueOf(typeString.toUpperCase(Locale.ROOT));

            this.dataPath = conf.getString("data-path", "");

            this.hostIP = conf.getString("mysql/host-ip", "");
            this.username = conf.getString("mysql/username", "");
            this.password = conf.getString("mysql/password", "");
            this.databaseName = conf.getString("mysql/dbname", "");
            this.port = conf.getInt("mysql/port", 0);

            return true;
        } catch (IllegalArgumentException e) {
            LOGGER.warn("storage-type was not expected value. Make sure the value matches what is in the config.", e);
        }

        return false;
    }

    public boolean usingDatabase() {
        return this.type == StorageType.MYSQL;
    }

    public StorageType storageType() {
        return type;
    }

    public String dataPath() {
        return dataPath;
    }

    public String hostIP() {
        return hostIP;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public String databaseName() {
        return databaseName;
    }

    public int port() {
        return port;
    }



    public enum StorageType {
        MYSQL,
        JSON
    }
}
