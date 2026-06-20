package worker

import (
	"fmt"
	"go-processor/io"
	"go-processor/model"
	"math/rand"
	"os"
	"sync"
	"time"
)

// StartWorker executes an event loop pulling tasks out of a synchronized input channel.
func StartWorker(id int, taskQueue <-chan model.Task, writer *io.SafeWriter, wg *sync.WaitGroup, simulateErrors bool) {
	defer wg.Done() // Decrement WaitGroup counter when worker function leaves scope
	fmt.Printf("INFO: Worker %d started.\n", id)

	// Range block handles synchronization implicitly. It automatically blocks while empty,
	// extracts elements safely, and breaks out entirely once the channel closes.
	for task := range taskQueue {
		fmt.Printf("INFO: Worker %d processing Task %d\n", id, task.ID)

		// Core Requirement: Exception Handling Strategy
		// Prevents runtime errors from killing the entire thread context
		if simulateErrors && task.ID%5 == 0 {
			fmt.Fprintf(os.Stderr, "ERROR: Worker %d encountered processing error on Task %d\n", id, task.ID)
			continue // Safely skip to next entry
		}

		// Mock computational execution delay
		time.Sleep(time.Duration(rand.Intn(300)+100) * time.Millisecond)

		processedData := fmt.Sprintf("%s [PROCESSED]", task.Data)
		writer.WriteResult(id, task.ID, processedData)
	}

	fmt.Printf("INFO: Worker %d finished cleanly.\n", id)
}
