import sys
from transformers import pipeline, AutoTokenizer, AutoModelForSequenceClassification
import torch

# Check for correct input arguments
if len(sys.argv) != 3:
    print("Usage: python MiniLM.py <text1> <text2>")
    sys.exit(1)

# Read the inputs
statement1 = sys.argv[1]
statement2 = sys.argv[2]

try:
    # Load model and tokenizer explicitly
    model_name = "roberta-large-mnli"
    tokenizer = AutoTokenizer.from_pretrained(model_name)
    model = AutoModelForSequenceClassification.from_pretrained(model_name)
    
    # Prepare input
    encoded_input = tokenizer(statement1, statement2, return_tensors='pt', padding=True)
    
    # Get model prediction
    with torch.no_grad():
        output = model(**encoded_input)
        scores = torch.nn.functional.softmax(output.logits, dim=1)
        
    # The labels are typically [entailment, neutral, contradiction]
    # Get contradiction score (usually index 2)
    contradiction_score = scores[0][2].item()
    
    # Print result in a format the Java code expects
    print(f"contradiction_score: {contradiction_score:.4f}")
    
except Exception as e:
    print(f"Error: {str(e)}")
    print("contradiction_score: 0.0000")