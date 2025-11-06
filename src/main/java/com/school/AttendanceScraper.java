package com.school;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.ArrayList;
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
    private static final String ATTENDANCE_URL = "https://mms.mtiuk.org/register.php?mode=absent-calendar";

    private static final String USER_EMAIL = "your-email";
    private static final String USER_PASSWORD = "your-password";

    private static final String CSV_PATH = "src/main/resources/numbers.csv";
    private static final String SKIPPED_CSV_PATH = "src/main/resources/skipped_students.csv";

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

            // 2️⃣ Navigate to Attendance page
            driver.get(ATTENDANCE_URL);

            // 3️⃣ Fetch today's unauthorised absentees
            logger.info("Fetching today's unauthorised absentees...");
            List<String> absentees = attendancePage.getTodayUnauthorisedAbsentees();

            if (absentees.isEmpty()) {
                logger.info("No unauthorised absentees found today.");
            } else {
                WhatsAppMessenger messenger = new WhatsAppMessenger(CSV_PATH);
                messenger.openWhatsApp();
                System.out.println("Please scan WhatsApp QR code and press Enter when done...");
                new Scanner(System.in).nextLine(); // wait for manual QR scan

                List<String> skippedStudents = new ArrayList<>();

                for (String rawName : absentees) {
                    String normalizedName = normalizeScrapedName(rawName);

                    System.out.println("Looking up: '" + normalizedName + "'");
                    String phoneNumber = messenger.getPhoneForStudent(normalizedName);

                    if (phoneNumber == null) {
                        System.out.println("Skipping " + rawName + " due to missing or invalid number.");
                        skippedStudents.add(rawName);
                        continue; // skip
                    }

                    String message = "Assalamu Alaikum, we noticed that " + rawName +
                            " was absent today. Could you please let us know the reason?";

                    messenger.sendMessage(phoneNumber, message);
                    System.out.println("✅ Sent message for " + rawName + " (" + phoneNumber + ")");
                }

                messenger.close();

                // 4️⃣ Append skipped students to CSV
                if (!skippedStudents.isEmpty()) {
                    File file = new File(SKIPPED_CSV_PATH);
                    boolean fileExists = file.exists();

                    try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) { // append mode
                        if (!fileExists) {
                            pw.println("Student Name,Date"); // header
                        }

                        String today = java.time.LocalDate.now().toString();
                        for (String s : skippedStudents) {
                            pw.println(s + "," + today);
                        }
                    }

                    System.out.println("\n⚠️ Skipped students appended to " + SKIPPED_CSV_PATH);
                } else {
                    System.out.println("\n✅ All students were messaged successfully.");
                }
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred!", e);
        } finally {
            driver.quit();
        }
    }
}
