package org.example;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class App {

    public static void main(String[] args) throws IOException, URISyntaxException {
        String bearerToken = "AAAAAAAAAAAAAAAAAAAAAO6fxAEAAAAACpMKeL9AQM4dGiMPyNRxxgHCfHw%3DhbKyHC1W7QhXQNVAI1tqj4gNUcNdxG8Ae7VaF3iKHWhtxbrnkY"; // Replace with your actual bearer token

        // Get the URL from the user


        String url = "https://x.com/elonmusk/status/1870742007683137631";

        // Extract the tweet ID from the URL
        String tweetId = extractTweetIdFromUrl(url);
        if (tweetId == null) {
            System.out.println("Invalid Twitter URL. Please provide a valid URL.");
            return;
        }

        // Fetch the tweet
        String tweetText = getTweets(tweetId, bearerToken);
        if (tweetText != null && !tweetText.isEmpty()) {
            System.out.println("Tweet Text: " + tweetText);
        } else {
            System.out.println("Could not fetch the tweet.");
        }
    }

    /*
     * This method calls the v2 Tweets endpoint with ids as query parameter
     */
    private static String getTweets(String ids, String bearerToken) throws IOException, URISyntaxException {
        String tweetResponse = null;

        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        URIBuilder uriBuilder = new URIBuilder("https://api.twitter.com/2/tweets");
        ArrayList<NameValuePair> queryParameters = new ArrayList<>();
        queryParameters.add(new BasicNameValuePair("ids", ids));
        queryParameters.add(new BasicNameValuePair("tweet.fields", "created_at,text"));
        uriBuilder.addParameters(queryParameters);

        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.setHeader("Authorization", String.format("Bearer %s", bearerToken));
        httpGet.setHeader("Content-Type", "application/json");

        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            tweetResponse = EntityUtils.toString(entity, "UTF-8");
        }

        String text = "";
        try {
            // Parse JSON response
            Object obj = new JSONParser().parse(tweetResponse);
            JSONObject jsonResponse = (JSONObject) obj;

            JSONArray dataArray = (JSONArray) jsonResponse.get("data");
            if (dataArray != null && !dataArray.isEmpty()) {
                JSONObject tweetData = (JSONObject) dataArray.get(0);
                text = (String) tweetData.get("text");
            } else {
                System.out.println("No tweets found for the given ID.");
            }

        } catch (Exception e) {
            System.err.println("Error parsing the response: " + e.getMessage());
            e.printStackTrace();
        }

        return text;
    }

    /*
     * Extracts the tweet ID from a given Twitter URL
     */
    private static String extractTweetIdFromUrl(String url) {
        try {
            // Extract the tweet ID from the URL
            String[] parts = url.split("/");
            if (parts.length > 0) {
                String idPart = parts[parts.length - 1];
                // Ensure it's numeric (basic validation)
                if (idPart.matches("\\d+")) {
                    return idPart;
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting tweet ID: " + e.getMessage());
        }
        return null; // Return null if the ID could not be extracted
    }
}
