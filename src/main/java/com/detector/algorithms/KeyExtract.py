import spacy
import sys

def extract_entities(text, top_n=5):
    nlp = spacy.load("en_core_web_sm")
    doc = nlp(text)
    entities = [ent.text.lower() for ent in doc.ents if ent.label_ in {"ORG", "GPE", "EVENT", "LOC"}]
    
    entity_freq = {}
    for entity in entities:
        entity_freq[entity] = entity_freq.get(entity, 0) + 1

    sorted_entities = sorted(entity_freq.items(), key=lambda x: x[1], reverse=True)
    return [entity for entity, freq in sorted_entities[:top_n]]

if __name__ == "__main__":
    if len(sys.argv) > 1:
        input_text = sys.argv[1]
        entities = extract_entities(input_text)
        print(" ".join(entities))  # Return as comma-separated string
    else:
        print("No input text provided")
