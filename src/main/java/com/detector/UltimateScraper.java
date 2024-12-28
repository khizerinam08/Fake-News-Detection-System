package com.detector.utility;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;

import com.detector.SocialMediaRetrieve.InstagramPostScraper;
import com.detector.SocialMediaRetrieve.TwitterRetrieval;
import com.detector.SocialMediaRetrieve.WebScrapingFBUpdated;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CountDownLatch;

import java.time.Duration;
import java.util.*;
import java.nio.file.*;
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
    private static volatile String responseString;
    private static final Object lock = new Object();

    
    

    public UltimateScraper() {
        setupDriver();
    }

    private void setupDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
            "--disable-gpu",
            "--no-sandbox"
        );
        
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.images", 2);
        options.setExperimentalOption("prefs", prefs);
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        allowedDomains = new HashSet<>(Arrays.asList(
            "aljazeera.com",
            "bbc.co.uk",
            "bbc.com",
            "cnn.com",
            "foxnews.com"
        ));
        extractedHeadlines = new ArrayList<>();
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

    private static String[] applyMiniLM(String postText, Map<String, List<String>> headlinesMap) {
        double minScore = Double.MAX_VALUE;  // Initialize with a very high value
        String bestHeadline = "";  // To store the headline with the least score
        String bestConfidence = "";  // To store the corresponding confidence level
        double bestScore = 0.0;  // To store the corresponding score
    
        for (Map.Entry<String, List<String>> entry : headlinesMap.entrySet()) {
            String source = entry.getKey();
            List<String> headlines = entry.getValue();
    
            System.out.println("Contradiction/Similarity with headlines from " + source + ":");
            for (String headline : headlines) {
                String[] result = getContradictionScore(postText, headline);
    
                // Parse the score and confidence from the result
                if (result != null && result[1] != null && result[2] != null) {
                    try {
                        double score = Double.parseDouble(result[1]);  // Parse the score
                        String confidence = result[2];  // Get the confidence label
    
                        // If the current score is less than the previous minimum, update
                        if (score < minScore) {
                            minScore = score;
                            bestHeadline = headline;  // Store the corresponding headline
                            bestConfidence = confidence;  // Store the corresponding confidence
                            bestScore = score;  // Store the corresponding score
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid score format: " + result[1]);
                    }
                }
            }
        }
    
        // Return an array containing the headline, confidence, and score
        return new String[] { bestHeadline, bestConfidence, String.valueOf(bestScore) };
    }
    
    private static String[] getContradictionScore(String text1, String text2) {
        String[] result = new String[3];  // Array to hold label, score, and text
    
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
                System.out.println("Debug output: " + line);  // Debug line
                
                // Parse the output to extract the relevant information
                if (line.contains(":")) {
                    String[] parts = line.split(": ");
                    if (parts.length == 2) {
                        String label = parts[0].trim();
                        String score = parts[1].trim();
                        result[0] = label;  // Store the label (e.g., "ENTAILMENT", "CONTRADICTION")
                        result[1] = score;  // Store the score
                        result[2] = label;  // Assuming the label represents the confidence level (e.g., "ENTAILMENT", "CONTRADICTION")
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
    
        return result;
    }
    
    
    

    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(StringController.class, args); // Start Spring Boot Application
    
        UltimateScraper scraper = new UltimateScraper();
        WebDriver driver = scraper.driver;
        String postText;
        String receivedInput = "";
    
        try {
            while(receivedInput == "") {
                Thread.sleep(1000);
                receivedInput = StringController.getReceivedString();
            }
    
            if (receivedInput.isEmpty()) {
                System.out.println("No valid input received from the phone.");
                return; // Exit if no input received
            }
    
            // Process the link based on the received input
            if (receivedInput.contains("instagram.com")) {
                postText = scrapeInstagramPost(driver, receivedInput);
            } else if (receivedInput.contains("facebook.com")) {
                postText = scrapeFacebookPost(driver, receivedInput);
            } else if (receivedInput.contains("x.com")) {
                postText = scrapeTwitterPost(receivedInput);
            } else {
                System.out.println("Unsupported link. Please provide a link from Instagram, Facebook, or Twitter.");
                return; // Exit if link is unsupported
            }
    
            if (!postText.isEmpty()) {
                // Extract keywords from the post text
                String keywords = extractKeywords(postText);
    
                // Scrape news headlines based on keywords
                List<String> headlines = scraper.scrapeKeyword(keywords);
                headlines.forEach(System.out::println);
    
                // Apply MiniLM for contradiction/similarity
                Map<String, List<String>> headlinesMap = new HashMap<>();
                headlinesMap.put("source", headlines);
                String[] result = applyMiniLM(postText, headlinesMap); // Apply MiniLM model
                String bestHeadline = result[0];
                String bestConfidence = result[1];
                String bestScore = result[2];
    
                // Combine the three strings into a single response string
                String combinedResponse = "Best Headline: " + bestHeadline + "\n"
                                        + "Confidence: " + bestConfidence + "\n"
                                        + "Score: " + bestScore;
    
                // Set the combined response string to be sent back
                StringController.setResponseString(combinedResponse);
    
                System.out.println(combinedResponse); // Print the response for debugging
            } else {
                System.out.println("Failed to extract post text.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scraper.close(); // Ensure WebDriver is closed properly
        }
    }
    
    public static String getReceivedString() {
        return StringController.getReceivedString();
    }

    public static String getResponseString() {
        synchronized (lock) {
            return responseString;
        }
    }

    public static void setResponseString(String response) {
        synchronized (lock) {
            if (response != null) {
                responseString = response;
                System.out.println("Response string updated: " + response);
            } else {
                System.err.println("Warning: Attempted to set null response");
            }
        }
    }
    
    
    

    

}
@SpringBootApplication
@RestController
@RequestMapping("/api")
class StringController {

    // Static variable to store the input received from the Android client
    private static String receivedString = "";
    private static String responseString = "";

    @PostMapping("/send-receive")
    public Map<String, String> sendAndReceive(@RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();
        
        if (request != null && request.containsKey("input")) {
            receivedString = request.get("input");
            System.out.println("Received from Android: " + receivedString);

            // Simulating processing logic
            if(responseString != "") {
                response.put("response", responseString);
            }
        } else {
            System.out.println("No input received.");
        }
        
        return response;
    }

    public static String getReceivedString() {
        return receivedString;
    }

    // In StringController class, add this method to update responseString
    public static void setResponseString(String response) {
        responseString = response;
    }
}
