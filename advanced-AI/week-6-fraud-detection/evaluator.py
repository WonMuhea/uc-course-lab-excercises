import pandas as pd
from sklearn.metrics import classification_report, roc_auc_score
# Import all file paths from config
from config import SCORE_FILE, REPORT_FILE, HIST_PLOT_FILE, ROC_PLOT_FILE, PR_PLOT_FILE
# Import the new plotter module
from plotter import plot_reconstruction_histogram, plot_roc_curve, plot_pr_curve 

def evaluate_model(model, X_full_scaled, y_true):
    """Calculates anomaly scores, reports performance, and saves results and plots."""
    
    # 1. Get scores and predictions
    y_scores = model.decision_function(X_full_scaled)
    y_pred = model.predict(X_full_scaled)
    
    # --- A. Prepare and Save Anomaly Scores to CSV ---
    results_df = pd.DataFrame({
        'True_Class': y_true, 
        'Anomaly_Score': y_scores,
        'Predicted_Class': y_pred
    })
    results_df.to_csv(SCORE_FILE, index=False)
    print(f"Anomaly scores and predictions saved to: {SCORE_FILE}")


    # --- B. Calculate and Save Performance Report to TXT ---
    auc_roc = roc_auc_score(y_true, y_scores)
    report_str = classification_report(y_true, y_pred, digits=4)
    
    with open(REPORT_FILE, 'w') as f:
        f.write("--- Model Performance Report ---\n")
        f.write(f"AUC-ROC Score: {auc_roc:.4f}\n\n")
        f.write("Classification Report:\n")
        f.write(report_str)
        
    print(f"Performance report saved to: {REPORT_FILE}")
    print(f"AUC-ROC Score: {auc_roc:.4f}")
    
    
    # --- C. Generate and Save Plots using plotter.py ---
    
    # 1. Histogram Plot
    plot_reconstruction_histogram(results_df, HIST_PLOT_FILE)
    
    # 2. ROC Curve Plot
    plot_roc_curve(y_true, y_scores, ROC_PLOT_FILE)
    
    # 3. Precision-Recall Curve Plot
    plot_pr_curve(y_true, y_scores, PR_PLOT_FILE)

    return y_scores, y_pred
