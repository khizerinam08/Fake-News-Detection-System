package src.main;

import com.detector.SocialMediaRetrieve.InstagramPostScraper;
import com.detector.SocialMediaRetrieve.WebScrapingFBUpdated;
import com.detector.Searching.AlJazeeraScrapper;
import com.detector.Searching.BBCNewsScraper;
import com.detector.Searching.CNNNewsSearch;
import com.detector.Searching.FoxNewsSearch;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;
import java.util.Queue;

public class MainScraper {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please provide a link as an argument.");
            return;
        }

        String link = args[0];
        WebDriver driver = initializeDriver();
        String postText = "";

        try {
            if (link.contains("instagram.com")) {
                postText = scrapeInstagramPost(driver, link);
            } else if (link.contains("facebook.com")) {
                postText = scrapeFacebookPost(driver, link);
            } else if (link.contains("twitter.com")) {
                postText = scrapeTwitterPost(driver, link);
            } else {
                System.out.println("Unsupported link. Please provide a link from Instagram, Facebook, or Twitter.");
            }

            if (!postText.isEmpty()) {
                // Collect headlines from various news websites using the post text
                collectHeadlines(driver, postText);
            } else {
                System.out.println("Failed to extract post text.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private static WebDriver initializeDriver() {
        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver-win64\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-notifications");
        options.addArguments("--lang=en-US");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
        return new ChromeDriver(options);
    }

    private static String scrapeInstagramPost(WebDriver driver, String link) {
        InstagramPostScraper scraper = new InstagramPostScraper(driver);
        String postCaption = scraper.scrapePostCaption(link);
        if (postCaption != null) {
            System.out.println("Instagram Post Caption: " + postCaption);
            return postCaption;
        } else {
            System.out.println("Failed to extract Instagram post caption.");
            return "";
        }
    }

    private static String scrapeFacebookPost(WebDriver driver, String link) {
        WebScrapingFBUpdated scraper = new WebScrapingFBUpdated(driver);
        String postContent = scraper.scrapePost(link);
        if (postContent != null) {
            System.out.println("Facebook Post Content: " + postContent);
            return postContent;
        } else {
            System.out.println("Failed to extract Facebook post content.");
            return "";
        }
    }

    private static String scrapeTwitterPost(WebDriver driver, String link) {
        // Implement Twitter scraper here
        System.out.println("Twitter scraping is not implemented yet.");
        return "";
    }

    private static void collectHeadlines(WebDriver driver, String keyword) {
        // Al Jazeera
        System.out.println("Searching headlines on Al Jazeera...");
        AlJazeeraScrapper alJazeeraScrapper = new AlJazeeraScrapper(driver);
        List<String> alJazeeraHeadlines = alJazeeraScrapper.searchAlJazeera(keyword);
        printHeadlines("Al Jazeera", alJazeeraHeadlines);

        // BBC News
        System.out.println("Searching headlines on BBC News...");
        try (BBCNewsScraper bbcNewsScraper = new BBCNewsScraper(10)) {
            Queue<String> bbcHeadlines = bbcNewsScraper.scrapeBBCNews(keyword);
            printHeadlines("BBC News", bbcHeadlines);
        }

        // CNN News
        System.out.println("Searching headlines on CNN News...");
        try (CNNNewsSearch cnnNewsSearch = new CNNNewsSearch(10)) {
            List<String> cnnHeadlines = cnnNewsSearch.scrapeCNNNews(keyword);
            printHeadlines("CNN News", cnnHeadlines);
        }

        // Fox News
        System.out.println("Searching headlines on Fox News...");
        FoxNewsSearch foxNewsSearch = new FoxNewsSearch(driver);
        List<String> foxHeadlines = foxNewsSearch.searchFoxNews(keyword);
        printHeadlines("Fox News", foxHeadlines);
    }

    private static void printHeadlines(String source, List<String> headlines) {
        if (!headlines.isEmpty()) {
            System.out.println("Matching headlines on " + source + ":");
            for (String headline : headlines) {
                System.out.println("- " + headline);
            }
        } else {
            System.out.println("No matching headlines found on " + source);
        }
    }

    private static void printHeadlines(String source, Queue<String> headlines) {
        if (!headlines.isEmpty()) {
            System.out.println("Matching headlines on " + source + ":");
            for (String headline : headlines) {
                System.out.println("- " + headline);
            }
        } else {
            System.out.println("No matching headlines found on " + source);
        }
    }
}