package constraintsolver.impl;
/**
 * DLinkedListNode class from package.
 * uk.ac.standrews.cs.cs2001.lecture10;
 */

public class DLinkedListNode {

    /**
     * Element which this node contains.
     */
    public BinaryTuple element;
    /**
     * Next node it points to.
     */
    public DLinkedListNode next;
    /**
     * Previous node it points to.
     */
    public DLinkedListNode previous;

    /**
     * DLinkedListNode constructor.
     * @param element element to be stored in the node.
     */
    DLinkedListNode(BinaryTuple element) {

        this(element, null, null);
    }

    /**
     * DLinkedListNode constructor.
     * @param element element to be stored in the node.
     * @param next next node it points to.
     * @param previous previous node it points to.
     */
    public DLinkedListNode(BinaryTuple element, DLinkedListNode next, DLinkedListNode previous) {

        this.element = element;
        this.next = next;
        this.previous = previous;
    }

}
