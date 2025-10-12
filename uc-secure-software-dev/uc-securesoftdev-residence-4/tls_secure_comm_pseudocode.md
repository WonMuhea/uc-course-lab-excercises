# Secure Communication with TLS 1.3 — Pseudocode (Client & Server)

This file focuses on the **core TLS 1.3 setup and use** for a client–server system. Each pseudocode block is immediately followed by a **deep “Why”** section that explains security rationale, pitfalls, and trade-offs.

## 1) Server Boot & TLS Configuration

```pseudocode
FUNCTION SERVER_BOOT():
    cfg = NEW TlsServerConfig()
    cfg.min_tls_version = "TLS1.3"
    cfg.max_tls_version = "TLS1.3"

    cfg.cipher_suites = [
        TLS_AES_128_GCM_SHA256,
        TLS_AES_256_GCM_SHA384
    ]

    (cfg.cert_chain, cfg.private_key) = LOAD_X509_CHAIN_AND_KEY("/etc/ssl/server.crt", "/etc/ssl/server.key")
    ASSERT KEY_MATCHES_CERT(cfg.cert_chain, cfg.private_key)

    cfg.ocsp_stapling = ENABLE_IF_AVAILABLE("/etc/ssl/ocsp.der")
    cfg.session_resumption = ENABLE_TICKET_KEYS(ROTATE_EVERY=24h, KEEP_PREV_KEYS=48h)
    cfg.alpn_protocols = ["h2", "http/1.1"]

    cfg.hsts = { enabled: true, max_age: 31536000, include_subdomains: true }  // HTTPS endpoints

    LISTEN_SOCKET = TCP_LISTEN(ADDR="0.0.0.0:443", BACKLOG=1024)

    WHILE TRUE:
        conn = TCP_ACCEPT(LISTEN_SOCKET)
        SPAWN THREAD HANDLE_CONNECTION(conn, cfg)
```

### Why (Deep Rationale)
- **TLS 1.3 only**: Mandatory security baseline. This eliminates legacy protocol risks and all known vulnerabilities from older versions (TLS 1.0/1.1/1.2). It also enforces Perfect Forward Secrecy (PFS) via ephemeral key exchange, ensuring past traffic remains safe even if the server's long term key is later stolen.
- **AEAD ciphers (AES-GCM)**: Provides Authenticated Encryption with Associated Data, combining Confidentiality and Integrity in a single primitive. This avoids the fragility and padding oracle attacks (like POODLE) inherent in older CBC modes without Encrypt-then-MAC (EtM).
- **Cert & Key sanity check**: A crucial operational step to prevent misconfiguration (e.g., using the wrong private key for a certificate) and guard against the accidental reuse of unauthorized keys.
- **OCSP stapling**: Improves client revocation reliability by allowing the server to attach a fresh, CA-signed status. This minimizes latency and prevents a major privacy leak (exposing client browsing history) that occurs when clients must check the CA directly.
- **Session resumption & Ticket key rotation**: Enables highly efficient 1-RTT or 0-RTT reconnects. Regular key rotation (24h) limits the time window for an attacker to compromise the master ticket-encryption key, thus containing the security risk if a ticket is compromised.
- **Application Layer Protocol Negotiation (ALPN)**: allows authenticated agreement on the application protocol (HTTP/2, HTTP/1.1) within the TLS handshake. This prevents protocol confusion and enables high-performance protocols without extra network round-trips.
- **HTTP Strict Transport Security (HSTS)**:  is a vital defense. The long max_age forces compliant browsers to use HTTPS on repeat visits, actively mitigating SSL stripping attacks where an attacker downgrades the connection to plaintext HTTP.
- **Listen/accept loop**: Standard server pattern used for fault tolerance and resource isolation. Isolating each connection to a thread ensures that a failure in one connection (e.g., a buggy client or DoS attempt) doesn not crash the entire service.

---

## 2) Server Connection Handler (Handshake → App Protocol)

```pseudocode
FUNCTION HANDLE_CONNECTION(tcp_conn, cfg):
    tls_conn = TLS_ACCEPT(tcp_conn, cfg)

    IF NOT tls_conn.handshake_success:
        SECURE_LOG("TLS handshake failed", REASON=tls_conn.error)
        CLOSE(tcp_conn)
        RETURN

    proto = tls_conn.selected_alpn
    IF proto == "h2":
        HTTP2_SERVE(tls_conn)
    ELSE:
        HTTP1_SERVE(tls_conn)
```

### Why
- **Fail closed**: it is a core security principle. If the handshake fails, the server must immediately terminate. Never attempt to "fallback" to plaintext or weaker protocols as this creates a downgrade oracle that an active attacker can exploit.
- **ALPN branching**: since the protocol negotiation is authenticated under the TLS session keys, the server can trust the result. This prevents protocol smuggling or misrouting by ensuring the server handles traffic correctly based on the agreed-upon application protocol.

