package com.dataprocessor;

import com.dataprocessor.model.Task;
import com.dataprocessor.queue.SharedTaskQueue;
import com.dataprocessor.io.ResultWriter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    
    /**
     * Thread execution unit that processes data pulled from the shared queue.
     */
    static class Worker implements Runnable {
        private final SharedTaskQueue queue;
        private final ResultWriter writer;
        private final boolean simulateErrors;

        public Worker(SharedTaskQueue queue, ResultWriter writer, boolean simulateErrors) {
            this.queue = queue;
            this.writer = writer;
            this.simulateErrors = simulateErrors;
        }

        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();
            System.out.println("INFO: " + threadName + " started.");
            try {
                while (true) {
                    Task task = queue.getTask();
                    if (task == null) break; // Poison-pill/shutdown signal received

                    System.out.println("INFO: " + threadName + " processing Task " + task.getId());
                    
                    // Core Requirement: Exception Handling Strategy
                    // Captures errors and isolates them to keep the thread alive for remaining work
                    if (simulateErrors && task.getId() % 5 == 0) {
                        System.err.println("ERROR: " + threadName + " encountered a simulated processing exception on Task " + task.getId());
                        continue; 
                    }

                    // Simulate processing workload delay
                    Thread.sleep((long) (Math.random() * 300) + 100); 
                    
                    String processedData = task.getData().toUpperCase() + " [PROCESSED]";
                    writer.writeResult(threadName, task.getId(), processedData);
                }
            } catch (InterruptedException e) {
                System.err.println("WARN: " + threadName + " interrupted explicitly.");
                Thread.currentThread().interrupt(); // Maintain interrupt flag status
            } finally {
                System.out.println("INFO: " + threadName + " finished cleanly.");
            }
        }
    }

    public static void main(String[] args) {
        int numWorkers = 4;
        int numTasks = 20;
        boolean simulateErrors = false;
        String outputFile = "java_results.txt";

        // Parse command line inputs
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-w": numWorkers = Integer.parseInt(args[++i]); break;
                case "-t": numTasks = Integer.parseInt(args[++i]); break;
                case "-e": simulateErrors = true; break;
                case "-f": outputFile = args[++i]; break;
                case "-h":
                case "--help":
                    printHelp();
                    return;
            }
        }

        System.out.println(String.format("INFO: Configuration -> Workers: %d | Tasks: %d | Errors Mode: %b | Output: %s", 
                numWorkers, numTasks, simulateErrors, outputFile));

        SharedTaskQueue taskQueue = new SharedTaskQueue();
        
        // Execute and handle system-level instantiation anomalies
        try (ResultWriter writer = new ResultWriter(outputFile)) {
            ExecutorService executor = Executors.newFixedThreadPool(numWorkers);

            // Populate the thread pool
            for (int i = 0; i < numWorkers; i++) {
                executor.submit(new Worker(taskQueue, writer, simulateErrors));
            }

            // Produce initial test payload strings
            for (int i = 1; i <= numTasks; i++) {
                taskQueue.addTask(new Task(i, "payload_" + i));
            }

            // Coordinated shutdown orchestration sequence
            taskQueue.shutdown(); 
            executor.shutdown();

            // Explicit timeout block avoiding indefinite app hanging
            if (!executor.awaitTermination(15, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            System.out.println("INFO: Java Processing Complete.");
        } catch (IOException e) {
            System.err.println("FATAL: Could not open file resource. Reason: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("FATAL: System orchestration interrupted.");
            Thread.currentThread().interrupt();
        }
    }

    private static void printHelp() {
        System.out.println("Java Data Processor CLI Options:");
        System.out.println("  -w <int>   Number of worker threads (default: 4)");
        System.out.println("  -t <int>   Number of tasks to generate (default: 20)");
        System.out.println("  -e         Simulate task processing exceptions (multiples of 5 will fail)");
        System.out.println("  -f <str>   Output file path (default: java_results.txt)");
    }
}