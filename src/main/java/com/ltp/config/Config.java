package com.ltp.config;

import com.ltp.db.DBConnection;
import com.ltp.db.InfluxDBConnection;
import com.ltp.db.LoggingDBConnection;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

public abstract class Config {

    private static final Logger LOG = Logger.getLogger(Config.class);

    private static final String DEVICE_COLLECTOR_PROPERTIES = "device-collector.properties";
    private static final String DEVICE_NAMES_PROPERTIES = "device-names.properties";
    private static final String DEVICE_SOURCES_PROPERTIES = "device-sources.properties";


    private static String influxUrl;
    private static String influxDatabase;
    private static String influxMeasurement;
    private static String influxUser;
    private static String influxPassword;
    private static String influxRetentionPolicy;
    private static boolean influxGzip;
    private static boolean influxBatch;
    private static int influxBatchMaxSize;
    private static int influxBatchMaxTimeMs;

    private static String baseAddress;

    private static String storageMethod;

    private static final Map<String, String> DEVICE_NAMES = new HashMap<>();
    private static final Map<String, String> DEVICE_SOURCES = new HashMap<>();
    private static DBConnection dbConnection;
    private static Function<String, File> configFileFinder;

    static {
        reload();
    }

    public static void reload() {
        reload(defaultConfigFileFinder());
    }

    public static void reload(final Function<String, File> configFileFinder) {
        Config.configFileFinder = configFileFinder;
        loadDefaults();
        readConfigFromProperties(readConfigIntoProps(DEVICE_COLLECTOR_PROPERTIES));
        readDeviceNames();
        readDeviceSources();
    }

    private static void loadDefaults() {
        influxUrl = "http://localhost:8086";
        influxDatabase = "ltp";
        influxMeasurement = "ltp_measurements";
        influxUser = "ltp";
        influxPassword = "ltp";
        influxRetentionPolicy = "autogen";
        influxGzip = true;
        influxBatch = true;
        influxBatchMaxSize = 2000;
        influxBatchMaxTimeMs = 100;

        storageMethod = "influxdb";

        baseAddress = "192.168.1.";

        DEVICE_NAMES.clear();
        DEVICE_SOURCES.clear();
        dbConnection = null;
    }

    private static Properties readConfigIntoProps(String configFilename) {
        Properties props = new Properties();
        try {
            final File configFile = configFileFinder.apply(configFilename);
            if (configFile != null) {
                LOG.debug("Config: " + configFile);
                props.load(new FileInputStream(configFile));
            }
        } catch (IOException ex) {
            LOG.warn("Failed to read configuration, using default values...", ex);
        }
        return props;
    }

    public static void readConfigFromProperties(final Properties props) {
        influxUrl = props.getProperty("influxUrl", influxUrl);
        influxDatabase = props.getProperty("influxDatabase", influxDatabase);
        influxMeasurement = props.getProperty("influxMeasurement", influxMeasurement);
        influxUser = props.getProperty("influxUser", influxUser);
        influxPassword = props.getProperty("influxPassword", influxPassword);
        storageMethod = props.getProperty("storage.method", storageMethod);
        influxRetentionPolicy = props.getProperty("influxRetentionPolicy", influxRetentionPolicy);
        influxGzip = Boolean.valueOf(props.getProperty("influxGzip", influxGzip?"true":"false"));
        influxBatch = Boolean.valueOf(props.getProperty("influxBatch", influxGzip?"true":"false"));
        influxBatchMaxSize = Integer.valueOf(props.getProperty("influxBatchMaxSize", String.valueOf(influxBatchMaxSize)));
        influxBatchMaxTimeMs = Integer.valueOf(props.getProperty("influxBatchMaxTime", String.valueOf(influxBatchMaxTimeMs)));
        baseAddress = props.getProperty("baseAddress", baseAddress);
    }


