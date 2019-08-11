package com.ltp.config;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigTest {

    public static Function<String, File> configTestFileFinder() {
        return propertiesFileName -> Optional.ofNullable(Config.class.getResource(String.format("/%s", propertiesFileName)))
            .map(url -> {
                try {
                    return url.toURI();
                } catch (final URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            })
            .map(File::new)
            .orElse(null);
    }

    @BeforeEach
    void resetConfigBefore() {
        Config.reload(configTestFileFinder());
    }

    @AfterAll
    static void resetConfigAfter() {
        Config.reload(configTestFileFinder());
    }

    @Test
    void testDefaultStringValue() {
        assertEquals("ltp", Config.getInfluxUser());
    }

    @Test
    void testDefaultBooleanValue() {
        assertEquals(true, Config.isInfluxBatch());
    }

    @Test
    void testDefaultIntegerValue() {
        assertEquals(100, Config.getInfluxBatchMaxTimeMs());
    }

    // @Test
    // void testOverriddenStringValue() {
    //     assertEquals("testing", Config.getInfluxPassword());
    // }

    // @Test
    // void testOverriddenIntegerValue() {
    //     assertEquals(1234, Config.getInfluxBatchMaxSize());
    // }

    // @Test
    // void testOverriddenBooleanValue() {
    //     assertFalse(Config.isInfluxGzip());
    // }

    @Test
    void testRefreshingConfigOnTheFly() {
        // Assert the default value:
        assertEquals("ltp", Config.getInfluxUser());

        // Load in a new value:
        final Properties properties = new Properties();
        properties.put("influxUser", "screw");
        Config.readConfigFromProperties(properties);

        // Test that it worked:
        assertEquals("screw", Config.getInfluxUser());
    }
}
