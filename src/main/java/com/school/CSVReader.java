package com.school;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {

    public static List<String> readNumbers() {
        List<String> numbers = new ArrayList<>();
        try {
            // Load the file from resources
            InputStream is = CSVReader.class.getClassLoader().getResourceAsStream("numbers.csv");
            if (is == null) {
                throw new RuntimeException("numbers.csv not found in resources!");
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                numbers.add(line.trim());
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return numbers;
    }
}
