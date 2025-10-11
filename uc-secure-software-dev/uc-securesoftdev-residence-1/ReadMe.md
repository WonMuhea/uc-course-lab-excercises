# uc-securesoftdev-residence-1

## Project Overview

**uc-securesoftdev-residence-1** is a secure web application for managing user tasks, built with **Spring Boot**, **Spring Security**, and **JWT (JSON Web Tokens)**. The project features a single-page application (SPA) front end (`index.html`) that interacts with a RESTful API backend. It includes robust authentication, authorization, and rate-limiting mechanisms to ensure data security and application stability.

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
*   **User and Task Overview:** Provides a consolidated view for admins to see all users and their related tasks.

### Technology Stack
*   **Backend:** Spring Boot, Spring Security, Spring Data JPA, H2 Database, Lombok, JJWT, Redis (for rate limiting).
*   **Database:** H2 (in-memory) for development and easy setup.
*   **Frontend:** HTML, CSS (Bootstrap), JavaScript (pure).
*   **Build Tool:** Maven.

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

