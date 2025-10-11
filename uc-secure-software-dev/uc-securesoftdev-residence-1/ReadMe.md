# uc-securesoftdev-residence-1

## Project Overview

**uc-securesoftdev-residence-1** is a secure web application for managing user tasks, built with **Spring Boot**, **Spring Security**, and **JWT (JSON Web Tokens)**. The project showcases a robust, full-stack implementation of authentication, authorization, and data management. It provides a modern, stateless API backend that interacts with a dynamic single-page application (SPA) built with plain HTML and JavaScript. Key features include a sliding window rate limiter for abuse prevention and clear role-based access control for both standard users and administrators.

## Features

### Authentication and Security
*   **JWT-based Authentication:** Uses JWTs for stateless, token-based authentication.
*   **Access and Refresh Tokens:** Implements both short-lived access tokens and long-lived refresh tokens for enhanced security and a seamless user experience.
*   **HS512 Algorithm:** Uses the HS512 hashing algorithm for secure JWT signing.
*   **Logout Functionality:** Provides a secure logout mechanism that invalidates the user's refresh token on the server.
*   **Secure Access to Database:** Employs JPA to protect against SQL injection vulnerabilities.
*   **HTTPS Enabled:** Configured for secure communication over HTTPS, ensuring all data is encrypted during transit.

### Performance and Stability
*   **Sliding Window Rate Limiter:** Protects the API from abuse and overuse by limiting the number of requests per user over a specific time window. This is implemented using Redis for efficient, distributed rate limiting.

### User Management
*   **User Registration:** Allows new users to sign up for an account.
*   **User Login:** Provides a login form for returning users.
*   **Task Management (User):** Logged-in users can create and view their tasks.

### Admin Functionality
*   **Admin Access:** Securely restricts certain endpoints to users with the "ADMIN" role.
*   **User and Task Overview:** Provides a consolidated view for admins to see all users. An admin can then click a button next to each user to view that user's specific tasks in a modal window.

## User Interface (UI) Overview

### What a User Sees
*   **Registration Form:** An unauthenticated user sees a form to create a new account.
*   **Login Form:** An unauthenticated user sees a form to log in with an existing account.
*   **Task Management Section:** After logging in, a user sees:
    *   A welcome message.
    *   A list of their own tasks.
    *   A form to create a new task.
    *   A logout button.

### What an Admin Sees
*   **Admin User List:** After logging in, an admin sees:
    *   A welcome message.
    *   A list of all registered users.
    *   **User Task View:** Each user in the list has a "View Tasks" button. Clicking this button opens a modal showing a list of that specific user's tasks.
    *   A logout button.

## Getting Started

### Prerequisites
*   Java 17 or higher
*   Maven
*   Redis server instance
*   An IDE like IntelliJ IDEA or Visual Studio Code

### Running the Application

1.  **Clone the repository:**
    ```bash
    git clone [repository_url] uc-securesoftdev-residence-1
    ```
2.  **Navigate to the project directory:**
    ```bash
    cd uc-securesoftdev-residence-1
    ```
3.  **Build the project with Maven:**
    ```bash
    ./mvnw clean install
    ```
4.  **Start your Redis server.**
5.  **Run the Spring Boot application:**
    ```bash
    ./mvnw spring-boot:run
    ```

### Accessing the Application

*   **Frontend UI:** Open your web browser and navigate to `https://localhost:8443`.

### Initial Users

The application is pre-configured with a command-line runner to create an initial admin user and roles upon startup.

*   **Admin User:**
    *   **Username:** `admin`
    *   **Password:** `adminpass`

## API Endpoints

