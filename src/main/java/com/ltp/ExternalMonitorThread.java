package com.ltp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.influxdb.dto.Point;

import com.ltp.config.Config;
import com.ltp.sensor.SensorPower;
import com.ltp.sensor.SensorTemp;

public class ExternalMonitorThread implements Runnable {

    private static final Logger LOG = Logger.getLogger(ExternalMonitorThread.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void start() {
        final Runnable monitorRunner = new ExternalMonitorThread();
        scheduler.scheduleAtFixedRate(monitorRunner, 10, 10, TimeUnit.SECONDS);
    }

    public void run() {
        
        try {
            Map<String, String> sensors = DeviceDiscoveryThread.getSonoffMap();
            for (String address : sensors.keySet()) {
                readUrl(address,sensors.get(address));
            }
        } catch (Exception e) {
            LOG.error("InterruptedException", e);
        }
    }

    private static void readUrl(String address, String hostname) {
        try {
            URL url = new URL("http://" + address + "/cm?cmnd=status%2010");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            if (connection.getResponseCode() != 200) {
                DeviceDiscoveryThread.remove(address);
                throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String output;
                while ((output = br.readLine()) != null) {
                    processInput(address, hostname, output);
                }
            }
        } catch (MalformedURLException e) {
            LOG.error("MalformedURLException", e);
            DeviceDiscoveryThread.remove(address);
        } catch (IOException e) {
            LOG.error("IOException", e);
            DeviceDiscoveryThread.remove(address);

        }
    }

    private static void processInput(String address, String hostname, String input) {
        //TODO: Do we want to overide the name with something friendly
        //String deviceName = Config.getDeviceFriendlyName(address);
        if ( SensorPower.isValid(input)) {
            Point point = SensorPower.generate(address, hostname, input).getInfluxdbPoint();
            Config.getDBConnection().save(point);
        } else if ( SensorTemp.isValid(input)) {
            Point point = SensorTemp.generate(address, hostname, input).getInfluxdbPoint();
            Config.getDBConnection().save(point);
        } else {
            LOG.warn("Found sensor thats not understood " + hostname + "@" + address);
        }
    }
}
