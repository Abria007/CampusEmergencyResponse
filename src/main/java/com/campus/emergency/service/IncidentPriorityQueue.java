
package com.campus.emergency.service;

import com.campus.emergency.model.Incident;

import java.util.Comparator;
import java.util.PriorityQueue;

public class IncidentPriorityQueue {

    private final PriorityQueue<Incident> queue;

    public IncidentPriorityQueue() {

        queue = new PriorityQueue<>(
                Comparator
                        .comparingInt(
                                (Incident incident) ->
                                        getPriorityValue(
                                                incident.getPriority()
                                        )
                        )
                        .thenComparingInt(Incident::getId)
        );
    }

    private int getPriorityValue(String priority) {

        if (priority == null) {
            return 5;
        }

        switch (priority.toUpperCase()) {
            case "CRITICAL":
                return 1;

            case "HIGH":
                return 2;

            case "MEDIUM":
                return 3;

            case "LOW":
                return 4;

            default:
                return 5;
        }
    }

    public void addIncident(Incident incident) {

        if (incident == null) {
            throw new IllegalArgumentException(
                    "Incident cannot be null."
            );
        }

        queue.offer(incident);
    }

    public Incident getNextIncident() {
        return queue.poll();
    }

    public Incident peekNextIncident() {
        return queue.peek();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }
}
