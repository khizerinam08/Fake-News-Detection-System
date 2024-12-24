import sys
from transformers import pipeline

# Load the pre-trained NLI model
nli_model = pipeline("text-classification", model="roberta-large-mnli")

# Get the input arguments
if len(sys.argv) != 3:
    print("Usage: python MiniLM.py <text1> <text2>")
    sys.exit(1)

statement1 = sys.argv[1]
statement2 = sys.argv[2]

# Pass the statements as a pair (premise, hypothesis)
input_text = (statement1, statement2)

# Get the model result
result = nli_model(input_text)

# Extract the contradiction score
contradiction_score = next((item['score'] for item in result if item['label'] == "CONTRADICTION"), 0.0)

# Print the result in a parsable format
print(f"contradiction_score: {contradiction_score}")
