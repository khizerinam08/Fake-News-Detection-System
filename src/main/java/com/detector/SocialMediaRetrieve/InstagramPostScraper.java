// References For Selenium Method Usage: https://www.browserstack.com/guide/run-selenium-tests-using-selenium-chromedriver

package com.detector.SocialMediaRetrieve;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

public class InstagramPostScraper {

    private WebDriver driver; // WebDriver instance variable
    public InstagramPostScraper() {  // Constructor to initialize the WebDriver
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
    }
    public String scrapePostCaption(String postUrl) { // Method to scrape post caption based on the URL
        try {
            driver.get(postUrl); // Navigating to post
            Thread.sleep(5000); // Waiting for the content to load
             // Locating the meta tag with property="og:description"
            WebElement metaTag = driver.findElement(By.xpath("//meta[@property='og:description']"));            
            // Extracting the content attribute from the meta tag
            String postDescription = metaTag.getAttribute("content");
            int startIndex = postDescription.indexOf("\"") + 1; // Finding the indices of the first and last double quotes
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
    public void closeDriver() {
        driver.quit();
    }
    public static void main(String[] args) { // Main method to test the InstagramPostScraper
        InstagramPostScraper scraper = new InstagramPostScraper();
        String postUrl = "https://www.instagram.com/p/DD3tDOXxSYK/?igsh=bjZuNTl4NXo5NW13";
        String postCaption = scraper.scrapePostCaption(postUrl);
        if (postCaption != null) {
            System.out.println(postCaption);
        } else {
            System.out.println("Failed to extract post caption.");
        }
        scraper.closeDriver();
    }
}
