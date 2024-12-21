package org.example;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SimpleWebScraper {
    public static void main(String[] args) {
        // Set up ChromeOptions (optional for headless mode)
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");

        // Initialize WebDriver
        WebDriver driver = new ChromeDriver(options);

        try {
            String url = "https://www.aljazeera.com/search/";
            String keyword = "Donald Trump";

            String searchUrl = url + keyword.replace(" ", "%20");
            driver.get(searchUrl);

            // Initialize WebDriverWait with a timeout of 10 seconds
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

            // List to store the extracted article titles
            List<String> articleTitles = new ArrayList<>();

            // Continue until we have at least 20 articles
            while (articleTitles.size() < 20) {
                // Wait for the articles to load
                List<WebElement> articles = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("h3.gc__title")));

                // Extract titles from the visible articles
                for (WebElement article : articles) {
                    if (articleTitles.size() >= 20) break;

                    // Find the anchor tag inside the h3 element
                    WebElement link = article.findElement(By.cssSelector("a.u-clickable-card__link"));
                    WebElement spanElement = link.findElement(By.tagName("span"));

                    // Get the text inside the <span> element
                    String spanText = spanElement.getText();

                    // Add to the list if not already added
                    if (!articleTitles.contains(spanText)) {
                        articleTitles.add(spanText);
                        System.out.println("Extracted Article: " + spanText);
                    }
                }

                // Check if the "See More" button exists and is clickable
                try {
                    WebElement seeMoreButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.show-more-button big-margin")));
                    seeMoreButton.click(); // Click the "See More" button
                } catch (Exception e) {
                    System.out.println("No more articles to load or 'See More' button not clickable.");
                    break; // Exit loop if button is not found or clickable
                }

                // Add a short wait to allow articles to load after clicking "See More"
                Thread.sleep(2000);
            }

            // Print all extracted article titles
            System.out.println("Total Articles Extracted: " + articleTitles.size());
            articleTitles.forEach(title -> System.out.println(title));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the browser
            driver.quit();
        }
    }
}
