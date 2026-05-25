# Cycle Detection in Directed Graph — Complete Deep Dive

A comprehensive guide to detecting cycles in directed graphs using both **DFS** and **BFS (Kahn's Algorithm)** approaches. Includes diagrams, dry runs, common pitfalls, and a comparison with undirected graph cycle detection.

---

## Table of Contents

1. [Problem Statement](#1-problem-statement)
2. [What Is a Cycle in a Directed Graph?](#2-what-is-a-cycle-in-a-directed-graph)
3. [Why Cycle Detection in Directed Graphs Matters](#3-why-cycle-detection-in-directed-graphs-matters)
4. [The Core Insight: Why Directed Is Different](#4-the-core-insight-why-directed-is-different)
5. [Visual Examples of Cycles vs No Cycles](#5-visual-examples-of-cycles-vs-no-cycles)
6. [Approach 1: DFS with 3-State Coloring](#6-approach-1-dfs-with-3-state-coloring)
7. [Approach 2: BFS with Kahn's Algorithm](#7-approach-2-bfs-with-kahns-algorithm)
8. [Edge Types in DFS Tree](#8-edge-types-in-dfs-tree)
9. [Comparing DFS and BFS Approaches](#9-comparing-dfs-and-bfs-approaches)
10. [Dry Run with Diagrams](#10-dry-run-with-diagrams)
11. [Edge Cases](#11-edge-cases)
12. [Common Mistakes](#12-common-mistakes)
13. [Complexity Analysis](#13-complexity-analysis)
14. [Directed vs Undirected Cycle Detection](#14-directed-vs-undirected-cycle-detection)
15. [Variations and Follow-ups](#15-variations-and-follow-ups)
16. [Complete Java Code](#16-complete-java-code)
17. [Interview Tips](#17-interview-tips)

---

## 1. Problem Statement

> Given a **directed graph** with `V` vertices and `E` edges, determine whether the graph contains a **cycle**.
>
> Return `true` if a cycle exists, `false` otherwise.

### Input Format
Typically given as one of:
- Adjacency list: `List<List<Integer>> adj` (where `adj.get(u)` lists nodes `u → ?`).
- Edge list: `int[][] edges` where `edges[i] = [u, v]` means `u → v`.
- Number of vertices `V` + edges.

### Examples

#### Example 1: Has a cycle
```
Vertices: 4
Edges: 0→1, 1→2, 2→3, 3→1

Graph (arrows show direction):
    0 ──▶ 1 ──▶ 2
          ▲     │
          │     ▼
          └──── 3

Output: TRUE
Cycle: 1 → 2 → 3 → 1
```

#### Example 2: No cycle (DAG)
```
Vertices: 4
Edges: 0→1, 0→2, 1→3, 2→3

Graph:
       0
      ╱ ╲
     ▼   ▼
     1   2
      ╲ ╱
       ▼
       3

Output: FALSE  (This is a Directed Acyclic Graph - DAG)
```

#### Example 3: Self-loop
```
Vertices: 2
Edges: 0→0, 0→1

Graph:
    ┌──┐
    ▼  │
    0──┘──▶1

Output: TRUE
Self-loop (0 → 0) is a cycle of length 1.
```

---

## 2. What Is a Cycle in a Directed Graph?

A **cycle** in a directed graph is a sequence of vertices `v0 → v1 → v2 → ... → vk → v0` where:
1. Each edge `vi → vi+1` exists in the graph.
2. The last edge `vk → v0` brings us back to the start.
3. Length can be **1** (self-loop), **2** (mutual pair), **3+** (longer cycles).

### Cycle Length 1 (Self-Loop)

```
Edge: 0 → 0

    ┌───┐
    ▼   │
    0───┘

This IS a cycle of length 1.
```

### Cycle Length 2 (Mutual Edges)

```
Edges: 0 → 1, 1 → 0

    0 ──▶ 1
    ▲     │
    └─────┘

This IS a cycle of length 2.

Note: In undirected, two edges between same vertices isn't a cycle.
But in directed, "A → B" and "B → A" are TWO different edges, and following both creates a cycle.
```

### Cycle Length 3+

```
Edges: 0 → 1, 1 → 2, 2 → 0 (triangle, directed)

       0
      ╱ ▲
     ▼   ╲
     1 ──▶ 2

Cycle: 0 → 1 → 2 → 0  ✓
```

### Critical Distinction from Undirected

In undirected graphs, a single edge `(A, B)` lets you go A→B→A, but this isn't a cycle — it's just traversing the same edge twice.

In directed graphs, `A → B` only goes one way. Going back requires another edge `B → A`. So `A → B → A` IS a cycle (length 2).

---

## 3. Why Cycle Detection in Directed Graphs Matters

### Real-World Applications

#### 1. Detecting Deadlocks
A resource-allocation graph: vertices = processes/resources, directed edges = "waiting for". A cycle here = deadlock.

#### 2. Course Prerequisites
> "Can I complete all courses given prerequisite relationships?"

If A requires B, B requires C, and C requires A → impossible (cycle).

#### 3. Build Systems & Dependency Resolution
Maven, npm, Gradle: if module A depends on B, B on C, C on A → build fails (circular dependency).

#### 4. Compiler — Class Inheritance
Java forbids cyclic inheritance: `class A extends B { } class B extends A { }` is rejected.

#### 5. Spreadsheet Formulas
Excel detects circular references: cell A1 = B1, B1 = A1 → error.

#### 6. Task Scheduling
For any DAG-based scheduling (Airflow, Celery, etc.), cycles must be detected and rejected.

#### 7. Topological Sort Prerequisite
**You CAN'T topologically sort a graph if it has a cycle.** Cycle detection is a prerequisite or byproduct of topological sort.

---

## 4. The Core Insight: Why Directed Is Different

### The Naive (Wrong) Approach

You might think: "Just track visited, and if I revisit a node, it's a cycle."

```java
// WRONG approach
if (visited[neighbor]) return true;
```

**This is wrong** for directed graphs. Why?

### Counter-Example

```
Graph:
   0 ──▶ 1
   │     ▲
   ▼     │
   2 ────┘

Edges: 0→1, 0→2, 2→1

This is a DAG (no cycle). But trace DFS from 0:
1. Visit 0. Mark visited.
2. Go to 1. Mark visited.
3. Return to 0.
4. Go to 2. Mark visited.
5. From 2, neighbor is 1. 1 is visited!
   → Naive algorithm says "CYCLE!" ✗ WRONG.
```

There's no cycle here — node 1 is reachable from BOTH 0 (directly) AND 2 (via 0→2→1). Both paths arriving at the same node doesn't mean cycle.

### The Right Insight

> A cycle exists ONLY IF we encounter a node that is **currently being processed in our DFS path** (an ancestor in the DFS tree).
>
> Simply being "visited" isn't enough — we need to know if it's currently on the **active DFS recursion stack**.

### The Three States

Color each node with one of three states:

| State | Meaning | Color |
|-------|---------|-------|
| **0 = Unvisited (WHITE)** | Never seen yet. | ⬜ |
| **1 = In-Progress (GRAY)** | Currently in DFS path; we entered but haven't finished. | ⬛ |
| **2 = Done (BLACK)** | Fully processed; all descendants explored. | ⚫ |

### The Cycle Condition

> If during DFS we encounter a neighbor with state = **GRAY (in-progress)**, it means we've reached a node that's currently in our active recursion — i.e., it's an ancestor in the DFS tree. This is a **back edge**, which means a **cycle**.

If neighbor is **BLACK (done)**, it means we already finished exploring it. Reaching it again is fine — just a "cross edge" or "forward edge", not a back edge. NOT a cycle.

---

## 5. Visual Examples of Cycles vs No Cycles

### Example A: Triangle Cycle

```
   0 ──▶ 1
   ▲     │
   │     ▼
   └──── 2

Edges: 0→1, 1→2, 2→0

DFS from 0:
- Visit 0 (GRAY). Stack: {0}
- Go to 1. Visit 1 (GRAY). Stack: {0, 1}
- Go to 2. Visit 2 (GRAY). Stack: {0, 1, 2}
- Neighbor of 2: 0. State of 0 = GRAY! → CYCLE! ✓
```

### Example B: Linear DAG (No Cycle)

```
   0 ──▶ 1 ──▶ 2 ──▶ 3

Edges: 0→1, 1→2, 2→3

DFS from 0:
- Visit 0 (GRAY). Recurse on 1.
  - Visit 1 (GRAY). Recurse on 2.
    - Visit 2 (GRAY). Recurse on 3.
      - Visit 3 (GRAY). No neighbors.
      - Mark 3 BLACK.
    - Mark 2 BLACK.
  - Mark 1 BLACK.
- Mark 0 BLACK.

No GRAY neighbor ever encountered. NO CYCLE.
```

### Example C: Diamond DAG (No Cycle)

```
       0
      ╱ ╲
     ▼   ▼
     1   2
      ╲ ╱
       ▼
       3

Edges: 0→1, 0→2, 1→3, 2→3

DFS from 0:
- Visit 0 (GRAY).
  - Visit 1 (GRAY).
    - Visit 3 (GRAY).
      - No outgoing edges.
      - Mark 3 BLACK.
    - Mark 1 BLACK.
  - Visit 2 (GRAY).
    - Neighbor of 2: 3. State of 3 = BLACK.
      - BLACK is fine — already processed. NOT a cycle.
    - Mark 2 BLACK.
- Mark 0 BLACK.

NO CYCLE.
```

This is the key difference from naive "visited check"! Node 3 is visited (BLACK), but it's NOT in the current DFS path, so no cycle.

### Example D: Cycle Across Components

```
   0 ──▶ 1       3 ──▶ 4
   ▲     │       ▲     │
   │     ▼       │     ▼
   └──── 2       6 ──── 5

Two separate components. The first has cycle 0→1→2→0. 
The second has cycle 3→4→5→6→3.

DFS from 0 finds the first cycle. Return TRUE early.

(Or if no cycle in first component, continue outer loop to find cycle in second.)
```

### Example E: Self-Loop

```
    ┌───┐
    ▼   │
    0───┘──▶ 1 ──▶ 2

Edges: 0→0, 0→1, 1→2

DFS from 0:
- Visit 0 (GRAY).
  - Neighbor of 0: 0 (itself!). State of 0 = GRAY → CYCLE!
```

### Example F: Tree-Like Structure (No Cycle)

```
       0
      ╱│╲
     ▼ ▼ ▼
     1 2 3
       │
       ▼
       4

Edges: 0→1, 0→2, 0→3, 2→4

Trace any DFS — no GRAY neighbor ever encountered. NO CYCLE.
```

---

## 6. Approach 1: DFS with 3-State Coloring

### The Algorithm

```
1. Initialize state[] to all 0 (UNVISITED).
2. For each unvisited node, call DFS:
   a. Mark current node as 1 (IN-PROGRESS / GRAY).
   b. For each neighbor:
      - If state[neighbor] == 1 (GRAY): CYCLE FOUND!
      - If state[neighbor] == 0 (WHITE): recurse.
        If recursion finds cycle, propagate.
      - If state[neighbor] == 2 (BLACK): skip (already processed).
   c. Mark current node as 2 (DONE / BLACK).
3. If no cycle found in any component, return false.
```

### State Transitions

```
       Enter DFS(node)
            │
            ▼
       state[node] = 1 (GRAY)
       Explore neighbors
            │
            ▼
       All neighbors done
            │
            ▼
       state[node] = 2 (BLACK)
            │
            ▼
       Return from DFS(node)
```

### Java Code

```java
import java.util.*;

class DirectedCycleDetector {
    
    /**
     * Detects if directed graph has a cycle.
     * @param V Number of vertices
     * @param adj Adjacency list (adj.get(u) = list of nodes u points to)
     * @return true if cycle exists, false otherwise
     */
    public boolean hasCycle(int V, List<List<Integer>> adj) {
        int[] state = new int[V];  // 0=WHITE, 1=GRAY, 2=BLACK
        
        for (int i = 0; i < V; i++) {
            if (state[i] == 0) {
                if (dfs(i, state, adj)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * DFS from node. Returns true if cycle is detected.
     */
    private boolean dfs(int node, int[] state, List<List<Integer>> adj) {
        state[node] = 1;  // GRAY (in-progress)
        
        for (int neighbor : adj.get(node)) {
            if (state[neighbor] == 1) {
                return true;  // GRAY neighbor → back edge → CYCLE!
            }
            if (state[neighbor] == 0) {
                if (dfs(neighbor, state, adj)) {
                    return true;  // cycle in descendants
                }
            }
            // If state[neighbor] == 2 (BLACK), do nothing.
        }
        
        state[node] = 2;  // BLACK (done)
        return false;
    }
}
```

### Line-by-Line Explanation

#### `int[] state = new int[V];`
Default values are 0 (WHITE — unvisited).

#### Outer loop: `for (int i = 0; i < V; i++)`
Handle disconnected components. Each unvisited node could be the start of a new tree in the DFS forest.

#### `state[node] = 1;` (Mark as GRAY)
Critical: do this BEFORE recursing on neighbors. This way, any neighbor that recurses back to this node will see it as GRAY → cycle detected.

#### `if (state[neighbor] == 1) return true;`
**THE key check**: GRAY means the neighbor is an ancestor in the current DFS path. Reaching an ancestor = back edge = cycle.

#### `if (state[neighbor] == 0) { dfs(...) }`
Standard recursion on unvisited neighbors.

#### `state[node] = 2;` (Mark as BLACK)
After all descendants are processed, this node is fully done. Future references to it (from other branches or other DFS roots) will see BLACK and skip.

### Why "Set GRAY Before, BLACK After"?

```java
state[node] = 1;        // GRAY: "I'm currently processing this"
for neighbor:
    explore neighbor    // descendants might come back and see me as GRAY
state[node] = 2;        // BLACK: "I'm done, move along"
```

If we set BLACK directly (skipping GRAY), the algorithm becomes the naive "visited" check — which fails on cases like Example C (diamond DAG).

If we never reset to BLACK, the algorithm might falsely report cycles when revisiting via different paths.

The 3 states give us exactly the discrimination we need.

### Iterative DFS Version

Recursive DFS can stack-overflow on huge graphs. Iterative version using explicit stack:

```java
public boolean hasCycleIterative(int V, List<List<Integer>> adj) {
    int[] state = new int[V];
    
    for (int i = 0; i < V; i++) {
        if (state[i] != 0) continue;
        
        // Stack holds {node, iterator-progress}
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{i, 0});
        state[i] = 1;  // GRAY
        
        while (!stack.isEmpty()) {
            int[] top = stack.peek();
            int node = top[0];
            int idx = top[1];
            
            List<Integer> neighbors = adj.get(node);
            if (idx < neighbors.size()) {
                int neighbor = neighbors.get(idx);
                top[1]++;  // advance iterator
                
                if (state[neighbor] == 1) return true;  // CYCLE!
                if (state[neighbor] == 0) {
                    state[neighbor] = 1;  // GRAY
                    stack.push(new int[]{neighbor, 0});
                }
                // BLACK: skip
            } else {
                // Finished all neighbors of this node
                state[node] = 2;  // BLACK
                stack.pop();
            }
        }
    }
    return false;
}
```

More complex but safe for huge graphs.

---

## 7. Approach 2: BFS with Kahn's Algorithm

### The Big Idea

Kahn's algorithm is primarily a **topological sort** algorithm. But it doubles as cycle detection:

> If we can topologically sort all `V` nodes, the graph has no cycle.
> If we can't (i.e., some nodes are left unprocessed), the graph has a cycle.

### The Algorithm

```
1. Compute in-degree of every node.
2. Add all nodes with in-degree 0 to a queue.
3. Initialize count = 0.
4. While queue is not empty:
   a. Dequeue a node, increment count.
   b. For each neighbor (outgoing edge target), decrement its in-degree.
      If in-degree becomes 0, enqueue it.
5. If count == V, no cycle. Otherwise, cycle exists.
```

### Why It Works

**Intuition**: In a DAG, there must be at least one node with no incoming edges (a "source"). We process it first, then remove its outgoing edges. This may create new sources. Repeat.

If the graph has a cycle, the nodes in the cycle will **never have in-degree 0** (they all point at each other in a loop). They get stuck.

```
   1 ──▶ 2
   ▲     │
   │     ▼
   └──── 3

In-degrees: 1=1, 2=1, 3=1.
No node has in-degree 0. Queue is empty from the start.
count = 0. Since V = 3, count < V → CYCLE.
```

```
   1 ──▶ 2 ──▶ 3

In-degrees: 1=0, 2=1, 3=1.
Queue starts with {1}.

Dequeue 1. count=1. Decrement in-deg of 2 to 0. Enqueue 2.
Dequeue 2. count=2. Decrement in-deg of 3 to 0. Enqueue 3.
Dequeue 3. count=3. No neighbors.

count = 3 = V. NO CYCLE.
```

### Java Code

```java
public boolean hasCycleKahn(int V, List<List<Integer>> adj) {
    int[] inDegree = new int[V];
    
    // Compute in-degrees
    for (int u = 0; u < V; u++) {
        for (int v : adj.get(u)) {
            inDegree[v]++;
        }
    }
    
    // Add all in-degree-0 nodes to queue
    Queue<Integer> queue = new LinkedList<>();
    for (int i = 0; i < V; i++) {
        if (inDegree[i] == 0) {
            queue.offer(i);
        }
    }
    
    int count = 0;
    while (!queue.isEmpty()) {
        int node = queue.poll();
        count++;
        
        for (int neighbor : adj.get(node)) {
            inDegree[neighbor]--;
            if (inDegree[neighbor] == 0) {
                queue.offer(neighbor);
            }
        }
    }
    
    // If we processed fewer than V nodes, some are stuck in cycle
    return count != V;
}
```

### Visualizing Kahn's Algorithm

#### Graph (with cycle):

```
       0
       │
       ▼
   ┌──▶1──▶2
   │   │   │
   3◀──┘   ▼
   ▲       4
   │       │
   └───────┘

Edges: 0→1, 1→2, 1→3, 2→4, 4→3, 3→1
```

#### Step-by-step:

```
Initial:
  In-degrees: 0=0, 1=2 (from 0, 3), 2=1, 3=2 (from 1, 4), 4=1
  Queue: {0}    (only 0 has in-degree 0)
  Count: 0

Step 1: Dequeue 0. Count=1.
  Decrement in-deg of 1 to 1.
  Queue: {}
  
Step 2: Queue empty. STOP.
  Count = 1 ≠ V = 5.
  CYCLE DETECTED! ✓
  
  (Nodes 1, 2, 3, 4 are stuck — they form a cycle: 1→2→4→3→1)
```

#### Graph (DAG, no cycle):

```
       0
      ╱ ╲
     ▼   ▼
     1   2
      ╲ ╱
       ▼
       3

Edges: 0→1, 0→2, 1→3, 2→3
```

#### Step-by-step:

```
Initial:
  In-degrees: 0=0, 1=1, 2=1, 3=2
  Queue: {0}
  Count: 0

Step 1: Dequeue 0. Count=1.
  Decrement in-deg of 1 to 0, enqueue 1.
  Decrement in-deg of 2 to 0, enqueue 2.
  Queue: {1, 2}

Step 2: Dequeue 1. Count=2.
  Decrement in-deg of 3 to 1.
  Queue: {2}

Step 3: Dequeue 2. Count=3.
  Decrement in-deg of 3 to 0, enqueue 3.
  Queue: {3}

Step 4: Dequeue 3. Count=4.
  3 has no outgoing edges.
  Queue: {}

Count = 4 = V. NO CYCLE. ✓
```

### The Topological Order Bonus

While running Kahn's, the order in which nodes are dequeued IS a valid topological order. Cycle detection + topological sort in one algorithm.

For the DAG above: order = [0, 1, 2, 3] (or [0, 2, 1, 3]). Either is valid.

---

## 8. Edge Types in DFS Tree

Understanding DFS edge classification deepens the intuition for cycle detection.

### The 4 Types of Edges (for Directed Graphs)

When you run DFS on a directed graph, every edge falls into one of 4 categories:

```
Original graph:        After DFS from 0, the DFS tree is:
   0──▶1──▶2                  0
   │   │   │                 / \
   ▼   ▼   ▼                1   3 (cross from 3 to 4 in DFS forest)
   3──▶4   5                |
       ▲                    2
       │                    
       └── (forward edge)   

Let's classify all edges:
```

#### 1. Tree Edge
Goes from parent to child in DFS tree. Connects a GRAY node to a WHITE node (about to be visited).

```
Example: 0→1 (tree edge if DFS goes from 0 to 1 directly).
```

#### 2. Back Edge (THE CYCLE INDICATOR!)
Goes from descendant to ancestor in DFS tree. Connects a GRAY node to another GRAY node (which is currently on the recursion stack).

```
Example: 2→0 if 0 is still GRAY when 2 sees it.
This creates the cycle 0 → 1 → 2 → 0.
```

**Back edge = cycle!**

#### 3. Forward Edge
Goes from ancestor to non-direct descendant. Connects a GRAY node to a BLACK node which was discovered after the GRAY one.

```
Example: 0 → 2 directly, while there's also a path 0 → 1 → 2.
When DFS processes 0, after exploring 1 (going through 2), comes back to 0.
0 sees its other neighbor 2 (now BLACK) — that's a forward edge.
```

Forward edge is NOT a cycle indicator.

#### 4. Cross Edge
Goes between unrelated branches. Connects a GRAY node to a BLACK node in a different subtree.

```
Example: In DFS, we explore 1 first (becomes BLACK), then 2.
If 2 → 1, that's a cross edge.
```

Cross edge is NOT a cycle indicator.

### Summary

| Edge Type | Source State | Dest State | Indicates Cycle? |
|-----------|-------------|------------|------------------|
| Tree | GRAY | WHITE | No (tree expansion) |
| Back | GRAY | GRAY | **YES — CYCLE!** |
| Forward | GRAY | BLACK (later descendant) | No |
| Cross | GRAY | BLACK (different subtree) | No |

### Visualizing the 4 Types

```
DFS tree from node 0:                
                                     
       0                              
      ╱│╲                             
     1 2 3       1, 2, 3 are children of 0
    ╱│                                
   4 5         4, 5 are children of 1
                                     
Original directed graph might also have these extra edges:
                                     
- Tree edge:    0→1, 0→2, 0→3, 1→4, 1→5 (the DFS tree itself)
- Back edge:    4→0 (cycle!) or 5→1 (cycle!)
- Forward edge: 0→4 or 0→5 (ancestor to descendant, but not tree)
- Cross edge:   2→4 or 5→3 (between sibling subtrees, but only one direction)
```

### The Key Insight

> In directed DFS, **only back edges indicate cycles**.
> Forward and cross edges DO NOT cause cycles — they're just edges to already-processed nodes.

This is why the 3-state coloring works:
- GRAY = on current stack = potential back edge target.
- BLACK = done = forward/cross edge target (no cycle).

---

## 9. Comparing DFS and BFS Approaches

| Aspect | DFS (3-State) | BFS (Kahn's) |
|--------|--------------|--------------|
| **Time** | O(V + E) | O(V + E) |
| **Space** | O(V) (state[] + recursion stack) | O(V) (in-degree[] + queue) |
| **Bonus output** | Just yes/no | **Also gives topological order** |
| **Stack overflow risk** | Yes (deep recursion) | No |
| **Iterative version** | Possible but tricky | Naturally iterative |
| **Best for** | Just cycle detection | When you also need topo order |
| **Conceptual difficulty** | Medium (3 states) | Easy (in-degree tracking) |

### When to Use Each

**Use DFS when**:
- You want pure cycle detection.
- Graph is small/medium.
- You're more comfortable with DFS.

**Use Kahn's (BFS) when**:
- You also need topological order.
- Graph is huge (avoid stack overflow).
- You want naturally iterative code.

For Course Schedule problems (LC 207 + 210), Kahn's is often preferred because Schedule II requires the order.

---

## 10. Dry Run with Diagrams

Let's trace a moderately complex example through both algorithms.

### The Graph

```
   0 ──▶ 1 ──▶ 2
         │     │
         ▼     ▼
         3 ──▶ 4
               │
               ▼
   5 ──▶ 6 ◀──┘
   ▲           
   │           
   └─── 7

Edges (adjacency list):
0: [1]
1: [2, 3]
2: [4]
3: [4]
4: [6]
5: [6]
6: []
7: [5]

This is a DAG (no cycle).
```

### DFS Trace

```
state = [0,0,0,0,0,0,0,0]   (all WHITE)

Outer loop, i=0: state[0]=0, call dfs(0)
  dfs(0):
    state[0] = 1 (GRAY)
    Neighbors of 0: [1]
    - 1: state=0, call dfs(1)
      dfs(1):
        state[1] = 1 (GRAY)
        Neighbors of 1: [2, 3]
        - 2: state=0, call dfs(2)
          dfs(2):
            state[2] = 1 (GRAY)
            Neighbors of 2: [4]
            - 4: state=0, call dfs(4)
              dfs(4):
                state[4] = 1 (GRAY)
                Neighbors of 4: [6]
                - 6: state=0, call dfs(6)
                  dfs(6):
                    state[6] = 1 (GRAY)
                    Neighbors of 6: []
                    state[6] = 2 (BLACK)
                    return false
                state[4] = 2 (BLACK)
                return false
            state[2] = 2 (BLACK)
            return false
        - 3: state=0, call dfs(3)
          dfs(3):
            state[3] = 1 (GRAY)
            Neighbors of 3: [4]
            - 4: state=2 (BLACK). Skip.
                ↑ This would be a CROSS/FORWARD edge.
                  NOT a cycle.
            state[3] = 2 (BLACK)
            return false
        state[1] = 2 (BLACK)
        return false
    state[0] = 2 (BLACK)
    return false

state = [2,2,2,2,2,0,2,0]   (5 and 7 still WHITE)

Outer loop, i=5: state[5]=0, call dfs(5)
  dfs(5):
    state[5] = 1 (GRAY)
    Neighbors of 5: [6]
    - 6: state=2 (BLACK). Skip.
    state[5] = 2 (BLACK)
    return false

Outer loop, i=7: state[7]=0, call dfs(7)
  dfs(7):
    state[7] = 1 (GRAY)
    Neighbors of 7: [5]
    - 5: state=2 (BLACK). Skip.
    state[7] = 2 (BLACK)
    return false

No cycle found. Return false. ✓
```

### Kahn's BFS Trace

```
Compute in-degrees:
  0: 0 (no incoming)
  1: 1 (from 0)
  2: 1 (from 1)
  3: 1 (from 1)
  4: 2 (from 2, 3)
  5: 1 (from 7)
  6: 2 (from 4, 5)
  7: 0 (no incoming)

Initial queue: nodes with in-deg 0 = {0, 7}
Count: 0

Step 1: Dequeue 0. Count=1.
  For each neighbor [1]:
    - 1: in-deg 1→0. Enqueue.
  Queue: {7, 1}

Step 2: Dequeue 7. Count=2.
  For each neighbor [5]:
    - 5: in-deg 1→0. Enqueue.
  Queue: {1, 5}

Step 3: Dequeue 1. Count=3.
  For each neighbor [2, 3]:
    - 2: in-deg 1→0. Enqueue.
    - 3: in-deg 1→0. Enqueue.
  Queue: {5, 2, 3}

Step 4: Dequeue 5. Count=4.
  For each neighbor [6]:
    - 6: in-deg 2→1.
  Queue: {2, 3}

Step 5: Dequeue 2. Count=5.
  For each neighbor [4]:
    - 4: in-deg 2→1.
  Queue: {3}

Step 6: Dequeue 3. Count=6.
  For each neighbor [4]:
    - 4: in-deg 1→0. Enqueue.
  Queue: {4}

Step 7: Dequeue 4. Count=7.
  For each neighbor [6]:
    - 6: in-deg 1→0. Enqueue.
  Queue: {6}

Step 8: Dequeue 6. Count=8.
  No neighbors.
  Queue: {}

Final count: 8 = V. NO CYCLE. ✓
Topological order found: [0, 7, 1, 5, 2, 3, 4, 6]
```

### Now Add a Cycle Edge

Add edge `4 → 1`:

```
   0 ──▶ 1 ──▶ 2
         ▲     │
         │     ▼
         3 ──▶ 4
               │
               └── (back to 1)

Edges: 1→2, 2→4, 4→1 form a cycle.
```

#### DFS Trace (with cycle)

```
dfs(0):
  state[0] = 1
  dfs(1):
    state[1] = 1
    dfs(2):
      state[2] = 1
      dfs(4):
        state[4] = 1
        Neighbors of 4: [6, 1]    (4→1 added)
        - 6: state=0, dfs(6)
          state[6] = 1 → 2
        - 1: state = 1 (GRAY!) → CYCLE! ✓
        return true
      return true
    return true
  return true
```

Cycle detected at the step where 4 sees 1 still being GRAY.

#### Kahn's Trace (with cycle)

```
In-degrees:
  0: 0
  1: 2 (from 0, 4)
  2: 1 (from 1)
  3: 1 (from 1)
  4: 2 (from 2, 3)
  5: 1, 6: 2, 7: 0

Queue: {0, 7}

Dequeue 0. Count=1.
  1: in-deg 2→1.
Queue: {7}

Dequeue 7. Count=2.
  5: in-deg 1→0.
Queue: {5}

Dequeue 5. Count=3.
  6: in-deg 2→1.
Queue: {}

Done. Count=3 ≠ V=8. CYCLE! ✓
```

Notice how Kahn's stops processing because nodes 1, 2, 3, 4, 6 are stuck in (or downstream of) the cycle.

---

## 11. Edge Cases

### 1. Empty Graph
```java
V = 0, edges = []
→ count = 0 = V. NO CYCLE.
→ DFS does nothing. NO CYCLE.
Return FALSE.
```

### 2. Single Node, No Edges
```java
V = 1, edges = []
→ Kahn's: in-degree 0, queue starts {0}. Count=1=V. NO CYCLE.
→ DFS: state[0] = 1, no neighbors, state[0] = 2. NO CYCLE.
```

### 3. Single Node with Self-Loop
```java
V = 1, edges = [(0,0)]
   ┌──┐
   ▼  │
   0──┘

→ DFS: state[0]=1, neighbor is 0 (state=1, GRAY!) → CYCLE.
→ Kahn's: in-deg[0]=1. Queue is empty initially. Count=0 ≠ V=1 → CYCLE.

Both report cycle. ✓
```

### 4. Two-Node Mutual Loop
```java
V = 2, edges = [(0,1), (1,0)]
   0 ──▶ 1
   ▲     │
   └─────┘

→ DFS: 0 GRAY → 1 GRAY → 0 still GRAY → CYCLE.
→ Kahn's: in-degrees both 1. Empty queue. count=0 ≠ V. CYCLE.
```

### 5. Simple Path (No Cycle)
```java
V = 3, edges = [(0,1), (1,2)]
   0 ──▶ 1 ──▶ 2

→ NO CYCLE.
```

### 6. Diamond DAG (Tests the BLACK-skip Logic)
```java
V = 4, edges = [(0,1), (0,2), (1,3), (2,3)]
       0
      ╱ ╲
     ▼   ▼
     1   2
      ╲ ╱
       ▼
       3

→ DFS visits 3 twice (once from 1, then from 2), but second time 3 is BLACK → SKIP.
NO CYCLE.
```

This case is what trips up the naive "if visited, cycle" approach.

### 7. Disconnected with Cycle in One Component
```java
V = 6, edges = [(0,1), (1,2), (2,0), (3,4)]

   0 ──▶ 1      3 ──▶ 4
   ▲     │              
   └──── 2              

Component 1: cycle.
Component 2: edge (no cycle).
Vertex 5: isolated.

→ DFS from 0 finds cycle. Return TRUE.
→ Kahn's: in-deg [0,1,1, 0,1, 0]. Queue: {0, 3, 5}.
  Processing dequeues 3 nodes. Count=3.
  3 → 4: in-deg 0. Dequeue 4. Count=4.
  Other nodes (0, 1, 2) stuck because in-deg never reaches 0. Cycle.
```

### 8. Multi-Edges (Two edges from u to v)
```java
V = 2, edges = [(0,1), (0,1)]

In adjacency list: adj.get(0) = [1, 1]

DFS: visit 0 GRAY → visit 1 (first edge) GRAY → BLACK. 
Back at 0, second edge: 1 BLACK → skip. NO CYCLE.

Kahn's: in-deg[1] = 2 (two edges incoming).
  Process 0: in-deg[1] = 2 → 1, then → 0.
  Process 1.
  Count = 2 = V. NO CYCLE.
```

Multi-edges don't create cycles in directed graphs.

### 9. Large Linear Chain
```java
V = 10^5, edges = (0,1), (1,2), ..., (n-2, n-1)

→ NO CYCLE.
→ Recursive DFS might stack overflow on huge chains.
→ Kahn's handles it fine.
```

---

## 12. Common Mistakes

### Mistake 1: Using Parent Tracking (Like Undirected)

```java
// WRONG for directed:
private boolean dfs(int node, int parent, boolean[] visited, ...) {
    visited[node] = true;
    for (int neighbor : adj.get(node)) {
        if (!visited[neighbor]) {
            if (dfs(neighbor, node, ...)) return true;
        } else if (neighbor != parent) {  // ← wrong condition for directed!
            return true;
        }
    }
    return false;
}
```

This is the UNDIRECTED algorithm. For DIRECTED, we need 3-state coloring, not parent tracking.

**Why**: in a DAG like `0 → 1, 0 → 2, 1 → 2`, when 0 visits 2 (after recursing through 1), 2 is visited but not 0's "parent" → false positive.

### Mistake 2: Using Boolean Visited (Only 2 States)

```java
// WRONG for directed:
if (visited[neighbor]) return true;  // ← false positive on diamond DAG
```

You need 3 states (or equivalent): unvisited, in-progress, done.

### Mistake 3: Not Resetting State After Recursion

```java
// WRONG: state stays at 1 (GRAY) even after fully processed
private boolean dfs(int node, int[] state, ...) {
    state[node] = 1;
    for (int neighbor : ...) {
        // ...
    }
    // BUG: forgot state[node] = 2
    return false;
}
```

Without setting state to 2 (BLACK) at the end, subsequent DFS calls would see this node as GRAY → false cycle reports.

### Mistake 4: Mixing Up the Cycle Condition

```java
// WRONG conditions:
if (state[neighbor] == 0) return true;    // wrong! 0 means unvisited.
if (state[neighbor] == 2) return true;    // wrong! BLACK means done (no cycle).

// RIGHT:
if (state[neighbor] == 1) return true;    // GRAY means in current DFS path → cycle.
```

### Mistake 5: For Kahn's — Forgetting In-degree Decrement

```java
// WRONG: just count enqueues
while (!queue.isEmpty()) {
    int node = queue.poll();
    count++;
    for (int neighbor : adj.get(node)) {
        queue.offer(neighbor);  // ← wrong! doesn't track in-degree
    }
}
```

You must decrement in-degree and only enqueue when it reaches 0.

### Mistake 6: For Kahn's — Forgetting Initial Queue Population

```java
// WRONG: queue starts empty
Queue<Integer> queue = new LinkedList<>();
// Forgot to add nodes with in-degree 0!

while (!queue.isEmpty()) { ... }  // never executes
```

You'd report cycle for every graph because count=0 < V.

### Mistake 7: Not Handling Disconnected Components in DFS

```java
// WRONG: only DFS from node 0
dfs(0, state, adj);
return state contains 1 somewhere; // wrong logic too

// RIGHT: outer loop
for (int i = 0; i < V; i++) {
    if (state[i] == 0 && dfs(i, state, adj)) return true;
}
```

If the cycle is in a component not reachable from node 0, you'd miss it.

### Mistake 8: Confusing in-degree with out-degree in Kahn's

```java
// WRONG: counting outgoing edges as in-degree
for (int u = 0; u < V; u++) {
    inDegree[u] = adj.get(u).size();  // ← this is out-degree
}

// RIGHT:
for (int u = 0; u < V; u++) {
    for (int v : adj.get(u)) {
        inDegree[v]++;  // increment v's in-degree for each edge u→v
    }
}
```

This is a very common mix-up.

---

## 13. Complexity Analysis

### DFS Approach

**Time**: O(V + E)
- Each vertex's state changes from 0 → 1 → 2 (constant work).
- Each edge examined exactly once (from its source).

**Space**:
- `state[]`: O(V).
- Recursion stack: O(V) worst case (linear chain).
- Adjacency list: O(V + E).

**Total**: O(V + E).

### Kahn's BFS Approach

**Time**: O(V + E)
- Each vertex enqueued and dequeued at most once.
- Each edge processed once when decrementing in-degree.

**Space**:
- `inDegree[]`: O(V).
- Queue: up to O(V).
- Adjacency list: O(V + E).

**Total**: O(V + E).

### Comparison

```
For V = 10^5, E = 10^5:
- Both: ~200,000 operations.
- DFS: stack space up to 100,000 (potential overflow without -Xss tuning).
- Kahn's: no stack issues.
```

Performance is essentially identical. **Choose based on:**
- Stack safety concerns? → Kahn's.
- Need topological order too? → Kahn's.
- Just yes/no, prefer recursion? → DFS.

---

## 14. Directed vs Undirected Cycle Detection

This is a critical interview comparison.

### Algorithmic Difference

| Aspect | Undirected | Directed |
|--------|-----------|----------|
| **Cycle condition** | Visited AND not parent | Visited AND in current DFS path (GRAY) |
| **Tracking mechanism** | Parent of each node | 3-state coloring (or stack membership) |
| **Self-loop** | Often excluded (definitional) | Always a cycle (length 1) |
| **2-edge cycle** | Not possible (single edge isn't a cycle) | Possible: u→v and v→u |
| **Length** | Always ≥ 3 | Can be 1, 2, 3, ... |
| **Union-Find applicable?** | Yes — works great | No — UF doesn't capture direction |
| **Kahn's algorithm applicable?** | No — needs directed structure | Yes — topological sort |

### Why the Difference?

**Undirected**: edge `(u, v)` is symmetric. Going u→v→u uses the SAME edge twice. We need parent tracking to know we just came from there.

**Directed**: edges `u→v` and `v→u` are DIFFERENT edges. If both exist, that's a real cycle (length 2). No special "parent edge" exists — just back edges in the DFS tree.

### Code Side-by-Side

**Undirected**:
```java
private boolean dfs(int node, int parent, boolean[] visited, ...) {
    visited[node] = true;
    for (int neighbor : adj.get(node)) {
        if (!visited[neighbor]) {
            if (dfs(neighbor, node, ...)) return true;
        } else if (neighbor != parent) {
            return true;  // cycle
        }
    }
    return false;
}
```

**Directed**:
```java
private boolean dfs(int node, int[] state, ...) {
    state[node] = 1;  // GRAY
    for (int neighbor : adj.get(node)) {
        if (state[neighbor] == 1) return true;  // back edge → cycle
        if (state[neighbor] == 0 && dfs(neighbor, ...)) return true;
    }
    state[node] = 2;  // BLACK
    return false;
}
```

### Mistakes from Confusing the Two

1. Using parent tracking on directed graph: misses 2-edge cycles like u→v→u.
2. Using 3-state on undirected: would falsely report cycles for simple edges (the second visit is just walking the same edge back).
3. Trying Union-Find on directed: ignores direction, may report false cycles.
4. Trying Kahn's on undirected: nodes have "degree" not "in-degree" — algorithm doesn't apply.

---

## 15. Variations and Follow-ups

### Variation 1: Topological Sort

If no cycle, output a topological order. Use Kahn's directly — the dequeue order IS the topological order.

```java
public int[] topologicalSort(int V, List<List<Integer>> adj) {
    int[] inDeg = new int[V];
    for (int u = 0; u < V; u++)
        for (int v : adj.get(u)) inDeg[v]++;
    
    Queue<Integer> queue = new LinkedList<>();
    for (int i = 0; i < V; i++) if (inDeg[i] == 0) queue.offer(i);
    
    int[] order = new int[V];
    int idx = 0;
    while (!queue.isEmpty()) {
        int node = queue.poll();
        order[idx++] = node;
        for (int v : adj.get(node)) {
            if (--inDeg[v] == 0) queue.offer(v);
        }
    }
    return idx == V ? order : new int[0];  // empty if cycle
}
```

### Variation 2: Find the Cycle (Reconstruct)

Track parent during DFS. When you encounter GRAY neighbor, trace back from current to that neighbor.

```java
private List<Integer> cycle = null;
private int[] state;
private int[] parent;

private boolean dfs(int node, List<List<Integer>> adj) {
    state[node] = 1;
    for (int neighbor : adj.get(node)) {
        if (state[neighbor] == 1) {
            // Cycle found! Reconstruct it.
            cycle = new ArrayList<>();
            int curr = node;
            while (curr != neighbor) {
                cycle.add(curr);
                curr = parent[curr];
            }
            cycle.add(neighbor);
            Collections.reverse(cycle);
            return true;
        }
        if (state[neighbor] == 0) {
            parent[neighbor] = node;
            if (dfs(neighbor, adj)) return true;
        }
    }
    state[node] = 2;
    return false;
}
```

### Variation 3: Find All Cycles

Harder. Use Johnson's algorithm or DFS variants. Generally NP-hard if you want all simple cycles.

### Variation 4: Shortest Cycle

For each vertex, BFS from it and check when you revisit it. Track minimum.

### Variation 5: Course Schedule Family

| LC | Problem | Approach |
|----|---------|----------|
| [LC 207](https://leetcode.com/problems/course-schedule/) | Can finish all courses? | Pure cycle detection. |
| [LC 210](https://leetcode.com/problems/course-schedule-ii/) | Output order. | Kahn's algorithm. |
| [LC 1136](https://leetcode.com/problems/parallel-courses/) | Min semesters. | Topo sort with levels. |
| [LC 269](https://leetcode.com/problems/alien-dictionary/) | Derive alphabet order. | Build graph + topo sort. |
| [LC 802](https://leetcode.com/problems/find-eventual-safe-states/) | Find safe nodes (no cycle from them). | Reverse + topo sort. |
| [LC 1591](https://leetcode.com/problems/strange-printer-ii/) | Print order. | Dependency cycle check. |

### Variation 6: Detecting Strongly Connected Components (SCC)

Tarjan's or Kosaraju's algorithm. Each SCC of size > 1 contains a cycle.

---

## 16. Complete Java Code

Here's a complete, tested solution with both approaches:

```java
import java.util.*;

public class DirectedCycleDetection {
    
    /**
     * Approach 1: DFS with 3-state coloring.
     */
    public boolean hasCycleDFS(int V, List<List<Integer>> adj) {
        int[] state = new int[V];  // 0=WHITE, 1=GRAY, 2=BLACK
        
        for (int i = 0; i < V; i++) {
            if (state[i] == 0) {
                if (dfs(i, state, adj)) return true;
            }
        }
        return false;
    }
    
    private boolean dfs(int node, int[] state, List<List<Integer>> adj) {
        state[node] = 1;  // GRAY
        
        for (int neighbor : adj.get(node)) {
            if (state[neighbor] == 1) return true;  // back edge → CYCLE
            if (state[neighbor] == 0 && dfs(neighbor, state, adj)) return true;
        }
        
        state[node] = 2;  // BLACK
        return false;
    }
    
    /**
     * Approach 2: BFS with Kahn's Algorithm (also gives topo order).
     */
    public boolean hasCycleKahn(int V, List<List<Integer>> adj) {
        int[] inDeg = new int[V];
        
        // Compute in-degrees
        for (int u = 0; u < V; u++) {
            for (int v : adj.get(u)) {
                inDeg[v]++;
            }
        }
        
        // Initialize queue with in-degree-0 nodes
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < V; i++) {
            if (inDeg[i] == 0) queue.offer(i);
        }
        
        int count = 0;
        while (!queue.isEmpty()) {
            int node = queue.poll();
            count++;
            
            for (int neighbor : adj.get(node)) {
                inDeg[neighbor]--;
                if (inDeg[neighbor] == 0) queue.offer(neighbor);
            }
        }
        
        return count != V;  // if not all nodes processed, there's a cycle
    }
    
    // Helper: build adjacency list from directed edges
    public static List<List<Integer>> buildAdjList(int V, int[][] edges) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < V; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) {
            adj.get(e[0]).add(e[1]);  // u → v
        }
        return adj;
    }
    
    public static void main(String[] args) {
        DirectedCycleDetection cd = new DirectedCycleDetection();
        
        // Test 1: Cycle
        int[][] e1 = {{0,1},{1,2},{2,3},{3,1}};
        List<List<Integer>> adj1 = buildAdjList(4, e1);
        System.out.println("Test 1 DFS:   " + cd.hasCycleDFS(4, adj1));   // true
        System.out.println("Test 1 Kahn:  " + cd.hasCycleKahn(4, adj1));  // true
        
        // Test 2: DAG (no cycle)
        int[][] e2 = {{0,1},{0,2},{1,3},{2,3}};
        List<List<Integer>> adj2 = buildAdjList(4, e2);
        System.out.println("Test 2 DFS:   " + cd.hasCycleDFS(4, adj2));   // false
        System.out.println("Test 2 Kahn:  " + cd.hasCycleKahn(4, adj2));  // false
        
        // Test 3: Self-loop
        int[][] e3 = {{0,0}};
        List<List<Integer>> adj3 = buildAdjList(1, e3);
        System.out.println("Test 3 DFS:   " + cd.hasCycleDFS(1, adj3));   // true
        System.out.println("Test 3 Kahn:  " + cd.hasCycleKahn(1, adj3));  // true
        
        // Test 4: Two-node cycle
        int[][] e4 = {{0,1},{1,0}};
        List<List<Integer>> adj4 = buildAdjList(2, e4);
        System.out.println("Test 4 DFS:   " + cd.hasCycleDFS(2, adj4));   // true
        System.out.println("Test 4 Kahn:  " + cd.hasCycleKahn(2, adj4));  // true
        
        // Test 5: Disconnected with cycle
        int[][] e5 = {{0,1},{1,0},{2,3}};
        List<List<Integer>> adj5 = buildAdjList(4, e5);
        System.out.println("Test 5 DFS:   " + cd.hasCycleDFS(4, adj5));   // true
        System.out.println("Test 5 Kahn:  " + cd.hasCycleKahn(4, adj5));  // true
        
        // Test 6: Empty graph
        int[][] e6 = {};
        List<List<Integer>> adj6 = buildAdjList(5, e6);
        System.out.println("Test 6 DFS:   " + cd.hasCycleDFS(5, adj6));   // false
        System.out.println("Test 6 Kahn:  " + cd.hasCycleKahn(5, adj6));  // false
    }
}
```

---

## 17. Interview Tips

### How to Approach This Problem

1. **Restate the problem**: "Given a directed graph, detect any cycle."
2. **Clarify**:
   - "How is the graph given — adjacency list or edges?"
   - "Are self-loops counted as cycles?"
   - "Are there multi-edges?"
   - "Could it be disconnected?"
3. **Mention both approaches**: DFS with 3-state + Kahn's BFS.
4. **Pick one as primary** (usually DFS for conciseness; Kahn's if you also need topo order).
5. **Highlight the key insight**: GRAY = in-progress = back edge target.
6. **Code carefully**.
7. **Test edge cases**: self-loop, two-node cycle, disconnected.

### Discussion Points to Score Bonus

#### 1. The 3-State Coloring Explanation
> "I'll use 3-state coloring: WHITE (unvisited), GRAY (in current DFS path), BLACK (fully processed). A back edge — encountering a GRAY neighbor — means we've found a cycle. The naive 2-state visited check fails because a node visited via a different DFS path doesn't necessarily indicate a cycle."

#### 2. Why Not Use Parent Tracking?
> "Parent tracking works for undirected graphs because each edge is bidirectional. For directed graphs, an edge from u to v is different from v to u. We need to know if a node is currently on the DFS recursion stack, hence 3-state coloring."

#### 3. The Kahn's Algorithm Connection
> "Kahn's algorithm is primarily a topological sort, but it doubles as cycle detection. If we can topologically sort all V nodes, the graph is a DAG. If we can't, there's a cycle. This is a 2-for-1 — useful when the problem also wants topo order."

#### 4. Edge Types Insight
> "In directed DFS, we have 4 edge types: tree, back, forward, cross. Only **back edges** (GRAY → GRAY) indicate cycles. Forward/cross edges (GRAY → BLACK) don't."

#### 5. Comparison with Undirected
> "For undirected graphs, the algorithm is different — we use parent tracking instead of 3-state. The reason is that edges in undirected graphs are bidirectional, so encountering a visited node could just mean we're walking the same edge backward."

#### 6. Complexity
> "Both DFS and Kahn's run in O(V + E). Space is O(V) for state/in-degree array plus O(V) for recursion stack or queue."

### Likely Follow-Up Questions

#### Q: What if I have weighted edges?
**A**: Cycle detection doesn't care about weights. But if you want shortest cycle (smallest sum of weights), use Floyd-Warshall — check `dist[i][i]` for negative or shortest values.

#### Q: How do you find the actual cycle, not just detect?
**A**: Track parent during DFS. When you find a GRAY neighbor, walk back from current node to that neighbor using parent pointers.

#### Q: Can you do this without recursion?
**A**: Yes — iterative DFS with explicit stack. More complex to implement correctly. Kahn's is naturally iterative.

#### Q: What if graph is huge (V = 10^7)?
**A**: Recursive DFS may stack overflow. Use Kahn's or iterative DFS. Memory is the limit then.

#### Q: How would you detect ALL cycles?
**A**: Much harder. NP-hard in general for all simple cycles. Use Johnson's algorithm for elementary cycles, or SCC decomposition (Tarjan's) for strongly connected components (which contain cycles).

#### Q: What's the difference from undirected cycle detection?
**A**: See Section 14 for the full comparison. Key: 3-state vs parent-tracking; back edge vs visited-not-parent.

#### Q: Course Schedule II (LC 210) — modify approach?
**A**: Use Kahn's. The order of dequeues gives the answer (the topological order = valid course completion order).

### Common Interview Mistakes

1. Using parent tracking (undirected algorithm) for directed graph.
2. Using boolean visited (only 2 states) — fails on diamond DAG.
3. Forgetting to mark BLACK after finishing → false cycle reports.
4. Forgetting outer loop for disconnected components.
5. Mixing up in-degree vs out-degree in Kahn's.
6. Forgetting to initialize Kahn's queue with all in-degree-0 nodes.

---

## TL;DR

### The Mental Model

**For directed graph cycle detection**:

> "A cycle exists if during DFS, we encounter a node that is **currently being processed** (on the active recursion stack). This is called a back edge, and it's the only edge type that indicates a cycle."

### The Two Approaches in 30 Seconds

| Approach | Core Idea | Bonus |
|----------|-----------|-------|
| **DFS (3-state)** | WHITE → GRAY (entering) → BLACK (leaving). GRAY neighbor = cycle. | None |
| **Kahn's BFS** | Process nodes in topo order (in-deg=0 first). If stuck, cycle. | Gives topological order! |

### The Three States Explained

```
WHITE (0):  Not yet visited.
GRAY  (1):  Currently in DFS recursion (on stack).
BLACK (2):  Fully processed (recursion returned).

Cycle = encountering a GRAY neighbor.
```

### Why Directed Is Different from Undirected

| | Undirected | Directed |
|-|-----------|----------|
| Edge | Bidirectional | One-way |
| Cycle check | "visited AND not parent" | "currently on recursion stack" |
| Self-loop | Often excluded | Always a cycle |
| 2-edge cycle (u↔v) | Not possible | Possible: u→v + v→u |
| Mechanism | Parent tracking | 3-state coloring |

### The Five Key Insights

1. **3-state coloring is mandatory** for directed cycle detection. 2 states isn't enough.
2. **Only back edges cause cycles** — forward/cross edges don't.
3. **Kahn's algorithm is 2-for-1**: cycle detection + topological sort.
4. **Self-loops and 2-edge cycles** are valid cycles in directed graphs.
5. **Always loop over all nodes** to handle disconnected components.

### Final Code Snippet (Memorize This)

```java
public boolean hasCycle(int V, List<List<Integer>> adj) {
    int[] state = new int[V];
    for (int i = 0; i < V; i++) {
        if (state[i] == 0 && dfs(i, state, adj)) return true;
    }
    return false;
}

private boolean dfs(int node, int[] state, List<List<Integer>> adj) {
    state[node] = 1;  // GRAY
    for (int neighbor : adj.get(node)) {
        if (state[neighbor] == 1) return true;
        if (state[neighbor] == 0 && dfs(neighbor, state, adj)) return true;
    }
    state[node] = 2;  // BLACK
    return false;
}
```

### Kahn's BFS Version (Bonus for Topo Order)

```java
public boolean hasCycleKahn(int V, List<List<Integer>> adj) {
    int[] inDeg = new int[V];
    for (int u = 0; u < V; u++)
        for (int v : adj.get(u)) inDeg[v]++;
    
    Queue<Integer> queue = new LinkedList<>();
    for (int i = 0; i < V; i++) if (inDeg[i] == 0) queue.offer(i);
    
    int count = 0;
    while (!queue.isEmpty()) {
        int node = queue.poll();
        count++;
        for (int v : adj.get(node)) {
            if (--inDeg[v] == 0) queue.offer(v);
        }
    }
    return count != V;
}
```

---

*Master cycle detection in directed graphs and you've also mastered topological sort. These two go hand-in-hand and unlock a whole family of problems: course scheduling, build systems, dependency resolution, compiler design, and many real-world planning problems.*
