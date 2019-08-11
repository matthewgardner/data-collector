package com.ltp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
            for (String address : Config.getDeviceAddresses()) {
                readUrl(address);
            }
        } catch (Exception e) {
            LOG.error("InterruptedException", e);
        }
    }

    private static void readUrl(String address) {
        try {
            URL url = new URL("http://" + address + "/cm?cmnd=status%2010");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String output;
            while ((output = br.readLine()) != null) {
                processInput(address, output);
            }
            conn.disconnect();
        } catch (MalformedURLException e) {
            LOG.error("MalformedURLException", e);
        } catch (IOException e) {
            LOG.error("IOException", e);
        }
    }

    private static void processInput(String address, String input) {
        String deviceName = Config.getDeviceFriendlyName(address);
        String deviceType = Config.getDeviceType(address);

        Point point = "POW".equals(deviceType) ? SensorPower.generate(address, deviceName, input).getInfluxdbPoint()
                : SensorTemp.generate(address, deviceName, input).getInfluxdbPoint();
        Config.getDBConnection().save(point);
    }
}
