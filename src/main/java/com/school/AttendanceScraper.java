package com.school;

import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

public class AttendanceScraper {
    private static final Logger logger = Logger.getLogger(AttendanceScraper.class.getName());

    // =======================
    // 🔹 Config
    // =======================
    private static final String LOGIN_URL = "https://mms.mtiuk.org";
    private static final String ATTENDANCE_URL = "https://mms.mtiuk.org/register.php?mode=attendance";

    private static final String USER_EMAIL = "your-email"; // TODO: load securely from env/config
    private static final String USER_PASSWORD = "your-password";

    // Path to numbers CSV
    private static final String CSV_PATH = "src/main/resources/numbers.csv";

    public static void main(String[] args) {
        logger.info("Starting Attendance Scraper...");

        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();

        try {
            // Page objects
            LoginPage loginPage = new LoginPage(driver);
            AttendancePage attendancePage = new AttendancePage(driver);

            // 1. Login
            logger.info("Logging in...");
            driver.get(LOGIN_URL);
            loginPage.login(USER_EMAIL, USER_PASSWORD);
            Thread.sleep(1000);

            // 2. Go to attendance
            logger.info("Opening attendance page...");
            driver.get(ATTENDANCE_URL);

            // 3. Apply filters
            Thread.sleep(1000);
            attendancePage.openFilters();
            Thread.sleep(1000);
            attendancePage.selectQuality();
            Thread.sleep(1000);
            attendancePage.enterPercentage("50");
            Thread.sleep(1000);
            attendancePage.setDateRange("30092025", "30092025");
            Thread.sleep(1000);
            attendancePage.selectAboveBelow("Below");
            Thread.sleep(1000);
            attendancePage.selectFilterResults();
            Thread.sleep(2000); // wait for table to refresh

            logger.info("Filters applied successfully. Fetching absentees...");

            // 4. Scrape students list
            List<String[]> absentees = attendancePage.getAbsentees();

            if (absentees.isEmpty()) {
                logger.info("No absentees found.");
            } else {
                logger.info("Absentees found. Sending WhatsApp messages...");

                // 🔹 WhatsApp integration
                WhatsAppMessenger messenger = new WhatsAppMessenger(CSV_PATH);
                messenger.openWhatsApp();
                Thread.sleep(15000); // wait for QR scan

                for (String[] row : absentees) {
                    String studentName = row[0];
                    String phoneNumber = messenger.getPhoneForStudent(studentName);

                    if (phoneNumber != null && !phoneNumber.isEmpty()) {
                        String message = "Assalamu Alaikum, we noticed that " + studentName +
                                " was absent today. Could you please let us know the reason?";
                        messenger.sendMessage(phoneNumber, message);
                        System.out.println("✅ Sent message for " + studentName + " (" + phoneNumber + ")");
                    } else {
                        System.out.println("⚠️ No phone number found for " + studentName);
                    }
                }

                messenger.close();
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred while scraping!", e);
        } finally {
            logger.info("Closing browser...");
            driver.quit();
        }
    }
}
