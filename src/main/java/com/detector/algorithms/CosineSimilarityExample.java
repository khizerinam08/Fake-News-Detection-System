import org.apache.commons.text.similarity.CosineSimilarity;

import java.util.HashMap;
import java.util.Map;

public class CosineSimilarityExample {
    public static void main(String[] args) {
        // Input strings
        String text1 = "This is a sample text.";
        String text2 = "This text is a sample.";

        // Convert strings to term frequency maps
        Map<CharSequence, Integer> vector1 = getTermFrequencyMap(text1);
        Map<CharSequence, Integer> vector2 = getTermFrequencyMap(text2);

        // Compute cosine similarity
        CosineSimilarity cosineSimilarity = new CosineSimilarity();
        double similarity = cosineSimilarity.cosineSimilarity(vector1, vector2);

        System.out.println("Cosine Similarity: " + similarity);
    }

    // Helper method to create term frequency map
    private static Map<CharSequence, Integer> getTermFrequencyMap(String text) {
        Map<CharSequence, Integer> termFrequencyMap = new HashMap<>();
        String[] terms = text.toLowerCase().split("\\s+");
        for (String term : terms) {
            termFrequencyMap.put(term, termFrequencyMap.getOrDefault(term, 0) + 1);
        }
        return termFrequencyMap;
    }
}
