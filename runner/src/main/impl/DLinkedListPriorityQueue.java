
import impl.exception.QueueEmptyException;

/**
 * A simple Priority Queue implemented using doubly linked list data structure.
 * code from the package uk.ac.standrews.cs.cs2001.lecture10;
 */
public class DLinkedListPriorityQueue implements impl.IPriorityQueue {
    private DLinkedListNode head = new DLinkedListNode(new BinaryTuple(-1,0));//"Head");
    private DLinkedListNode tail = new DLinkedListNode(new BinaryTuple(-1,1));//"Tail");
    private int current_size;

    /**
     * DoublyLinkedListPriorityQueue constructor.
     */
    public DLinkedListPriorityQueue() {
        System.out.println("initialise");
        head.previous = tail.next;
        head.next = tail.previous;
        current_size = 0;
    }

    @Override
    public void enqueue(Comparable element) {
        System.out.println("enqueue elements " + ((BinaryTuple) element).toString());
        BinaryTuple bt = (BinaryTuple) element;
        DLinkedListNode new_node = new DLinkedListNode(bt);
        if (isEmpty()) {
            new_node.previous = head;
            new_node.next = tail;
            head.next = new_node;
            tail.previous = new_node;

        }
        else {
            Class classOfElement = bt.getClass();
            if (!classOfElement.isInstance(head.next.element)) {
                throw new ClassCastException();
            }
            DLinkedListNode last_node = tail.previous;
            new_node.previous = last_node;
            new_node.next = tail;
            tail.previous = new_node;
            last_node.next = new_node;

        }
        current_size++;
    }

    public void blockEnqueue(DLinkedListPriorityQueue q1){
        DLinkedListNode start = q1.head.next;
        while(start != q1.tail){
            enqueue(start.element);
            start = start.next;
        }
    }

    @Override
    public Comparable dequeue() throws QueueEmptyException {
        if (isEmpty()) {
            throw new QueueEmptyException();
        }
        else if (current_size == 1) {
            BinaryTuple object = head.next.element;
            clear();
            return object;
        }
        else {
            DLinkedListNode current = head.next;
            BinaryTuple object = current.element;

            DLinkedListNode found = head.next;
            while (current != tail.previous) {
                if (current.next.element.compareTo(object) > 0) {
                    found = current.next;
                    object = current.next.element;
                }
                current = current.next;
            }
            object = found.element;

            if (found == tail.previous) {
                found.previous.next = tail.previous;
            }
            if (found == head.next) {
                DLinkedListNode after = found.next;
                after.previous = head;
                head.next = after;
            }
            else {
                found.previous.next = found.next;
                found.next.previous = found.previous;
            }
            current_size--;
            return object;
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
        head.previous = tail.next;
        head.next = tail.previous;
        current_size = 0;
    }
}
