# 🤖 Django Chatterbot Terminal Client

This is a **Python terminal client** that communicates with a **Django REST Framework (DRF)** backend serving a **Chatterbot** instance. This project demonstrates separating the application logic (the bot) from the client interface (the terminal).

---

## 🚀 Project Structure

The project is split into two main components:

1.  **Server (`mychatbot_project`):** A Django application exposing a single API endpoint to process chat messages.
2.  **Client (`terminal_client.py`):** A standalone Python script that sends user input to the server and prints the bot's response.


## ✨ Prerequisites

You need Python 3.8+ (done in my local with pythong 3.12) and a standard terminal environment.

---

## ⚙️ Setup and Running

Follow these steps to get the server and client running.

### 1. Start the Django Server (Terminal 1)

The server handles initializing and training the Chatterbot, and exposes the chat API.

1.  Go to the parent read me and activate virtual env [Go to parent readme](../README.md)
2. Ensure (venv) is active!
   ```bash
   pip install -r requirements.txt
   ```
2.  Run database migrations:
    ```bash
    python manage.py migrate
    ```
3.  Start the development server. **The bot will train itself on first run.**
    ```bash
    python manage.py runserver
    ```
    *Wait for the "Chatterbot training complete" message.*

### 2. Run the Terminal Client (Terminal 2)

Open a **separate terminal window** to run the client.

1.  Navigate to the project root directory [Go to parent](../).
2.  Activate the virtual environment (again, in this new terminal):
    ```bash
    source venv/bin/activate 
    ```
3.  Execute the client script:
    ```bash
    python terminal_client.py
    ```

---

## 💬 Usage

🤖 Terminal Chatterbot Client
-----------------------------------
Connecting to API at: [http://127.0.0.1:8000/api/chat/](http://127.0.0.1:8000/api/chat/)
Type 'quit' or 'exit' to end the session.
-----------------------------------
You: Hello, how are you?
Bot: I am doing well.
You: What is your name?
Bot: My name is MyTerminalBot.
You: exit

Goodbye! 👋