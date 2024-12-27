package com.detector;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.util.*;
import java.nio.file.*;

public class UltimateScraper {
    private WebDriver driver;
    private WebDriverWait wait;
    private Set<String> allowedDomains;
    private List<String> extractedHeadlines;
    private static final int MAX_ARTICLES = 10;
    private static final int MAX_PAGES = 3;
    private int articlesFound = 0;
    private int pagesSearched = 0;
    
    public UltimateScraper() {
        setupDriver();
    }

    private void setupDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
            "--disable-gpu",
            "--no-sandbox"
        );
        
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.images", 2);
        options.setExperimentalOption("prefs", prefs);
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        allowedDomains = new HashSet<>(Arrays.asList(
            "aljazeera.com",
            "bbc.co.uk",
            "bbc.com",
            "cnn.com",
            "foxnews.com"
        ));
        extractedHeadlines = new ArrayList<>();
    }

    public List<String> scrapeKeyword(String keyword) {
        try {
            performSearch(keyword);
            processPages();
        } catch (Exception e) {
            System.err.println("Error during scraping: " + e.getMessage());
            e.printStackTrace();
        }
        return extractedHeadlines;
    }

    private void performSearch(String keyword) {
        driver.get("https://www.google.com");
        WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(By.name("q")));
        searchBox.sendKeys(keyword);
        searchBox.submit();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search")));
        sleep(2000); // Allow search results to fully load
    }

    private void processPages() {
        while (pagesSearched < MAX_PAGES && articlesFound < MAX_ARTICLES) {
            processCurrentPage();
            
            if (articlesFound >= MAX_ARTICLES || !moveToNextPage()) {
                break;
            }
            pagesSearched++;
        }
    }

    private void processCurrentPage() {
        List<WebElement> searchResults = driver.findElements(
            By.cssSelector("div.g, div[jscontroller] > div.g, div[jscontroller] div[jsname]")
        );
        
        for (WebElement result : searchResults) {
            if (articlesFound >= MAX_ARTICLES) break;
            
            try {
                processSearchResult(result);
            } catch (StaleElementReferenceException e) {
            }
        }
    }

    private void processSearchResult(WebElement result) {
        try {
            List<WebElement> links = result.findElements(By.cssSelector("a[href]"));
            for (WebElement link : links) {
                String url = link.getAttribute("href");
                if (isValidArticleUrl(url)) {
                    String currentUrl = driver.getCurrentUrl();
                    processArticle(url, currentUrl);
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing search result: " + e.getMessage());
        }
    }

    private boolean isValidArticleUrl(String url) {
        return url != null && 
               !url.contains("google") && 
               allowedDomains.stream().anyMatch(url::contains);
    }

    private void processArticle(String articleUrl, String returnUrl) {
        try {
            driver.get(articleUrl);
            String headline = extractHeadline();
            
            if (headline != null && !headline.trim().isEmpty()) {
                extractedHeadlines.add(headline);
                articlesFound++;
                System.out.println("Article " + articlesFound + ": " + headline);
            }
            
            driver.navigate().to(returnUrl);
            sleep(1000);
        } catch (Exception e) {
            System.err.println("Error processing article: " + e.getMessage());
            driver.navigate().to(returnUrl);
        }
    }

    private String extractHeadline() {
        try {
            String url = driver.getCurrentUrl();
            By selector;
            
            if (url.contains("aljazeera.com")) {
                selector = By.cssSelector(".article__heading, .article__subhead");
            } else if (url.contains("bbc.co.uk")) {
                selector = By.cssSelector("h1#main-heading, h1.story-body__h1, h1");
            } else if(url.contains("bbc.com")){
                selector = By.cssSelector("h1#main-heading, h1.story-body__h1, h1");
            } else if(url.contains("cnn.com")){
                selector = By.cssSelector("h1#main-heading, h1.story-body__h1, h1");
            } else if(url.contains("foxnews.com")){
                selector = By.cssSelector("h1.headline speakable, h1.sub-headline speakable, h1");
            }
            else {
                return null;
            }
            
            return wait.until(ExpectedConditions.presenceOfElementLocated(selector)).getText();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean moveToNextPage() {
        try {
            WebElement nextButton = driver.findElement(By.id("pnnext"));
            if (nextButton.isDisplayed()) {
                nextButton.click();
                sleep(2000);
                return true;
            }
        } catch (Exception e) {
            System.out.println("No more pages available");
        }
        return false;
    }

    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        UltimateScraper scraper = new UltimateScraper();
        try {
            List<String> headlines = scraper.scrapeKeyword("azerbaijan airlines kazakhstan al jazeera");
            System.out.println("\nFound Headlines:");
            headlines.forEach(System.out::println);
        } finally {
            scraper.close();
        }
    }
    
    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }
}
