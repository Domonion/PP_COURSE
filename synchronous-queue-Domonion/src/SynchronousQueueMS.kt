import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SynchronousQueueMS<E> : SynchronousQueue<E> {
    private enum class NodeType { ITEM, RESERVATION }
    private class Node<T>(myItem: T?, myContinuation: Continuation<Boolean>?, myType: NodeType) {
//        @Volatile
        var type: NodeType = myType
//        @Volatile
        var continuation: AtomicReference<Continuation<Boolean>?> = AtomicReference(myContinuation)
//        @Volatile
        var item: AtomicReference<T?> = AtomicReference(myItem)
//        @Volatile
        var next: AtomicReference<Node<T>?> = AtomicReference(null)
    }

    private val sentinel = Node<E>(null, null, NodeType.ITEM)
    private var head = AtomicReference<Node<E>>(sentinel)
    private var tail = AtomicReference<Node<E>>(sentinel)

    override suspend fun send(element: E) {
        while (true) {
            val t = tail.get()
            var h = head.get()
            if (h == t || t.type == NodeType.ITEM) {
                val n = t.next.get()
                if (t == tail.get()) {
                    if (n != null) {
                        tail.compareAndSet(t, n)
                    } else {
                        var offer: Node<E>? = null
                        val res = suspendCoroutine<Boolean> sc@{ cont ->
                            offer = Node(element, cont, NodeType.ITEM)
                            if (t.next.compareAndSet(n, offer)) {
                                tail.compareAndSet(t, offer)
                            } else {
                                cont.resume(false)
                                return@sc
                            }
                        }
                        if (res) {
                            h = head.get()
                            if (offer!! == h.next.get())
                                head.compareAndSet(h, offer)
                            return
                        }
                    }
                }
            } else {
                val n = h.next.get()
                if (t != tail.get() || h != head.get() || n == null) {
                    continue
                }
                val success = n.item.compareAndSet(null, element)
                head.compareAndSet(h, n)
                if (success) {
                    n.continuation.get()!!.resume(true)
                    return
                }
            }
        }
    }


    override suspend fun receive(): E {
        while (true) {
            val t = tail.get()
            var h = head.get()
            var offer: Node<E>? = null
            if (h == t || t.type == NodeType.RESERVATION) {
                val n = t.next.get()
                if (t == tail.get()) {
                    if (n != null) {
                        tail.compareAndSet(t, n)
                    } else {
                        val res = suspendCoroutine<Boolean> sc@{ cont ->
                            offer = Node(null, cont, NodeType.RESERVATION)
                            if (t.next.compareAndSet(n, offer)) {
                                tail.compareAndSet(t, offer)
                            } else {
                                cont.resume(false)
                                return@sc
                            }
                        }
                        if (res) {
                            h = head.get()
                            if (offer == h.next.get())
                                head.compareAndSet(h, offer)
                            return offer!!.item.get()!!
                        }
                    }
                }
            } else {
                val n = h.next.get()
                if (t != tail.get() || h != head.get() || n == null) {
                    continue
                }
                val element = n.item.get() ?: continue
                val success = n.item.compareAndSet(element, null)
                head.compareAndSet(h, n)
                if (success) {
                    if(n.type == NodeType.RESERVATION)
                        throw AssertionError("should be impossible, only item")
                    n.continuation.get()!!.resume(true)
                    return element
                }
            }
        }
    }
}
