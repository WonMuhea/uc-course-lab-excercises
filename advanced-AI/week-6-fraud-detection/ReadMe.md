### PyOD AutoEncoder Fraud Detection

This project uses the PyOD AutoEncoder for unsupervised credit card fraud detection, identifying outliers by measuring high reconstruction error. The application is modularized into dedicated files for configuration, training, evaluation, and plotting.

## Quick Start

1.  Navigate to the project root directory to setup project [Go to parent](../).
2.  Activate the virtual environment (again, in this new terminal):
    ```bash
    source venv/bin/activate 
    ```
3.  Execute the script to install dependencies and ensure the ```creditcard.csv``` dataset path is configured in ```config.py.```:
    ```bash
    pip install -r requirements.txt
    ```
4. Run the app and output_results directory will be created and plots will be generated
   ```bash 
   python main.py
   ```