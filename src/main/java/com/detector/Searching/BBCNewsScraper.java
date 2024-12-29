package com.detector.Searching;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.util.Set;
import java.util.List;
import com.detector.CustomDataStructures.*;

public class BBCNewsScraper implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(BBCNewsScraper.class);
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Set<String> headlinesSet; 
    private static final int PAGES_TO_SCRAPE = 3;

    public BBCNewsScraper(int timeoutSeconds) {
        this.driver = initializeDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        this.headlinesSet = new CustomHashSet<>(); // Used HashSet for uniqueness of headlines
        logger.info("BBCNewsScraper initialized with timeout of {} seconds", timeoutSeconds);
    }
    private WebDriver initializeDriver() {
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--start-maximized");
            options.addArguments("--disable-notifications");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--no-sandbox");
            options.addArguments("--headless"); // Added headless mode for better performance
            options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
            return new ChromeDriver(options);
        } catch (Exception e) {
            logger.error("Failed to initialize WebDriver: {}", e.getMessage());
            throw new ScraperException("Failed to initialize WebDriver", e);
        }
    }
    public CustomQueue<String> scrapeBBCNews(String searchQuery) {
        try {
            navigateToHomepage();
            if (performSearch(searchQuery)) {
                collectHeadlinesFromMultiplePages();
            }
            return new CustomQueue<String>(headlinesSet); 
        } catch (Exception e) {
            logger.error("Error during scraping: {}", e.getMessage());
            return new CustomQueue<>();
        }
    }
    private void navigateToHomepage() {
        try {
            driver.get("https://www.bbc.co.uk/news");
            handleConsentDialog();
            logger.info("Navigated to BBC News homepage");
        } catch (Exception e) {
            logger.error("Failed to navigate to homepage: {}", e.getMessage());
            throw new ScraperException("Failed to navigate to homepage", e);
        }
    }
    private void handleConsentDialog() {
        try {
            WebElement consentButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='agree-button']")));
            consentButton.click();
            logger.info("Handled consent dialog");
        } catch (Exception e) {
            logger.debug("No consent dialog found or already accepted");
        }
    }
    private boolean performSearch(String query) {
        try {
            WebElement searchIcon = waitForElement(
                By.cssSelector("button[role='button'][aria-label='Search BBC']"));
            searchIcon.click();
            WebElement searchBox = waitForElement(
                By.cssSelector("[data-testid='search-input-field']"));
            searchBox.clear();
            searchBox.sendKeys(query);
            searchBox.sendKeys(Keys.RETURN);
            logger.info("Search performed for query: {}", query);
            return true;
        } catch (Exception e) {
            logger.error("Error during search: {}", e.getMessage());
            return false;
        }
    }
    private void collectHeadlinesFromMultiplePages() {
        try {
            waitForElement(By.cssSelector("[data-testid='newport-card']"));
            for (int page = 1; page <= PAGES_TO_SCRAPE; page++) {
                logger.info("Collecting headlines from page {}", page);
                collectHeadlinesFromCurrentPage();
                if (page < PAGES_TO_SCRAPE) {
                    String currentUrl = driver.getCurrentUrl();  // Store current URL to check if page changes
                    WebElement pagination = new WebDriverWait(driver, Duration.ofSeconds(20)) // Wait for pagination element with timeout
                        .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='pagination']")));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", pagination); // Scroll to pagination
                    Thread.sleep(1500);
                    List<WebElement> buttons = pagination.findElements(By.tagName("button")); // Find next page button
                    WebElement nextPageButton = null;
                    for (WebElement button : buttons) { // Look for button with next page number or "Next" text
                        String buttonText = button.getText().trim();
                        if (buttonText.equals(String.valueOf(page + 1)) || buttonText.equalsIgnoreCase("Next")) {
                            nextPageButton = button;
                            break;
                        }
                    }
                    if (nextPageButton == null || !nextPageButton.isEnabled()) {
                        logger.warn("No more pages available after page {}", page);
                        break;
                    }
                    try {
                        nextPageButton.click(); // Click next page button
                    } catch (Exception e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextPageButton);
                    }// If regular click fails, try JavaScript click
                    new WebDriverWait(driver, Duration.ofSeconds(10)) // Wait for URL to change
                        .until(driver -> !driver.getCurrentUrl().equals(currentUrl));
                    Thread.sleep(1000);  // Wait for new content to load
                    waitForElement(By.cssSelector("[data-testid='card-headline']"));// Wait for at least one new headline to appear
                    logger.info("Successfully navigated to page {}", page + 1);
                }
            }
            logger.info("Finished collecting headlines. Total unique headlines: {}", headlinesSet.size());
        } catch (Exception e) {
            logger.error("Error while navigating pages: {}", e.getMessage());
        }
    }
    private void collectHeadlinesFromCurrentPage() {
        try {
            Thread.sleep(3000); // Wait longer for the page to load completely
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy( // Wait for headlines to be present
                By.cssSelector("[data-testid='card-headline']")));
            List<WebElement> headlineElements = driver.findElements( // Find all headline elements
                By.cssSelector("[data-testid='card-headline']"));
            int previousSize = headlinesSet.size();
            for (WebElement element : headlineElements) { // Extract and store headlines
                try {
                    String headline = element.getText().trim();
                    if (!headline.isEmpty()) {
                        headlinesSet.add(headline);
                    }
                } catch (Exception e) {
                    logger.debug("Failed to extract headline: {}", e.getMessage());
                }
            }
            int newHeadlines = headlinesSet.size() - previousSize;
            logger.info("Added {} new unique headlines from current page", newHeadlines);
        } catch (Exception e) {
            logger.error("Error collecting headlines from current page: {}", e.getMessage());
        }
    }
    private WebElement waitForElement(By locator) {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (Exception e) {
            logger.error("Element not found: {}", locator);
            throw new ScraperException("Element not found: " + locator, e);
        }
    }
    @Override
    public void close() {
        try {
            if (driver != null) {
                driver.quit();
                logger.info("WebDriver closed successfully");
            }
        } catch (Exception e) {
            logger.error("Error closing WebDriver: {}", e.getMessage());
        }
    }

    public static void main(String[] args) { //Main class for Testing
        try (BBCNewsScraper scraper = new BBCNewsScraper(10)) {
            String searchQuery = "technology";
            CustomQueue<String> headlines = scraper.scrapeBBCNews(searchQuery);
            if (headlines.isEmpty()) {
                System.out.println("No headlines found");
            } else {
                System.out.println("\nFound headlines across " + PAGES_TO_SCRAPE + " pages:");
                System.out.println("Total headlines: " + headlines.size());
                System.out.println("-----------------");
                int count = 1;
                for (String headline : headlines) {
                    System.out.printf("%d. %s%n", count++, headline);
                }
            }
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            logger.error("Application error: ", e);
        }
    }
}

class ScraperException extends RuntimeException {
    public ScraperException(String message) {
        super(message);
    }

    public ScraperException(String message, Throwable cause) {
        super(message, cause);
    }
}