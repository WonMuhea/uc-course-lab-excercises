# Ride-Sharing Persistent System 

A terminal-based ride-sharing ecosystem tracking riders, drivers, and polymorphic ride calculations. This repository contains twin implementations written in both **C++** and **GNU Smalltalk**.


## How to Run the C++ Version

### Prerequisites
* A standard C++ compiler supporting **C++17** or newer (`g++` or `clang++`).

### Compilation
Navigate into your C++ folder and build the application binary using your terminal:
```bash
cd cpp_version
g++ -std=c++17 main.cpp Driver.cpp Rider.cpp StandardRide.cpp PremiumRide.cpp StorageManager.cpp -o RideShareApp
```

### Execution
Run the compiled executable application:
```bash
./RideShareApp
```

---

## How to Run the GNU Smalltalk Version

### Prerequisites
* **GNU Smalltalk (GST)** engine framework environment installed on your host system.

### Verification Check
Ensure you clear out any older incompatible or half-formed JSON cache files from previous testing:
```bash
rm -f system_state.json
```

### Execution
Navigate to the root directory where `main.st` is located and load the application script file:
```bash
cd smalltalk_version
gst main.st
```

---

## 📝 Features Checklist Matrix

* **Polymorphism**: Computes Tier Fares (`Standard` vs `Premium`) dynamically at runtime.
* **Regex Engine Validation**: Verifies street addresses strictly follow the format: `Street Name, ST 12345`.
* **State Persistence**: Serializes runtime memory object collections into a flat layout local JSON database.
* **Cross-Language Parity**: Business rules match identically across both languages.
