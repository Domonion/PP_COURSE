package linked_list_set;

import kotlinx.atomicfu.AtomicRef;

public class SetImpl implements Set {

    private static class Node {
        int key;
        AtomicRef<RefNode> next;

        Node(int x) {
            key = x;
            next = new AtomicRef<>(new RefNode());
        }

        Node(int x, Node _next) {
            key = x;
            next = new AtomicRef<>(new RefNode(_next));
        }
    }

    private static class RefNode {
        Node next;
        boolean removed;

        RefNode() {
            removed = false;
            next = null;
        }

        RefNode(Node _next) {
            next = _next;
            removed = false;
        }

        RefNode(boolean _removed) {
            next = null;
            removed = _removed;
        }

        RefNode(Node _next, boolean _removed) {
            next = _next;
            removed = _removed;
        }
    }

    private static class Window {
        Node l, r;

        Window(Node _cur, Node _next) {
            l = _cur;
            r = _next;
        }
    }

    private final Node head;

    SetImpl() {
        Node tail = new Node(Integer.MAX_VALUE);
        head = new Node(Integer.MIN_VALUE, tail);
    }


    /**
     * Returns the {@link Window}, where cur.x < x <= next.x
     */
    private Window findWindow(int key) {
        while (true) {
            boolean failed = false;
            Node before = head;
            Node now = before.next.getValue().next;
            while (now.key < key) {
                RefNode beforeRefNode = before.next.getValue();
                RefNode nowRefNode = now.next.getValue();
                if (beforeRefNode.removed || beforeRefNode.next != now) {
                    failed = true;
                    break;
                }
                if (nowRefNode.removed) {
                    RefNode newBeforeRefNode = new RefNode(nowRefNode.next, false);
                    if (!before.next.compareAndSet(beforeRefNode, newBeforeRefNode)) {
                        failed = true;
                        break;
                    }
                    now = newBeforeRefNode.next;
                } else {
                    before = now;
                    now = before.next.getValue().next;
                }
            }
            if (!failed) {
                RefNode beforeRefNode = before.next.getValue();
                RefNode nowRefNode = now.next.getValue();
                if (beforeRefNode.next != now) {
                    continue;
                }
                if (beforeRefNode.removed) {
                    continue;
                }
                if (nowRefNode.removed) {
                    RefNode newBeforeRefNode = new RefNode(nowRefNode.next, false);
                    before.next.compareAndSet(beforeRefNode, newBeforeRefNode);
                    continue;
                }
                return new Window(before, now);
            }
        }
    }

        @Override
        public boolean add ( int key){
            while (true) {
                Window w = findWindow(key);
                if (w.r.key == key) {
                    return false;
                }
                RefNode beforeRefNode = w.l.next.getValue();
                if (beforeRefNode.next != w.r || beforeRefNode.removed) {
                    continue;
                }
                Node node = new Node(key, w.r);
                RefNode newBeforeRefNode = new RefNode(node, false);
                if (w.l.next.compareAndSet(beforeRefNode, newBeforeRefNode)) {
                    return true;
                }
            }
        }

        @Override
        public boolean remove ( int key){
            while (true) {
                Window w = findWindow(key);
                if (w.r.key != key) {
                    return false;
                }
                RefNode nextRefNode = w.r.next.getValue();
                if (nextRefNode.removed) {
                    continue;
                }
                RefNode newRefNode = new RefNode(nextRefNode.next, true);
                if (w.r.next.compareAndSet(nextRefNode, newRefNode)) {
                    return true;
                }
            }
        }


        @Override
        public boolean contains ( int key){
            Window w = findWindow(key);
            return w.r.key == key;
        }
    }