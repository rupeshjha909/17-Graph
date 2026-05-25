# Prim's Minimum Spanning Tree (MST) — Complete Deep Dive

A line-by-line, in-depth explanation of Prim's algorithm for finding the Minimum Spanning Tree using a priority queue (min-heap). This guide covers the theory, the algorithm, every code design choice, and the Java conversion from the original C++ code.

---

## Table of Contents

1. [Problem Statement](#1-problem-statement)
2. [Quick MST Recap](#2-quick-mst-recap)
3. [The Big Idea Behind Prim's](#3-the-big-idea-behind-prims)
4. [Why a Priority Queue?](#4-why-a-priority-queue)
5. [The Concept of "Active Edges"](#5-the-concept-of-active-edges)
6. [The Algorithm — Step by Step](#6-the-algorithm--step-by-step)
7. [Walking Through the Code Section by Section](#7-walking-through-the-code-section-by-section)
8. [C++ to Java Conversion Notes](#8-c-to-java-conversion-notes)
9. [Visual Examples](#9-visual-examples)
10. [Detailed Dry Run with Diagrams](#10-detailed-dry-run-with-diagrams)
11. [Edge Cases](#11-edge-cases)
12. [Complexity Analysis](#12-complexity-analysis)
13. [Common Mistakes](#13-common-mistakes)
14. [Prim vs Kruskal — Detailed Comparison](#14-prim-vs-kruskal--detailed-comparison)
15. [Why It's Optimal — The Cut Property](#15-why-its-optimal--the-cut-property)
16. [Variants of Prim's](#16-variants-of-prims)
17. [Real-World Applications](#17-real-world-applications)
18. [Complete Java Code](#18-complete-java-code)
19. [Interview Tips](#19-interview-tips)

---

## 1. Problem Statement

> Given an **undirected, weighted, connected graph** with `n` vertices and `m` edges, find a **Minimum Spanning Tree (MST)**: a subset of edges that:
> - Connects all vertices.
> - Has no cycles.
> - Has the **minimum possible total edge weight**.

### Output

Total weight of the MST (or the list of selected edges).

### Example

```
Graph (4 vertices):

        1
    0───────1
    │      ╱│
   2│  3 ╱  │5
    │   ╱   │
    2───────3
        4

Edges: (0,1,1), (0,2,2), (1,2,3), (1,3,5), (2,3,4)

MST (taking the cheapest spanning subset):
  (0-1, 1), (0-2, 2), (2-3, 4)
  Total weight: 7
```

---

## 2. Quick MST Recap

### Spanning Tree Properties
- Includes ALL V vertices.
- Has EXACTLY V - 1 edges.
- Has NO cycles.
- Connected.

### Minimum Spanning Tree (MST)
- Among all possible spanning trees, the one with **smallest total edge weight**.
- May not be unique (when weights tie).

### Two Classic MST Algorithms
1. **Kruskal's**: edge-based, uses sorting + DSU.
2. **Prim's**: vertex-based, uses a priority queue.

This document covers **Prim's**.

---

## 3. The Big Idea Behind Prim's

### The Greedy Strategy

> "Start at any vertex. Repeatedly add the cheapest edge that extends the current MST to a new vertex."

The MST **grows one vertex at a time**, always choosing the cheapest "boundary" edge.

### Two Sets During Execution

At any point, we partition vertices into:
- **Inside the MST** (already added).
- **Outside the MST** (not yet added).

An **active edge** is an edge from "inside" to "outside" — a candidate for the next addition.

### The Algorithm in One Sentence

> "Pick the cheapest active edge. If it leads to a new vertex, add that vertex to the MST. Repeat until all vertices are added."

### Why This Differs From Kruskal's

| | Kruskal's | Prim's |
|-|-----------|--------|
| **Approach** | Sort all edges; pick non-cycle ones | Grow MST from a seed; pick cheapest boundary edge |
| **Data structure** | DSU (cycle detection) | Priority queue (min-weight boundary edge) |
| **Builds** | Forest that gradually merges | One growing tree |

Both produce a valid MST. They just take different paths to get there.

---

## 4. Why a Priority Queue?

### The Core Question

At each step, we need to find the **cheapest active edge**. How?

#### Naive approach: scan all active edges each step.
- Per step: O(E) to find minimum.
- For V steps: O(V × E) = too slow for large graphs.

#### Better: keep edges in a min-heap.
- Per step: O(log E) to find minimum.
- Total: O(E log E) ≈ O(E log V).

The **min-heap (priority queue) is the perfect data structure** for "find me the smallest among many things efficiently".

### How the PQ Works in Prim's

```
pq stores: (weight, node) pairs.

When we add a vertex to MST:
  → push all its (edge_weight, neighbor) for unvisited neighbors.

To pick next edge:
  → pop from PQ. Smallest weight comes out first.
  → If the destination node is already visited, this edge is stale. Discard.
  → Otherwise, it's our next MST edge.
```

### Why "stale" Edges?

The same vertex can be reached by multiple edges from different MST vertices. We push ALL of them into the PQ.

When we pop, the smallest one might point to an already-visited vertex (already added via a cheaper edge). We discard those.

This "lazy" approach is simpler than maintaining the PQ exactly, and still gives correct results.

---

## 5. The Concept of "Active Edges"

### Definition

An **active edge** is an edge `(u, v)` where:
- `u` is INSIDE the MST.
- `v` is OUTSIDE the MST.

Active edges are the "frontier" — candidates to grow the MST.

### Visual Example

```
After processing some edges, MST contains {0, 1, 2}:

      0────1
       \  /
        2     3  ← outside
       /|
      / |
     ... 

Active edges (from inside to outside):
  - (0, 3, w1) if such edge exists
  - (1, 3, w2) if such edge exists
  - (2, 3, w3) if such edge exists

Among these, Prim's picks the cheapest.
```

### Evolution Over Time

```
Initially: MST = {start vertex}. Active edges = all edges from start.
Step 1: Add cheapest edge's endpoint. MST = {start, v1}. Update active edges.
Step 2: Add next cheapest. MST = {start, v1, v2}. ...
...
Final: MST has all V vertices. No active edges remain (all "crossings" used or stale).
```

### The Lazy PQ Approach

Instead of carefully maintaining "only active edges", we **lazily push everything and filter on pop**:

```
On adding vertex v to MST:
  For each neighbor u of v that's not yet in MST:
    push (weight(v,u), u) to PQ.

On popping:
  If the destination is already in MST → stale. Discard and continue.
  Else → it's an active edge. Take it.
```

This is simpler and still O(E log V).

---

## 6. The Algorithm — Step by Step

### Pseudocode

```
1. Initialize visited[] = false for all vertices.
2. Initialize PQ with (0, start_vertex).  // weight 0 to reach start
3. total = 0.
4. While PQ is not empty:
     a. Pop (weight, node) from PQ.
     b. If visited[node]: continue (stale edge).
     c. Mark visited[node] = true.
     d. total += weight.
     e. For each neighbor of node:
          If not visited[neighbor]:
            Push (edge_weight, neighbor) to PQ.
5. Return total.
```

### Three Phases of Each Iteration

#### Phase 1: Pop from PQ
Get the cheapest candidate edge.

#### Phase 2: Validate
Check if the destination is still unvisited.

#### Phase 3: Add or Discard
- If valid → add to MST, update total, add new candidates.
- If stale → discard, try next.

### Starting Vertex

Prim's can start from **any** vertex. The choice doesn't affect the MST's total weight (the MST is the same set of edges regardless of start point, when MST is unique).

Convention: start at vertex 0.

### Why Initial Push is `(0, 0)`?

We push (weight=0, node=0). The 0 weight is artificial — there's no "edge to vertex 0 from outside". This is just to bootstrap the algorithm so vertex 0 is the first to be "popped and added".

When we add vertex 0 with weight 0, total += 0, no effect. Then we add its real edges to the PQ.

---

## 7. Walking Through the Code Section by Section

Let me walk through every part of the Java code.

### Section A: The Graph Class

```java
static class Graph {
    List<List<int[]>> adj;
    int v;
    
    public Graph(int n) {
        this.v = n;
        this.adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
    }
    
    public void addEdge(int x, int y, int wt) {
        adj.get(x).add(new int[]{y, wt});
        adj.get(y).add(new int[]{x, wt});
    }
}
```

#### Why Adjacency List?

Prim's needs to query: "from vertex `v`, what are the edges going out?"

Adjacency list answers this in O(1) (just iterate `adj.get(v)`). Edge list would require scanning all edges.

#### Storage: `int[]{neighbor, weight}`

Each entry in `adj.get(x)` is `int[2]` representing one edge from `x`:
- `[0]` = the neighbor.
- `[1]` = the weight.

Alternative: a `Pair<Integer, Integer>` class. But `int[]` is more memory-efficient.

#### Why Both Directions?

```java
adj.get(x).add(new int[]{y, wt});
adj.get(y).add(new int[]{x, wt});
```

For an undirected edge, we add it to BOTH endpoints' adjacency lists. This way, when processing either endpoint, we see the edge.

### Section B: The primMST Function

```java
public int primMST() {
    PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0]));
    boolean[] visited = new boolean[v];
    int totalWeight = 0;
    
    pq.offer(new int[]{0, 0});  // {weight, node}
    
    while (!pq.isEmpty()) {
        int[] best = pq.poll();
        int weight = best[0];
        int node = best[1];
        
        if (visited[node]) continue;
        
        visited[node] = true;
        totalWeight += weight;
        
        for (int[] edge : adj.get(node)) {
            int neighbor = edge[0];
            int edgeWeight = edge[1];
            if (!visited[neighbor]) {
                pq.offer(new int[]{edgeWeight, neighbor});
            }
        }
    }
    
    return totalWeight;
}
```

#### Line-by-Line

**`PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0]));`**

Create a min-heap. The comparator says: compare by `a[0]` vs `b[0]` (the weight).

**`boolean[] visited = new boolean[v];`**

Track which vertices are in the MST. All start as false.

**`int totalWeight = 0;`**

Accumulator for MST weight.

**`pq.offer(new int[]{0, 0});`**

Start with vertex 0, weight 0. This bootstraps the algorithm.

**`while (!pq.isEmpty())`**

Process until queue is empty (all reachable vertices added).

**`int[] best = pq.poll();`**

Pop the smallest-weight entry.

**`if (visited[node]) continue;`**

If already in MST, this is a stale edge. Skip.

**`visited[node] = true; totalWeight += weight;`**

Add this vertex. Account for the edge weight that brought us here.

**`for (int[] edge : adj.get(node))`**

Iterate all edges from this vertex.

**`if (!visited[neighbor]) pq.offer(...)`**

Only push edges going to unvisited vertices (optimization to keep PQ smaller).

#### Why `{weight, node}` Order in the Array?

Java's PriorityQueue with a comparator can compare any way we want. We chose `(a, b) -> Integer.compare(a[0], b[0])`, which means index 0 of the array is what determines priority.

We put **weight at index 0** so the heap orders by weight (smallest weight = highest priority).

Alternative ordering `{node, weight}` would require the comparator to use `a[1]` — same effect, different convention.

#### The Initial Push: `{0, 0}` 

This pushes weight=0, node=0. When popped:
- `weight = 0`, `node = 0`.
- Not visited, mark visited, total += 0 (no effect).
- Push all 0's edges to PQ.

This is a clean way to bootstrap without special-casing the start.

### Section C: The Main Function

```java
public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    int n = scanner.nextInt();
    int m = scanner.nextInt();
    
    Graph g = new Graph(n);
    for (int i = 0; i < m; i++) {
        int x = scanner.nextInt();
        int y = scanner.nextInt();
        int w = scanner.nextInt();
        g.addEdge(x - 1, y - 1, w);
    }
    
    System.out.println(g.primMST());
    scanner.close();
}
```

#### Input Format

```
n m
x1 y1 w1
x2 y2 w2
...
xm ym wm
```

#### 1-Indexed → 0-Indexed

`g.addEdge(x - 1, y - 1, w);` converts 1-indexed input to 0-indexed arrays.

---

## 8. C++ to Java Conversion Notes

### Differences and Translations

#### 1. Array of Vectors → List of Lists

**C++**:
```cpp
vector<pair<int, int>> *l;  // array of vectors of pairs
l = new vector<pair<int, int>>[n];
```

This is a C-style array where each element is a vector of pairs.

**Java**:
```java
List<List<int[]>> adj;
adj = new ArrayList<>();
for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
```

Java doesn't have array of generics directly, so we use `List<List<...>>` (a list of lists).

#### 2. Pair → int[]

**C++**:
```cpp
pair<int, int>  // {first, second}
l[x].push_back({y, wt});  // brace-init list
```

**Java**:
```java
int[]  // length-2 array
adj.get(x).add(new int[]{y, wt});
```

Java's `int[]` is more memory-efficient than `Pair<Integer, Integer>` (no autoboxing).

#### 3. Priority Queue with Comparator

**C++**:
```cpp
priority_queue<pair<int,int>, vector<pair<int,int>>, greater<pair<int,int>>> q;
```

The `greater<...>` template parameter makes it a min-heap (default is max-heap).

**Java**:
```java
PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0]));
```

Java's `PriorityQueue` is min-heap by default. We provide a custom comparator since `int[]` doesn't have a natural order.

#### 4. Top + Pop

**C++**:
```cpp
auto best = q.top();
q.pop();
```

Two operations: `top()` peeks, `pop()` removes.

**Java**:
```java
int[] best = pq.poll();
```

`poll()` does both: removes and returns the top.

#### 5. Pair Access

**C++**:
```cpp
int to = best.second;
int wt = best.first;
```

C++ pair has `.first` and `.second`.

**Java**:
```java
int weight = best[0];
int node = best[1];
```

Array index access.

#### 6. Boolean Array Init

**C++**:
```cpp
bool *visited = new bool[v]{0};  // all false
```

**Java**:
```java
boolean[] visited = new boolean[v];  // all false by default
```

Java arrays are zero-initialized automatically.

#### 7. Range-Based For

**C++**:
```cpp
for (auto x : l[to]) {
    if (visited[x.first] == 0) {
        q.push({x.second, x.first});
    }
}
```

**Java**:
```java
for (int[] edge : adj.get(node)) {
    int neighbor = edge[0];
    int edgeWeight = edge[1];
    if (!visited[neighbor]) {
        pq.offer(new int[]{edgeWeight, neighbor});
    }
}
```

#### 8. Push With Initializer

**C++**:
```cpp
q.push({x.second, x.first});  // {wt, node}
```

**Java**:
```java
pq.offer(new int[]{edgeWeight, neighbor});
```

Java needs explicit `new int[]{}`.

#### 9. int32_t

**C++**:
```cpp
int32_t main() { ... }
```

`int32_t` from `<cstdint>` is a guaranteed 32-bit int. Java's `int` is always 32-bit, so we just use `void main`.

#### 10. Fast I/O

**C++**:
```cpp
ios_base::sync_with_stdio(false);
cin.tie(NULL);
```

**Java equivalent for competitive**:
```java
BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
StreamTokenizer in = new StreamTokenizer(br);
```

For most interview-scale problems, `Scanner` is sufficient.

---

## 9. Visual Examples

### Example 1: Small Triangle

```
       1
    0───────1
    │      ╱
   3│  2 ╱
    │   ╱
    2───┘

Edges: (0,1,1), (0,2,3), (1,2,2)
```

#### Prim's Trace

```
Start: PQ = [(0, 0)], visited = []

Pop (0, 0): not visited. Add 0. Total=0. visited={0}.
  Push edges from 0: (1, 1), (3, 2).
  PQ = [(1, 1), (3, 2)].

Pop (1, 1): not visited. Add 1. Total=1. visited={0, 1}.
  Push edges from 1: (1, 0) already visited, skip. (2, 2).
  PQ = [(2, 2), (3, 2)].

Pop (2, 2): not visited. Add 2. Total=3. visited={0, 1, 2}.
  All neighbors visited. Push nothing.
  PQ = [(3, 2)].

Pop (3, 2): visited! Skip.
PQ = [].

MST total: 3.
MST edges: (0,1,1), (1,2,2). 
```

### Example 2: 4-Node Square

```
        1
    0───────1
    │       │
   2│      5│
    │       │
    2───────3
        3

Edges: (0,1,1), (0,2,2), (1,3,5), (2,3,3)
```

#### Prim's Trace

```
PQ = [(0, 0)]

Pop (0, 0): Add 0. Total=0.
  Push (1, 1), (2, 2).
  PQ = [(1, 1), (2, 2)].

Pop (1, 1): Add 1. Total=1.
  Push (5, 3).
  PQ = [(2, 2), (5, 3)].

Pop (2, 2): Add 2. Total=3.
  Push (3, 3).
  PQ = [(3, 3), (5, 3)].

Pop (3, 3): Add 3. Total=6.
  No unvisited neighbors.
  PQ = [(5, 3)].

Pop (5, 3): visited! Skip.

Final total: 6.
```

### Example 3: Star Graph

```
         1
         │
         5
    2────0────3
         │
         4
         
Edges: (0,1,5), (0,2,10), (0,3,3), (0,4,7)
```

#### Prim's Trace

```
PQ = [(0, 0)]

Pop (0, 0): Add 0. Total=0.
  Push (5, 1), (10, 2), (3, 3), (7, 4).
  PQ has 4 elements; min is (3, 3).

Pop (3, 3): Add 3. Total=3. No unvisited neighbors.

Pop (5, 1): Add 1. Total=8.

Pop (7, 4): Add 4. Total=15.

Pop (10, 2): Add 2. Total=25.

All visited. Done.

Final total: 25.
```

---

## 10. Detailed Dry Run with Diagrams

Let's trace through this 6-vertex example:

```
Input:
n = 6, m = 9
Edges (0-indexed):
  (0, 1, 4)
  (0, 2, 6)
  (0, 5, 5)
  (1, 2, 2)
  (1, 3, 2)
  (2, 3, 3)
  (2, 4, 6)
  (3, 4, 4)
  (4, 5, 8)
```

### Initial State

```
adj[0] = [(1,4), (2,6), (5,5)]
adj[1] = [(0,4), (2,2), (3,2)]
adj[2] = [(0,6), (1,2), (3,3), (4,6)]
adj[3] = [(1,2), (2,3), (4,4)]
adj[4] = [(2,6), (3,4), (5,8)]
adj[5] = [(0,5), (4,8)]

visited = [F, F, F, F, F, F]
totalWeight = 0
PQ = []
```

### Iteration 1: Push Start

```
Push (0, 0).
PQ = [(0, 0)]
```

### Iteration 2: Pop (0, 0)

```
Pop: weight=0, node=0.
visited[0]? No.
Mark visited[0] = true.
totalWeight = 0 + 0 = 0.

Push edges from 0 to unvisited:
  (4, 1) — push
  (6, 2) — push
  (5, 5) — push

PQ = [(4, 1), (6, 2), (5, 5)]
visited = [T, F, F, F, F, F]

Current MST: {0}
```

### Iteration 3: Pop (4, 1)

```
Pop: weight=4, node=1.
visited[1]? No.
Mark visited[1] = true.
totalWeight = 0 + 4 = 4.

Push edges from 1 to unvisited (skip 0):
  (2, 2) — push  (edge 1-2 weight 2)
  (2, 3) — push  (edge 1-3 weight 2)

PQ = [(2, 2), (2, 3), (6, 2), (5, 5)]
visited = [T, T, F, F, F, F]

Current MST: {0, 1}
```

### Iteration 4: Pop (2, 2)

```
Pop: weight=2, node=2.
visited[2]? No.
Mark visited[2] = true.
totalWeight = 4 + 2 = 6.

Push edges from 2 to unvisited (skip 0, 1):
  (3, 3) — push  (edge 2-3 weight 3)
  (6, 4) — push  (edge 2-4 weight 6)

PQ = [(2, 3), (3, 3), (5, 5), (6, 2), (6, 4)]
visited = [T, T, T, F, F, F]

Current MST: {0, 1, 2}
```

### Iteration 5: Pop (2, 3)

```
Pop: weight=2, node=3.
visited[3]? No.
Mark visited[3] = true.
totalWeight = 6 + 2 = 8.

Push edges from 3 to unvisited (skip 1, 2):
  (4, 4) — push  (edge 3-4 weight 4)

PQ = [(3, 3), (4, 4), (5, 5), (6, 2), (6, 4)]
visited = [T, T, T, T, F, F]

Current MST: {0, 1, 2, 3}
```

### Iteration 6: Pop (3, 3)

```
Pop: weight=3, node=3.
visited[3]? YES (added in previous step).
SKIP — stale edge (we already added 3 via a cheaper edge).

PQ = [(4, 4), (5, 5), (6, 2), (6, 4)]
```

### Iteration 7: Pop (4, 4)

```
Pop: weight=4, node=4.
visited[4]? No.
Mark visited[4] = true.
totalWeight = 8 + 4 = 12.

Push edges from 4 to unvisited (skip 2, 3):
  (8, 5) — push  (edge 4-5 weight 8)

PQ = [(5, 5), (6, 2), (6, 4), (8, 5)]
visited = [T, T, T, T, T, F]

Current MST: {0, 1, 2, 3, 4}
```

### Iteration 8: Pop (5, 5)

```
Pop: weight=5, node=5.
visited[5]? No.
Mark visited[5] = true.
totalWeight = 12 + 5 = 17.

No new edges to push (5's only unvisited would have been... none, since all 0-4 are visited).

PQ = [(6, 2), (6, 4), (8, 5)]
visited = [T, T, T, T, T, T]

Current MST: {0, 1, 2, 3, 4, 5} — DONE!
```

### Iteration 9-11: Stale Edges

```
Pop (6, 2): visited. Skip.
Pop (6, 4): visited. Skip.
Pop (8, 5): visited. Skip.

PQ = []. Loop exits.
```

### Final Result

```
MST total: 17.

MST edges added (in order):
  - Pop (0, 0):  starting vertex (weight 0 doesn't count)
  - Pop (4, 1):  edge 0-1 weight 4
  - Pop (2, 2):  edge 1-2 weight 2
  - Pop (2, 3):  edge 1-3 weight 2
  - Pop (4, 4):  edge 3-4 weight 4
  - Pop (5, 5):  edge 0-5 weight 5

Total: 4 + 2 + 2 + 4 + 5 = 17 ✓
5 edges = V - 1 = 6 - 1 ✓
```

### Comparison with Kruskal's

This same graph would produce the SAME total (17) using Kruskal's, though the order of edge selection and the specific edges may differ when ties exist. Both algorithms produce a valid MST.

---

## 11. Edge Cases

### 1. Single Vertex

```
n = 1, m = 0
PQ starts with (0, 0). Pop, add 0. No edges. Done.
Total = 0.
```

### 2. Two Vertices with One Edge

```
n = 2, m = 1, edge (0, 1, 5)
PQ = [(0, 0)].
Pop (0, 0): add 0. Push (5, 1).
Pop (5, 1): add 1. Total = 5.
```

### 3. Disconnected Graph

```
n = 4, m = 2, edges (0,1,3), (2,3,5)
PQ = [(0, 0)].
Pop (0, 0): add 0. Push (3, 1).
Pop (3, 1): add 1. No new pushes.
PQ becomes empty. Loop exits.

⚠️ Only 2 of 4 vertices visited!
Total = 3, but it's NOT a spanning tree.

For disconnected graphs, Prim's only spans the starting component.
Solution: check if all vertices visited at the end; if not, the graph is disconnected.

You could also run Prim's from each unvisited vertex to get a Minimum Spanning FOREST.
```

### 4. Self-Loop

```
n = 2, m = 2, edges (0,1,5), (0,0,100)
adj[0] = [(1,5), (0,100), (0,100)]  (added twice for undirected? OR once)

In addEdge for self-loop:
  adj.get(0).add({0, 100});  // y=0, wt=100
  adj.get(0).add({0, 100});  // adds again because x=y=0
So adj[0] gets the self-loop twice.

Prim's processes 0, pushes (100, 0) twice. But (0, 0) is already visited.
Both pops are stale. Self-loop never affects MST.
```

### 5. Multiple Edges Between Same Vertices

```
n = 2, m = 3
edges: (0,1,10), (0,1,3), (0,1,7)
adj[0] has three entries for 1: (1,10), (1,3), (1,7).

Prim's pushes all three. The smallest, (3, 1), is popped first → added.
Others are stale.

Prim's automatically picks cheapest parallel edge.
```

### 6. Negative Weights

```
Edges: (0,1,-5), (0,2,-3), (1,2,10)

PQ = [(0, 0)]. Pop 0.
Push (-5, 1), (-3, 2).
Pop (-5, 1): add 1. Total = -5.
Push (10, 2).
Pop (-3, 2): add 2. Total = -8.
Pop (10, 2): visited. Skip.

Total = -8.

Prim's works fine with negative weights! Unlike Dijkstra's.
```

### 7. All Same Weight

```
Any spanning tree is valid MST.
Prim's picks some valid one based on PQ order.
Total = (V-1) × weight.
```

---

## 12. Complexity Analysis

### Time Complexity

#### PQ Operations
- Push: O(log E).
- Pop: O(log E).

#### Total Operations
- Each edge is pushed at most twice (once from each endpoint, both directions in undirected). So total pushes: O(E).
- Each edge is popped at most twice. Total pops: O(E).

**Total: O(E log E) ≈ O(E log V)** (since E ≤ V²).

#### Comparison with Naive O(V²)

For sparse graphs (E ≈ V), Prim's with PQ is O(V log V).
For dense graphs (E ≈ V²), Prim's with array (O(V²)) can actually be faster: O(V²) vs O(V² log V).

### Space Complexity

- Adjacency list: O(V + E).
- PQ: O(E) worst case (all edges pushed).
- visited[]: O(V).

**Total: O(V + E)**.

### When Prim's Wins Over Kruskal's

| Aspect | Prim's | Kruskal's |
|--------|--------|-----------|
| **Time** | O(E log V) | O(E log E) |
| **Space** | O(V + E) | O(V + E) |
| **Dense graphs (E ≈ V²)** | Better | Worse |
| **Sparse graphs (E ≈ V)** | Comparable | Better |

For dense graphs, Prim's avoids the O(E log E) sort overhead.

---

## 13. Common Mistakes

### Mistake 1: Forgetting Min-Heap

```java
// WRONG: default PriorityQueue with Integer[] (compares lexicographically)
PriorityQueue<int[]> pq = new PriorityQueue<>();  
// ↑ ERROR: no natural ordering for int[]

// RIGHT: explicit comparator
PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0]));
```

Java's `int[]` doesn't have natural ordering, so you must provide a comparator.

### Mistake 2: Wrong Comparison Direction

```java
// WRONG: max-heap behavior
PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(b[0], a[0]));
// ↑ This picks LARGEST first, giving Maximum Spanning Tree

// RIGHT: min-heap
PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0]));
```

### Mistake 3: Marking Visited Before Pushing

```java
// WRONG: mark visited when pushing
if (!visited[neighbor]) {
    pq.offer(...);
    visited[neighbor] = true;  // ← TOO EARLY
}

// What if a CHEAPER edge to this neighbor exists later? 
// We'd never push it because it's already marked visited.

// RIGHT: mark visited when popping
while (!pq.isEmpty()) {
    int[] best = pq.poll();
    if (visited[best[1]]) continue;
    visited[best[1]] = true;
    // ...
}
```

This is the LAZY approach. Don't mark too early.

### Mistake 4: Not Handling Stale Edges

```java
// WRONG: assume top of PQ is always valid
while (!pq.isEmpty()) {
    int[] best = pq.poll();
    // directly process — but what if it's stale?
}

// RIGHT: check and skip stale
while (!pq.isEmpty()) {
    int[] best = pq.poll();
    if (visited[best[1]]) continue;  // STALE!
    // process
}
```

Same vertex can be in PQ multiple times. After the cheapest is popped, others are stale.

### Mistake 5: Adding to Visited Right After Pop, Forgetting

```java
// WRONG: process without checking visited
int node = pq.poll()[1];
visited[node] = true;  // mark NOW
total += weight;
// ...

// What if THIS is the stale one?

// RIGHT:
int[] best = pq.poll();
int node = best[1];
if (visited[node]) continue;
visited[node] = true;
// ...
```

### Mistake 6: Forgetting Undirected Edges

```java
// WRONG: only one direction
adj.get(x).add(new int[]{y, wt});
// adj.get(y) doesn't know about this edge!

// RIGHT: add both directions
adj.get(x).add(new int[]{y, wt});
adj.get(y).add(new int[]{x, wt});
```

For undirected graphs, MUST add both ways.

### Mistake 7: Starting at a Disconnected Vertex

```
If you start Prim's at a vertex in a disconnected component, 
you'll only get the MST of that component.

For full MST (or determining disconnection), check if all 
vertices are visited at the end.
```

### Mistake 8: Integer Overflow on Large Weights

```java
// WRONG:
int total = 0;
// If weights are 10^9 and 10^5 edges, total can exceed int range.

// RIGHT:
long total = 0;
// Or use cast: (int) (long_sum)
```

### Mistake 9: Wrong Pair Storage Order

```java
// If you store {node, weight}:
pq.offer(new int[]{neighbor, edgeWeight});

// But your comparator uses a[0] (the node):
// ((a, b) -> Integer.compare(a[0], b[0]))

// You'd be sorting by NODE ID, not weight! Bug.

// RIGHT: use consistent {weight, node} ordering.
```

---

## 14. Prim vs Kruskal — Detailed Comparison

### Side-by-Side Algorithm Comparison

| Aspect | Prim's | Kruskal's |
|--------|--------|-----------|
| **Strategy** | Vertex-based: grow MST from a seed | Edge-based: sort and pick |
| **Data structure** | Priority queue (min-heap) | DSU (Union-Find) |
| **Process** | Maintain growing MST + frontier | Process edges in sorted order |
| **Cycle check** | "Has this vertex been added yet?" | "Are these two in same component?" |

### Performance Comparison

| Graph Type | Kruskal's | Prim's (PQ) | Prim's (Array) |
|------------|-----------|-------------|-----------------|
| **Sparse (E ≈ V)** | O(E log V) | O(E log V) | O(V²) |
| **Dense (E ≈ V²)** | O(V² log V) | O(V² log V) | **O(V²)** ← best |

**Bottom line**: For dense graphs, array-based Prim's (no PQ) is fastest.

### When to Choose Which

**Choose Kruskal's if**:
- Input is edge list (natural format).
- Graph is sparse.
- DSU is more comfortable for you.
- You only need yes/no questions about connectivity.

**Choose Prim's if**:
- Input is adjacency list (natural format).
- Graph is dense.
- You want to grow MST incrementally (e.g., visualization).
- You're already comfortable with PQ.

### Algorithmic Difference in One Line

> Kruskal's: process EDGES in sorted order.
> Prim's: process VERTICES in order of frontier edge weight.

### Output: Are They Identical?

When edge weights are **distinct**, Kruskal's and Prim's produce **the exact same MST**.

When weights have ties, they may produce **different MSTs**, but **both have the same minimum total weight**.

---

## 15. Why It's Optimal — The Cut Property

### The Theorem (Same as Kruskal's)

> **Cut Property**: For any cut in the graph, the minimum-weight edge crossing the cut is in SOME MST.

### Why Prim's Satisfies This

At each step, Prim's considers the cut between "inside MST" and "outside MST".

The active edges in the PQ are exactly the cut edges (or stale ones from past cuts).

When we pop the cheapest non-stale edge, that's the minimum cut edge.

By the Cut Property, this edge is in some MST → Prim's choice is consistent with building an optimal MST.

### Formal Proof Sketch

Suppose Prim's produces tree P, and the actual MST is T*.

For each edge `e` Prim's added (in order), if e is in T*, fine. If not:
- Adding e to T* creates a cycle (since T* is a spanning tree).
- The cycle must contain some other edge `e'` between Prim's "inside" and "outside" at the moment we picked e.
- Since Prim's picked the cheapest such edge, weight(e) ≤ weight(e').
- Swap e for e' in T*: still a spanning tree, weight ≤ original.

Repeat the swap argument to transform T* into P. Since each swap doesn't increase weight, P's total ≤ T*'s total.

But T* is the MINIMUM, so P's total = T*'s total → P is also an MST.

### Greedy Choice Matrix Property

The MST problem has a "matroid" structure: spanning trees of a graph form a matroid. Greedy algorithms always work on matroids — this is the deep reason both Kruskal's and Prim's are correct.

---

## 16. Variants of Prim's

### Variant 1: Array-Based (O(V²))

For dense graphs, use a simple array instead of PQ:

```java
public int primArrayMST(int v, int[][] adjMatrix) {
    int[] key = new int[v];  // min edge weight to reach this vertex
    boolean[] inMST = new boolean[v];
    Arrays.fill(key, Integer.MAX_VALUE);
    key[0] = 0;
    
    int total = 0;
    for (int count = 0; count < v; count++) {
        // Find min key among non-MST vertices
        int u = -1;
        for (int i = 0; i < v; i++) {
            if (!inMST[i] && (u == -1 || key[i] < key[u])) {
                u = i;
            }
        }
        
        inMST[u] = true;
        total += key[u];
        
        // Update keys of u's neighbors
        for (int v2 = 0; v2 < v; v2++) {
            if (adjMatrix[u][v2] != 0 && !inMST[v2] && adjMatrix[u][v2] < key[v2]) {
                key[v2] = adjMatrix[u][v2];
            }
        }
    }
    return total;
}
```

Time: O(V²). Better than PQ for dense graphs.

### Variant 2: Indexed Priority Queue

For more efficient updates, use an "indexed priority queue" (a heap that supports decrease-key). Time: O(E log V) — better than lazy PQ.

Java's standard library doesn't have this, but you can implement it or use libraries like Apache Commons.

### Variant 3: Fibonacci Heap

Theoretical optimum: O(E + V log V). Rarely used in practice due to constant factor.

### Variant 4: Track MST Edges (Not Just Weight)

```java
// In addition to total, store the parent edge for each vertex
int[] parent = new int[v];
Arrays.fill(parent, -1);

// When adding a vertex via edge from `from`:
parent[node] = from;

// Reconstruct: edges = {(parent[i], i) for i in 0..v-1, i != start}
```

---

## 17. Real-World Applications

### 1. Network Design
Connect cities with minimum total cable length.

### 2. Cluster Analysis
Single-linkage hierarchical clustering uses MST.

### 3. Image Segmentation
Build MST on pixel grid, segment based on edges.

### 4. Power Grid Design
Minimize total wire length connecting all stations.

### 5. Game Map Generation
Generate connected dungeons with minimal "corridor" cost.

### 6. Approximation Algorithms
TSP heuristics often use MST as a baseline.

### 7. Phylogeny in Biology
Build evolutionary trees from DNA distance matrices.

---

## 18. Complete Java Code

### Faithful Conversion (Matches C++ Style)

```java
import java.util.*;

public class PrimMST {
    
    static class Graph {
        List<List<int[]>> adj;
        int v;
        
        public Graph(int n) {
            this.v = n;
            this.adj = new ArrayList<>();
            for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        }
        
        public void addEdge(int x, int y, int wt) {
            adj.get(x).add(new int[]{y, wt});
            adj.get(y).add(new int[]{x, wt});
        }
        
        public int primMST() {
            PriorityQueue<int[]> pq = new PriorityQueue<>(
                (a, b) -> Integer.compare(a[0], b[0])
            );
            boolean[] visited = new boolean[v];
            int totalWeight = 0;
            
            pq.offer(new int[]{0, 0});
            
            while (!pq.isEmpty()) {
                int[] best = pq.poll();
                int weight = best[0];
                int node = best[1];
                
                if (visited[node]) continue;
                
                visited[node] = true;
                totalWeight += weight;
                
                for (int[] edge : adj.get(node)) {
                    int neighbor = edge[0];
                    int edgeWeight = edge[1];
                    if (!visited[neighbor]) {
                        pq.offer(new int[]{edgeWeight, neighbor});
                    }
                }
            }
            
            return totalWeight;
        }
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        int m = scanner.nextInt();
        
        Graph g = new Graph(n);
        for (int i = 0; i < m; i++) {
            int x = scanner.nextInt();
            int y = scanner.nextInt();
            int w = scanner.nextInt();
            g.addEdge(x - 1, y - 1, w);
        }
        
        System.out.println(g.primMST());
        scanner.close();
    }
}
```

### Production-Ready Version (Returns MST Edges)

```java
import java.util.*;

public class PrimMSTPro {
    
    /**
     * Compute MST using Prim's algorithm.
     * @param v Number of vertices
     * @param adj Adjacency list: adj.get(u) = list of {neighbor, weight}
     * @return [totalWeight, mstEdges]: total weight and the V-1 edges chosen
     */
    public static Object[] prim(int v, List<List<int[]>> adj) {
        PriorityQueue<int[]> pq = new PriorityQueue<>(
            (a, b) -> Integer.compare(a[0], b[0])
        );  // {weight, node, parent}
        
        boolean[] visited = new boolean[v];
        long totalWeight = 0;  // long for safety
        List<int[]> mstEdges = new ArrayList<>();
        
        pq.offer(new int[]{0, 0, -1});  // start: weight 0, node 0, no parent
        
        while (!pq.isEmpty()) {
            int[] best = pq.poll();
            int weight = best[0];
            int node = best[1];
            int parent = best[2];
            
            if (visited[node]) continue;
            
            visited[node] = true;
            totalWeight += weight;
            
            if (parent != -1) {
                mstEdges.add(new int[]{parent, node, weight});
            }
            
            for (int[] edge : adj.get(node)) {
                if (!visited[edge[0]]) {
                    pq.offer(new int[]{edge[1], edge[0], node});
                }
            }
        }
        
        // Verify spanning tree (handle disconnected)
        for (boolean visit : visited) {
            if (!visit) {
                return new Object[]{Long.MAX_VALUE, null};  // disconnected
            }
        }
        
        return new Object[]{totalWeight, mstEdges};
    }
    
    public static void main(String[] args) {
        // Test: 4 vertices, classic example
        int v = 4;
        List<List<int[]>> adj = new ArrayList<>();
        for (int i = 0; i < v; i++) adj.add(new ArrayList<>());
        
        // Add edges (undirected)
        int[][] edges = {{0,1,10}, {0,2,6}, {0,3,5}, {1,3,15}, {2,3,4}};
        for (int[] e : edges) {
            adj.get(e[0]).add(new int[]{e[1], e[2]});
            adj.get(e[1]).add(new int[]{e[0], e[2]});
        }
        
        Object[] result = prim(v, adj);
        System.out.println("MST total: " + result[0]);
        List<int[]> mst = (List<int[]>) result[1];
        for (int[] e : mst) {
            System.out.println("  Edge: " + e[0] + "-" + e[1] + " weight " + e[2]);
        }
    }
}
```

---

## 19. Interview Tips

### How to Approach in Interview

1. **Restate the problem**: "Find the minimum-weight spanning tree."
2. **Mention both algorithms**: "I'll use Prim's with a priority queue. Kruskal's with DSU is also valid."
3. **Explain the greedy intuition**: "Grow the MST one vertex at a time, always picking the cheapest active edge."
4. **Justify the PQ**: "PQ gives us the cheapest active edge in O(log E)."
5. **Code it carefully**.
6. **Discuss complexity**: O(E log V) with PQ.

### Discussion Points to Score Bonus

#### 1. The Greedy Intuition
> "At each step, I pick the cheapest edge connecting the MST to an outside vertex. This works because of the Cut Property — the minimum cut edge is always in some MST."

#### 2. Why PQ?
> "The priority queue gives me the minimum-weight active edge in O(log E). Without it, I'd need to scan all active edges each step — too slow."

#### 3. The Lazy Pattern
> "I use lazy deletion — when popping, I check if the destination is already visited. If yes, that's a stale edge. This is simpler than maintaining a 'clean' PQ."

#### 4. Comparison with Kruskal's
> "Kruskal's is edge-based, Prim's is vertex-based. Kruskal's sorts all edges; Prim's uses a PQ to track active edges. For sparse graphs they're comparable; for dense graphs, Prim's with an array (no PQ) can be faster."

#### 5. Why Start Anywhere?
> "Prim's starts at any vertex. The MST's total weight is the same regardless. I conventionally start at vertex 0."

#### 6. Disconnected Graph Handling
> "Prim's only finds MST of one component. To detect disconnection, verify all vertices are visited at the end."

### Likely Follow-Up Questions

#### Q: What if you start at a different vertex?
**A**: Same MST total. The specific edges might differ if ties exist, but total weight is unique.

#### Q: How to extract the MST edges, not just total?
**A**: Track parent of each added vertex. Build edges from parent[] array.

#### Q: Can it handle negative weights?
**A**: Yes! Unlike Dijkstra's, Prim's doesn't care about sign of weights.

#### Q: What about disconnected graphs?
**A**: Prim's spans only one component. Check visited[] afterward.

#### Q: Prim's vs Kruskal's — which to use?
**A**: For dense graphs, Prim's (with array). For sparse, either works; Kruskal's is simpler if you have edge list.

#### Q: How would you find the second-best MST?
**A**: For each edge in MST, find the cheapest replacement; pick the smallest weight increase.

#### Q: What's the time complexity?
**A**: O(E log V) with lazy PQ. O(E + V log V) with Fibonacci heap. O(V²) with array (best for dense).

### Common Interview Mistakes

1. Forgetting min-heap (Java needs explicit comparator).
2. Marking visited too early (when pushing instead of popping).
3. Not skipping stale edges in PQ.
4. Forgetting to add both directions for undirected edges.
5. Using `int` for total (overflow on large weights).

---

## TL;DR

### The Mental Model

```
Grow MST one vertex at a time.
Maintain a min-heap of "active edges" (from inside MST to outside).
Repeatedly pop the cheapest active edge.
  If destination not yet in MST → add it.
  Else → it's a stale edge. Skip.

Continue until all vertices are added.
```

### The Algorithm in 30 Seconds

```
1. Initialize PQ with (0, start_vertex).
2. While PQ not empty:
   - Pop (weight, node).
   - If visited, skip.
   - Mark visited. total += weight.
   - Push (edge_weight, neighbor) for unvisited neighbors.
3. Return total.
```

### The Five Key Insights

1. **Greedy works** because of the Cut Property.
2. **Priority queue** gives us the cheapest active edge in O(log E).
3. **Lazy deletion**: skip stale edges on pop, not on push.
4. **Adjacency list** is the natural representation.
5. **Start anywhere** — result is the same.

### When This Problem Appears

| Tier | Frequency | Example |
|------|-----------|---------|
| Tier 1 | Sometimes | Basic version |
| Tier 2 | Often | Paytm, Flipkart (LC 1584, 1135) |
| Tier 3 | Often | Google, Amazon |
| Tier 4 | Variations | Top quant |

### C++ to Java Cheatsheet

| C++ | Java |
|-----|------|
| `vector<pair<int,int>>*` | `List<List<int[]>>` |
| `pair<int,int>` | `int[]` |
| `priority_queue<...,greater<>>` | `PriorityQueue<>(Comparator)` |
| `q.top(); q.pop()` | `pq.poll()` |
| `.first / .second` | `[0] / [1]` |
| `bool* arr = new bool[n]{0}` | `boolean[] arr = new boolean[n]` |
| `class { public: ... }` | `static class { ... }` |
| `int32_t main` | `public static void main` |

### Final Code Snippet to Memorize

```java
public int primMST(int v, List<List<int[]>> adj) {
    PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]);
    boolean[] visited = new boolean[v];
    int total = 0;
    pq.offer(new int[]{0, 0});
    
    while (!pq.isEmpty()) {
        int[] curr = pq.poll();
        int weight = curr[0], node = curr[1];
        if (visited[node]) continue;
        visited[node] = true;
        total += weight;
        for (int[] edge : adj.get(node)) {
            if (!visited[edge[0]]) {
                pq.offer(new int[]{edge[1], edge[0]});
            }
        }
    }
    return total;
}
```

---

*Master Prim's MST and you've internalized a fundamental pattern: greedy growth with priority queue. This pattern recurs in Dijkstra's shortest path, A* search, and many other algorithms. The "lazy PQ + skip stale" trick is especially powerful and appears throughout competitive programming.*
