# Cross-Site Scripting (XSS) — Secure Online Payment System
---
This document covers the scope of Cross-Site Scripting (XSS) protections for an online payment system. It focuses on preventing script injection in customer-facing and admin UIs, including receipt pages, gift messages, admin notes, dashboards, and any UI that renders user-supplied content. Protections include template auto-escaping, context-aware encoding, server-side sanitization for allowed HTML, strict CSP, secure cookies, and client-side safe DOM APIs.

---

## Assumptions & Global Requirements

* **TLS (HTTPS)** enforced; **HSTS** enabled.
* **Secure headers** (global): strict **CSP**, X-Frame-Options, X-Content-Type-Options, Referrer-Policy.
* **HttpOnly + Secure + SameSite** cookies.
* **Tokenization** for card data; never render secrets in DOM.
* Templates auto-escape by default; raw HTML only from **server-sanitized** sources.

---

## Protection Levels

### Level 1 — Basic (Auto-escape + safe DOM sinks)

```pseudo
FUNCTION ViewReceipt_Basic(order):
    // Template auto-escapes:
    // <p>Note: {{ order.description }}</p>
    RETURN RENDER_TEMPLATE("receipt", { order })

// Client-side safe write:
SELECT("#note").textContent = apiData.note  // never innerHTML for untrusted input
```

**Why:** Auto escaping within templates guarantees that user supplied data (such as an order description or note) is considered just text, not executable HTML or JavaScript code. Such encoding eradicates special characters such as `<`, `>`, and `"` so that they do not terminate HTML contexts and inject scripts. The use of `.textContent` rather than `.innerHTML` in the DOM prevents inadvertent HTML parsing, an important vector of XSS when untrusted input (such as customer comments or refund reasons) is displayed. In an online payment form, this would prevent an attacker injecting malicious code like `<script>` or `<img onerror>` payloads into customer receipts or confirmation pages that could steal session cookies or card-related session tokens.
**Limit:** It doesn’t support intentional rich HTML fields.

---

### Level 2 — Intermediate (Allow limited HTML + CSP)

```pseudo
FUNCTION SaveGiftMessage(req):
    raw = req.body.giftMessage
    // Server-side sanitize (allow only a tiny tag set)
    safeHtml = HTML_SANITIZE(raw, policy={allowTags:["b","i","a"], allowAttrs:{a:["href","rel","target"]}})
    STORE(orderId, "giftMessage", safeHtml)

FUNCTION ViewGiftMessage(order):
    // Intentionally render sanitized HTML (engine-specific raw slot)
    RETURN RENDER_TEMPLATE("gift", { giftMessage: order.giftMessage })

// Global header:
SET_HEADER("Content-Security-Policy", "default-src 'self'; script-src 'self'; object-src 'none'")
```

**Why:** Sanitize on write. Some user interface fields (such as gift messages and product reviews) might intentionally allow rich text. By sanitizing HTML at write time, only safe tags and attributes are permitted, with anything that could bring a script or event handler (`onclick`, `onerror`, etc) stripped out. By validating attributes (for example, `href`), dangerous protocols such as javascript and data are prevented from execution. Adding a Content Security Policy (CSP)further restricts the realms from which scripts can be loaded. These combined means of prevention limit both the injection of scripts as well as the execution of them thus ensuring safe interactivity whilst providing the user with the convenience of rich text in messages greeting on the confirmation payment pages.
**Note:** Validate URLs to block `javascript:` and unsafe `data:` URIs.

---

### Level 3 — Advanced (Contextual encoders + nonce CSP)

```pseudo
FUNCTION RenderAdmin_Advanced(order):
    nonce = GENERATE_CRYPTO_NONCE()
    SET_HEADER("Content-Security-Policy", "default-src 'self'; script-src 'nonce-" + nonce + "'; base-uri 'self'; frame-ancestors 'none'")

    jsSafeNote = ENCODE_FOR_JS_STRING(order.customerNote)  // JS string context
    attrSafe   = HTML_ATTR_ENCODE(order.customerName)      // HTML attribute context

    // Template (conceptual):
    // <script nonce="{{nonce}}">const note = "{{jsSafeNote}}";</script>
    // <div title="{{attrSafe}}"></div>

    RETURN RENDER_TEMPLATE("admin", { nonce, jsSafeNote, attrSafe })
```

