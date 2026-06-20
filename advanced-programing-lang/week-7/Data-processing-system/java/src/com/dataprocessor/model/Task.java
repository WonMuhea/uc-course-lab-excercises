package com.dataprocessor.model;

/**
 * Represents a discrete unit of work to be processed by a worker thread.
 */
public class Task {
    private final int id;
    private final String data;

    public Task(int id, String data) {
        this.id = id;
        this.data = data;
    }

    public int getId() { return id; }
    public String getData() { return data; }
}