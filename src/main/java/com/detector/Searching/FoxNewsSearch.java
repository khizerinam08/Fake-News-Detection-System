package com.detector.Searching;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import com.detector.CustomDataStructures.CustomArrayList;
public class FoxNewsSearch {

    private WebDriver driver;

    public FoxNewsSearch(WebDriver driver) {
        this.driver = driver;
    }
    public List<String> searchFoxNews(String searchString) {
        List<String> articleTitles = new CustomArrayList<>();
        try {
            driver.get("https://www.foxnews.com"); // Navigate to Fox News homepage
            String searchQuery = String.join(" ", searchString); // Build the search query from keywords
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30)); // Wait until the search toggle is clickable and click it to reveal the search box
            WebElement searchToggle = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".js-focus-search")));
            searchToggle.click(); // Click to reveal the search box
            WebElement searchBox = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input.resp_site_search")));
            // Wait until the search box is present

            searchBox.clear(); // Clear the search box, then enter the search query
            searchBox.sendKeys(searchQuery);
            // Wait for the search button to be clickable and click it
            WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input.resp_site_submit")));
            searchButton.click();

            // Wait for the results to load using the exact HTML structure
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("article.article div.info")));

            // Find all articles and retrieve their titles
            List<WebElement> articles = driver.findElements(
                By.cssSelector("article.article div.info header.info-header h2.title")
            );

                if (!articles.isEmpty()) {
                    System.out.println("Article Titles:");
                    for (WebElement article : articles) {
                        String title = article.getText();
                        articleTitles.add(title);
                        System.out.println(title);
                    }
                } else {
                    System.out.println("No articles found.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        return articleTitles;
    }
    public static void main(String[] args) { // Example usage
        // Initialize WebDriver (Chrome in this case)
        WebDriver driver = new ChromeDriver();
        try {
            // Create an instance of FoxNewsSearch
            FoxNewsSearch foxNewsSearch = new FoxNewsSearch(driver);
            // Call searchFoxNews with a sample list of keywords
            foxNewsSearch.searchFoxNews("donald trump");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the driver
            driver.quit();
        }
    }
}

