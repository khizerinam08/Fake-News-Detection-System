package com.example;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.stemmer.PorterStemmer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PostToSearchConvert {
    private TokenizerME tokenizer;
    private POSTaggerME posTagger;
    private PorterStemmer stemmer;
    private Set<String> stopWords;
    private Set<String> exceptions;

    public PostToSearchConvert() {
        try {
            // Initialize OpenNLP tools
            InputStream tokenizerStream = getClass().getClassLoader().getResourceAsStream("en-token.bin");
            if (tokenizerStream == null) {
                System.out.println("Error: Tokenizer model file not found.");
                return;
            }
            TokenizerModel tokenizerModel = new TokenizerModel(tokenizerStream);
            tokenizer = new TokenizerME(tokenizerModel);

            InputStream posStream = getClass().getClassLoader().getResourceAsStream("en-pos-maxent.bin");
            if (posStream == null) {
                System.out.println("Error: POS model file not found.");
                return;
            }
            POSModel posModel = new POSModel(posStream);
            posTagger = new POSTaggerME(posModel);

            stemmer = new PorterStemmer();
            
            // Initialize common English stop words
            stopWords = new HashSet<>(Arrays.asList(
                "a", "an", "and", "are", "as", "at", "be", "by", "for", "from",
                "has", "he", "in", "is", "it", "its", "of", "on", "that", "the",
                "to", "was", "were", "will", "with", "the", "this", "but", "they",
                "have", "had", "what", "when", "where", "who", "which", "why", "how"
            ));

            // Expanded exceptions list to preserve certain words
            exceptions = new HashSet<>(Arrays.asList(
                "news", "football", "score", "highlight", "match", "breaking", "update",
                "climate", "wildlife", "impact", "results", "refugees", "syria", "bashar",
                "toppling", "fragile", "conflict", "return", "society", "genova", "press", "briefing",
                "country", "people"
            ));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String convert(String caption) {
        // Tokenize the text
        String[] tokens = tokenizer.tokenize(caption);

        // Get parts of speech
        String[] tags = posTagger.tag(tokens);

        List<String> keywords = new ArrayList<>();

        // Extract important words (nouns, verbs, adjectives)
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].toLowerCase();
            String tag = tags[i];

            // Skip stop words and punctuation
            if (stopWords.contains(token) || token.matches("\\p{Punct}")) {
                continue;
            }

            // Keep nouns (NN*), verbs (VB*), and adjectives (JJ*), and avoid stemming for some useful words
            if (tag.startsWith("NN") || tag.startsWith("VB") || tag.startsWith("JJ")) {
                // Exclude stemming for words in the exceptions list
                if (exceptions.contains(token)) {
                    keywords.add(token);
                } else {
                    // Stem the word to get its root form
                    String stemmed = stemmer.stem(token);
                    if (!keywords.contains(stemmed)) {
                        keywords.add(stemmed);
                    }
                }
            }
        }

        // Restrict the output to the first 10 words
        if (keywords.size() > 4) {
            keywords = keywords.subList(0, 4);
        }

        // Join keywords into a search query
        return String.join(" ", keywords);
    }

    public static void main(String[] args) {
        PostToSearchConvert converter = new PostToSearchConvert();

        // Example caption
        String caption1 = "CEO UnitedHealthcare was fatally shot what police said appears be \\\"premeditated, preplanned targeted\\\"";

        // Convert caption to search-friendly string
        String searchQuery1 = converter.convert(caption1);

        System.out.println("Original Caption 1: " + caption1);
        System.out.println("Search Query 1: " + searchQuery1);
    }
}
