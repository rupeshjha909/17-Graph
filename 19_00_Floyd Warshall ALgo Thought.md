# Floyd-Warshall All-Pairs Shortest Path — Complete Deep Dive

A line-by-line, in-depth explanation of the Floyd-Warshall algorithm for finding shortest distances between **every pair** of vertices. Covers theory, the elegant DP formulation, every code design choice, Java conversion from C++, and detailed approaches for related interview problems.

---

## Table of Contents

1. [Problem Statement](#1-problem-statement)
2. [Why "All Pairs"? The Big Picture](#2-why-all-pairs-the-big-picture)
3. [The DP Insight — Heart of Floyd-Warshall](#3-the-dp-insight--heart-of-floyd-warshall)
4. [Walking Through the Recurrence](#4-walking-through-the-recurrence)
5. [Why the Loop Order Matters (k, i, j)](#5-why-the-loop-order-matters-k-i-j)
6. [Why In-Place Updates Work](#6-why-in-place-updates-work)
7. [Walking Through the Code Section by Section](#7-walking-through-the-code-section-by-section)
8. [C++ to Java Conversion Notes](#8-c-to-java-conversion-notes)
9. [The Overflow Problem with INT_MAX](#9-the-overflow-problem-with-int_max)
10. [Visual Examples](#10-visual-examples)
11. [Detailed Dry Run with Matrix Evolution](#11-detailed-dry-run-with-matrix-evolution)
12. [Negative Cycle Detection](#12-negative-cycle-detection)
13. [Edge Cases](#13-edge-cases)
14. [Complexity Analysis](#14-complexity-analysis)
15. [Common Mistakes](#15-common-mistakes)
16. [Floyd-Warshall vs Dijkstra vs Bellman-Ford](#16-floyd-warshall-vs-dijkstra-vs-bellman-ford)
17. [Path Reconstruction](#17-path-reconstruction)
18. [Optimizations](#18-optimizations)
19. [Related Problems and How to Approach Them](#19-related-problems-and-how-to-approach-them)
20. [Complete Java Code](#20-complete-java-code)
21. [Interview Tips](#21-interview-tips)

---

## 1. Problem Statement

> Given a **weighted directed graph** with `V` vertices (possibly with negative edge weights, but no negative cycles), find the **shortest distance between every pair of vertices**.

### Input
- An adjacency matrix `graph[i][j]` where:
  - `graph[i][j] = w` if there's a direct edge i→j with weight w.
  - `graph[i][j] = ∞` (INF) if no direct edge.
  - `graph[i][i] = 0` (distance from a node to itself).

### Output
- A matrix `dist[i][j]` where `dist[i][j]` = shortest distance from i to j.
- Negative cycles can be detected by checking if `dist[i][i] < 0` for any i.

### Example

```
Input graph (4 vertices):
       1     2     3     4
  1 [  0,   INF,  -2,   INF ]
  2 [  4,    0,    3,   INF ]
  3 [ INF,  INF,   0,    2  ]
  4 [ INF,  -1,   INF,    0 ]

Direct edges:
  1 → 3: -2
  2 → 1: 4, 2 → 3: 3
  3 → 4: 2
  4 → 2: -1

Output (shortest distances):
       1     2     3     4
  1 [  0,   -1,   -2,    0  ]   ← e.g., 1→3→4→2 = -2+2-1 = -1
  2 [  4,    0,    2,    4  ]   ← e.g., 2→1→3 = 4-2 = 2
  3 [  5,    1,    0,    2  ]   ← e.g., 3→4→2 = 2-1 = 1
  4 [  3,   -1,    1,    0  ]
```

---

## 2. Why "All Pairs"? The Big Picture

### Three Shortest Path Problems

1. **Single Pair**: shortest path from A to B (one source, one target).
2. **Single Source**: shortest paths from A to all vertices.
3. **All Pairs**: shortest paths between every pair of vertices.

### How They Relate

| Problem | Best Algorithm | Time |
|---------|---------------|------|
| Single Pair (unweighted) | BFS | O(V + E) |
| Single Source (non-neg weights) | Dijkstra | O((V+E) log V) |
| Single Source (neg weights) | Bellman-Ford | O(V × E) |
| **All Pairs** | **Floyd-Warshall** | **O(V³)** |

### When to Use Floyd-Warshall

- When you need shortest distance between **many pairs** of vertices.
- When the graph is **dense** (E close to V²).
- When V is small enough (say V ≤ 500) that V³ is acceptable.

### Alternative: Run V Dijkstras

For all-pairs with non-negative weights:
- V × O((V+E) log V) = O(V(V+E) log V).
- For dense graphs (E = V²): O(V³ log V) — actually SLOWER than Floyd-Warshall.
- For sparse graphs (E = V): O(V² log V) — FASTER than Floyd-Warshall.

So Floyd-Warshall shines on **dense graphs** or graphs with **negative weights**.

---

## 3. The DP Insight — Heart of Floyd-Warshall

### The Beautiful Idea

Floyd-Warshall is fundamentally a **dynamic programming** algorithm. The key insight:

> "Consider the shortest path from i to j. This path either uses vertex k as an intermediate stop, or it doesn't."

### The Subproblem

Define:
```
dp[k][i][j] = shortest distance from i to j, using ONLY vertices {0, 1, ..., k}
              as intermediate stops on the path.
```

Note: i and j themselves are not "intermediate" — they're the endpoints.

### The Recurrence

```
dp[k][i][j] = min(
    dp[k-1][i][j],                       // don't use vertex k
    dp[k-1][i][k] + dp[k-1][k][j]        // use vertex k
)
```

In other words:
- **Don't use k**: same as best path using {0, ..., k-1}.
- **Use k**: go from i to k (using {0, ..., k-1}), then from k to j (using {0, ..., k-1}).

### Base Case

```
dp[-1][i][j] = graph[i][j]  // no intermediate vertices allowed
```

The original adjacency matrix.

### Final Answer

```
dp[V-1][i][j] = shortest distance from i to j (using any vertices as intermediates)
```

This is the answer we want.

### Why This Works

For any path from i to j, identify the highest-numbered intermediate vertex. Call it m.
- The path = (i → ... → m) + (m → ... → j).
- Both sub-paths only use vertices < m as intermediates (otherwise m wouldn't be the highest).
- So `dist(i, m)` and `dist(m, j)` are correctly computed by `dp[m-1]`.

This is the **optimal substructure** property, fundamental to DP.

---

## 4. Walking Through the Recurrence

### The Iteration Logic

```
For k = 0, 1, 2, ..., V-1:    // For each potential intermediate vertex
  For i = 0 to V-1:            // For each source
    For j = 0 to V-1:          // For each destination
      dist[i][j] = min(dist[i][j], dist[i][k] + dist[k][j])
```

This is the **entire algorithm**. Three nested loops. O(V³).

### Visualizing "k as intermediate"

```
Initially (k=-1, no intermediates allowed):
  dist[i][j] = direct edge weight (or INF if no edge).

After k=0 phase (vertex 0 allowed as intermediate):
  dist[i][j] could now be cheaper via vertex 0.

After k=1 phase (vertices 0, 1 allowed):
  Could now route via {0, 1}.

...

After k=V-1 phase (all vertices allowed):
  All shortest paths found.
```

### The "Allowed Intermediates" Grows

Each phase ADDS one more potential intermediate vertex. After V phases, all V vertices are potential intermediates → we've considered all possible paths.

### Example of Progression

Consider this graph:
```
1 ──(4)──▶ 2 ──(3)──▶ 3
```

- Initially: dist[1][3] = INF (no direct edge).
- After k=1 (vertex 1 as intermediate): unchanged.
- After k=2 (vertex 2 as intermediate): dist[1][3] = dist[1][2] + dist[2][3] = 4 + 3 = 7. ✓
- After k=3 (vertex 3 as intermediate): unchanged.

The "answer" appears when k = the highest intermediate on the shortest path.

---

## 5. Why the Loop Order Matters (k, i, j)

This is the **most counterintuitive part** of Floyd-Warshall.

### The Correct Order

```
for k:        // OUTER: which intermediate vertex
  for i:      // MIDDLE: source
    for j:    // INNER: destination
```

### Why k Must Be Outermost

The recurrence reads from `dist[i][k]` and `dist[k][j]` (both from the previous k-iteration).

If k is the outermost loop:
- We complete ALL (i, j) pairs for one k before moving to k+1.
- When we read `dist[i][k]`, all updates for the current k are done already in the same "phase".
- This is the key property that makes in-place updates work (see Section 6).

### What If We Reorder?

#### Order (i, j, k) — WRONG

```
for i:
  for j:
    for k:
      dist[i][j] = min(dist[i][j], dist[i][k] + dist[k][j])
```

This computes dist[i][j] by trying every k. But it never gives k a chance to ITSELF be the result of a longer path through other vertices.

Example:
- Initial: dist[1][3] = INF, dist[1][2] = 4, dist[2][3] = 3.
- We want dist[1][3] = 7 (via 2).
- With (i, j, k) order: When processing dist[1][3], we try k=2. dist[1][2] + dist[2][3] = 4 + 3 = 7. ✓
- OK, this seems to work for direct cases.

But here's where it fails:
- Add a vertex 4. dist[1][4] = 1, dist[4][2] = 2, dist[2][3] = 3.
- The actual shortest from 1 to 3 is 1 + 2 + 3 = 6 (via 4 then 2).
- With (k, i, j) order:
  - k=4 phase: dist[1][2] = min(4, dist[1][4] + dist[4][2]) = min(4, 1+2) = 3.
  - k=2 phase (next): dist[1][3] = min(INF, dist[1][2] + dist[2][3]) = min(INF, 3+3) = 6. ✓

- With (i, j, k) order:
  - For (i=1, j=3): try k=2: dist[1][2] + dist[2][3] = 4 + 3 = 7.
                     try k=4: dist[1][4] + dist[4][3] = 1 + INF = INF.
                     Result: dist[1][3] = 7. WRONG (should be 6).
  
  - It missed the update to dist[1][2] (via 4) that happens later.

So **k must be the outermost loop**.

### The Right Mental Model

> "For each k in order, FIX vertex k as a 'now-allowed' intermediate. Then update ALL pairs to incorporate this new possibility."

This sequential "adding intermediates" is what makes the DP work.

---

## 6. Why In-Place Updates Work

Floyd-Warshall uses ONE 2D array, not 3D. We update `dist[i][j]` in-place as we go.

Naively, you'd think this is wrong — we need `dist[i][k]` and `dist[k][j]` from the PREVIOUS iteration (`k-1`), not the current one.

But Floyd-Warshall's loop structure makes in-place safe!

### The Argument

In phase k, we update `dist[i][j]` using `dist[i][k]` and `dist[k][j]`.

#### Claim 1: dist[i][k] doesn't change in phase k.

In phase k, we'd update `dist[i][k]` if `dist[i][k] > dist[i][k] + dist[k][k]`.

But `dist[k][k] = 0` (always, since it's a distance to itself).

So `dist[i][k] + dist[k][k] = dist[i][k] + 0 = dist[i][k]`. No improvement possible.

Therefore, `dist[i][k]` is unchanged during phase k.

#### Claim 2: dist[k][j] doesn't change in phase k.

Similarly, `dist[k][j]` in phase k considers updating to `dist[k][k] + dist[k][j] = 0 + dist[k][j] = dist[k][j]`. No change.

#### Conclusion

Both `dist[i][k]` and `dist[k][j]` are stable during phase k. So even though we use them for in-place updates of `dist[i][j]`, the values are correct.

### What This Means

We don't need a 3D DP array. The 2D in-place update is correct and saves space:
- O(V²) space instead of O(V³).

---

## 7. Walking Through the Code Section by Section

Let me walk through every part of the Java code.

### Section A: The INF Sentinel

```java
private static final long INF = (long) 1e18;
```

**Why `1e18` (and not `Long.MAX_VALUE`)?**

`Long.MAX_VALUE` is about 9.2e18. Adding two such values overflows.

`1e18` is large enough to represent "infinity" but small enough that `1e18 + 1e18 = 2e18 < Long.MAX_VALUE` — no overflow.

The C++ code uses `INT_MAX` which is 2^31 - 1 ≈ 2.1e9. Adding two `INT_MAX` overflows in `int` but fits in `long`.

Java's analog: use `long` and `1e18`.

### Section B: Initialization

```java
long[][] dist = new long[n][n];
for (int i = 0; i < n; i++) {
    dist[i] = graph[i].clone();
}
```

#### Why Clone?

We don't want to modify the input `graph[][]`. We clone each row.

`graph[i].clone()` creates a shallow copy of the i-th row.

Alternative:
```java
for (int j = 0; j < n; j++) dist[i][j] = graph[i][j];
```

Same effect, more verbose.

### Section C: The Main Triple Loop

```java
for (int k = 0; k < n; k++) {
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            if (dist[i][k] == INF || dist[k][j] == INF) continue;
            
            if (dist[i][k] + dist[k][j] < dist[i][j]) {
                dist[i][j] = dist[i][k] + dist[k][j];
            }
        }
    }
}
```

#### The k-i-j Loop Order

As explained in Section 5: k MUST be outermost.

#### The INF Check

```java
if (dist[i][k] == INF || dist[k][j] == INF) continue;
```

Why? Because adding INF to anything gives garbage (overflow concern, even with long).

The C++ code uses `INT_MAX`. If both `dist[i][k]` and `dist[k][j]` are `INT_MAX`, their sum overflows.

By skipping when either is INF, we avoid the problem entirely.

In the C++ code:
```cpp
if (dist[i][k] + dist[k][j] < dist[i][j])
```

Without the check, `INT_MAX + INT_MAX` overflows to a negative number. The check `< dist[i][j]` would be true (since negative is small) → false update.

**This is the same overflow bug we saw in Bellman-Ford** — fix is the same: explicit INF check.

#### The Relaxation

```java
if (dist[i][k] + dist[k][j] < dist[i][j]) {
    dist[i][j] = dist[i][k] + dist[k][j];
}
```

The relaxation: if going via k is shorter, update.

### Section D: Negative Cycle Detection (Optional)

```java
for (int i = 0; i < n; i++) {
    if (dist[i][i] < 0) {
        System.out.println("Negative cycle detected involving vertex " + (i + 1));
    }
}
```

After Floyd-Warshall, `dist[i][i]` represents the shortest path from i back to itself.

- Normally this is 0 (don't go anywhere).
- If there's a negative cycle through i, the shortest path is negative.

So `dist[i][i] < 0` ⟺ i is in (or can reach + come back from) a negative cycle.

### Section E: Output

```java
for (int i = 0; i < n; i++) {
    System.out.print("From " + (i + 1) + " -> ");
    for (int j = 0; j < n; j++) {
        if (dist[i][j] == INF) {
            System.out.print("INF ");
        } else {
            System.out.print(dist[i][j] + " ");
        }
    }
    System.out.println();
}
```

Print 1-indexed for human readability. Special-case INF to avoid printing a huge number.

---

## 8. C++ to Java Conversion Notes

### Differences and Translations

#### 1. Vector of Vectors

**C++**:
```cpp
vector<vector<long long>> graph;
```

**Java**:
```java
long[][] graph;
```

Java's 2D arrays are simpler and faster.

#### 2. The Size

In the C++ code:
```cpp
for (int k = 0; k < 4; k++)
```

Hardcoded `4`. Should use `n` (size). The Java version generalizes:
```java
for (int k = 0; k < n; k++)
```

#### 3. INT_MAX

**C++**:
```cpp
INT_MAX  // ≈ 2.1e9
```

**Java**:
```java
Integer.MAX_VALUE  // same
```

But for Floyd-Warshall, we use `long` and `1e18` to avoid overflow.

#### 4. long long → long

**C++**:
```cpp
long long  // 64-bit
```

**Java**:
```java
long  // 64-bit
```

Java's `long` is always 64-bit (unlike C's `long`).

#### 5. cout → System.out.println

**C++**:
```cpp
cout << "From " << i + 1 << "->";
```

**Java**:
```java
System.out.print("From " + (i + 1) + " -> ");
```

#### 6. Cloning Arrays

**C++**:
```cpp
vector<vector<long long>> dist(graph);  // copy constructor
```

**Java**:
```java
long[][] dist = new long[n][n];
for (int i = 0; i < n; i++) dist[i] = graph[i].clone();
```

Java doesn't have automatic deep copy for 2D arrays. We clone each row.

Alternative: `Arrays.copyOf` or manual loop.

---

## 9. The Overflow Problem with INT_MAX

The original C++ uses `INT_MAX` as sentinel, then adds two distances. This is a **major overflow risk**.

### The Bug

```cpp
vector<vector<long long>> graph = {
    {0, INT_MAX, -2, INT_MAX},
    // ...
};

// Later:
if (dist[i][k] + dist[k][j] < dist[i][j])
```

`INT_MAX` is `2^31 - 1 ≈ 2.147 × 10^9`. But the C++ code uses `long long` (64-bit), so `INT_MAX + INT_MAX = ~4.3 × 10^9` doesn't overflow `long long`.

So in the C++ code, even though it doesn't explicitly check for INF, it kind of gets away with it because of `long long`.

### The Java Fix

In Java, `int + int` overflows at 32 bits. So using `Integer.MAX_VALUE` is dangerous.

**Best practice**: use `long` and a smaller sentinel like `1e18` (so `1e18 + 1e18 < Long.MAX_VALUE ≈ 9.2e18`).

Plus, explicitly check `if (dist[i][k] == INF || dist[k][j] == INF) continue;`

This makes the code robust regardless of sentinel choice.

### Sentinel Best Practices

| Language | Sentinel | Why |
|----------|----------|-----|
| C++ `int` | INT_MAX/2 | Half of INT_MAX so doubled values don't overflow |
| C++ `long long` | INT_MAX | Sum fits in long long |
| Java `int` | Integer.MAX_VALUE/2 OR use `long` | Java int is 32-bit, overflow risk |
| Java `long` | (long) 1e18 | Doubled values fit in long |

For Floyd-Warshall specifically:
- **Always use `long`** for safety.
- **Use `1e18` as INF** for clean arithmetic.
- **Explicitly check for INF before adding** as defensive coding.

---

## 10. Visual Examples

### Example 1: Simple Path (No Negative Edges)

```
Graph:
   (0)──(5)──▶(1)──(3)──▶(2)

Initial matrix:
       0    1    2
  0 [  0,   5,  INF ]
  1 [ INF,  0,   3  ]
  2 [ INF, INF,  0  ]

After k=0 (vertex 0 as intermediate): no changes (no path through 0 helps).
After k=1 (vertex 1 as intermediate): 
  dist[0][2] = dist[0][1] + dist[1][2] = 5 + 3 = 8.
  
After k=2: no changes.

Final:
       0    1    2
  0 [  0,   5,   8 ]
  1 [ INF,  0,   3 ]
  2 [ INF, INF,  0 ]
```

### Example 2: With Negative Edge (No Cycle)

```
Graph:
   (0)──(5)──▶(1)
    │          │
   10          -3
    │          │
    ▼          ▼
   (2)◀────────┘

dist[0][2] direct: 10.
But also: 0 → 1 → 2 = 5 + (-3) = 2. Shorter!

After Floyd-Warshall:
       0    1    2
  0 [  0,   5,   2 ]   ← improved via 1
  1 [ INF,  0,  -3 ]
  2 [ INF, INF,  0 ]
```

### Example 3: Original C++ Example

```
Graph:
       1     2     3     4
  1 [  0,   INF,  -2,   INF ]
  2 [  4,    0,    3,   INF ]
  3 [ INF,  INF,   0,    2  ]
  4 [ INF,  -1,   INF,    0 ]

Edges: 1→3:-2, 2→1:4, 2→3:3, 3→4:2, 4→2:-1

Final matrix (after Floyd-Warshall):
       1     2     3     4
  1 [  0,   -1,   -2,    0  ]
  2 [  4,    0,    2,    4  ]
  3 [  5,    1,    0,    2  ]
  4 [  3,   -1,    1,    0  ]
```

Notice paths like:
- 1 → 3 → 4 → 2 = -2 + 2 + (-1) = -1
- 2 → 1 → 3 = 4 + (-2) = 2

---

## 11. Detailed Dry Run with Matrix Evolution

Let me trace through the original C++ example phase by phase.

### Initial Matrix

```
       1     2     3     4
  1 [  0,   INF,  -2,   INF ]
  2 [  4,    0,    3,   INF ]
  3 [ INF,  INF,   0,    2  ]
  4 [ INF,  -1,   INF,    0 ]
```

### Phase k=1 (vertex 1 as potential intermediate)

For each pair (i, j), check if going through vertex 1 helps.

```
(1,2): direct INF. Via 1: dist[1][1] + dist[1][2] = 0 + INF = INF. No help.
(1,4): direct INF. Via 1: 0 + INF = INF. No help.
(2,3): direct 3. Via 1: 4 + (-2) = 2 < 3. ← UPDATE dist[2][3] = 2.
(2,4): direct INF. Via 1: 4 + INF = INF. No help.
(3,1): direct INF. Via 1: INF + 0 = INF. No help.
(3,2): direct INF. Via 1: INF + INF = INF. No help.
(3,4): direct 2. No change.
(4,1): direct INF. Via 1: INF + 0 = INF. No help.
(4,3): direct INF. Via 1: INF + (-2) = INF. No help.

(Other pairs unchanged.)

After k=1:
       1     2     3     4
  1 [  0,   INF,  -2,   INF ]
  2 [  4,    0,    2,   INF ]   ← 2→3 improved to 2
  3 [ INF,  INF,   0,    2  ]
  4 [ INF,  -1,   INF,    0 ]
```

### Phase k=2 (vertex 2 as potential intermediate)

```
(1,2): direct INF. Via 2: dist[1][2] + dist[2][2] = INF + 0 = INF. No help.
(1,3): direct -2. Via 2: INF + 2 = INF. No help.
(1,4): direct INF. Via 2: INF + INF = INF. No help.
(3,1): direct INF. Via 2: INF + 4 = INF. No help.
(4,1): direct INF. Via 2: dist[4][2] + dist[2][1] = -1 + 4 = 3. ← UPDATE dist[4][1] = 3.
(4,3): direct INF. Via 2: -1 + 2 = 1. ← UPDATE dist[4][3] = 1.

After k=2:
       1     2     3     4
  1 [  0,   INF,  -2,   INF ]
  2 [  4,    0,    2,   INF ]
  3 [ INF,  INF,   0,    2  ]
  4 [  3,   -1,    1,    0  ]   ← 4→1 = 3, 4→3 = 1
```

### Phase k=3 (vertex 3 as potential intermediate)

```
(1,2): direct INF. Via 3: dist[1][3] + dist[3][2] = -2 + INF = INF. No help.
(1,4): direct INF. Via 3: -2 + 2 = 0. ← UPDATE dist[1][4] = 0.
(2,1): direct 4. Via 3: 2 + INF = INF. No help.
(2,4): direct INF. Via 3: 2 + 2 = 4. ← UPDATE dist[2][4] = 4.
(4,2): direct -1. Via 3: 1 + INF = INF. No help.
(4,4): direct 0. Via 3: 1 + 2 = 3 < 0? No.

After k=3:
       1     2     3     4
  1 [  0,   INF,  -2,    0  ]   ← 1→4 = 0
  2 [  4,    0,    2,    4  ]   ← 2→4 = 4
  3 [ INF,  INF,   0,    2  ]
  4 [  3,   -1,    1,    0  ]
```

### Phase k=4 (vertex 4 as potential intermediate)

```
(1,2): direct INF. Via 4: dist[1][4] + dist[4][2] = 0 + (-1) = -1. ← UPDATE dist[1][2] = -1.
(1,3): direct -2. Via 4: 0 + 1 = 1. No improvement.
(2,1): direct 4. Via 4: 4 + 3 = 7. No improvement.
(2,2): direct 0. Via 4: 4 + (-1) = 3. No improvement.
(2,3): direct 2. Via 4: 4 + 1 = 5. No improvement.
(3,1): direct INF. Via 4: 2 + 3 = 5. ← UPDATE dist[3][1] = 5.
(3,2): direct INF. Via 4: 2 + (-1) = 1. ← UPDATE dist[3][2] = 1.
(3,3): direct 0. Via 4: 2 + 1 = 3. No improvement.

Final after k=4:
       1     2     3     4
  1 [  0,   -1,   -2,    0  ]   ← 1→2 = -1
  2 [  4,    0,    2,    4  ]
  3 [  5,    1,    0,    2  ]   ← 3→1 = 5, 3→2 = 1
  4 [  3,   -1,    1,    0  ]
```

### Verification with Expected Output

Expected:
```
1: 0 -1 -2 0
2: 4 0 2 4
3: 5 1 0 2
4: 3 -1 1 0
```

✓ Match!

---

## 12. Negative Cycle Detection

### How Floyd-Warshall Detects Negative Cycles

After running the algorithm:
- `dist[i][i]` should be 0 (distance from i to itself).
- If `dist[i][i] < 0`, vertex i is part of (or can reach) a negative cycle.

### Why?

Suppose there's a cycle through i with negative total weight. Floyd-Warshall will find a path: i → ... → i with negative weight. This is `dist[i][i]`.

### Example

```
Graph:
   (0)──(1)──▶(1)
    ▲          │
    │          -3
    │          ▼
   (1)──────(2)
        1

Cycle: 0 → 1 → 2 → 0 = 1 + (-3) + 1 = -1.

After Floyd-Warshall, dist[0][0] = -1 (negative!). Negative cycle detected.
```

### Code

```java
for (int i = 0; i < n; i++) {
    if (dist[i][i] < 0) {
        // i is in a negative cycle
    }
}
```

### What If Cycle Doesn't Reach a Vertex?

If a negative cycle exists but doesn't reach vertex i, `dist[i][i]` stays 0.

Floyd-Warshall detects ALL negative cycles by checking all vertices. If ANY `dist[i][i] < 0`, there's a negative cycle somewhere in the graph.

### Robustness

This makes Floyd-Warshall **strictly more powerful** than Bellman-Ford for cycle detection:
- Bellman-Ford detects only cycles reachable from source.
- Floyd-Warshall detects cycles anywhere in the graph.

---

## 13. Edge Cases

### 1. Single Vertex

```
graph = [[0]]
After algorithm: dist[0][0] = 0. No iterations needed (trivial).
```

### 2. Disconnected Graph

```
graph = [
  [0, 5, INF],
  [INF, 0, INF],
  [INF, INF, 0]
]
dist[0][2] stays INF (unreachable).
```

Floyd-Warshall handles this naturally — INF stays INF if no path exists.

### 3. Self-Loops

```
graph[i][i] should be 0 (not the self-loop weight).

If there's a negative self-loop (e.g., graph[i][i] = -5), 
that's already a negative cycle of length 1 → dist[i][i] = -5.
```

### 4. Negative Cycle

```
Algorithm runs to completion. Then check dist[i][i] for any vertex.
If negative → cycle detected.

Distances from/to vertices in/affected-by cycle may be unreliable
(should be -∞, but we don't compute that).
```

### 5. Graph with Only Vertices, No Edges

```
graph = [
  [0, INF, INF],
  [INF, 0, INF],
  [INF, INF, 0]
]
No updates possible. Final matrix unchanged.
```

### 6. Dense Graph (Complete Graph)

```
All graph[i][j] have weights.
Floyd-Warshall still runs in O(V³) — no speedup, but no slowdown either.
```

### 7. Large V

```
For V = 1000: V³ = 10^9 operations. About 5-10 seconds.
For V = 500: V³ = 1.25 × 10^8. Easily fits in 1-2 seconds.
```

Floyd-Warshall is feasible for V ≤ 500-1000.

---

## 14. Complexity Analysis

### Time: O(V³)

Three nested loops, each going from 0 to V-1. Total: V × V × V = V³ iterations.

Each iteration does O(1) work (one comparison, one possible update).

**Total: O(V³)**.

### Space: O(V²)

The distance matrix is V × V.

If we don't need the input graph after computing distances, we can update in-place and use just one V × V matrix (which we do).

### Practical Performance

| V | V³ | Approximate Time |
|---|-----|------------------|
| 100 | 10^6 | < 10 ms |
| 500 | 1.25 × 10^8 | ~1 second |
| 1000 | 10^9 | ~5-10 seconds |
| 5000 | 1.25 × 10^11 | Too slow |

For V ≤ 1000, Floyd-Warshall is practical. For larger V, consider V × Dijkstra or other approaches.

---

## 15. Common Mistakes

### Mistake 1: Wrong Loop Order

```java
// WRONG (i, j, k order):
for (int i = 0; i < n; i++) {
    for (int j = 0; j < n; j++) {
        for (int k = 0; k < n; k++) {
            // ...
        }
    }
}

// RIGHT (k, i, j order):
for (int k = 0; k < n; k++) {
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            // ...
        }
    }
}
```

k MUST be outermost. See Section 5 for why.

### Mistake 2: Forgetting the INF Check (Overflow)

```java
// WRONG:
if (dist[i][k] + dist[k][j] < dist[i][j]) {
    // overflow if both are INF
}

// RIGHT:
if (dist[i][k] == INF || dist[k][j] == INF) continue;
if (dist[i][k] + dist[k][j] < dist[i][j]) {
    // ...
}
```

Or use `long` with a smaller INF like `1e18`.

### Mistake 3: Using Wrong INF Value

```java
// RISKY: INT_MAX in Java, additions overflow int
int INF = Integer.MAX_VALUE;

// SAFER: use long with smaller INF
long INF = (long) 1e18;
```

### Mistake 4: Not Initializing graph[i][i] = 0

```java
// WRONG: graph[i][i] = INF (incorrect — distance to self is 0)
```

Always set diagonal to 0.

### Mistake 5: Treating as Single-Source Problem

```java
// WRONG: pick a source and only compute distances FROM it
```

Floyd-Warshall computes ALL pairs. If you only need single-source, use Dijkstra (faster).

### Mistake 6: Modifying Input

```java
// WRONG: modifying input graph
floyd(graph);  // if floyd modifies graph[][] directly, caller loses original

// RIGHT: copy first
long[][] dist = clone(graph);
```

### Mistake 7: Confusing Vertex Indexing

```java
// 0-indexed vs 1-indexed confusion is common
```

Pick one convention and stick to it. The original C++ uses 0-indexed arrays but prints 1-indexed for humans.

### Mistake 8: Forgetting to Handle Negative Cycles

If the problem says "may have negative cycles":
- Without check, you'd return wrong distances silently.
- Always check `dist[i][i] < 0` after running.

---

## 16. Floyd-Warshall vs Dijkstra vs Bellman-Ford

### Comparison Table

| Algorithm | Time | Space | Handles Negative Edges? | Detects Negative Cycles? | Use Case |
|-----------|------|-------|------------------------|--------------------------|----------|
| **BFS** | O(V + E) | O(V) | N/A | N/A | Unweighted |
| **Dijkstra** | O((V+E) log V) | O(V) | NO | NO | Non-neg single source |
| **Bellman-Ford** | O(V × E) | O(V) | YES | YES (from source) | Neg edges single source |
| **Floyd-Warshall** | O(V³) | O(V²) | YES | YES (anywhere) | All-pairs |

### When to Use Each

| Scenario | Algorithm |
|----------|-----------|
| Single source, non-negative | Dijkstra |
| Single source, may have negative | Bellman-Ford |
| All pairs, may have negative | Floyd-Warshall |
| All pairs, non-negative, sparse | V × Dijkstra (faster than V³) |
| All pairs, non-negative, dense | Floyd-Warshall (cache-friendly) |

### Why Floyd-Warshall Wins for Dense Graphs

- **Floyd-Warshall**: O(V³) regardless of E.
- **V × Dijkstra**: O(V × (V + E) log V) = O(V × V² log V) = O(V³ log V) for dense graphs.

So Floyd-Warshall is **log V faster** for dense graphs.

Also, Floyd-Warshall has **excellent cache behavior** due to simple array access patterns.

### Why V × Dijkstra Wins for Sparse Graphs

For E = V:
- **Floyd-Warshall**: O(V³).
- **V × Dijkstra**: O(V × V log V) = O(V² log V).

For V = 1000: Floyd-Warshall = 10^9, V × Dijkstra = 10^7. **Dijkstra is 100x faster!**

So for **sparse non-negative graphs**, V × Dijkstra wins.

---

## 17. Path Reconstruction

Floyd-Warshall computes distances, but you might also want the actual paths.

### The Trick: Track Predecessor Matrix

In addition to `dist[i][j]`, maintain `next[i][j]` = the next vertex on the shortest path from i to j.

```java
int[][] next = new int[n][n];

// Initialize: next[i][j] = j if there's a direct edge i→j, else -1
for (int i = 0; i < n; i++) {
    for (int j = 0; j < n; j++) {
        if (graph[i][j] != INF && i != j) {
            next[i][j] = j;
        } else {
            next[i][j] = -1;
        }
    }
}

// Update during relaxation:
if (dist[i][k] + dist[k][j] < dist[i][j]) {
    dist[i][j] = dist[i][k] + dist[k][j];
    next[i][j] = next[i][k];  // path goes through k now
}
```

### Reconstructing the Path

```java
public List<Integer> getPath(int from, int to, int[][] next) {
    if (next[from][to] == -1) return new ArrayList<>();  // no path
    
    List<Integer> path = new ArrayList<>();
    path.add(from);
    int curr = from;
    while (curr != to) {
        curr = next[curr][to];
        path.add(curr);
    }
    return path;
}
```

### Why `next[i][j] = next[i][k]`?

If the shortest path i→j goes through k:
- The first step from i is the first step on the path i→k.
- Hence `next[i][j] = next[i][k]`.

---

## 18. Optimizations

### 1. Early Termination on Negative Cycle

If you detect a negative cycle early, you can stop processing.

```java
for (int k = 0; k < n; k++) {
    // ... relaxation ...
    
    // After each phase, check diagonal
    for (int i = 0; i < n; i++) {
        if (dist[i][i] < 0) {
            // negative cycle found, exit early
            return null;
        }
    }
}
```

### 2. Symmetry for Undirected Graphs

For undirected graphs, `dist[i][j] = dist[j][i]`. Can save half the work, but it's complicated to implement correctly.

### 3. Sparse Graph Optimization

For sparse graphs, V × Dijkstra is faster anyway.

### 4. Bit Manipulation for Reachability

If you only care about REACHABILITY (not distance), use boolean OR instead of min/add. Implemented with bitsets for extreme speed.

### 5. Avoid Recomputation

If the graph rarely changes, cache the distance matrix.

---

## 19. Related Problems and How to Approach Them

### Problem 1: LC 1334 — Find the City With Smallest Number of Neighbors

**Statement**: Given `n` cities and weighted edges, find the city with the **fewest other cities** within distance `distanceThreshold`.

**Approach**:

```
Step 1: Use Floyd-Warshall to compute shortest distances between all pairs.
Step 2: For each city i, count cities j where dist[i][j] <= distanceThreshold (and i != j).
Step 3: Return the city with smallest count. If tie, return the largest index.
```

**Key Insight**: This is a CLASSIC Floyd-Warshall problem. n ≤ 100, so O(n³) is fine.

**Why Floyd-Warshall over V × Dijkstra?**
- For n=100, V³ = 10^6, very fast.
- Code is simpler.
- Dense graph (lots of edges) is likely.

**Java Code**:

```java
public int findTheCity(int n, int[][] edges, int distanceThreshold) {
    int[][] dist = new int[n][n];
    int INF = 1_000_000_000;  // safe sentinel
    
    // Initialize
    for (int[] row : dist) Arrays.fill(row, INF);
    for (int i = 0; i < n; i++) dist[i][i] = 0;
    for (int[] e : edges) {
        dist[e[0]][e[1]] = e[2];
        dist[e[1]][e[0]] = e[2];  // undirected
    }
    
    // Floyd-Warshall
    for (int k = 0; k < n; k++) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (dist[i][k] + dist[k][j] < dist[i][j]) {
                    dist[i][j] = dist[i][k] + dist[k][j];
                }
            }
        }
    }
    
    // Count and find min
    int minCount = Integer.MAX_VALUE, result = -1;
    for (int i = 0; i < n; i++) {
        int count = 0;
        for (int j = 0; j < n; j++) {
            if (i != j && dist[i][j] <= distanceThreshold) count++;
        }
        if (count <= minCount) {  // <= for tie-breaking (largest index)
            minCount = count;
            result = i;
        }
    }
    return result;
}
```

### Problem 2: LC 743 — Network Delay Time

**Statement**: Given a network of nodes with edge times, find the time for a signal to reach ALL nodes from source `k`.

**Approach**:

Two options:
1. **Dijkstra** from k. Then answer = max distance.
2. **Floyd-Warshall** to compute all pairs, then answer = max(dist[k][*]).

Dijkstra is faster (single source). But Floyd-Warshall works too.

**Why pick Dijkstra here?**
- Single source.
- All weights positive.
- Dijkstra is O((V+E) log V), better than O(V³) for sparse graphs.

**However**, if asked for "shortest time from ANY source", Floyd-Warshall + take max becomes natural.

### Problem 3: LC 787 — Cheapest Flights Within K Stops

**Statement**: Find cheapest flight from src to dst with at most K stops.

**Approach**:

NOT directly Floyd-Warshall (different problem structure). Use either:
- **Bellman-Ford variant** with K+1 iterations.
- **Dijkstra with state (node, stops_used)**.

Floyd-Warshall doesn't naturally handle the K-stops constraint.

### Problem 4: LC 1462 — Course Schedule IV

**Statement**: Given course prerequisites, answer queries: "Is course A a prerequisite of course B?"

**Approach**:

This is a **transitive closure** problem. Floyd-Warshall adapted with boolean OR.

```java
public List<Boolean> checkIfPrerequisite(int n, int[][] prerequisites, int[][] queries) {
    boolean[][] reachable = new boolean[n][n];
    
    // Initialize direct prerequisites
    for (int[] p : prerequisites) {
        reachable[p[0]][p[1]] = true;
    }
    
    // Floyd-Warshall (boolean version)
    for (int k = 0; k < n; k++) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                reachable[i][j] = reachable[i][j] || (reachable[i][k] && reachable[k][j]);
            }
        }
    }
    
    // Answer queries
    List<Boolean> result = new ArrayList<>();
    for (int[] q : queries) {
        result.add(reachable[q[0]][q[1]]);
    }
    return result;
}
```

**Key Insight**: Replace `min` and `add` with `OR` and `AND`. Floyd-Warshall generalizes beyond shortest paths!

### Problem 5: LC 2642 — Design Graph With Shortest Path Calculator

**Statement**: Build a graph that supports `addEdge` and `shortestPath` queries.

**Approach**:

Option 1: Recompute Floyd-Warshall after each `addEdge`. O(V³) per add.

Option 2: Smart update — only update affected pairs:
```
When edge (u, v, w) is added:
  for i in 0..n-1:
    for j in 0..n-1:
      newDist = dist[i][u] + w + dist[v][j]
      if newDist < dist[i][j]:
        dist[i][j] = newDist
```

This is **O(V²) per update**, much better than O(V³).

For `shortestPath`: just look up `dist[u][v]`. O(1).

### Problem 6: Transitive Closure of a Directed Graph

**Statement**: Compute whether each vertex can reach every other vertex.

**Approach**: Boolean Floyd-Warshall (as in LC 1462).

**Time**: O(V³). For large graphs, BFS/DFS from each vertex is O(V × (V + E)) — possibly faster.

### Problem 7: Counting Number of Paths Between Pairs (BFS/DFS Doesn't Scale)

**Statement**: Given a DAG, count paths from i to j for all (i, j).

**Approach**: Modified Floyd-Warshall:
```
count[i][j] = count[i][j] + count[i][k] * count[k][j]
```

The "min" becomes "sum", "addition" becomes "multiplication". Different DP, similar structure.

### Problem 8: Minimum Path Through K Specific Vertices (TSP Variant)

**Statement**: Find shortest path that visits a specific subset of vertices.

**Approach**: Combine Floyd-Warshall (precompute all-pairs) + DP on subsets (bitmask DP).

```java
// First: floyd_warshall to get dist[i][j].

// Then DP: dp[mask][i] = min cost to visit vertices in mask, ending at i.
// Transition: dp[mask | (1 << j)][j] = min(dp[mask | (1 << j)][j], dp[mask][i] + dist[i][j])
```

**Time**: O(2^k × k²) where k is the number of "must visit" vertices.

### Problem 9: Currency Arbitrage Detection

**Statement**: Given currency exchange rates, detect if you can profit by cycling through currencies.

**Approach**:
1. Convert rates: `weight(i → j) = -log(rate)`. 
2. An arbitrage = a cycle with NEGATIVE total weight.
3. Run Floyd-Warshall and check `dist[i][i] < 0` for any i.

**Why log?** Multiplying rates → adding logs. Profitable cycle (product > 1) → sum of logs > 0 → with negation, sum < 0.

### Problem 10: Diameter of Weighted Graph

**Statement**: Find the longest shortest path in a graph (graph diameter).

**Approach**:
1. Floyd-Warshall to get all dist[i][j].
2. Diameter = max over all (i, j) of dist[i][j].

### How to Choose for Interview Problems

| Situation | Algorithm |
|-----------|-----------|
| Single source, non-neg | Dijkstra |
| Single source, neg | Bellman-Ford |
| **All pairs, small V (≤ 500)** | **Floyd-Warshall** |
| All pairs, large V, non-neg | V × Dijkstra |
| Transitive closure | Floyd-Warshall (boolean) |
| Count paths | Floyd-Warshall (modified) |
| Reach all + max | Dijkstra + max |

---

## 20. Complete Java Code

### Faithful Conversion

```java
import java.util.*;

public class FloydWarshall {
    private static final long INF = (long) 1e18;
    
    public static void floyd(long[][] graph) {
        int n = graph.length;
        
        long[][] dist = new long[n][n];
        for (int i = 0; i < n; i++) {
            dist[i] = graph[i].clone();
        }
        
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (dist[i][k] == INF || dist[k][j] == INF) continue;
                    if (dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                    }
                }
            }
        }
        
        // Negative cycle detection
        for (int i = 0; i < n; i++) {
            if (dist[i][i] < 0) {
                System.out.println("Negative cycle detected at vertex " + (i + 1));
            }
        }
        
        // Print
        for (int i = 0; i < n; i++) {
            System.out.print("From " + (i + 1) + " -> ");
            for (int j = 0; j < n; j++) {
                if (dist[i][j] == INF) System.out.print("INF ");
                else System.out.print(dist[i][j] + " ");
            }
            System.out.println();
        }
    }
    
    public static void main(String[] args) {
        long[][] graph = {
            {0,   INF, -2,  INF},
            {4,   0,   3,   INF},
            {INF, INF, 0,   2  },
            {INF, -1,  INF, 0  }
        };
        floyd(graph);
    }
}
```

### Production-Ready Version (With Path Reconstruction)

```java
import java.util.*;

public class FloydWarshallPro {
    private static final int INF = Integer.MAX_VALUE / 2;  // avoid overflow when doubled
    
    public static class Result {
        public int[][] dist;
        public int[][] next;  // for path reconstruction
        public boolean hasNegativeCycle;
        
        public Result(int n) {
            dist = new int[n][n];
            next = new int[n][n];
        }
    }
    
    public static Result floyd(int n, int[][] edges) {
        Result r = new Result(n);
        
        // Initialize
        for (int i = 0; i < n; i++) {
            Arrays.fill(r.dist[i], INF);
            r.dist[i][i] = 0;
            Arrays.fill(r.next[i], -1);
        }
        for (int[] e : edges) {
            if (e[2] < r.dist[e[0]][e[1]]) {
                r.dist[e[0]][e[1]] = e[2];
                r.next[e[0]][e[1]] = e[1];
            }
        }
        
        // Floyd-Warshall
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (r.dist[i][k] + r.dist[k][j] < r.dist[i][j]) {
                        r.dist[i][j] = r.dist[i][k] + r.dist[k][j];
                        r.next[i][j] = r.next[i][k];
                    }
                }
            }
        }
        
        // Negative cycle check
        for (int i = 0; i < n; i++) {
            if (r.dist[i][i] < 0) r.hasNegativeCycle = true;
        }
        
        return r;
    }
    
    public static List<Integer> getPath(Result r, int from, int to) {
        if (r.next[from][to] == -1) return new ArrayList<>();
        
        List<Integer> path = new ArrayList<>();
        path.add(from);
        int curr = from;
        while (curr != to) {
            curr = r.next[curr][to];
            path.add(curr);
        }
        return path;
    }
}
```

---

## 21. Interview Tips

### How to Approach in Interview

1. **Restate the problem**: "Find shortest distances between all pairs of vertices."
2. **Mention complexity tradeoff**: "For V ≤ 500, Floyd-Warshall is great. For sparse graphs, V × Dijkstra is faster."
3. **Explain the DP insight**: "Define dp[k][i][j] = shortest path using only {0,...,k} as intermediates."
4. **Code the triple loop**: emphasize **k is outermost**.
5. **Address overflow**: use `long` or smaller INF.
6. **Mention negative cycle detection**: check `dist[i][i] < 0`.

### Discussion Points to Score Bonus

#### 1. The DP Formulation
> "Floyd-Warshall is essentially DP. For each potential intermediate vertex k, we decide: is the path i→j shorter via k or not? Iterating k from 0 to V-1 lets us progressively allow more intermediates."

#### 2. Why k Is Outermost
> "k must be the outermost loop. Other orderings would allow `dist[i][k]` to be improved AFTER it's used for (i, j) pairs, breaking correctness."

#### 3. Negative Cycle Detection (Bonus)
> "Floyd-Warshall detects negative cycles anywhere in the graph, not just those reachable from a source. If `dist[i][i] < 0` after the algorithm, vertex i is in a negative cycle."

#### 4. Overflow Awareness
> "I use `long` and a smaller sentinel like 1e18 to avoid overflow when adding two large distances."

#### 5. When NOT to Use Floyd-Warshall
> "For sparse graphs with non-negative weights, V × Dijkstra is asymptotically faster. Floyd-Warshall wins for dense graphs or when we need negative-weight support."

### Likely Follow-Up Questions

#### Q: How would you find the actual paths, not just distances?
**A**: Track a `next[i][j]` matrix during relaxation. Reconstruct by following `next` pointers.

#### Q: How is this DP?
**A**: dp[k][i][j] = shortest path from i to j using only {0,..k} as intermediates. The recurrence min(dp[k-1][i][j], dp[k-1][i][k] + dp[k-1][k][j]) gives the standard "include vs exclude k" DP.

#### Q: Can we space-optimize from O(V²) to O(V)?
**A**: Not for storing distances — we need V² values. But we can drop the k dimension in DP (which we already do with in-place updates).

#### Q: What if V = 10000?
**A**: V³ = 10^12 — too slow. For huge V, run V Dijkstras (if non-negative) or use specialized algorithms.

#### Q: How does Floyd-Warshall compare to Bellman-Ford for all-pairs?
**A**: Running Bellman-Ford from each vertex: O(V × V × E) = O(V²E). For dense (E = V²): O(V⁴) — worse than Floyd-Warshall's O(V³).

#### Q: How would you detect WHICH negative cycle a vertex belongs to?
**A**: After Floyd-Warshall, vertices in negative cycles have `dist[i][i] < 0`. To find a specific cycle, track parent during relaxation and reconstruct.

#### Q: What's Johnson's algorithm?
**A**: For sparse graphs with negative weights, Johnson's combines Bellman-Ford (reweighting) + V Dijkstras. O(V² log V + VE) — better than Floyd-Warshall for sparse.

### Common Interview Mistakes

1. Wrong loop order (k must be outermost).
2. Integer overflow without INF check.
3. Using Floyd-Warshall when Dijkstra suffices.
4. Forgetting `dist[i][i] = 0` initialization.
5. Not knowing how to reconstruct paths.

---

## TL;DR

### The Mental Model

```
For each intermediate vertex k (in order):
  For each pair (i, j):
    Can we improve dist[i][j] by going through k?
    if dist[i][k] + dist[k][j] < dist[i][j]:
      dist[i][j] = dist[i][k] + dist[k][j]
```

### The Algorithm in 30 Seconds

```
Initialize dist = adjacency matrix.
For k = 0 to V-1:
  For i = 0 to V-1:
    For j = 0 to V-1:
      dist[i][j] = min(dist[i][j], dist[i][k] + dist[k][j])

Negative cycle: dist[i][i] < 0 for some i.
```

### The Five Key Insights

1. **DP insight**: progressively allow more intermediate vertices.
2. **k must be outermost** — other orderings break correctness.
3. **In-place updates work** because dist[i][k] and dist[k][j] don't change during phase k.
4. **Overflow check** is essential — explicit `if (INF) continue` or use long.
5. **Detects negative cycles anywhere** — check `dist[i][i] < 0`.

### C++ to Java Cheatsheet

| C++ | Java |
|-----|------|
| `vector<vector<long long>>` | `long[][]` |
| `INT_MAX` | `(long) 1e18` (safer) |
| `auto edge : graph` | `for (int[] edge : graph)` |
| `graph[i][j]` | `graph[i][j]` (same!) |
| `cout << x << endl` | `System.out.println(x)` |
| `long long` | `long` |

### Final Code Snippet to Memorize

```java
public int[][] floydWarshall(int n, int[][] edges) {
    int[][] dist = new int[n][n];
    int INF = Integer.MAX_VALUE / 2;
    
    for (int[] row : dist) Arrays.fill(row, INF);
    for (int i = 0; i < n; i++) dist[i][i] = 0;
    for (int[] e : edges) dist[e[0]][e[1]] = e[2];
    
    for (int k = 0; k < n; k++) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (dist[i][k] + dist[k][j] < dist[i][j]) {
                    dist[i][j] = dist[i][k] + dist[k][j];
                }
            }
        }
    }
    
    return dist;
}
```

### When This Problem Appears

| Tier | Frequency | Example |
|------|-----------|---------|
| Tier 1 | Rare | Basic version |
| Tier 2 | Sometimes (LC 1334) | Paytm, Flipkart |
| Tier 3 | Often (with twists) | Google, Amazon (LC 1462, 2642) |
| Tier 4 | Variations (TSP, currency) | Top quant |

---

*Master Floyd-Warshall and you've learned a beautifully simple O(V³) DP for all-pairs shortest paths. The same pattern (triple loop with k outermost) generalizes to transitive closure, path counting, and even more exotic problems. The "iterate over intermediate vertices" framework is a powerful idea worth keeping in your toolkit.*
