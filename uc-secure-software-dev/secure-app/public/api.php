<?php

header('Content-Type: application/json');

require __DIR__ . '/../config/config.php';

$method = $_SERVER['REQUEST_METHOD'];

/**
 * Helper: send a JSON error and exit
 */
function json_error(int $status, string $message): void {
    http_response_code($status);
    echo json_encode(['error' => $message]);
    exit;
}

/**
 * Helper: sanitize strings (trim + strip tags)
 */
function clean_string(?string $value): string {
    return trim(strip_tags((string)$value));
}

/**
 * Helper: validate user input; returns array [isValid, errorMessage]
 */
function validate_user_input(string $username, string $email, string $role): array {
    // Username: 3–32 chars, letters, digits, underscores only
    if ($username === '' || strlen($username) < 3 || strlen($username) > 32) {
        return [false, 'Username must be between 3 and 32 characters'];
    }
    if (!preg_match('/^[a-zA-Z0-9_]+$/', $username)) {
        return [false, 'Username may only contain letters, numbers, and underscores'];
    }

    // Email: basic validation + max length
    if ($email === '' || strlen($email) > 100) {
        return [false, 'Email is required and must be shorter than 100 characters'];
    }
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        return [false, 'Invalid email address'];
    }

    // Role: whitelist
    $allowedRoles = ['admin', 'editor', 'user'];
    if (!in_array($role, $allowedRoles, true)) {
        return [false, 'Invalid role'];
    }

    return [true, ''];
}

if ($method === 'GET') {
    // LIST USERS (read-only)
    try {
        $stmt = $pdo->query('SELECT id, username, email, role, created_at FROM users ORDER BY id DESC');
        $users = $stmt->fetchAll();

        echo json_encode([
            'success' => true,
            'data'    => $users,
        ]);
    } catch (PDOException $e) {
        error_log('DB query error (GET /users): ' . $e->getMessage());
        json_error(500, 'Internal server error');
    }
    exit;
}

if ($method === 'POST') {
    // CREATE USER
    $contentType = $_SERVER['CONTENT_TYPE'] ?? '';
    if (stripos($contentType, 'application/json') === false) {
        json_error(400, 'Content-Type must be application/json');
    }

    $rawBody = file_get_contents('php://input');
    $input = json_decode($rawBody, true);

    if (!is_array($input)) {
        json_error(400, 'Invalid JSON body');
    }

    $username = clean_string($input['username'] ?? '');
    $email    = clean_string($input['email'] ?? '');
    $role     = clean_string($input['role'] ?? '');

    [$valid, $errorMsg] = validate_user_input($username, $email, $role);
    if (!$valid) {
        json_error(400, $errorMsg);
    }

    try {
        $stmt = $pdo->prepare(
            'INSERT INTO users (username, email, role) VALUES (:username, :email, :role)'
        );
        $stmt->execute([
            ':username' => $username,
            ':email'    => $email,
            ':role'     => $role,
        ]);

        $id = (int)$pdo->lastInsertId();

        echo json_encode([
            'success' => true,
            'data'    => [
                'id'       => $id,
                'username' => $username,
                'email'    => $email,
                'role'     => $role,
            ],
        ]);
    } catch (PDOException $e) {
        error_log('DB insert error (POST /users): ' . $e->getMessage());
        json_error(500, 'Internal server error');
    }

    exit;
}

// Unsupported method
json_error(405, 'Method not allowed');
