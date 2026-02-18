import polyglot
import json
from transformers import BertTokenizer, BertModel
import torch
from logs.logs import echo

# Load pre-trained model and tokenizer
tokenizer = BertTokenizer.from_pretrained('bert-base-uncased')
model = BertModel.from_pretrained('bert-base-uncased')

def analyze_sentiment(text):
    echo()
    """Analyzes sentiment of a given text using BERT."""
    inputs = tokenizer(text, return_tensors="pt")
    outputs = model(**inputs)
    last_hidden_states = outputs.last_hidden_state

    sentence_embedding = torch.mean(last_hidden_states, dim=1).detach().numpy().tolist()

    return json.dumps({"embedding": sentence_embedding})

polyglot.export_value("analyze_sentiment", analyze_sentiment)
