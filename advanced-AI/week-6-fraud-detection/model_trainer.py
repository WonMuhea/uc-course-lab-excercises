from pyod.models.auto_encoder import AutoEncoder
from config import AE_HIDDEN_LIST, AE_EPOCHS, AE_BATCH_SIZE, CONTAMINATION_RATE 

def train_model(X_train, contamination_rate):
    """Initializes and trains the PyOD AutoEncoder model."""
    print("Initializing and training AutoEncoder...")
    
    ae = AutoEncoder(
        hidden_neuron_list=AE_HIDDEN_LIST,
        epoch_num=AE_EPOCHS,
        batch_size=AE_BATCH_SIZE,
        contamination=contamination_rate,
        verbose=0
    )
    ae.fit(X_train)
    print("Training complete.")
    return ae