---

## 3) Client TLS Setup & Connection

```pseudocode
FUNCTION CLIENT_CONNECT(server_hostname, server_port):
    cfg = NEW TlsClientConfig()
    cfg.min_tls_version = "TLS1.3"
    cfg.max_tls_version = "TLS1.3"
    cfg.cipher_suites = [TLS_AES_128_GCM_SHA256, TLS_AES_256_GCM_SHA384]

    cfg.trust_store = LOAD_SYSTEM_TRUST_STORE()
    cfg.enable_ocsp_stapling_check = true
    cfg.alpn_protocols = ["h2", "http/1.1"]

    tcp_conn = TCP_CONNECT(server_hostname, server_port, TIMEOUT=10s)

    tls_conn = TLS_CONNECT(tcp_conn, cfg, SNI=server_hostname)

    IF NOT tls_conn.handshake_success:
        SECURE_LOG("Handshake failed", REASON=tls_conn.error)
        CLOSE(tcp_conn)
        RAISE "TLSHandshakeError"

    IF NOT VERIFY_HOSTNAME(tls_conn.peer_cert, expected_hostname=server_hostname):
        SECURE_LOG("Hostname mismatch")
        CLOSE(tls_conn)
        RAISE "HostnameVerificationError"

    RETURN tls_conn
```

### Why
- **Server Name Indication (SNI)**: This binds the handshake to the intended hostname, which is necessary for virtual hosting. Sending SNI ensures the CDN or server presents the correct certificate, avoiding "wrong-host" failures.
- **System trust store**: Minimizes custom CA mistakes and leverages the platform's audited roots and built-in revocation mechanisms, ensuring a high degree of trust.
- **Hostname verification**: This is the most critical client-side check. It prevents accepting a valid certificate that was issued for a different domain (attacker.com). Many historical MITM incidents exploit missing or incorrect hostname checks.
- **No silent fallback**: Consistent with the server's "fail closed" principle. Any TLS error must result in an immediate, loud abort to prevent active downgrade attacks to plaintext or weaker TLS versions.
  
---

## 4) Application Data Exchange (Encrypted)

```pseudocode
FUNCTION CLIENT_DO_REQUEST(tls_conn, method, path, headers, body):
    request = HTTP_BUILD_REQUEST(method, path, headers, body)
    TLS_WRITE(tls_conn, request)

    response = TLS_READ(tls_conn, TIMEOUT=10s)
    RETURN PARSE_HTTP_RESPONSE(response)
```

### Why
- **Confidentiality & Integrity**: All application data is protected by the AEAD cipher, ensuring attackers can neither read nor undetectably modify traffic. Sequence numbers and per record nonces prevent replay and reordering attacks within the session.
- **Timeouts**: Enforcing timeouts is a defense against Denial of Service (DoS) and resource exhaustion. It ensures connections are released promptly instead of being held open indefinitely by a slow or malicious MITM.

---

## 5) Session Resumption (and Thoughts on 0‑RTT)

```pseudocode
FUNCTION CLIENT_TRY_RESUMPTION(server_identity):
    ticket = SESSION_TICKET_CACHE.GET(server_identity)
    IF ticket IS NULL:
        RETURN FULL_HANDSHAKE()

    tls_conn = TLS_CONNECT_WITH_TICKET(server_identity, ticket)
    IF tls_conn.resumed:
        RETURN tls_conn
    ELSE:
        RETURN FULL_HANDSHAKE()
```

### Why
- **Performance with safety**: Resumption significantly cuts latency by eliminating the expensive key exchange and certificate steps while preserving PFS. Ticket rotation and cache scoping limit the potential value and lifetime of a compromised ticket.
- **0‑RTT caution**: Zero Round-Trip Time (0-RTT) provides maximum speed but lacks full anti-replay guarantees at the TLS layer. Therefore, 0-RTT data must be restricted to idempotent operations (GET, idempotent PUT) and strictly disabled for high-risk, non-idempotent endpoints (POST requests) to prevent transaction duplication from replay attacks.

---

## 6) Clean Shutdown

```pseudocode
FUNCTION CLOSE_SECURELY(tls_conn):
    TRY TLS_CLOSE_NOTIFY(tls_conn)   // send close_notify alert
    FINALLY CLOSE_UNDERLYING_SOCKET(tls_conn)
```

### Why
- **close_notify** close_notify prevents truncation attacks. Since the close_notify alert is authenticated by the session keys, it signals a cryptographically verified end-of-stream. Explicitly sending this message prevents an attacker from simply closing the underlying TCP socket to prematurely cut off the data stream without the application noticing.
