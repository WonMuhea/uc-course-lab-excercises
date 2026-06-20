# Data Processing System Simulation

This project provides a robust, cross-platform simulation of concurrent data processing systems implemented in both **Java** and **Go**. Both versions feature a thread-safe task queue, pooled workers with simulated computational delays, thread-safe resource writing, and fault-tolerant exception handling designed to prevent deadlocks and data corruption under high concurrent load.

---

## 📂 System Architecture & Package Layout

To maintain clear separation of concerns, both codebases are modularized into domain models, input/output management, queue coordination, and orchestration layers.

### Java Package Structure
```text
src/
└── com/
    └── dataprocessor/
        ├── Main.java               # CLI parsing, thread pool orchestration, and Worker definition
        ├── model/
        │   └── Task.java           # Simple immutable data encapsulation object
        ├── io/
        │   └── ResultWriter.java   # Thread-safe file writer protected by a ReentrantLock
        └── queue/
            └── SharedTaskQueue.java # Thread-safe bounded queue utilizing Condition signaling variables
```

```bash
cd java-processor/src

javac com/dataprocessor/model/Task.java \
      com/dataprocessor/queue/SharedTaskQueue.java \
      com/dataprocessor/io/ResultWriter.java \
      com/dataprocessor/Main.java
```
