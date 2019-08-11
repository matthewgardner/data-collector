package com.ltp.db;

import org.influxdb.dto.Point;

public interface DBConnection {

    /**
     * Saves the point
     *
     * @param point
     */
    void save(Point point);
    
    /**
     * Closes the DB connection
     */
    void close();
}
