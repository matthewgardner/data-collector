package com.ltp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.apache.log4j.Logger;

import com.ltp.config.Config;

public class DeviceDiscoveryThread implements Runnable {

    private static final Logger LOG = Logger.getLogger(DeviceDiscoveryThread.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static Map<String, String> addressToHostnameMap = new ConcurrentHashMap<>();

    public void start() {
        final Runnable discoveryRunner = new DeviceDiscoveryThread();
        scheduler.scheduleAtFixedRate(discoveryRunner, 10, 60 * 60, TimeUnit.SECONDS);
    }

    public void run() {
        ForkJoinPool forkJoinPool = new ForkJoinPool(Config.getDeviceDiscoveryThreadPool());
        try {
            forkJoinPool.submit(
                    () -> IntStream.range(Config.getDeviceDiscoveryStartRange(), Config.getDeviceDiscoveryStopRange())
                            .parallel().forEach(DeviceDiscoveryThread::checkPort))
                    .get();
        } catch (InterruptedException e1) {
            LOG.error("Failed to check port (1)", e1);
        } catch (ExecutionException e1) {
            LOG.error("Failed to check port (2)", e1);
        }
        LOG.info("Rescanning Complete - " + addressToHostnameMap.keySet().size());
    }

    public static Map<String, String> getSonoffMap() {
        return addressToHostnameMap;
    }

    private static void checkPort(Integer address) {
        String ipAddress = Config.getDeviceDiscoveryBaseAddress() + address;
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(ipAddress);
            if (inetAddress.isReachable(1000)) {
                String hostname = inetAddress.getCanonicalHostName();
                if (isSonoff(hostname)) {
                    LOG.info("Sonof found " + hostname + "@" + ipAddress);
                    addressToHostnameMap.put(ipAddress, hostname);
                }
            }
        } catch (UnknownHostException e1) {
            LOG.error("Failed to check port (3)", e1);
            // Do nothing
        } catch (IOException e) {
            LOG.error("Failed to check port (4)", e);
        }
    }

    private static boolean isSonoff(String hostname) {
        return hostname != null && hostname.startsWith("sonoff");
    }

    public static void remove(String address) {
        if (addressToHostnameMap.containsKey(address)) {
            LOG.info("Sonof lost and being removed " + addressToHostnameMap.get(address) + "@" + address);
            addressToHostnameMap.remove(address);
        }
    }
}
