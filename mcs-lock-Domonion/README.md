# MCS Lock
## Build and tests
To check solution run:
* `./gradlew build` for Linux or MacOS
* `gradlew build` for Windows
 
Tests:
* [`СodeTest`](test/CodeTest.kt) checks codestyle. 
* [`CorrectnessTest`](test/CorrectnessTest.kt) checks correctness for park/unpark methods.
* [`StressTest`](test/StressTest.kt) checks mutual-exclusion and performance. 