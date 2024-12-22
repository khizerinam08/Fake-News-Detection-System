from transformers import pipeline

# Load a pre-trained NLI model
nli_model = pipeline("text-classification", model="roberta-large-mnli")

# Define statements
statement1 = "Israel attacks Gaza through the north side"
statement2 = "Palestine is not safe for now. Israel is marching forward."

# Combine as premise and hypothesis
result = nli_model(f"{statement1} [SEP] {statement2}")
print(result)
