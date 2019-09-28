package com.ltp;

import org.apache.log4j.Logger;

public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class);

    public static void main(String[] args) {

        DeviceDiscoveryThread sensorDiscovery = new DeviceDiscoveryThread();
        LOG.info("Starting intial scan");
        // Initial population of sensors
        sensorDiscovery.run();

        LOG.info("Starting data collection");
        // Kick off timer to search for more
        sensorDiscovery.start();

        // Read sensors
        ExternalMonitorThread sensorMonitor = new ExternalMonitorThread();
        sensorMonitor.start();

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {}
        //System.exit(0); // due to a bug in the InfluxDB library, we have to force the exit as a
                        // workaround. See: https://github.com/influxdata/influxdb-java/issues/359
    }

}
