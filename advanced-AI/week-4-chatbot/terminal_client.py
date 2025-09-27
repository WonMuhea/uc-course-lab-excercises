# terminal_client.py

import requests
import json
import time

# --- Configuration ---
# Must match the Django server address and the API endpoint defined in urls.py
API_URL = 'http://127.0.0.1:8000/api/chat/' 
# ---------------------

def send_message(message):
    """Sends a message to the Django/Chatterbot API and returns the response."""
    # Ensure a message is provided
    if not message:
        return ""

    try:
        # Prepare data as a JSON payload
        payload = {'message': message}
        
        # Send POST request
        response = requests.post(API_URL, json=payload, timeout=15) # Add a timeout
        response.raise_for_status() # Raises an exception for bad status codes (4xx or 5xx)
        
        # Parse the JSON response
        data = response.json()
        return data.get('response', '⚠️ Error: Bot response field missing in API data.')
        
    except requests.exceptions.ConnectionError:
        return "⚠️ Error: Could not connect to the API server. Is Django running?"
    except requests.exceptions.Timeout:
         return "⚠️ Error: Request timed out. The server took too long to respond."
    except requests.exceptions.RequestException as e:
        # Includes HTTPError, TooManyRedirects, etc.
        return f"⚠️ An API request error occurred: {e}"
    except json.JSONDecodeError:
        return "⚠️ Error: Failed to decode JSON response from the API."

def chat_client():
    """The main loop for the terminal chat client."""
    print("🤖 Terminal Chatterbot Client")
    print("-" * 35)
    print(f"Connecting to API at: {API_URL}")
    print("Type 'quit' or 'exit' to end the session.")
    print("-" * 35)
    
    while True:
        try:
            # Get user input
            user_input = input("You: ").strip()
            
            if user_input.lower() in ['quit', 'exit']:
                print("\nGoodbye! 👋")
                break
            
            if not user_input:
                continue

            # Display a typing indicator while waiting for the response
            print("Bot: Thinking...", end='\r')
            time.sleep(0.3) # Give it a moment to show "Thinking..."
            
            # Send the message and get the bot's reply
            bot_reply = send_message(user_input)
            
            # Clear the "Thinking..." line and print the final response
            print(" " * 30, end='\r') # Clear the previous line
            print(f"Bot: {bot_reply}")
            
        except KeyboardInterrupt:
            # Handle Ctrl+C to exit gracefully
            print("\n\nGoodbye! 👋")
            break

if __name__ == '__main__':
    chat_client()