# Secure Spring Boot Application

This is a sample project demonstrating a secure Spring Boot application with various security features.

## Features

*   **Secure Authentication:** Uses Spring Security with BCrypt password encoding.
*   **Role-Based Access Control:** Protects the `/admin` endpoint, accessible only to users with the `ADMIN` role.
*   **Rate Limiting:** Prevents abuse of public APIs using Bucket4j.
*   **Session Hijacking Protection:** Configured with session fixation protection and concurrent session control.
*   **Security Headers:** Implements crucial security headers like `X-Content-Type-Options` and `Permissions-Policy`.
*   **HTTPS Enforcement:** Redirects all HTTP traffic to a secure HTTPS connection.
*   **One-Time Encryption API:** A REST endpoint for authenticated users to perform one-time text encryption.

## Navigation

*   **Login Page:** Access the login form to authenticate.
*   **Home Page:** For authenticated users, use the one time encryption tool to encrypt your text.
*   **Admin Page:** For users with the `ADMIN` role.
*   **SQL Injection Demo:** Demonstrates the difference between a vulnerable and a secure endpoint.

## Getting Started

1.  Log in using the predefined credentials: `user`/`password` or `admin`/`password`.
2.  Explore the features by navigating to the links above.
