from django.shortcuts import render

from rest_framework.views import APIView
from rest_framework.response import Response
from chatterbot import ChatBot
from chatterbot.trainers import ChatterBotCorpusTrainer
import os

# --- CHATTERBOT INITIALIZATION ---
# Initialize the bot outside of the view to prevent re-initialization on every request.
# Note: For production, a more robust persistence/singleton pattern is recommended.

# Configure the storage adapter to use a local file for the SQLite database
# The database file will be stored in the project root directory
BOT_DB_PATH = os.path.join(os.getcwd(), 'chatterbot_db.sqlite3')

try:
    chatbot = ChatBot(
        'TerminalBot',
        storage_adapter='chatterbot.storage.SQLStorageAdapter',
        database_uri=f'sqlite:///{BOT_DB_PATH}'
    )

    # Initial Training (Run this once on startup or manually)
    print("Initializing and training Chatterbot...")
    trainer = ChatterBotCorpusTrainer(chatbot)
    trainer.train(
        'chatterbot.corpus.english'
    )
    print("Chatterbot training complete.")

except Exception as e:
    print(f"Error initializing Chatterbot: {e}")
    chatbot = None # Set to None if initialization fails

# ---------------------------------


class ChatView(APIView):
    """
    API endpoint to handle user messages and return bot responses.
    """
    def post(self, request, *args, **kwargs):
        if not chatbot:
             return Response({'error': 'Chatbot failed to initialize on the server.'}, status=503)

        user_input = request.data.get('message')
        
        if not user_input:
            return Response({'error': 'No message provided in the request payload.'}, status=400)
        
        try:
            # Get response from Chatterbot
            bot_response = str(chatbot.get_response(user_input))
            
            # Return the bot's response
            return Response({'response': bot_response})
            
        except Exception as e:
            # Handle potential Chatterbot or database errors
            return Response({'error': f'An internal server error occurred during response generation: {e}'}, status=500)
