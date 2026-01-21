# NotesHub (Android + TensorFlow Lite)

**NotesHub** is a mobile notes app featuring **on-device text classification**. Notes
are automatically assigned to a category (`shopping`, `tasks`, `finance`, `education`, `other`) using a **TensorFlow model that I trained myself** and exported to **TensorFlow Lite** for fast, offline inference on Android.

This project demonstrates a real-world ML-to-mobile pipeline: training a text
classifier, exporting it to TFLite, and ensuring the **preprocessing on Android exactly matches the training process** (standardization, tokenization, vocabulary, and class mapping).

---

## Why this project is important

Most "AI apps" call a cloud API. This one runs entirely **on-device**:
- **
Works offline**: Classification doesn't require an internet connection.
- **Low latency**: Results are instantaneous.
- **Privacy-focused**: User data (the content of their notes) never leaves the phone.
- **Reproducible ML pipeline**: The same input text will always produce the same output.

---


## Key Features

- **On-device note categorization** on create/edit (TensorFlow Lite).
- **Full CRUD functionality**: Create, read, update, and delete notes.
- **Category Filtering**: Filter visible notes by category using the side navigation drawer.
- **Local Persistence**: All notes are saved to
a local Room database.
- **Modern UI**:
  - Two-column grid layout (`GridLayoutManager`).
  - Light and Dark theme support with a toggle in the menu.
  - Side navigation drawer (`NavigationDrawer`).
- **Deterministic Preprocessing**:
  - Text standardization (lowercase + punctuation removal
).
  - Tokenization using the same `vocab.json` from the training pipeline.
  - Fixed sequence length of `SEQ_LEN = 16`.
- **Debugging Tools**:
  - Model hash check to guarantee the correct file is loaded.
  - Logging for token IDs and model
confidence scores.

---

## Model and Inference Details

### Inputs
- Model file: `core_model.tflite`
- Input tensor: **INT32 token IDs**
- Shape: `[1, 16]` (batch size 1, sequence length 16)


### Metadata
- `vocab.json` — The vocabulary map to convert tokens to integer IDs.
- `classes.json` — The ordered list of class labels, used to map output indices to human-readable names.

### Outputs
- A vector of scores/probabilities for each class.
- The top-1
predicted class with the highest confidence score.

---

## Debugging & Reproducibility

This project solves the classic "Python vs. Android mismatch" problem:

### 1) Verify Tokens
For the same input text, confirm that the Android app and the Python script produce identical token IDs. This requires identical standardization rules
, the same vocabulary, and the same sequence length/padding strategy.

### 2) Verify Class Mapping
Ensure the output indices from the model on Android map to the same class order as defined in `classes.json`.

### 3) Verify Model Identity (Critical)
Even if everything else looks correct, mismatches
can occur if Android loads a different model file. **A real issue encountered in this project was:**

- Python predicted: **98% shopping** for `"I need to buy milk"`
- Android predicted: **~73% tasks**
- The tokens and class order were identical.
- **Root Cause**:
The Android app was loading a different `core_model.tflite` asset.
- **Fix**: Hash the model on both sides and replace the incorrect asset.

**Recommendation:** Print the model's **SHA-256 hash** at app startup to guarantee you are using the intended model file
.

---

## How to Run

1. Open the project in Android Studio.
2. Make sure the following files are present in the `app/src/main/assets/` directory:
   - `core_model.tflite`
   - `vocab.json`
   - 
`classes.json`
3. Build and run the project on an emulator or a physical device.

---

## Tech Stack

- **Platform**: Android
- **Language**: Kotlin
- **UI**: XML Layouts, Material Components, Coroutines, RecyclerView
- **Database**: Room Persistence Library
-
**Machine Learning**: TensorFlow Lite (`org.tensorflow:tensorflow-lite`)

---

## Notes

This repository is intentionally built around a practical ML deployment problem: **getting identical predictions across Python and Android requires strict artifact and preprocessing consistency**.

If you are learning mobile ML, this is the kind of issue you will
definitely face in real projects—and this repository shows how to debug it properly.