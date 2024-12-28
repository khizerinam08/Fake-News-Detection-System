package com.detector.utility;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import com.detector.CustomDataStructures.*;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class PostToSearchConvert {
    private final StanfordCoreNLP pipeline;
    private final Set<String> stopwords;
    private final Set<String> commonWords;
    private static final int MAX_KEYWORDS = 4;
    private static final int MIN_WORD_LENGTH = 3;

    public PostToSearchConvert() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        pipeline = new StanfordCoreNLP(props);
        
        stopwords = new CustomHashSet<>();
        stopwords.addAll(Arrays.asList(
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from",
            "has", "he", "in", "is", "it", "its", "of", "on", "that", "the",
            "to", "was", "were", "will", "with", "what", "said", "appears",
            "reported", "according", "sources", "breaking", "latest", "update",
            "during", "after", "before", "while", "says", "claimed", "reports",
            "could", "would", "should", "must", "may", "might", "new"
        ));

        commonWords = new CustomHashSet<>();
        commonWords.addAll(Arrays.asList(
            "people", "time", "year", "day", "man", "woman", "world", "life",
            "country", "state", "city", "government", "president", "minister",
            "official", "police", "military", "army", "force", "security",
            "report", "statement", "announcement", "news", "press", "media"
        ));
    }

    public String convert(String caption) {
        if (caption == null || caption.trim().isEmpty()) {
            return "";
        }

        CoreDocument doc = new CoreDocument(caption);
        pipeline.annotate(doc);

        // Track word frequency for uniqueness scoring
        Map<String, Integer> wordFrequency = new CustomHashMap<>();
        Map<String, Integer> firstPositions = new CustomHashMap<>();
        Map<String, Double> keywordScores = new CustomHashMap<>();
        
        // First pass: collect word frequencies
        doc.tokens().forEach(token -> {
            String word = token.word().toLowerCase();
            if (!stopwords.contains(word) && word.length() >= MIN_WORD_LENGTH) {
                wordFrequency.merge(word, 1, Integer::sum);
            }
        });

        // Second pass: score words
        int position = 0;
        for (CoreLabel token : doc.tokens()) {
            String word = token.word().toLowerCase();
            String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
            String ner = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

            if (stopwords.contains(word) || word.length() < MIN_WORD_LENGTH) {
                position++;
                continue;
            }

            firstPositions.putIfAbsent(word, position);
            double score = calculateScore(ner, pos, position, word, wordFrequency.get(word));
            keywordScores.merge(word, score, Double::sum);
            position++;
        }

        // Select and order keywords
        return keywordScores.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(MAX_KEYWORDS)
            .sorted(Comparator.comparing(e -> firstPositions.get(e.getKey())))
            .map(Map.Entry::getKey)
            .collect(Collectors.joining(" "));
    }

    private double calculateScore(String ner, String pos, int position, String word, int frequency) {
        double score = 0.0;
        
        // NER scoring with higher weights
        switch (ner) {
            case "ORGANIZATION": score += 8.0; break;
            case "PERSON": score += 7.0; break;
            case "LOCATION": score += 6.0; break;
            case "DATE": score += 4.0; break;
            case "MONEY": score += 3.0; break;
            case "PERCENT": score += 3.0; break;
            default: 
                if (pos.startsWith("NN")) score += 2.5;
                else if (pos.startsWith("VB")) score += 2.0;
                else if (pos.startsWith("JJ")) score += 1.5;
                break;
        }
        
        // Uncommon word bonus
        if (!commonWords.contains(word)) {
            score *= 1.5;
        }
        
        // Word length bonus (favor longer words as they tend to be more specific)
        score += Math.min(2.0, word.length() / 5.0);
        
        // Position bonus (exponential decay)
        score += 2.0 * Math.exp(-position / 20.0);
        
        // Frequency penalty (favor unique words)
        score *= (1.0 + (1.0 / frequency));
        
        return score;
    }


}