package com.detector.utility;

import java.util.*;
import java.util.stream.Collectors;
import com.detector.CustomDataStructures.*;
public class KeywordExtractor {

    // Stopwords list (you can extend this as needed)
    private static final Set<String> STOPWORDS = new CustomHashSet<>();
    static {
        STOPWORDS.addAll(Arrays.asList(
            "is", "and", "the", "of", "to", "a", "in", "on", "for", "with", "by", "at", "an", "it", "from", "as", "that", "this", "was", "are", "be"
        ));
    }
    public static List<String> extractKeywords(String text, int topN) {
        // Tokenize the text into words
        String[] tokens = text.toLowerCase().split("\\W+"); // Split by non-word characters
        
        // Count word frequencies while ignoring stopwords
        Map<String, Integer> wordFrequencies = new CustomHashMap<>();
        for (String token : tokens) {
            if (!STOPWORDS.contains(token) && token.length() > 2) { // Ignore stopwords and short words
                wordFrequencies.put(token, wordFrequencies.getOrDefault(token, 0) + 1);
            }
        }

        // Sort words by frequency in descending order
        return wordFrequencies.entrySet()
                .stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue())) // Sort by value (frequency)
                .map(Map.Entry::getKey) // Extract keys (words)
                .limit(topN) // Take the top N words
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        // Example input
        String sentence = "NASA plans to send a new rover to Mars to explore possible signs of water and life.";
        
        // Extract keywords
        List<String> keywords = extractKeywords(sentence, 4);
        
        // Output keywords
        System.out.println("Keywords: " + keywords);
    }
}
