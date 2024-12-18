package com.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.remote.RemoteWebElement;

public class WebScrapingFB {
    private static final int WAIT_TIMEOUT = 30;
    
    public static void main(String[] args) {
        WebDriver driver = null;
        
        try {
            driver = initializeDriver();
            scrapePost(driver);
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
    
    private static WebDriver initializeDriver() {
        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver-win64\\chromedriver.exe");
        
        ChromeOptions options = new ChromeOptions();
        
        // Add arguments to make the browser appear more human-like
        options.addArguments("--start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-notifications");
        options.addArguments("--lang=en-US");
        
        // Add user agent
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
        
        // Add experimental flags
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);
        
        // Remove automation flags
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
        
        WebDriver driver = new ChromeDriver(options);
        
        // Add CDP commands to modify navigator.webdriver flag
        if (driver instanceof ChromeDriver) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("source", "Object.defineProperty(navigator, 'webdriver', { get: () => undefined });");
            ((ChromeDriver) driver).executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", parameters);
        }
        
        return driver;
    }
    
    private static void scrapePost(WebDriver driver) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT));
        
        try {
            // First navigate to Facebook homepage
            driver.get("https://www.facebook.com");
            System.out.println("Navigated to Facebook homepage");
            
            // Wait a bit and check if we're on the correct page
            Thread.sleep(2000);
            
            // Try to find login elements with different locators
            WebElement emailField = findLoginField(driver, wait);
            if (emailField == null) {
                throw new RuntimeException("Could not find email field with any known locator");
            }
            
            // Handle login
            handleLogin(driver, wait, emailField);
            
            // After successful login, navigate to the post
            System.out.println("Navigating to post...");
            Thread.sleep(3000); // Wait for login to complete
            driver.get("https://www.facebook.com/share/p/14eHng1xDF/?mibextid=oFDknk");
            
            // Wait for the modal dialog and extract text
            extractPostContent(driver, wait);
            
        } catch (Exception e) {
            System.err.println("Error during scraping: " + e.getMessage());
            throw e;
        }
    }
    
    private static WebElement findLoginField(WebDriver driver, WebDriverWait wait) {
        String[] emailLocators = {
            "input[name='email']",
            "input[id='email']",
            "input[type='text']",
            "input[data-testid='royal_email']"
        };
        
        for (String locator : emailLocators) {
            try {
                System.out.println("Trying to find email field with locator: " + locator);
                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(locator)));
                if (element.isDisplayed()) {
                    return element;
                }
            } catch (Exception e) {
                System.out.println("Locator " + locator + " failed");
            }
        }
        return null;
    }
    
    private static void handleLogin(WebDriver driver, WebDriverWait wait, WebElement emailField) throws InterruptedException {
        try {
            String fbEmail = "coolshark5954@gmail.com";
            String fbPassword = "sh@rkcool5954";
            
            if (fbEmail == null || fbPassword == null) {
                throw new IllegalArgumentException("Facebook credentials not found in environment variables");
            }
            
            // Clear and fill email
            emailField.clear();
            emailField.sendKeys(fbEmail);
            Thread.sleep(1000); // Simulate human typing delay
            
            // Find password field
            WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
            passwordField.clear();
            passwordField.sendKeys(fbPassword);
            Thread.sleep(1000);
            
            // Find and click login button
            WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit'], button[name='login']"));
            loginButton.click();
            
            // Wait for login to complete
            System.out.println("Waiting for login to complete...");
            Thread.sleep(5000);
            
        } catch (Exception e) {
            System.err.println("Login failed: " + e.getMessage());
            throw e;
        }
    }
    
    private static void extractPostContent(WebDriver driver, WebDriverWait wait) {
        // First wait for the modal dialog
        WebElement modal = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div[role='dialog']")));
        
        // Define selectors specifically for the post content within the modal
        String[] contentSelectors = {
            // Try to find text content within the modal
            "div[role='dialog'] div[dir='auto']",
            // More specific selector targeting the main post content
            "div[role='dialog'] div.x1iorvi4.x1pi30zi",
            // Target the post message area
            "div[role='dialog'] div[data-ad-comet-preview='message']",
            // Try finding by class name combinations
            "div[role='dialog'] div.xdj266r.x11i5rnm.xat24cr"
        };
        
        String postText = null;
        for (String selector : contentSelectors) {
            try {
                System.out.println("Trying to extract text using selector: " + selector);
                WebElement postContent = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));
                
                // Get text content
                postText = postContent.getText();
                
                // Verify we have actual content and it's within the modal
                if (postText != null && !postText.trim().isEmpty() && isElementInModal(postContent, modal)) {
                    System.out.println("Successfully extracted post text:\n" + postText);
                    return;
                }
            } catch (Exception e) {
                System.out.println("Selector failed: " + selector);
            }
        }
        
        if (postText == null || postText.trim().isEmpty()) {
            System.out.println("Could not extract post content with any selector");
        }
    }
    
    private static boolean isElementInModal(WebElement element, WebElement modal) {
        try {
            // Check if the element is a child of the modal
            WebElement parent = element;
            while (parent != null) {
                if (parent.equals(modal)) {
                    return true;
                }
                parent = (WebElement) ((JavascriptExecutor) ((RemoteWebElement) element).getWrappedDriver())
                    .executeScript("return arguments[0].parentNode;", parent);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}