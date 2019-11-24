# Universal construction
## Task

Implement counter with atomic increment with lock-free universal construction base on consensus object.

## Build and tests
To check solution run:
* `./gradlew build` for Linux or MacOS
* `gradlew build` for Windows
 
Tests:
* [`CorrectnessTest`](test/CorrectnessTest.kt) checks sequential consistency.
* [`LinearizabilityTest`](test/LinearizabilityTest.kt) checks linearizability.
* [`LockFreedomTest`](test/LockFreedomTest.kt) checks lock-freedom.