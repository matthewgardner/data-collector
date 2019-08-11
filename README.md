# DataCollector

DataCollector is an application for collecting sensor measurements from Sonoff Tasmota devices and storing them to InfluxDB.

### Requirements

* Maven
* JDK8 

### Building

Execute 

```sh
mvn clean package
```

### Configuration

The default configuration which works without a config file assumes InfluxDB is running locally with default settings, with a database called 'ltp'.
To change the default settings, copy the data-collector.properties.example file as data-collector.properties in the same directory as the collector jar file and change the settings you want.

To determine which sonoff Tasmota instances to target you MUST create a file called device-sources.properties in the same directory as the collector jar file and set the names and type of the devices.
POW - for Sonoff POW devices
TH - for Sonoff TH devices 

To give human readable friendly names to the sonoffs (based on their IP addresses), copy the device-names.properties.example file as device-names.properties in the same directory as the collector jar file and set the names in this file according to the examples there.

### Running

For built version (while in the "root" of the project):

```sh
java -jar target/data-collector-*.jar
```

Easily compile and run while developing:

```
mvn compile exec:java
```
