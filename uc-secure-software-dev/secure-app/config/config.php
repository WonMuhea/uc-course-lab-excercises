<?php
// config/config.php

// Path to .env (one level up from /config)
$envPath = dirname(__DIR__) . '/.env';
$env = [];

// Load .env if present
if (file_exists($envPath)) {
    $parsed = parse_ini_file($envPath, false, INI_SCANNER_TYPED);
    if (is_array($parsed)) {
        $env = $parsed;
    } else {
        error_log("Failed to parse .env file at {$envPath}");
    }
} else {
    error_log(".env file missing at {$envPath} — using built-in defaults");
}

// Read DB config from env or fallbacks (for dev only)
$db_host = $env['DB_HOST'] ?? '127.0.0.1';
$db_port = $env['DB_PORT'] ?? '3306';
$db_name = $env['DB_NAME'] ?? 'my_secure_db';
$db_user = $env['DB_USER'] ?? 'my_db_user';
$db_pass = $env['DB_PASS'] ?? 'supersecret_db_password';

$dsn = "mysql:host={$db_host};port={$db_port};dbname={$db_name};charset=utf8mb4";

$options = [
    PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    PDO::ATTR_EMULATE_PREPARES   => false,
];

try {
    $pdo = new PDO($dsn, $db_user, $db_pass, $options);

    // 🔐 Ensure the `users` table exists (idempotent & limited in scope)
    $createTableSql = "
        CREATE TABLE IF NOT EXISTS users (
            id INT AUTO_INCREMENT PRIMARY KEY,
            username VARCHAR(50) NOT NULL,
            email VARCHAR(100) NOT NULL,
            role VARCHAR(50) NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            INDEX idx_users_email (email)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
    ";
    $pdo->exec($createTableSql);

} catch (PDOException $e) {
    // Log detailed error on the server only
    error_log('DB connection error: ' . $e->getMessage());

    // Send generic error to client
    http_response_code(500);
    header('Content-Type: application/json');
    echo json_encode([
        'error' => 'Database connection failed',
    ]);
    exit;
}
