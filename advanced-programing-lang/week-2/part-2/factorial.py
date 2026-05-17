def do_factorial_counter():
    count = 0  # Enclosed variable for closure
    
    def factorial(n: int) -> int:
        nonlocal count
        count += 1
        
        if n < 0: 
            return 0
        result = 1
        for i in range(1, n + 1):
            result *= i
        return result, count
        
    return factorial

# Usage
fact_gen = do_factorial_counter()
res1, c1 = fact_gen(5)
print(f"Result: {res1}, Called: {c1} times") # Result: 120, Called: 1 times