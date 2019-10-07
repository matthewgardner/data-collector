package com.ltp.sensor;

import org.influxdb.dto.Point;
import org.json.JSONObject;

public class SensorTemp {

   private String deviceName;
   private String ipAddress;
   private Double temperature;
   private Double humidity;

   private SensorTemp(String ipAddress, String name, Double temperature, Double humidity) {
      this.ipAddress = ipAddress;
      this.deviceName = name;
      this.temperature = temperature;
      this.humidity = humidity;
   }

   public static SensorTemp generate(String address, String name, String input) {
      JSONObject inputObj = new JSONObject(input);
      JSONObject statusObj = inputObj.getJSONObject("StatusSNS");
      JSONObject energyObj = statusObj.getJSONObject("SI7021");
      // TODO: Handle Null
      Double temperature = energyObj.getDouble("Temperature");
      Double humidity = energyObj.getDouble("Humidity");
      SensorTemp sensor = new SensorTemp(address, name, temperature, humidity);
      return sensor;
  }

  public Point getInfluxdbPoint() {
      Point.Builder p = Point.measurement("temp_monitor").tag("device", this.ipAddress);
      p.tag("friendly_name", this.deviceName);
      p.addField("temperature", this.temperature);
      p.addField("humidity", this.humidity);
      return p.build();
  }

  public static boolean isValid(String input){
     try {
      JSONObject inputObj = new JSONObject(input);
      JSONObject statusObj = inputObj.getJSONObject("StatusSNS");
      return statusObj.has("SI7021");
      } catch (Exception e) {}
      return false;
   }
}
