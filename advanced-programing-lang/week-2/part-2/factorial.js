function doFactorialCounter() {
    let count = 0; // Enclosed variable for closure

    return function factorial(n) {
        count++;
        if (n < 0) return 0;
        
        let result = 1;
        for (let i = 1; i <= n; i++) {
            result *= i;
        }
        return { result, count };
    };
}

// Usage
const factorialGen = doFactorialCounter();
const res1 = factorialGen(5);
console.log(`Result: ${res1.result}, Called: ${res1.count} times`); // Result: 120, Called: 1 times