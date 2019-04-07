package impl;


import impl.exception.QueueEmptyException;
import impl.exception.QueueFullException;

import java.util.Arrays;

/**
 * Simple implementation of IPriorityQueue using an array.
 */
public class ArrayPriorityQueue implements IPriorityQueue {
    private Comparable[] elements;
    private int current_size = 0;
    private int maxSize;

    /**
     * ArrayPriorityQueue constructor.
     * @param maxSize indicates the maximum possible size of the queue
     */
    public ArrayPriorityQueue(int maxSize) {
        this.maxSize = maxSize;
        elements = new Comparable[maxSize];
    }

    @Override
    public void enqueue(Comparable element) throws QueueFullException {

        if (current_size < maxSize) {
            if (current_size != 0 && element.getClass() != elements[current_size - 1].getClass()) {
                    throw new ClassCastException();
            }
            elements[current_size++] = element;
        }
        else {
            throw new QueueFullException();
        }

    }

    @Override
    public Comparable dequeue() throws QueueEmptyException {
        if (isEmpty()) {
            throw new QueueEmptyException();
        }
        else if (current_size == 1) {
            current_size--;
            return elements[0];
        }
        else {
            int index = 0;
            Comparable element = elements[0];
            for (int i = 1; i < current_size; i++) {

                if (elements[i].compareTo(element) > 0) {
                    index = i;
                    element = elements[i];
                }
            }
            current_size--;
            if (index < elements.length) {
                int value = index;
                while (value < elements.length - 1) {
                    elements[value] = elements[value + 1];
                    value++;
                }
            }

            return element;
        }
    }

    @Override
    public int size() {
        return current_size;
    }

    @Override
    public boolean isEmpty() {
        return current_size == 0;
    }

    @Override
    public void clear() {
        current_size = 0;
        Arrays.fill(elements, null);
    }


}
