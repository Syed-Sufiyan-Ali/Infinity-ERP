package com.nova.factoryerp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);
    private static final Properties props = new Properties();
    private static AppConfig instance;

    static {
        try (InputStream in = AppConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in == null) throw new RuntimeException("application.properties not found on classpath");
            props.load(in);
            log.info("Configuration loaded successfully");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }
    }

    private AppConfig() {}

    public static AppConfig getInstance() {
        if (instance == null) instance = new AppConfig();
        return instance;
    }

    public String get(String key) {
        return props.getProperty(key, "");
    }

    public String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        try { return Integer.parseInt(props.getProperty(key)); }
        catch (Exception e) { return defaultValue; }
    }

    public String getDbUrl()      { return get("db.url"); }
    public String getDbUsername() { return get("db.username"); }
    public String getDbPassword() { return get("db.password"); }
    public String getAppName()    { return get("app.name", "Nova Factory ERP"); }
    public String getAppVersion() { return get("app.version", "1.0.0"); }
}
