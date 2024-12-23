package com.detector.SocialMediaRetrieve;

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

    // Method to scrape post caption based on the URL
    public String scrapePostCaption(String postUrl) {
        try {
            // Navigating to post
            driver.get(postUrl);

            // Waiting for the content to load
            Thread.sleep(5000);

            // Locating the meta tag with property="og:description"
            WebElement metaTag = driver.findElement(By.xpath("//meta[@property='og:description']"));

            // Extracting the content attribute from the meta tag
            String postDescription = metaTag.getAttribute("content");

            // Finding the indices of the first and last double quotes
            int startIndex = postDescription.indexOf("\"") + 1;
            int endIndex = postDescription.lastIndexOf("\"");

            // Extracting the caption text between the quotes
            if (startIndex > 0 && endIndex > startIndex) {
                return postDescription.substring(startIndex, endIndex);
            } else {
                return "No caption found.";
            }

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
        String postUrl = "https://www.instagram.com/p/DD3tDOXxSYK/?igsh=bjZuNTl4NXo5NW13";

        // Scraping the post caption
        String postCaption = scraper.scrapePostCaption(postUrl);

        // Printing the post caption
        if (postCaption != null) {
            System.out.println(postCaption);
        } else {
            System.out.println("Failed to extract post caption.");
        }

        // Closing the driver
        scraper.closeDriver();
    }
}
