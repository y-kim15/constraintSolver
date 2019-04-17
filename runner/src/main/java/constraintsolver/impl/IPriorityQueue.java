package constraintsolver.impl;
/**
 * Simple priority queue interface.
 * 
 */
interface IPriorityQueue {

    /**
     * Adds an element to the queue.
     * 
     * @param element the element to be queued
     */
    void enqueue(Comparable element);

    /**
     * Removes the largest element.
     * 
     * @return the element removed
     *
     */
    Comparable dequeue();

    /**
     * Returns the number of elements in the queue.
     * @return the number of elements in the queue
     */
    int size();

    /**
     * Checks whether the queue is empty.
     * @return true if the queue is empty
     */
    boolean isEmpty();

    /**
     * Removes all elements from the queue.
     */
    void clear();
}
