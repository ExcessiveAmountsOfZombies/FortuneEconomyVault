package com.epherical.fortune.impl.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

public class DefaultConfig {

    protected static final Logger LOGGER = LogManager.getLogger();

    protected File dataFolder;
    protected YamlConfiguration conf;
    protected final String configName;


    public DefaultConfig(File dataFolder, String configName) {
        this.dataFolder = dataFolder;
        this.configName = configName;
    }

    public boolean loadConfig() {
        File file = getConfigFile();
        if (file != null && file.exists()) {
            this.conf = new YamlConfiguration();
            this.conf.options().pathSeparator('/');

            try {
                this.conf.load(file);
            } catch (InvalidConfigurationException | IOException e) {
                e.printStackTrace();
            }

            return !this.conf.getKeys(false).isEmpty();
        }
        return false;
    }

    private File getConfigFile() {
        File file = new File(dataFolder, configName);
        if (file.exists()) {
            return file;
        } else {
            try (InputStream stream = getClass().getResourceAsStream("/" + configName)) {
                byte[] bytes = new byte[stream.available()];
                stream.read(bytes);
                LOGGER.debug("Creating default config file: " + configName);
                if (!createFile(file)) {
                    return null;
                }
                try (FileOutputStream outputStream = new FileOutputStream(file)) {
                    outputStream.write(bytes);
                }
                return file;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    private boolean createFile(File file) {
        try {
            if (!file.getParentFile().exists() && file.getParentFile().mkdirs()) {
                LOGGER.debug("Created directory for: " + file.getParentFile().getCanonicalPath());
            }

            if (!file.exists() && file.createNewFile()) {
                return true;
            }
        } catch (IOException e) {
            LOGGER.warn("error creating new config file ", e);
        }
        return false;
    }

}
