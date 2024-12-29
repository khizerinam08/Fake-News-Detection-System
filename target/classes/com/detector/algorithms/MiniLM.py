from transformers import pipeline
import sys

def analyze_sentence_relationship(text1, text2):
    try:
        # Initialize model
        nli_model = pipeline("text-classification", model="roberta-large-mnli")
        
        # Get predictions
        result = nli_model(f"{text1} [SEP] {text2}")
        
        # Format output for Java
        output = []
        for prediction in result:
            label = prediction['label']
            score = int(prediction['score'] * 100)
            output.append(f"{label}: {score:.4f}")
        
        # Print results in expected format
        print(", ".join(output))
        return result
        
    except Exception as e:
        print("Error: 0.0")
        sys.stderr.write(f"Error: {str(e)}\n")
        return []

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Error: 0.0")
        sys.stderr.write("Error: Expected 2 text arguments\n")
    else:
        text1 = sys.argv[1]
        text2 = sys.argv[2]
        analyze_sentence_relationship(text1, text2)
