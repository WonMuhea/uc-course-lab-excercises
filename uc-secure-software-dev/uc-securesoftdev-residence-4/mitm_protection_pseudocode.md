# Protections Against MITM — Pseudocode & Detailed Explanations

This companion file focuses on **defenses that specifically target man‑in‑the‑middle (MITM) attacks**, layered on top of a correct TLS 1.3 setup. Each pseudocode block is followed by deeper “Why” context and implementation notes.

---

## 1) Certificate Chain & Hostname Validation (Client)

```pseudocode
FUNCTION VERIFY_HOSTNAME(peer_cert, expected_hostname):
    names = EXTRACT_SAN_DNS_NAMES(peer_cert)
    IF names IS EMPTY:
        names = [EXTRACT_LEGACY_CN(peer_cert)]
    RETURN MATCH_EXPECTED_HOSTNAME(expected_hostname, names)  // RFC 6125 rules

FUNCTION CLIENT_CERT_VALIDATION(tls_conn, expected_hostname, trust_store):
    chain = tls_conn.peer_cert_chain

    IF NOT VALIDATE_CHAIN(chain, trust_store):
        RETURN false

    IF NOT VERIFY_HOSTNAME(chain[0], expected_hostname):
        RETURN false

    IF IS_EXPIRED_OR_NOT_YET_VALID(chain[0]):
        RETURN false

    RETURN true
```

### Why (Deep Rationale)
- **Chain validation** verifys the path to a trusted Root CA and check constraints to prevent self-signed or invalid certs.
- **Hostname verification** blocks an MITM using a certificate issued for a different domain (attacker.com) which avoids "valid cert, wrong site" attacks.
- **Time checks** maintain accurate system time (via NTP) to enforce validity windows and prevent replay attacks by avoiding accepting expired or future-dated certs. 

---

## 2) Certificate Pinning (Optional but Powerful)

```pseudocode
GLOBAL PINSET = {
    host: "api.example.com",
    pins: [
        "sha256/BASE64(SPKI_HASH_1)",
        "sha256/BASE64(SPKI_HASH_2)"
    ],
    max_age_days: 365
}

FUNCTION CHECK_PINNING(peer_cert, host):
    IF host != PINSET.host:
        RETURN true

    spki_hash = SHA256(EXTRACT_SPKI(peer_cert))
    b64 = BASE64(spki_hash)

    IF "sha256/" + b64 IN PINSET.pins:
        RETURN true
    ELSE:
        SECURE_LOG("Pinning failure for host", host)
        RETURN false
```

### Why
- **Defense against rogue CAs**: strictly requires a match to a known public key hash (SPKI) of a CA  to defend against a compromised or misissuing CA.
- **Operational guidance**: Pin the SPKI (key) for renewal flexibility. Crucially, include backup pins associated with unused keys to avoid locking out clients during an emergency key rotation. Only use for high-value applications you fully control.

---

## 3) Revocation Checks (Prefer OCSP Stapling)

```pseudocode
FUNCTION CHECK_REVOCATION(tls_conn):
    IF tls_conn.ocsp_response IS PRESENT:
        status = VERIFY_OCSP_RESPONSE(tls_conn.ocsp_response, tls_conn.peer_cert_chain)
        RETURN (status == "GOOD")
    ELSE:
        status = QUERY_OCSP_RESPONDER(tls_conn.peer_cert_chain)  // policy dependent
        RETURN (status == "GOOD")
```

### Why
- **Revoked certs** must be rejected. OCSP Stapling is the preferred method. It's low latency, privacy preserving, and proves the server is up to date.
- **Actionable advice**: Direct OCSP/CRL queries can be blocked by an MITM to force a soft fail. Servers should staple, and clients should implement a hard fail policy if revocation status is expected but unavailable.

---

## 4) Mutual TLS (mTLS)

```pseudocode
// Server configuration
cfg.client_auth = {
    mode: "REQUIRE",
    trust_store: LOAD_ENTERPRISE_CA_BUNDLE("/etc/ssl/clients_ca.pem"),
    verify_callback: OPTIONAL_CUSTOM_POLICY
}

FUNCTION HANDLE_CONNECTION(tcp_conn, cfg):
    tls_conn = TLS_ACCEPT(tcp_conn, cfg)
    IF NOT tls_conn.handshake_success:
        CLOSE(tcp_conn); RETURN

    IF NOT VALIDATE_CLIENT_CERT(tls_conn.peer_cert_chain, cfg.client_auth.trust_store):
        SECURE_LOG("Client cert invalid")
        CLOSE(tls_conn); RETURN

    // proceed...
```

### Why
- **Strong client authentication**: mTLS forces the client to prove its identity cryptographically with a private key.
- **MITM defense**: An attacker cannot impersonate an authorized client to the server, even witht a successful termination of the server side TLS connection. Because they lack the valid client certificate and key. Use for B2B APIs or internal services and manage client keys securely (HSM/TPM).

