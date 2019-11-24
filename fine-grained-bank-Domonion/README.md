# Synchronization with fine-grained locking
## Build and tests
For test use `mvn test`. This automatically runs:

* `FunctionalTest.java` correctness in single-threaded environment
* `MTStressTest.java` correctness in multi-threaded environment
* `LinearizabilityTest.java` linearizability of operations