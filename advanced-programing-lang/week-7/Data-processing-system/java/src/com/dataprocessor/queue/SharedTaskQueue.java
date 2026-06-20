package com.dataprocessor.queue;

import com.dataprocessor.model.Task;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A thread-safe FIFO queue designed to coordinate workload distribution 
 * between data producers and consumer threads using explicit locks.
 */
public class SharedTaskQueue {
    private final Queue<Task> queue = new LinkedList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private boolean isShutdown = false;

    /**
     * Appends a task to the queue and signals any waiting threads.
     */
    public void addTask(Task task) {
        lock.lock();
        try {
            if (isShutdown) {
                throw new IllegalStateException("Queue is shutdown.");
            }
            queue.add(task);
            notEmpty.signal(); // Awake a single thread waiting on an empty queue
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves and removes the head task of the queue. Blocks the calling thread 
     * if the queue is empty, until a task is added or the queue shuts down.
     * * @return The next Task, or null if the queue is shut down and empty.
     */
    public Task getTask() throws InterruptedException {
        lock.lock();
        try {
            // Guard against spurious wakeups while waiting for data
            while (queue.isEmpty() && !isShutdown) {
                notEmpty.await();
            }
            // Gracefully terminate worker threads if no work remains
            if (queue.isEmpty() && isShutdown) {
                return null;
            }
            return queue.poll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Initiates orderly closure of the queue. No new tasks can be added, 
     * and waiting threads are notified to finish draining remaining work.
     */
    public void shutdown() {
        lock.lock();
        try {
            isShutdown = true;
            notEmpty.signalAll(); // Wake up all waiting workers to allow clean exit
        } finally {
            lock.unlock();
        }
    }
}