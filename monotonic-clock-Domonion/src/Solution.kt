import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Solution : MonotonicClock {

    private var h1 by RegularInt(0)
    private var h2 by RegularInt(0)
    private var m1 by RegularInt(0)
    private var m2 by RegularInt(0)
    private var s1 by RegularInt(0)
    private var s2 by RegularInt(0)

    override fun write(time: Time) {

        h2 = time.d1
        m2 = time.d2
        s2 = time.d3

        s1 = time.d3
        m1 = time.d2
        h1 = time.d1

    }


    override fun read(): Time {
        val h3 = h1
        val m3 = m1
        val s3 = s1

        val s4 = s2
        val m4 = m2
        val h4 = h2

        val time1 = Time(h3, m3, s3)
        val time2 = Time(h4, m4, s4)
        if (time1.compareTo(time2) == 0) {
            return time1;
        } else {
            if (h3 == h4) {
                if (m3 == m4) {
                    return Time(h4, m4, s4)
                }
                return Time(h4, m4, 0)
            }
            return Time(h4, 0, 0)
        }
    }
}