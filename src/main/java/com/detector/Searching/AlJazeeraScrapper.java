package com.detector.Searching;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AlJazeeraScrapper {

    public static List<String> searchAlJazeera(String searchTerm) {
        WebDriver driver = null;
        List<String> articleTitles = new ArrayList<>();

        try {
            ChromeOptions options = new ChromeOptions();
            
            options.setPageLoadStrategy(org.openqa.selenium.PageLoadStrategy.NONE);

            driver = new ChromeDriver(options);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // Format the search URL
            String searchUrl = "https://www.aljazeera.com/search/" + searchTerm.replace(" ", "%20");
            driver.get(searchUrl);

            // Wait for and extract articles as soon as they appear
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("h3.gc__title a.u-clickable-card__link span")));

            // Extract available article titles
            List<WebElement> articles = driver.findElements(
                    By.cssSelector("h3.gc__title a.u-clickable-card__link span"));

            for (WebElement article : articles) {
                String title = article.getText().trim();
                if (!title.isEmpty() && !articleTitles.contains(title)) {
                    System.out.println(title);
                    articleTitles.add(title);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }

        return articleTitles;
    }

    public static void main(String[] args) {
        // Example usage
        List<String> articles = searchAlJazeera("PTI protest");
        for (String article : articles) {
            System.out.println(article);
        }
    }
}
