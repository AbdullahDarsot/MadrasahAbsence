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

    // Load CSV where name and number are in the same cell
    private void loadNumbers(String csvPath) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line;
            br.readLine(); // skip header

            while ((line = br.readLine()) != null) {
                line = line.replace("\uFEFF", "").trim();
                if (line.isEmpty()) continue;

                // Extract phone = all digits
                String phone = line.replaceAll("[^0-9]", "");

                // Extract student name = everything except digits
                String student = line.replaceAll("[0-9]", "")
                                     .replaceAll("[^\\p{L} ]", "")
                                     .replaceAll("\\s+", " ")
                                     .trim()
                                     .toLowerCase();

                if (student.isEmpty() || phone.isEmpty()) {
                    System.out.println("⚠️ Skipping invalid line: " + line);
                    continue;
                }

                numbersMap.put(student, phone);
            }
        }

        System.out.println("✅ Loaded " + numbersMap.size() + " phone numbers.");
    }

    // Normalize names
    private String normalizeName(String name) {
        if (name == null) return "";
        return name.replaceAll("[^\\p{L} ]", "")
                   .replaceAll("\\s+", " ")
                   .trim()
                   .toLowerCase();
    }

    public void openWhatsApp() {
        driver.get("https://web.whatsapp.com/");
        System.out.println("📱 Please scan the QR code to log in.");
    }

    public void sendMessage(String phoneNumber, String message) throws Exception {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            // ✅ Skip silently
            return;
        }

        try {
            WebElement searchBox = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//div[@contenteditable='true'][@data-tab='3']")
                    )
            );

            searchBox.clear();
            searchBox.sendKeys(phoneNumber);
            searchBox.sendKeys(Keys.ENTER);

            WebElement messageBox = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//div[@contenteditable='true'][@data-tab='10']")
                    )
            );

            messageBox.sendKeys(message);

            WebElement sendBtn = driver.findElement(By.cssSelector("div[aria-label='Send'][role='button']"));
            sendBtn.click();

            Thread.sleep(1000);

        } catch (Exception e) {
            // ✅ On any failure (e.g., invalid number), skip silently
            System.out.println("⚠️ Could not send message to " + phoneNumber + " — skipping.");
        }
    }

    // Return clean, validated number or null if invalid
    public String getPhoneForStudent(String studentName) {
        String key = normalizeName(studentName);

        if (!numbersMap.containsKey(key)) {
            System.out.println("⚠️ No number found for: " + studentName + " — skipping.");
            return null;
        }

        String number = numbersMap.get(key);

        // Clean again
        number = number.replaceAll("[^0-9]", "").trim();

        // Validate digits only
        if (!number.matches("\\d{10,15}")) {
            System.out.println("⚠️ Invalid number for " + studentName + ": " + number + " — skipping.");
            return null;
        }

        // UK conversion 07 → 447
        if (number.startsWith("07")) {
            number = "44" + number.substring(1);
        }

        return number;
    }

    public void close() {
        if (driver != null) driver.quit();
    }
}