    private static Function<String, File> defaultConfigFileFinder() {
        return propertiesFileName -> {
            try {
                final File jarLocation = new File(Config.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
                Optional<File> configFile = findConfigFile(propertiesFileName, jarLocation);
                if (!configFile.isPresent()) {
                    // look for config files in the parent directory if none found in the current directory, this is useful during development when
                    // RuuviCollector can be run from maven target directory directly while the config file sits in the project root
                    final File parentFile = jarLocation.getParentFile();
                    configFile = findConfigFile(propertiesFileName, parentFile);
                }
                return configFile.orElse(null);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static Optional<File> findConfigFile(String propertiesFileName, File parentFile) {
        return Optional.ofNullable(parentFile.listFiles(f -> f.isFile() && f.getName().equals(propertiesFileName)))
            .filter(configFiles -> configFiles.length > 0)
            .map(configFiles -> configFiles[0]);
    }


    private static void readDeviceNames() {
        try {
            final File configFile = configFileFinder.apply(DEVICE_NAMES_PROPERTIES);
            if (configFile != null) {
                LOG.debug("Device names: " + configFile);
                Properties props = new Properties();
                props.load(new FileInputStream(configFile));
                Enumeration<?> e = props.propertyNames();
                while (e.hasMoreElements()) {
                    String key = StringUtils.trimToEmpty((String) e.nextElement()).toUpperCase();
                    String value = StringUtils.trimToEmpty(props.getProperty(key));
                    if (key.length() >0 && value.length() > 0) {
                        DEVICE_NAMES.put(key, value);
                    }
                }
            }
        } catch (IOException ex) {
            LOG.warn("Failed to read device names", ex);
        }
    }

    private static void readDeviceSources() {
        try {
            final File configFile = configFileFinder.apply(DEVICE_SOURCES_PROPERTIES);
            if (configFile != null) {
                LOG.debug("Device Source: " + configFile);
                Properties props = new Properties();
                props.load(new FileInputStream(configFile));
                Enumeration<?> e = props.propertyNames();
                while (e.hasMoreElements()) {
                    String key = StringUtils.trimToEmpty((String) e.nextElement()).toUpperCase();
                    String value = StringUtils.trimToEmpty(props.getProperty(key));
                    if (key.length() >0 && value.length() > 0) {
                        DEVICE_SOURCES.put(key, value);
                    }
                }
            }
        } catch (IOException ex) {
            LOG.warn("Failed to read device names", ex);
        }
    }

    public static DBConnection getDBConnection() {
        if (dbConnection == null) {
            dbConnection = createDBConnection();
        }
        return dbConnection;
    }

    private static DBConnection createDBConnection() {
        switch (storageMethod) {
            case "influxdb":
                return new InfluxDBConnection();
            case "logging":
                return new LoggingDBConnection();
            default:
                try {
                    LOG.info("Trying to use custom DB dbConnection class: " + storageMethod);
                    return (DBConnection) Class.forName(storageMethod).newInstance();
                } catch (final Exception e) {
                    throw new IllegalArgumentException("Invalid storage method: " + storageMethod, e);
                }
        }
    }

    public static String getInfluxUrl() {
        return influxUrl;
    }

    public static String getInfluxDatabase() {
        return influxDatabase;
    }

    public static String getInfluxMeasurement() {
        return influxMeasurement;
    }

    public static String getInfluxUser() {
        return influxUser;
    }

    public static String getInfluxPassword() {
        return influxPassword;
    }

    public static String getInfluxRetentionPolicy() {
        return influxRetentionPolicy;
    }

    public static boolean isInfluxGzip() {
        return influxGzip;
    }

    public static boolean isInfluxBatch() {
        return influxBatch;
    }

    public static int getInfluxBatchMaxSize() {
        return influxBatchMaxSize;
    }

    public static int getInfluxBatchMaxTimeMs() {
        return influxBatchMaxTimeMs;
    }

    public static String getBaseAddress(){
        return baseAddress;
    }

    public static Set<String> getDeviceAddresses(){
        return DEVICE_NAMES.keySet();
    }
    public static String getDeviceFriendlyName(String address){
        return DEVICE_NAMES.containsKey(address)? DEVICE_NAMES.get(address):address;
    }

    public static Set<String> getDeviceSources(){
        return DEVICE_SOURCES.keySet();
    }

    public static String getDeviceType(String address){
        return DEVICE_SOURCES.containsKey(address)? DEVICE_SOURCES.get(address):address;
    }
}
