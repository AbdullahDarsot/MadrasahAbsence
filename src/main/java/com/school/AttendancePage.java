package com.school;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AttendancePage {
    private WebDriver driver;
    private WebDriverWait wait;

    public AttendancePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Clicks today's badge on the calendar and returns a list of unauthorised absentees.
     */
    public List<String> getTodayUnauthorisedAbsentees() {
        try {
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String todayIdSuffix = today.format(formatter) + "0"; // 0 = unauthorised
            System.out.println("Looking for today's badge with suffix: " + todayIdSuffix);

            // Find all badges and filter by today's suffix
            List<WebElement> badges = driver.findElements(By.cssSelector("[data-target]"));
            WebElement todayBadge = badges.stream()
                    .filter(b -> b.getAttribute("data-target").contains(todayIdSuffix))
                    .findFirst()
                    .orElse(null);

            if (todayBadge == null) {
                System.out.println("No badge found for today!");
                return Collections.emptyList();
            }

            // Click the badge
            todayBadge.click();
            System.out.println("Clicked today's badge.");

            // Wait for modal
            String modalId = todayBadge.getAttribute("data-target").replace("#", "");
            WebElement modalBody = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("#" + modalId + " .modal-body")));

            // Extract student names
            List<WebElement> students = modalBody.findElements(By.tagName("a"));
            List<String> studentNames = students.stream()
                    .map(WebElement::getText)
                    .map(name -> name.replace("×", "").trim())
                    .collect(Collectors.toList());

            // Close modal
            WebElement closeButton = driver.findElement(By.cssSelector("#" + modalId + " .modal-footer button"));
            closeButton.click();
            System.out.println("Closed the modal.");

            return studentNames;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error fetching today's absentees.");
            return Collections.emptyList();
        }
    }
}
