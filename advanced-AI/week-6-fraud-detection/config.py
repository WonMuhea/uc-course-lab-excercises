

# Data Settings
DATA_PATH = 'data/creditcard.csv'
CONTAMINATION_RATE = 0.0017 # Expected fraction of outliers

# AutoEncoder Hyperparameters
N_FEATURES = 29
# The list should define the layers *between* the input layer (N_FEATURES)
# and the output layer (N_FEATURES). PyOD handles the input/output layers automatically.
# However, for AutoEncoder, the full symmetric structure is often expected:
# Input -> 128 -> 64 (bottleneck) -> 128 -> Output
# We will define the hidden layers only: [128, 64]
AE_HIDDEN_LIST = [128, 64] 
AE_EPOCHS = 10
AE_BATCH_SIZE = 32
RANDOM_STATE = 42

# Output File Settings
OUTPUT_DIR = 'output_results' # Directory to save all outputs
SCORE_FILE = f'{OUTPUT_DIR}/anomaly_scores.csv'
REPORT_FILE = f'{OUTPUT_DIR}/performance_report.txt'
PLOT_FILE = f'{OUTPUT_DIR}/reconstruction_error_hist.png'

# Plot File Paths (New/Updated)
HIST_PLOT_FILE = f'{OUTPUT_DIR}/reconstruction_error_hist.png'
ROC_PLOT_FILE = f'{OUTPUT_DIR}/roc_curve.png'
PR_PLOT_FILE = f'{OUTPUT_DIR}/precision_recall_curve.png'