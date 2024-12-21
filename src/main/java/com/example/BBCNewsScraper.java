package com.example;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.util.Optional;

public class BBCNewsScraper implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(BBCNewsScraper.class);
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final int timeoutSeconds;

    public BBCNewsScraper(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        this.driver = initializeDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        logger.info("BBCNewsScraper initialized with timeout of {} seconds", timeoutSeconds);
    }

    private WebDriver initializeDriver() {
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--start-maximized");
            options.addArguments("--disable-notifications");
            options.addArguments("--remote-allow-origins=*");
            // Add arguments to reduce warnings
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--no-sandbox");
            options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
            return new ChromeDriver(options);
        } catch (Exception e) {
            logger.error("Failed to initialize WebDriver: {}", e.getMessage());
            throw new ScraperException("Failed to initialize WebDriver", e);
        }
    }

    public Optional<String> scrapeBBCNews(String searchQuery) {
        try {
            navigateToHomepage();
            if (!performSearch(searchQuery)) {
                return Optional.empty();
            }
            return getTopArticleHeadline();
        } catch (Exception e) {
            logger.error("Error during scraping: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private void navigateToHomepage() {
        try {
            driver.get("https://www.bbc.co.uk/news");
            // Wait for page load and handle any consent dialogs if they appear
            handleConsentDialog();
            logger.info("Navigated to BBC News homepage");
        } catch (Exception e) {
            logger.error("Failed to navigate to homepage: {}", e.getMessage());
            throw new ScraperException("Failed to navigate to homepage", e);
        }
    }

    private void handleConsentDialog() {
        try {
            // Wait for a short time to see if consent dialog appears
            WebElement consentButton = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[data-testid='agree-button']")));
            consentButton.click();
            logger.info("Handled consent dialog");
        } catch (Exception e) {
            // If no consent dialog appears, continue normally
            logger.debug("No consent dialog found or already accepted");
        }
    }

    private boolean performSearch(String query) {
        try {
            // Updated selector for the search button
            WebElement searchIcon = waitForElement(By.cssSelector("button[role='button'][aria-label='Search BBC']"));
            searchIcon.click();
    
            // Wait for the search input field
            WebElement searchBox = waitForElement(By.cssSelector("[data-testid='search-input-field']"));
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
    private Optional<String> getTopArticleHeadline() {
        try {
            // Wait for and locate the first headline
            WebElement headlineElement = waitForElement(By.cssSelector("h2[data-testid='card-headline']"));
            String headline = headlineElement.getText();
            logger.info("Found headline: {}", headline);
            return Optional.of(headline);
        } catch (Exception e) {
            logger.error("Error getting article headline: {}", e.getMessage());
            return Optional.empty();
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
    public static void main(String[] args) {
        try (BBCNewsScraper scraper = new BBCNewsScraper(10)) {
            String searchQuery = "technology";
            Optional<String> headline = scraper.scrapeBBCNews(searchQuery);
            
            headline.ifPresentOrElse(
                h -> System.out.println("Found headline: " + h),
                () -> System.out.println("Failed to retrieve headline")
            );
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

