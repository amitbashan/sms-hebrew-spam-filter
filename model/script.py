import torch
import json
import pandas as pd
from onnxruntime.quantization import quantize_dynamic
from sklearn.model_selection import train_test_split
from sklearn.metrics import confusion_matrix
from matplotlib import pyplot as plt
from transformers import BertTokenizerFast, BertForSequenceClassification
import torch.utils
import torch.utils.data
import tqdm

EPOCHS = 5
BATCH_SIZE=16
DEVICE = torch.device("mps")
MODEL_NAME = "avichr/heBERT"
DATASET_PATH = "./dataset.json"
MAX_LENGTH = 512
FP_MODEL_OUT = "fpmodel.onnx"
QUANTIZED_MODEL_OUT = "quantized_model.onnx"

tokenizer = BertTokenizerFast.from_pretrained(MODEL_NAME, max_length=MAX_LENGTH)

def load_dataset():
    with open(DATASET_PATH) as file:
        data = json.load(file)
        return pd.DataFrame.from_dict(data)

class SmsDataset(torch.utils.data.Dataset):
    def __init__(self, texts, labels):
        self.len = len(texts)
        assert(self.len == len(labels))
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
train_texts, test_texts, train_labels, test_labels = train_test_split(list(x), list(y), test_size=0.1)
train_dataset = SmsDataset(train_texts, train_labels)
test_dataset = SmsDataset(test_texts, test_labels)

train_loader = torch.utils.data.DataLoader(train_dataset, batch_size=BATCH_SIZE, shuffle=True)
test_loader = torch.utils.data.DataLoader(test_dataset, batch_size=BATCH_SIZE, shuffle=False)

model = BertForSequenceClassification.from_pretrained(MODEL_NAME).to(DEVICE)
optim = torch.optim.AdamW(model.parameters(), lr=5e-5)

def accuracy_score(actual, predicted):
    assert(len(actual) == len(predicted))
    correctlyPredicted = sum(map(lambda r: int(r[0] == r[1]), zip(actual, predicted)))
    return correctlyPredicted / len(actual)

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

def measure_accuracy(model):
    y_actual, y_pred, _ = validation(model, test_loader)
    return accuracy_score(y_actual, y_pred)

def train(model, epochs):
    model.train()
    losses = []

    for epoch in tqdm.tqdm(range(epochs)):
        for batch in tqdm.tqdm(train_loader):
            optim.zero_grad()
            input_ids = batch['input_ids'].to(DEVICE)
            attention_mask = batch['attention_mask'].to(DEVICE)
            labels = batch['labels'].to(DEVICE)
            outputs = model(input_ids, attention_mask=attention_mask, labels=labels)
            loss = outputs[0]
            losses.append(loss.item())
            loss.backward()
            optim.step()

    return losses

print("Training...")
losses = train(model, EPOCHS)

plt.plot(losses)
plt.ylabel("Loss")
plt.xlabel("Batch number")
plt.show()

print("Finished training.")
print(f"Model accuracy: {measure_accuracy(model)}")

y_actual, y_pred, _ = validation(model, test_loader)
cm = confusion_matrix(y_actual, y_pred)
print(cm)

x = torch.randn(1, MAX_LENGTH, dtype=torch.long, device=DEVICE)
attention_mask = torch.randn(1, MAX_LENGTH, dtype=torch.long, device=DEVICE)

print(f"Exporting as ONNX... (saving as {FP_MODEL_OUT})")
torch.onnx.export(model, (x, attention_mask), FP_MODEL_OUT, input_names=["messageInput", "attentionMask"], output_names=["prediction"])
print(f"Quantizing model... (saving as {QUANTIZED_MODEL_OUT})")
quantize_dynamic(FP_MODEL_OUT, QUANTIZED_MODEL_OUT)