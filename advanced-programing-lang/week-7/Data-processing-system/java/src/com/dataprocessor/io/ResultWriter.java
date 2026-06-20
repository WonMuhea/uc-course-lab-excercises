package com.dataprocessor.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe file writer wrapper that prevents multi-threaded interleaving 
 * or corruption when saving job outputs.
 */
public class ResultWriter implements AutoCloseable {
    private final BufferedWriter writer;
    private final ReentrantLock fileLock = new ReentrantLock();

    public ResultWriter(String filePath) throws IOException {
        this.writer = new BufferedWriter(new FileWriter(filePath));
    }

    /**
     * Atomically appends a processed task record to the output log.
     */
    public void writeResult(String threadName, int taskId, String result) {
        fileLock.lock();
        try {
            writer.write(String.format("[%s] Task %d Result: %s\n", threadName, taskId, result));
            writer.flush();
        } catch (IOException e) {
            System.err.println("ERROR: [" + threadName + "] File I/O Error: " + e.getMessage());
        } finally {
            fileLock.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }
}