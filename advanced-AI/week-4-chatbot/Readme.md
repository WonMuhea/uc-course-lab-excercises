# 🤖 Django Chatterbot Terminal Client

This is a **Python terminal client** that communicates with a **Django REST Framework (DRF)** backend serving a **Chatterbot** instance:the application logic (the bot) seprated from the client interface (the terminal).

## 🚀 Project Structure

The project is split into two main components:

1.  **Server (`mychatbot_project`):** A Django application exposing a single API endpoint to process chat messages.
2.  **Client (`terminal_client.py`):** A standalone Python script that sends user input to the server and prints the bot's response.


## ✨ Prerequisites

You need Python 3.8+ (done in my local with pythong 3.12) and a standard terminal environment.

1.  **Python Packages:** Django, Django REST Framework, Chatterbot, and the Python `requests` library.

    ```bash
    pip install django djangorestframework chatterbot chatterbot-corpus requests
    ```

---

## ⚙️ Setup and Running

Follow these steps to get the server and client running.

### 1. Start the Django Server (Terminal 1)

The server handles initializing and training the Chatterbot, and exposes the chat API.

1.  Go to the parent read me and activate virtual env [Go to parent readme](../README.md)
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

The terminal client will prompt you for input.