---

## 5) Downgrade & Fallback Resistance

```pseudocode
// Client
cfg.min_tls_version = "TLS1.3"
cfg.reject_legacy_renegotiation = true
cfg.enforce_hsts = true

// Server
cfg.min_tls_version = "TLS1.3"
cfg.disable_legacy_renegotiation = true
cfg.support_downgrade_protection = true
```

### Why
- **Block known exploits**: blocks insecure renegotiation and legacy protocols (SSLv3/TLS 1.0/1.1) to block an attacker from forcing a fallback to exploit weaknesses (e.g., POODLE, BEAST).
- **HSTS defense**: HTTP Strict Transport Security (HSTS)  forces browsers to use HTTPS exclusively to stops SSL Stripping by preventing attackers from intercepting initial http:// connections.
---

## 6) Time Sync & Clock Skew

```pseudocode
FUNCTION ENSURE_TIME_SYNC():
    if ABS(NOW() - NTP_TRUSTED_TIME()) > 5 minutes:
        RAISE "ClockSkewError"
```

### Why
- **Enforce validity**: Certificate validity checks are relative to the client's time. Significant clock skew can cause false acceptance of expired certs (time too far in the past) or future certs (time too far in the future).
- **Actionable advice**: Enforce time synchronization via a trusted protocol like NTP to mitigate this vulnerability, often found in isolated or captive networks.
- 
---

## 7) Secure DNS (Optional but Useful)

```pseudocode
ip = DNS_RESOLVE_SECURE("api.example.com")  // DoT/DoH
tcp_conn = TCP_CONNECT(ip, 443)
```

### Why
- **Reduces tampering risk**: DNS over TLS/HTTPS (DoT/DoH) encrypts the DNS query, making it harder for a local MITM to tamper with the response and redirect the client to malicious infrastructure.
- **Benefit**: Though TLS validation protects the final connection, secure DNS lowers the overall attack surface and redirection risks.
---

## 8) Error Handling & Fail‑Closed Policy

```pseudocode
FUNCTION FAIL_CLOSED(reason):
    SECURE_LOG("Connection aborted", reason)
    ABORT_CONNECTION()

IF NOT CLIENT_CERT_VALIDATION(tls_conn, expected_hostname, LOAD_SYSTEM_TRUST_STORE()):
    FAIL_CLOSED("Cert/Hostname validation failed")

IF NOT CHECK_PINNING(tls_conn.peer_cert, expected_hostname):
    FAIL_CLOSED("Pinning failed")

IF NOT CHECK_REVOCATION(tls_conn):
    FAIL_CLOSED("Revocation check failed")
```

### Why
- **Security over availability**: A fail-closed policy is mandatory. Any doubt about the peer's identity, the channel's integrity, or the failure of a required security check (validation, pinning, revocation) must abort the connection.
- **Avoid soft-fails**: Never silently ignore or allow users to click past TLS errors as this normalizes insecure behavior and creates exploitable gaps. Log failures for forensics.
---

## 9) End‑to‑End Flows (Putting It Together)

```pseudocode
FUNCTION SECURE_CLIENT_REQUEST(host, port, request):
    ENSURE_TIME_SYNC()

    tls_conn = CLIENT_CONNECT(host, port)

    IF NOT CLIENT_CERT_VALIDATION(tls_conn, host, LOAD_SYSTEM_TRUST_STORE()):
        CLOSE(tls_conn); RAISE "CertValidationError"

    IF NOT CHECK_PINNING(tls_conn.peer_cert, host):
        CLOSE(tls_conn); RAISE "PinningError"

    IF NOT CHECK_REVOCATION(tls_conn):
        CLOSE(tls_conn); RAISE "RevocationError"

    response = CLIENT_DO_REQUEST(tls_conn, request.method, request.path, request.headers, request.body)
    CLOSE(tls_conn)
    RETURN response
```

### Why
- **Defense in Depth**: This flow integrates all checks (authentication and freshness) before sending any sensitive data. It forces an attacker to simultaneously defeat multiple, independent security layers (CA trust, hostname match, pin match, revocation status).

---

## 10) Hardening Checklist (Quick Reference)

- Enforce **TLS 1.3**, AEAD suites; disable legacy renegotiation & compression.
- Use **OCSP stapling**; monitor revocation status.
- Consider **mTLS** for privileged APIs; store private keys securely (HSM/TPM).
- Consider **SPKI pinning** for high‑value domains, with backup pins.
- Implement strict **hostname verification** everywhere (CLI tools too).
- Prefer **secure DNS** (DoT/DoH), and validate DNSSEC where available.
- Implement **fail‑closed** behavior and actionable **security logging**.
- Rotate TLS **ticket keys**; renew server certs early; monitor unexpected cert changes.
