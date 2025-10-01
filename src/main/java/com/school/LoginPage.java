package com.school;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage {
    private WebDriver driver;

    // Locators
    private static final By EMAIL_INPUT = By.name("email");
    private static final By PASSWORD_INPUT = By.name("password");
    private static final By LOGIN_BUTTON = By.xpath("//button[normalize-space()='Login']");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    public void login(String email, String password) {
        driver.findElement(EMAIL_INPUT).sendKeys("info@mtiuk.org");
        driver.findElement(PASSWORD_INPUT).sendKeys("gee");
        driver.findElement(LOGIN_BUTTON).click();
    }
}
