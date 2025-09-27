# chatbot_api/urls.py

from django.urls import path
from .views import ChatView

urlpatterns = [
    # The client will POST to http://127.0.0.1:8000/api/chat/
    path('chat/', ChatView.as_view(), name='chat'),
]