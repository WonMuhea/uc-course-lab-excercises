#include <iostream>
#include <string>

struct NetworkSession {
    std::string session_id;
    NetworkSession(std::string id) : session_id(id) {
        std::cout << "[C++] Connection Opened: " << session_id << "\n";
    }
    ~NetworkSession() {
        std::cout << "[C++] Connection Closed (Freed): " << session_id << "\n";
    }
};

int main() {
    // Manually allocated on the heap
    NetworkSession* s1 = new NetworkSession("Session_A");
    NetworkSession* s2 = new NetworkSession("Session_B");

    delete s1; 
    return 0;
}