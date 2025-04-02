package com.mischenkov.lightsout.entity;

public final class PriorityPosition extends Position implements Comparable<PriorityPosition> {

    private final int priority;

    public PriorityPosition(Position position, int priority) {
        super(position.x(), position.y());
        this.priority = priority;
    }

    @Override
    public int compareTo(PriorityPosition o) {
        return o.priority - this.priority;
    }

    public int getPriority() {
        return priority;
    }
}
