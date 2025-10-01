package com.school;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

public class WhatsAppMessenger {

    private WebDriver driver;
    private Map<String, String> numbersMap = new HashMap<>();

    // Constructor that sets up WebDriver and loads numbers
    public WhatsAppMessenger(String csvPath) throws Exception {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();

        loadNumbers(csvPath); // load CSV during construction
    }

    // Load numbers.csv into memory
    private void loadNumbers(String csvPath) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line;
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String student = parts[0].trim();
                    String phone = parts[1].trim();
                    numbersMap.put(student, phone);
                }
            }
        }
        System.out.println("📂 Loaded " + numbersMap.size() + " phone numbers.");
    }

    public void openWhatsApp() {
        driver.get("https://web.whatsapp.com/");
        System.out.println("📱 Please scan the QR code with your WhatsApp app.");
    }

    public void sendMessage(String phoneNumber, String message) throws Exception {
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
        String url = "https://web.whatsapp.com/send?phone=" + phoneNumber + "&text=" + encodedMessage;
        driver.get(url);

        Thread.sleep(5000); // wait for chat to load
        driver.findElement(By.xpath("//span[@data-icon='send']")).click();
        Thread.sleep(2000);
    }

    public void close() {
        driver.quit();
    }

    // Get phone number for a student
    public String getPhoneForStudent(String studentName) {
        return numbersMap.get(studentName);
    }
}
