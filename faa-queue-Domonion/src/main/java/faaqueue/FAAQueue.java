package faaqueue;

import kotlinx.atomicfu.*;

import static faaqueue.FAAQueue.Node.NODE_SIZE;


public class FAAQueue<T> implements Queue<T> {
    private static final Object DONE = new Object(); // Marker for the "DONE" slot state; to avoid memory leaks

    private AtomicRef<Node> head; // Head pointer, similarly to the Michael-Scott queue (but the first node is _not_ sentinel)
    private AtomicRef<Node> tail; // Tail pointer, similarly to the Michael-Scott queue

    public FAAQueue() {
        Node firstNode = new Node();
        head = new AtomicRef<>(firstNode);
        tail = new AtomicRef<>(firstNode);
    }

    @Override
    public void enqueue(T x) {
        while (true) {
            Node tail = this.tail.getValue();
            int enqIdx = tail.enqIdx.getAndIncrement();
            if (enqIdx >= NODE_SIZE) {
                if (tail.next.compareAndSet(null, new Node(x))) {
                    if (!this.tail.compareAndSet(tail, tail.next.getValue())) {
                        System.out.println(1);
                    }
                    return;
                }
            } else {
                if (tail.data.get(enqIdx).compareAndSet(null, x))
                    return;
            }
        }
    }

    @Override
    public T dequeue() {
        while (true) {
            Node head = this.head.getValue();
            if (head.isEmpty()) {
                Node headNext = head.next.getValue();
                if (headNext == null) {
                    return null;
                }
                this.head.compareAndSet(head, headNext);
            } else {
                int deqIdx = head.deqIdx.getAndIncrement();
                if (deqIdx >= NODE_SIZE) continue;
                Object res = head.data.get(deqIdx).getAndSet(DONE);
                if (res != null && res != DONE) {
                    return (T) res;
                }
            }
        }
    }

    static class Node {
        static final int NODE_SIZE = 2; // CHANGE ME FOR BENCHMARKING ONLY

        private AtomicRef<Node> next = new AtomicRef<Node>(null);
        private AtomicInt enqIdx = new AtomicInt(0); // index for the next enqueue operation
        private AtomicInt deqIdx = new AtomicInt(0); // index for the next dequeue operation
        private final AtomicArray<Object> data = new AtomicArray<>(NODE_SIZE);

        Node() {
            for (int i = 0; i < NODE_SIZE; i++) {
                this.data.get(i).setValue(null);
            }
        }

        Node(Object x) {
            this.enqIdx = new AtomicInt(1);
            this.data.get(0).setValue(x);
            for (int i = 1; i < NODE_SIZE; i++) {
                this.data.get(i).setValue(null);
            }
        }

        private boolean isEmpty() {
            return this.deqIdx.getValue() >= this.enqIdx.getValue() || this.deqIdx.getValue() >= NODE_SIZE;
        }
    }
}