import matplotlib.pyplot as plt
from sklearn.metrics import roc_curve, roc_auc_score, precision_recall_curve, auc
import numpy as np

def plot_reconstruction_histogram(results_df, plot_path):
    """
    Generates and saves a histogram showing the distribution of anomaly scores
    for both normal and fraudulent transactions.
    """
    plt.figure(figsize=(10, 6))
    
    # Separate data points
    normal_scores = results_df[results_df['True_Class'] == 0]['Anomaly_Score']
    fraud_scores = results_df[results_df['True_Class'] == 1]['Anomaly_Score']
    
    # Plot Histograms
    plt.hist(normal_scores, bins=50, density=True, alpha=0.6, label='Normal (Class 0)', color='#3b82f6')
    plt.hist(fraud_scores, bins=50, density=True, alpha=0.8, label='Fraud (Class 1)', color='#ef4444')
    
    plt.title('Reconstruction Error Distribution by Class', fontsize=14, fontweight='bold')
    plt.xlabel('Reconstruction Error (Anomaly Score)', fontsize=12)
    plt.ylabel('Density', fontsize=12)
    plt.legend(loc='upper right')
    plt.grid(axis='y', alpha=0.4)
    plt.tight_layout()
    plt.savefig(plot_path)
    plt.close()
    print(f"Distribution histogram saved to: {plot_path}")


def plot_roc_curve(y_true, y_scores, plot_path):
    """
    Generates and saves the Receiver Operating Characteristic (ROC) curve.
    """
    fpr, tpr, _ = roc_curve(y_true, y_scores)
    roc_auc = roc_auc_score(y_true, y_scores)
    
    plt.figure(figsize=(8, 8))
    plt.plot(fpr, tpr, color='#059669', lw=2, 
             label=f'ROC curve (AUC = {roc_auc:.4f})')
    
    # Plot the random guess line
    plt.plot([0, 1], [0, 1], color='#4b5563', lw=2, linestyle='--')
    
    plt.xlim([0.0, 1.0])
    plt.ylim([0.0, 1.05])
    plt.xlabel('False Positive Rate (FPR)', fontsize=12)
    plt.ylabel('True Positive Rate (TPR) / Recall', fontsize=12)
    plt.title('Receiver Operating Characteristic (ROC) Curve', fontsize=14, fontweight='bold')
    plt.legend(loc="lower right")
    plt.grid(axis='both', alpha=0.4)
    plt.tight_layout()
    plt.savefig(plot_path)
    plt.close()
    print(f"ROC curve saved to: {plot_path}")


def plot_pr_curve(y_true, y_scores, plot_path):
    """
    Generates and saves the Precision-Recall (PR) curve, better for imbalanced data.
    """
    precision, recall, _ = precision_recall_curve(y_true, y_scores)
    pr_auc = auc(recall, precision)
    
    plt.figure(figsize=(8, 8))
    plt.plot(recall, precision, color='#8b5cf6', lw=2, 
             label=f'PR curve (AUC = {pr_auc:.4f})')
    
    plt.xlim([0.0, 1.0])
    plt.ylim([0.0, 1.05])
    plt.xlabel('Recall', fontsize=12)
    plt.ylabel('Precision', fontsize=12)
    plt.title('Precision-Recall Curve', fontsize=14, fontweight='bold')
    plt.legend(loc="lower left")
    plt.grid(axis='both', alpha=0.4)
    plt.tight_layout()
    plt.savefig(plot_path)
    plt.close()
    print(f"PR curve saved to: {plot_path}")
