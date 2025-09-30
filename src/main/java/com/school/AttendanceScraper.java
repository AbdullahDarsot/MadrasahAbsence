package com.school;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

public class AttendanceScraper {
    private static final Logger logger = Logger.getLogger(AttendanceScraper.class.getName());

    public static class Student {
        String name;
        String phone;
        String status;

        public Student(String name, String phone, String status) {
            this.name = name;
            this.phone = phone;
            this.status = status;
        }

        @Override
        public String toString() {
            return "Student{name='" + name + "', phone='" + phone + "', status='" + status + "'}";
        }
    }

    public static void main(String[] args) {
        logger.info("Starting Attendance Scraper...");

        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();

        try {
            // 1. Open login page
            String loginUrl = "https://mms.mtiuk.org";
            logger.info("Navigating to login page: " + loginUrl);
            driver.get(loginUrl);

            // 2. Log in
            logger.info("Entering login credentials...");
            driver.findElement(By.name("email")).sendKeys("info@mtiuk.org");
            driver.findElement(By.name("password")).sendKeys("gee");
            driver.findElement(By.xpath("//button[normalize-space()='Login']")).click();
            logger.info("Login submitted.");

            // 3. Go to attendance page
            String attendanceUrl = "https://mms.mtiuk.org/register.php?mode=absent-calendar";
            logger.info("Navigating to attendance page: " + attendanceUrl);
            driver.get(attendanceUrl);

            // 4. Open the modal with absentees (adjust selector if modal needs to be clicked)
            driver.findElement(By.cssSelector("span[data-target='#absent202509290']")).click();
            Thread.sleep(1000);
            //WebElement table = driver.findElement(By.xpath("//*[@id=\"absent202509290\"]/div/div/div[2]/a[1]")); // adjust ID if needed
            logger.info("Attendance container found.");

            // 5. Locate the modal body containing absentees
            WebElement modalBody = driver.findElement(By.xpath("//*[@id=\"absent202509290\"]/div/div/div[2]"));
            List<WebElement> absentLinks = modalBody.findElements(By.cssSelector("a.badge-danger"));

            List<Student> absentees = new ArrayList<>();
            logger.info("Total absentees found: " + absentLinks.size());

            for (WebElement link : absentLinks) {
                String name = link.getText().trim();
                Student student = new Student(name, "", "Absent"); // phone unknown
                absentees.add(student);
                logger.warning("Absent detected → " + name);
            }

            // 6. Print absent summary
            logger.info("=== Final Report ===");
            if (absentees.isEmpty()) {
                logger.info("✅ No absentees found today.");
            } else {
                absentees.forEach(s -> logger.info("❌ Absent: " + s));
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred while scraping!", e);
        } finally {
            logger.info("Closing browser...");
            driver.quit();
        }
    }
}
