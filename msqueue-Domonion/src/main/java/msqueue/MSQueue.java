package msqueue;

import kotlinx.atomicfu.AtomicRef;

public class MSQueue implements Queue {
    private AtomicRef<Node> head;
    private AtomicRef<Node> tail;

    public MSQueue() {
        Node dummy = new Node();
        head = new AtomicRef<>(dummy);
        tail = new AtomicRef<>(dummy);
    }

    @Override
    public void enqueue(int x) {
        Node node = new Node();
        node.x = x;
        while (true) {
            Node tail = this.tail.getValue();
            if (tail.next.compareAndSet(null, node)) {
                this.tail.compareAndSet(tail, node);
                break;
            }
            else{
                this.tail.compareAndSet(tail, tail.next.getValue());
            }
        }
    }

    @Override
    public int dequeue() {
        int res;
        while (true) {
            Node head = this.head.getValue();
            Node tail = this.tail.getValue();
            Node next = head.next.getValue();
            if (head == this.head.getValue()) {
                if (head == tail) {
                    if (next == null) {
                        res = Integer.MIN_VALUE;
                        break;
                    }
                    this.tail.compareAndSet(tail, next);
                } else {
                    res = next.x;
                    if (this.head.compareAndSet(head, next)) {
                        break;
                    }
                }
            }
        }
        return res;
    }

    @Override
    public int peek() {
        Node next = this.head.getValue().next.getValue();
        if (next == null)
            return Integer.MIN_VALUE;
        return next.x;
    }

    private static class Node {
        Integer x;
        AtomicRef<Node> next;

        Node() {
            x = null;
            next = new AtomicRef<>(null);
        }
    }
}