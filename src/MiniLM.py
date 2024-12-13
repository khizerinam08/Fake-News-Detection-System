import sys
import torch
from transformers import AutoTokenizer, AutoModel
import warnings
import json
import re

# Suppress warnings
warnings.filterwarnings("ignore")

# Load model and tokenizer
model_name = "sentence-transformers/all-MiniLM-L6-v2"
tokenizer = AutoTokenizer.from_pretrained(model_name)
model = AutoModel.from_pretrained(model_name)

def normalize_text(text):
    """Normalize text by removing punctuation and standardizing whitespace"""
    text = text.lower()
    text = re.sub(r'[^\w\s]', '', text)
    text = re.sub(r'\s+', ' ', text).strip()
    return text

def detect_direct_contradiction(text1, text2):
    """Check for direct contradictions using pattern matching"""
    # Normalize texts
    t1 = normalize_text(text1)
    t2 = normalize_text(text2)
    
    # Basic patterns for contradictions
    patterns = [
        # Direct negation
        (r'\b(is|was|are|were)\b', r'\1 not'),
        (r'\b(is|was|are|were)n\'?t\b', r'\1'),
        # Presence vs absence
        (r'\bhad\b', r'had no'),
        (r'\bhad no\b', r'had'),
        # Antonyms (expand this list as needed)
        (r'\bwith\b', r'without'),
        (r'\bwithout\b', r'with'),
    ]
    
    # Check each pattern
    for pattern, replacement in patterns:
        t1_mod = re.sub(pattern, replacement, t1)
        if t1_mod == t2 or t2 == t1_mod:
            return True
            
    # Handle double negatives
    if ('not' in t1 and 'not' in t2) or ("nt" in t1 and "nt" in t2):
        t1_positive = t1.replace(' not ', ' ').replace('nt ', ' ')
        t2_positive = t2.replace(' not ', ' ').replace('nt ', ' ')
        if t1_positive == t2_positive:
            return True
    
    return False

def get_semantic_contradiction(text1, text2):
    """Calculate semantic contradiction using embeddings"""
    inputs = tokenizer([text1, text2], padding=True, truncation=True, return_tensors="pt")
    
    with torch.no_grad():
        outputs = model(**inputs)
        embeddings = outputs.last_hidden_state.mean(dim=1)
        similarity = torch.nn.functional.cosine_similarity(embeddings[0].unsqueeze(0), embeddings[1].unsqueeze(0))
        contradiction_score = (1 - similarity.item())
    
    return contradiction_score

def analyze_contradiction(text1, text2):
    """Analyze contradiction using both pattern matching and semantic analysis"""
    # First check for direct contradictions
    if detect_direct_contradiction(text1, text2):
        return {
            "contradiction_score": 1.0,
            "is_contradictory": True,
            "method": "pattern_matching",
            "confidence": "high"
        }
    
    # If no direct contradiction found, use semantic analysis
    contradiction_score = get_semantic_contradiction(text1, text2)
    confidence = "high" if abs(contradiction_score - 0.5) > 0.3 else "medium"
    
    return {
        "contradiction_score": contradiction_score,
        "is_contradictory": contradiction_score > 0.3,
        "method": "semantic_analysis",
        "confidence": confidence
    }

def main():
    if len(sys.argv) != 3:
        print(json.dumps({"error": "Expected 2 arguments"}))
        sys.exit(1)
    
    try:
        result = analyze_contradiction(sys.argv[1], sys.argv[2])
        print(json.dumps(result))
        sys.stdout.flush()
    except Exception as e:
        print(json.dumps({"error": str(e)}))
        sys.exit(1)

if __name__ == "__main__":
    main()