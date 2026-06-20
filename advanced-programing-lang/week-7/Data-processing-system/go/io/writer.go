package io

import (
	"fmt"
	"os"
	"sync"
)

// SafeWriter orchestrates mutually exclusive write access to a shared file descriptor.
type SafeWriter struct {
	file *os.File
	mu   sync.Mutex
}

// NewSafeWriter opens a handle to a file or returns a clean error if initialization fails.
func NewSafeWriter(filename string) (*SafeWriter, error) {
	file, err := os.Create(filename)
	if err != nil {
		return nil, err
	}
	return &SafeWriter{file: file}, nil
}

// WriteResult safely locks file resources across concurrently competing goroutines.
func (sw *SafeWriter) WriteResult(workerID int, taskID int, result string) {
	sw.mu.Lock()
	defer sw.mu.Unlock() // Guaranteed unlock pattern in Go

	logLine := fmt.Sprintf("[Worker %d] Task %d Result: %s\n", workerID, taskID, result)
	if _, err := sw.file.WriteString(logLine); err != nil {
		fmt.Fprintf(os.Stderr, "ERROR: [Worker %d] Error writing file: %v\n", workerID, err)
	}
}

// Close releases the underlying file system resource.
func (sw *SafeWriter) Close() error {
	return sw.file.Close()
}
