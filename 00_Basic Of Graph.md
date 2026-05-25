# Graph Algorithms — Complete Interview Guide

A comprehensive guide to graph algorithms, patterns, and problems organized by interview tier. From basics to advanced techniques, with LeetCode links and the company tier each problem typically appears at.

---

## Table of Contents

1. [Graph Fundamentals](#1-graph-fundamentals)
2. [Graph Representations](#2-graph-representations)
3. [How to Approach Any Graph Problem](#3-how-to-approach-any-graph-problem)
4. [Company Tier Definitions](#4-company-tier-definitions)
5. [Pattern 1: Grid Traversal (DFS/BFS)](#5-pattern-1-grid-traversal-dfsbfs)
6. [Pattern 2: BFS / Shortest Path in Unweighted Graph](#6-pattern-2-bfs--shortest-path-in-unweighted-graph)
7. [Pattern 3: DFS Traversal (Generic Graph)](#7-pattern-3-dfs-traversal-generic-graph)
8. [Pattern 4: Cycle Detection](#8-pattern-4-cycle-detection)
9. [Pattern 5: Topological Sort](#9-pattern-5-topological-sort)
10. [Pattern 6: Union-Find (Disjoint Set Union)](#10-pattern-6-union-find-disjoint-set-union)
11. [Pattern 7: Shortest Path Algorithms (Weighted)](#11-pattern-7-shortest-path-algorithms-weighted)
12. [Pattern 8: Minimum Spanning Tree](#12-pattern-8-minimum-spanning-tree)
13. [Pattern 9: Strongly Connected Components](#13-pattern-9-strongly-connected-components)
14. [Pattern 10: Bipartite Graph / Graph Coloring](#14-pattern-10-bipartite-graph--graph-coloring)
15. [Pattern 11: Multi-Source BFS](#15-pattern-11-multi-source-bfs)
16. [Pattern 12: Backtracking on Graphs](#16-pattern-12-backtracking-on-graphs)
17. [Pattern 13: Advanced Topics](#17-pattern-13-advanced-topics)
18. [Suggested Study Order](#18-suggested-study-order)
19. [Templates and Cheat Sheets](#19-templates-and-cheat-sheets)
20. [Interview Strategy](#20-interview-strategy)

---

## 1. Graph Fundamentals

### What Is a Graph?

A graph `G = (V, E)` consists of:
- **V**: set of vertices (nodes).
- **E**: set of edges (connections between vertices).

### Types of Graphs

| Type | Description | Example |
|------|-------------|---------|
| **Directed** | Edges have direction (one-way) | Twitter follows, course prerequisites |
| **Undirected** | Edges are bidirectional | Facebook friends, road networks |
| **Weighted** | Edges carry values | Distances, costs |
| **Unweighted** | All edges treated equally | Social connections |
| **Cyclic** | Contains cycles | Network with loops |
| **Acyclic (DAG)** | No cycles | Dependency graph |
| **Connected** | Path exists between every pair | Single landmass |
| **Disconnected** | Has isolated components | Multiple islands |
| **Tree** | Connected, no cycles, V-1 edges | File system hierarchy |
| **Bipartite** | Vertices split into 2 sets, edges only between sets | Matching, scheduling |
| **Sparse** | Few edges relative to vertices | Most real-world graphs |
| **Dense** | Many edges, close to V² | Complete graphs, cliques |

### Key Terminology

- **Adjacent**: two nodes connected by an edge.
- **Degree**: number of edges at a node (in-degree, out-degree for directed).
- **Path**: sequence of vertices connected by edges.
- **Cycle**: path where start = end.
- **Connected component**: maximal set of mutually reachable vertices.
- **Strongly connected** (directed): every pair reachable in both directions.
- **Spanning tree**: subgraph that includes all vertices but no cycles.
- **DAG**: Directed Acyclic Graph (no cycles, has direction).

### Where Graphs Show Up

- **Social networks**: friend recommendations, influencer analysis.
- **Maps/Navigation**: shortest route, traffic.
- **Course planning**: prerequisites (topological sort).
- **Dependency management**: build systems, package installation.
- **Network routing**: internet packets, telephony.
- **Game AI**: pathfinding, decision trees.
- **Web crawling**: links between pages.
- **Computer biology**: protein networks, gene interactions.

---

## 2. Graph Representations

### Adjacency Matrix
A V×V 2D array where `matrix[i][j] = 1` if edge exists (or weight for weighted graphs).

```java
int[][] matrix = new int[V][V];
matrix[1][2] = 1;  // edge from 1 to 2
```

**Pros**: O(1) edge lookup, simple.
**Cons**: O(V²) space — wasteful for sparse graphs.

**When to use**: small V (≤ 1000), dense graphs.

### Adjacency List
Each vertex has a list of its neighbors.

```java
// Most common in interviews
Map<Integer, List<Integer>> graph = new HashMap<>();
graph.computeIfAbsent(1, k -> new ArrayList<>()).add(2);

// Or with arrays:
List<List<Integer>> graph = new ArrayList<>();
for (int i = 0; i < V; i++) graph.add(new ArrayList<>());
graph.get(1).add(2);  // edge from 1 to 2

// For weighted:
List<List<int[]>> graph = new ArrayList<>();  // [neighbor, weight]
graph.get(1).add(new int[]{2, 5});  // edge 1→2 with weight 5
```

**Pros**: O(V + E) space, efficient for sparse graphs.
**Cons**: O(degree) edge lookup.

**When to use**: most interview problems. Default choice.

### Edge List
Just a list of all edges.

```java
int[][] edges = {{1, 2}, {2, 3}, {3, 4}};
```

**Pros**: Simple, good for Kruskal's MST, Bellman-Ford.
**Cons**: Slow for neighbor queries.

**When to use**: edge-based algorithms (MST, Union-Find).

### Implicit Graph (Grid)
For grid problems, the grid itself IS the graph — no need to build adjacency lists.

```java
char[][] grid;  // each cell is a node, neighbors are 4 (or 8) adjacent cells
```

### Which to Choose?

| Scenario | Use |
|----------|-----|
| Sparse graph, generic interview | **Adjacency List** |
| Dense graph, lots of edge queries | Adjacency Matrix |
| Need to sort/process edges | Edge List |
| Grid problem | Implicit (the grid itself) |
| Building from input | Adjacency List (default) |

---

## 3. How to Approach Any Graph Problem

### Step 1: Identify It as a Graph Problem

Signs:
- "Network", "connections", "relationships".
- "Reach from A to B".
- "Group/cluster things".
- "Dependencies", "ordering".
- "Grid", "matrix" with movement rules.
- "Tree" (a special graph).

### Step 2: Model It

Ask:
1. **What are the nodes?**
2. **What are the edges?**
3. **Directed or undirected?**
4. **Weighted or unweighted?**
5. **Connected or could have multiple components?**
6. **Can there be cycles?**

### Step 3: Pick the Right Pattern

Match the problem type:

| Question | Pattern |
|----------|---------|
| "Can we reach X from Y?" | DFS/BFS |
| "Shortest path (unweighted)?" | BFS |
| "Shortest path (weighted)?" | Dijkstra / Bellman-Ford |
| "Count connected components?" | DFS/BFS / Union-Find |
| "Cycle detection?" | DFS / Union-Find |
| "Order tasks with dependencies?" | Topological Sort |
| "Are these connected eventually?" | Union-Find |
| "Minimum spanning tree?" | Kruskal / Prim |
| "Is graph 2-colorable?" | Bipartite check (BFS/DFS) |
| "Multi-source shortest reachability?" | Multi-source BFS |
| "All paths between A and B?" | Backtracking |

### Step 4: Code Carefully

Common bugs:
- Forgetting visited marker → infinite loop.
- Marking visited too late → revisit.
- Off-by-one in node indexing.
- Wrong direction for undirected edges (must add both ways).

### Step 5: Discuss Complexity

- DFS/BFS: O(V + E).
- Dijkstra (with heap): O((V + E) log V).
- Union-Find: O(E α(V)) ≈ O(E).
- Floyd-Warshall: O(V³).

---

## 4. Company Tier Definitions

Different companies have different question difficulty distributions. This guide tags problems by tier:

### 🏢 Tier 1 — Service-based / Startups
- Companies: TCS, Infosys, Wipro, Accenture, Cognizant, mid-sized startups.
- Problem difficulty: Easy to Medium.
- Focus: standard patterns, clear solutions.
- Time: 30-45 mins per problem.

### 🏢 Tier 2 — Mid-tier Product Companies
- Companies: Paytm, Flipkart, Walmart, Adobe, Oracle, IBM, Cisco, Visa.
- Problem difficulty: Medium to Hard.
- Focus: pattern recognition, code quality, edge cases.
- Time: 45 mins per problem.

### 🏢 Tier 3 — FAANG / Top Product
- Companies: Google, Amazon, Microsoft, Meta, Apple, Netflix, Atlassian, Uber, LinkedIn.
- Problem difficulty: Hard, Medium-Hard.
- Focus: optimal solutions, deep analysis, follow-ups.
- Time: 45 mins, often multiple problems.

### 🏢 Tier 4 — Top-tier Quant / Tier 3+ FAANG
- Companies: Two Sigma, Citadel, Jane Street, D.E. Shaw, Google L5+, Meta E5+.
- Problem difficulty: Hard, novel.
- Focus: optimal complexity, creative thinking.
- Time: 45-60 mins per problem.

### Tier Mapping in This Guide

Each problem is tagged like:
- 🟢 **Tier 1+** (everyone asks)
- 🟡 **Tier 2+** (mid-tier and up)
- 🔴 **Tier 3+** (FAANG and up)
- 🟣 **Tier 4+** (top quant / senior FAANG)

---

## 5. Pattern 1: Grid Traversal (DFS/BFS)

### Core Idea
The grid is an implicit graph. Each cell is a node; neighbors are adjacent cells (4-directional or 8-directional). Use DFS or BFS to traverse.

### Template (DFS)
```java
private int[] dx = {0, 0, 1, -1};
private int[] dy = {1, -1, 0, 0};

void dfs(int[][] grid, int i, int j) {
    if (i < 0 || i >= grid.length || j < 0 || j >= grid[0].length || grid[i][j] != 1) return;
    grid[i][j] = -1;  // mark visited
    for (int k = 0; k < 4; k++) {
        dfs(grid, i + dx[k], j + dy[k]);
    }
}
```

### When to Use
- Connected components in a grid.
- Area / size of regions.
- Flood fill.
- Existence of paths.

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Number of Islands | Medium | 🟢 Tier 1+ | [LC 200](https://leetcode.com/problems/number-of-islands/) |
| Max Area of Island | Medium | 🟢 Tier 1+ | [LC 695](https://leetcode.com/problems/max-area-of-island/) |
| Flood Fill | Easy | 🟢 Tier 1+ | [LC 733](https://leetcode.com/problems/flood-fill/) |
| Island Perimeter | Easy | 🟢 Tier 1+ | [LC 463](https://leetcode.com/problems/island-perimeter/) |
| Number of Closed Islands | Medium | 🟡 Tier 2+ | [LC 1254](https://leetcode.com/problems/number-of-closed-islands/) |
| Number of Enclaves | Medium | 🟡 Tier 2+ | [LC 1020](https://leetcode.com/problems/number-of-enclaves/) |
| Surrounded Regions | Medium | 🟡 Tier 2+ | [LC 130](https://leetcode.com/problems/surrounded-regions/) |
| Making a Large Island | Hard | 🔴 Tier 3+ | [LC 827](https://leetcode.com/problems/making-a-large-island/) |
| Number of Distinct Islands | Medium | 🔴 Tier 3+ | [LC 694](https://leetcode.com/problems/number-of-distinct-islands/) |
| Pacific Atlantic Water Flow | Medium | 🟡 Tier 2+ | [LC 417](https://leetcode.com/problems/pacific-atlantic-water-flow/) |

---

## 6. Pattern 2: BFS / Shortest Path in Unweighted Graph

### Core Idea
BFS explores level by level. The first time you reach a node, you've found the shortest path (in terms of edges).

### Template
```java
public int bfs(int[][] graph, int start, int target) {
    Queue<Integer> queue = new LinkedList<>();
    Set<Integer> visited = new HashSet<>();
    queue.offer(start);
    visited.add(start);
    int level = 0;
    
    while (!queue.isEmpty()) {
        int size = queue.size();
        for (int i = 0; i < size; i++) {
            int node = queue.poll();
            if (node == target) return level;
            for (int neighbor : graph[node]) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }
        level++;
    }
    return -1;
}
```

### Critical Pitfalls
- **Mark visited when ENQUEUEING**, not when dequeuing. Otherwise duplicates.
- BFS gives shortest path **only for unweighted** (or uniform-weight) graphs.

### When to Use
- Shortest path in unweighted graph.
- Minimum number of steps.
- Level-order traversal.
- "Minimum moves to reach X" problems.

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Binary Tree Level Order | Medium | 🟢 Tier 1+ | [LC 102](https://leetcode.com/problems/binary-tree-level-order-traversal/) |
| Word Ladder | Hard | 🔴 Tier 3+ | [LC 127](https://leetcode.com/problems/word-ladder/) |
| Word Ladder II | Hard | 🟣 Tier 4+ | [LC 126](https://leetcode.com/problems/word-ladder-ii/) |
| Shortest Path in Binary Matrix | Medium | 🟡 Tier 2+ | [LC 1091](https://leetcode.com/problems/shortest-path-in-binary-matrix/) |
| Open the Lock | Medium | 🟡 Tier 2+ | [LC 752](https://leetcode.com/problems/open-the-lock/) |
| Jump Game III | Medium | 🟢 Tier 1+ | [LC 1306](https://leetcode.com/problems/jump-game-iii/) |
| Snakes and Ladders | Medium | 🟡 Tier 2+ | [LC 909](https://leetcode.com/problems/snakes-and-ladders/) |
| Sliding Puzzle | Hard | 🔴 Tier 3+ | [LC 773](https://leetcode.com/problems/sliding-puzzle/) |
| Bus Routes | Hard | 🔴 Tier 3+ | [LC 815](https://leetcode.com/problems/bus-routes/) |
| Shortest Bridge | Medium | 🔴 Tier 3+ | [LC 934](https://leetcode.com/problems/shortest-bridge/) |

---

## 7. Pattern 3: DFS Traversal (Generic Graph)

### Core Idea
Recursive exploration of a graph. Often used for connectivity, cycle detection, and tree-like problems.

### Template
```java
public void dfs(Map<Integer, List<Integer>> graph, int node, Set<Integer> visited) {
    if (visited.contains(node)) return;
    visited.add(node);
    
    // Process node
    
    for (int neighbor : graph.getOrDefault(node, new ArrayList<>())) {
        dfs(graph, neighbor, visited);
    }
}
```

### Iterative DFS (for huge graphs)
```java
public void dfsIterative(Map<Integer, List<Integer>> graph, int start) {
    Deque<Integer> stack = new ArrayDeque<>();
    Set<Integer> visited = new HashSet<>();
    stack.push(start);
    
    while (!stack.isEmpty()) {
        int node = stack.pop();
        if (visited.contains(node)) continue;
        visited.add(node);
        
        for (int neighbor : graph.getOrDefault(node, new ArrayList<>())) {
            if (!visited.contains(neighbor)) stack.push(neighbor);
        }
    }
}
```

### When to Use
- Reachability.
- Connected components.
- Tree/graph traversal.
- Path enumeration.

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Find If Path Exists | Easy | 🟢 Tier 1+ | [LC 1971](https://leetcode.com/problems/find-if-path-exists-in-graph/) |
| Clone Graph | Medium | 🟡 Tier 2+ | [LC 133](https://leetcode.com/problems/clone-graph/) |
| All Paths from Source to Target | Medium | 🟡 Tier 2+ | [LC 797](https://leetcode.com/problems/all-paths-from-source-to-target/) |
| Number of Connected Components | Medium | 🟢 Tier 1+ | [LC 323](https://leetcode.com/problems/number-of-connected-components-in-an-undirected-graph/) |
| Reconstruct Itinerary | Hard | 🔴 Tier 3+ | [LC 332](https://leetcode.com/problems/reconstruct-itinerary/) |
| Keys and Rooms | Medium | 🟡 Tier 2+ | [LC 841](https://leetcode.com/problems/keys-and-rooms/) |
| Evaluate Division | Medium | 🔴 Tier 3+ | [LC 399](https://leetcode.com/problems/evaluate-division/) |
| Most Stones Removed | Medium | 🔴 Tier 3+ | [LC 947](https://leetcode.com/problems/most-stones-removed-with-same-row-or-column/) |

---

## 8. Pattern 4: Cycle Detection

### Core Idea
Detect if a graph contains a cycle. The approach differs for **directed** vs **undirected** graphs.

### Undirected Graph (Cycle Detection)
Use DFS with parent tracking. If we visit a node we've already seen AND it's NOT our parent, there's a cycle.

```java
public boolean hasCycleUndirected(int[][] graph, int n) {
    boolean[] visited = new boolean[n];
    for (int i = 0; i < n; i++) {
        if (!visited[i]) {
            if (dfs(graph, i, -1, visited)) return true;
        }
    }
    return false;
}

private boolean dfs(int[][] graph, int node, int parent, boolean[] visited) {
    visited[node] = true;
    for (int neighbor : graph[node]) {
        if (!visited[neighbor]) {
            if (dfs(graph, neighbor, node, visited)) return true;
        } else if (neighbor != parent) {
            return true;  // visited and not parent → cycle
        }
    }
    return false;
}
```

### Directed Graph (Cycle Detection)
Use DFS with 3 states: unvisited, in-progress, done. Cycle exists if we re-enter an "in-progress" node.

```java
private boolean hasCycleDirected(List<List<Integer>> graph, int n) {
    int[] state = new int[n];  // 0=unvisited, 1=in-progress, 2=done
    for (int i = 0; i < n; i++) {
        if (state[i] == 0 && dfs(graph, i, state)) return true;
    }
    return false;
}

private boolean dfs(List<List<Integer>> graph, int node, int[] state) {
    state[node] = 1;  // in progress
    for (int neighbor : graph.get(node)) {
        if (state[neighbor] == 1) return true;  // cycle!
        if (state[neighbor] == 0 && dfs(graph, neighbor, state)) return true;
    }
    state[node] = 2;  // done
    return false;
}
```

### Union-Find Alternative (Undirected Only)
For each edge, check if the two endpoints are already in the same set. If yes → cycle. If no → union them.

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Find Eventual Safe States | Medium | 🟡 Tier 2+ | [LC 802](https://leetcode.com/problems/find-eventual-safe-states/) |
| Graph Valid Tree | Medium | 🟡 Tier 2+ | [LC 261](https://leetcode.com/problems/graph-valid-tree/) |
| Detect Cycles in 2D Grid | Medium | 🔴 Tier 3+ | [LC 1559](https://leetcode.com/problems/detect-cycles-in-2d-grid/) |
| Redundant Connection | Medium | 🟡 Tier 2+ | [LC 684](https://leetcode.com/problems/redundant-connection/) |
| Redundant Connection II | Hard | 🔴 Tier 3+ | [LC 685](https://leetcode.com/problems/redundant-connection-ii/) |
| Course Schedule | Medium | 🟢 Tier 1+ | [LC 207](https://leetcode.com/problems/course-schedule/) |

---

## 9. Pattern 5: Topological Sort

### Core Idea
Linear ordering of vertices in a DAG such that for every edge `u → v`, `u` comes before `v`. Used for ordering tasks with dependencies.

### Two Approaches

#### A. Kahn's Algorithm (BFS-based)
```java
public int[] topoSort(int n, int[][] edges) {
    List<List<Integer>> graph = new ArrayList<>();
    int[] inDegree = new int[n];
    for (int i = 0; i < n; i++) graph.add(new ArrayList<>());
    
    for (int[] edge : edges) {
        graph.get(edge[0]).add(edge[1]);
        inDegree[edge[1]]++;
    }
    
    Queue<Integer> queue = new LinkedList<>();
    for (int i = 0; i < n; i++) {
        if (inDegree[i] == 0) queue.offer(i);
    }
    
    int[] order = new int[n];
    int idx = 0;
    while (!queue.isEmpty()) {
        int node = queue.poll();
        order[idx++] = node;
        for (int neighbor : graph.get(node)) {
            if (--inDegree[neighbor] == 0) queue.offer(neighbor);
        }
    }
    
    // If idx < n, there's a cycle
    return idx == n ? order : new int[0];
}
```

#### B. DFS-based (Post-Order)
```java
public List<Integer> topoSortDFS(int n, int[][] edges) {
    List<List<Integer>> graph = new ArrayList<>();
    for (int i = 0; i < n; i++) graph.add(new ArrayList<>());
    for (int[] e : edges) graph.get(e[0]).add(e[1]);
    
    int[] state = new int[n];
    Deque<Integer> stack = new ArrayDeque<>();
    
    for (int i = 0; i < n; i++) {
        if (state[i] == 0 && hasCycle(graph, i, state, stack)) return new ArrayList<>();
    }
    
    List<Integer> result = new ArrayList<>(stack);
    return result;
}

private boolean hasCycle(List<List<Integer>> graph, int node, int[] state, Deque<Integer> stack) {
    state[node] = 1;
    for (int neighbor : graph.get(node)) {
        if (state[neighbor] == 1) return true;
        if (state[neighbor] == 0 && hasCycle(graph, neighbor, state, stack)) return true;
    }
    state[node] = 2;
    stack.push(node);
    return false;
}
```

### When to Use
- Course prerequisites.
- Build dependency order.
- Task scheduling.
- "Can these tasks be completed?"

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Course Schedule | Medium | 🟢 Tier 1+ | [LC 207](https://leetcode.com/problems/course-schedule/) |
| Course Schedule II | Medium | 🟡 Tier 2+ | [LC 210](https://leetcode.com/problems/course-schedule-ii/) |
| Alien Dictionary | Hard | 🔴 Tier 3+ | [LC 269](https://leetcode.com/problems/alien-dictionary/) |
| Minimum Height Trees | Medium | 🔴 Tier 3+ | [LC 310](https://leetcode.com/problems/minimum-height-trees/) |
| Sequence Reconstruction | Medium | 🟡 Tier 2+ | [LC 444](https://leetcode.com/problems/sequence-reconstruction/) |
| Sort Items by Groups | Hard | 🔴 Tier 3+ | [LC 1203](https://leetcode.com/problems/sort-items-by-groups-respecting-dependencies/) |
| Parallel Courses | Medium | 🟡 Tier 2+ | [LC 1136](https://leetcode.com/problems/parallel-courses/) |
| Build Order | Medium | 🟢 Tier 1+ | (CTCI 4.7) |

---

## 10. Pattern 6: Union-Find (Disjoint Set Union)

### Core Idea
Data structure to efficiently manage a collection of disjoint sets. Supports `union(a, b)` (merge sets) and `find(a)` (which set is `a` in?).

### Template (With Path Compression & Union by Rank)
```java
class UnionFind {
    int[] parent;
    int[] rank;
    int count;  // number of components
    
    public UnionFind(int n) {
        parent = new int[n];
        rank = new int[n];
        count = n;
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
        if (px == py) return false;  // already in same set
        
        // Union by rank
        if (rank[px] < rank[py]) parent[px] = py;
        else if (rank[px] > rank[py]) parent[py] = px;
        else { parent[py] = px; rank[px]++; }
        
        count--;
        return true;
    }
}
```

### Complexity
With path compression + union by rank: **O(α(n))** per operation (effectively O(1)).

### When to Use
- Connected components.
- Cycle detection (undirected).
- Online connectivity queries.
- "Are these two in the same group?"
- Kruskal's MST.

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Number of Islands | Medium | 🟢 Tier 1+ | [LC 200](https://leetcode.com/problems/number-of-islands/) |
| Friend Circles / Provinces | Medium | 🟢 Tier 1+ | [LC 547](https://leetcode.com/problems/number-of-provinces/) |
| Redundant Connection | Medium | 🟡 Tier 2+ | [LC 684](https://leetcode.com/problems/redundant-connection/) |
| Accounts Merge | Medium | 🔴 Tier 3+ | [LC 721](https://leetcode.com/problems/accounts-merge/) |
| Number of Islands II | Hard | 🔴 Tier 3+ | [LC 305](https://leetcode.com/problems/number-of-islands-ii/) |
| Most Stones Removed | Medium | 🔴 Tier 3+ | [LC 947](https://leetcode.com/problems/most-stones-removed-with-same-row-or-column/) |
| Satisfiability of Equality | Medium | 🟡 Tier 2+ | [LC 990](https://leetcode.com/problems/satisfiability-of-equality-equations/) |
| Swim in Rising Water | Hard | 🔴 Tier 3+ | [LC 778](https://leetcode.com/problems/swim-in-rising-water/) |
| Min Cost to Connect All Points | Medium | 🟡 Tier 2+ | [LC 1584](https://leetcode.com/problems/min-cost-to-connect-all-points/) |
| Regions Cut by Slashes | Medium | 🔴 Tier 3+ | [LC 959](https://leetcode.com/problems/regions-cut-by-slashes/) |

---

## 11. Pattern 7: Shortest Path Algorithms (Weighted)

### A. Dijkstra's Algorithm

For graphs with **non-negative** weights. O((V + E) log V) with priority queue.

```java
public int dijkstra(int[][] graph, int n, int start, int end) {
    int[] dist = new int[n];
    Arrays.fill(dist, Integer.MAX_VALUE);
    dist[start] = 0;
    
    PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[1] - b[1]);  // [node, dist]
    pq.offer(new int[]{start, 0});
    
    while (!pq.isEmpty()) {
        int[] curr = pq.poll();
        int node = curr[0], d = curr[1];
        
        if (d > dist[node]) continue;  // stale entry
        if (node == end) return d;
        
        for (int[] edge : graph[node]) {  // [neighbor, weight]
            int next = edge[0], w = edge[1];
            if (dist[node] + w < dist[next]) {
                dist[next] = dist[node] + w;
                pq.offer(new int[]{next, dist[next]});
            }
        }
    }
    return -1;
}
```

### B. Bellman-Ford
Handles **negative weights**. Detects negative cycles. O(VE).

```java
public int[] bellmanFord(int n, int[][] edges, int start) {
    int[] dist = new int[n];
    Arrays.fill(dist, Integer.MAX_VALUE);
    dist[start] = 0;
    
    for (int i = 0; i < n - 1; i++) {  // V-1 iterations
        for (int[] edge : edges) {  // [u, v, w]
            if (dist[edge[0]] != Integer.MAX_VALUE 
                && dist[edge[0]] + edge[2] < dist[edge[1]]) {
                dist[edge[1]] = dist[edge[0]] + edge[2];
            }
        }
    }
    
    // Detect negative cycle by trying one more relaxation
    for (int[] edge : edges) {
        if (dist[edge[0]] != Integer.MAX_VALUE 
            && dist[edge[0]] + edge[2] < dist[edge[1]]) {
            return null;  // negative cycle!
        }
    }
    return dist;
}
```

### C. Floyd-Warshall
All-pairs shortest paths. O(V³).

```java
public int[][] floydWarshall(int n, int[][] edges) {
    int[][] dist = new int[n][n];
    for (int[] row : dist) Arrays.fill(row, Integer.MAX_VALUE);
    for (int i = 0; i < n; i++) dist[i][i] = 0;
    for (int[] edge : edges) dist[edge[0]][edge[1]] = edge[2];
    
    for (int k = 0; k < n; k++) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (dist[i][k] != Integer.MAX_VALUE 
                    && dist[k][j] != Integer.MAX_VALUE 
                    && dist[i][k] + dist[k][j] < dist[i][j]) {
                    dist[i][j] = dist[i][k] + dist[k][j];
                }
            }
        }
    }
    return dist;
}
```

### When to Use Each

| Algorithm | Use When |
|-----------|----------|
| **BFS** | Unweighted graph, shortest path |
| **Dijkstra** | Non-negative weights, single source |
| **Bellman-Ford** | Negative weights possible, detect negative cycles |
| **Floyd-Warshall** | All-pairs shortest paths, small V (< 500) |

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Network Delay Time | Medium | 🟡 Tier 2+ | [LC 743](https://leetcode.com/problems/network-delay-time/) |
| Cheapest Flights Within K Stops | Medium | 🔴 Tier 3+ | [LC 787](https://leetcode.com/problems/cheapest-flights-within-k-stops/) |
| Path With Minimum Effort | Medium | 🔴 Tier 3+ | [LC 1631](https://leetcode.com/problems/path-with-minimum-effort/) |
| Path With Max Probability | Medium | 🟡 Tier 2+ | [LC 1514](https://leetcode.com/problems/path-with-maximum-probability/) |
| Find Cheapest Path | Medium | 🟡 Tier 2+ | [LC 787](https://leetcode.com/problems/cheapest-flights-within-k-stops/) |
| The Maze II | Medium | 🟡 Tier 2+ | [LC 505](https://leetcode.com/problems/the-maze-ii/) |
| Swim in Rising Water | Hard | 🔴 Tier 3+ | [LC 778](https://leetcode.com/problems/swim-in-rising-water/) |
| Reachable Nodes In Subdivided Graph | Hard | 🟣 Tier 4+ | [LC 882](https://leetcode.com/problems/reachable-nodes-in-subdivided-graph/) |
| Find the City with Smallest Reachable | Medium | 🟡 Tier 2+ | [LC 1334](https://leetcode.com/problems/find-the-city-with-the-smallest-number-of-neighbors-at-a-threshold-distance/) |
| Min Cost to Make Valid Path | Hard | 🟣 Tier 4+ | [LC 1368](https://leetcode.com/problems/minimum-cost-to-make-at-least-one-valid-path-in-a-grid/) |

---

## 12. Pattern 8: Minimum Spanning Tree

### Core Idea
Given a connected undirected weighted graph, find a subset of edges that connects all vertices with **minimum total weight** and **no cycles**.

### A. Kruskal's Algorithm (Edge-based)
Sort edges by weight. Add each if it doesn't create a cycle (use Union-Find).

```java
public int kruskalMST(int n, int[][] edges) {
    Arrays.sort(edges, (a, b) -> a[2] - b[2]);  // sort by weight
    UnionFind uf = new UnionFind(n);
    int totalCost = 0, edgeCount = 0;
    
    for (int[] edge : edges) {
        if (uf.union(edge[0], edge[1])) {
            totalCost += edge[2];
            if (++edgeCount == n - 1) break;
        }
    }
    return edgeCount == n - 1 ? totalCost : -1;
}
```

### B. Prim's Algorithm (Node-based)
Grow MST one vertex at a time. Use min-heap to pick cheapest edge from frontier.

```java
public int primMST(List<List<int[]>> graph, int n) {
    PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[1] - b[1]);
    boolean[] visited = new boolean[n];
    pq.offer(new int[]{0, 0});  // [node, weight]
    int totalCost = 0, count = 0;
    
    while (!pq.isEmpty() && count < n) {
        int[] curr = pq.poll();
        int node = curr[0], w = curr[1];
        if (visited[node]) continue;
        visited[node] = true;
        totalCost += w;
        count++;
        
        for (int[] edge : graph.get(node)) {
            if (!visited[edge[0]]) pq.offer(edge);
        }
    }
    return count == n ? totalCost : -1;
}
```

### Kruskal vs Prim

| Aspect | Kruskal | Prim |
|--------|---------|------|
| Approach | Edge-based | Node-based |
| Data structure | Union-Find | Min-heap |
| Complexity | O(E log E) | O(E log V) |
| Sparse graphs | Better | OK |
| Dense graphs | OK | Better |

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Min Cost to Connect All Points | Medium | 🟡 Tier 2+ | [LC 1584](https://leetcode.com/problems/min-cost-to-connect-all-points/) |
| Connecting Cities With Minimum Cost | Medium | 🟡 Tier 2+ | [LC 1135](https://leetcode.com/problems/connecting-cities-with-minimum-cost/) |
| Optimize Water Distribution | Hard | 🔴 Tier 3+ | [LC 1168](https://leetcode.com/problems/optimize-water-distribution-in-a-village/) |
| Find Critical and Pseudo-Critical Edges | Hard | 🟣 Tier 4+ | [LC 1489](https://leetcode.com/problems/find-critical-and-pseudo-critical-edges-in-minimum-spanning-tree/) |

---

## 13. Pattern 9: Strongly Connected Components

### Core Idea
In a directed graph, find groups of vertices where every vertex is reachable from every other in the group.

### Algorithms

#### Kosaraju's Algorithm
1. DFS to compute finishing times.
2. Transpose the graph (reverse all edges).
3. DFS in reverse-finish order on transposed graph.

```java
public List<List<Integer>> kosaraju(int n, List<List<Integer>> graph) {
    boolean[] visited = new boolean[n];
    Deque<Integer> stack = new ArrayDeque<>();
    
    // Pass 1: order by finish time
    for (int i = 0; i < n; i++) {
        if (!visited[i]) dfs1(graph, i, visited, stack);
    }
    
    // Build transpose
    List<List<Integer>> transpose = new ArrayList<>();
    for (int i = 0; i < n; i++) transpose.add(new ArrayList<>());
    for (int u = 0; u < n; u++) {
        for (int v : graph.get(u)) transpose.get(v).add(u);
    }
    
    // Pass 2: DFS in reverse finish order
    Arrays.fill(visited, false);
    List<List<Integer>> sccs = new ArrayList<>();
    while (!stack.isEmpty()) {
        int node = stack.pop();
        if (!visited[node]) {
            List<Integer> scc = new ArrayList<>();
            dfs2(transpose, node, visited, scc);
            sccs.add(scc);
        }
    }
    return sccs;
}
```

#### Tarjan's Algorithm
Single DFS, more elegant. Uses discovery time and low-link values.

### When to Use
- Find tight communities in directed graphs.
- Cycle analysis in directed graphs.
- Compiler optimization (find self-dependent sets).

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Critical Connections (Bridges) | Hard | 🟣 Tier 4+ | [LC 1192](https://leetcode.com/problems/critical-connections-in-a-network/) |

This is more of an academic/competitive topic. Rarely asked at Tier 2 and below.

---

## 14. Pattern 10: Bipartite Graph / Graph Coloring

### Core Idea
A graph is **bipartite** if vertices can be split into two sets such that all edges go between sets (no edges within a set). Equivalent: graph is 2-colorable.

### BFS Approach
```java
public boolean isBipartite(int[][] graph) {
    int n = graph.length;
    int[] color = new int[n];  // 0 = uncolored, 1 = red, -1 = blue
    
    for (int start = 0; start < n; start++) {
        if (color[start] != 0) continue;
        
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(start);
        color[start] = 1;
        
        while (!queue.isEmpty()) {
            int node = queue.poll();
            for (int neighbor : graph[node]) {
                if (color[neighbor] == 0) {
                    color[neighbor] = -color[node];
                    queue.offer(neighbor);
                } else if (color[neighbor] == color[node]) {
                    return false;  // same color → not bipartite
                }
            }
        }
    }
    return true;
}
```

### When to Use
- "Can we split into 2 teams?"
- Conflict detection.
- Matching problems.

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Is Graph Bipartite? | Medium | 🟢 Tier 1+ | [LC 785](https://leetcode.com/problems/is-graph-bipartite/) |
| Possible Bipartition | Medium | 🟡 Tier 2+ | [LC 886](https://leetcode.com/problems/possible-bipartition/) |
| Flower Planting | Easy | 🟢 Tier 1+ | [LC 1042](https://leetcode.com/problems/flower-planting-with-no-adjacent/) |

---

## 15. Pattern 11: Multi-Source BFS

### Core Idea
Start BFS from **multiple sources simultaneously**. All sources are added to the queue initially. Useful when "spread from multiple starting points" is the question.

### Template
```java
public int multiSourceBFS(int[][] grid) {
    int r = grid.length, c = grid[0].length;
    Queue<int[]> queue = new LinkedList<>();
    
    // Add all sources to queue
    for (int i = 0; i < r; i++) {
        for (int j = 0; j < c; j++) {
            if (grid[i][j] == 2) {  // source
                queue.offer(new int[]{i, j});
            }
        }
    }
    
    int level = 0;
    int[] dx = {1, -1, 0, 0};
    int[] dy = {0, 0, 1, -1};
    
    while (!queue.isEmpty()) {
        int size = queue.size();
        for (int i = 0; i < size; i++) {
            int[] cell = queue.poll();
            for (int k = 0; k < 4; k++) {
                int ni = cell[0] + dx[k], nj = cell[1] + dy[k];
                if (ni >= 0 && ni < r && nj >= 0 && nj < c && grid[ni][nj] == 1) {
                    grid[ni][nj] = 2;  // mark visited
                    queue.offer(new int[]{ni, nj});
                }
            }
        }
        level++;
    }
    return level;
}
```

### When to Use
- "Spread from all source cells simultaneously."
- "Minimum time for all cells to be affected."
- "Distance from nearest X."

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Rotting Oranges | Medium | 🟢 Tier 1+ | [LC 994](https://leetcode.com/problems/rotting-oranges/) |
| Walls and Gates | Medium | 🟡 Tier 2+ | [LC 286](https://leetcode.com/problems/walls-and-gates/) |
| 01 Matrix | Medium | 🟡 Tier 2+ | [LC 542](https://leetcode.com/problems/01-matrix/) |
| As Far From Land as Possible | Medium | 🔴 Tier 3+ | [LC 1162](https://leetcode.com/problems/as-far-from-land-as-possible/) |
| Map of Highest Peak | Medium | 🟡 Tier 2+ | [LC 1765](https://leetcode.com/problems/map-of-highest-peak/) |

---

## 16. Pattern 12: Backtracking on Graphs

### Core Idea
DFS where we **undo** the visited marker after exploring. Used when we need to find ALL paths, ALL configurations, etc.

### Template
```java
public List<List<Integer>> allPaths(int[][] graph, int start, int end) {
    List<List<Integer>> result = new ArrayList<>();
    List<Integer> path = new ArrayList<>();
    Set<Integer> visited = new HashSet<>();
    backtrack(graph, start, end, path, result, visited);
    return result;
}

private void backtrack(int[][] graph, int node, int end, 
                       List<Integer> path, List<List<Integer>> result, Set<Integer> visited) {
    path.add(node);
    visited.add(node);
    
    if (node == end) {
        result.add(new ArrayList<>(path));
    } else {
        for (int neighbor : graph[node]) {
            if (!visited.contains(neighbor)) {
                backtrack(graph, neighbor, end, path, result, visited);
            }
        }
    }
    
    // BACKTRACK: undo
    path.remove(path.size() - 1);
    visited.remove(node);
}
```

### Difference from Standard DFS
- DFS marks visited and **never unmarks** → finds reachability, not all paths.
- Backtracking marks visited, explores, then **unmarks** → finds all configurations.

### When to Use
- All paths from A to B.
- Find a valid configuration (Sudoku, N-Queens).
- Word Search on grid.

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| All Paths from Source to Target | Medium | 🟡 Tier 2+ | [LC 797](https://leetcode.com/problems/all-paths-from-source-to-target/) |
| Word Search | Medium | 🟢 Tier 1+ | [LC 79](https://leetcode.com/problems/word-search/) |
| Word Search II | Hard | 🔴 Tier 3+ | [LC 212](https://leetcode.com/problems/word-search-ii/) |
| N-Queens | Hard | 🔴 Tier 3+ | [LC 51](https://leetcode.com/problems/n-queens/) |
| Sudoku Solver | Hard | 🔴 Tier 3+ | [LC 37](https://leetcode.com/problems/sudoku-solver/) |
| Restore IP Addresses | Medium | 🟡 Tier 2+ | [LC 93](https://leetcode.com/problems/restore-ip-addresses/) |

---

## 17. Pattern 13: Advanced Topics

These topics are less common in standard interviews but appear in **Tier 3+** and competitive programming.

### A. Bridges and Articulation Points
- **Bridge**: edge whose removal disconnects the graph.
- **Articulation point**: vertex whose removal disconnects the graph.
- Algorithm: Tarjan's (using DFS with disc/low arrays).

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Critical Connections | Hard | 🟣 Tier 4+ | [LC 1192](https://leetcode.com/problems/critical-connections-in-a-network/) |

### B. Eulerian Paths and Circuits
- Path that visits every edge exactly once.
- Exists iff at most 2 vertices have odd degree.

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Reconstruct Itinerary | Hard | 🔴 Tier 3+ | [LC 332](https://leetcode.com/problems/reconstruct-itinerary/) |
| Valid Arrangement of Pairs | Hard | 🟣 Tier 4+ | [LC 2097](https://leetcode.com/problems/valid-arrangement-of-pairs/) |

### C. Max Flow / Min Cut
- Network flow problems.
- Algorithms: Ford-Fulkerson, Edmonds-Karp.
- Rarely asked except at top quant or research labs.

### D. A* Search
- Informed search with heuristics.
- Used in path-finding (games, robotics).
- Less common in interviews.

### E. 0-1 BFS / Deque BFS
- BFS variant for graphs with edge weights 0 or 1.
- O(V + E) instead of Dijkstra's O((V+E) log V).

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Minimum Obstacle Removal | Hard | 🟣 Tier 4+ | [LC 2290](https://leetcode.com/problems/minimum-obstacle-removal-to-reach-corner/) |

### F. Tree Algorithms
Special case of graphs (no cycles, connected).

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Diameter of Tree | Medium | 🟡 Tier 2+ | [LC 543](https://leetcode.com/problems/diameter-of-binary-tree/) |
| Tree Diameter (General Tree) | Medium | 🔴 Tier 3+ | [LC 1245](https://leetcode.com/problems/tree-diameter/) |
| Sum of Distances in Tree | Hard | 🟣 Tier 4+ | [LC 834](https://leetcode.com/problems/sum-of-distances-in-tree/) |
| Count Subtrees With Max Distance | Hard | 🟣 Tier 4+ | [LC 1617](https://leetcode.com/problems/count-subtrees-with-max-distance-between-cities/) |

---

## 18. Suggested Study Order

### Phase 1: Foundations (1-2 weeks) — Tier 1+
Master these before anything else.

1. **Grid Traversal (DFS)**:
   - Number of Islands (LC 200)
   - Max Area of Island (LC 695)
   - Flood Fill (LC 733)

2. **Grid Traversal (BFS)**:
   - Rotting Oranges (LC 994)
   - 01 Matrix (LC 542)

3. **Basic Graph DFS/BFS**:
   - Find If Path Exists (LC 1971)
   - Number of Connected Components (LC 323)
   - Clone Graph (LC 133)

4. **Union-Find Basics**:
   - Number of Provinces (LC 547)
   - Number of Islands using Union-Find

### Phase 2: Intermediate (2-3 weeks) — Tier 2+

1. **Cycle Detection**:
   - Course Schedule (LC 207)
   - Graph Valid Tree (LC 261)
   - Redundant Connection (LC 684)

2. **Topological Sort**:
   - Course Schedule II (LC 210)
   - Sequence Reconstruction (LC 444)

3. **Bipartite Check**:
   - Is Graph Bipartite? (LC 785)
   - Possible Bipartition (LC 886)

4. **Shortest Path (Unweighted)**:
   - Shortest Path in Binary Matrix (LC 1091)
   - Word Ladder (LC 127)

5. **Multi-Source BFS**:
   - Walls and Gates (LC 286)
   - As Far From Land (LC 1162)

### Phase 3: Advanced (2-3 weeks) — Tier 3+

1. **Dijkstra**:
   - Network Delay Time (LC 743)
   - Path With Min Effort (LC 1631)
   - Cheapest Flights K Stops (LC 787)

2. **Advanced Union-Find**:
   - Accounts Merge (LC 721)
   - Number of Islands II (LC 305)
   - Most Stones Removed (LC 947)

3. **Advanced DFS/BFS**:
   - Word Ladder II (LC 126)
   - Alien Dictionary (LC 269)
   - Minimum Height Trees (LC 310)

4. **MST**:
   - Min Cost to Connect All Points (LC 1584)

### Phase 4: Mastery (1-2 weeks) — Tier 4+

1. **Bellman-Ford / SPFA**:
   - Cheapest Flights K Stops (alternate solution).

2. **Advanced Topics**:
   - Critical Connections (LC 1192) - Bridges/Tarjan.
   - Reconstruct Itinerary (LC 332) - Eulerian path.
   - Sum of Distances in Tree (LC 834) - Tree DP.

3. **0-1 BFS**:
   - Minimum Obstacle Removal (LC 2290).

---

## 19. Templates and Cheat Sheets

### Quick-Reference Table

| Pattern | When | Complexity | Key Insight |
|---------|------|-----------|-------------|
| DFS | Reachability, components | O(V+E) | Stack/recursion-based |
| BFS | Shortest path (unweighted) | O(V+E) | Queue-based, mark on enqueue |
| Union-Find | Components, cycles | O(E α(V)) | Path compression + rank |
| Topological Sort | DAG ordering | O(V+E) | Kahn (BFS) or DFS-post-order |
| Dijkstra | Shortest path (non-neg) | O((V+E) log V) | Min-heap |
| Bellman-Ford | Shortest path (neg edges) | O(VE) | V-1 iterations |
| Floyd-Warshall | All-pairs shortest | O(V³) | DP on intermediates |
| Bipartite | 2-color check | O(V+E) | BFS with color flip |
| Multi-Source BFS | Distance from any source | O(V+E) | All sources start in queue |
| Backtracking | All paths/configs | O(V!) worst | Undo visited |

### Boilerplate: Adjacency List from Edges
```java
Map<Integer, List<Integer>> graph = new HashMap<>();
for (int[] edge : edges) {
    graph.computeIfAbsent(edge[0], k -> new ArrayList<>()).add(edge[1]);
    graph.computeIfAbsent(edge[1], k -> new ArrayList<>()).add(edge[0]);  // undirected
}
```

### Boilerplate: Visited Tracking
```java
Set<Integer> visited = new HashSet<>();
// OR for n nodes 0..n-1:
boolean[] visited = new boolean[n];
```

### Boilerplate: Direction Arrays
```java
int[] dx = {0, 0, 1, -1};
int[] dy = {1, -1, 0, 0};
// 8 directions: {-1,-1,-1,0,0,1,1,1}, {-1,0,1,-1,1,-1,0,1}
```

---

## 20. Interview Strategy

### How to Approach a Graph Question

#### Step 1: Identify it's a graph problem (sometimes hidden)
- "Group", "connection", "network" → graph.
- Grids with movement → implicit graph.
- "Dependencies", "ordering" → DAG.

#### Step 2: Clarify the graph
- "Is it directed or undirected?"
- "Can there be cycles?"
- "Are weights non-negative?"
- "What's the max V and E?"

#### Step 3: Pick the algorithm
Use the decision table from Section 19.

#### Step 4: Choose representation
- Sparse: adjacency list.
- Need edge weights: list of `int[]{neighbor, weight}`.
- Dense or O(1) lookup needed: matrix.

#### Step 5: Code it carefully
Watch for:
- Visited marker.
- Direction of edges (add both for undirected).
- Bounds checks for grids.
- Queue vs stack for BFS vs DFS.

#### Step 6: Test on small examples
- Single node.
- Disconnected graph.
- Cycle.
- Edge cases like empty input.

#### Step 7: Discuss complexity and optimizations

### Top Interview Phrases

> "This is a connected components problem, so I'll use DFS (or Union-Find)."

> "Since the graph is unweighted and we want shortest path, BFS is optimal."

> "I'll use Union-Find with path compression and union-by-rank for amortized O(α(N))."

> "For non-negative weighted shortest path, Dijkstra with a min-heap gives O((V+E)log V)."

> "I'd convert this to a topological sort problem since there are dependencies."

### Common Interview Mistakes

1. **Not asking about graph properties** (directed? weighted? cycles?).
2. **Wrong representation** (matrix for sparse graph → wasted space).
3. **BFS without level tracking** → loses distance info.
4. **Marking visited too late** → infinite loops.
5. **Not handling disconnected components** in the outer loop.
6. **Confusing DFS with backtracking** (when to unmark).
7. **Using Dijkstra with negative edges** (use Bellman-Ford instead).

### How Companies Differ

#### Tier 1 (Service / Easy startups)
- Standard patterns.
- BFS / DFS, simple Union-Find.
- "Number of Islands" level.

#### Tier 2 (Paytm, Flipkart, etc.)
- Cycle detection, topological sort.
- Union-Find with optimizations.
- Some Dijkstra.

#### Tier 3 (FAANG)
- Optimal algorithms expected.
- Subtle variations of standard problems.
- Multiple solutions discussed.

#### Tier 4 (Top quant / FAANG senior)
- Novel problems.
- Advanced algorithms (SCC, bridges, max flow).
- Tight analysis, deep follow-ups.

---

## Bonus: Top 30 Must-Solve Graph Problems

Ranked by importance for interviews:

1. [LC 200 — Number of Islands](https://leetcode.com/problems/number-of-islands/) 🟢
2. [LC 207 — Course Schedule](https://leetcode.com/problems/course-schedule/) 🟢
3. [LC 133 — Clone Graph](https://leetcode.com/problems/clone-graph/) 🟡
4. [LC 994 — Rotting Oranges](https://leetcode.com/problems/rotting-oranges/) 🟢
5. [LC 547 — Number of Provinces](https://leetcode.com/problems/number-of-provinces/) 🟢
6. [LC 695 — Max Area of Island](https://leetcode.com/problems/max-area-of-island/) 🟢
7. [LC 210 — Course Schedule II](https://leetcode.com/problems/course-schedule-ii/) 🟡
8. [LC 127 — Word Ladder](https://leetcode.com/problems/word-ladder/) 🔴
9. [LC 785 — Is Graph Bipartite?](https://leetcode.com/problems/is-graph-bipartite/) 🟢
10. [LC 130 — Surrounded Regions](https://leetcode.com/problems/surrounded-regions/) 🟡
11. [LC 684 — Redundant Connection](https://leetcode.com/problems/redundant-connection/) 🟡
12. [LC 743 — Network Delay Time](https://leetcode.com/problems/network-delay-time/) 🟡
13. [LC 542 — 01 Matrix](https://leetcode.com/problems/01-matrix/) 🟡
14. [LC 286 — Walls and Gates](https://leetcode.com/problems/walls-and-gates/) 🟡
15. [LC 79 — Word Search](https://leetcode.com/problems/word-search/) 🟢
16. [LC 1091 — Shortest Path in Binary Matrix](https://leetcode.com/problems/shortest-path-in-binary-matrix/) 🟡
17. [LC 261 — Graph Valid Tree](https://leetcode.com/problems/graph-valid-tree/) 🟡
18. [LC 323 — Connected Components](https://leetcode.com/problems/number-of-connected-components-in-an-undirected-graph/) 🟢
19. [LC 269 — Alien Dictionary](https://leetcode.com/problems/alien-dictionary/) 🔴
20. [LC 787 — Cheapest Flights K Stops](https://leetcode.com/problems/cheapest-flights-within-k-stops/) 🔴
21. [LC 721 — Accounts Merge](https://leetcode.com/problems/accounts-merge/) 🔴
22. [LC 1584 — Min Cost to Connect Points](https://leetcode.com/problems/min-cost-to-connect-all-points/) 🟡
23. [LC 399 — Evaluate Division](https://leetcode.com/problems/evaluate-division/) 🔴
24. [LC 1162 — As Far From Land](https://leetcode.com/problems/as-far-from-land-as-possible/) 🔴
25. [LC 1192 — Critical Connections](https://leetcode.com/problems/critical-connections-in-a-network/) 🟣
26. [LC 332 — Reconstruct Itinerary](https://leetcode.com/problems/reconstruct-itinerary/) 🔴
27. [LC 1631 — Path with Min Effort](https://leetcode.com/problems/path-with-minimum-effort/) 🔴
28. [LC 305 — Number of Islands II](https://leetcode.com/problems/number-of-islands-ii/) 🔴
29. [LC 827 — Making a Large Island](https://leetcode.com/problems/making-a-large-island/) 🔴
30. [LC 947 — Most Stones Removed](https://leetcode.com/problems/most-stones-removed-with-same-row-or-column/) 🔴

---

## TL;DR

### The Six Truths of Graph Interviews

1. **Most graph problems are DFS or BFS variants** — master these first.
2. **Representation matters** — adjacency list for sparse (default), matrix for dense.
3. **BFS for unweighted shortest path, Dijkstra for non-negative weighted**.
4. **Topological sort for DAG ordering** — Kahn's BFS or DFS post-order.
5. **Union-Find for "are they connected?" queries** — almost O(1) with optimizations.
6. **Always mark visited carefully** — before recursion for DFS, before enqueue for BFS.

### Pattern Recognition Cheat Sheet

| Keyword | Pattern |
|---------|---------|
| "How many islands/groups?" | DFS/BFS or Union-Find |
| "Shortest path" | BFS (unweighted) or Dijkstra |
| "Course prerequisites" | Topological sort |
| "Can we color with 2 colors?" | Bipartite check |
| "Cycle?" | DFS with state OR Union-Find |
| "All paths" | Backtracking |
| "Cheapest connection" | MST (Kruskal/Prim) |
| "Spreading from sources" | Multi-source BFS |
| "After K operations" | BFS with state |
| "Networks of equations" | Union-Find with weights |

### Final Advice

1. **Practice the templates** until they're muscle memory.
2. **Solve at least 30 graph problems** before any FAANG interview.
3. **Know when to use each algorithm** — don't default to BFS for everything.
4. **Read the problem twice** — graph problems often hide the graph structure.
5. **Draw small examples** — graph algorithms are visual.

---

*Master graphs and you've mastered ~25% of every coding interview at top companies. Graphs are the connective tissue of computer science — pun intended.*
