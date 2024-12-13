package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ContradictionDetector {
    private static final String PYTHON_SCRIPT_PATH = "C:\\Users\\Khize\\Downloads\\MiniLM.py";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        List<NewsComparison> testCases = createTestCases();

        for (NewsComparison comparison : testCases) {
            try {
                ContradictionResult result = analyzeContradiction(comparison);
                printAnalysis(comparison, result);
            } catch (Exception e) {
                System.err.println("Error processing comparison: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static ContradictionResult analyzeContradiction(NewsComparison comparison) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("python", PYTHON_SCRIPT_PATH,
                comparison.statement1, comparison.statement2);

        Process process = pb.start();

        // Read both standard output and error
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            // Check for errors
            String errorLine;
            StringBuilder errorOutput = new StringBuilder();
            while ((errorLine = errorReader.readLine()) != null) {
                errorOutput.append(errorLine).append("\n");
            }
            if (errorOutput.length() > 0) {
                System.err.println("Python script errors:\n" + errorOutput);
            }

            // Read the result
            String output = reader.readLine();
            if (output == null) {
                throw new IOException("No output from Python script");
            }

            JsonNode resultNode = objectMapper.readTree(output);

            if (resultNode.has("error")) {
                throw new IOException("Python script error: " + resultNode.get("error").asText());
            }

            return new ContradictionResult(resultNode);
        }
    }

    private static void printAnalysis(NewsComparison comparison, ContradictionResult result) {
        System.out.println("\nAnalyzing News Comparison:");
        System.out.println("Statement 1: " + comparison.statement1);
        System.out.println("Statement 2: " + comparison.statement2);
        System.out.printf("Contradiction Score: %.2f%n", result.contradictionScore);
        System.out.println("Is Contradictory: " + result.isContradictory);
        System.out.println("Detection Method: " + result.method);
        System.out.println("Confidence: " + result.confidence);
        System.out.println("Analysis: " + (result.isContradictory ?
                "These statements CONTRADICT each other!" :
                "These statements are CONSISTENT with each other."));
        System.out.println("-".repeat(80));
    }

    private static List<NewsComparison> createTestCases() {
        List<NewsComparison> cases = new ArrayList<>();

        // Direct contradictions
        cases.add(new NewsComparison(
                "The sky is blue",
                "The sky is not blue"
        ));

        cases.add(new NewsComparison(
                "The car wasn't red",
                "The car was red"
        ));

        // Double negative case
        cases.add(new NewsComparison(
                "There wasn't any ammunition with security",
                "Security personnel weren't deployed 'without live ammunition'"
        ));

        // Similar meanings
        cases.add(new NewsComparison(
                "The weather is nice today",
                "It's a beautiful day"
        ));

        // Complex contradictions
        cases.add(new NewsComparison(
                "The president announced new policies today",
                "The president made no announcements today"
        ));

        return cases;
    }

    private static class NewsComparison {
        final String statement1;
        final String statement2;

        NewsComparison(String statement1, String statement2) {
            this.statement1 = statement1;
            this.statement2 = statement2;
        }
    }

    private static class ContradictionResult {
        final double contradictionScore;
        final boolean isContradictory;
        final String method;
        final String confidence;

        ContradictionResult(JsonNode node) {
            this.contradictionScore = node.get("contradiction_score").asDouble();
            this.isContradictory = node.get("is_contradictory").asBoolean();
            this.method = node.get("method").asText();
            this.confidence = node.get("confidence").asText();
        }
    }
}