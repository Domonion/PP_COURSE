package dijkstra

import java.util.*
import java.util.concurrent.Phaser
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.Comparator
import kotlin.concurrent.thread

//
//class MyNode {
//    var node: Node? = null
//    var distance: Int = Int.MAX_VALUE
//}
private val NODE_DISTANCE_COMPARATOR = Comparator<Node> { o1, o2 -> Integer.compare(o1!!.distance, o2!!.distance) }
//
//class BinaryPQ {
//    private val arr = Array(10000 * 4) { MyNode() }
//    var size = 0
//    val lock = ReentrantLock()
//
//    fun add(value: Node, dst: Int) {
//        arr[size].distance = dst
//        arr[size++].node = value
//        lift(size - 1)
//    }
//
//    fun peek(): MyNode {
//        return arr[0]
//    }
//
//    fun extractMin(): MyNode {
//        size--
//        val res = arr[0]
//        swap(0, size)
//        fall()
//        return res
//    }
//
//    private fun swap(a: Int, b: Int) {
//        val buff = arr[a]
//        arr[a] = arr[b]
//        arr[b] = buff
//    }
//
//    private fun fall() {
//        var ind = 0
//        while (ind * 2 + 2 < size) {
//            ind = if (arr[ind * 2 + 1].distance < arr[ind * 2 + 2].distance) {
//                if (arr[ind * 2 + 1].distance < arr[ind].distance) {
//                    swap(ind, ind * 2 + 1)
//                    ind * 2 + 1
//                } else {
//                    break
//                }
//            } else {
//                if (arr[ind * 2 + 2].distance < arr[ind].distance) {
//                    swap(ind, ind * 2 + 2)
//                    ind * 2 + 2
//                } else {
//                    break
//                }
//            }
//        }
//        if (ind * 2 + 1 < size) {
//            if (arr[ind * 2 + 1].distance < arr[ind].distance) {
//                swap(ind, ind * 2 + 1)
//            }
//        }
//    }
//
//    private fun lift(index: Int) {
//        var ind = index
//        while (ind != 0 && arr[(ind - 1) / 2].distance > arr[ind].distance) {
//            swap((ind - 1) / 2, ind)
//            ind = (ind - 1) / 2
//        }
//    }
//}

class ConcurrentPQ(workers: Int) {

    private val arr = Array(workers * 2) { PriorityQueue<Node>(NODE_DISTANCE_COMPARATOR) }
    private val locks = Array(workers * 2) { ReentrantLock() }

    private fun tryLock(index: Int, lambda: () -> Unit) {
        if (locks[index].tryLock()) {
            lambda()
            locks[index].unlock()
        }
    }

    fun add(value: Node) {
        var done = false
        while (!done) {
            val index = ThreadLocalRandom.current().nextInt(arr.size)
            tryLock(index) {
                arr[index].add(value)
                done = true
            }
        }
    }

    fun get(): Node? {
        var res: Node? = null
        val ind1 = ThreadLocalRandom.current().nextInt(arr.size)
        val ind2 = (ind1 + ThreadLocalRandom.current().nextInt(arr.size - 1)) % arr.size
        tryLock(ind1) {
            tryLock(ind2) {
                if (arr[ind1].size != 0 || arr[ind2].size != 0) {
                    var first: Node? = null
                    var second: Node? = null
                    if (arr[ind1].size > 0)
                        first = arr[ind1].peek()
                    if (arr[ind2].size > 0)
                        second = arr[ind2].peek()
                    res = if (first != null && second != null) {
                        if (first.distance < second.distance) {
                            arr[ind1].poll()
                        } else {
                            arr[ind2].poll()
                        }
                    } else {
                        if (first != null) {
                            arr[ind1].poll()
                        } else {
                            arr[ind2].poll()
                        }
                    }
                }
            }
        }
        return res
    }
}

// Returns `Integer.MAX_VALUE` if a path has not been found.
fun shortestPathParallel(start: Node) {
    val workers = Runtime.getRuntime().availableProcessors()
    // The distance to the start node is `0`
    start.distance = 0
    // Create a priority (by distance) queue and add the start node into it
    val q = ConcurrentPQ(workers)
    q.add(start)
    // Run worker threads and wait until the total work is done
    val onFinish = Phaser(workers + 1) // `arrive()` should be invoked at the end by each worker
    val activeNodes = AtomicInteger(0)
    activeNodes.incrementAndGet()
    repeat(workers) {
        thread {
            while (true) {
                val cur = q.get() ?: if (activeNodes.get() == 0) break else continue
                val dist = cur.distance
//                if (dist == cur.distance) {
                for (e in cur.outgoingEdges) {
                    while (true) {
                        val eDist = e.to.distance
                        if (eDist > dist + e.weight) {
                            if (e.to.casDistance(eDist, dist + e.weight)) {
                                q.add(e.to)
                                activeNodes.incrementAndGet()
                                break
                            }
                        } else {
                            break
                        }
                    }
                }
//                }


                activeNodes.decrementAndGet()
            }
            onFinish.arrive()
        }
    }
    onFinish.arriveAndAwaitAdvance()
}