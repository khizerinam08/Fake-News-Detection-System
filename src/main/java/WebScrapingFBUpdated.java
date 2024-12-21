import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class WebScrapingFBUpdated {
    private static final int WAIT_TIMEOUT = 30;
    public static void main(String[] args) {
        WebDriver driver = null;
        try {
            driver = initializeDriver();
            scrapePost(driver);
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
    private static WebDriver initializeDriver() {
        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver-win64\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions(); // Create Chrome options
        options.addArguments("--start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-notifications");
        options.addArguments("--lang=en-US");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
        WebDriver driver = new ChromeDriver(options);
        Map<String, Object> parameters = new HashMap<>(); // Set up Chrome DevTools Protocol parameters
        parameters.put("source", "Object.defineProperty(navigator, 'webdriver', { get: () => undefined });");
        ((ChromeDriver) driver).executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", parameters);
        return driver;
    }
    private static void scrapePost(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT));
        try {
            System.out.println("Navigating to post...");
            driver.get("https://www.facebook.com/share/p/14eHng1xDF/?mibextid=oFDknk");
            Thread.sleep(3000); // Wait for page to load
            // Trying different selectors to find the post content
            String[] contentSelectors = {
                // General content selectors
                "div[role='main'] div[dir='auto']",
                "div[data-pagelet='FeedUnit_0'] div[dir='auto']",
                // Class-based selectors
                "div.x1iorvi4 div.x193iq5w",
                "div.xdj266r",
                // Specific content area selectors
                "div[data-ad-comet-preview='message']",
                "div.x11i5rnm.xat24cr div[dir='auto']"
            };
            boolean contentFound = false;
            for (String selector : contentSelectors) {
                try {
                    System.out.println("Trying selector: " + selector);
                    WebElement contentElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));// Wait for element with current selector
                    String postText = contentElement.getText(); // Get text content
                    if (postText != null && !postText.trim().isEmpty())  // Verify we have actual content 
                    {
                        System.out.println("\nFound post content with selector: " + selector);
                        System.out.println("Content:\n" + postText);
                        contentFound = true;
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("Selector failed: " + selector);
                }
            }
            if (!contentFound) {
                JavascriptExecutor js = (JavascriptExecutor) driver; // Try using JavaScript as a fallback
                String jsScript = "return Array.from(document.querySelectorAll('div[dir=\"auto\"]')).map(el => el.textContent).join('\\n');";
                String content = (String) js.executeScript(jsScript);
                if (content != null && !content.trim().isEmpty()) {
                    System.out.println("\nFound content using JavaScript fallback:\n" + content);
                    contentFound = true;
                }
            }
            if (!contentFound) {
                System.out.println("Could not extract post content with any method");
            }
        } catch (Exception e) {
            System.err.println("Error during scraping: " + e.getMessage());
            e.printStackTrace();
        }
    }
}