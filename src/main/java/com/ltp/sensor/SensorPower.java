package com.ltp.sensor;

import org.influxdb.dto.Point;
import org.json.JSONObject;

public class SensorPower {

   private String deviceName;
   private String ipAddress;
   private Double voltage;
   private Double current;
   private Double power;
   private Double reactivePower;
   private Double apparentPower;
   private Double total;
   private Double today;

   private SensorPower(){}

   public static SensorPower generate(String ipAddress, String name, String input) {
      JSONObject inputObj = new JSONObject(input);
      JSONObject statusObj = inputObj.getJSONObject("StatusSNS");
      JSONObject energyObj = statusObj.getJSONObject("ENERGY");

      // TODO: Handle Null
      Double voltage = energyObj.getDouble("Voltage");
      Double current = energyObj.getDouble("Current");
      Double power = energyObj.getDouble("Power");
      Double reactivePower = energyObj.getDouble("ReactivePower");
      Double apparentPower = energyObj.getDouble("ApparentPower");
      Double total = energyObj.getDouble("Total");
      Double today = energyObj.getDouble("Today");


      SensorPower sensorPower = new SensorPower();
      sensorPower.ipAddress = ipAddress;
      sensorPower.deviceName = name;
      sensorPower.voltage = voltage;
      sensorPower.current = current;
      sensorPower.power = power;
      sensorPower.reactivePower = reactivePower;
      sensorPower.apparentPower = apparentPower;
      sensorPower.total = total;
      sensorPower.today = today;

      return sensorPower;
  }

  public Point getInfluxdbPoint() {
      Point.Builder p = Point.measurement("power_monitor").tag("device", this.ipAddress);
      p.tag("friendly_name", this.deviceName);
      p.addField("voltage", this.voltage);
      p.addField("current", this.current);
      p.addField("power", this.power);
      p.addField("reactive_power", this.reactivePower);
      p.addField("apparent_power", this.apparentPower);
      p.addField("total", this.total);
      p.addField("today", this.today);
      return p.build();
  }

  public static boolean isValid(String input){
      try {
         JSONObject inputObj = new JSONObject(input);
         JSONObject statusObj = inputObj.getJSONObject("StatusSNS");
         return statusObj.has("ENERGY");
      } catch (Exception e) {}
      return false;
}
}
