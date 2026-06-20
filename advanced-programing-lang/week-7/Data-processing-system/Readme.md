# Data Processing System Simulation

This project provides a robust, cross-platform simulation of concurrent data processing systems implemented in both **Java** and **Go**. Both versions feature a thread-safe task queue, pooled workers with simulated computational delays, thread-safe resource writing, and fault-tolerant exception handling designed to prevent deadlocks and data corruption under high concurrent load.

---

## System Architecture & Package Layout

To maintain clear separation of concerns, both codebases are modularized into domain models, input/output management, queue coordination, and orchestration layers.

## Local Running the apps for testing 

### Testing Java 
```bash
cd java/src

javac com/dataprocessor/model/Task.java \
      com/dataprocessor/queue/SharedTaskQueue.java \
      com/dataprocessor/io/ResultWriter.java \
      com/dataprocessor/Main.java

# High throughput test
java com.dataprocessor.Main -w 8 -t 100

#Exception handling test
java com.dataprocessor.Main -w 4 -t 15 -e

#I/O Failure test
java com.dataprocessor.Main -f "/invalid_directory/output.txt"

```

### Testing Go

```bash
cd go
go mod tidy

#High throughput test
go run main.go -w 12 -t 200

#Error handling test
go run main.go -w 3 -t 15 -e

#I/O error test
go run main.go -f "/sys/protected_root.txt"

```

