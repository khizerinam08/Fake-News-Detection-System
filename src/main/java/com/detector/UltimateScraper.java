package com.detector;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;

import com.detector.SocialMediaRetrieve.InstagramPostScraper;
import com.detector.SocialMediaRetrieve.TwitterRetrieval;
import com.detector.SocialMediaRetrieve.WebScrapingFBUpdated;
import com.detector.CustomDataStructures.*;

import java.time.Duration;
import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

public class UltimateScraper {
    private WebDriver driver;
    private WebDriverWait wait;
    private Set<String> allowedDomains;
    private List<String> extractedHeadlines;
    private static final int MAX_ARTICLES = 10;
    private static final int MAX_PAGES = 3;
    private int articlesFound = 0;
    private int pagesSearched = 0;
    
    public UltimateScraper() {
        setupDriver();
    }

    private void setupDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
            "--disable-gpu",
            "--no-sandbox"
        );
        
        Map<String, Object> prefs = new CustomHashMap<String, Object>();
        prefs.put("profile.default_content_setting_values.images", 2);
        options.setExperimentalOption("prefs", prefs);
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        allowedDomains = new CustomHashSet<>();
        allowedDomains.addAll(Arrays.asList(
            "aljazeera.com",
            "bbc.co.uk",
            "bbc.com",
            "cnn.com",
            "foxnews.com"
        ));
        extractedHeadlines = new CustomArrayList<String>();
    }

    public List<String> scrapeKeyword(String keyword) {
        try {
            performSearch(keyword);
            processPages();
        } catch (Exception e) {
            System.err.println("Error during scraping: " + e.getMessage());
            System.err.println("An error occurred: " + e.getMessage());
        }
        return extractedHeadlines;
    }

    private void performSearch(String keyword) {
        driver.get("https://www.google.com");
        WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(By.name("q")));
        searchBox.sendKeys(keyword);
        searchBox.submit();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search")));
        sleep(2000); // Allow search results to fully load
    }

    private void processPages() {
        while (pagesSearched < MAX_PAGES && articlesFound < MAX_ARTICLES) {
            processCurrentPage();
            
            if (articlesFound >= MAX_ARTICLES || !moveToNextPage()) {
                break;
            }
            pagesSearched++;
        }
    }

    private void processCurrentPage() {
        List<WebElement> searchResults = driver.findElements(
            By.cssSelector("div.g, div[jscontroller] > div.g, div[jscontroller] div[jsname]")
        );
        
        for (WebElement result : searchResults) {
            if (articlesFound >= MAX_ARTICLES) break;
            
            try {
                processSearchResult(result);
            } catch (StaleElementReferenceException e) {
            }
        }
    }

    private void processSearchResult(WebElement result) {
        try {
            List<WebElement> links = result.findElements(By.cssSelector("a[href]"));
            for (WebElement link : links) {
                String url = link.getAttribute("href");
                if (isValidArticleUrl(url)) {
                    String currentUrl = driver.getCurrentUrl();
                    processArticle(url, currentUrl);
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing search result: " + e.getMessage());
        }
    }

    private boolean isValidArticleUrl(String url) {
        return url != null && 
               !url.contains("google") && 
               allowedDomains.stream().anyMatch(url::contains);
    }

    private void processArticle(String articleUrl, String returnUrl) {
        try {
            driver.get(articleUrl);
            String headline = extractHeadline();
            
            if (headline != null && !headline.trim().isEmpty()) {
                extractedHeadlines.add(headline);
                articlesFound++;
                System.out.println("Article " + articlesFound + ": " + headline);
            }
            
            driver.navigate().to(returnUrl);
            sleep(1000);
        } catch (Exception e) {
            System.err.println("Error processing article: " + e.getMessage());
            driver.navigate().to(returnUrl);
        }
    }

    private String extractHeadline() {
        try {
            String url = driver.getCurrentUrl();
            By selector;
            
            if (url.contains("aljazeera.com")) {
                selector = By.cssSelector(".article__heading, .article__subhead");
            } else if (url.contains("bbc.co.uk")) {
                selector = By.cssSelector("h1#main-heading, h1.story-body__h1, h1");
            } else if(url.contains("bbc.com")){
                selector = By.cssSelector("h1#main-heading, h1.story-body__h1, h1");
            } else if(url.contains("cnn.com")){
                selector = By.cssSelector("h1#main-heading, h1.story-body__h1, h1");
            } else if(url.contains("foxnews.com")){
                selector = By.cssSelector("h1.headline speakable, h1.sub-headline speakable, h1");
            }
            else {
                return null;
            }
            
            return wait.until(ExpectedConditions.presenceOfElementLocated(selector)).getText();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean moveToNextPage() {
        try {
            WebElement nextButton = driver.findElement(By.id("pnnext"));
            if (nextButton.isDisplayed()) {
                nextButton.click();
                sleep(2000);
                return true;
            }
        } catch (Exception e) {
            System.out.println("No more pages available");
        }
        return false;
    }

    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String extractKeywords(String text) {
        try {
            // Clean the input text to avoid command line issues
            text = text.replaceAll("[\"'\n\r]", " ").trim();
            
            ProcessBuilder processBuilder = new ProcessBuilder(
                "python",
                "src/main/java/com/detector/algorithms/KeywordExtract.py",
                text
            );
            
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            
            // Read the output into a string
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line.trim());
                }
            }

            // Wait for process to complete and check exit code
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Python script failed with exit code: " + exitCode);
                return "";
            }

            String keywords = output.toString().trim();
            return keywords.isEmpty() ? "" : keywords;

        } catch (Exception e) {
            System.err.println("Error extracting keywords: " + e.getMessage());
            return "";
        }
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
            // Clean and normalize the input texts
            text1 = text1.replaceAll("[\"'\n\r]", " ").trim();
            text2 = text2.replaceAll("[\"'\n\r]", " ").trim();
            
            // Create process with explicit Python command
            ProcessBuilder processBuilder = new ProcessBuilder(
                "python",  // or "python3" depending on your system
                "src/main/java/com/detector/algorithms/MiniLM.py",
                text1,
                text2
            );
            
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
    
            // Read the output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Debug output: " + line); // Debug line
                if (line.startsWith("contradiction_score:")) {
                    String scoreStr = line.substring("contradiction_score:".length()).trim();
                    try {
                        double score = Double.parseDouble(scoreStr);
                        System.out.println("Parsed score: " + score); // Debug line
                        return score;
                    } catch (NumberFormatException e) {
                        System.err.println("Failed to parse score: " + scoreStr);
                    }
                }
            }
    
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Python script exited with code: " + exitCode);
            }
    
        } catch (Exception e) {
            System.err.println("Error in getContradictionScore: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    public static void main(String[] args) {
        UltimateScraper scraper = new UltimateScraper();
        WebDriver driver = scraper.driver;
        String link = "https://www.instagram.com/p/DD_nJvcNBwM/?utm_source=ig_web_copy_link&igsh=MzRlODBiNWFlZA==";
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
                String keywords = extractKeywords(postText);
                
                List<String> headlines = scraper.scrapeKeyword(keywords);
                headlines.forEach(System.out::println);
                // Apply MiniLM for contradiction/similarity
                Map<String, List<String>> headlinesMap = new CustomHashMap<>();
                headlinesMap.put("source", headlines);
                applyMiniLM(postText, headlinesMap);
            } else {
                System.out.println("Failed to extract post text.");
            }

        } 
        catch(Exception e){
            e.printStackTrace();
        }
        finally {
        scraper.close();
        }
    }
    
    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }
}
