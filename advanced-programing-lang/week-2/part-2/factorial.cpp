#include <iostream>
#include <functional>

auto doFactorialCounter() {
    int count = 0; // Captured by the lambda closure

    // We use a mutable lambda to allow modifying 'count'
    return [count](int n) mutable {
        count++;
        if (n < 0) return std::make_pair(0LL, count);
        
        long long result = 1;
        for (int i = 1; i <= n; ++i) {
            result *= i;
        }
        return std::make_pair(result, count);
    };
}

int main() {
    auto factorialGen = doFactorialCounter();
    auto res1 = factorialGen(5);
    std::cout << "Result: " << res1.first << ", Called: " << res1.second << " times\n";
    return 0;
}