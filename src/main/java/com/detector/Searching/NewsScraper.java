import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewsScraper {

    public static List<String> fetchHeadlines(String keyword, String url, String headlineSelector) {
        List<String> matchingHeadlines = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(url).get();
            Elements headlines = doc.select(headlineSelector);
            
            // Filter headlines containing the keyword
            for (Element headline : headlines) {
                String text = headline.text();
                if (text.toLowerCase().contains(keyword.toLowerCase())) {
                    matchingHeadlines.add(text);
                }
            }
        } catch (IOException e) {
            System.err.println("Error fetching data from " + url + ": " + e.getMessage());
        }
        return matchingHeadlines;
    }

    public static void main(String[] args) {
        // Define websites and their headline CSS selectors
        String keyword = "manager"; // Example keyword to search for
        String[][] websites = {
            {"BBC", "https://www.bbc.com/news", ".gs-c-promo-heading__title"},
            {"Al Jazeera", "https://www.aljazeera.com", ".post-title"},
            {"CNN", "https://edition.cnn.com", ".cd__headline-text"},
            {"RT", "https://www.rt.com", ".card__title"},
            {"Bloomberg", "https://www.bloomberg.com", "h1, h2"},
            {"FOX News", "https://www.foxnews.com", ".title"}
        };

        for (String[] site : websites) {
            String siteName = site[0];
            String url = site[1];
            String selector = site[2];

            System.out.println("Searching headlines on " + siteName + "...");
            List<String> headlines = fetchHeadlines(keyword, url, selector);

            if (!headlines.isEmpty()) {
                System.out.println("Matching headlines on " + siteName + ":");
                for (String headline : headlines) {
                    System.out.println("- " + headline);
                }
            } else {
                System.out.println("No matching headlines found on " + siteName);
            }
        }
    }
}
