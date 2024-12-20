package com.example;

import java.util.Arrays;
import java.util.List;

public class PostToSearchConvert {


    public String convert(String caption) {

        // Step 1: Remove filler words (optional, for concise queries)
        List<String> fillerWords = Arrays.asList("the", "is", "in", "at", "of", "a", "and", "to", "for", "on", "with", "by");
        String[] words = caption.split("\\s+");
        StringBuilder filteredCaption = new StringBuilder();

        for (String word : words) {
            if (!fillerWords.contains(word.toLowerCase())) {
                filteredCaption.append(word).append(" ");
            }
        }

        // Step 2: Trim and normalize whitespace
        String shortenedCaption = filteredCaption.toString().trim();

        // Step 3: Optionally, shorten the query to a maximum length
        int maxQueryLength = 100; // Example: limit query to 100 characters
        if (shortenedCaption.length() > maxQueryLength) {
            shortenedCaption = shortenedCaption.substring(0, maxQueryLength).trim();
        }

        return shortenedCaption;
    }

    public static void main(String[] args) {
        PostToSearchConvert converter = new PostToSearchConvert();

        // Example captions
        String caption1 = "The CEO of UnitedHealthcare was fatally shot in what police said appears to be a \"premeditated, preplanned targeted attack\" in bustling midtown Manhattan on Wednesday morning. Surveillance video captured the chilling shooting and suspect?s escape. ";
        String caption2 = "Latest football match results are in! Check out the highlights and scores now.";

        // Convert captions to search-friendly strings
        String searchQuery1 = converter.convert(caption1);
        String searchQuery2 = converter.convert(caption2);

        System.out.println("Search Query 1: " + searchQuery1);
        System.out.println("Search Query 2: " + searchQuery2);
    }
}
