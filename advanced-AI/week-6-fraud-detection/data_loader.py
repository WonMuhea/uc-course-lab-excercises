
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from config import RANDOM_STATE

def load_and_preprocess_data(data_path):
    """Loads, scales, and splits data into normal training/validation sets."""
    data = pd.read_csv(data_path)
    X = data.drop(['Time', 'Class'], axis=1)
    y = data['Class']
    
    # 1. Train only on normal data (Class == 0)
    X_normal = X[y == 0]
    
    # 2. Scale features
    scaler = StandardScaler()
    X_scaled_normal = scaler.fit_transform(X_normal)
    
    # 3. Split normal data
    X_train, X_val, _, _ = train_test_split(
        X_scaled_normal, X_normal, test_size=0.2, random_state=RANDOM_STATE
    )
    
    # Return the full scaled features and the target for final scoring/evaluation
    X_full_scaled = scaler.transform(X) 
    
    return X_train, X_full_scaled, y