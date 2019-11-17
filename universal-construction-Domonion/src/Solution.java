public class Solution implements AtomicCounter {
    private final Node head = new Node(0);
    private final ThreadLocal<Node> last = ThreadLocal.withInitial(() -> head);

    public int getAndAdd(int x) {
        Node node;
        int now;
        do {
            now = last.get().value;
            node = new Node(now + x);
            last.set(last.get().next.decide(node));
        } while (node != last.get());

        return now;
    }

    private static class Node {
        private final Consensus<Node> next = new Consensus<>();
        private final int value;

        private Node(int x) {
            this.value = x;
        }
    }
}