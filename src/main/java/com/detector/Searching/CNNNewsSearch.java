package com.detector.Searching;

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
import java.util.ArrayList;
import java.util.List;

public class CNNNewsSearch implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(CNNNewsSearch.class);
    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final int MAX_PAGES = 3; // Limit to first 3 pages

    public CNNNewsSearch(int timeoutSeconds) {
        this.driver = initializeDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        logger.info("CNNNewsSearch initialized with timeout of {} seconds", timeoutSeconds);
    }

    private WebDriver initializeDriver() {
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--start-maximized");
            options.addArguments("--disable-notifications");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--no-sandbox");
            options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
            return new ChromeDriver(options);
        } catch (Exception e) {
            logger.error("Failed to initialize WebDriver: {}", e.getMessage());
            throw new ScraperException("Failed to initialize WebDriver", e);
        }
    }

    public List<String> scrapeCNNNews(String searchQuery) {
        try {
            navigateToHomepage();
            if (!performSearch(searchQuery)) {
                return new ArrayList<>();
            }
            return getAllHeadlinesFromMultiplePages();
        } catch (Exception e) {
            logger.error("Error during scraping: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private void navigateToHomepage() {
        try {
            driver.get("https://edition.cnn.com/");
            handleConsentDialog();
            logger.info("Navigated to CNN News homepage");
        } catch (Exception e) {
            logger.error("Failed to navigate to homepage: {}", e.getMessage());
            throw new ScraperException("Failed to navigate to homepage", e);
        }
    }

    private void handleConsentDialog() {
        try {
            WebElement consentButton = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("[data-testid='agree-button']")));
            consentButton.click();
            logger.info("Handled consent dialog");
        } catch (Exception e) {
            logger.debug("No consent dialog found or already accepted");
        }
    }

    private boolean performSearch(String query) {
        try {
            WebElement searchIcon = waitForElement(By.cssSelector("[aria-label=\"Search Icon\"]"));
            searchIcon.click();
            WebElement searchBox = waitForElement(By.cssSelector("[class='search-bar__input']"));
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

    private List<String> getAllHeadlinesFromMultiplePages() {
        List<String> allHeadlines = new ArrayList<>();
        int currentPage = 1;

        try {
            while (currentPage <= MAX_PAGES) {
                // Wait for headlines to load
                Thread.sleep(2000); // Add a small delay for page load
                
                // Fetch headlines from the current page
                List<WebElement> headlineElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                        By.cssSelector("[class='container__headline-text']")));
                
                for (WebElement element : headlineElements) {
                    allHeadlines.add(element.getText());
                }
                logger.info("Retrieved {} headlines from page {}", headlineElements.size(), currentPage);

                if (currentPage >= MAX_PAGES) {
                    break;
                }

                // Updated selector for the next page button
                WebElement nextPageButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("div.pagination-arrow.pagination-arrow-right.search__pagination-link.text-active")));
                
                if (nextPageButton.isDisplayed() && nextPageButton.isEnabled()) {
                    nextPageButton.click();
                    currentPage++;
                    // Wait for the new page to load
                    Thread.sleep(2000); // Add a small delay after clicking
                } else {
                    logger.info("Next page button not clickable on page {}", currentPage);
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Error during pagination: {}", e.getMessage());
        }

        return allHeadlines;
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
        try (CNNNewsSearch scraper = new CNNNewsSearch(10)) {
            String searchQuery = "technology";
            List<String> headlines = scraper.scrapeCNNNews(searchQuery);

            if (headlines.isEmpty()) {
                System.out.println("No headlines found.");
            } else {
                System.out.println("Found Headlines:");
                for (String headline : headlines) {
                    System.out.println("- " + headline);
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