**Why:** Use the correct encoder for each sink (HTML text, attribute, JS string, URL). Nonce-based CSP whitelists only your inline scripts.
Context-aware encoding guarantees that every untrusted value is encoded according to the location of insertion such as HTML body, attribute or JavaScript string. Different escaping strategies are required for different contexts to guarantee that one does not escape into executable script blocks. For instance, in a JavaScript string, `'` and `"` are encoded to prevent premature string termination. In an HTML attribute, `onmouseover` or `src` attributes could be injected. In text nodes, element injection via `<` and `>` is avoided. The nonce based CSP allows execution of scripts that have a valid random nonce for that particular response. As a result injected or modified inline scripts are prevented from running even if the attacker somehow manipulates content. This is a critical defence from attacks against admin dashboards which tend to have sensitive customer notes, refunds and operational data.

---

### Level 4 — Defense-in-Depth (Strict CSP, HttpOnly cookies, reporting)

```pseudo
FUNCTION RenderReceipt_Defense(order):
    safeName = HTML_ENCODE(order.customerName)
    safeAmt  = HTML_ENCODE(STRING(order.amount))
    safeMsg  = order.safeMessageHtml   // sanitized when created

    SET_HEADER("Content-Security-Policy",
      "default-src 'none'; script-src 'self' nonce-{N}; connect-src 'self'; img-src 'self' data:; style-src 'self'; base-uri 'self'; frame-ancestors 'none'")
    SET_HEADER("X-Content-Type-Options", "nosniff")
    SET_HEADER("X-Frame-Options", "DENY")
    SET_COOKIE("session", value, { HttpOnly:true, Secure:true, SameSite:'Strict' })

    RETURN RENDER_TEMPLATE("receipt_defense", { safeName, safeAmt, safeMsg, nonce:N })
```

**Why:** This level of control uses multiple layers for full-stack protection.

> Strict CSP blocks all unauthorized types and sources of resources, thereby reducing the effective attack surface of the browser.
> HttpOnly cookies protect against theft of session identifiers via injected scripts.
> Secure + SameSite cookies ensure they are sent over HTTPS only, and not leaked through cross-site requests.
> X-Frame-Options protects against clickjacking by preventing framing of the payment user interface.
> X-Content-Type-Options: nosniff makes it impossible for browsers to interpret files served with the wrong MIME type as scripts.

All these mechanisms combine to ensure that even if a sanitisation or encoding error is made, the path of attack is still blocked. Rogue scripts cannot be loaded, data cannot be exfiltrated, nor can payment sessions be hijacked.

---

## Common XSS Scenarios & Fixes

| Scenario             | Insecure                  | Secure Fix                                                        |
| -------------------- | ------------------------- | ----------------------------------------------------------------- |
| Echo query to page   | `<div>{{ q }}</div>`      | `HTML_ENCODE(q)` or return JSON and render with `.textContent`    |
| Client DOM insertion | `el.innerHTML = userText` | `el.textContent = userText` (or sanitize then insert)             |
| Embed in JS          | `var a = '<?= v ?>';`     | `JS_STRING_ENCODE(v)`                                             |
| Attribute value      | `<div title="<?= v ?>">`  | `HTML_ATTR_ENCODE(v)`                                             |
| Allowed rich HTML    | raw store + render        | **Sanitize on write** (server) + strict CSP + cautious raw render |

---

## Combined Level-4 Rendering Flow (XSS-hardened)

```pseudo
FUNCTION ViewReceipt_Level4(orderIdFromUrl):
    // 1) Validate input & fetch
    orderId = VALIDATE_UUID(orderIdFromUrl)
    order   = ORM.select("orders").where({id:orderId}).first()

    // 2) Prepare safe fields
    safeOrderId = HTML_ENCODE(order.id)
    safeAmount  = HTML_ENCODE(STRING(order.amount))
    safeNote    = HTML_ENCODE(order.description)        // or pre-sanitized HTML if allowed

    // 3) Strict headers & cookies are global
    // 4) Render with auto-escaping; only raw-slot sanitized HTML if explicitly allowed
    RETURN RENDER_TEMPLATE("receipt", { orderId: safeOrderId, amount: safeAmount, note: safeNote })
```

---

## Operational Checklist (XSS)

* Auto-escape templates by default; **never** disable globally.
* Client: prefer `.textContent`; avoid `innerHTML` for untrusted data.
* If rich HTML is required: **sanitize on write** (server), validate URLs, and keep library updated.
* Use **strict CSP**; prefer nonce-based policies; collect CSP violation reports.
* Cookies: **HttpOnly + Secure + SameSite=Strict**; never store secrets in the DOM.
* Avoid embedding sensitive data in pages or scripts; fetch via JSON as needed.
* Test: unit tests for encoders; fuzz XSS payloads; periodic pentests.
