package com.detector;

import com.detector.SocialMediaRetrieve.InstagramPostScraper;
import com.detector.SocialMediaRetrieve.TwitterRetrieval;
import com.detector.SocialMediaRetrieve.WebScrapingFBUpdated;
import com.detector.Searching.AlJazeeraScrapper;
import com.detector.Searching.BBCNewsScraper;
import com.detector.Searching.CNNNewsSearch;
import com.detector.Searching.FoxNewsSearch;
import com.detector.utility.PostToSearchConvert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.*;

public class MainScraper {
    public static void main(String[] args) {
        //if (args.length == 0) {
           //System.out.println("Please provide a link as an argument.");
            //return;
       // }

        String link = "https://www.instagram.com/p/DD3tDOXxSYK/?igsh=bjZuNTl4NXo5NW13";
        WebDriver driver = initializeDriver();
        String postText = "";

        try {
            if (link.contains("instagram.com")) {
                postText = scrapeInstagramPost(driver, link);
            } else if (link.contains("facebook.com")) {
                postText = scrapeFacebookPost(driver, link);
            } else if (link.contains("x.com")) {
                postText = scrapeTwitterPost(link);
            } else {
                System.out.println("Unsupported link. Please provide a link from Instagram, Facebook, or Twitter.");
            }

            if (!postText.isEmpty()) {
                // Convert post text to search-friendly string
                PostToSearchConvert converter = new PostToSearchConvert();
                String searchQuery = converter.convert(postText);

                // Collect headlines from various news websites using the search query
                Map<String, List<String>> headlinesMap = collectHeadlines(driver, searchQuery);

                // Apply MiniLM for contradiction/similarity
                applyMiniLM(postText, headlinesMap);
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
        System.setProperty("webdriver.chrome.driver", "./drivers/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-notifications");
        options.addArguments("--lang=en-US");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
        return new ChromeDriver(options);
    }

    private static String scrapeInstagramPost(WebDriver driver, String link) {
        InstagramPostScraper scraper = new InstagramPostScraper();
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
        String postContent = WebScrapingFBUpdated.scrapePost(driver);
        if (postContent != null) {
            System.out.println("Facebook Post Content: " + postContent);
            return postContent;
        } else {
            System.out.println("Failed to extract Facebook post content.");
            return "";
        }
    }

    private static String scrapeTwitterPost(String link) {
        String bearerToken = "AAAAAAAAAAAAAAAAAAAAAO6fxAEAAAAACpMKeL9AQM4dGiMPyNRxxgHCfHw%3DhbKyHC1W7QhXQNVAI1tqj4gNUcNdxG8Ae7VaF3iKHWhtxbrnkY"; 
        String tweetId = TwitterRetrieval.extractTweetIdFromUrl(link);
        if (tweetId == null) {
            System.out.println("Invalid Twitter URL. Please provide a valid URL.");
            return "";
        }

        try {
            String tweetText = TwitterRetrieval.getTweets(tweetId, bearerToken);
            if (tweetText != null && !tweetText.isEmpty()) {
                System.out.println("Tweet Text: " + tweetText);
                return tweetText;
            } else {
                System.out.println("Could not fetch the tweet.");
                return "";
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static Map<String, List<String>> collectHeadlines(WebDriver driver, String keyword) {
        Map<String, List<String>> headlinesMap = new HashMap<>();

        // Al Jazeera
        System.out.println("Searching headlines on Al Jazeera...");
       System.out.println("Searching headlines on Al Jazeera...");
        List<String> alJazeeraHeadlines = AlJazeeraScrapper.searchAlJazeera(keyword);
        headlinesMap.put("Al Jazeera", alJazeeraHeadlines);

        // BBC News
        System.out.println("Searching headlines on BBC News...");
        try (BBCNewsScraper bbcNewsScraper = new BBCNewsScraper(10)) {
            Queue<String> bbcHeadlines = bbcNewsScraper.scrapeBBCNews(keyword);
            headlinesMap.put("BBC News", new ArrayList<>(bbcHeadlines));
        }

        // CNN News
        System.out.println("Searching headlines on CNN News...");
        try (CNNNewsSearch cnnNewsSearch = new CNNNewsSearch(10)) {
            List<String> cnnHeadlines = cnnNewsSearch.scrapeCNNNews(keyword);
            headlinesMap.put("CNN News", cnnHeadlines);
        }

        // Fox News
        System.out.println("Searching headlines on Fox News...");
        FoxNewsSearch foxNewsSearch = new FoxNewsSearch(driver);
        List<String> foxHeadlines = foxNewsSearch.searchFoxNews(keyword);
        headlinesMap.put("Fox News", foxHeadlines);

        return headlinesMap;
    }

    private static void applyMiniLM(String postText, Map<String, List<String>> headlinesMap) {
        for (Map.Entry<String, List<String>> entry : headlinesMap.entrySet()) {
            String source = entry.getKey();
            List<String> headlines = entry.getValue();

            System.out.println("Contradiction/Similarity with headlines from " + source + ":");
            for (String headline : headlines) {
                double contradictionScore = getContradictionScore(postText, headline);
                System.out.printf("Contradiction score with \"%s\": %.4f%n", headline, contradictionScore);
            }
        }
    }

    private static double getContradictionScore(String text1, String text2) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python", "src/main/java/com/detector/algorithms/MiniLM.py", text1, text2);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            process.waitFor();
            reader.close();

            String result = output.toString();
            if (result.contains("contradiction_score")) {
                int startIndex = result.indexOf("contradiction_score") + 20;
                int endIndex = result.indexOf(",", startIndex);
                return Double.parseDouble(result.substring(startIndex, endIndex));
            } else {
                System.out.println("Error in MiniLM script output: " + result);
                return 0.0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
}
