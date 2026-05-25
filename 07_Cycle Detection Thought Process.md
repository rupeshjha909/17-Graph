# Cycle Detection in Undirected Graph — Complete Deep Dive

A comprehensive guide to detecting cycles in undirected graphs using both **DFS** and **BFS** approaches. Includes diagrams, dry runs, common pitfalls, and a comparison with directed graph cycle detection.

---

## Table of Contents

1. [Problem Statement](#1-problem-statement)
2. [What Is a Cycle?](#2-what-is-a-cycle)
3. [Why Cycle Detection Matters](#3-why-cycle-detection-matters)
4. [The Core Insight: Why Undirected Is Different](#4-the-core-insight-why-undirected-is-different)
5. [Visual Examples of Cycles vs No Cycles](#5-visual-examples-of-cycles-vs-no-cycles)
6. [Approach 1: DFS with Parent Tracking](#6-approach-1-dfs-with-parent-tracking)
7. [Approach 2: BFS with Parent Tracking](#7-approach-2-bfs-with-parent-tracking)
8. [Approach 3: Union-Find (Bonus)](#8-approach-3-union-find-bonus)
9. [Comparing the Three Approaches](#9-comparing-the-three-approaches)
10. [Dry Run with Diagrams](#10-dry-run-with-diagrams)
11. [Edge Cases](#11-edge-cases)
12. [Common Mistakes](#12-common-mistakes)
13. [Complexity Analysis](#13-complexity-analysis)
14. [Undirected vs Directed Cycle Detection](#14-undirected-vs-directed-cycle-detection)
15. [Variations and Follow-ups](#15-variations-and-follow-ups)
16. [Complete Java Code](#16-complete-java-code)
17. [Interview Tips](#17-interview-tips)

---

## 1. Problem Statement

> Given an **undirected graph** with `V` vertices and `E` edges, determine whether the graph contains a **cycle**.
>
> Return `true` if a cycle exists, `false` otherwise.

### Input Format
Typically given as one of:
- Adjacency list: `List<List<Integer>> adj`.
- Edge list: `int[][] edges` where `edges[i] = [u, v]`.
- Number of vertices `V` + edges.

### Examples

#### Example 1: Has a cycle
```
Vertices: 5
Edges: [(0,1), (1,2), (2,3), (3,4), (4,1)]

Graph:
    0
    |
    1───2
    |   |
    4───3

Output: TRUE
Cycle: 1 → 2 → 3 → 4 → 1
```

#### Example 2: No cycle
```
Vertices: 5
Edges: [(0,1), (1,2), (2,3), (3,4)]

Graph:
    0 ─ 1 ─ 2 ─ 3 ─ 4
    
(This is a tree — no cycle)

Output: FALSE
```

#### Example 3: Disconnected with cycle
```
Vertices: 6
Edges: [(0,1), (1,2), (2,0), (3,4)]

Graph:
   0─1         3─4
    \|         
     2          5  (isolated)

Component 1 has cycle: 0 → 1 → 2 → 0
Component 2 is just an edge (no cycle)
Vertex 5 is isolated

Output: TRUE
```

---

## 2. What Is a Cycle?

A **cycle** in an undirected graph is a path that:
1. Starts and ends at the **same vertex**.
2. Uses **distinct vertices** along the way (except start = end).
3. Uses **distinct edges** (no edge repeated).
4. Has **length ≥ 3** (at least 3 vertices involved).

### Why Length ≥ 3?
In a directed graph, a self-loop (`A → A`) is a cycle of length 1.

But in an **undirected** graph:
- An edge `(A, B)` lets you go A → B and B → A.
- Going A → B → A is NOT considered a cycle. It's just traversing the same edge twice.
- This is a critical distinction (covered in detail in Section 4).

### Visualization

```
Length-3 cycle (triangle):       Length-4 cycle (square):
    A                                A───B
   ╱ ╲                                │   │
  ╱   ╲                               │   │
 B─────C                              D───C
 
TRUE cycle                         TRUE cycle


Tree (no cycle):                  Path (no cycle):
       A                          A ─ B ─ C ─ D ─ E
      ╱ ╲
     B   C
    ╱ ╲
   D   E

FALSE                              FALSE
```

---

## 3. Why Cycle Detection Matters

### Real-World Applications

#### 1. Detecting Deadlocks
In a system where processes hold resources and wait for others, a cycle in the resource-allocation graph = **deadlock**.

#### 2. Finding Loops in Networks
- Network routing protocols (avoid forwarding loops).
- Circuit verification (no short circuits).

#### 3. Validating Trees
A graph is a tree if and only if:
- It's connected.
- It has no cycles.
- It has exactly V-1 edges.

#### 4. Dependency Cycles
- If your build system finds a cycle in dependencies → can't build.
- Class inheritance loops are illegal in most languages.

#### 5. Game Maps
- Find loops in pathfinding grids.

---

## 4. The Core Insight: Why Undirected Is Different

### The Trap with Undirected Graphs

Consider this simple edge:

```
A ──── B
```

In adjacency list:
```
A: [B]
B: [A]
```

If we start DFS from A:
- Visit A. Mark A as visited.
- Go to A's neighbor: B.
- Visit B. Mark B as visited.
- Go to B's neighbor: **A** ← already visited!

Naive cycle detection would say: "A is visited → cycle detected!"

**But this is NOT a cycle.** We just walked back along the same edge.

### The Fix: Parent Tracking

The key idea: when visiting B from A, **remember that A is B's parent in the DFS traversal**. When checking neighbors of B:
- If neighbor is `A` (the parent) → that's the edge we came from. **Ignore it**.
- If neighbor is `visited AND not parent` → real cycle!

```
A ── B
^    │
│    ▼ (visit B)
│    Check neighbors of B: {A}
│    A is visited, BUT A is B's parent → NOT a cycle
```

vs.

```
A ── B
 \   │
  \  │
   \ ▼
    C
    │
    A (back to A)

When visiting C, we see A as a neighbor.
A is visited AND A is NOT C's parent (B was) → CYCLE!
```

### The Two Conditions for a Real Cycle

In undirected DFS:
> A neighbor `n` of current node `u` indicates a cycle if:
> 1. `n` is visited, **AND**
> 2. `n` is NOT `u`'s parent (i.e., not the node we came from).

This is the **entire trick** for undirected cycle detection.

---

## 5. Visual Examples of Cycles vs No Cycles

### Example A: Simple Triangle (Cycle)

```
     0
    ╱ ╲
   ╱   ╲
  1─────2

Edges: (0,1), (1,2), (0,2)
```

DFS from 0:
```
Step 1: Visit 0. Parent: -1.
         visited = {0}
Step 2: Neighbors of 0: {1, 2}. Pick 1.
         Visit 1. Parent: 0.
         visited = {0, 1}
Step 3: Neighbors of 1: {0, 2}.
         - 0: visited, but it's parent. Skip.
         - 2: not visited. Visit it. Parent: 1.
         visited = {0, 1, 2}
Step 4: Neighbors of 2: {0, 1}.
         - 0: visited AND NOT parent (parent is 1). CYCLE! ✓
```

### Example B: Path (No Cycle)

```
0 ── 1 ── 2 ── 3

Edges: (0,1), (1,2), (2,3)
```

DFS from 0:
```
Step 1: Visit 0. Parent: -1.
Step 2: Neighbors of 0: {1}. Pick 1.
         Visit 1. Parent: 0.
Step 3: Neighbors of 1: {0, 2}.
         - 0: visited, parent → skip.
         - 2: not visited. Visit it. Parent: 1.
Step 4: Neighbors of 2: {1, 3}.
         - 1: visited, parent → skip.
         - 3: not visited. Visit it. Parent: 2.
Step 5: Neighbors of 3: {2}.
         - 2: visited, parent → skip.
DFS done. No cycle detected.
```

### Example C: Tree (No Cycle)

```
       0
      ╱ ╲
     1   2
    ╱ ╲
   3   4

Edges: (0,1), (0,2), (1,3), (1,4)
```

A tree is connected and acyclic. The DFS traversal flows down without ever finding a "visited non-parent" → no cycle.

### Example D: Disconnected with Cycle

```
   0─1      3       5
   │ │      │
   └─2      4

Components:
  Component 1: {0, 1, 2} with edges (0,1), (1,2), (0,2) — TRIANGLE → CYCLE
  Component 2: {3, 4} with edge (3,4) — just an edge, no cycle
  Component 3: {5} — isolated, no cycle
```

Critical: must run DFS/BFS from **every unvisited node** to handle disconnected components.

### Example E: Subtle Case (4-cycle in larger graph)

```
   0───1
   │   │
   2   │
   │   │
   3───4───5
       │
       6

Edges: (0,1), (0,2), (1,4), (2,3), (3,4), (4,5), (4,6)

Visually it looks complex but:
- Tracing 0 → 1 → 4 → 3 → 2 → 0 → CYCLE!
```

DFS from 0 would find this cycle when it tries to revisit 0 through path 0 → 2 → 3 → 4 → 1 → 0.

---

## 6. Approach 1: DFS with Parent Tracking

### The Algorithm

```
1. Mark all nodes as unvisited.
2. For each unvisited node, call DFS:
   a. Mark current node as visited.
   b. For each neighbor of current node:
      - If neighbor is not visited:
        Recursively call DFS on neighbor with current as parent.
        If DFS returns true → cycle found, return true.
      - Else if neighbor is NOT the parent:
        Return true (cycle found!).
3. If no cycle found in any component, return false.
```

### Visualization of the DFS Tree

When DFS runs on a graph, it builds a **DFS tree**. Edges fall into two categories:

```
Original graph:           DFS tree (from node 0):
     0                          0
    ╱│╲                        ╱│
   ╱ │ ╲                      ╱ │
  1  2  3                    1  3
  │  │                       │
  └──┘                       2  ← visited but not via tree
                              ↑
                              "back edge" — indicates CYCLE!

Tree edges: (0,1), (1,2), (0,3)
Back edge:  (2,0)  ← this is the cycle indicator
```

A **back edge** (edge to an already-visited non-parent ancestor) signals a cycle.

### Java Code (Recursive DFS)

```java
import java.util.*;

class GraphCycleDetector {
    
    /**
     * Detects if undirected graph has a cycle.
     * @param V Number of vertices
     * @param adj Adjacency list
     * @return true if cycle exists, false otherwise
     */
    public boolean hasCycle(int V, List<List<Integer>> adj) {
        boolean[] visited = new boolean[V];
        
        // Handle disconnected components
        for (int i = 0; i < V; i++) {
            if (!visited[i]) {
                if (dfs(i, -1, visited, adj)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * DFS from node `node`, with `parent` being the node we came from.
     * Returns true if a cycle is detected.
     */
    private boolean dfs(int node, int parent, boolean[] visited, List<List<Integer>> adj) {
        visited[node] = true;
        
        for (int neighbor : adj.get(node)) {
            if (!visited[neighbor]) {
                // Recurse with current node as parent
                if (dfs(neighbor, node, visited, adj)) {
                    return true;
                }
            } else if (neighbor != parent) {
                // Visited AND not the parent → CYCLE!
                return true;
            }
        }
        return false;
    }
}
```

### Line-by-Line Explanation

#### `boolean[] visited = new boolean[V];`
Track which nodes have been visited. Index = node number.

#### Outer loop: `for (int i = 0; i < V; i++)`
This is **crucial for disconnected graphs**. We run DFS from every unvisited node.

If we only ran DFS from node 0, we'd miss cycles in components not connected to 0.

#### `if (dfs(i, -1, visited, adj))`
- Start DFS from node `i`.
- Parent is `-1` (no parent, since this is the start).
- If DFS returns true (cycle found) → propagate up.

#### Inside `dfs()`:

```java
visited[node] = true;
```
Mark current node before recursing — prevents re-visiting in this DFS call.

```java
for (int neighbor : adj.get(node)) {
```
Iterate all neighbors.

```java
if (!visited[neighbor]) {
    if (dfs(neighbor, node, visited, adj)) {
        return true;
    }
}
```
If neighbor is unvisited, recurse with `node` as the new parent. If recursive call finds a cycle, propagate it.

```java
} else if (neighbor != parent) {
    return true;
}
```
**The key check**: neighbor is visited (already seen) AND is NOT our parent → cycle!

### Iterative DFS Version (Avoiding Stack Overflow)

For very large graphs, recursion can blow the stack. Here's an iterative version using an explicit stack:

```java
public boolean hasCycleIterative(int V, List<List<Integer>> adj) {
    boolean[] visited = new boolean[V];
    
    for (int start = 0; start < V; start++) {
        if (visited[start]) continue;
        
        // Stack stores {node, parent}
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{start, -1});
        
        while (!stack.isEmpty()) {
            int[] curr = stack.pop();
            int node = curr[0], parent = curr[1];
            
            if (visited[node]) continue;
            visited[node] = true;
            
            for (int neighbor : adj.get(node)) {
                if (!visited[neighbor]) {
                    stack.push(new int[]{neighbor, node});
                } else if (neighbor != parent) {
                    return true;  // CYCLE!
                }
            }
        }
    }
    return false;
}
```

⚠️ **Subtle Issue**: This iterative version has a potential bug! Because the same node can be pushed to the stack multiple times before being processed, we might incorrectly report a cycle. The recursive version is safer for correctness. For huge graphs, prefer BFS (next section) which doesn't have this issue.

---

## 7. Approach 2: BFS with Parent Tracking

### The Algorithm

```
1. Mark all nodes as unvisited.
2. For each unvisited node:
   a. Use BFS starting from this node.
   b. Track parent of each node as we enqueue.
   c. If we encounter an already-visited node that's NOT the parent
      of the current node → cycle found.
3. Return false if no cycle found.
```

### Why BFS Works

The same parent-tracking idea applies. When we BFS-explore from a node, we look at its neighbors:
- If neighbor unvisited → enqueue with current as parent.
- If neighbor visited AND not parent → cycle!

### Visualization

```
Graph:
   0
   │
   1
  ╱ ╲
 2   3
 │   │
 └───4───5

Edges: (0,1), (1,2), (1,3), (2,4), (3,4), (4,5)
```

BFS from 0:
```
Queue: [(0, -1)]
       Visit 0, parent=-1, visited={0}

Process 0, enqueue 1 with parent 0
Queue: [(1, 0)]
       Visit 1, visited={0,1}

Process 1, enqueue 2 (parent 1), enqueue 3 (parent 1)
Queue: [(2, 1), (3, 1)]
       Visit 2, visited={0,1,2}

Process 2, neighbors: {1, 4}
  - 1: visited, IS parent. Skip.
  - 4: not visited. Enqueue (4, 2).
Queue: [(3, 1), (4, 2)]
       Visit 3, visited={0,1,2,3}

Process 3, neighbors: {1, 4}
  - 1: visited, IS parent. Skip.
  - 4: visited (because we just enqueued it as parent=2), 
       AND 4's parent (as tracked) is 2, NOT 3.
       
       Wait — let me think about this carefully.
```

This is where BFS cycle detection gets tricky. Let me be precise about the algorithm.

### BFS Cycle Detection — The Right Way

The standard formulation: **track parent[] array** as we BFS.

```java
public boolean hasCycleBFS(int V, List<List<Integer>> adj) {
    boolean[] visited = new boolean[V];
    int[] parent = new int[V];
    Arrays.fill(parent, -1);
    
    for (int start = 0; start < V; start++) {
        if (visited[start]) continue;
        
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(start);
        visited[start] = true;
        
        while (!queue.isEmpty()) {
            int node = queue.poll();
            
            for (int neighbor : adj.get(node)) {
                if (!visited[neighbor]) {
                    visited[neighbor] = true;
                    parent[neighbor] = node;  // record who discovered me
                    queue.offer(neighbor);
                } else if (neighbor != parent[node]) {
                    // Visited AND not my parent → cycle!
                    return true;
                }
            }
        }
    }
    return false;
}
```

### Re-Tracing the Visualization

```
Graph:
   0
   │
   1
  ╱ ╲
 2   3
  ╲ ╱
   4

Edges: (0,1), (1,2), (1,3), (2,4), (3,4)
```

BFS from 0:

```
Init: visited[0]=true, parent[0]=-1
Queue: [0]

Process 0:
  Neighbors of 0: {1}
  - 1 not visited → visited[1]=true, parent[1]=0, enqueue 1
Queue: [1]

Process 1:
  Neighbors of 1: {0, 2, 3}
  - 0: visited, parent[1]=0 → 0 IS parent. Skip.
  - 2: not visited → visited[2]=true, parent[2]=1, enqueue 2
  - 3: not visited → visited[3]=true, parent[3]=1, enqueue 3
Queue: [2, 3]

Process 2:
  Neighbors of 2: {1, 4}
  - 1: visited, parent[2]=1 → IS parent. Skip.
  - 4: not visited → visited[4]=true, parent[4]=2, enqueue 4
Queue: [3, 4]

Process 3:
  Neighbors of 3: {1, 4}
  - 1: visited, parent[3]=1 → IS parent. Skip.
  - 4: visited, parent[3]=1, but 4 ≠ 1 → 4 is NOT 3's parent.
       
       CYCLE DETECTED! ✓
```

The cycle is: `1 → 2 → 4 → 3 → 1`.

### Why This Works

When BFS encounters an already-visited node `4` while processing node `3`:
- If `4` was discovered by `3` itself, `parent[3]` would be `4` (or 4 wouldn't be visited).
- Since `parent[3] = 1` (not 4), it means `4` was reached via a DIFFERENT path.
- This means there are TWO ways to reach `4`: through `2` and through `3`.
- Two paths to the same node = cycle.

---

## 8. Approach 3: Union-Find (Bonus)

### The Algorithm

For each edge `(u, v)`:
1. Find the parent (root) of `u` and `v`.
2. If they have the **same root** → cycle! (They're already connected.)
3. Otherwise → union them.

### Why It Works

If `u` and `v` are already in the same component when we process edge `(u, v)`, then there's already a path from `u` to `v`. Adding the direct edge creates a cycle.

### Java Implementation

```java
class UnionFind {
    int[] parent;
    int[] rank;
    
    public UnionFind(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
    }
    
    public int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]);  // path compression
        }
        return parent[x];
    }
    
    public boolean union(int x, int y) {
        int px = find(x), py = find(y);
        if (px == py) return false;  // already connected!
        
        // Union by rank
        if (rank[px] < rank[py]) parent[px] = py;
        else if (rank[px] > rank[py]) parent[py] = px;
        else { parent[py] = px; rank[px]++; }
        
        return true;
    }
}

public boolean hasCycleUnionFind(int V, int[][] edges) {
    UnionFind uf = new UnionFind(V);
    for (int[] edge : edges) {
        if (!uf.union(edge[0], edge[1])) {
            return true;  // already in same set → cycle!
        }
    }
    return false;
}
```

### Why Union-Find Is Elegant

- No parent tracking complexity.
- Each edge processed once: O(α(V)) per operation (effectively O(1)).
- Total: **O(E · α(V))** which is nearly O(E + V).
- Handles disconnected components naturally.

### When Union-Find Wins
- When you only have an edge list, not adjacency list.
- When you also want to know which component a node belongs to.
- For incremental queries (add edges over time, detect when cycle forms).

---

## 9. Comparing the Three Approaches

| Aspect | DFS | BFS | Union-Find |
|--------|-----|-----|------------|
| **Time** | O(V + E) | O(V + E) | O(E · α(V)) ≈ O(E) |
| **Space** | O(V) (visited + recursion stack) | O(V) (visited + queue + parent) | O(V) (parent + rank) |
| **Input format** | Adjacency list | Adjacency list | Edge list |
| **Stack overflow risk** | Yes (deep recursion) | No | No |
| **Iterative** | Possible but tricky | Naturally iterative | Naturally iterative |
| **Best for** | Standard interview answer | Large graphs, avoid recursion | Edge-based input, incremental |
| **Difficulty** | Medium | Medium | Easy (with template) |

### Recommendation

- **Interview answer**: DFS (cleanest, most intuitive).
- **Production with huge graphs**: BFS or iterative DFS.
- **If edges given as a list**: Union-Find is most natural.

---

## 10. Dry Run with Diagrams

Let me trace through a complete example step by step.

### The Graph

```
   0 ─── 1
   │     │
   │     │
   2 ─── 3
         │
         4 ─── 5

Edges (in adjacency list format):
0: [1, 2]
1: [0, 3]
2: [0, 3]
3: [1, 2, 4]
4: [3, 5]
5: [4]
```

### DFS Trace from Node 0

#### State diagram with annotations:

```
Step 1: Visit 0
   parent[0] = -1
   visited = {0}
   
       [0]*
        │
        1
        │
        2 ─── 3
              │
              4 ─── 5
   
   (* = currently visiting)
```

```
Step 2: Visit 0's neighbor 1
   parent[1] = 0
   visited = {0, 1}
   
       [0] ─── [1]*
        │       │
        │       │
        2       3
              
       4 ─── 5
```

```
Step 3: Visit 1's neighbor 3
   (skip 0 — it's 1's parent)
   parent[3] = 1
   visited = {0, 1, 3}
   
       [0] ─── [1]
        │       │
        │       │
        2 ──── [3]*
                │
              [4] ─── [5]  (not yet visited)
```

```
Step 4: Visit 3's neighbor 2
   (skip 1 — it's 3's parent)
   parent[2] = 3
   visited = {0, 1, 2, 3}
   
       [0] ─── [1]
        │       │
        │       │
       [2]*── [3]
                │
                4 ─── 5
```

```
Step 5: Check 2's neighbors
   Neighbors of 2: {0, 3}
   - 0: visited, parent[2]=3, so 0 ≠ 3 → 0 is NOT parent of 2 → CYCLE! ✓

Cycle found: 0 → 1 → 3 → 2 → 0
```

### What If the Cycle Wasn't There?

Remove edge (0, 2):

```
   0 ─── 1
         │
         │
   2 ─── 3
         │
         4 ─── 5

0: [1]
1: [0, 3]
2: [3]
3: [1, 2, 4]
4: [3, 5]
5: [4]
```

DFS trace:
```
Visit 0, parent=-1
  Visit 1, parent=0
    Visit 3, parent=1
      Visit 2, parent=3
        Neighbors of 2: {3}
        - 3 is parent. Skip.
        Return (no cycle).
      Visit 4, parent=3
        Visit 5, parent=4
          Neighbors of 5: {4}
          - 4 is parent. Skip.
          Return.
        Return.
      Return.
    Return.
  Return.

No cycle found. Return FALSE.
```

### BFS Trace on the Original Graph

Same graph (with cycle):

```
Queue: [0], visited={0}, parent[0]=-1

Process 0:
  Neighbors: {1, 2}
  - 1: not visited. visited[1]=true, parent[1]=0, enqueue
  - 2: not visited. visited[2]=true, parent[2]=0, enqueue
Queue: [1, 2]

Process 1:
  Neighbors: {0, 3}
  - 0: visited, parent[1]=0, IS parent. Skip.
  - 3: not visited. visited[3]=true, parent[3]=1, enqueue
Queue: [2, 3]

Process 2:
  Neighbors: {0, 3}
  - 0: visited, parent[2]=0, IS parent. Skip.
  - 3: visited, parent[2]=0, 3 ≠ 0 → NOT parent!
       CYCLE DETECTED! ✓
```

Notice: BFS detects the cycle faster (at node 2) than DFS does in this example.

### Visualizing the BFS Tree

```
BFS tree from 0:
        0
       / \
      1   2
      |
      3
      |
      4
      |
      5

Edges (1,2 not in tree because 2 was discovered from 0, not from 1).
The edge (1,3) is a tree edge.
The edge (2,3) is a CROSS EDGE — both 2 and 3 are at depth 1+ but in different branches.
A cross edge in BFS on an undirected graph = cycle!
```

---

## 11. Edge Cases

### 1. Empty Graph
```java
V = 0, edges = []
→ No cycle (trivially).
Return FALSE.
```

### 2. Single Node
```java
V = 1, edges = []
→ No cycle.
Return FALSE.
```

### 3. Two Nodes One Edge
```java
V = 2, edges = [(0,1)]
   0 ─── 1
→ No cycle.
Return FALSE.
```

This is the simplest "edge case" where naive cycle detection fails. We go 0 → 1, then check 1's neighbors and see 0 (visited!). Without parent tracking, we'd incorrectly say "cycle". With parent tracking, we correctly say no cycle.

### 4. Self-Loop
```java
V = 2, edges = [(0,0)]
   0 ⟲

Some problem definitions consider this a "cycle of length 1" (TRUE).
Others don't.

Our algorithm: when checking neighbor 0 of node 0, 0 IS visited 
AND 0 IS its own parent only if we set parent[0]=0. 

If the parent is initialized as -1 and current=0, neighbor=0:
  visited[0]=true, neighbor=0, parent[0]=-1
  → 0 ≠ -1 → CYCLE detected.

This MAY or may not be desired. Specify in interview!
```

### 5. Multi-Edge (Parallel Edges)
```java
V = 2, edges = [(0,1), (0,1)]
   0 ══ 1   (two edges between same vertices)

In a simple graph, this isn't allowed.
In a multigraph, two edges between same pair form a cycle of length 2.

In adj list: adj.get(0) = [1, 1], adj.get(1) = [0, 0]

DFS from 0: visit 0, parent=-1
  Neighbor 1 (first): not visited. visit 1, parent=0.
    Neighbors of 1: {0, 0}
    - First 0: visited, parent. Skip.
    - Second 0: visited, parent. Skip.
    Return.
  Neighbor 1 (second): visited! parent[0]=-1, 1≠-1 → CYCLE!

Note: depending on the problem, this may or may not be a "real" cycle.
For a SIMPLE graph (no multi-edges), this case doesn't arise.
```

### 6. Disconnected Graph
```java
V = 6, edges = [(0,1), (1,2), (2,0), (3,4)]
Components: {0,1,2} (cycle), {3,4} (path), {5} (isolated)
```

DFS must run from EVERY unvisited node:
- Start from 0 → finds cycle in {0,1,2}. Return TRUE.

If we didn't have the outer loop:
- Starting only from 0 would still find the cycle.
- But if the cycle was in component {3,4,5,6,7} and we started at 0, we'd miss it.

### 7. Disconnected Without Cycle
```java
V = 5, edges = [(0,1), (2,3)]
   0─1     2─3     4 (isolated)

DFS from 0: no cycle in this component.
DFS from 2: no cycle in this component.
DFS from 4: just one node, no cycle.
Return FALSE.
```

### 8. Complete Graph (K_n)
```java
V = 4, edges = [(0,1), (0,2), (0,3), (1,2), (1,3), (2,3)]
A complete graph on 4 nodes — every pair connected.

This obviously has many cycles. Algorithm should return TRUE quickly.
```

### 9. Star Graph (No Cycle)
```java
V = 5, edges = [(0,1), (0,2), (0,3), (0,4)]
Star centered at 0.

   1   2
    \ /
     0
    / \
   3   4

This is a tree. NO CYCLE.
DFS from 0 visits each leaf. Each leaf's only neighbor (0) is the parent.
Return FALSE.
```

---

## 12. Common Mistakes

### Mistake 1: Forgetting Parent Tracking

```java
// WRONG: simple "visited" check causes false positives!
if (visited[neighbor]) return true;

// RIGHT: check if it's not the parent
if (visited[neighbor] && neighbor != parent) return true;
```

This is THE classic bug. Without parent tracking, even a simple 2-node graph (just one edge) reports a cycle.

### Mistake 2: Not Handling Disconnected Components

```java
// WRONG: only one DFS call
dfs(0, -1, visited, adj);

// RIGHT: loop over all nodes
for (int i = 0; i < V; i++) {
    if (!visited[i]) {
        if (dfs(i, -1, visited, adj)) return true;
    }
}
```

If the graph is disconnected and the cycle is not in the component containing node 0, you'd miss it.

### Mistake 3: Marking Visited AFTER Recursing

```java
// WRONG:
private boolean dfs(int node, int parent, ...) {
    for (int neighbor : adj.get(node)) {
        if (!visited[neighbor]) {
            if (dfs(neighbor, node, ...)) return true;
        }
        // ...
    }
    visited[node] = true;  // ← TOO LATE!
    return false;
}

// RIGHT:
private boolean dfs(int node, int parent, ...) {
    visited[node] = true;  // ← FIRST
    for (int neighbor : adj.get(node)) {
        // ...
    }
    return false;
}
```

If we mark visited after recursing, neighbors will see this node as unvisited and recurse back → infinite loop.

### Mistake 4: Using Same Logic as Directed Graph

```java
// WRONG for undirected — would mark false cycles
// (This is the algorithm for DIRECTED graphs with 3-state coloring)
if (state[neighbor] == 1) return true;  // in-progress means cycle (DIRECTED only!)
```

For undirected, the "in-progress" state isn't enough. You need parent tracking.

### Mistake 5: Not Handling Self-Loops Explicitly

```java
// Edge (v, v) — does adj list have v in adj.get(v)?
// If yes, DFS will see v as neighbor, parent=-1, v ≠ -1 → CYCLE.
// But is this what you want?
```

Self-loops are a definitional ambiguity. Clarify with interviewer.

### Mistake 6: For BFS — Marking Visited When Dequeuing

```java
// WRONG:
queue.offer(start);
while (!queue.isEmpty()) {
    int node = queue.poll();
    if (visited[node]) continue;
    visited[node] = true;
    // ...
}

// This causes the same node to be enqueued multiple times,
// and the parent[] tracking gets confused.

// RIGHT: mark visited when ENQUEUEING
queue.offer(start);
visited[start] = true;
while (!queue.isEmpty()) {
    int node = queue.poll();
    // visited[node] is already true
    for (int neighbor : adj.get(node)) {
        if (!visited[neighbor]) {
            visited[neighbor] = true;  // mark before enqueue
            queue.offer(neighbor);
            parent[neighbor] = node;
        } else if (neighbor != parent[node]) {
            return true;  // CYCLE
        }
    }
}
```

### Mistake 7: Wrong Initial Parent

```java
// WRONG: parent of starting node is 0 by default
int[] parent = new int[V];  // all zeros
// Then for node 0, when we check neighbor 1, neighbor=1, parent[0]=0, 
// they're not equal, so we'd check IS neighbor 1 visited.
// But what if node 0 has neighbor 0? (Self-loop)
// parent[0]=0, neighbor=0, neighbor == parent → SKIP (treat as parent)
// This is WRONG for self-loops.

// RIGHT: initialize parent to -1 (or some impossible value)
int[] parent = new int[V];
Arrays.fill(parent, -1);
```

---

## 13. Complexity Analysis

### DFS Approach

**Time**: O(V + E)
- Each vertex visited exactly once.
- Each edge examined exactly twice (once from each endpoint in undirected graph).

**Space**:
- `visited[]`: O(V).
- Recursion stack: up to O(V) in the worst case (linear graph).
- Adjacency list space: O(V + E).

**Total space**: O(V + E).

### BFS Approach

**Time**: O(V + E) — same as DFS.

**Space**:
- `visited[]`: O(V).
- `parent[]`: O(V).
- Queue: up to O(V).

**Total space**: O(V + E).

### Union-Find Approach

**Time**: O(E · α(V))
- For each edge, we do find and union.
- With path compression + union by rank, each is O(α(V)) where α is inverse Ackermann.
- Effectively O(E) for all practical purposes.

**Space**: O(V) for parent and rank arrays.

### Comparison

```
For V=1000, E=10000:
- DFS/BFS: 11,000 operations.
- Union-Find: ~10,000 operations.

All three are very fast. Difference matters only at extreme scales.
```

---

## 14. Undirected vs Directed Cycle Detection

A common interview follow-up: "What if the graph were directed?"

### Directed Graph Cycle Detection

For directed graphs, the algorithm is different. Use **3 states**:
- **0 = unvisited (white)**
- **1 = in current DFS path (gray)**
- **2 = fully processed (black)**

```java
private boolean hasCycleDirected(int V, List<List<Integer>> adj) {
    int[] state = new int[V];  // 0, 1, 2
    
    for (int i = 0; i < V; i++) {
        if (state[i] == 0 && dfsDirected(i, state, adj)) {
            return true;
        }
    }
    return false;
}

private boolean dfsDirected(int node, int[] state, List<List<Integer>> adj) {
    state[node] = 1;  // mark as in-progress
    
    for (int neighbor : adj.get(node)) {
        if (state[neighbor] == 1) return true;  // back to in-progress = cycle!
        if (state[neighbor] == 0 && dfsDirected(neighbor, state, adj)) return true;
    }
    
    state[node] = 2;  // done
    return false;
}
```

### Key Difference

| Aspect | Undirected | Directed |
|--------|-----------|----------|
| **Cycle condition** | Visited AND not parent | Visited AND in current DFS path |
| **Tracking** | Parent of each node | 3-state coloring |
| **Self-loop** | Ambiguous (depends on definition) | Always a cycle |

### Why Different?

In undirected graphs, the edge `(u, v)` can be traversed in both directions, so we need parent tracking to avoid false positives.

In directed graphs, `u → v` is one-way. Reaching `u` via `v` requires another edge from v back to u (or via more nodes). Just being "visited" isn't enough — we need to know if it's currently in our DFS path (forming a back edge in the DFS tree).

```
Undirected: A ── B  → DFS A → B, neighbor of B is A. A is parent. NOT cycle.

Directed: A → B  → DFS A → B, B has no outgoing edges. Return. No cycle.
Directed: A → B → A → DFS A (state=1) → B (state=1) → A. A is state=1 (in path). CYCLE!
```

---

## 15. Variations and Follow-ups

### Variation 1: Find the Cycle (Not Just Detect)

If a cycle exists, return the vertices forming the cycle.

```java
private boolean dfs(int node, int parent, ...) {
    visited[node] = true;
    parentMap[node] = parent;
    
    for (int neighbor : adj.get(node)) {
        if (!visited[neighbor]) {
            if (dfs(neighbor, node, ...)) return true;
        } else if (neighbor != parent) {
            // Reconstruct cycle by walking back from `node` to `neighbor`
            // using parentMap
            cycleStart = neighbor;
            cycleEnd = node;
            return true;
        }
    }
    return false;
}
```

### Variation 2: Find ALL Cycles (Complex)

This is much harder. Typically use Johnson's algorithm or DFS with backtracking to enumerate.

### Variation 3: Shortest Cycle (Girth)

Find the length of the shortest cycle in the graph.

Approach: For each vertex `v`, BFS from `v` and check if any edge (other than the one used to reach a neighbor) reconnects to `v`'s subtree. Track the shortest such cycle.

### Variation 4: Is the Graph a Tree?

A connected undirected graph is a tree iff:
- It has no cycle.
- It has exactly V-1 edges.

```java
public boolean isTree(int V, List<List<Integer>> adj, int E) {
    if (E != V - 1) return false;
    
    boolean[] visited = new boolean[V];
    if (dfs(0, -1, visited, adj)) return false;  // has cycle
    
    // Check connected
    for (boolean v : visited) {
        if (!v) return false;  // disconnected
    }
    return true;
}
```

### Variation 5: Count Connected Components

While detecting cycles, also count components.

```java
int components = 0;
for (int i = 0; i < V; i++) {
    if (!visited[i]) {
        components++;
        dfs(i, -1, visited, adj);
    }
}
```

### Related Problems (LeetCode)

| Problem | Difficulty | Link |
|---------|-----------|------|
| Graph Valid Tree | Medium | [LC 261](https://leetcode.com/problems/graph-valid-tree/) |
| Redundant Connection | Medium | [LC 684](https://leetcode.com/problems/redundant-connection/) |
| Redundant Connection II | Hard | [LC 685](https://leetcode.com/problems/redundant-connection-ii/) |
| Number of Connected Components | Medium | [LC 323](https://leetcode.com/problems/number-of-connected-components-in-an-undirected-graph/) |
| Critical Connections | Hard | [LC 1192](https://leetcode.com/problems/critical-connections-in-a-network/) |
| Find Eventual Safe States | Medium | [LC 802](https://leetcode.com/problems/find-eventual-safe-states/) |

---

## 16. Complete Java Code

Here's a complete, tested solution with all three approaches:

```java
import java.util.*;

public class CycleDetection {
    
    /**
     * Approach 1: DFS with parent tracking.
     */
    public boolean hasCycleDFS(int V, List<List<Integer>> adj) {
        boolean[] visited = new boolean[V];
        
        for (int i = 0; i < V; i++) {
            if (!visited[i]) {
                if (dfs(i, -1, visited, adj)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean dfs(int node, int parent, boolean[] visited, List<List<Integer>> adj) {
        visited[node] = true;
        
        for (int neighbor : adj.get(node)) {
            if (!visited[neighbor]) {
                if (dfs(neighbor, node, visited, adj)) return true;
            } else if (neighbor != parent) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Approach 2: BFS with parent tracking.
     */
    public boolean hasCycleBFS(int V, List<List<Integer>> adj) {
        boolean[] visited = new boolean[V];
        int[] parent = new int[V];
        Arrays.fill(parent, -1);
        
        for (int start = 0; start < V; start++) {
            if (visited[start]) continue;
            
            Queue<Integer> queue = new LinkedList<>();
            queue.offer(start);
            visited[start] = true;
            
            while (!queue.isEmpty()) {
                int node = queue.poll();
                
                for (int neighbor : adj.get(node)) {
                    if (!visited[neighbor]) {
                        visited[neighbor] = true;
                        parent[neighbor] = node;
                        queue.offer(neighbor);
                    } else if (neighbor != parent[node]) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Approach 3: Union-Find.
     */
    public boolean hasCycleUnionFind(int V, int[][] edges) {
        int[] parent = new int[V];
        int[] rank = new int[V];
        for (int i = 0; i < V; i++) parent[i] = i;
        
        for (int[] edge : edges) {
            int u = edge[0], v = edge[1];
            int pu = find(parent, u);
            int pv = find(parent, v);
            
            if (pu == pv) return true;  // cycle!
            
            // Union by rank
            if (rank[pu] < rank[pv]) parent[pu] = pv;
            else if (rank[pu] > rank[pv]) parent[pv] = pu;
            else { parent[pv] = pu; rank[pu]++; }
        }
        return false;
    }
    
    private int find(int[] parent, int x) {
        if (parent[x] != x) {
            parent[x] = find(parent, parent[x]);
        }
        return parent[x];
    }
    
    // Helper to build adjacency list from edges
    public static List<List<Integer>> buildAdjList(int V, int[][] edges) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < V; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) {
            adj.get(e[0]).add(e[1]);
            adj.get(e[1]).add(e[0]);  // undirected!
        }
        return adj;
    }
    
    public static void main(String[] args) {
        CycleDetection cd = new CycleDetection();
        
        // Test 1: Cycle
        int[][] edges1 = {{0, 1}, {1, 2}, {2, 3}, {3, 0}};
        List<List<Integer>> adj1 = buildAdjList(4, edges1);
        System.out.println("Test 1 DFS: " + cd.hasCycleDFS(4, adj1));    // true
        System.out.println("Test 1 BFS: " + cd.hasCycleBFS(4, adj1));    // true
        System.out.println("Test 1 UF:  " + cd.hasCycleUnionFind(4, edges1));  // true
        
        // Test 2: No cycle (tree)
        int[][] edges2 = {{0, 1}, {1, 2}, {2, 3}};
        List<List<Integer>> adj2 = buildAdjList(4, edges2);
        System.out.println("Test 2 DFS: " + cd.hasCycleDFS(4, adj2));    // false
        System.out.println("Test 2 BFS: " + cd.hasCycleBFS(4, adj2));    // false
        System.out.println("Test 2 UF:  " + cd.hasCycleUnionFind(4, edges2));  // false
        
        // Test 3: Disconnected with cycle
        int[][] edges3 = {{0, 1}, {2, 3}, {3, 4}, {4, 2}};
        List<List<Integer>> adj3 = buildAdjList(5, edges3);
        System.out.println("Test 3 DFS: " + cd.hasCycleDFS(5, adj3));    // true
        System.out.println("Test 3 BFS: " + cd.hasCycleBFS(5, adj3));    // true
        System.out.println("Test 3 UF:  " + cd.hasCycleUnionFind(5, edges3));  // true
    }
}
```

---

## 17. Interview Tips

### How to Approach This Problem

1. **Restate the problem**: "Given an undirected graph, detect if there's any cycle."
2. **Clarify**:
   - "Is it directed or undirected?" (HUGE difference!)
   - "Connected or possibly disconnected?"
   - "Are self-loops considered cycles?"
   - "How is the graph given — adjacency list or edges?"
3. **Mention multiple approaches**: DFS, BFS, Union-Find.
4. **Choose DFS** as primary answer (cleanest for adjacency list).
5. **Highlight the key insight**: parent tracking.
6. **Code it carefully**.
7. **Discuss complexity**.
8. **Test edge cases**: disconnected graph, single node, empty graph.

### Discussion Points to Score Bonus

#### 1. Parent Tracking Explanation
> "The key insight is that in an undirected graph, just checking 'visited' isn't enough — we'd report a cycle even for a simple edge. We need to ignore the edge we came from, hence parent tracking."

#### 2. Handle Disconnected Components
> "I need to loop over all nodes and start DFS from each unvisited one — otherwise I'd miss cycles in components not connected to node 0."

#### 3. Compare with Directed
> "For directed graphs, the algorithm is different — we use 3-state coloring (white/gray/black) instead of parent tracking. That's because directed edges only go one way."

#### 4. Mention Union-Find Alternative
> "Union-Find is elegant when the input is given as an edge list. Process each edge: if the endpoints are already in the same set, we've found a cycle. O(E·α(V)) — nearly linear."

#### 5. Complexity Awareness
> "Both DFS and BFS run in O(V + E). Space is O(V) for the visited array plus O(V) for recursion stack or BFS queue."

### Likely Follow-Up Questions

#### Q: How would you find ALL cycles?
**A**: Much harder. Need to track parents during DFS and reconstruct the cycle when found. Enumerating all cycles is NP-hard for finding all simple cycles in a graph.

#### Q: What if I have edge weights?
**A**: Cycle detection itself doesn't care about weights. But if you want the **shortest cycle**, that's a different problem (use BFS from each vertex).

#### Q: How do you handle self-loops?
**A**: Depends on definition. Usually self-loops count as cycles. In DFS, if `parent = -1` and we encounter `neighbor = node`, since `node != -1`, the algorithm reports cycle. Adjust if your definition excludes self-loops.

#### Q: How does this scale to 10^7 nodes?
**A**: 
- DFS may blow the stack — use iterative DFS or BFS.
- Memory: O(V + E) for adjacency list. 10^7 nodes is feasible if memory available.
- Union-Find scales beautifully.

#### Q: Can you do it without modifying the input?
**A**: Yes — both DFS and BFS only use external `visited[]` and `parent[]`, not modifying the graph.

### Common Interview Mistakes

1. **Forgetting parent tracking** → false positives on every edge.
2. **Using directed graph algorithm** → wrong logic for undirected.
3. **Not handling disconnected components** → miss cycles.
4. **Marking visited after recursion** → infinite loop.
5. **Confusing self-loop handling** without clarifying with interviewer.

---

## TL;DR

### The Mental Model

**For undirected graph cycle detection**:

> "A neighbor that's already visited indicates a cycle ONLY IF it's not the parent — the node we came from. Without that check, every single edge would falsely trigger 'cycle found'."

### The Three Approaches in 30 Seconds

| Approach | Core Idea |
|----------|-----------|
| **DFS** | Recurse with parent tracking; cycle = visited non-parent. |
| **BFS** | Iterate with parent[] array; cycle = visited non-parent. |
| **Union-Find** | For each edge, if endpoints already in same set → cycle. |

### Why Undirected Is Different from Directed

| | Undirected | Directed |
|-|-----------|----------|
| Each edge | Bidirectional | One-way |
| Cycle check | "visited AND not parent" | "in current DFS path" |
| Self-loop | Ambiguous | Definitely a cycle |
| Tracking | Parent of each node | 3-state coloring |

### The Five Key Insights

1. **Parent tracking is mandatory** for undirected graphs.
2. **Loop over all nodes** to handle disconnected components.
3. **Mark visited BEFORE recursing**, not after.
4. **BFS works too** — same idea, different traversal.
5. **Union-Find is most natural** when given edges directly.

### Final Code Snippet (Memorize This)

```java
public boolean hasCycle(int V, List<List<Integer>> adj) {
    boolean[] visited = new boolean[V];
    for (int i = 0; i < V; i++) {
        if (!visited[i] && dfs(i, -1, visited, adj)) return true;
    }
    return false;
}

private boolean dfs(int node, int parent, boolean[] visited, List<List<Integer>> adj) {
    visited[node] = true;
    for (int neighbor : adj.get(node)) {
        if (!visited[neighbor]) {
            if (dfs(neighbor, node, visited, adj)) return true;
        } else if (neighbor != parent) {
            return true;
        }
    }
    return false;
}
```

---

*Master cycle detection in undirected graphs, and you've learned a pattern that recurs in many graph problems: when each edge is bidirectional, you need to remember where you came from. This same insight powers algorithms like finding bridges, articulation points, and biconnected components.*
