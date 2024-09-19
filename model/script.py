import torch
import json
import pandas as pd
from sklearn.model_selection import train_test_split
from transformers import AutoTokenizer, AutoModelForSequenceClassification, AutoModel, BertTokenizerFast, BertForSequenceClassification
import torch.utils
import torch.utils.data
import tqdm

DEVICE = torch.device("cuda") if torch.cuda.is_available() else torch.device("cpu")
MODEL_NAME = "avichr/heBERT"
DATASET_PATH = "./dataset.json"

def load_dataset():
    with open(DATASET_PATH) as file:
        data = json.load(file)
        return pd.DataFrame.from_dict(data)

class SmsDataset(torch.utils.data.Dataset):
    def __init__(self, texts, labels):
        self.len = len(texts)
        assert(self.len == len(labels))
        tokenizer = BertTokenizerFast.from_pretrained(MODEL_NAME, max_length=512)
        self.encodings = tokenizer(texts, truncation=True, padding=True)
        self.labels = labels
    
    def __getitem__(self, idx):
       item = {key: torch.tensor(val[idx]) for key, val in self.encodings.items()}
       item['labels'] = torch.tensor(self.labels[idx])
       return item    
    
    def __len__(self):
        return self.len
        

df = load_dataset()
x = df["message"]
y = df["spam"].astype(int)
train_texts, test_texts, train_labels, test_labels = train_test_split(list(x), list(y), test_size=0.5)
train_dataset = SmsDataset(train_texts, train_labels)
test_dataset = SmsDataset(test_texts, test_labels)
model = BertForSequenceClassification.from_pretrained(MODEL_NAME)

model.to(DEVICE)
model.train()
 
train_loader = torch.utils.data.DataLoader(train_dataset, batch_size=16, shuffle=True)
test_loader = torch.utils.data.DataLoader(test_dataset, batch_size=16, shuffle=False)
optim = torch.optim.AdamW(model.parameters(), lr=5e-5)
 
for epoch in range(2):
   for batch in train_loader:
       optim.zero_grad()
       input_ids = batch['input_ids'].to(DEVICE)
       attention_mask = batch['attention_mask'].to(DEVICE)
       labels = batch['labels'].to(DEVICE)
       print(f"batch: {labels.size()}")
       outputs = model(input_ids, attention_mask=attention_mask, labels=labels)
       loss = outputs[0]
       loss.backward()
       optim.step()
 
model.eval()

def validation(model, dataloader):
    predictions_labels = []
    true_labels = []
    total_loss = 0

    model.eval()

    for batch in tqdm.tqdm(dataloader, total=len(dataloader)):
        true_labels += batch['labels'].numpy().flatten().tolist()
        batch = {k: v.type(torch.long).to(DEVICE) for k, v in batch.items()}

        with torch.no_grad():
            outputs = model(**batch)
            loss, logits = outputs[:2]
            logits = logits.detach().cpu().numpy()
            total_loss += loss.item()
            predict_content = logits.argmax(axis=-1).flatten().tolist()
            predictions_labels += predict_content
    
    avg_epoch_loss = total_loss / len(dataloader)
 
    return true_labels, predictions_labels, avg_epoch_loss

y_actual, y_pred, avg_epoch_loss = validation(model, test_loader)

def accuracy_score(actual, predicted):
    assert(len(actual) == len(predicted))
    correctlyPredicted = sum(map(lambda r: int(r[0] == r[1]), zip(actual, predicted)))
    return correctlyPredicted / len(actual)

print(f"actual {y_actual}")
print(f"pred {y_pred}")
print(f"accuracy: {accuracy_score(y_actual, y_pred)}")