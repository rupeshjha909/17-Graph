# Bipartite Graph Detection — Complete Deep Dive

A line-by-line, in-depth explanation of detecting bipartite graphs using DFS with 2-coloring. Covers the theory, the algorithm, every code design choice, edge cases, and the Java conversion from the original C++ code.

---

## Table of Contents

1. [Problem Statement](#1-problem-statement)
2. [What Is a Bipartite Graph?](#2-what-is-a-bipartite-graph)
3. [The Beautiful Theorem: Bipartite iff No Odd Cycle](#3-the-beautiful-theorem-bipartite-iff-no-odd-cycle)
4. [The Big Idea: 2-Coloring](#4-the-big-idea-2-coloring)
5. [Why "3 - col" Is Genius](#5-why-3---col-is-genius)
6. [Why "child != par" Matters](#6-why-child--par-matters)
7. [Walking Through the Code Section by Section](#7-walking-through-the-code-section-by-section)
8. [C++ to Java Conversion Notes](#8-c-to-java-conversion-notes)
9. [Visual Examples](#9-visual-examples)
10. [Dry Run with Step-by-Step Diagrams](#10-dry-run-with-step-by-step-diagrams)
11. [Edge Cases](#11-edge-cases)
12. [The Disconnected Graph Issue](#12-the-disconnected-graph-issue)
13. [Common Mistakes](#13-common-mistakes)
14. [Alternative: BFS Approach](#14-alternative-bfs-approach)
15. [Complexity Analysis](#15-complexity-analysis)
16. [Real-World Applications](#16-real-world-applications)
17. [Variations and Follow-ups](#17-variations-and-follow-ups)
18. [Complete Java Code](#18-complete-java-code)
19. [Interview Tips](#19-interview-tips)

---

## 1. Problem Statement

> Given an undirected graph with `n` vertices and `m` edges, determine whether the graph is **bipartite**.
>
> A graph is bipartite if its vertices can be divided into two disjoint sets such that **every edge connects vertices from different sets** (no edge connects two vertices in the same set).
>
> Return `true` (or print "bipartite") if the graph is bipartite, `false` ("not bipartite") otherwise.

### Examples

#### Example 1: Bipartite (4-cycle)
```
    1 ─── 2
    │     │
    │     │
    4 ─── 3

We can split vertices: Set A = {1, 3}, Set B = {2, 4}.
Edges: 1-2 (A→B), 2-3 (B→A), 3-4 (A→B), 4-1 (B→A). 
All edges go between sets. ✓ BIPARTITE
```

#### Example 2: NOT Bipartite (Triangle)
```
       1
      ╱ ╲
     ╱   ╲
    2─────3

If we try: 1=A, then 2=B, then 3=A (because 2-3 edge).
But then 1-3 edge is A-A. CONFLICT!

No matter how we try, we can't 2-color this. NOT BIPARTITE.
```

#### Example 3: Bipartite (Path)
```
   1 ─── 2 ─── 3 ─── 4

Split: Set A = {1, 3}, Set B = {2, 4}.
All edges go between sets. ✓ BIPARTITE
```

---

## 2. What Is a Bipartite Graph?

### Formal Definition

A graph G = (V, E) is **bipartite** if:
- V can be partitioned into two disjoint sets U and W.
- Every edge in E has one endpoint in U and the other in W.
- No edge has both endpoints in the same set.

### Visual Representation

```
Bipartite layout (the "U-W partition"):

   U side          W side
   ─────          ─────
    A ─────────── X
    B ─────────── Y
    C ─────────── Z
    
All edges go LEFT-to-RIGHT (between sets).
No edge connects A-B or X-Y (within same set).
```

### Equivalent Definitions

A graph is bipartite if and only if:
1. It can be 2-colored such that no edge connects same-colored vertices.
2. It contains **no odd-length cycles**.
3. It's the underlying graph of a "matching" structure.

These three definitions are mathematically equivalent. The key one for our algorithm is **definition 2** (no odd cycles).

### Examples in Real Life

- **Job assignment**: workers on one side, jobs on the other; edges = "can do this job".
- **Movie cast**: actors on one side, movies on the other; edges = "acted in".
- **Stable marriage problem**: men and women.
- **Bipartite matching** in scheduling.

---

## 3. The Beautiful Theorem: Bipartite iff No Odd Cycle

### The Theorem

> **A graph is bipartite if and only if it contains no odd-length cycle.**

### Why This Is True

#### Forward direction: Bipartite → No odd cycles

Suppose G is bipartite with sets U and W. Take any cycle: v0, v1, v2, ..., vk, v0.

Since each edge goes between U and W, the vertices alternate:
```
v0 (U) → v1 (W) → v2 (U) → v3 (W) → ...
```

For the cycle to come back to v0 (which is in U), we need an even number of edges. The cycle length must be **even**.

#### Backward direction: No odd cycles → Bipartite

Suppose G has no odd cycles. We can 2-color it:
- Pick any vertex, color it RED.
- BFS/DFS outward; each neighbor gets the OPPOSITE color.
- For this to be consistent (no conflict), all paths between any two vertices must have the same parity.
- If two paths have different parities, combining them creates an odd cycle. Since we have none → coloring is consistent.

### Why Odd Cycles Break Bipartite-ness

Consider a triangle: 1 - 2 - 3 - 1.

```
Start at 1, color A.
Go to 2 (neighbor), must be color B.
Go to 3 (neighbor of 2), must be color A.
Now back to 1: edge 3-1 means 3 and 1 must be DIFFERENT colors.
But 3 = A and 1 = A. CONFLICT.

No matter where we start, we get a conflict. NOT BIPARTITE.
```

Generalize: any odd cycle creates this conflict. Any even cycle works fine.

### Reduction to Algorithm

> **Algorithm**: Try to 2-color the graph. If you encounter a conflict (same color on both ends of an edge), there must be an odd cycle → NOT bipartite.

This is exactly what the given code does.

---

## 4. The Big Idea: 2-Coloring

### The Strategy

```
1. Start at any vertex. Assign it color 1.
2. For each neighbor, assign the OPPOSITE color (color 2).
3. For each neighbor of neighbor, color 1 again. And so on.
4. If you ever try to color a vertex with a color OPPOSITE to what it already has,
   that's a CONFLICT → odd cycle → NOT bipartite.
5. If you successfully color every vertex without conflict → BIPARTITE.
```

### The Coloring Schema

We use color values 1 and 2 (not 0 and 1):
- **0** = uncolored (unvisited).
- **1** = first color (Set A).
- **2** = second color (Set B).

Why 1 and 2 instead of 0 and 1? Because we use the trick `3 - col` to flip colors, and using 0 would conflict with "uncolored".

### Walk-Through Example

```
Graph (path):  1 — 2 — 3 — 4

Step 1: Visit 1, color = 1.
Step 2: Visit 2 (neighbor of 1), color = 2.
Step 3: Visit 3 (neighbor of 2), color = 1.
Step 4: Visit 4 (neighbor of 3), color = 2.

Final coloring:
   1 (color 1) — 2 (color 2) — 3 (color 1) — 4 (color 2)

Sets: A = {1, 3}, B = {2, 4}.
Every edge connects A to B. BIPARTITE ✓
```

```
Graph (triangle): 1 — 2 — 3 — 1

Step 1: Visit 1, color = 1.
Step 2: Visit 2, color = 2.
Step 3: Visit 3 (neighbor of 2), color = 1.
Step 4: Check neighbors of 3: includes 1.
        1 has color 1, current node 3 has color 1.
        SAME COLOR → CONFLICT → odd cycle. NOT BIPARTITE ✗
```

---

## 5. Why "3 - col" Is Genius

### The Color Flip Trick

```cpp
dfs(child, cur, 3 - col);
```

This is the classic competitive-programming trick to alternate between two values.

### How It Works

If `col = 1`, then `3 - col = 3 - 1 = 2`.
If `col = 2`, then `3 - col = 3 - 2 = 1`.

It flips between 1 and 2 perfectly.

### Why Not Just `if/else`?

```java
// Alternative without the trick:
if (col == 1) {
    dfs(child, cur, 2);
} else {
    dfs(child, cur, 1);
}
```

This works but is more verbose.

### Variants of the Trick

```
3 - col:    flips 1 ↔ 2
1 - col:    flips 0 ↔ 1
col ^ 1:    flips 0 ↔ 1 (using XOR)
-col:       flips 1 ↔ -1
```

In this problem, we use 1 and 2 because:
- 0 is reserved for "uncolored".
- 3 - col elegantly toggles between 1 and 2.

### Java Equivalent

In Java, the same trick works:
```java
dfs(child, cur, 3 - col);
```

No language difference here.

---

## 6. Why "child != par" Matters

### The Edge Case

In an undirected graph, edge `(u, v)` is stored in both `adj[u]` and `adj[v]`.

When DFS visits `v` from `u`:
- `v` is marked visited.
- We iterate `v`'s neighbors.
- We see `u` in `v`'s adjacency list.
- `u` is already visited.

**Is this a back edge indicating a cycle?**

NO! It's just the edge we used to get to `v`. We're not in a cycle; we're just looking back along the same edge.

### The Fix: Parent Tracking

```cpp
else if (child != par && col == vis[child]) {
    odd_cycle = 1;
}
```

The condition `child != par` excludes the edge we came from. We only report a conflict if:
1. The child is visited.
2. The child is NOT our parent.
3. The child has the SAME color as the current node.

### The Two Cases Without Parent Check

Without `child != par`:
```
DFS(1, parent=0, color=1):
  vis[1] = 1
  Neighbor 2: unvisited. DFS(2, parent=1, color=2).
    vis[2] = 2
    Neighbor 1: visited.
      Check: col(2) = 2, vis(1) = 1. Different colors. OK.
      But wait — without the parent check, we'd still examine this edge.
      Since colors differ, no false positive HERE.
    
But consider:
DFS(1, parent=0, color=1):
  vis[1] = 1
  Neighbor 2: unvisited. DFS(2, parent=1, color=2).
    vis[2] = 2
    Neighbor 1: visited, same parent. Skip.

Actually, wait. col(2)=2 and vis(1)=1. They're DIFFERENT.
So `col == vis[child]` is false anyway. No false positive.
```

So actually for **bipartite check specifically**, the parent check is **a nice-to-have but not strictly necessary** when the algorithm correctly uses opposite colors. Let me re-examine why it's there.

### The Real Reason: Robustness

Even though the parent edge wouldn't trigger a false cycle (parent has opposite color), the check `child != par` makes the algorithm:
1. **More efficient**: don't even check the parent edge.
2. **Safer for variants**: if you adapt the algorithm to detect any cycle (not just odd), you'd need this check.
3. **Code clarity**: shows the intent that we're checking back edges.

This is a defensive programming pattern.

### Subtle Note

In standard undirected graph DFS (e.g., for cycle detection), `child != par` is **mandatory** to avoid false positives. Here in bipartite check, the color alternation does most of the work, but the `child != par` check is a good habit.

---

## 7. Walking Through the Code Section by Section

Let me walk through every part of the (now-converted) Java code.

### Section A: Global Variables

```java
private static List<List<Integer>> gr;  // adjacency list
private static int[] vis;                // colors (0=uncolored, 1, 2)
private static boolean oddCycle = false; // flag for odd cycle
```

These are **class-level (static) globals** — paralleling the C++ original which uses file-scope arrays.

**Why static globals?**
- In competitive programming style, globals avoid passing parameters through recursive calls.
- Faster to write, but less clean for production code.

**Production refactor**: encapsulate in a class with instance variables, or pass them as parameters.

### Section B: The DFS Function

```java
private static void dfs(int cur, int par, int col) {
    vis[cur] = col;
    
    for (int child : gr.get(cur)) {
        if (vis[child] == 0) {
            dfs(child, cur, 3 - col);
        } else if (child != par && col == vis[child]) {
            oddCycle = true;
        }
    }
}
```

Let me dissect each line.

#### Line 1: `vis[cur] = col;`
Color the current node with the assigned color. This must happen **before** recursing.

#### Line 3: `for (int child : gr.get(cur))`
Iterate all neighbors of the current node.

#### Line 4: `if (vis[child] == 0)`
The child hasn't been visited yet (color 0 = uncolored). Recurse.

#### Line 5: `dfs(child, cur, 3 - col);`
Recursive call:
- `child` becomes the new current node.
- `cur` becomes its parent.
- `3 - col` flips the color (1 ↔ 2).

#### Line 6: `else if (child != par && col == vis[child])`
The child IS visited. Two conditions must both be true for an odd cycle:
- `child != par`: it's NOT the edge we came from.
- `col == vis[child]`: same color → conflict → odd cycle.

#### Line 7: `oddCycle = true;`
Mark the flag. **Note**: we don't return early. We continue checking. This is fine because once `oddCycle = true`, no further state matters.

**Optimization**: in production code, you'd return early after setting the flag.

### Section C: The Solve Function

```java
public static void solve(Scanner scanner) {
    int n = scanner.nextInt();
    int m = scanner.nextInt();
    
    gr = new ArrayList<>();
    for (int i = 0; i <= n; i++) gr.add(new ArrayList<>());
    
    vis = new int[n + 1];
    oddCycle = false;
    
    for (int i = 0; i < m; i++) {
        int x = scanner.nextInt();
        int y = scanner.nextInt();
        gr.get(x).add(y);
        gr.get(y).add(x);
    }
    
    dfs(1, 0, 1);
    
    System.out.println(oddCycle ? "not bipartite" : "bipartite");
}
```

#### Reading n, m
Standard input format: vertex count and edge count.

#### Initializing the adjacency list
`gr` is created with `n + 1` slots because the graph uses 1-indexed nodes (vertex 1 to n). Index 0 is unused.

#### Initializing `vis`
Size `n + 1` for the same 1-indexing reason. Default values are 0 (uncolored).

#### Reading edges
For each edge `(x, y)`:
- Add `y` to `x`'s neighbor list.
- Add `x` to `y`'s neighbor list.

This is the **undirected** graph adjacency list pattern.

#### Starting DFS
`dfs(1, 0, 1)`:
- Start from node 1.
- Parent = 0 (sentinel, no parent).
- Color = 1.

**⚠️ Important**: this only handles **connected** graphs. If the graph has disconnected components, we'd miss them. (Section 12 addresses this.)

#### Output
Standard ternary on the flag.

### Section D: Main Function

```java
public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    solve(scanner);
    scanner.close();
}
```

Java's main function. The C++ original used `ios_base::sync_with_stdio(false)` and `cin.tie(NULL)` for fast I/O. The Java equivalent for very fast I/O is `BufferedReader`, but `Scanner` is fine for most cases.

For competitive Java, replace `Scanner` with `BufferedReader` + `StreamTokenizer` for huge inputs.

---

## 8. C++ to Java Conversion Notes

### Differences Between the Original and Java Version

#### 1. Adjacency List Declaration

**C++**:
```cpp
std::vector<int> gr[N];  // fixed-size array of vectors
```

**Java**:
```java
private static List<List<Integer>> gr;
gr = new ArrayList<>();
for (int i = 0; i <= n; i++) gr.add(new ArrayList<>());
```

Java lacks fixed-size arrays of generics, so we use ArrayList of ArrayLists.

#### 2. Visited Array

**C++**:
```cpp
int vis[N];
```

**Java**:
```java
private static int[] vis;
vis = new int[n + 1];
```

C++ allows global arrays of fixed size. Java requires dynamic allocation.

#### 3. Push Back

**C++**:
```cpp
gr[x].pb(y);  // pb = push_back
```

**Java**:
```java
gr.get(x).add(y);
```

#### 4. Boolean Flag

**C++**:
```cpp
bool odd_cycle = 0;
```

**Java**:
```java
boolean oddCycle = false;
```

Java's strict typing requires `false`, not `0`.

#### 5. Input

**C++**:
```cpp
cin >> n >> m;
```

**Java**:
```java
int n = scanner.nextInt();
int m = scanner.nextInt();
```

#### 6. Output

**C++**:
```cpp
cout << "not bipartite\n";
```

**Java**:
```java
System.out.println("not bipartite");
```

#### 7. Range-Based For Loop

**C++**:
```cpp
for (auto child : gr[cur])
```

**Java**:
```java
for (int child : gr.get(cur))
```

Both are foreach loops. Java's syntax is similar.

#### 8. Fast I/O

**C++**:
```cpp
ios_base::sync_with_stdio(false);
cin.tie(NULL);
```

**Java**:
```java
// Scanner is slower; for fast I/O use:
BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
StreamTokenizer in = new StreamTokenizer(br);
```

For typical interview problems, Scanner is fine. For competitive programming with huge inputs, switch to BufferedReader.

---

## 9. Visual Examples

### Example 1: Path (Bipartite)

```
   1 ──── 2 ──── 3 ──── 4 ──── 5

After 2-coloring:
   1 (1) ── 2 (2) ── 3 (1) ── 4 (2) ── 5 (1)

Set A = {1, 3, 5}, Set B = {2, 4}.
All edges between sets. ✓ BIPARTITE
```

### Example 2: Even Cycle (Bipartite)

```
   1 ──── 2
   │      │
   │      │
   4 ──── 3

After 2-coloring (start at 1):
   1 (1) ── 2 (2)
   │           │
   4 (2) ── 3 (1)

Edges:
   1-2: (1, 2) different ✓
   2-3: (2, 1) different ✓
   3-4: (1, 2) different ✓
   4-1: (2, 1) different ✓

✓ BIPARTITE (4-cycle is even)
```

### Example 3: Odd Cycle (NOT Bipartite)

```
       1
      ╱ ╲
     ╱   ╲
    2─────3

DFS from 1 (color 1):
   Visit 2, color 2.
   Visit 3 (from 2), color 1.
   
Back to 1's neighbors. Check 3.
   3 != par(0), color of 1 = 1, color of 3 = 1. SAME!
   odd_cycle = TRUE.

✗ NOT BIPARTITE
```

### Example 4: Bipartite K_{3,3}

```
   A ──── X
   │ ╲   ╱ │
   │  ╲ ╱  │
   B   ✕   Y
   │  ╱ ╲  │
   │ ╱   ╲ │
   C ──── Z

(Imagine each of A,B,C connects to each of X,Y,Z, but no A-A or X-X)

Set A = {A, B, C}, Set B = {X, Y, Z}.
All edges between sets. ✓ BIPARTITE
```

### Example 5: Tree (Always Bipartite)

```
            1
           ╱│╲
          ╱ │ ╲
         2  3  4
        ╱│  │
       5 6  7

Trees have no cycles → always bipartite.

Layer 0: {1} → color 1
Layer 1: {2, 3, 4} → color 2
Layer 2: {5, 6, 7} → color 1

Set A = {1, 5, 6, 7}, Set B = {2, 3, 4}. ✓
```

### Example 6: 5-Cycle (Not Bipartite)

```
       1
      ╱ ╲
     2   5
     │   │
     3 ─ 4

DFS from 1:
   1 → color 1
   2 → color 2
   3 → color 1
   4 → color 2 (neighbor of 3)
   Back to 4's other neighbor: 5
   5 → color 1 (neighbor of 4)
   Back to 5's other neighbor: 1
   1 is already colored 1. col(5) = 1 = col(1). CONFLICT!

5-cycle has odd length. NOT BIPARTITE.
```

---

## 10. Dry Run with Step-by-Step Diagrams

Let's trace through this 6-vertex graph:

```
Input:
n = 6, m = 7
Edges: (1,2), (2,3), (3,4), (4,5), (5,6), (6,1), (2,5)

Visualization:
     1 ────── 2
     │       ╱ │
     │     ╱   │
     6   ╱   5───3
     │ ╱      │
     │ 5      4
     └────────┘

Cleaner:
     1 ─── 2
     │     │
     6     3
     │     │
     5 ─── 4
     and edge 2-5

This has multiple cycles:
- 1→2→3→4→5→6→1 (length 6, even)
- 2→5→4→3→2 (length 4, even)
- 2→3→4→5→2 (length 4, even)
... etc.

Is it bipartite? Let's run the algorithm.
```

### Step 1: Initialization

```
gr[1] = [2, 6]
gr[2] = [1, 3, 5]
gr[3] = [2, 4]
gr[4] = [3, 5]
gr[5] = [4, 6, 2]
gr[6] = [5, 1]

vis = [_, 0, 0, 0, 0, 0, 0]  (index 0 unused)
oddCycle = false
```

### Step 2: dfs(1, 0, 1)

```
Mark vis[1] = 1.

State:
   1 (1) ── 2 (?)
   │         │
   6 (?)    3 (?)
   │         │
   5 (?) ── 4 (?)
   (2-5 edge)
```

Neighbors of 1: [2, 6]

#### Step 2a: dfs(2, 1, 2)

```
Mark vis[2] = 2.

State:
   1 (1) ── 2 (2)
   │         │
   6 (?)    3 (?)
   │         │
   5 (?) ── 4 (?)
```

Neighbors of 2: [1, 3, 5]
- 1: vis[1]=1, par=1. 1 == par → skip.
- 3: vis[3]=0 → recurse.

#### Step 2a-i: dfs(3, 2, 1)

```
Mark vis[3] = 1.

State:
   1 (1) ── 2 (2)
   │         │
   6 (?)    3 (1)
   │         │
   5 (?) ── 4 (?)
```

Neighbors of 3: [2, 4]
- 2: vis[2]=2, par=2. 2 == par → skip.
- 4: vis[4]=0 → recurse.

#### Step 2a-i-α: dfs(4, 3, 2)

```
Mark vis[4] = 2.

State:
   1 (1) ── 2 (2)
   │         │
   6 (?)    3 (1)
   │         │
   5 (?) ── 4 (2)
```

Neighbors of 4: [3, 5]
- 3: vis[3]=1, par=3. 3 == par → skip.
- 5: vis[5]=0 → recurse.

#### Step 2a-i-α-A: dfs(5, 4, 1)

```
Mark vis[5] = 1.

State:
   1 (1) ── 2 (2)
   │         │
   6 (?)    3 (1)
   │         │
   5 (1) ── 4 (2)
```

Neighbors of 5: [4, 6, 2]
- 4: vis[4]=2, par=4. 4 == par → skip.
- 6: vis[6]=0 → recurse.

#### Step 2a-i-α-A-x: dfs(6, 5, 2)

```
Mark vis[6] = 2.

State:
   1 (1) ── 2 (2)
   │         │
   6 (2)    3 (1)
   │         │
   5 (1) ── 4 (2)
```

Neighbors of 6: [5, 1]
- 5: vis[5]=1, par=5. 5 == par → skip.
- 1: vis[1]=1.
  - Check: 1 != par(5) ✓. col(6) = 2, vis(1) = 1. **DIFFERENT colors. NOT a conflict.**

dfs(6) returns. oddCycle still false.

#### Back to dfs(5):

Continue iterating neighbors of 5: [4, 6, **2**]
- 2: vis[2]=2.
  - Check: 2 != par(4) ✓. col(5) = 1, vis(2) = 2. **DIFFERENT colors. NOT a conflict.**

dfs(5) returns. oddCycle still false.

#### Back to dfs(4), dfs(3), dfs(2):

No more neighbors to process. All return.

#### Back to dfs(1):

Continue iterating: [2, **6**]
- 6: vis[6]=2.
  - Check: 6 != par(0) ✓. col(1) = 1, vis(6) = 2. **DIFFERENT. OK.**

dfs(1) returns.

### Final State

```
vis = [_, 1, 2, 1, 2, 1, 2]
oddCycle = false

Coloring:
   1 (1) ── 2 (2)
   │         │
   6 (2)    3 (1)
   │         │
   5 (1) ── 4 (2)
   (with 2-5 edge)

Set A = {1, 3, 5}
Set B = {2, 4, 6}

Every edge connects A to B. ✓ BIPARTITE
```

Output: "bipartite".

### Counter-example: With Odd Cycle

Add edge (1, 3):

```
gr[1] = [2, 6, 3]
gr[3] = [2, 4, 1]
```

When dfs(1) iterates neighbors and reaches 3:
- vis[3] = 1, par = 0.
- 3 != par ✓. col(1) = 1, vis(3) = 1. **SAME! odd_cycle = TRUE.**

Output: "not bipartite".

(Adding edge 1-3 creates an odd cycle 1→2→3→1 of length 3.)

---

## 11. Edge Cases

### 1. Empty Graph

```
n = 5, m = 0
No edges.

DFS from 1: vis[1] = 1. No neighbors. Return.
oddCycle = false.
Output: "bipartite"

⚠️ But: vertices 2, 3, 4, 5 are uncolored. 
The original code IGNORES them (only DFS from 1).
This is OK because they're isolated — trivially bipartite.

However, if asked about coloring, you'd need to color them too.
```

### 2. Single Vertex

```
n = 1, m = 0
DFS from 1: vis[1] = 1. No neighbors.
Output: "bipartite" ✓
```

### 3. Single Edge

```
n = 2, m = 1, Edge: (1, 2)
DFS: 1 (color 1) → 2 (color 2). Check 2's neighbors: 1 is parent. Skip.
Output: "bipartite" ✓
```

### 4. Self-Loop

```
n = 1, m = 1, Edge: (1, 1)
gr[1] = [1, 1]   (added twice for undirected)

DFS(1, par=0, col=1):
  vis[1] = 1
  Neighbor 1: vis[1] = 1. 1 != par(0) ✓. col(1) = 1, vis(1) = 1. SAME! oddCycle = TRUE.

Output: "not bipartite"

A self-loop is an odd cycle of length 1. NOT bipartite.
```

### 5. Multi-Edge

```
n = 2, m = 2, Edges: (1, 2), (1, 2)
gr[1] = [2, 2]
gr[2] = [1, 1]

DFS(1, par=0, col=1):
  vis[1] = 1
  Neighbor 2 (first): vis[2]=0. dfs(2, 1, 2). vis[2]=2. Skip parent 1. Return.
  Neighbor 2 (second): vis[2]=2. 2 != par(0) ✓. col(1)=1, vis(2)=2. DIFFERENT. OK.

Output: "bipartite" ✓

Multi-edges don't create odd cycles in this case.
But careful: 3 edges between same pair would still be bipartite (no odd cycle from this).
```

### 6. Disconnected Graph (THE BIG ISSUE)

```
n = 6, m = 4
Edges: (1, 2), (2, 3), (3, 1), (4, 5)

Component 1: triangle (1, 2, 3) — NOT bipartite!
Component 2: edge (4, 5) — bipartite
Vertex 6: isolated.

Original C++ code only calls dfs(1, 0, 1).
The triangle is found via vertex 1's DFS → detected.
Output: "not bipartite" ✓

BUT:
```

```
n = 6, m = 4
Edges: (1, 2), (3, 4), (4, 5), (5, 3)

Component 1: edge (1, 2) — bipartite
Component 2: triangle (3, 4, 5) — NOT bipartite!

Original C++ code only calls dfs(1, 0, 1).
DFS from 1 doesn't reach 3, 4, 5.
oddCycle stays false.
Output: "bipartite" ✗ WRONG!

This is a BUG in the original algorithm for disconnected graphs.
The fix: loop over all unvisited nodes (covered in Section 12).
```

### 7. Two Disconnected Cycles

```
n = 8, m = 8
Edges: (1,2),(2,3),(3,4),(4,1),(5,6),(6,7),(7,8),(8,5)

Two separate 4-cycles. Both bipartite.
DFS from 1 finds the first. The second is unreached.
Output: "bipartite" ✓ (by luck)

But if the second were a triangle:
Edges: (1,2),(2,3),(3,4),(4,1),(5,6),(6,7),(7,5)
DFS from 1 finds 4-cycle (bipartite).
The 5-6-7 triangle is missed.
Output: "bipartite" ✗ WRONG!
```

### 8. Complete Bipartite K_{m,n}

```
K_{3,3}: 3 vertices on each side, all 9 edges between sides.

This is bipartite by construction.
Output: "bipartite" ✓
```

---

## 12. The Disconnected Graph Issue

### The Bug in the Original Code

```cpp
dfs(1, 0, 1);  // only starts from vertex 1
```

If the graph is disconnected, vertices not reachable from 1 are never visited. If a non-bipartite component exists elsewhere, it's missed.

### The Fix

Loop over all unvisited vertices:

```java
for (int start = 1; start <= n; start++) {
    if (vis[start] == 0) {
        dfs(start, 0, 1);
    }
}
```

### Updated Solve Function

```java
public static void solve(Scanner scanner) {
    int n = scanner.nextInt();
    int m = scanner.nextInt();
    
    gr = new ArrayList<>();
    for (int i = 0; i <= n; i++) gr.add(new ArrayList<>());
    
    vis = new int[n + 1];
    oddCycle = false;
    
    for (int i = 0; i < m; i++) {
        int x = scanner.nextInt();
        int y = scanner.nextInt();
        gr.get(x).add(y);
        gr.get(y).add(x);
    }
    
    // FIX: iterate over all components
    for (int start = 1; start <= n; start++) {
        if (vis[start] == 0) {
            dfs(start, 0, 1);
        }
    }
    
    System.out.println(oddCycle ? "not bipartite" : "bipartite");
}
```

### Why the Original Worked for Some Cases

- If the test cases never had disconnected graphs, the bug is invisible.
- Many competitive problems guarantee connectivity to simplify input.

But for **general bipartite checking**, you MUST handle disconnected components.

### LeetCode Bipartite Problems
LC 785 (Is Graph Bipartite?) **explicitly states** the graph may be disconnected. The "iterate all" pattern is mandatory there.

---

## 13. Common Mistakes

### Mistake 1: Forgetting to Iterate Over All Vertices

```java
// WRONG: only DFS from vertex 1
dfs(1, 0, 1);

// RIGHT:
for (int i = 1; i <= n; i++) {
    if (vis[i] == 0) dfs(i, 0, 1);
}
```

### Mistake 2: Using 0 and 1 as Colors

```java
// WRONG (if you also use 0 for "uncolored"):
vis[cur] = 0 or 1;

// Then how do you distinguish "uncolored" from "color 0"?
```

Use values 1 and 2 for colors, 0 for uncolored. Or use a separate `boolean[] visited` array.

### Mistake 3: Not Marking Visited Before Recursing

```java
// WRONG:
private static void dfs(int cur, int par, int col) {
    for (int child : gr.get(cur)) {
        if (vis[child] == 0) {
            dfs(child, cur, 3 - col);
        }
    }
    vis[cur] = col;  // ← TOO LATE!
}
```

The child's recursion can come back to cur and see it as uncolored → loop forever or wrong color.

**Always mark visited BEFORE recursing.**

### Mistake 4: Skipping the Parent Check

```java
// WRONG: no parent check
if (vis[child] != 0 && col == vis[child]) {
    odd_cycle = true;
}
```

Without parent check, the algorithm might incorrectly examine the parent edge. While in this specific algorithm (with color alternation) it doesn't cause false positives, it's bad practice.

### Mistake 5: Wrong Color Initialization

```java
// WRONG:
dfs(1, 0, 0);  // starting with color 0

// Then vis[1] = 0, which is "uncolored". 
// Other DFS calls would re-process node 1!
```

Always start with color 1 or 2, not 0.

### Mistake 6: Not Resetting `oddCycle` Between Test Cases

If you run multiple test cases without resetting:
```java
oddCycle = false;  // ← MUST reset for each test case
```

### Mistake 7: Stack Overflow on Large Graphs

For graphs with V > ~10^5 and deep paths, recursive DFS can blow the stack.

Solutions:
- Increase JVM stack size: `java -Xss10m`.
- Use iterative DFS with explicit stack.
- Use BFS (Section 14).

### Mistake 8: Confusing Bipartite with "2-Colorable Vertices"

Bipartite ≠ "we can color with 2 colors" in general graph coloring sense.

Specifically: bipartite means we can 2-color such that **adjacent vertices have different colors**. This is the same as "no odd cycle" — but not the same as "chromatic number is at most 2" (which is the same thing in this case, but the terminology can be confusing).

---

## 14. Alternative: BFS Approach

DFS and BFS both work for bipartite check. Here's the BFS version.

### Java BFS Code

```java
import java.util.*;

public class BipartiteCheckBFS {
    public boolean isBipartite(int n, int[][] edges) {
        // Build adjacency list
        List<List<Integer>> graph = new ArrayList<>();
        for (int i = 0; i <= n; i++) graph.add(new ArrayList<>());
        for (int[] e : edges) {
            graph.get(e[0]).add(e[1]);
            graph.get(e[1]).add(e[0]);
        }
        
        int[] color = new int[n + 1];  // 0=uncolored, 1, 2
        
        for (int start = 1; start <= n; start++) {
            if (color[start] != 0) continue;
            
            // BFS from start
            Queue<Integer> queue = new LinkedList<>();
            queue.offer(start);
            color[start] = 1;
            
            while (!queue.isEmpty()) {
                int node = queue.poll();
                int currColor = color[node];
                
                for (int neighbor : graph.get(node)) {
                    if (color[neighbor] == 0) {
                        // Uncolored: assign opposite color
                        color[neighbor] = 3 - currColor;
                        queue.offer(neighbor);
                    } else if (color[neighbor] == currColor) {
                        // Same color: conflict!
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
```

### DFS vs BFS Comparison

| Aspect | DFS | BFS |
|--------|-----|-----|
| Code length | Slightly shorter (recursion) | Slightly longer (queue) |
| Stack safety | Risks stack overflow for huge graphs | Safe |
| Conceptual model | Recursive coloring | Level-by-level coloring |
| Naturally handles disconnected | Need outer loop | Need outer loop |

**Recommendation**:
- **Interview**: either works. DFS is more concise.
- **Production / huge graphs**: BFS for safety.

---

## 15. Complexity Analysis

### Time Complexity: O(V + E)

Each vertex is visited at most once. Each edge is examined exactly twice (once from each endpoint in an undirected graph).

Total operations: O(V + E).

### Space Complexity: O(V + E)

- Adjacency list: O(V + E).
- `vis[]` array: O(V).
- Recursion stack (DFS): O(V) worst case.
- Queue (BFS): O(V) worst case.

For 10^5 vertices and 10^5 edges: ~3 × 10^5 operations. Microseconds.

---

## 16. Real-World Applications

### 1. Job Assignment Problem
Workers on one side, jobs on the other. Edges = "qualifications". Bipartite matching algorithms (like Hopcroft-Karp) build on this.

### 2. Stable Marriage Problem
Men ↔ women, edges = preferences. Bipartite structure enables matching algorithms.

### 3. Network Flow
Many max-flow problems reduce to bipartite matching.

### 4. Scheduling
Tasks vs time slots. Tasks → time slots edges = "can be scheduled at this time".

### 5. Computer Science
- Compiler design: register allocation (graph coloring; bipartite is the easiest case).
- Database normalization.
- Resource conflict detection.

### 6. Social Networks
"User-Item" bipartite graphs (Netflix users ↔ movies, Amazon users ↔ products) for recommendations.

### 7. Biology
Protein-protein interaction networks (often bipartite structures).

---

## 17. Variations and Follow-ups

### Variation 1: Find the Two Sets

Modify to also output the two sets:

```java
public List<List<Integer>> getBipartition(int n, int[][] edges) {
    // ... (build graph, run BFS/DFS, check oddCycle)
    
    List<Integer> setA = new ArrayList<>();
    List<Integer> setB = new ArrayList<>();
    
    for (int i = 1; i <= n; i++) {
        if (vis[i] == 1) setA.add(i);
        else if (vis[i] == 2) setB.add(i);
    }
    
    return Arrays.asList(setA, setB);
}
```

### Variation 2: Possible Bipartition (LC 886)

> Given dislike relationships, can you split people into two groups so no two people in the same group dislike each other?

This is exactly bipartite check! Build a graph where edges = "dislikes" and check bipartite.

### Variation 3: K-Coloring (Generalization)

Bipartite = 2-coloring. The general "k-coloring" problem (can we color with k colors such that no two adjacent vertices share a color?) is NP-hard for k ≥ 3.

For trees and bipartite graphs: 2 colors suffice.

### Variation 4: Maximum Matching in Bipartite

Once we know a graph is bipartite, we can find the maximum matching (largest set of edges with no shared endpoints).

Algorithms:
- Hungarian algorithm.
- Hopcroft-Karp.
- Network flow approach.

### LeetCode Problems

| Problem | Difficulty | Link |
|---------|-----------|------|
| Is Graph Bipartite? | Medium | [LC 785](https://leetcode.com/problems/is-graph-bipartite/) |
| Possible Bipartition | Medium | [LC 886](https://leetcode.com/problems/possible-bipartition/) |
| Flower Planting With No Adjacent | Easy | [LC 1042](https://leetcode.com/problems/flower-planting-with-no-adjacent/) |
| Maximum Number of Achievable Transfer Requests | Hard | [LC 1601](https://leetcode.com/problems/maximum-number-of-achievable-transfer-requests/) |
| Minimum Hours of Training to Win | Easy | [LC 2383](https://leetcode.com/problems/minimum-hours-of-training-to-win-a-competition/) |

---

## 18. Complete Java Code

Here's the complete, robust Java version handling disconnected graphs:

```java
import java.util.*;
import java.io.*;

public class BipartiteCheck {
    
    private static List<List<Integer>> gr;
    private static int[] vis;
    private static boolean oddCycle;
    
    /**
     * DFS to color the graph with alternating colors.
     * If any back edge connects same-colored vertices, set oddCycle flag.
     */
    private static void dfs(int cur, int par, int col) {
        vis[cur] = col;
        
        for (int child : gr.get(cur)) {
            if (vis[child] == 0) {
                dfs(child, cur, 3 - col);
            } else if (child != par && col == vis[child]) {
                oddCycle = true;
            }
        }
    }
    
    public static void solve(Scanner scanner) {
        int n = scanner.nextInt();
        int m = scanner.nextInt();
        
        gr = new ArrayList<>();
        for (int i = 0; i <= n; i++) gr.add(new ArrayList<>());
        
        vis = new int[n + 1];
        oddCycle = false;
        
        for (int i = 0; i < m; i++) {
            int x = scanner.nextInt();
            int y = scanner.nextInt();
            gr.get(x).add(y);
            gr.get(y).add(x);
        }
        
        // Handle disconnected components
        for (int start = 1; start <= n; start++) {
            if (vis[start] == 0) {
                dfs(start, 0, 1);
            }
        }
        
        System.out.println(oddCycle ? "not bipartite" : "bipartite");
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        solve(scanner);
        scanner.close();
    }
}
```

### LeetCode-Style Version

For LC 785 (Is Graph Bipartite?):

```java
class Solution {
    public boolean isBipartite(int[][] graph) {
        int n = graph.length;
        int[] color = new int[n];  // 0 = uncolored
        
        for (int start = 0; start < n; start++) {
            if (color[start] == 0) {
                if (!dfs(start, 1, color, graph)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean dfs(int node, int col, int[] color, int[][] graph) {
        color[node] = col;
        
        for (int neighbor : graph[node]) {
            if (color[neighbor] == 0) {
                if (!dfs(neighbor, 3 - col, color, graph)) {
                    return false;
                }
            } else if (color[neighbor] == col) {
                return false;
            }
        }
        return true;
    }
}
```

Cleaner, return-based version using early termination.

---

## 19. Interview Tips

### How to Approach This Problem

1. **Restate the problem**: "Can the graph be 2-colored such that adjacent vertices have different colors?"
2. **State the theorem**: "A graph is bipartite iff it has no odd cycle."
3. **Propose the algorithm**: "I'll do DFS/BFS and assign alternating colors. If I encounter a vertex that should have an opposite color but already has the same color, that's an odd cycle."
4. **Handle disconnected components**: "I'll loop over all unvisited vertices."
5. **Code it carefully**.
6. **Test edge cases**: triangle, even cycle, disconnected.
7. **Discuss complexity**: O(V + E).

### Discussion Points to Score Bonus

#### 1. The Theorem
> "The key insight is that a graph is bipartite if and only if it has no odd-length cycle. Trying to 2-color the graph is equivalent to checking this."

#### 2. Why "3 - col"
> "I use colors 1 and 2 (reserving 0 for uncolored). The expression `3 - col` flips between 1 and 2 — it's a clean way to alternate without an if-else."

#### 3. Handle Disconnected Components
> "I need to loop over all vertices and start DFS from any unvisited one. Otherwise, I'd miss components not reachable from the starting vertex."

#### 4. Parent Check Subtlety
> "The `child != parent` check ensures we don't falsely treat the edge we came from as a back edge. Though in this algorithm, the color alternation handles it anyway, the explicit check is good defensive coding."

#### 5. Could Use BFS Too
> "BFS is equivalent here. I prefer DFS for conciseness, but BFS is safer for very large graphs to avoid stack overflow."

### Likely Follow-Up Questions

#### Q: What if the graph is directed?
**A**: Bipartite is typically defined for undirected. For directed, you can treat edges as bidirectional and apply the same check.

#### Q: Find the two sets, not just yes/no.
**A**: After running, group vertices by their color: `vis[i] == 1` → Set A, `vis[i] == 2` → Set B.

#### Q: What if you have weighted edges?
**A**: Weights don't affect bipartite-ness. The check is purely structural.

#### Q: How do you find an actual odd cycle if not bipartite?
**A**: Track parent during DFS. When you encounter same-colored neighbor (not parent), trace back from current node to that neighbor using parent pointers — that's the odd cycle.

#### Q: What's the complexity?
**A**: O(V + E) time, O(V + E) space.

#### Q: Could this graph be 3-colorable but not 2-colorable?
**A**: Yes. A triangle is 3-colorable (use 3 different colors) but not 2-colorable (not bipartite). 3-coloring (and higher) is NP-hard in general.

### Common Interview Mistakes

1. **Not handling disconnected graphs**.
2. **Using 0/1 as colors with 0 as "uncolored"** — confusing.
3. **Forgetting to reset the flag** between test cases.
4. **Mixing up bipartite with "any 2 colors work"** vs "adjacent must differ".
5. **Not knowing the odd-cycle theorem**.

---

## TL;DR

### The Mental Model

```
A graph is bipartite ⟺ it has no odd cycle ⟺ it can be 2-colored.

Algorithm:
  For each unvisited vertex:
    Try to 2-color the connected component using DFS/BFS.
    If a conflict arises (adjacent vertices same color), NOT bipartite.
```

### The Color Trick

Use colors 1 and 2. Use `3 - col` to flip between them.

### The Key Code (Memorize)

```java
private static void dfs(int cur, int par, int col) {
    vis[cur] = col;
    for (int child : gr.get(cur)) {
        if (vis[child] == 0) {
            dfs(child, cur, 3 - col);
        } else if (child != par && col == vis[child]) {
            oddCycle = true;
        }
    }
}
```

### The 5 Key Insights

1. **Bipartite ⟺ No odd cycle** — the foundational theorem.
2. **2-coloring** is the algorithmic technique.
3. **Use 1 and 2 for colors**, 0 for uncolored.
4. **3 - col** elegantly flips colors.
5. **Loop over all vertices** to handle disconnected components.

### C++ to Java Cheatsheet

| C++ | Java |
|-----|------|
| `vector<int> gr[N]` | `List<List<Integer>> gr` |
| `gr[x].pb(y)` | `gr.get(x).add(y)` |
| `int vis[N]` | `int[] vis` |
| `bool` | `boolean` |
| `cin >> n` | `scanner.nextInt()` |
| `cout << ...` | `System.out.println(...)` |
| `auto child : gr[cur]` | `int child : gr.get(cur)` |

### When This Problem Appears

Common at:
- Tier 1: TCS, Infosys (basic version).
- Tier 2: Paytm, Flipkart, Adobe (LC 785 / LC 886).
- Tier 3: Google, Amazon (with follow-ups on matching, k-coloring).
- Tier 4: Top quant (Tarjan's-related, advanced matching).

It's a **must-know pattern** for any graph-heavy interview prep.

---

*Master bipartite detection and you've also mastered the foundation of bipartite matching, network flow, and graph coloring. The "2-color and check for conflict" pattern recurs throughout graph algorithms.*
