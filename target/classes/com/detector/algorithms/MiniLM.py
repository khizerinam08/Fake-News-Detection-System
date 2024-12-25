from transformers import pipeline
import sys

def get_contradiction_score(text1, text2):
    try:
        # Initialize model
        nli_model = pipeline("text-classification", model="roberta-large-mnli")
        
        # Get prediction
        result = nli_model(f"{text1} [SEP] {text2}")
        
        # Extract contradiction score
        contradiction_score = 0.0
        for prediction in result:
            if prediction['label'] == 'CONTRADICTION':
                contradiction_score = prediction['score']
                
        # Output score in format expected by Java
        print(f"contradiction_score: {contradiction_score}")
        return contradiction_score
        
    except Exception as e:
        print(f"contradiction_score: 0.0")
        sys.stderr.write(f"Error: {str(e)}\n")
        return 0.0

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("contradiction_score: 0.0")
        sys.stderr.write("Error: Expected 2 text arguments\n")
    else:
        text1 = sys.argv[1]
        text2 = sys.argv[2]
        get_contradiction_score(text1, text2)