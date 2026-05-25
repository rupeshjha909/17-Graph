# Multi-Source BFS — Distance to Nearest "1" Cell

A line-by-line, in-depth explanation of the **multi-source BFS** technique for grid distance problems. Covers both the **brute force O((n×m)²)** approach and the **efficient O(n×m)** approach, with detailed Java implementations, theory, and related interview problems.

---

## Table of Contents

1. [Problem Statement](#1-problem-statement)
2. [Why This Problem Is Important](#2-why-this-problem-is-important)
3. [The Brute Force Approach](#3-the-brute-force-approach)
4. [Why Brute Force Is Slow](#4-why-brute-force-is-slow)
5. [The Big Insight: Multi-Source BFS](#5-the-big-insight-multi-source-bfs)
6. [Multi-Source BFS — Step by Step](#6-multi-source-bfs--step-by-step)
7. [Why Multi-Source BFS Works](#7-why-multi-source-bfs-works)
8. [Manhattan Distance and BFS](#8-manhattan-distance-and-bfs)
9. [Walking Through the Code Section by Section](#9-walking-through-the-code-section-by-section)
10. [C++ to Java Conversion Notes](#10-c-to-java-conversion-notes)
11. [Visual Examples](#11-visual-examples)
12. [Detailed Dry Run with Diagrams](#12-detailed-dry-run-with-diagrams)
13. [Edge Cases](#13-edge-cases)
14. [Complexity Analysis](#14-complexity-analysis)
15. [Common Mistakes](#15-common-mistakes)
16. [Multi-Source BFS vs Single-Source BFS](#16-multi-source-bfs-vs-single-source-bfs)
17. [The In-Place DP Alternative (Two-Pass)](#17-the-in-place-dp-alternative-two-pass)
18. [Related Problems and How to Approach Them](#18-related-problems-and-how-to-approach-them)
19. [Complete Java Code](#19-complete-java-code)
20. [Interview Tips](#20-interview-tips)

---

## 1. Problem Statement

> Given an `n × m` grid of 0s and 1s, find for each cell the **Manhattan distance to the nearest cell containing 1**.
>
> If a cell contains 1, its distance is 0.

### Input

A grid like:
```
0 0 0
0 1 0
1 1 1
```

### Output

A matrix where each cell shows the Manhattan distance to the nearest 1:
```
2 1 2
1 0 1
0 0 0
```

### Manhattan Distance

For cells (r1, c1) and (r2, c2): `|r1 - r2| + |c1 - c2|`.

It's the "taxicab" distance — moving in straight lines along grid axes.

### Real Example

```
Grid:
  Row 0: 0 0 0
  Row 1: 0 1 0
  Row 2: 0 0 0

For cell (0, 0):
  Nearest 1 is at (1, 1).
  Manhattan = |0-1| + |0-1| = 2.

For cell (0, 1):
  Nearest 1 is at (1, 1).
  Manhattan = |0-1| + |1-1| = 1.

For cell (1, 1):
  Itself is 1.
  Distance = 0.

Output:
  2 1 2
  1 0 1
  2 1 2
```

---

## 2. Why This Problem Is Important

### Pattern Importance

**Multi-source BFS** is a fundamental pattern that appears in many interview problems:
- "Distance to nearest X" problems (LC 542, 994, 1162).
- "Time for spreading" problems (rotten oranges, infection).
- "Reach all cells from multiple starts" problems.

Once you internalize this pattern, you'll recognize it in 10+ LeetCode problems.

### Algorithmic Significance

This problem teaches:
- **When BFS gives shortest distance** (unweighted/uniform edges).
- **How to handle multiple sources** efficiently.
- **The brute-force-to-efficient transformation** — a classic interview skill.

---

## 3. The Brute Force Approach

### The Idea

> For each cell containing 1, scan ALL cells in the grid and update their distance with the Manhattan distance.

### Pseudocode

```
For each cell (i, j):
  if grid[i][j] == 1:
    For each cell (ii, jj):
      manhattan = |i - ii| + |j - jj|
      if manhattan < dist[ii][jj]:
        dist[ii][jj] = manhattan
```

### Java Code

```java
public static int[][] bruteForce(int n, int m, int[][] grid) {
    int[][] dist = new int[n][m];
    for (int i = 0; i < n; i++) Arrays.fill(dist[i], Integer.MAX_VALUE);
    
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < m; j++) {
            if (grid[i][j] == 1) {
                for (int ii = 0; ii < n; ii++) {
                    for (int jj = 0; jj < m; jj++) {
                        int manhattan = Math.abs(ii - i) + Math.abs(jj - j);
                        if (manhattan < dist[ii][jj]) {
                            dist[ii][jj] = manhattan;
                        }
                    }
                }
            }
        }
    }
    return dist;
}
```

### Visualization

```
Grid (3x3):
  0 0 0
  0 1 0
  0 0 0

Only one '1' at (1, 1).

For source (1, 1):
  Update dist[0][0] = |1-0|+|1-0| = 2
  Update dist[0][1] = |1-0|+|1-1| = 1
  Update dist[0][2] = |1-0|+|1-2| = 2
  Update dist[1][0] = |1-1|+|1-0| = 1
  Update dist[1][1] = 0
  ...
  
Final:
  2 1 2
  1 0 1
  2 1 2
```

### Why It Works

We're computing the Manhattan distance from each source to every cell, and keeping the minimum across all sources. This is correct by definition.

---

## 4. Why Brute Force Is Slow

### Complexity

- Outer loops: `n × m` iterations (for each cell).
- For each source cell, inner loops: `n × m` iterations.
- **Total: O((n × m)²)**.

### Concrete Numbers

For a 100 × 100 grid:
- Cells: 10,000.
- If half are sources: 5,000 × 10,000 = 50,000,000 operations.
- Still fast (~50 ms).

For a 1000 × 1000 grid:
- Cells: 1,000,000.
- If half are sources: 500,000 × 1,000,000 = **5 × 10¹¹ operations**.
- **Way too slow** (hours).

### The Inefficiency

Every source iterates over ALL cells. Most of that work is wasted — we don't need to compute distance from every source to every target, just to the **nearest source for each target**.

### What We Need

> "For each cell, find the distance to the NEAREST source."

This is fundamentally different from "for each source, update all cells". We want a single efficient pass that grows out from all sources simultaneously.

**Enter multi-source BFS.**

---

## 5. The Big Insight: Multi-Source BFS

### The Key Idea

> "Add ALL sources to the BFS queue at the START with distance 0. Then BFS outward. The first time we visit a cell, that's its shortest distance to the NEAREST source."

### Why This Works

Imagine multiple "waves" expanding from all sources simultaneously. The first wave to hit a cell is from the nearest source — and BFS naturally explores in increasing order of distance.

### Visualization

```
Initially (sources at (0,0) and (2,3)):
  Queue: [(0,0,0), (2,3,0)]
  
  S . . .
  . . . .
  . . . S

After step 1 (wavefront at distance 1):
  S 1 . .
  1 . . 1
  . . 1 S

After step 2 (wavefront at distance 2):
  S 1 2 .
  1 2 2 1
  2 2 1 S

After step 3:
  S 1 2 3   ← cell (0,3) is closer to source (0,0) → distance 3
  1 2 2 1   ← but actually closer to source (2,3) → distance 2? 
  2 2 1 S

Wait, let me redo this with correct distances.
```

Let me redo with Manhattan distance carefully (BFS uses 4-directional movement, which gives Manhattan distance):

```
Initially:
  S . . .       (0,0)=S, (2,3)=S
  . . . .
  . . . S

BFS expands from both:
  Distance 0: (0,0) and (2,3)
  Distance 1: neighbors of (0,0) = (0,1),(1,0). neighbors of (2,3) = (1,3),(2,2).
  Distance 2: ... and so on.

Final distances:
  0 1 2 2   ← (0,3) is dist 2 from (2,3)? No: |0-2|+|3-3|=2 ✓
  1 2 2 1
  2 2 1 0
```

Wait, that doesn't match my earlier example output. Let me recompute. From the verified output:
```
[0, 1, 2, 2]
[1, 2, 2, 1]
[2, 2, 1, 0]
```

For cell (0, 2): 
- Distance to (0,0): |0-0|+|2-0| = 2.
- Distance to (2,3): |0-2|+|2-3| = 3.
- Minimum = 2. ✓

For cell (0, 3):
- Distance to (0,0): |0-0|+|3-0| = 3.
- Distance to (2,3): |0-2|+|3-3| = 2.
- Minimum = 2. ✓

The BFS correctly finds these.

### The Magic

BFS visits cells in order of increasing distance. With multiple sources in the queue at distance 0, the BFS naturally:
1. Visits all sources first (distance 0).
2. Visits their neighbors (distance 1).
3. Visits cells 2 steps away (distance 2).
4. ... and so on.

The first source to "reach" a cell wins — that's the nearest.

---

## 6. Multi-Source BFS — Step by Step

### The Algorithm

```
1. Initialize a queue.
2. For each cell (i, j) with grid[i][j] == 1:
     Add (i, j, 0) to queue.
     Mark dist[i][j] = 0.
     Mark visited[i][j] = true.
3. While queue is not empty:
     Dequeue (r, c, d).
     For each of 4 neighbors (nr, nc):
       If valid and not visited:
         Mark visited.
         dist[nr][nc] = d + 1.
         Enqueue (nr, nc, d + 1).
4. Return dist.
```

### Java Code

```java
public static int[][] multiSourceBFS(int n, int m, int[][] grid) {
    int[][] dist = new int[n][m];
    boolean[][] visited = new boolean[n][m];
    Queue<int[]> queue = new LinkedList<>();
    
    // Initialize
    for (int i = 0; i < n; i++) {
        Arrays.fill(dist[i], Integer.MAX_VALUE);
        for (int j = 0; j < m; j++) {
            if (grid[i][j] == 1) {
                queue.offer(new int[]{i, j, 0});
                dist[i][j] = 0;
                visited[i][j] = true;
            }
        }
    }
    
    // BFS
    int[][] directions = {{-1,0},{1,0},{0,-1},{0,1}};
    while (!queue.isEmpty()) {
        int[] curr = queue.poll();
        int r = curr[0], c = curr[1], d = curr[2];
        
        for (int[] dir : directions) {
            int nr = r + dir[0];
            int nc = c + dir[1];
            if (nr < 0 || nr >= n || nc < 0 || nc >= m) continue;
            if (visited[nr][nc]) continue;
            
            visited[nr][nc] = true;
            dist[nr][nc] = d + 1;
            queue.offer(new int[]{nr, nc, d + 1});
        }
    }
    
    return dist;
}
```

### The Three Phases

#### Phase 1: Initialize Sources
All cells with value 1 go into the queue at distance 0. They're the "starts" of multiple BFS waves.

#### Phase 2: BFS Outward
Process the queue in FIFO order. Each dequeue is the smallest-distance cell in the queue.

#### Phase 3: First Visit Wins
The first time a cell is added to the queue, we record its distance. We don't revisit (the `visited` check).

---

## 7. Why Multi-Source BFS Works

### The Theorem

> "When all sources are added to the BFS queue at distance 0, the FIRST time a cell is visited gives its shortest distance to ANY source."

### Proof Intuition

BFS visits cells in **non-decreasing order of distance**.

Consider any cell `c` and its nearest source `s` (at Manhattan distance `d`).
- After d steps of BFS from s alone, c would be visited.
- With multi-source BFS starting from many sources simultaneously, c could be visited earlier only by a closer source — but s is the nearest, so no closer source exists.
- Therefore c is visited at exactly step d.

### Formal Argument

Let `f(c)` be the distance to the nearest source. We want to show BFS computes `f(c)` correctly.

**Claim**: After BFS, `dist[c] = f(c)` for all cells c.

**Proof by induction on f(c)**:

**Base case**: f(c) = 0 means c is itself a source. We initialize `dist[c] = 0`. ✓

**Inductive step**: Assume cells at distance ≤ k-1 are correctly labeled.

For cell c with f(c) = k:
- There's a path of length k from some source s to c.
- The second-to-last cell on this path, call it c', has f(c') ≤ k-1 (it's one step closer to s).
- By induction, c' was correctly visited with dist = k-1.
- When c' was processed, it tried to visit c.
- If c wasn't visited yet, dist[c] = k = f(c). ✓
- If c was already visited, it must have been at distance ≤ k-1, contradicting f(c) = k.

QED.

---

## 8. Manhattan Distance and BFS

### The Connection

In a 4-connected grid (no diagonal moves), BFS distance equals **Manhattan distance**:

```
Cell (r1, c1) and (r2, c2):
  Manhattan distance = |r1 - r2| + |c1 - c2|
  BFS distance (in 4-connected grid) = same as Manhattan
```

### Why?

To go from A to B in a 4-connected grid, you must change row `|r1-r2|` times and column `|c1-c2|` times. Each move is one step. Total: `|r1-r2| + |c1-c2|`.

BFS finds the shortest path, which matches this.

### What About 8-Connected (Diagonal Moves)?

With diagonal moves allowed, BFS distance equals **Chebyshev distance**:
```
Chebyshev distance = max(|r1 - r2|, |c1 - c2|)
```

A diagonal move covers one row AND one column step simultaneously.

### Important Distinction

| Grid Movement | BFS Computes |
|---------------|--------------|
| 4-directional | Manhattan distance |
| 8-directional | Chebyshev distance |
| Weighted | Not BFS — use Dijkstra |

For this problem, we want Manhattan distance → 4-directional BFS.

---

## 9. Walking Through the Code Section by Section

### Section A: Initialization

```java
int[][] dist = new int[n][m];
boolean[][] visited = new boolean[n][m];
Queue<int[]> queue = new LinkedList<>();

for (int i = 0; i < n; i++) {
    Arrays.fill(dist[i], Integer.MAX_VALUE);
    for (int j = 0; j < m; j++) {
        if (grid[i][j] == 1) {
            queue.offer(new int[]{i, j, 0});
            dist[i][j] = 0;
            visited[i][j] = true;
        }
    }
}
```

#### What Happens Here

1. Create `dist` matrix initialized to "infinity" (Integer.MAX_VALUE).
2. Create `visited` boolean matrix.
3. Create a queue for BFS.
4. Iterate over the grid:
   - If cell is a source (value 1):
     - Add to queue with distance 0.
     - Mark dist = 0.
     - Mark as visited.

#### Why Mark Visited Immediately?

If we don't mark sources as visited, when their neighbors are processed and try to revisit them, they'd be re-added to the queue (wasted work).

### Section B: BFS Loop

```java
int[][] directions = {{-1,0},{1,0},{0,-1},{0,1}};
while (!queue.isEmpty()) {
    int[] curr = queue.poll();
    int r = curr[0], c = curr[1], d = curr[2];
    
    for (int[] dir : directions) {
        int nr = r + dir[0];
        int nc = c + dir[1];
        if (nr < 0 || nr >= n || nc < 0 || nc >= m) continue;
        if (visited[nr][nc]) continue;
        
        visited[nr][nc] = true;
        dist[nr][nc] = d + 1;
        queue.offer(new int[]{nr, nc, d + 1});
    }
}
```

#### The `directions` Array

`{{-1,0},{1,0},{0,-1},{0,1}}` represents up, down, left, right.

This is a **common pattern** for grid problems — define directions as offsets.

#### The Inner Loop

For each direction, compute the neighbor cell `(nr, nc)`:
- Bounds check: must be inside grid.
- Visited check: skip if already visited.
- Otherwise: mark visited, set distance, enqueue.

#### Why `d + 1`?

We're one step further from the source. BFS layer by layer.

### Section C: Alternative — No `visited` Array

You can skip the `visited` array if you check `dist[nr][nc] == Integer.MAX_VALUE` instead:

```java
if (dist[nr][nc] != Integer.MAX_VALUE) continue;  // already visited
dist[nr][nc] = d + 1;
queue.offer(new int[]{nr, nc, d + 1});
```

This works because once dist is set, the cell is "visited". Saves memory.

### Section D: Brute Force vs BFS — Same Output

Both approaches produce the same `dist` matrix. The BFS just does it in O(n*m) instead of O((n*m)²).

---

## 10. C++ to Java Conversion Notes

### Differences and Translations

#### 1. 2D Arrays

**C++**:
```cpp
int a[n][m];
int dist[n][m];
```

Variable-length arrays (VLA) — actually a GCC extension, not standard C++.

**Java**:
```java
int[][] grid = new int[n][m];
int[][] dist = new int[n][m];
```

Standard 2D array allocation.

#### 2. INT_MAX

**C++**:
```cpp
dist[i][j] = INT_MAX;
```

**Java**:
```java
Arrays.fill(dist[i], Integer.MAX_VALUE);
```

Same value (~2.1 × 10^9).

#### 3. pair<int, int>

**C++**:
```cpp
pair<int, int> nearest[n][m];
nearest[ii][jj] = {i, j};
```

**Java**:
```java
int[][][] nearest = new int[n][m][2];
nearest[ii][jj][0] = i;
nearest[ii][jj][1] = j;
```

Java lacks a native pair type. Use `int[]` of length 2.

#### 4. Queue

C++ doesn't use a queue in the brute force version. For BFS:

**C++**:
```cpp
queue<tuple<int, int, int>> q;
q.push({i, j, 0});
auto [r, c, d] = q.front(); q.pop();
```

**Java**:
```java
Queue<int[]> queue = new LinkedList<>();
queue.offer(new int[]{i, j, 0});
int[] curr = queue.poll();
int r = curr[0], c = curr[1], d = curr[2];
```

Java's `LinkedList` implements `Queue`. `ArrayDeque` is also a good choice.

#### 5. abs

**C++**: `abs(x)` (in <cstdlib>).

**Java**: `Math.abs(x)`.

#### 6. min

**C++**: `min(a, b)` (in <algorithm>).

**Java**: `Math.min(a, b)`.

#### 7. cin/cout

**C++**: `cin >> n; cout << x;`

**Java**: `scanner.nextInt(); System.out.println(x);`

---

## 11. Visual Examples

### Example 1: Single Source

```
Grid:        Distance:
0 0 0        2 1 2
0 1 0   →    1 0 1
0 0 0        2 1 2

BFS from (1,1) expands in waves:
  Wave 0: (1,1)
  Wave 1: (0,1), (1,0), (1,2), (2,1)
  Wave 2: (0,0), (0,2), (2,0), (2,2)
```

### Example 2: Multiple Sources

```
Grid:        Distance:
0 0 0 0      2 1 1 2
0 1 0 0  →   1 0 1 2
0 1 1 0      1 0 0 1

BFS starts from all three 1s simultaneously.
The "waves" meet at the boundary between source regions.
```

### Example 3: All Zeros (No Sources)

```
Grid:        Distance:
0 0 0        INF INF INF
0 0 0   →    INF INF INF
0 0 0        INF INF INF

Queue starts empty. BFS does nothing. All cells stay INF.
(Or the problem may guarantee at least one source.)
```

### Example 4: All Ones

```
Grid:        Distance:
1 1 1        0 0 0
1 1 1   →    0 0 0
1 1 1        0 0 0

Every cell is a source. All distances are 0.
```

---

## 12. Detailed Dry Run with Diagrams

Let's trace through this 4×4 example:

```
Grid:
  1 0 0 0
  0 0 0 0
  0 0 0 1
  0 0 0 0
```

Two sources: (0, 0) and (2, 3).

### Initialization

```
dist:                    visited:                  Queue:
  0    INF  INF  INF      T  F  F  F                [(0,0,0), (2,3,0)]
  INF  INF  INF  INF      F  F  F  F
  INF  INF  INF  0        F  F  F  T
  INF  INF  INF  INF      F  F  F  F
```

### Iteration 1: Pop (0, 0, 0)

Neighbors of (0,0): up (out), down (1,0), left (out), right (0,1).

```
- (1, 0): not visited. Mark visited, dist=1. Enqueue.
- (0, 1): not visited. Mark visited, dist=1. Enqueue.

dist:                    Queue:
  0    1    INF  INF      [(2,3,0), (1,0,1), (0,1,1)]
  1    INF  INF  INF
  INF  INF  INF  0
  INF  INF  INF  INF
```

### Iteration 2: Pop (2, 3, 0)

Neighbors of (2,3): up (1,3), down (3,3), left (2,2), right (out).

```
- (1, 3): not visited. dist=1. Enqueue.
- (3, 3): not visited. dist=1. Enqueue.
- (2, 2): not visited. dist=1. Enqueue.

dist:                    Queue:
  0    1    INF  INF      [(1,0,1), (0,1,1), (1,3,1), (3,3,1), (2,2,1)]
  1    INF  INF  1
  INF  INF  1    0
  INF  INF  INF  1
```

### Iteration 3: Pop (1, 0, 1)

Neighbors: (0,0) visited, (2,0), (1,1), out-of-bounds.

```
- (2, 0): dist=2. Enqueue.
- (1, 1): dist=2. Enqueue.

dist:                    
  0    1    INF  INF      
  1    2    INF  1        
  2    INF  1    0        
  INF  INF  INF  1        
```

### Iteration 4: Pop (0, 1, 1)

Neighbors: (0,0) visited, (1,1) visited, (0,2).

```
- (0, 2): dist=2. Enqueue.

dist:                    
  0    1    2    INF      
  1    2    INF  1        
  2    INF  1    0        
  INF  INF  INF  1        
```

### Iteration 5: Pop (1, 3, 1)

Neighbors: (0,3), (2,3) visited, (1,2).

```
- (0, 3): dist=2. Enqueue.
- (1, 2): dist=2. Enqueue.

dist:                    
  0    1    2    2        
  1    2    2    1        
  2    INF  1    0        
  INF  INF  INF  1        
```

### Iteration 6: Pop (3, 3, 1)

Neighbors: (2,3) visited, out-of-bounds, (3,2).

```
- (3, 2): dist=2. Enqueue.

dist:                    
  0    1    2    2        
  1    2    2    1        
  2    INF  1    0        
  INF  INF  2    1        
```

### Iteration 7: Pop (2, 2, 1)

Neighbors: (1,2) visited, (3,2) visited, (2,1), (2,3) visited.

```
- (2, 1): dist=2. Enqueue.

dist:                    
  0    1    2    2        
  1    2    2    1        
  2    2    1    0        
  INF  INF  2    1        
```

### Iteration 8: Pop (2, 0, 2)

Neighbors: (1,0) visited, (3,0), (2,1) visited.

```
- (3, 0): dist=3. Enqueue.

dist:                    
  0    1    2    2        
  1    2    2    1        
  2    2    1    0        
  3    INF  2    1        
```

### Continue...

(Similar iterations process remaining cells.)

### Final Result

```
dist:
  0    1    2    2
  1    2    2    1
  2    2    1    0
  3    3    2    1

Final state matches the verified Python output.
```

---

## 13. Edge Cases

### 1. Empty Grid (n=0 or m=0)

```
No cells. Return empty grid.
```

### 2. No Sources (All Zeros)

```
Queue is empty initially. BFS does nothing.
All distances remain INF.

Problem may or may not allow this. Often guaranteed at least one source.
```

### 3. All Sources (All Ones)

```
Every cell is in the queue at distance 0.
All distances are 0.
```

### 4. Single Cell Grid

```
1x1 grid with value 1: dist[0][0] = 0.
1x1 grid with value 0: dist[0][0] = INF (no source to reach).
```

### 5. Single Row or Column

```
Row: [0, 1, 0, 0, 1]
Distances: [1, 0, 1, 1, 0]

Single-row/column BFS works fine. Manhattan distance simplifies to |col difference|.
```

### 6. Sources at Corners

```
1 0 0
0 0 0
0 0 1

Both corners are sources. Cell (1,1) has min(2, 2) = 2.
```

### 7. Disconnected Components in 0s

```
Doesn't matter — BFS reaches everything reachable. 
In a grid, all 0-cells are reachable from at least one source if the grid is connected.
```

### 8. Very Large Grid

```
For grid 1000×1000:
  Brute force: O(10^12) — too slow.
  Multi-source BFS: O(10^6) — easy.
```

---

## 14. Complexity Analysis

### Brute Force

**Time**: O((n × m)²)
- For each source: O(n × m) work to update all cells.
- At most n × m sources.
- Total: O((n × m)²).

**Space**: O(n × m) for the dist matrix.

### Multi-Source BFS

**Time**: O(n × m)
- Each cell is enqueued at most once (due to `visited`).
- Each enqueue/dequeue is O(1).
- Each cell processes 4 neighbors → constant work.
- **Total: O(n × m)**.

**Space**: O(n × m)
- dist matrix.
- visited matrix (or fused with dist).
- Queue can hold up to O(n × m) entries.

### Comparison

| Grid Size | Brute Force | Multi-Source BFS |
|-----------|-------------|------------------|
| 10 × 10 | 10,000 | 100 |
| 100 × 100 | 10⁸ | 10⁴ |
| 1000 × 1000 | 10¹² (years!) | 10⁶ (instant) |

**Speedup is enormous** for large grids.

---

## 15. Common Mistakes

### Mistake 1: Single-Source BFS Per Source

```java
// WRONG: BFS once per source — O((n*m)^2) total
for each source:
    BFS from this source, update dist;
```

This is essentially the brute force in disguise. Use multi-source BFS instead.

### Mistake 2: Forgetting to Initialize Sources at Distance 0

```java
// WRONG: only mark sources as visited but not in queue
visited[i][j] = true;
// forgot: queue.offer(...) and dist[i][j] = 0

// → BFS won't start from these sources.
```

### Mistake 3: Not Marking Sources as Visited

```java
// WRONG: enqueue sources but don't mark visited
queue.offer(new int[]{i, j, 0});
// forgot: visited[i][j] = true;

// → Sources will be revisited when processed as neighbors of other cells.
```

### Mistake 4: Forgetting Bounds Check

```java
// WRONG:
int nr = r + dir[0];
int nc = c + dir[1];
if (visited[nr][nc]) continue;  // ← ArrayIndexOutOfBoundsException if out of bounds!

// RIGHT:
if (nr < 0 || nr >= n || nc < 0 || nc >= m) continue;
if (visited[nr][nc]) continue;
```

### Mistake 5: Using DFS Instead of BFS

```java
// WRONG: DFS doesn't give shortest distance
dfs(i, j, 0);
```

DFS goes deep first, so it might assign a larger distance to a cell that could be reached more shortly via another path.

**BFS is essential for shortest distance in unweighted/uniform grids.**

### Mistake 6: Using Dijkstra (Overkill)

```java
// SLOW: Dijkstra with PQ for unit edges
PriorityQueue<int[]> pq = new PriorityQueue<>(...);
```

For uniform edge weights (all 1), BFS is equivalent and faster (O(V+E) vs O((V+E) log V)).

Only use Dijkstra if edges have different weights.

### Mistake 7: Wrong Movement Type

```java
// For Chebyshev (diagonal allowed):
int[][] directions = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};  // 8-direction

// For Manhattan (no diagonal):
int[][] directions = {{-1,0},{1,0},{0,-1},{0,1}};  // 4-direction
```

The problem says Manhattan distance → 4-directional BFS.

### Mistake 8: Inefficient Queue

```java
// SLOW: LinkedList in Java has some overhead
Queue<int[]> queue = new LinkedList<>();

// FASTER:
Deque<int[]> queue = new ArrayDeque<>();
queue.offer(...);
queue.poll();
```

`ArrayDeque` is generally faster than `LinkedList` for queue operations.

---

## 16. Multi-Source BFS vs Single-Source BFS

### Single-Source BFS

Starts from ONE vertex. Computes distance from that vertex to all others.

```java
public int[][] singleSourceBFS(int startR, int startC, int[][] grid) {
    // BFS only from (startR, startC)
    queue.offer(new int[]{startR, startC, 0});
    // ...
}
```

### Multi-Source BFS

Starts from MULTIPLE vertices simultaneously. Computes distance to NEAREST source for each vertex.

```java
public int[][] multiSourceBFS(int[][] grid) {
    // Enqueue ALL sources at start
    for each source:
        queue.offer(new int[]{r, c, 0});
    // BFS continues normally
}
```

### Key Difference

| | Single-Source | Multi-Source |
|-|---------------|--------------|
| **Sources in queue at start** | 1 | All |
| **Distance computed** | From source X | From NEAREST source |
| **Use case** | "From vertex X" | "To nearest of many" |

### When to Use Multi-Source

Anytime the problem asks "distance to nearest X" or "time for X to spread", multi-source BFS is the tool.

Examples:
- LC 542: 01 Matrix.
- LC 994: Rotting Oranges.
- LC 1162: As Far From Land as Possible.

---

## 17. The In-Place DP Alternative (Two-Pass)

For LC 542 specifically, there's a clever **two-pass DP** that's even faster (and simpler):

### The Idea

For each cell (i, j), the distance is the minimum of:
- 1 + distance of left neighbor (i, j-1).
- 1 + distance of top neighbor (i-1, j).
- 1 + distance of right neighbor (i, j+1).
- 1 + distance of bottom neighbor (i+1, j).

But we don't have all neighbors computed yet! So we do two passes:

**Pass 1 (top-left to bottom-right)**: consider left and top neighbors.
**Pass 2 (bottom-right to top-left)**: consider right and bottom neighbors.

After both passes, every cell has the correct minimum.

### Java Code

```java
public int[][] dpApproach(int[][] mat) {
    int n = mat.length, m = mat[0].length;
    int INF = n + m;  // safe upper bound for grid distance
    int[][] dist = new int[n][m];
    
    // Initialize: 0 if source, INF otherwise
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < m; j++) {
            dist[i][j] = (mat[i][j] == 1) ? 0 : INF;
        }
    }
    
    // Pass 1: top-left to bottom-right
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < m; j++) {
            if (i > 0) dist[i][j] = Math.min(dist[i][j], dist[i-1][j] + 1);
            if (j > 0) dist[i][j] = Math.min(dist[i][j], dist[i][j-1] + 1);
        }
    }
    
    // Pass 2: bottom-right to top-left
    for (int i = n - 1; i >= 0; i--) {
        for (int j = m - 1; j >= 0; j--) {
            if (i < n-1) dist[i][j] = Math.min(dist[i][j], dist[i+1][j] + 1);
            if (j < m-1) dist[i][j] = Math.min(dist[i][j], dist[i][j+1] + 1);
        }
    }
    
    return dist;
}
```

### Complexity

- Time: O(n × m) — two passes.
- Space: O(1) extra (besides output).

### Trade-offs

| Approach | Time | Space | Generalizes to weighted? |
|----------|------|-------|--------------------------|
| Multi-Source BFS | O(n × m) | O(n × m) | No (use Dijkstra) |
| Two-Pass DP | O(n × m) | O(1) extra | No |
| Dijkstra | O((n×m) log(n×m)) | O(n × m) | Yes |

BFS is more general (works for any 0-1 weight). DP is more space-efficient.

For LC 542, both BFS and DP get accepted with similar runtime.

---

## 18. Related Problems and How to Approach Them

### Problem 1: LC 542 — 01 Matrix

**Statement**: This exact problem. For each cell, find distance to nearest 0.

**Approach**:

> **Multi-source BFS from all 0s.** OR **Two-pass DP**.

Note: LC 542 inverts the convention — distances are to nearest 0, not 1. Just flip the source check.

**Code**:

```java
public int[][] updateMatrix(int[][] mat) {
    int n = mat.length, m = mat[0].length;
    int[][] dist = new int[n][m];
    Queue<int[]> queue = new LinkedList<>();
    
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < m; j++) {
            if (mat[i][j] == 0) {
                queue.offer(new int[]{i, j});
                // dist already 0 from default
            } else {
                dist[i][j] = Integer.MAX_VALUE;
            }
        }
    }
    
    int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
    while (!queue.isEmpty()) {
        int[] curr = queue.poll();
        for (int[] dir : dirs) {
            int nr = curr[0] + dir[0], nc = curr[1] + dir[1];
            if (nr < 0 || nr >= n || nc < 0 || nc >= m) continue;
            if (dist[nr][nc] > dist[curr[0]][curr[1]] + 1) {
                dist[nr][nc] = dist[curr[0]][curr[1]] + 1;
                queue.offer(new int[]{nr, nc});
            }
        }
    }
    return dist;
}
```

### Problem 2: LC 994 — Rotting Oranges

**Statement**: Grid with values 0 (empty), 1 (fresh orange), 2 (rotten orange). Each minute, rotten oranges spread to adjacent fresh oranges. Find time until all are rotten (or -1 if impossible).

**Approach**:

> **Multi-source BFS from all initially rotten oranges (value 2).**

**Key Insight**:
- Sources = cells with value 2 initially.
- BFS spreads "rotten state" outward.
- Answer = max distance reached (the last minute when something rots).
- If any fresh orange remains unvisited → return -1.

**Code Sketch**:
```java
public int orangesRotting(int[][] grid) {
    Queue<int[]> queue = new LinkedList<>();
    int fresh = 0;
    
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < m; j++) {
            if (grid[i][j] == 2) queue.offer(new int[]{i, j, 0});
            else if (grid[i][j] == 1) fresh++;
        }
    }
    
    int time = 0;
    while (!queue.isEmpty()) {
        int[] curr = queue.poll();
        time = Math.max(time, curr[2]);
        for (int[] dir : dirs) {
            int nr = curr[0]+dir[0], nc = curr[1]+dir[1];
            if (inBounds && grid[nr][nc] == 1) {
                grid[nr][nc] = 2;  // mark rotten
                fresh--;
                queue.offer(new int[]{nr, nc, curr[2]+1});
            }
        }
    }
    return fresh > 0 ? -1 : time;
}
```

### Problem 3: LC 1162 — As Far From Land as Possible

**Statement**: Grid of 0s (water) and 1s (land). Find the water cell whose distance to nearest land is MAXIMUM. Return that max distance.

**Approach**:

> **Multi-source BFS from all land cells.** Then return the max distance among water cells.

**Twist**: After BFS, scan the grid for the maximum distance value.

**Code**:
```java
public int maxDistance(int[][] grid) {
    int n = grid.length;
    Queue<int[]> queue = new LinkedList<>();
    
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            if (grid[i][j] == 1) queue.offer(new int[]{i, j});
        }
    }
    
    if (queue.isEmpty() || queue.size() == n * n) return -1;  // all water or all land
    
    int maxDist = 0;
    int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
    while (!queue.isEmpty()) {
        int size = queue.size();
        for (int i = 0; i < size; i++) {
            int[] curr = queue.poll();
            for (int[] dir : dirs) {
                int nr = curr[0]+dir[0], nc = curr[1]+dir[1];
                if (nr >= 0 && nr < n && nc >= 0 && nc < n && grid[nr][nc] == 0) {
                    grid[nr][nc] = 1;  // mark visited
                    queue.offer(new int[]{nr, nc});
                }
            }
        }
        maxDist++;
    }
    return maxDist - 1;
}
```

### Problem 4: LC 286 — Walls and Gates

**Statement**: Grid with -1 (wall), INF (empty room), 0 (gate). Fill each empty room with distance to nearest gate.

**Approach**:

> **Multi-source BFS from all gates (value 0).** Skip walls.

**Code Sketch**:
```java
public void wallsAndGates(int[][] rooms) {
    Queue<int[]> queue = new LinkedList<>();
    for each cell:
        if (cell == 0) queue.offer(cell);
    
    while (!queue.isEmpty()) {
        // standard BFS, skip walls (-1) and only update INF cells
    }
}
```

### Problem 5: LC 1765 — Map of Highest Peak

**Statement**: Grid with 0 (water) and 1 (land). Assign heights such that water cells have height 0 and adjacent cells differ by at most 1. Maximize the maximum height.

**Approach**:

> **Multi-source BFS from all water cells (value 0).** The height of each land cell = its BFS distance.

This guarantees adjacent cells differ by at most 1 (a BFS property).

### Problem 6: LC 815 — Bus Routes

**Statement**: Given bus routes, find min number of buses to get from source to target.

**Approach**:

> **BFS where each "level" is a bus.** Multiple bus stops can be reached in one bus → multi-source-like.

The pattern is "multi-source within a layer" — not exactly multi-source BFS but a related variant.

### Problem 7: LC 934 — Shortest Bridge

**Statement**: Grid with two islands. Find min number of 0s to flip to connect them.

**Approach**:

> **Step 1**: DFS to find one island. Mark all its cells.
> **Step 2**: Multi-source BFS from all cells of the first island. Find first cell of the second island. Distance = answer.

**Combination of DFS + Multi-source BFS**.

### Problem 8: Bombs Defusing Distance

**Statement**: Grid with bombs at certain cells. Distance to safety = nearest bomb-free cell.

**Approach**:

> Multi-source BFS from bomb-free cells, or vice versa.

### Problem 9: Knight Distance from Multiple Sources

**Statement**: Knight on a chessboard. Multiple starting positions. Find min knight moves to reach each cell.

**Approach**:

> Multi-source BFS with knight moves (8 L-shape moves) instead of 4 cardinal.

Same pattern, different `directions` array.

### Problem 10: COVID Spread Simulation

**Statement**: Some cells infected. Each step, infected cells infect neighbors (with some probability or always). Find time for full infection.

**Approach**:

> Multi-source BFS from initially infected cells.

### How to Recognize Multi-Source BFS Problems

**Look for these clues**:
1. "Distance to NEAREST X" (X = special cells).
2. "Time for SOMETHING to spread / reach all".
3. "Min steps from MULTIPLE starting points".
4. Grid problem with unit edge weights.
5. Multiple sources but one type of question per cell.

**Standard pattern**:
1. Initialize queue with ALL sources at distance 0.
2. BFS outward.
3. First visit gives shortest distance.

---

## 19. Complete Java Code

### Both Approaches

See `NearestWhiteCell.java`. The key parts:

#### Brute Force
```java
for each source:
    for each cell:
        update with Manhattan distance
```

#### Multi-Source BFS
```java
enqueue all sources at dist 0
while queue not empty:
    pop (r, c, d)
    for each 4 neighbors:
        if not visited:
            mark visited, dist = d+1, enqueue
```

### Production-Ready Version (LC 542 Style)

```java
import java.util.*;

public class Solution {
    public int[][] updateMatrix(int[][] mat) {
        int n = mat.length, m = mat[0].length;
        int[][] dist = new int[n][m];
        Queue<int[]> queue = new ArrayDeque<>();
        
        // Initialize
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (mat[i][j] == 0) {
                    queue.offer(new int[]{i, j});
                } else {
                    dist[i][j] = Integer.MAX_VALUE;  // sentinel "unvisited"
                }
            }
        }
        
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            for (int[] d : dirs) {
                int nr = curr[0] + d[0], nc = curr[1] + d[1];
                if (nr < 0 || nr >= n || nc < 0 || nc >= m) continue;
                if (dist[nr][nc] <= dist[curr[0]][curr[1]] + 1) continue;
                
                dist[nr][nc] = dist[curr[0]][curr[1]] + 1;
                queue.offer(new int[]{nr, nc});
            }
        }
        
        return dist;
    }
}
```

### Two-Pass DP Version (More Space-Efficient)

```java
public int[][] updateMatrixDP(int[][] mat) {
    int n = mat.length, m = mat[0].length;
    int INF = n + m;
    int[][] dist = new int[n][m];
    
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < m; j++) {
            dist[i][j] = (mat[i][j] == 0) ? 0 : INF;
        }
    }
    
    // Pass 1: top-left
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < m; j++) {
            if (i > 0) dist[i][j] = Math.min(dist[i][j], dist[i-1][j] + 1);
            if (j > 0) dist[i][j] = Math.min(dist[i][j], dist[i][j-1] + 1);
        }
    }
    
    // Pass 2: bottom-right
    for (int i = n-1; i >= 0; i--) {
        for (int j = m-1; j >= 0; j--) {
            if (i < n-1) dist[i][j] = Math.min(dist[i][j], dist[i+1][j] + 1);
            if (j < m-1) dist[i][j] = Math.min(dist[i][j], dist[i][j+1] + 1);
        }
    }
    
    return dist;
}
```

---

## 20. Interview Tips

### How to Approach in Interview

1. **Restate the problem**: "For each cell, find the distance to the nearest source."
2. **Acknowledge brute force**: "Brute force is O((n×m)²) — iterate every source and update every cell."
3. **Identify the inefficiency**: "Most work is wasted — we don't need to update every cell from every source."
4. **Propose multi-source BFS**: "Add all sources to queue at start. BFS naturally finds the nearest source for each cell in O(n×m)."
5. **Code carefully**.
6. **Mention alternatives**: two-pass DP, Dijkstra (overkill).

### Discussion Points to Score Bonus

#### 1. Why Multi-Source BFS Works
> "When all sources are at distance 0 in the queue, BFS expands them simultaneously. The FIRST source to reach a cell is the nearest — and BFS explores in order of distance, so the first visit is shortest."

#### 2. BFS = Manhattan Distance
> "In a 4-connected grid with unit weights, BFS distance equals Manhattan distance. This is the perfect tool for this problem."

#### 3. The Two-Pass DP Alternative
> "We can also solve this with two passes. First, top-left to bottom-right considering left/up neighbors. Then, bottom-right to top-left considering right/down. Each cell ends up with the minimum of all four directions."

#### 4. Why Not DFS?
> "DFS doesn't give shortest distance in unweighted graphs. It might find a longer path first. BFS is essential."

#### 5. Why Not Dijkstra?
> "Dijkstra is for weighted graphs. For unit weights, BFS is equivalent and faster (no log factor)."

### Likely Follow-Up Questions

#### Q: What if edges have different weights?
**A**: Use Dijkstra with a priority queue, starting from all sources at distance 0. The pattern (multi-source) still applies.

#### Q: What if there are obstacles?
**A**: Treat obstacles as cells to skip during BFS. Just add a check before enqueuing neighbors.

#### Q: What if we want the actual nearest source, not just distance?
**A**: Track the source ID along with the distance in the queue. When you reach a cell, store both the distance and which source brought you there.

#### Q: What if it's a 3D grid?
**A**: Same algorithm, 6 directions (up/down/left/right/forward/backward) instead of 4. Still O(n × m × k).

#### Q: Can you do this with O(1) extra space?
**A**: Yes — the two-pass DP modifies the grid in-place (besides the output).

#### Q: How would you handle very large grids?
**A**: 
- Both BFS and DP are O(n × m). For 10^7 cells, they take a few seconds.
- For huge grids, consider parallelization or coarse approximations.

### Common Interview Mistakes

1. Doing BFS from each source separately (= brute force).
2. Using DFS instead of BFS.
3. Forgetting bounds check.
4. Forgetting to mark sources as visited.
5. Using Dijkstra (overkill for unit weights).
6. Wrong direction array (e.g., using 8-direction when problem requires 4).

---

## TL;DR

### The Mental Model

```
Multi-Source BFS = "Multiple BFS waves starting simultaneously."

Add ALL sources to queue at distance 0.
BFS outward. First visit to each cell = distance to nearest source.

Time: O(n × m). Space: O(n × m).
```

### The Algorithm in 30 Seconds

```
1. Enqueue all sources (cells with value 1) at distance 0.
2. While queue not empty:
   - Pop (r, c, d).
   - For each 4-direction neighbor (nr, nc):
       If valid AND not visited:
         Mark visited.
         dist[nr][nc] = d + 1.
         Enqueue.
```

### The Five Key Insights

1. **Multi-source BFS is a single BFS** with multiple starting points.
2. **First visit wins** — BFS guarantees shortest distance.
3. **All sources at distance 0** — they start simultaneously.
4. **4-directional BFS gives Manhattan distance** in a unit-weight grid.
5. **Pattern matches**: "distance to nearest X" → multi-source BFS.

### C++ to Java Cheatsheet

| C++ | Java |
|-----|------|
| `int a[n][m]` | `int[][] a = new int[n][m]` |
| `INT_MAX` | `Integer.MAX_VALUE` |
| `pair<int, int>` | `int[]` of length 2 |
| `queue<...>` | `Queue<int[]>` or `Deque<int[]>` |
| `q.push(...)` | `queue.offer(...)` |
| `q.front(); q.pop()` | `queue.poll()` |
| `abs(x)` | `Math.abs(x)` |
| `min(a, b)` | `Math.min(a, b)` |

### Final Code Snippet to Memorize

```java
public int[][] multiSourceBFS(int[][] grid) {
    int n = grid.length, m = grid[0].length;
    int[][] dist = new int[n][m];
    Queue<int[]> queue = new ArrayDeque<>();
    
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < m; j++) {
            if (grid[i][j] == 1) {  // source
                queue.offer(new int[]{i, j});
            } else {
                dist[i][j] = Integer.MAX_VALUE;
            }
        }
    }
    
    int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
    while (!queue.isEmpty()) {
        int[] curr = queue.poll();
        for (int[] d : dirs) {
            int nr = curr[0] + d[0], nc = curr[1] + d[1];
            if (nr < 0 || nr >= n || nc < 0 || nc >= m) continue;
            if (dist[nr][nc] <= dist[curr[0]][curr[1]] + 1) continue;
            
            dist[nr][nc] = dist[curr[0]][curr[1]] + 1;
            queue.offer(new int[]{nr, nc});
        }
    }
    
    return dist;
}
```

### When This Problem Appears

| Tier | Frequency | Example |
|------|-----------|---------|
| Tier 1 | Sometimes | Basic version |
| Tier 2 | **Very often** (LC 542, 994) | Paytm, Flipkart, Adobe |
| Tier 3 | Very often | Google, Amazon, Meta |
| Tier 4 | Variations | Top quant |

Multi-source BFS is one of the **most-asked patterns** in interviews. Master it.

---

*Master multi-source BFS and you've unlocked one of the most important grid algorithms. The pattern (enqueue all sources at distance 0, then BFS outward) recurs across 10+ LeetCode problems and many real-world scenarios. The brute-force-to-BFS transformation is also a classic interview moment — recognizing the inefficiency and proposing the elegant fix demonstrates strong algorithmic thinking.*
