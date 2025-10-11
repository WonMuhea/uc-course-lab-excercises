# Secure Web Application Demo: XSS and eval()

This project demonstrates two common web vulnerabilities (XSS and eval()) and their secure fixes.

## 🚀 Setup

1.  **Files:** Ensure you have the following three files in one folder:
    * `index.html` (The UI)
    * `script.js` (The Code)
    * `style.css` (The Styles)
2.  **Run:** Open a terminal in the project folder and run a simple HTTP server (Python recommended) to avoid browser security restrictions:
    ```bash
    python -m http.server 8000
    ```
3.  **Access:** Go to `http://localhost:8000/index.html` in your browser.

***

## 🛡️ Vulnerabilities and Fixes

### 1. Cross-Site Scripting (XSS)

| Vulnerability (😈) | Fix (✅) | Testing Method |
| :--- | :--- | :--- |
| **Vulnerable Code:** Uses **`.innerHTML`** to directly insert user input. | **Secure Fix:** Uses an **`escapeHtml()`** function to convert special characters (`<`, `>`, etc.) to harmless text entities, and renders output with **`.textContent`**. | Click **"😈 Run VULNERABLE XSS Demo"**. An **alert pop-up** confirms the script ran. Click **"✅ Run SECURE XSS Mitigation"**. The script text is displayed, but **no alert runs.** |
| **Issue:** Executes injected code like `<script>alert('...')</script>`. | **Mitigation:** The browser sees harmless text like `&lt;script&gt;...`. | |

### 2. Dynamic Code Evaluation (`eval()`)

| Vulnerability (😈) | Fix (✅) | Testing Method |
| :--- | :--- | :--- |
| **Vulnerable Code:** Uses **`eval()`** on user input. | **Secure Fix:** Implements a **strict Regex validator** that only allows digits and basic math operators (`0-9`, `+`, `-`, etc.) before evaluation. | The input field is pre-loaded with a malicious payload: `alert('Hacked! ... document.cookie')`. |
| **Issue:** Executes **any** JavaScript, including `document.cookie` or `alert()`. | **Mitigation:** **Blocks** any input containing restricted words or characters like `alert`, `document`, or letters. | Click **"😈 Run VULNERABLE eval() Demo"**. An **alert pop-up** confirms code execution. Click **"✅ Run SECURE Mitigation"**. The input is **rejected with an error message** due to unauthorized characters. |

***

## 🔬 Testing Malicious Payloads

The demo uses the following payloads to prove the vulnerability before implementing the secure fix:

| Vulnerability | Malicious Payload (Default Input) |
| :--- | :--- |
| **XSS** | `<img src=x onerror="alert('XSS Successful! Payload Executed')">` |
| **`eval()`** | `alert('Hacked! Running malicious code to steal cookies: ' + document.cookie);` |