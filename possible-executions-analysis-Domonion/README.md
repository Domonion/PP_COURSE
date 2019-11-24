# Every possible execution analysis

##Task

In this task you should analyze every execution order in next simple program.

```
shared int a = 0, b = 0

== Thread P: ==
while true:
  1: a = 1
  2: while b != 0: pass // do nothing
  3: pass // critical section, do nothing
  4: a = 0

== Thread Q: ==
while true:
  1: b = 1
  2: if a == 0: break // to line 4
  3: b = 0
4: stop // outside of loop
```

Write in the [solution.txt](solution.txt) every program state in the next gramar:
 * States defined as `[Px,Qy,a,b]` where `x` is **P** line, `y` is **Q** line,
   `a` is value of a, `b` is value of b. For example, initial state is
   `[P1,Q1,0,0]`. 
 * Every line should define transition `<state1> -> <state2>`. For example, 
   execution of first line **P** from initial states writes as `[P1,Q1,0,0] -> [P2,Q1,1,0]`. 

## Build and tests
 
To test correctness run `./gradlew build` from the root. 