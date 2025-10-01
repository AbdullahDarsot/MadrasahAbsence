package com.school;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class AttendancePage {
    private WebDriver driver;

    // Locators
    private static final By moreFilters = By.xpath("//*[@id='filter-btn']");
    private static final By qualityRadioButton = By.cssSelector("input[type='radio'][name='what_above_below'][value='quality']");
    private static final By percentageOption = By.cssSelector("input[name='ab_amount']");
    private static final By fromDatePicker = By.cssSelector("input[type='date'][name='date_from']");
    private static final By toDatePicker = By.cssSelector("input[type='date'][name='date_to']");
    private static final By aboveBelowDropdown = By.cssSelector("select[name='above_below']");
    private static final By filterResultsButton = By.xpath("//button[contains(@class,'btn-info')]");

    // Table locator
    private static final By attendanceTable = By.id("attendance");

    public AttendancePage(WebDriver driver) {
        this.driver = driver;
    }

    public void openFilters() {
        driver.findElement(moreFilters).click();
    }

    public void selectQuality() {
        driver.findElement(qualityRadioButton).click();
    }

    public void enterPercentage(String value) {
        driver.findElement(percentageOption).clear();
        driver.findElement(percentageOption).sendKeys(value);
    }

    public void setDateRange(String fromDate, String toDate) {
        driver.findElement(fromDatePicker).sendKeys(fromDate);
        driver.findElement(toDatePicker).sendKeys(toDate);
    }

    public void selectAboveBelow(String option) {
        WebElement dropdown = driver.findElement(aboveBelowDropdown);
        org.openqa.selenium.support.ui.Select select = new org.openqa.selenium.support.ui.Select(dropdown);
        select.selectByVisibleText(option);
    }

    public void selectFilterResults() {
        driver.findElement(filterResultsButton).click();
    }

    /**
     * Extracts absentee details from the attendance table.
     * @return List of String arrays, each array representing one row [Student, Class, Entries, Present, Absent, Late, %age, Strict %age, Quality].
     */
    public List<String[]> getAbsentees() {
        List<String[]> absentees = new ArrayList<>();

        WebElement table = driver.findElement(attendanceTable);
        List<WebElement> rows = table.findElements(By.xpath(".//tbody/tr"));

        for (WebElement row : rows) {
            List<WebElement> cols = row.findElements(By.tagName("td"));
            String[] rowData = new String[cols.size()];
            for (int i = 0; i < cols.size(); i++) {
                rowData[i] = cols.get(i).getText().trim();
            }
            absentees.add(rowData);
        }

        return absentees;
    }
}
