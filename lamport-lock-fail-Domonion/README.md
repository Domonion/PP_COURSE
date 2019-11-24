# Wrong Lamport's algorithm

Here is wrong Lamport's algorithm.

```java
threadlocal int id       // 0..N-1 -- threadId
shared      int label[N] // zeros 

def lock:
  1: my = 1 // currentId
  2: for k in range(N): if k != id:
  3:     my = max(my, label[k] + 1) 
  4: label[id] = my 
  5: for k in range(N): if k != id:
  6:     while true: 
  7:         other = label[k] 
  8:         if other == 0 or (other, k) > (my, id): break@6 

def unlock:
  9: label[id] = 0
```

## Task

1. Show wrong execution of ```2``` threads that breaks mutual-exclusion.

 Execution's description should be placed [here](execution) and contain a number of events in the following grammar:
 ```
 <tid> <line> <action> <location> <value>
 ```
 * `<tid>` - threadId, `0` or `1`.
 * `<line>` - execution line; `3`, `4` or `7`;
 * `<action>` - event type, `rd` (read) or `wr` (write)
 * `<location>` - variable description, which is accessed, `label[<index>]`;
 * `<value>` - value read or writted, for example, `10`.

 To check correctness, run `./gradlew run` from the root.