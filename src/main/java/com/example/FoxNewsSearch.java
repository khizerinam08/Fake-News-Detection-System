package com.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class FoxNewsSearch {

    private WebDriver driver;

    public FoxNewsSearch(WebDriver driver) {
        this.driver = driver;
    }

    public void searchFoxNews(String searchString) {
        try {
            // Navigate to Fox News
            driver.get("https://www.foxnews.com");

            // Build the search query from keywords
            String searchQuery = String.join(" ", searchString);

            // Wait until the search toggle is clickable and click it to reveal the search box
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            WebElement searchToggle = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".js-focus-search")));
            searchToggle.click(); // Click to reveal the search box

            // Wait until the search box is present
            WebElement searchBox = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input.resp_site_search")));

            // Clear the search box, then enter the search query
            searchBox.clear();
            searchBox.sendKeys(searchQuery);

            // Wait for the search button to be clickable and click it
            WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input.resp_site_submit")));
            searchButton.click();

            // Wait for the results to load
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".article .title")));

            // Extract and print article titles
            List<WebElement> articles = driver.findElements(By.cssSelector(".article .title"));
            for (WebElement article : articles) {
                String articleTitle = article.getText();
                System.out.println("Article Title: " + articleTitle);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Set the path for your WebDriver executable (e.g., ChromeDriver)
        System.setProperty("webdriver.chrome.driver", "drivers/chromedriver.exe");

        // Initialize WebDriver (Chrome in this case)
        WebDriver driver = new ChromeDriver();

        try {
            // Create an instance of FoxNewsSearch
            FoxNewsSearch foxNewsSearch = new FoxNewsSearch(driver);

            // Call searchFoxNews with a sample list of keywords
            foxNewsSearch.searchFoxNews("CEO UnitedHealthcare was fatally shot what police said appears be \"premeditated, preplanned targeted");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the driver
            driver.quit();
        }
    }
}
