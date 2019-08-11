package com.ltp;

import com.ltp.config.Config;

import org.apache.log4j.Logger;

public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class);

    public static void main(String[] args) {

        LOG.info("Storage is " + Config.getDBConnection().getClass());

        LOG.info("List of Devices");
        for (String address : Config.getDeviceAddresses()) {
            LOG.info("{ 'address' : '" + address + "', 'type' : '" + Config.getDeviceType(address)
                    + "', 'friendlyName' : '" + Config.getDeviceFriendlyName(address) + "'}");
        }

        ExternalMonitorThread monitor = new ExternalMonitorThread();
        monitor.start();

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {}
        //System.exit(0); // due to a bug in the InfluxDB library, we have to force the exit as a
                        // workaround. See: https://github.com/influxdata/influxdb-java/issues/359
    }
}
