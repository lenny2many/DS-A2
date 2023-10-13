package util;

import java.lang.Math;

public class LamportClock {
    public enum EventType {
        CONCURRENT,
        BEFORE,
        AFTER
    }    

    private int timestamp;

    public LamportClock() {
        this.timestamp = 0;
    }

    /**
     * Increments the Lamport timestamp by one due to a local event.
     */
    public synchronized void tick() {
        this.timestamp++;
    }

    /**
     * Adjusts the Lamport timestamp based on a received message's timestamp.
     *
     * @param incomingTimestamp The timestamp of a received message.
     */
    public synchronized void updateTime(int incomingTimestamp) {
        this.timestamp = Math.max(this.timestamp, incomingTimestamp) + 1;
    }

    /**
     * Retrieves the current value of the Lamport timestamp.
     *
     * @return Current Lamport timestamp value.
     */
    public synchronized int peekTime() {
        return this.timestamp;
    }

    public synchronized EventType processReceivedTimestamp(int receivedTimestamp) {
        int currentTimestamp = peekTime();

        if (receivedTimestamp <= currentTimestamp) {
            tick(); // Increment for the received event
            return EventType.CONCURRENT;
        } else {
            updateTime(receivedTimestamp);
            return EventType.AFTER;
        }
    }
}