// Get input elements once the script loads
const xssInput = document.getElementById('xss-input');
const evalInput = document.getElementById('eval-input');

// =========================================================
// SECTION 1: XSS VULNERABILITY AND MITIGATION
// =========================================================

/**
 * Function to perform HTML escaping (the core mitigation for XSS).
 * Converts characters like <, >, and " into their HTML entities.
 */
function escapeHtml(str) {
    if (typeof str !== 'string') return '';
    return str.replace(/[&<>"']/g, function(match) {
        switch (match) {
            case '&': return '&amp;';
            case '<': return '&lt;';
            case '>': return '&gt;';
            case '"': return '&quot;';
            case "'": return '&#39;';
            default: return match;
        }
    });
}

// 😈 VULNERABLE FUNCTION (XSS)
function runVulnerableXSS() {
    const userInput = xssInput.value;
    const outputDiv = document.getElementById('vulnerable-xss-output');
    
    // ⚠️ VULNERABILITY: Direct assignment to .innerHTML executes any script tags.
    outputDiv.innerHTML = `You entered: ${userInput}`;
    
    console.log('Vulnerable XSS executed. Check for alert box.');
}

// ✅ SECURE FUNCTION (XSS Mitigation)
function runSecureXSS() {
    const userInput = xssInput.value;
    const outputDiv = document.getElementById('secure-xss-output');
    
    // 🛡️ MITIGATION: HTML-escape the input first.
    const safeInput = escapeHtml(userInput);
    
    // 🛡️ BEST PRACTICE: Use .textContent instead of .innerHTML to render the output 
    // strictly as text, ensuring the browser doesn't interpret it as HTML code.
    outputDiv.textContent = `You entered: ${safeInput}`; 
    
    console.log('Secure XSS executed. The script payload should be visible as harmless text.');
}


// =========================================================
// SECTION 2: EVAL() VULNERABILITY AND MITIGATION
// =========================================================

// 😈 VULNERABLE FUNCTION (eval)
function runVulnerableEval() {
    const userInput = evalInput.value;
    const outputDiv = document.getElementById('vulnerable-eval-output');
    
    // ⚠️ VULNERABILITY: Use of eval() executes arbitrary JavaScript code from the input.
    try {
        const result = eval(userInput); 
        outputDiv.textContent = `Result: ${result}`;
    } catch (e) {
        outputDiv.textContent = `Error: ${e.message}`;
    }
}

// ✅ SECURE FUNCTION (eval() Mitigation)
function runSecureEval() {
    const userInput = evalInput.value;
    const outputDiv = document.getElementById('secure-eval-output');

    // 🛡️ MITIGATION 1: Input Validation
    // Strict regex to allow ONLY numbers, basic arithmetic operators, and parentheses.
    // This blocks dangerous commands like "alert" or "document.cookie".
    const safeRegex = /^[0-9+\-*/().\s]+$/;

    if (!safeRegex.test(userInput)) {
        outputDiv.textContent = "Error: Input contains unauthorized characters. Only simple math is allowed. Like 2 + 2 or (3 * 4) - 5.";
        return;
    }

    // 🛡️ MITIGATION 2: Avoid eval(). Use a safer method.
    try {
        // Use new Function() only after strict validation. In a real project, use a dedicated parser library!
        const result = new Function('return ' + userInput)(); 
        outputDiv.textContent = `Result (Secured): ${result}`;
    } catch (e) {
        outputDiv.textContent = `Error: ${e.message}`;
    }
}