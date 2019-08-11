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

   private SensorPower(String ipAddress, String name, Double voltage, Double current, Double power, Double reactivePower, Double apparentPower) {
      this.ipAddress = ipAddress;
      this.deviceName = name;
      this.voltage = voltage;
      this.current = current;
      this.power = power;
      this.reactivePower = reactivePower;
      this.apparentPower = apparentPower;
   }

   public static SensorPower generate(String address, String name, String input) {
      JSONObject inputObj = new JSONObject(input);
      JSONObject statusObj = inputObj.getJSONObject("StatusSNS");
      JSONObject energyObj = statusObj.getJSONObject("ENERGY");
      Double voltage = energyObj.getDouble("Voltage");
      Double current = energyObj.getDouble("Current");
      Double power = energyObj.getDouble("Power");
      Double reactivePower = energyObj.getDouble("ReactivePower");
      Double apparentPower = energyObj.getDouble("ApparentPower");
      SensorPower sensorPower = new SensorPower(address, name, voltage, current, power, reactivePower, apparentPower);
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
      return p.build();
  }
}
