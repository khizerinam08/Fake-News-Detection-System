package com.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

public class InstagramPostScraper {
    
    // WebDriver instance variable
    private WebDriver driver;

    // Constructor to initialize the WebDriver
    public InstagramPostScraper() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
    }

    // Method to scrape post description based on the URL
    public String scrapePostDescription(String postUrl) {
        try {
            // Navigating to post
            driver.get(postUrl);

            // Waiting for the content to load
            Thread.sleep(5000);

            // Locating the meta tag with property="og:description"
            WebElement metaTag = driver.findElement(By.xpath("//meta[@property='og:description']"));

            // Extracting the content attribute from the meta tag
            String postDescription = metaTag.getAttribute("content");

            // Returning the post description
            return postDescription;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method to close the driver
    public void closeDriver() {
        driver.quit();
    }

    public static void main(String[] args) {
        InstagramPostScraper scraper = new InstagramPostScraper();

        // URL of the Instagram post
        String postUrl = "https://www.instagram.com/p/DDY8vg2iju9/?img_index=13&igsh=dDdzbTM5d3dtdG9q";

        // Scraping the post description
        String postDescription = scraper.scrapePostDescription(postUrl);

        // Printing the post description
        if (postDescription != null) {
            System.out.println("Post Description: " + postDescription);
        } else {
            System.out.println("Failed to extract post description.");
        }

        // Closing the driver
        scraper.closeDriver();
    }
}
