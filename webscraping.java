import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

public class WebScraping {
    public static void main(String[] args) {
        try {
            Document doc = Jsoup.connect("https://x.com/RizzOhioSk1669").get();
            Elements tweets = doc.select("div.tweet");
            for (Element tweet : tweets) {
                System.out.println(tweet.text());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
