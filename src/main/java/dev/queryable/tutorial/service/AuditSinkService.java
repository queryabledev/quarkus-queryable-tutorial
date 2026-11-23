package dev.queryable.tutorial.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.queryable.runtime.QueryableOperation;

@ApplicationScoped
public class AuditSinkService {
    private final List<String> events = new CopyOnWriteArrayList<>();

    public void add(QueryableOperation operation, String id) {
        events.add(operation + ":" + id);
    }

    public List<String> events() {
        return List.copyOf(events);
    }

    public void clear() {
        events.clear();
    }
}
