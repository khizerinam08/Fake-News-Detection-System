package com.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

public class InstagramPostScraper {
    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();

        try {
            //Navigating to post 
            String postUrl = "https://www.instagram.com/p/DDY8vg2iju9/?img_index=13&igsh=dDdzbTM5d3dtdG9q";
            driver.get(postUrl);

            //waiting for the content to load
            Thread.sleep(5000);

            //locatin the meta tag with property="og:description"
            WebElement metaTag = driver.findElement(By.xpath("//meta[@property='og:description']"));

            //extracting the content attribute from the meta tag
            String postDescription = metaTag.getAttribute("content");

            //printing the post description
            System.out.println("Post Description: " + postDescription);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
