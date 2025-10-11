# SQL Injection (SQLi) — Secure Online Payment System
---
This document covers the scope of SQL Injection protections for an online payment system. It focuses on preventing injection in endpoints that handle order creation(ProcessPayment), transaction searches(SearchTransactions), bulk fetch operations(BulkFetch). Protections include input validation, parameterized queries, safe handling of LIKE/IN/ORDER BY, use of stored procedures, least-privilege DB users, and deployment-level defenses (WAF, monitoring).

---

## Assumptions & Global Requirements

* **TLS (HTTPS)** enforced; **HSTS** enabled.
* App never stores raw PAN/CVV; uses **tokenization** via gateway.
* **Input validation & canonicalization** on server (type/length/range/whitelist).
* **Least-privilege** DB user; credentials loaded from a **secrets manager**.
* Pseudocode uses:

  * `DB_EXECUTE(sql, params)` → parameterized queries
  * `DB_CALL(proc, params)` → stored procedures
  * `ORM.*` → an ORM that parameterizes by default

---

## Protection Levels

### Level 1 — Basic

```pseudo
FUNCTION ProcessPayment_Basic(req):
    userId   = VALIDATE_UUID(req.body.userId)
    amount   = PARSE_DECIMAL(req.body.amount)
    IF amount <= 0: RETURN 400
    currency = WHITELIST(req.body.currency, ["USD","EUR","GBP"])

    sql = "INSERT INTO orders(user_id, amount, currency, card_token, description) VALUES (?, ?, ?, ?, ?)"
    DB_EXECUTE(sql, [userId, amount, currency, req.body.cardToken, req.body.description])

    RETURN 201
```

**Why:** Parameterized queries guarantee that parameters are interpreted as values (not SQL) and thus prevent SQL injection by avoiding concatenation. Minimal validation of the parameters to ensure type, length, whitelist, etc., prevents malformed SQL commands and reduces the attack surface but given these choices there are no protections against dynamic identifiers or complex query builders of this nature being built. 

**Limit:** This does not protect against dynamic identifiers (e.g. `ORDER BY`) or complex builders where the structure of the query changes based on input.

---

### Level 2 — Intermediate (Tricky cases: `LIKE`, `IN`, sorting)

```pseudo
FUNCTION SearchTransactions(req):
    term  = TRIM(req.query.q, maxLen=150)
    term  = ESCAPE_FOR_LIKE(term)                      // escape %, _, \
    limit = CLAMP(INT(req.query.limit), 1, 100)
    sql   = "SELECT id, amount, created_at FROM txns WHERE description ILIKE ? ESCAPE '\\' LIMIT ?"
    RETURN DB_EXECUTE(sql, ["%" + term + "%", limit])

FUNCTION BulkFetch(req):
    ids = PARSE_UUID_LIST(req.body.ids)                // validate each
    IF ids.empty: RETURN []
    placeholders = JOIN(",", REPEAT("?", LEN(ids)))    // e.g., "?,?,?"
    sql = "SELECT id, status FROM payments WHERE id IN (" + placeholders + ")"
    RETURN DB_EXECUTE(sql, ids)

FUNCTION SortedList(req):
    sort = WHITELIST(req.query.sort, ["created_at","amount","status"], default="created_at")
    dir  = WHITELIST(req.query.dir,  ["asc","desc"], default="desc")
    sql  = "SELECT id, amount FROM payments ORDER BY " + sort + " " + dir + " LIMIT ? OFFSET ?"
    RETURN DB_EXECUTE(sql, [limit, offset])
```

**Why:** Specifically handle the usual edge cases in injection by escaping user input in LIKE patterns, validating and constructing placeholders in IN lists so values can stay parameterized, and limiting any concatenated identifiers to a small whitelist. These focused measures prevent the subtle ways attackers exploit pattern matching, list expansion, and ORDER BY concatenation.

---

### Level 3 — Advanced (Stored procedures + least privilege)

```sql
-- DB layer (conceptual):
PROCEDURE sp_create_order(p_user UUID, p_amt NUMERIC, p_ccy TEXT, p_token TEXT, p_desc TEXT) RETURNS UUID
    INSERT INTO orders(user_id, amount, currency, card_token, description)
    VALUES (p_user, p_amt, p_ccy, p_token, p_desc)
    RETURN id;
```

```pseudo
FUNCTION ProcessPayment_Advanced(req):
    userId   = VALIDATE_UUID(req.body.userId)
    amount   = VALIDATE_AMOUNT(req.body.amount)
    currency = WHITELIST(req.body.currency)

    orderId = DB_CALL("sp_create_order", [userId, amount, currency, req.body.cardToken, req.body.description])

    resp = GATEWAY.charge(orderId, amount, currency, req.body.cardToken)
    IF resp.failed:
        DB_CALL("sp_mark_failed", [orderId, resp.code])
        RETURN 402

    DB_CALL("sp_mark_paid", [orderId, resp.authCode])
    RETURN 200
```

