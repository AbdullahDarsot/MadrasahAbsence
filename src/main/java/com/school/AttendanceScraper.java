package com.school;

import java.time.Duration;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

public class AttendanceScraper {
    private static final Logger logger = Logger.getLogger(AttendanceScraper.class.getName());

    private static final String LOGIN_URL = "https://mms.mtiuk.org";
    private static final String ATTENDANCE_URL = "https://mms.mtiuk.org/register.php?mode=attendance";

    private static final String USER_EMAIL = "your-email";
    private static final String USER_PASSWORD = "your-password";

    private static final String CSV_PATH = "src/main/resources/numbers.csv";

    // Normalize scraped names
    private static String normalizeScrapedName(String name) {
        if (name == null) return "";
        return name.replaceAll("[^\\p{L} ]", "")
                   .replaceAll("\\s+", " ")
                   .trim()
                   .toLowerCase();
    }

    public static void main(String[] args) {
        logger.info("Starting Attendance Scraper...");

        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();

        try {
            // Browser info
            Capabilities caps = ((RemoteWebDriver) driver).getCapabilities();
            System.out.println("Browser: " + caps.getBrowserName() + " " + caps.getBrowserVersion());
            System.out.println("Driver version: " + caps.getCapability("chromedriverVersion"));

            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.manage().window().maximize();

            LoginPage loginPage = new LoginPage(driver);
            AttendancePage attendancePage = new AttendancePage(driver);

            // 1️⃣ Login
            logger.info("Logging in...");
            driver.get(LOGIN_URL);
            loginPage.login(USER_EMAIL, USER_PASSWORD);

            // 2️⃣ Attendance page
            driver.get(ATTENDANCE_URL);

            // 3️⃣ Apply filters
            attendancePage.openFilters();
            attendancePage.selectQuality();
            attendancePage.enterPercentage("50");
            attendancePage.setDateRange();
            attendancePage.selectAboveBelow("Below");
            attendancePage.selectFilterResults();

            logger.info("Filters applied. Fetching absentees...");

            List<String[]> absentees = attendancePage.getAbsentees();

            if (absentees.isEmpty()) {
                logger.info("No absentees found.");
            } else {
                WhatsAppMessenger messenger = new WhatsAppMessenger(CSV_PATH);
                messenger.openWhatsApp();
                System.out.println("Please scan WhatsApp QR code and press Enter when done...");
                new Scanner(System.in).nextLine(); // wait for manual QR scan

                for (String[] row : absentees) {
                    String rawName = row[0];
                    String normalizedName = normalizeScrapedName(rawName);

                    System.out.println("Looking up: '" + normalizedName + "'");
                    String phoneNumber = messenger.getPhoneForStudent(normalizedName);

                    if (phoneNumber != null && !phoneNumber.isEmpty()) {
                        String message = "Assalamu Alaikum, we noticed that " + rawName +
                                " was absent today. Could you please let us know the reason?";
                        messenger.sendMessage(phoneNumber, message);
                        System.out.println("Sent message for " + rawName + " (" + phoneNumber + ")");
                    } else {
                        System.out.println("No phone number found for " + rawName);
                    }
                }

                messenger.close();
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred!", e);
        } finally {
            driver.quit();
        }
    }
}
