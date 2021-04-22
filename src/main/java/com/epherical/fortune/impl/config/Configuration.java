package com.epherical.fortune.impl.config;

import com.epherical.fortune.impl.config.annotation.EnumValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Configuration<T> {

    protected static final Logger LOGGER = LogManager.getLogger();

    protected File dataFolder;
    protected YamlConfiguration conf;
    protected final String configName;
    private final Class<T> clazz;
    private T instance;


    public Configuration(Class<T> clazz, File dataFile, String configName) {
        this.clazz = clazz;
        this.dataFolder = dataFile;
        this.configName = configName;
    }

    public T loadConfig() {
        File file = getConfigFile();
        if (file != null && file.exists()) {

            try {
                String result = parseConfig(file);
                saveFile(result, file);
            } catch (NoSuchFieldException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                e.printStackTrace();
            }

            return instance;
        }
        return null;
    }

    private String parseConfig(File file) throws InstantiationException, IllegalAccessException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException {
        instance = clazz.newInstance();
        conf = new YamlConfiguration();
        conf.options().pathSeparator('/');
        try {
            conf.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        Field[] fields = instance.getClass().getDeclaredFields();

        Map<String, String> fieldNameComments = new HashMap<>();
        for (Field field : fields) {
            String fieldName = field.getName();
            for (Annotation annotation : field.getAnnotations()) {
                String configPath = (String) annotation.annotationType().getMethod("configPath").invoke(annotation);
                Object value = annotation.annotationType().getMethod("value").invoke(annotation);
                String[] oldVariables = ((String[]) annotation.annotationType().getMethod("oldVars").invoke(annotation));
                String comment = (String) annotation.annotationType().getMethod("comment").invoke(annotation);
                if (comment.length() > 1) {
                    fieldNameComments.putIfAbsent(fieldName, comment);
                }

                // grab the old variables, and change them into ones that will match the current config value.
                for (String oldVariable : oldVariables) {
                    if (oldVariable.length() < 1) {
                        continue;
                    }
                    String key = configPath.length() > 1 ? configPath + "/" + oldVariable : oldVariable;
                    if (conf.isSet(key)) {
                        Object configObject = conf.get(key);
                        value = configObject.toString().length() < 1 ? value : configObject;
                        conf.set(key, null);
                    }
                }
                // after that has finished, we can grab anything that is the same and then change the value variable
                String key = configPath.length() > 1 ? configPath + "/" + fieldName : fieldName;
                if (conf.isSet(key)) {
                    value = conf.get(key);
                }


                field.setAccessible(true);
                if (configPath.length() > 1) {
                    ConfigurationSection section = conf.getConfigurationSection(configPath);
                    if (section != null) {
                        section.set(fieldName, value);
                    } else {
                        conf.createSection(configPath).set(fieldName, value);
                    }
                } else {
                    if (!conf.isSet(fieldName)) {
                        conf.set(fieldName, value);
                    }
                }

                if (annotation.annotationType().equals(EnumValue.class)) {
                    EnumValue enumValue = (EnumValue) annotation;
                    field.set(instance, enumValue.clazz().getMethod("valueOf", String.class).invoke(enumValue.clazz(), value.toString().toUpperCase()));
                } else {
                    field.set(instance, value);
                }
            }
        }

        String configString = conf.saveToString();
        StringBuilder builder = new StringBuilder();

        skipAfterFind:
        for (String s : configString.split("\n")) {
            if (s.startsWith("#")) {
                continue;
            }
            for (Map.Entry<String, String> entry : fieldNameComments.entrySet()) {
                if (s.contains(entry.getKey())) {
                    builder.append("# ").append(entry.getValue()).append("\n");
                    builder.append(s).append("\n");
                    // we continue here so we don't have duplicate keys/values
                    continue skipAfterFind;
                }
            }
            builder.append(s).append("\n");
        }

        return builder.toString();
    }

    public T configInstance() {
        return instance;
    }


    private File getConfigFile() {
        File file = new File(dataFolder, configName);
        if (file.exists()) {
            return file;
        } else {
            if (createFile(file)) {
                return file;
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

    private void saveFile(String text, File file) {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(text.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
