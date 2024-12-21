from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity

# Load pre-trained Sentence-BERT model
model = SentenceTransformer('all-MiniLM-L6-v2')

# Input texts
text1 = "hot"
text2 = "bucket"

# Generate embeddings
embedding1 = model.encode(text1)
embedding2 = model.encode(text2)

# Compute cosine similarity
similarity = cosine_similarity([embedding1], [embedding2])[0][0]
print(f"Cosine Similarity: {similarity}")