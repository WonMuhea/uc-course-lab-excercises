# main.py (Add the os import and directory creation)

import os # Import at the top
from data_loader import load_and_preprocess_data
from model_trainer import train_model
from evaluator import evaluate_model
from config import DATA_PATH, CONTAMINATION_RATE, OUTPUT_DIR # Import OUTPUT_DIR

def main():
    # Create output directory if it doesn't exist
    os.makedirs(OUTPUT_DIR, exist_ok=True) 

    # 1. Load and Prepare Data
    X_train, X_full_scaled, y_true = load_and_preprocess_data(DATA_PATH)

    # 2. Train Model
    ae_model = train_model(X_train, CONTAMINATION_RATE)
    
    # 3. Evaluate Model and Save Results
    evaluate_model(ae_model, X_full_scaled, y_true) 

if __name__ == "__main__":
    main()