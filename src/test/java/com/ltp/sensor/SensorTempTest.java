package com.ltp.sensor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class SensorTempTest {

    private static List<String> readJsonFile(String filename) {
        URL fileURL = SensorTemp.class.getResource(String.format("/%s", filename));
        BufferedReader br =null;
        try {
            br = new BufferedReader(new FileReader(fileURL.getFile()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String output;
        List<String> results = new ArrayList<>();
        try {
            while ((output = br.readLine()) != null) {
                results.add(output);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
         return results;
    }

    @Test
    void testTempString() {
        List<String> testJson = readJsonFile("TempReading.dat");
        assertTrue(SensorTemp.isValid(testJson.get(0)));
        assertTrue(SensorTemp.isValid(testJson.get(1)));
    }


    @Test
    void testPowerString() {
        List<String> testJson = readJsonFile("PowerReading.dat");
        assertFalse(SensorTemp.isValid(testJson.get(0)));
    }
}
