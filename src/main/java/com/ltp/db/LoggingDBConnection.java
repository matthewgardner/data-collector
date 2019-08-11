package com.ltp.db;

import org.apache.log4j.Logger;
import org.influxdb.dto.Point;

public class LoggingDBConnection implements DBConnection {

    private static final Logger LOG = Logger.getLogger(LoggingDBConnection.class);

    public void save(Point point) {
        LOG.debug(point);
    }
    
    @Override
    public void close() {
    }
}
