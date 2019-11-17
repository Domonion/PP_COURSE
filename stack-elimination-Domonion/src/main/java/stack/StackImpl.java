package stack;

import kotlinx.atomicfu.AtomicArray;
import kotlinx.atomicfu.AtomicInt;
import kotlinx.atomicfu.AtomicIntArray;
import kotlinx.atomicfu.AtomicRef;

import java.util.Random;


public class StackImpl implements Stack {
    private static class Node {
        Node next;
        final int data;

        Node(int data, Node next) {
            this.next = next;
            this.data = data;
        }
    }

    private enum OperationType {
        PUSH, POP
    }

    private void SetThreadId(ThreadInfo info) {
        int nowCount = counter.getAndIncrement() % N;
        while (!threads.get(nowCount).compareAndSet(null, info)) {
            nowCount = counter.getAndIncrement() % N;
        }
        info.id = nowCount;
    }

    private static class ThreadInfo {
        int id;
        OperationType op;
        Node node;

        ThreadInfo(OperationType _op, Node _node) {
            id = -1;
            op = _op;
            node = _node;
        }
    }

    private static final int MAX_THREADS = 4;
    private static final int N = MAX_THREADS;
    private static final int SIZE = MAX_THREADS;
    private AtomicRef<Node> head = new AtomicRef<>(null);
    private AtomicArray<ThreadInfo> threads = new AtomicArray<>(N);
    private AtomicIntArray threadIds = new AtomicIntArray(SIZE);
    private Random randomer = new Random();
    private AtomicInt counter = new AtomicInt(0);

    public StackImpl() {
        for (int i = 0; i < N; i++) {
            threads.get(i).setValue(null);
        }
        for (int i = 0; i < SIZE; i++) {
            threadIds.get(i).setValue(-1);
        }
    }

    private void PerformOp(ThreadInfo myInfo) {
        while (!TryPlain(myInfo)) {
            boolean failed = false;
            SetThreadId(myInfo);
            int index = randomer.nextInt(SIZE);
            int anotherInfoId = threadIds.get(index).getValue();
            while (!threadIds.get(index).compareAndSet(anotherInfoId, myInfo.id)) {
                anotherInfoId = threadIds.get(index).getValue();
            }
            if (anotherInfoId != -1) {
                ThreadInfo anotherInfo = threads.get(anotherInfoId).getValue();
                if (anotherInfo != null && anotherInfo.id == anotherInfoId && anotherInfo.op != myInfo.op) {
                    if (threads.get(myInfo.id).compareAndSet(myInfo, null)) {
                        if (TryCollision(myInfo, anotherInfo)) {
                            return;
                        } else {
                            failed = true;
                        }
                    } else {
                        FinishCollision(myInfo);
                        return;
                    }
                }
            }
            if (!failed) {
                if (!threads.get(myInfo.id).compareAndSet(myInfo, null)) {
                    FinishCollision(myInfo);
                    return;
                }
            }
        }
    }

    private void FinishCollision(ThreadInfo myInfo) {
        if (myInfo.op == OperationType.POP) {
            myInfo.node = threads.get(myInfo.id).getValue().node;
            threads.get(myInfo.id).setValue(null);
        }
    }

    private boolean TryCollision(ThreadInfo p, ThreadInfo q) {
        if (p.op == OperationType.PUSH) {
            return threads.get(q.id).compareAndSet(q, p);
        } else {
            if (threads.get(q.id).compareAndSet(q, null)) {
                p.node = q.node;
                threads.get(p.id).compareAndSet(p, null);
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean TryPlain(ThreadInfo info) {
        Node current, next;
        if (info.op == OperationType.PUSH) {
            current = head.getValue();
            info.node.next = current;
            if (head.compareAndSet(current, info.node)) {
                return true;
            } else {
                info.node.next = null;
                return false;
            }
        } else {
            current = head.getValue();
            if (current == null) {
                info.node = null;
                return true;
            }
            next = current.next;
            if (head.compareAndSet(current, next)) {
                info.node = current;
                return true;
            } else {
                info.node = null;
                return false;
            }
        }
    }

    @Override
    public void push(int x) {
        ThreadInfo now = new ThreadInfo(OperationType.PUSH, new Node(x, null));
        PerformOp(now);
    }

    @Override
    public int pop() {
        ThreadInfo now = new ThreadInfo(OperationType.POP, null);
        PerformOp(now);
        return now.node == null ? Integer.MIN_VALUE : now.node.data;
    }
}