**Why:** App sends only parameters, DB enforces logic, minimized privileges. Transferring core sql into parameterized stored procedures eliminates the need for the application to create sql and centralizes the checking of permissions and data format at the database level. Combined with the practice of using least privilege database permissions, this reduces the application’s blast radius and attack surface in terms of privileges. Even if the application is compromised, the database user connected has only been granted access to certain operations. (Note: care must be taken to allow no dynamic sql code in dynamic statements or to use parameterized procedures on other internal dynamic sql code.)

---

### Level 4 — Defense-in-Depth (WAF, monitoring, ORM, secrets)

```pseudo
FUNCTION ProcessPayment_Defense(req):
    // Validation
    VALIDATE_INPUT(req)

    // ORM (prepared under the hood) + transaction
    TX_BEGIN()
      orderId = ORM.insert("orders").values({
        userId: req.user.id,
        amount: req.body.amount,
        currency: req.body.currency,
        cardToken: req.body.cardToken,
        description: req.body.description
      }).returning("id").exec()

      resp = GATEWAY.chargeMTLS(orderId, req.body.amount, req.body.currency, req.body.cardToken)

      ORM.insert("payments").values({ orderId, status: resp.status, authCode: resp.authCode }).exec()
    TX_COMMIT()

    // Observability & protections
    WAF.inspect(req)
    LOG_EVENT("payment", { user: req.user.id, amount: req.body.amount, status: resp.status })
    SIEM.alertOnAnomalyPatterns()

    RETURN 200
```

**Why:** Adds Web Application Firewall (WAF) rules, anomaly alerts, and secrets hygiene to app-level protections.By implementing these layered protections, a single control failure does not lead to exposure. Added ORMs and prepared statements to mitigate value injection, strict input validation and secrets hygiene to minimize misuse, transactional patterns for data integrity, WAF + SIEM for detection and automated mitigation of anomalous or malicious traffic. These factors will minimize many injection routes and give ops the means to quickly detect and respond to attacks.

---

## Summary of SQLi Scenarios and Fixes

| Scenario        | Insecure                             | Secure Fix                                                                     |
| --------------- | ------------------------------------ | ------------------------------------------------------------------------------ |
| Search (`LIKE`) | `"WHERE name LIKE '%" + term + "%'"` | Escape wildcards + param: `"WHERE name ILIKE ? ESCAPE '\\'"`, `["%"+term+"%"]` |
| Bulk `IN`       | `"WHERE id IN (" + ids + ")"`        | Validate list → placeholders: `"WHERE id IN (?,?,?)"`                          |
| Sort            | `"ORDER BY " + column`               | Whitelist `column` & `dir`; only concatenate whitelisted literals              |
| Auth lookup     | `"WHERE email='" + e + "'"`          | `WHERE email=?` with parameter                                                 |
| Pagination      | user values in LIMIT/OFFSET          | Validate/clamp integers; pass as parameters                                    |

---

## Combined: Payment Flow (SQLi-hardened)

```pseudo
FUNCTION ProcessPayment_Level4(req):
    // 1) Validate
    userId   = VALIDATE_UUID(req.body.userId)
    amount   = VALIDATE_AMOUNT(req.body.amount, min=0.50, max=10000.00, scale=2)
    currency = WHITELIST(req.body.currency, ["USD","EUR","GBP"])
    cardTok  = VALIDATE_TOKEN(req.body.cardToken, len=32..64)
    noteTxt  = TRIM(req.body.description, maxLen=200)

    // 2) Transaction + prepared queries
    TX_BEGIN()
      orderId = ORM.insert("orders").values({userId, amount, currency, cardToken:cardTok, description:noteTxt}).returning("id").exec()

      resp = GATEWAY.chargeMTLS(orderId, amount, currency, cardTok)
      IF resp.status != "APPROVED":
          ORM.insert("payments").values({orderId, status:"FAILED", code:resp.code}).exec()
          TX_ROLLBACK()
          RETURN JSON(402, { orderId, status: "DECLINED" })

      ORM.insert("payments").values({orderId, status:"APPROVED", authCode:resp.authCode}).exec()
    TX_COMMIT()

    RETURN JSON(200, { orderId, message: "Payment approved" })
```

---

## Operational Checklist (SQLi)

* Parameterize **every** query. **Never** concatenate user values.
* Escape wildcards for `LIKE`. Build placeholder lists for `IN`.
* Whitelist identifiers for `ORDER BY` and similar constructs.
* Use **least-privilege** DB accounts. Rotate credentials and secret manager.
* Apply Web Applicaiton Firewall (WAF) rules for SQLi. Log and alert on anomalies (excessive errors, obvious payloads).
* Unit test for validators. Integration test for query paths. Periodic penetration testing.
