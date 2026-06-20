package main

import (
	"flag"
	"fmt"
	"os"
	"sync"

	"go-processor/io"
	"go-processor/model"
	"go-processor/worker"
)

func main() {
	// Expose structured options via native Go flag package APIs
	numWorkers := flag.Int("w", 4, "Number of concurrent worker goroutines")
	numTasks := flag.Int("t", 20, "Number of tasks to generate")
	simulateErrors := flag.Bool("e", false, "Simulate exceptions/errors on tasks divisible by 5")
	outputFile := flag.String("f", "go_results.txt", "Output target file pathway")

	flag.Parse()

	fmt.Printf("INFO: Configuration -> Goroutines: %d | Tasks: %d | Errors Mode: %t | Output: %s\n",
		*numWorkers, *numTasks, *simulateErrors, *outputFile)

	// Buffered channel serves natively as our thread-safe concurrency queue
	taskQueue := make(chan model.Task, *numTasks)

	// Idiomatic Go error handling to prevent application runtime panic on system faults
	writer, err := io.NewSafeWriter(*outputFile)
	if err != nil {
		fmt.Fprintf(os.Stderr, "FATAL: Could not open file resource. Reason: %v\n", err)
		return
	}
	defer func() {
		if err := writer.Close(); err != nil {
			fmt.Fprintf(os.Stderr, "ERROR: Error closing file resource: %v\n", err)
		}
	}()

	// Orchestrates barrier synchronization for application termination lifecycle
	var wg sync.WaitGroup

	// Bootstrap worker routines
	for i := 1; i <= *numWorkers; i++ {
		wg.Add(1)
		go worker.StartWorker(i, taskQueue, writer, &wg, *simulateErrors)
	}

	// Dispatch payloads into the data stream
	for i := 1; i <= *numTasks; i++ {
		taskQueue <- model.Task{ID: i, Data: fmt.Sprintf("payload_%d", i)}
	}

	// Closing channel sends an implicit completion signal to all parsing listeners
	close(taskQueue)

	// Await completion response from execution dependencies
	wg.Wait()

	fmt.Println("INFO: Go Processing Complete.")
}
