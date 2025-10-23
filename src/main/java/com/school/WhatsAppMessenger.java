package com.school;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

public class WhatsAppMessenger {

    private WebDriver driver;
    private WebDriverWait wait;
    private Map<String, String> numbersMap = new HashMap<>();

    public WhatsAppMessenger(String csvPath) throws Exception {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        loadNumbers(csvPath);
    }

    // Load CSV numbers and normalize names
    private void loadNumbers(String csvPath) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line;
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String student = normalizeName(parts[0]);
                    String phone = parts[1].trim().replaceAll("[^0-9]", ""); // remove + or spaces
                    numbersMap.put(student, phone);
                }
            }
        }
        System.out.println("Loaded " + numbersMap.size() + " phone numbers.");
        System.out.println("CSV keys:");
        for (String key : numbersMap.keySet()) {
            System.out.println("'" + key + "'");
        }
    }

    // Normalize names: remove +, lowercase, remove non-letter chars, collapse spaces
    private String normalizeName(String name) {
        if (name == null) return "";
        return name.replace("+", "")
                   .replaceAll("[^\\p{L} ]", "")
                   .replaceAll("\\s+", " ")
                   .trim()
                   .toLowerCase();
    }

    public void openWhatsApp() {
        driver.get("https://web.whatsapp.com/");
        System.out.println("Please scan the QR code to log in.");
    }

    public void sendMessage(String phoneNumber, String message) throws Exception {
        if (phoneNumber == null || phoneNumber.isEmpty()) return;

        // Search for contact
        WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@contenteditable='true'][@data-tab='3']")));
        searchBox.clear();
        searchBox.sendKeys(phoneNumber);
        searchBox.sendKeys(Keys.ENTER);

        // Wait for message box
        WebElement messageBox = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@contenteditable='true'][@data-tab='10']")));
        messageBox.sendKeys(message);

        // Click send
        WebElement sendBtn = driver.findElement(By.cssSelector("div[aria-label='Send'][role='button']"));
        sendBtn.click();

        Thread.sleep(1000);
    }

    // Get phone number for a normalized student name
    public String getPhoneForStudent(String studentName) {
        return numbersMap.get(normalizeName(studentName));
    }

    public void close() {
        if (driver != null) driver.quit();
    }
}
