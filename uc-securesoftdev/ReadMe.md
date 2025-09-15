# 🛡️ Secure Spring Boot Demo

This is a sample web application demonstrating key Layer 7 security features and best practices using Spring Boot. This README outlines the application's security features and highlights how their implementation is verified.

## 🚀 Getting Started

### Prerequisites
*   Java 17 or higher
*   Maven

### Setup
1.  **Generate SSL Keystore:** Create a self-signed certificate for local HTTPS testing. Use the following command from the project's root directory to place the keystore directly into `src/main/resources`.
    ```shell
    keytool -genkeypair -alias my-key -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore src/main/resources/keystore.p12 -validity 3650 -dname "CN=localhost, OU=MyOrg, O=MyCompany, L=MyCity, S=MyState, C=US" -keypass password -storepass password
    ```
2.  **Run the application:**
    ```shell
    ./mvnw spring-boot:run
    ```
3.  **Access Homepage:** Open your browser to `https://localhost:8443/`. You will be redirected to the login page.

## ✅ How to Demo Security Features

### 1. User Login (Standard Form Login)
This test verifies that user authentication is working correctly, redirecting you to protected pages after a successful login.

1.  **Access the Login Page:** Navigate to `https://localhost:8443/login`.
2.  **Login with Admin Credentials:** The only user configured during startup is the admin. Use these admin details to log in.
    *   **Username:** `admin`
    *   **Password:** `addmin123`
    *   Click the "Login" button.
3.  **Expected Result:** You will be redirected to the admin dashboard at `/admin`, indicating a successful login.

### 2. Admin User Creation
The only user configured during startup is the admin. Use these admin credentials to log in and create new users.

1.  **Log in** with the admin credentials.
2.  **Access the Admin Dashboard:** You will be redirected to `/admin` upon login.
3.  **Create New User:** Use the form on the dashboard to create new users with their respective roles.
4.  **Verify New User:** Log out and log in with the new user's credentials to confirm their access rights (e.g., redirect to `/home` for a `ROLE_USER`).

### 3. Rate Limiter Test

#### Using Postman
This method uses Postman's Collection Runner to test the rate limit.

1. Before running the app, set the value of number of allowed requests per 60 sec to 5 in application.yml file (api.rate.limit property value)
2. **Create a Collection:** In Postman, create a new collection named "Rate Limiting Tests".
2.  **Add a Request:** Create a `GET` request to `https://localhost:8443/api/public/ping` and save it to the collection. In the request's **Settings**, disable **SSL certificate verification**.
3.  **Run the Collection:** Click the "Runner" button on the collection.
    *   Set **Iterations** to **10** or more.
    *   Set **Delay** to **0 ms**.
4.  **Analyze Results:** The Collection Runner will show `200 OK` for the first five requests and `429 Too Many Requests` for subsequent requests.

### 4. One-Time Encryption (via UI)
This test verifies that only authenticated users can use the encryption service, which is built into the user's home page.

1.  **Create a user** and log in with their credentials.
2.  **Navigate to the Home Page:** You should be automatically redirected to `/home`.
3.  **Enter text** into the "Enter text to encrypt:" text box.
4.  **Click "Encrypt".**
5.  **Expected Result:** The encrypted string will appear below the form in a blue alert box.

### 5. Session Fixation Test
1.  Open your browser's developer tools and view the application's cookies on the login page (`/login`).
2.  Log in as a user.
3.  Observe that the `JSESSIONID` cookie has changed after a successful login.

### 6. SQL Injection Test
1.  Log in to the application.
2.  Navigate to `https://localhost:8443/api/secure/user?username=user`. Confirm the API returns the user's data.
3.  Attempt a malicious query: `https://localhost:8443/api/secure/user?username={user}' OR 1=1 --`. The application should gracefully handle the request without returning unexpected data.
4.  Attempt a malicious query: `https://localhost:8443/api/unsafe/user?username={user}' OR 1=1 --`. The application should list all users which is why it is unsafe.

### 7. CSRF Protection Test
1.  As an authenticated user, navigate to `/home`.
2.  Use a browser's developer tools to copy the URL of the `/encrypt` endpoint and attempt to make a `POST` request without the CSRF token. The server will respond with a `403 Forbidden` error.

### 8. HTTPS Enforcement Test
1.  Try to access the application via the insecure HTTP port: `http://localhost:8080/`.
2.  Observe that your browser is automatically redirected to `https://localhost:8443/`.
