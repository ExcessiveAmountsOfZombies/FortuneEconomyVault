package com.epherical.fortune.impl.config.annotation;

import com.epherical.fortune.impl.config.FortuneConfig;

public class OtherFortuneConfig {

    @IntegerValue(value = 1)
    @CommentValue(comment = "Do not edit this value, it is for upgrading the config.")
    private int configVersion;

    @BooleanValue(value = true)
    @CommentValue(comment = "Enables or disables transaction logging. enabled by default.")
    private boolean logging;

    @StringValue(value = "")
    @CommentValue(comment = "A relative data path for flat file storage, This can be used if you wanted to " +
            "have a global balance system with flat files. EX: ../../../data")
    private String dataPath;

    @StorageValue(value = FortuneConfig.StorageType.JSON)
    @CommentValue(comment = "Two data storage options, MYSQL or JSON")
    private FortuneConfig.StorageType storageType;

    @StringValue(value = "", configPath = "mysql")
    private String hostIP;

    @StringValue(value = "", configPath = "mysql")
    private String username;

    @StringValue(value = "", configPath = "mysql")
    private String password;

    @StringValue(value = "", configPath = "mysql")
    private String dbname;

    @IntegerValue(value = 0, key = "mysql")
    private int port;

}
