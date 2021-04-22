package com.epherical.fortune.impl.config;

import com.epherical.fortune.impl.config.annotation.BooleanValue;
import com.epherical.fortune.impl.config.annotation.EnumValue;
import com.epherical.fortune.impl.config.annotation.IntegerValue;
import com.epherical.fortune.impl.config.annotation.StringValue;

public class FortuneConfig {

    @IntegerValue(value = 1, comment = "Do not edit this value, it is for upgrading the config.")
    private int configVersion;

    @BooleanValue(value = true, comment = "Enables or disables transaction logging. enabled by default.")
    private boolean logging;

    @StringValue(value = "", comment = "A relative data path for flat file storage," +
            " This can be used if you wanted to have a global balance system with flat files. EX: ../../../data", oldVars = {"data-path"})
    private String dataPath;

    @EnumValue(clazz = StorageType.class, value = "JSON", comment = "Two data storage options, MYSQL or JSON", oldVars = {"storage-type"})
    private StorageType storageType;

    @StringValue(value = "", configPath = "mysql", oldVars = {"host-ip"})
    private String hostIP;

    @StringValue(value = "", configPath = "mysql")
    private String username;

    @StringValue(value = "", configPath = "mysql")
    private String password;

    @StringValue(value = "", configPath = "mysql")
    private String dbname;

    @IntegerValue(value = 0, configPath = "mysql")
    private int port;

    public FortuneConfig() {

    }


    public boolean usingDatabase() {
        return this.storageType == StorageType.MYSQL;
    }

    public StorageType storageType() {
        return storageType;
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
        return dbname;
    }

    public int port() {
        return port;
    }

    public enum StorageType {
        MYSQL,
        JSON
    }

    @Override
    public String toString() {
        return "OtherFortuneConfig{" +
                "configVersion=" + configVersion +
                ", logging=" + logging +
                ", dataPath='" + dataPath + '\'' +
                ", storageType=" + storageType +
                ", hostIP='" + hostIP + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", dbname='" + dbname + '\'' +
                ", port=" + port +
                '}';
    }
}
