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

public class AlJazeeraScrapper {
    public static void main(String[] args) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");

        // Set page load strategy too NONE to avoid waiting for full page load
        options.setPageLoadStrategy(org.openqa.selenium.PageLoadStrategy.NONE);

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            // Navigate to the search page
            driver.get("https://www.aljazeera.com/search/Donald%20Trump");

            // Wait for and extract articles as soon as they appear
            List<String> articleTitles = new ArrayList<>();

            // Wait for the first batch of articles
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("h3.gc__title a.u-clickable-card__link span")));

            // Extract available article titles
            List<WebElement> articles = driver.findElements(
                    By.cssSelector("h3.gc__title a.u-clickable-card__link span"));

            for (WebElement article : articles) {
                String title = article.getText().trim();
                if (!title.isEmpty() && !articleTitles.contains(title)) {
                    articleTitles.add(title);
                    System.out.println("Article " + articleTitles.size() + ": " + title);
                }
            }

            // Print final results
            System.out.println("\nFinal Results:");
            System.out.println("Total Articles Found: " + articleTitles.size());
            for (int i = 0; i < articleTitles.size(); i++) {
                System.out.println((i + 1) + ". " + articleTitles.get(i));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
