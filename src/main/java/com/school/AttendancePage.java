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
        String modalId = "#absent" + today.format(formatter) + "0";

        // Find ONLY unauthorised absence badge for today
        WebElement todayBadge = driver.findElements(
                By.cssSelector("span.badge-light[data-target^='#absent'][data-target$='0']"))
            .stream()
            .filter(b -> modalId.equals(b.getAttribute("data-target")))
            .findFirst()
            .orElse(null);

        if (todayBadge == null) {
            System.out.println("No unauthorised absences found for today.");
            return Collections.emptyList();
        }

        todayBadge.click();

        WebElement modalBody = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(modalId + " .modal-body")));

        // Only pull unauthorised absence users (badge-light)
        List<String> studentNames = modalBody
                .findElements(By.cssSelector("a.badge-light"))
                .stream()
                .map(WebElement::getText)
                .map(t -> t.replace("×", "").trim())
                .collect(Collectors.toList());

        // close modal
        driver.findElement(By.cssSelector(modalId + " .modal-footer button")).click();

        return studentNames;

    } catch (Exception e) {
        e.printStackTrace();
        return Collections.emptyList();
    }
}

}
