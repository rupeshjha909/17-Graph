# Bellman-Ford Shortest Path — Complete Deep Dive

A line-by-line, in-depth explanation of the Bellman-Ford Single Source Shortest Path (SSSP) algorithm. Covers theory, code, edge cases, comparison with Dijkstra, and the Java conversion from the original C++ code.

---

## Table of Contents

1. [Problem Statement](#1-problem-statement)
2. [Why Bellman-Ford? The Gap Dijkstra Leaves](#2-why-bellman-ford-the-gap-dijkstra-leaves)
3. [The Big Idea: Iterative Relaxation](#3-the-big-idea-iterative-relaxation)
4. [Why Exactly V-1 Iterations?](#4-why-exactly-v-1-iterations)
5. [Negative Weight Cycle Detection](#5-negative-weight-cycle-detection)
6. [The Relaxation Operation (Revisited)](#6-the-relaxation-operation-revisited)
7. [Walking Through the Code Section by Section](#7-walking-through-the-code-section-by-section)
8. [C++ to Java Conversion Notes](#8-c-to-java-conversion-notes)
9. [Visual Examples](#9-visual-examples)
10. [Detailed Dry Run with Diagrams](#10-detailed-dry-run-with-diagrams)
11. [Why the `dist[u] != INF` Check Is Critical](#11-why-the-distu--inf-check-is-critical)
12. [Edge Cases](#12-edge-cases)
13. [Complexity Analysis](#13-complexity-analysis)
14. [Common Mistakes](#14-common-mistakes)
15. [Bellman-Ford vs Dijkstra — Detailed Comparison](#15-bellman-ford-vs-dijkstra--detailed-comparison)
16. [Why Dijkstra Fails on Negative Edges (and Bellman-Ford Doesn't)](#16-why-dijkstra-fails-on-negative-edges-and-bellman-ford-doesnt)
17. [Optimizations](#17-optimizations)
18. [Real-World Applications](#18-real-world-applications)
19. [Variations and Follow-ups](#19-variations-and-follow-ups)
20. [Complete Java Code](#20-complete-java-code)
21. [Interview Tips](#21-interview-tips)

---

## 1. Problem Statement

> Given a **directed weighted graph** with `n` vertices and `m` edges, and a source vertex `src`, find the **shortest distance from src to every other vertex**.
>
> The graph **may contain negative edge weights**, but **must not contain negative weight cycles reachable from src** (else shortest distances are undefined).
>
> Additionally, **detect whether a negative weight cycle exists**.

### Input
- `n`: number of vertices.
- `src`: source vertex.
- `edges`: list of `(u, v, weight)` triples.

### Output
- `dist[v]` = shortest distance from src to v.
- If unreachable: `dist[v] = ∞`.
- If negative weight cycle detected: report and exit.

### Example

```
Graph (5 vertices, directed):
   
       6           5
   (1)─────▶(2)──────▶(3)
    │   ▲    │  -2     ▲
   7│   │    │8        │7
    ▼   │    ▼         │
   (4)──┴──▶(5)────────┘
       -3     -4   9

Edges: 1→2 (6), 1→4 (7), 2→3 (5), 2→4 (8), 2→5 (-4),
       3→2 (-2), 4→3 (-3), 4→5 (9), 5→1 (2), 5→3 (7)

Note the NEGATIVE edges (2→5 with -4, 3→2 with -2, 4→3 with -3).

Dijkstra can't handle this. Bellman-Ford can!

Expected shortest distances from 1:
  1 → 0
  2 → 2  (via 1→4→3→2: 7-3-2 = 2)
  3 → 4  (via 1→4→3: 7-3 = 4)
  4 → 7  (via 1→4 direct)
  5 → -2 (via 1→4→3→2→5: 7-3-2-4 = -2)
```

---

## 2. Why Bellman-Ford? The Gap Dijkstra Leaves

### Dijkstra's Limitation

Dijkstra's algorithm is fast (O((V+E) log V)) but only works with **non-negative edge weights**.

### When Negative Edges Matter

Several real-world scenarios have negative weights:
- **Currency arbitrage**: a sequence of exchanges with negative log of rates.
- **Profit/loss in trading**: gains and losses.
- **Resource changes**: an action that gives back energy (negative cost).
- **Time corrections** in physics simulations.

### Dijkstra's Concrete Failure

```
Graph:
  A ─(2)─▶ B
  │         │
  5         -4
  │         │
  C ────────┘

Dijkstra from A:
  Settle A (dist 0).
  Extract B (dist 2). Settle B.
  Extract C (dist 5). Try to relax B via C: newDist = 5 + (-4) = 1 < 2.
  But B is already settled! Dijkstra won't reconsider.

Result: dist[B] = 2 (WRONG — actual shortest is 1).
```

Dijkstra's greedy invariant breaks when negative edges exist.

### Enter Bellman-Ford

Bellman-Ford gives up greediness for **exhaustive relaxation**. It checks every edge V-1 times, ensuring all updates propagate. Slower (O(V×E)) but handles negative edges.

---

## 3. The Big Idea: Iterative Relaxation

### The Core Strategy

> "Relax every edge in the graph repeatedly. After V-1 rounds, all shortest distances are final."

### What Does "Relax" Mean?

The same operation as in Dijkstra:

```
For edge (u, v, w):
  if dist[u] + w < dist[v]:
    dist[v] = dist[u] + w  ← improve distance to v
```

This is called **edge relaxation**.

### The Difference from Dijkstra

| Dijkstra | Bellman-Ford |
|----------|--------------|
| Smart: pick closest unsettled vertex | Brute force: check ALL edges every iteration |
| O((V+E) log V) | O(V × E) |
| Greedy: each vertex finalized once | Iterative: distances may update multiple times |
| Fails on negative edges | Works with negative edges |

### Intuition Behind V-1 Iterations

Each iteration, the "shortest distance information" propagates one edge further.

- After 1 iteration: shortest paths with ≤ 1 edge are correct.
- After 2 iterations: shortest paths with ≤ 2 edges are correct.
- ...
- After V-1 iterations: shortest paths with ≤ V-1 edges are correct.

Since any shortest path has ≤ V-1 edges (otherwise it has a cycle), V-1 iterations suffice.

---

## 4. Why Exactly V-1 Iterations?

### The Argument

In a graph with V vertices:
- A simple path (no repeated vertices) has at most V vertices.
- A simple path with V vertices has V-1 edges.
- Any path with V or more edges contains a cycle.

For shortest paths (without negative cycles):
- Cycles can never shorten a path (they either have positive weight = longer, or zero weight = same, or negative weight = error).
- So shortest paths are SIMPLE paths.
- Simple paths have ≤ V-1 edges.

Therefore: V-1 iterations is **enough** to find all shortest paths.

### Why Each Iteration Adds One Edge

After iteration k, all shortest paths with exactly k edges are correctly computed.

**Proof sketch**: By induction.

**Base case (k=0)**: dist[src] = 0. Correct.

**Inductive step**: Assume after k iterations, shortest paths with ≤ k edges are correct.

In iteration k+1, for each vertex v, we check all incoming edges (u, v, w). If u's shortest distance (correct by induction) + w improves v's distance, we update.

After iteration k+1, shortest paths with ≤ k+1 edges are correct.

After V-1 iterations, shortest paths with ≤ V-1 edges are correct → done.

### Why Not V Iterations?

V-1 is enough. Doing V iterations is unnecessary.

But we DO use a Vth iteration for a special purpose: **detecting negative cycles** (see next section).

---

## 5. Negative Weight Cycle Detection

### The Bonus Feature

After V-1 iterations, if shortest paths are valid (no negative cycles reachable from src), no edge can be further relaxed.

If we run **one more iteration** and find an edge that CAN still be relaxed, that means there's a negative cycle on the path from src.

### Why?

If a negative cycle is reachable from src, distances to vertices in the cycle can be made arbitrarily small by going around the cycle. So no number of iterations would converge.

```
Graph (negative cycle):
  1 → 2 → 3 → 1 with weights -1, -1, -1.
  Total cycle weight: -3.
  
  After each cycle traversal, dist decreases by 3.
  dist[2] could be 0, -3, -6, -9, ... ad infinitum.

The cycle CAN be relaxed forever — Bellman-Ford detects this in the Vth iteration.
```

### The Code

```java
// After V-1 iterations of relaxation, check once more
for (int[] edge : edges) {
    int u = edge[0], v = edge[1], wt = edge[2];
    if (dist[u] != INF && dist[u] + wt < dist[v]) {
        // We can still relax → negative cycle exists
        throw new RuntimeException("Negative weight cycle found");
    }
}
```

### What If Cycle Isn't Reachable from src?

If a negative cycle exists but isn't reachable from src, Bellman-Ford from src **won't detect it**. The algorithm gives correct distances for reachable vertices.

If you need to detect ALL negative cycles, add a "supersource" connected to every vertex with weight 0, then run Bellman-Ford from it.

---

## 6. The Relaxation Operation (Revisited)

The core line in Bellman-Ford:

```java
if (dist[u] != INF && dist[u] + wt < dist[v]) {
    dist[v] = dist[u] + wt;
}
```

### Step-by-Step

#### Check 1: `dist[u] != INF`
We can't relax via u if u itself isn't reachable. Skip such edges.

**Why this matters**: if `dist[u] == Integer.MAX_VALUE` and we compute `dist[u] + wt`, we get integer overflow → result wraps around to a negative number → could falsely update `dist[v]`.

Example:
```java
dist[u] = Integer.MAX_VALUE = 2147483647
wt = 5
dist[u] + wt = 2147483652  // overflows to about -2147483640
-2147483640 < dist[v] (anything reasonable) → false update!
```

Always check `dist[u] != INF` first.

#### Check 2: `dist[u] + wt < dist[v]`
The actual relaxation condition: does going through u give a shorter path to v?

#### Update: `dist[v] = dist[u] + wt`
Improve v's distance.

### Symmetric Case

For directed graphs, only the edge from u to v matters for updating v.

For undirected graphs, when you have edge (u, v, w), you'd typically store both directed edges (u, v, w) and (v, u, w) in the edge list. Then both update directions are considered.

---

## 7. Walking Through the Code Section by Section

Let me walk through every part of the Java code.

### Section A: Initialization

```java
int[] dist = new int[n + 1];
Arrays.fill(dist, INF);
dist[src] = 0;
```

#### Why `n + 1`?

The C++ code uses 1-indexed vertices (1 to n). So we allocate `n + 1` slots (indices 0 to n) and ignore index 0.

This is the **competitive-programming convention** — match the problem's indexing.

#### Why `Arrays.fill(dist, INF)`?

Initialize all distances to "infinity" (represented as `Integer.MAX_VALUE`). The source's distance is then set to 0.

#### Java vs C++

C++:
```cpp
vector<int> dist(n + 1, INT_MAX);
dist[src] = 0;
```

Java equivalent above. Note `Arrays.fill` vs initializing in constructor.

### Section B: The Main Loop (V-1 Iterations)

```java
for (int i = 0; i < n - 1; i++) {
    for (int[] edge : edges) {
        int u = edge[0];
        int v = edge[1];
        int wt = edge[2];
        
        if (dist[u] != INF && dist[u] + wt < dist[v]) {
            dist[v] = dist[u] + wt;
        }
    }
}
```

#### Outer Loop: `n - 1` Iterations

Run relaxation V-1 times. As discussed in Section 4.

#### Inner Loop: Iterate All Edges

For each edge, attempt relaxation. Order doesn't strictly matter — convergence happens after V-1 rounds regardless of order.

#### The Relaxation Block

The exact same operation as in Dijkstra, with the critical `dist[u] != INF` check.

### Section C: Negative Cycle Detection

```java
for (int[] edge : edges) {
    int u = edge[0];
    int v = edge[1];
    int wt = edge[2];
    
    if (dist[u] != INF && dist[u] + wt < dist[v]) {
        throw new RuntimeException("Negative weight cycle found");
    }
}
```

#### Same Loop, Different Purpose

Identical to the relaxation loop, but now we EXPECT no relaxation to succeed.

If ANY edge can still be relaxed → negative cycle exists.

#### Why Throw?

The C++ code uses `exit(0)`. Java doesn't typically exit the program directly — `throw new RuntimeException()` is cleaner. The caller can catch and handle.

Alternative: return a special value (like `null` or empty array) to indicate failure.

### Section D: Main Function

```java
public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    int n = scanner.nextInt();
    int m = scanner.nextInt();
    
    List<int[]> edges = new ArrayList<>();
    for (int i = 0; i < m; i++) {
        int u = scanner.nextInt();
        int v = scanner.nextInt();
        int wt = scanner.nextInt();
        edges.add(new int[]{u, v, wt});
    }
    
    try {
        int[] distance = bellmanFord(n, 1, edges);
        for (int i = 1; i <= n; i++) {
            if (distance[i] == INF) {
                System.out.println("node " + i + " is at dist INF (unreachable)");
            } else {
                System.out.println("node " + i + " is at dist " + distance[i]);
            }
        }
    } catch (RuntimeException e) {
        System.out.println(e.getMessage());
    }
}
```

#### Input Format

```
n m
u1 v1 w1
u2 v2 w2
...
um vm wm
```

#### Try/Catch for Negative Cycle

We wrap the call in try/catch to handle the negative cycle exception gracefully.

#### Output Format

For each vertex 1 to n, print its distance. Handle INF specially.

---

## 8. C++ to Java Conversion Notes

### Differences and Translations

#### 1. Vector → ArrayList

**C++**:
```cpp
vector<int> dist(n + 1, INT_MAX);
```

**Java**:
```java
int[] dist = new int[n + 1];
Arrays.fill(dist, INF);
```

We use `int[]` here (faster) instead of `ArrayList<Integer>` (slower due to boxing).

#### 2. INT_MAX

**C++**:
```cpp
INT_MAX
```

**Java**:
```java
Integer.MAX_VALUE
```

Equivalent.

#### 3. Edge Representation

**C++**:
```cpp
vector<vector<int>> edges;
edges.push_back({u, v, wt});
```

**Java**:
```java
List<int[]> edges = new ArrayList<>();
edges.add(new int[]{u, v, wt});
```

Each edge is a length-3 array `{u, v, weight}`.

#### 4. Range-Based For

**C++**:
```cpp
for (auto edge : edges) {
    int u = edge[0];
    int v = edge[1];
    int wt = edge[2];
    // ...
}
```

**Java**:
```java
for (int[] edge : edges) {
    int u = edge[0];
    int v = edge[1];
    int wt = edge[2];
    // ...
}
```

Almost identical.

#### 5. exit(0) → Exception

**C++**:
```cpp
if (...) {
    cout << "negative wt cycle found";
    exit(0);
}
```

**Java**:
```java
if (...) {
    throw new RuntimeException("Negative weight cycle found");
}
```

Java doesn't `exit()` mid-function typically. Throwing is more idiomatic.

#### 6. cout → System.out.println

**C++**:
```cpp
cout << "node " << i << " is at dist " << distance[i] << endl;
```

**Java**:
```java
System.out.println("node " + i + " is at dist " + distance[i]);
```

#### 7. cin → Scanner

**C++**:
```cpp
cin >> n >> m;
```

**Java**:
```java
Scanner scanner = new Scanner(System.in);
int n = scanner.nextInt();
int m = scanner.nextInt();
```

For very fast I/O in Java:
```java
BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
StreamTokenizer in = new StreamTokenizer(br);
```

#### 8. The `dist[u] != INT_MAX` Check

Both languages need this check to avoid overflow. Same logic.

---

## 9. Visual Examples

### Example 1: Simple Graph (No Negative Edges)

```
Graph:
  1 ──(5)──▶ 2 ──(3)──▶ 3
  │                      ▲
  └────────(10)──────────┘

From source 1:
  Iteration 1:
    1→2 (5): dist[2] = 5 (was INF)
    2→3 (3): dist[3] = 8 (was INF, via dist[2]=5)
            Wait — but iter 1 processes edge 1→2 first, so dist[2]=5 already, 
            then edge 2→3 sees dist[2]=5, computes 5+3=8. OK.
            But what if edge order was 2→3 before 1→2?
            Then iter 1: edge 2→3 sees dist[2]=INF, skips.
                       edge 1→2 sets dist[2]=5.
            After iter 1: dist[2]=5, dist[3]=INF.
            Iter 2: edge 2→3 sees dist[2]=5, sets dist[3]=8.
    1→3 (10): dist[3] = 10 then improved to 8 by 2→3
  
  After 1-2 iterations: 
    dist = [_, 0, 5, 8]
```

### Example 2: Graph with Negative Edge

```
Graph:
  1 ──(2)──▶ 2 ──(-4)──▶ 3
  │                      ▲
  └────────(5)───────────┘

From source 1:
  Iteration 1:
    1→2 (2): dist[2] = 2
    2→3 (-4): dist[3] = -2 (via 2)
    1→3 (5): dist[3] = -2 still (don't update)
  
  After iter 1: dist = [_, 0, 2, -2]
  
  Iteration 2: no changes.
  
  Final: 1→0, 2→2, 3→-2.

Dijkstra would give 1→0, 2→2, 3→5 (WRONG).
Bellman-Ford correctly handles negative edge.
```

### Example 3: Negative Cycle

```
Graph:
  1 ──(1)──▶ 2 ──(-1)──▶ 3
  ▲                      │
  └────────(-1)──────────┘

Cycle: 1→2→3→1, weight 1+(-1)+(-1) = -1 (negative!)

After many iterations:
  dist[2] keeps decreasing... by going around the cycle.

Bellman-Ford detects: in the Vth iteration (after V-1 normal iterations),
edge (3,1,-1) can STILL be relaxed → negative cycle!
```

### Example 4: Negative Edges OK (No Cycle)

```
Graph:
  1 ──(5)──▶ 2
  │          │
  3          -3
  │          │
  3 ◀────────┘

Edge weights: 1→2 (5), 1→3 (3), 2→3 (-3)
No cycle (it's a DAG).

From 1:
  Iter 1: 1→2 (5): dist[2]=5. 1→3 (3): dist[3]=3. 2→3 (-3): dist[3]=2.
  Iter 2: no changes.

Final: 1→0, 2→5, 3→2.
```

---

## 10. Detailed Dry Run with Diagrams

Let's trace through the **CLRS textbook example** (Cormen, Leiserson, Rivest, Stein):

```
Vertices: 1 (s), 2 (t), 3 (x), 4 (y), 5 (z)

Edges:
  1→2: 6     (s→t)
  1→4: 7     (s→y)
  2→3: 5     (t→x)
  2→4: 8     (t→y)
  2→5: -4    (t→z)
  3→2: -2    (x→t)
  4→3: -3    (y→x)
  4→5: 9     (y→z)
  5→1: 2     (z→s)
  5→3: 7     (z→x)

Source: 1
Expected: 1→0, 2→2, 3→4, 4→7, 5→-2
```

### Initial State

```
dist = [_, 0, INF, INF, INF, INF]
       (index 0 unused)
```

### Iteration 1

Process all 10 edges. Order matters for intermediate states (final is the same).

```
Edge 1→2 (6):
  dist[1] = 0 ≠ INF, 0 + 6 = 6 < INF.
  dist[2] = 6.

Edge 1→4 (7):
  dist[1] = 0 ≠ INF, 0 + 7 = 7 < INF.
  dist[4] = 7.

Edge 2→3 (5):
  dist[2] = 6 ≠ INF, 6 + 5 = 11 < INF.
  dist[3] = 11.

Edge 2→4 (8):
  dist[2] = 6 ≠ INF, 6 + 8 = 14 < 7? No. Skip.

Edge 2→5 (-4):
  dist[2] = 6 ≠ INF, 6 + (-4) = 2 < INF.
  dist[5] = 2.

Edge 3→2 (-2):
  dist[3] = 11 ≠ INF, 11 + (-2) = 9 < 6? No. Skip.

Edge 4→3 (-3):
  dist[4] = 7 ≠ INF, 7 + (-3) = 4 < 11.
  dist[3] = 4.   ← improved!

Edge 4→5 (9):
  dist[4] = 7 ≠ INF, 7 + 9 = 16 < 2? No. Skip.

Edge 5→1 (2):
  dist[5] = 2 ≠ INF, 2 + 2 = 4 < 0? No. Skip.

Edge 5→3 (7):
  dist[5] = 2 ≠ INF, 2 + 7 = 9 < 4? No. Skip.
```

After Iteration 1:
```
dist = [_, 0, 6, 4, 7, 2]
```

### Iteration 2

```
Edge 1→2 (6): 0 + 6 = 6 < 6? No.
Edge 1→4 (7): 0 + 7 = 7 < 7? No.
Edge 2→3 (5): 6 + 5 = 11 < 4? No.
Edge 2→4 (8): 6 + 8 = 14 < 7? No.
Edge 2→5 (-4): 6 + (-4) = 2 < 2? No.
Edge 3→2 (-2): 4 + (-2) = 2 < 6 ✓ Update dist[2] = 2.
Edge 4→3 (-3): 7 + (-3) = 4 < 4? No.
Edge 4→5 (9): 7 + 9 = 16 < 2? No.
Edge 5→1 (2): 2 + 2 = 4 < 0? No.
Edge 5→3 (7): 2 + 7 = 9 < 4? No.
```

After Iteration 2:
```
dist = [_, 0, 2, 4, 7, 2]
```

### Iteration 3

```
Edge 1→2 (6): 0 + 6 = 6 < 2? No.
Edge 1→4 (7): 0 + 7 = 7 < 7? No.
Edge 2→3 (5): 2 + 5 = 7 < 4? No.
Edge 2→4 (8): 2 + 8 = 10 < 7? No.
Edge 2→5 (-4): 2 + (-4) = -2 < 2 ✓ Update dist[5] = -2.
Edge 3→2 (-2): 4 + (-2) = 2 < 2? No.
Edge 4→3 (-3): 7 + (-3) = 4 < 4? No.
Edge 4→5 (9): 7 + 9 = 16 < -2? No.
Edge 5→1 (2): -2 + 2 = 0 < 0? No.
Edge 5→3 (7): -2 + 7 = 5 < 4? No.
```

After Iteration 3:
```
dist = [_, 0, 2, 4, 7, -2]
```

### Iteration 4

No edges can be relaxed. State stays the same.

### After V-1 = 4 Iterations

```
dist = [_, 0, 2, 4, 7, -2]

Expected:
  1 → 0 ✓
  2 → 2 ✓
  3 → 4 ✓
  4 → 7 ✓
  5 → -2 ✓
```

### Negative Cycle Check (Iteration 5)

Run one more iteration. No edges relax → no negative cycle. Return.

### Visual Comparison

```
Initial:       After iter 1:    After iter 2:    After iter 3:
  1: 0           1: 0             1: 0             1: 0
  2: ∞           2: 6             2: 2             2: 2
  3: ∞           3: 4             3: 4             3: 4
  4: ∞           4: 7             4: 7             4: 7
  5: ∞           5: 2             5: 2             5: -2
```

Notice how distances propagate over iterations. Each round, info travels one more edge.

---

## 11. Why the `dist[u] != INF` Check Is Critical

This is the **#1 most subtle bug** in Bellman-Ford implementations.

### The Problem: Integer Overflow

```java
int dist[] = new int[n + 1];
Arrays.fill(dist, Integer.MAX_VALUE);
// dist[u] = Integer.MAX_VALUE = 2147483647

int weight = 5;
int sum = dist[u] + weight;
// sum = 2147483647 + 5 = -2147483644 (OVERFLOW!)
```

In Java (and C++), `int` is 32-bit. Adding to `Integer.MAX_VALUE` overflows to a large negative number.

### The Consequence

Without the check:
```java
if (dist[u] + wt < dist[v]) {  // dist[u]+wt overflowed to negative!
    dist[v] = dist[u] + wt;     // dist[v] set to garbage negative number
}
```

This BREAKS the algorithm — we update distances to nonsense values.

### The Fix

```java
if (dist[u] != INF && dist[u] + wt < dist[v]) {
    // Only compute dist[u] + wt if dist[u] is a real number
}
```

The short-circuit `&&` ensures `dist[u] + wt` is only evaluated when `dist[u] != INF`.

### Alternative: Use `long`

```java
long sum = (long) dist[u] + wt;  // promote to long, no overflow
if (sum < dist[v]) {
    dist[v] = (int) sum;
}
```

Less efficient (more memory, slightly slower), but avoids the overflow concern.

### What If Source Can't Reach Vertex?

If u is unreachable from src, `dist[u]` stays as `Integer.MAX_VALUE`. We shouldn't use this to update anything. The check skips correctly.

---

## 12. Edge Cases

### 1. Single Vertex (No Edges)

```
n = 1, edges = []
dist[1] = 0.
Iter loop doesn't run (n-1 = 0).
Negative cycle check: no edges, no issue.
Result: dist[1] = 0.
```

### 2. Disconnected Graph

```
n = 4, src = 1, edges = [(1, 2, 5)]
After iterations: dist = [_, 0, 5, INF, INF].
Vertices 3 and 4 are unreachable.
```

### 3. Negative Cycle Reachable from Source

```
n = 3, src = 1, edges = [(1,2,1), (2,3,-1), (3,1,-1)]
Cycle 1→2→3→1 has weight -1 (negative).

After V-1 = 2 iterations:
  Iter 1: dist[2]=1, dist[3]=0.
  Iter 2: dist[1] could be -1 (via 3→1)... 

Actually let me redo this:
Edges: (1,2,1), (2,3,-1), (3,1,-1).
Iter 1:
  (1,2,1): dist[1]=0, dist[2] = 0+1 = 1.
  (2,3,-1): dist[2]=1, dist[3] = 1+(-1) = 0.
  (3,1,-1): dist[3]=0, dist[1] = 0+(-1) = -1.
Iter 2:
  (1,2,1): dist[1]=-1, dist[2] = -1+1 = 0 (< 1).
  (2,3,-1): dist[2]=0, dist[3] = 0+(-1) = -1.
  (3,1,-1): dist[3]=-1, dist[1] = -1+(-1) = -2.

After 2 iterations: dist = [_, -2, 0, -1].

Check iteration:
  (1,2,1): dist[1]=-2, dist[2] = -2+1 = -1 < 0. UPDATE!
  → NEGATIVE CYCLE DETECTED.

Bellman-Ford reports negative cycle.
```

### 4. Negative Cycle NOT Reachable from Source

```
n = 4
edges = [(1, 2, 1), (3, 4, -10), (4, 3, -10)]
Source: 1.

The cycle 3→4→3 (weight -20) is negative.
But it's not reachable from 1.

Bellman-Ford from 1:
  dist[1]=0, dist[2]=1, dist[3]=INF, dist[4]=INF.
  Negative cycle check: dist[3]=INF, so cycle edges can't relax (the check prevents).
  
Bellman-Ford DOES NOT detect this cycle.

If you need to detect ALL negative cycles, add a supersource connected to every vertex with weight 0.
```

### 5. Self-Loop with Negative Weight

```
edges = [(1, 1, -5)]
Bellman-Ford from 1:
  dist[1] = 0 initially.
  Iter 1: dist[1] = 0 + (-5) = -5 < 0. UPDATE.
  Iter 2: dist[1] = -5 + (-5) = -10. UPDATE.
  ...
  
This IS a negative cycle (of length 1).
Bellman-Ford detects it in the Vth iteration.
```

### 6. Multiple Edges Between Same Vertices

```
edges = [(1, 2, 5), (1, 2, 3), (1, 2, 7)]
All three are processed. The minimum (3) wins for dist[2].
No issue.
```

### 7. Self-Loop with Positive Weight

```
edges = [(1, 1, 5)]
dist[1] = 0.
Iter 1: 0 + 5 = 5 < 0? No. Don't update.

Positive self-loops never trigger updates. Harmless.
```

### 8. Zero-Weight Cycle

```
edges = [(1,2,1), (2,3,-1), (3,1,0)]
Cycle weight: 1 + (-1) + 0 = 0.

After V-1 iterations, all distances stabilize.
Check iteration: nothing relaxes (cycle is exactly 0).
No negative cycle reported. ✓
```

---

## 13. Complexity Analysis

### Time Complexity

**O(V × E)**

- Outer loop: V - 1 iterations.
- Inner loop: examine all E edges.
- Each edge relaxation is O(1).

Total: (V - 1) × E + E (cycle check) = O(V × E).

#### Worst Case
For dense graphs (E ≈ V²), this becomes **O(V³)**.

#### Best Case
Still O(V × E) — we always do V - 1 iterations (unless we add early termination).

### Space Complexity

**O(V + E)** — for the distance array and edge list.

### Comparison

| Algorithm | Time | Negative Weights? |
|-----------|------|-------------------|
| BFS | O(V + E) | N/A |
| Dijkstra | O((V + E) log V) | NO |
| **Bellman-Ford** | **O(V × E)** | **YES** |
| Floyd-Warshall (all-pairs) | O(V³) | YES |

Bellman-Ford is slower than Dijkstra but handles negative weights. Use it when you need that capability.

### Why So Slow?

Bellman-Ford is the price you pay for handling negative weights. Without the greedy invariant, you must check every edge V-1 times to ensure convergence.

---

## 14. Common Mistakes

### Mistake 1: Forgetting the `dist[u] != INF` Check

```java
// WRONG:
if (dist[u] + wt < dist[v]) {
    dist[v] = dist[u] + wt;
}
// → Integer overflow when dist[u] = INF.

// RIGHT:
if (dist[u] != INF && dist[u] + wt < dist[v]) {
    dist[v] = dist[u] + wt;
}
```

The #1 bug. ALWAYS include this check.

### Mistake 2: Wrong Number of Iterations

```java
// WRONG: V iterations (one too many for relaxation)
for (int i = 0; i < n; i++) { ... }

// RIGHT: V - 1 iterations
for (int i = 0; i < n - 1; i++) { ... }

// (The Vth iteration is used SEPARATELY for negative cycle detection.)
```

V iterations technically don't give wrong answers (no further changes), but it's wasteful.

### Mistake 3: Forgetting Negative Cycle Detection

```java
// WRONG: skip the check
return dist;

// RIGHT: check for negative cycles
for (int[] edge : edges) {
    if (dist[u] != INF && dist[u] + wt < dist[v]) {
        // negative cycle!
    }
}
```

If your problem doesn't care about negative cycles, you can skip. But if a negative cycle exists, your distances are still "ok-ish" (they show some valid path), but the actual shortest is -∞.

### Mistake 4: Using Bellman-Ford When Dijkstra Suffices

```java
// SLOW: using Bellman-Ford for non-negative weights
bellmanFord(...);  // O(V * E)

// FAST: use Dijkstra
dijkstra(...);  // O((V + E) log V)
```

Only use Bellman-Ford when you must (negative weights or cycle detection).

### Mistake 5: 0-Indexed vs 1-Indexed Confusion

```java
// If input is 1-indexed and you initialize dist as size n (instead of n+1):
int[] dist = new int[n];  // indices 0 to n-1
dist[src] = 0;  // src might be n, ArrayIndexOutOfBoundsException!

// Match the input's indexing:
int[] dist = new int[n + 1];  // indices 0 to n
```

### Mistake 6: Modifying Distances Mid-Iteration (Subtle)

The standard Bellman-Ford updates distances during the iteration. This is FINE for correctness — converges in V-1 iterations.

But some sources suggest "synchronous updates" (compute new distances using old, then commit). This is slower in practice. Use the standard async version.

### Mistake 7: Treating Undirected Edges as One Edge

```java
// WRONG for undirected graphs:
edges.add(new int[]{u, v, w});  // only one direction

// RIGHT: add both directions
edges.add(new int[]{u, v, w});
edges.add(new int[]{v, u, w});
```

For undirected graphs, an edge {u, v, w} means you can go BOTH ways. Need two entries.

### Mistake 8: Detecting Cycles Not Reachable from Source

```
Bellman-Ford from src can only detect negative cycles REACHABLE from src.

If you need to detect any negative cycle in the graph:
  Add a supersource connected to every vertex with weight 0.
  Run Bellman-Ford from supersource.
```

---

## 15. Bellman-Ford vs Dijkstra — Detailed Comparison

### Side-by-Side

| Aspect | Dijkstra | Bellman-Ford |
|--------|----------|--------------|
| **Approach** | Greedy + Priority Queue | Brute-force relaxation |
| **Time** | O((V + E) log V) | O(V × E) |
| **Space** | O(V + E) | O(V + E) |
| **Negative weights?** | NO | YES |
| **Detects negative cycles?** | NO | YES |
| **Implementation** | Complex (PQ, lazy deletion) | Simple (nested loops) |
| **Strategy** | Settle vertices one by one | Iterate all edges V-1 times |

### When to Use Which

#### Use Dijkstra when:
- All edge weights are non-negative.
- You need O((V + E) log V) speed.
- No need for negative cycle detection.

#### Use Bellman-Ford when:
- Edge weights may be negative.
- You need to detect negative cycles.
- Graph is small enough that O(V × E) is acceptable.
- Code simplicity matters more than speed.

### Performance Comparison

For V = 1000, E = 10000:
- Dijkstra: ~140,000 operations.
- Bellman-Ford: ~10,000,000 operations.

Bellman-Ford is **~70x slower** in this case.

But for small graphs (V ≤ 100), Bellman-Ford is fast enough and much simpler to code.

---

## 16. Why Dijkstra Fails on Negative Edges (and Bellman-Ford Doesn't)

### Dijkstra's Mistake

Dijkstra commits to a vertex's distance as soon as it's extracted from the PQ. With negative weights, a later path could improve the distance, but Dijkstra won't reconsider.

```
Graph:
  1 ──(2)──▶ 2
  │          │
  5         -4
  │          │
  3 ─────────┘

Dijkstra from 1:
  1. Extract 1 (dist 0). Settle.
  2. Relax 1→2 (2): dist[2] = 2.
  3. Relax 1→3 (5): dist[3] = 5.
  4. Extract 2 (dist 2). Settle. ← MISTAKE.
  5. Relax 3→2 (-4): dist[2] = 5 + (-4) = 1.
     But 2 is already settled → ignored.

Final: dist[2] = 2 (WRONG, actual is 1).
```

### Bellman-Ford's Solution

Bellman-Ford doesn't "settle" vertices. It just relaxes edges over and over.

```
Bellman-Ford from 1:
  Iter 1:
    1→2 (2): dist[2] = 2.
    1→3 (5): dist[3] = 5.
    3→2 (-4): dist[3] + wt = 5 + (-4) = 1 < 2. Update dist[2] = 1.
  Iter 2: no changes.

Final: dist[2] = 1 ✓ CORRECT.
```

By iterating over all edges multiple times, Bellman-Ford lets information propagate through negative edges naturally.

### The Theoretical Difference

**Dijkstra**: greedy. Decisions are FINAL. Requires monotonic increase of distances (non-negative weights).

**Bellman-Ford**: iterative. Decisions are RECONSIDERED. Allows distance to decrease across iterations.

---

## 17. Optimizations

### 1. Early Termination

If an iteration doesn't relax any edge, the algorithm has converged. We can break early.

```java
for (int i = 0; i < n - 1; i++) {
    boolean updated = false;
    for (int[] edge : edges) {
        if (dist[u] != INF && dist[u] + wt < dist[v]) {
            dist[v] = dist[u] + wt;
            updated = true;
        }
    }
    if (!updated) break;  // converged early
}
```

Best case: O(E) if the graph is "nice".

### 2. SPFA (Shortest Path Faster Algorithm)

SPFA is an optimization of Bellman-Ford using a queue:
- Only re-examine vertices whose distance was updated.
- Average case: O(k × E) where k is small.
- Worst case: still O(V × E).

In practice, SPFA is often much faster than naive Bellman-Ford.

```java
public int[] spfa(int n, int src, List<List<int[]>> adj) {
    int[] dist = new int[n + 1];
    Arrays.fill(dist, INF);
    dist[src] = 0;
    
    Queue<Integer> queue = new LinkedList<>();
    boolean[] inQueue = new boolean[n + 1];
    queue.offer(src);
    inQueue[src] = true;
    
    while (!queue.isEmpty()) {
        int u = queue.poll();
        inQueue[u] = false;
        
        for (int[] edge : adj.get(u)) {
            int v = edge[0], w = edge[1];
            if (dist[u] + w < dist[v]) {
                dist[v] = dist[u] + w;
                if (!inQueue[v]) {
                    queue.offer(v);
                    inQueue[v] = true;
                }
            }
        }
    }
    return dist;
}
```

SPFA is popular in competitive programming.

### 3. Detect Negative Cycle Faster

Track how many times each vertex has been relaxed. If a vertex is relaxed > V times, it's in a negative cycle.

---

## 18. Real-World Applications

### 1. Currency Arbitrage Detection
Convert currency exchange rates to negative logs. A negative cycle = arbitrage opportunity.

### 2. Network Routing
Some routing protocols (like RIP — Routing Information Protocol) use Bellman-Ford.

### 3. Distributed Systems
Bellman-Ford has a nice "distributed" property — each vertex only needs info from its neighbors.

### 4. Constraint Systems
Solving systems of difference constraints (x - y ≤ c) reduces to Bellman-Ford.

### 5. Game Theory
Some shortest-path-style problems in games allow negative weights (penalties, bonuses).

### 6. Operations Research
Project scheduling with negative weights (e.g., bonuses for early completion).

---

## 19. Variations and Follow-ups

### Variation 1: Find the Actual Shortest Path

Track parent pointers during relaxation:

```java
int[] parent = new int[n + 1];
Arrays.fill(parent, -1);

// During relaxation:
if (dist[u] + wt < dist[v]) {
    dist[v] = dist[u] + wt;
    parent[v] = u;
}

// To reconstruct path to vertex t:
List<Integer> path = new ArrayList<>();
for (int v = t; v != -1; v = parent[v]) path.add(v);
Collections.reverse(path);
```

### Variation 2: Detect Vertices Reachable from Negative Cycle

After Bellman-Ford, run one more iteration. Any vertex still being relaxed is "affected" by a negative cycle.

### Variation 3: Johnson's Algorithm

For all-pairs shortest paths with negative edges:
1. Add a supersource and run Bellman-Ford.
2. Reweight edges to make them non-negative.
3. Run Dijkstra from each vertex.

Time: O(V × E log V) — better than Floyd-Warshall O(V³) for sparse graphs.

### Variation 4: Constrained Shortest Paths

If you have additional constraints (e.g., max K edges allowed), modify Bellman-Ford to track edge count.

This is the basis of **Bellman-Ford with K stops** (LC 787 — Cheapest Flights Within K Stops).

### Related LeetCode Problems

| Problem | Difficulty | Link |
|---------|-----------|------|
| Cheapest Flights Within K Stops | Medium | [LC 787](https://leetcode.com/problems/cheapest-flights-within-k-stops/) |
| Network Delay Time | Medium | [LC 743](https://leetcode.com/problems/network-delay-time/) |
| Find the City With the Smallest Number of Neighbors | Medium | [LC 1334](https://leetcode.com/problems/find-the-city-with-the-smallest-number-of-neighbors-at-a-threshold-distance/) |
| Maximum Probability Path | Medium | [LC 1514](https://leetcode.com/problems/path-with-maximum-probability/) |

### LC 787 Specifically — A Perfect Bellman-Ford Application

> Find cheapest flight from src to dst with at most K stops.

This is a constrained shortest path. Standard Dijkstra doesn't handle the K constraint directly. Bellman-Ford does:

```java
public int findCheapestPrice(int n, int[][] flights, int src, int dst, int K) {
    int[] dist = new int[n];
    Arrays.fill(dist, Integer.MAX_VALUE);
    dist[src] = 0;
    
    // K+1 iterations because we want paths with at most K+1 edges
    for (int i = 0; i <= K; i++) {
        int[] temp = dist.clone();  // snapshot to prevent same-iteration updates
        for (int[] f : flights) {
            int u = f[0], v = f[1], w = f[2];
            if (dist[u] != Integer.MAX_VALUE && dist[u] + w < temp[v]) {
                temp[v] = dist[u] + w;
            }
        }
        dist = temp;
    }
    return dist[dst] == Integer.MAX_VALUE ? -1 : dist[dst];
}
```

Note the `dist.clone()` — for the K-stops constraint, we use synchronous updates (snapshot the previous iteration's distances).

---

## 20. Complete Java Code

### Faithful Conversion

See `BellmanFord.java`. The structure:

```java
public static int[] bellmanFord(int n, int src, List<int[]> edges) {
    int[] dist = new int[n + 1];
    Arrays.fill(dist, INF);
    dist[src] = 0;
    
    // V-1 iterations
    for (int i = 0; i < n - 1; i++) {
        for (int[] edge : edges) {
            int u = edge[0], v = edge[1], wt = edge[2];
            if (dist[u] != INF && dist[u] + wt < dist[v]) {
                dist[v] = dist[u] + wt;
            }
        }
    }
    
    // Negative cycle detection
    for (int[] edge : edges) {
        int u = edge[0], v = edge[1], wt = edge[2];
        if (dist[u] != INF && dist[u] + wt < dist[v]) {
            throw new RuntimeException("Negative weight cycle found");
        }
    }
    
    return dist;
}
```

### Production-Ready Version

```java
import java.util.*;

public class BellmanFordOptimized {
    
    public static class Result {
        public int[] distances;
        public int[] parent;
        public boolean hasNegativeCycle;
        public Set<Integer> negativeReachable;
        
        public Result(int n) {
            distances = new int[n];
            parent = new int[n];
            Arrays.fill(distances, Integer.MAX_VALUE);
            Arrays.fill(parent, -1);
            negativeReachable = new HashSet<>();
        }
    }
    
    public static Result bellmanFord(int n, int src, int[][] edges) {
        Result result = new Result(n);
        result.distances[src] = 0;
        
        // Early termination
        for (int i = 0; i < n - 1; i++) {
            boolean updated = false;
            for (int[] edge : edges) {
                int u = edge[0], v = edge[1], wt = edge[2];
                if (result.distances[u] != Integer.MAX_VALUE 
                    && result.distances[u] + wt < result.distances[v]) {
                    result.distances[v] = result.distances[u] + wt;
                    result.parent[v] = u;
                    updated = true;
                }
            }
            if (!updated) break;
        }
        
        // Detect negative cycle and affected vertices
        for (int[] edge : edges) {
            int u = edge[0], v = edge[1], wt = edge[2];
            if (result.distances[u] != Integer.MAX_VALUE 
                && result.distances[u] + wt < result.distances[v]) {
                result.hasNegativeCycle = true;
                result.negativeReachable.add(v);
            }
        }
        
        return result;
    }
    
    public static void main(String[] args) {
        // Test from CLRS
        int[][] edges = {
            {0, 1, 6}, {0, 3, 7},
            {1, 2, 5}, {1, 3, 8}, {1, 4, -4},
            {2, 1, -2},
            {3, 2, -3}, {3, 4, 9},
            {4, 0, 2}, {4, 2, 7}
        };
        
        Result result = bellmanFord(5, 0, edges);
        if (result.hasNegativeCycle) {
            System.out.println("Negative cycle detected!");
        } else {
            for (int i = 0; i < 5; i++) {
                System.out.println("Vertex " + i + " distance: " + result.distances[i]);
            }
        }
        // Expected: 0→0, 1→2, 2→4, 3→7, 4→-2
    }
}
```

This version includes:
- Parent tracking for path reconstruction.
- Early termination.
- Identifies vertices affected by negative cycle.
- Cleaner Result object.

---

## 21. Interview Tips

### How to Approach in Interview

1. **Restate the problem**: "Find shortest paths from source, allowing negative weights, detecting negative cycles."
2. **Why not Dijkstra?**: "Dijkstra fails with negative edges. Bellman-Ford handles them."
3. **Walk through the algorithm**: "Relax all edges V-1 times. After that, no more relaxations should be possible — if any are, there's a negative cycle."
4. **Code carefully**: include the `dist[u] != INF` check!
5. **Discuss complexity**: O(V × E), slower than Dijkstra but works with negative weights.
6. **Test edge cases**: disconnected graph, negative cycle, self-loops.

### Discussion Points to Score Bonus

#### 1. The V-1 Iterations Insight
> "Any simple path has at most V-1 edges. Each iteration extends correct shortest paths by one edge. So V-1 iterations suffice to cover all possible shortest paths."

#### 2. The Overflow Pitfall
> "I need to check `dist[u] != INF` before adding `wt`, otherwise `INT_MAX + wt` overflows in Java and gives a false positive update."

#### 3. Why Bellman-Ford Handles Negative Edges
> "Unlike Dijkstra, Bellman-Ford doesn't 'commit' to a vertex's distance after extraction. It revisits all edges, allowing later improvements via negative weights."

#### 4. Negative Cycle Detection
> "After V-1 iterations, if any edge can still be relaxed in a Vth iteration, that's a negative cycle on the path from source."

#### 5. When to Use Each
> "Use Dijkstra for non-negative weights (faster). Use Bellman-Ford for negative weights or when you need to detect negative cycles."

### Likely Follow-Up Questions

#### Q: Why V-1 iterations?
**A**: Any shortest path is a simple path with ≤ V-1 edges. Each iteration extends correctness by one more edge.

#### Q: What if the graph is undirected?
**A**: Treat each undirected edge as two directed edges (both directions). Then run Bellman-Ford as usual. Note: any edge with negative weight is automatically a "negative cycle" of length 2 (you can go back and forth). So undirected graphs with negative weights are tricky.

#### Q: How to find the actual path?
**A**: Track `parent[v]` during relaxation. To reconstruct: walk back from destination using parent pointers.

#### Q: What's SPFA?
**A**: Shortest Path Faster Algorithm — an optimization of Bellman-Ford using a queue. Only re-examine vertices whose distance was updated. Often much faster in practice.

#### Q: Compare with Floyd-Warshall.
**A**: 
- Floyd-Warshall: all-pairs shortest paths, O(V³).
- Bellman-Ford: single-source, O(V × E).
- For dense graphs, Floyd-Warshall is competitive. For sparse, Bellman-Ford per vertex is better.

#### Q: How would you solve LC 787 (Cheapest Flights K Stops)?
**A**: Modify Bellman-Ford to do K+1 iterations (each iteration extends path by one edge). Use snapshot of distances to avoid same-iteration updates.

#### Q: What if I have BOTH negative and zero weights?
**A**: Zero is fine. Bellman-Ford handles any real weights as long as no negative cycle.

### Common Interview Mistakes

1. Forgetting `dist[u] != INF` check → overflow.
2. Wrong number of iterations (V instead of V-1, or only 1).
3. Not handling negative cycle detection.
4. Using Bellman-Ford when Dijkstra suffices (slower).
5. Confusing with Dijkstra: trying to use a PQ instead of full iteration.

---

## TL;DR

### The Mental Model

```
Bellman-Ford = "Brute force relaxation, V-1 times."

For each iteration:
  For each edge (u, v, w):
    if dist[u] + w < dist[v]: update dist[v].

After V-1 iterations: done.
One more iteration: if anything still updates → negative cycle.
```

### The Algorithm in 30 Seconds

```
1. dist[src] = 0; all others = INF.
2. Repeat V-1 times:
     For each edge (u, v, w):
       if dist[u] != INF AND dist[u] + w < dist[v]:
         dist[v] = dist[u] + w.
3. Check once more: if any edge can be relaxed → negative cycle.
```

### The Five Key Insights

1. **V-1 iterations** is the magic number — shortest paths have ≤ V-1 edges.
2. **Handles negative weights** unlike Dijkstra.
3. **The `dist[u] != INF` check** prevents integer overflow.
4. **One extra iteration** detects negative cycles.
5. **O(V × E)** complexity — slower than Dijkstra but more powerful.

### C++ to Java Cheatsheet

| C++ | Java |
|-----|------|
| `vector<int> dist(n+1, INT_MAX)` | `int[] dist = new int[n+1]; Arrays.fill(dist, INF);` |
| `vector<vector<int>> edges` | `List<int[]> edges` |
| `edges.push_back({u, v, wt})` | `edges.add(new int[]{u, v, wt})` |
| `INT_MAX` | `Integer.MAX_VALUE` |
| `exit(0)` | `throw new RuntimeException(...)` |
| `cout << ...` | `System.out.println(...)` |
| `cin >> ...` | `scanner.nextInt()` |
| `for (auto edge : edges)` | `for (int[] edge : edges)` |

### Final Code Snippet to Memorize

```java
public int[] bellmanFord(int n, int src, int[][] edges) {
    int[] dist = new int[n + 1];
    Arrays.fill(dist, Integer.MAX_VALUE);
    dist[src] = 0;
    
    for (int i = 0; i < n - 1; i++) {
        for (int[] e : edges) {
            int u = e[0], v = e[1], w = e[2];
            if (dist[u] != Integer.MAX_VALUE && dist[u] + w < dist[v]) {
                dist[v] = dist[u] + w;
            }
        }
    }
    
    // Negative cycle check
    for (int[] e : edges) {
        int u = e[0], v = e[1], w = e[2];
        if (dist[u] != Integer.MAX_VALUE && dist[u] + w < dist[v]) {
            return null;  // negative cycle
        }
    }
    
    return dist;
}
```

### When This Problem Appears

| Tier | Frequency | Companies |
|------|-----------|-----------|
| Tier 1 | Rarely | Basic version |
| Tier 2 | Sometimes | Paytm, Flipkart (advanced) |
| Tier 3 | Often (with twists) | Google, Amazon, Meta (LC 787) |
| Tier 4 | Variations | Top quant (currency arbitrage) |

---

*Master Bellman-Ford and you've internalized a fundamental shortest-path algorithm that handles the gnarly case of negative weights. The "iterate over all edges V-1 times" pattern is a powerful template that extends to constraint satisfaction, distributed systems, and many specialized variants like SPFA and Johnson's algorithm.*
