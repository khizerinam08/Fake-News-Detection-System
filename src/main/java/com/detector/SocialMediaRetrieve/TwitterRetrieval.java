package com.detector.SocialMediaRetrieve;

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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class TwitterRetrieval {

    public static void main(String[] args) throws IOException, URISyntaxException {
        String bearerToken = "AAAAAAAAAAAAAAAAAAAAAO6fxAEAAAAACpMKeL9AQM4dGiMPyNRxxgHCfHw%3DhbKyHC1W7QhXQNVAI1tqj4gNUcNdxG8Ae7VaF3iKHWhtxbrnkY"; // Replace with your actual bearer token
        String response = getTweets("1860504779325128809", bearerToken);
        System.out.println(response);
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
}
