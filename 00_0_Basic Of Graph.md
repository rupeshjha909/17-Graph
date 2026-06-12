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
18. [Pattern 14: Tree/Graph Construction from Flat Data (Map-and-Wire)](#18-pattern-14-treegraph-construction-from-flat-data-map-and-wire)
19. [Suggested Study Order](#19-suggested-study-order)
20. [Templates and Cheat Sheets](#20-templates-and-cheat-sheets)
21. [Interview Strategy](#21-interview-strategy)

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
| "Build nested tree from flat list?" | Map-and-Wire (Pattern 14) |

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
- Map-and-Wire: O(N) — two passes with HashMap.

---

## 4. Company Tier Definitions

### 🏢 Tier 1 — Service-based / Startups
- Companies: TCS, Infosys, Wipro, Accenture, Cognizant, mid-sized startups.
- Problem difficulty: Easy to Medium.

### 🏢 Tier 2 — Mid-tier Product Companies
- Companies: Paytm, Flipkart, Walmart, Adobe, Oracle, IBM, Cisco, Visa.
- Problem difficulty: Medium to Hard.

### 🏢 Tier 3 — FAANG / Top Product
- Companies: Google, Amazon, Microsoft, Meta, Apple, Netflix, Atlassian, Uber, LinkedIn.
- Problem difficulty: Hard, Medium-Hard.

### 🏢 Tier 4 — Top-tier Quant / Tier 3+ FAANG
- Companies: Two Sigma, Citadel, Jane Street, D.E. Shaw, Google L5+, Meta E5+.
- Problem difficulty: Hard, novel.

**Tier tags:** 🟢 Tier 1+ | 🟡 Tier 2+ | 🔴 Tier 3+ | 🟣 Tier 4+


---

## 5. Pattern 1: Grid Traversal (DFS/BFS)

### Core Idea
The grid is an implicit graph. Each cell is a node; neighbors are adjacent cells (4-directional or 8-directional).

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

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Number of Islands | Medium | 🟢 Tier 1+ | [LC 200](https://leetcode.com/problems/number-of-islands/) |
| Max Area of Island | Medium | 🟢 Tier 1+ | [LC 695](https://leetcode.com/problems/max-area-of-island/) |
| Flood Fill | Easy | 🟢 Tier 1+ | [LC 733](https://leetcode.com/problems/flood-fill/) |
| Island Perimeter | Easy | 🟢 Tier 1+ | [LC 463](https://leetcode.com/problems/island-perimeter/) |
| Number of Closed Islands | Medium | 🟡 Tier 2+ | [LC 1254](https://leetcode.com/problems/number-of-closed-islands/) |
| Surrounded Regions | Medium | 🟡 Tier 2+ | [LC 130](https://leetcode.com/problems/surrounded-regions/) |
| Making a Large Island | Hard | 🔴 Tier 3+ | [LC 827](https://leetcode.com/problems/making-a-large-island/) |
| Pacific Atlantic Water Flow | Medium | 🟡 Tier 2+ | [LC 417](https://leetcode.com/problems/pacific-atlantic-water-flow/) |

---

## 6. Pattern 2: BFS / Shortest Path in Unweighted Graph

### Core Idea
BFS explores level by level. The first time you reach a node, you've found the shortest path.

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

**Critical:** Mark visited when ENQUEUEING, not when dequeuing.

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Word Ladder | Hard | 🔴 Tier 3+ | [LC 127](https://leetcode.com/problems/word-ladder/) |
| Shortest Path in Binary Matrix | Medium | 🟡 Tier 2+ | [LC 1091](https://leetcode.com/problems/shortest-path-in-binary-matrix/) |
| Open the Lock | Medium | 🟡 Tier 2+ | [LC 752](https://leetcode.com/problems/open-the-lock/) |
| Jump Game III | Medium | 🟢 Tier 1+ | [LC 1306](https://leetcode.com/problems/jump-game-iii/) |
| Sliding Puzzle | Hard | 🔴 Tier 3+ | [LC 773](https://leetcode.com/problems/sliding-puzzle/) |
| Shortest Bridge | Medium | 🔴 Tier 3+ | [LC 934](https://leetcode.com/problems/shortest-bridge/) |

---

## 7. Pattern 3: DFS Traversal (Generic Graph)

### Template
```java
public void dfs(Map<Integer, List<Integer>> graph, int node, Set<Integer> visited) {
    if (visited.contains(node)) return;
    visited.add(node);
    for (int neighbor : graph.getOrDefault(node, new ArrayList<>())) {
        dfs(graph, neighbor, visited);
    }
}
```

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Find If Path Exists | Easy | 🟢 Tier 1+ | [LC 1971](https://leetcode.com/problems/find-if-path-exists-in-graph/) |
| Clone Graph | Medium | 🟡 Tier 2+ | [LC 133](https://leetcode.com/problems/clone-graph/) |
| All Paths from Source to Target | Medium | 🟡 Tier 2+ | [LC 797](https://leetcode.com/problems/all-paths-from-source-to-target/) |
| Number of Connected Components | Medium | 🟢 Tier 1+ | [LC 323](https://leetcode.com/problems/number-of-connected-components-in-an-undirected-graph/) |
| Reconstruct Itinerary | Hard | 🔴 Tier 3+ | [LC 332](https://leetcode.com/problems/reconstruct-itinerary/) |
| Evaluate Division | Medium | 🔴 Tier 3+ | [LC 399](https://leetcode.com/problems/evaluate-division/) |

---

## 8. Pattern 4: Cycle Detection

### Undirected Graph — DFS with parent tracking
```java
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

### Directed Graph — DFS with 3 states (0=unvisited, 1=in-progress, 2=done)
```java
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

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Course Schedule | Medium | 🟢 Tier 1+ | [LC 207](https://leetcode.com/problems/course-schedule/) |
| Graph Valid Tree | Medium | 🟡 Tier 2+ | [LC 261](https://leetcode.com/problems/graph-valid-tree/) |
| Redundant Connection | Medium | 🟡 Tier 2+ | [LC 684](https://leetcode.com/problems/redundant-connection/) |
| Redundant Connection II | Hard | 🔴 Tier 3+ | [LC 685](https://leetcode.com/problems/redundant-connection-ii/) |

---

## 9. Pattern 5: Topological Sort

### Kahn's Algorithm (BFS-based)
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
    return idx == n ? order : new int[0];  // if idx < n → cycle
}
```

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Course Schedule | Medium | 🟢 Tier 1+ | [LC 207](https://leetcode.com/problems/course-schedule/) |
| Course Schedule II | Medium | 🟡 Tier 2+ | [LC 210](https://leetcode.com/problems/course-schedule-ii/) |
| Alien Dictionary | Hard | 🔴 Tier 3+ | [LC 269](https://leetcode.com/problems/alien-dictionary/) |
| Minimum Height Trees | Medium | 🔴 Tier 3+ | [LC 310](https://leetcode.com/problems/minimum-height-trees/) |
| Parallel Courses | Medium | 🟡 Tier 2+ | [LC 1136](https://leetcode.com/problems/parallel-courses/) |

---

## 10. Pattern 6: Union-Find (Disjoint Set Union)

### Template (Path Compression + Union by Rank)
```java
class UnionFind {
    int[] parent, rank;
    int count;
    public UnionFind(int n) {
        parent = new int[n]; rank = new int[n]; count = n;
        for (int i = 0; i < n; i++) parent[i] = i;
    }
    public int find(int x) {
        if (parent[x] != x) parent[x] = find(parent[x]);  // path compression
        return parent[x];
    }
    public boolean union(int x, int y) {
        int px = find(x), py = find(y);
        if (px == py) return false;
        if (rank[px] < rank[py]) parent[px] = py;
        else if (rank[px] > rank[py]) parent[py] = px;
        else { parent[py] = px; rank[px]++; }
        count--;
        return true;
    }
}
```

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Number of Provinces | Medium | 🟢 Tier 1+ | [LC 547](https://leetcode.com/problems/number-of-provinces/) |
| Redundant Connection | Medium | 🟡 Tier 2+ | [LC 684](https://leetcode.com/problems/redundant-connection/) |
| Accounts Merge | Medium | 🔴 Tier 3+ | [LC 721](https://leetcode.com/problems/accounts-merge/) |
| Number of Islands II | Hard | 🔴 Tier 3+ | [LC 305](https://leetcode.com/problems/number-of-islands-ii/) |
| Satisfiability of Equality | Medium | 🟡 Tier 2+ | [LC 990](https://leetcode.com/problems/satisfiability-of-equality-equations/) |
| Min Cost to Connect All Points | Medium | 🟡 Tier 2+ | [LC 1584](https://leetcode.com/problems/min-cost-to-connect-all-points/) |

---

## 11. Pattern 7: Shortest Path Algorithms (Weighted)

### Dijkstra's Algorithm (non-negative weights)
```java
public int dijkstra(List<List<int[]>> graph, int n, int start, int end) {
    int[] dist = new int[n];
    Arrays.fill(dist, Integer.MAX_VALUE);
    dist[start] = 0;
    PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[1] - b[1]);
    pq.offer(new int[]{start, 0});
    while (!pq.isEmpty()) {
        int[] curr = pq.poll();
        int node = curr[0], d = curr[1];
        if (d > dist[node]) continue;
        if (node == end) return d;
        for (int[] edge : graph.get(node)) {
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

### Bellman-Ford (handles negative weights, O(VE))
### Floyd-Warshall (all-pairs, O(V³))

### When to Use Each

| Algorithm | Use When |
|-----------|----------|
| **BFS** | Unweighted graph |
| **Dijkstra** | Non-negative weights, single source |
| **Bellman-Ford** | Negative weights possible |
| **Floyd-Warshall** | All-pairs shortest paths, small V |

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Network Delay Time | Medium | 🟡 Tier 2+ | [LC 743](https://leetcode.com/problems/network-delay-time/) |
| Cheapest Flights Within K Stops | Medium | 🔴 Tier 3+ | [LC 787](https://leetcode.com/problems/cheapest-flights-within-k-stops/) |
| Path With Minimum Effort | Medium | 🔴 Tier 3+ | [LC 1631](https://leetcode.com/problems/path-with-minimum-effort/) |
| Swim in Rising Water | Hard | 🔴 Tier 3+ | [LC 778](https://leetcode.com/problems/swim-in-rising-water/) |

---

## 12. Pattern 8: Minimum Spanning Tree

### Kruskal's (Edge-based, Union-Find)
```java
public int kruskalMST(int n, int[][] edges) {
    Arrays.sort(edges, (a, b) -> a[2] - b[2]);
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

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Min Cost to Connect All Points | Medium | 🟡 Tier 2+ | [LC 1584](https://leetcode.com/problems/min-cost-to-connect-all-points/) |
| Connecting Cities With Minimum Cost | Medium | 🟡 Tier 2+ | [LC 1135](https://leetcode.com/problems/connecting-cities-with-minimum-cost/) |

---

## 13. Pattern 9: Strongly Connected Components

### Core Idea
In a directed graph, find groups where every vertex is reachable from every other. Algorithms: Kosaraju's (two DFS passes + transpose) or Tarjan's (single DFS with disc/low).

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Critical Connections (Bridges) | Hard | 🟣 Tier 4+ | [LC 1192](https://leetcode.com/problems/critical-connections-in-a-network/) |

---

## 14. Pattern 10: Bipartite Graph / Graph Coloring

### BFS Approach
```java
public boolean isBipartite(int[][] graph) {
    int n = graph.length;
    int[] color = new int[n];
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
                    return false;
                }
            }
        }
    }
    return true;
}
```

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Is Graph Bipartite? | Medium | 🟢 Tier 1+ | [LC 785](https://leetcode.com/problems/is-graph-bipartite/) |
| Possible Bipartition | Medium | 🟡 Tier 2+ | [LC 886](https://leetcode.com/problems/possible-bipartition/) |

---

## 15. Pattern 11: Multi-Source BFS

### Core Idea
Start BFS from **multiple sources simultaneously**. All sources added to queue initially.

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Rotting Oranges | Medium | 🟢 Tier 1+ | [LC 994](https://leetcode.com/problems/rotting-oranges/) |
| Walls and Gates | Medium | 🟡 Tier 2+ | [LC 286](https://leetcode.com/problems/walls-and-gates/) |
| 01 Matrix | Medium | 🟡 Tier 2+ | [LC 542](https://leetcode.com/problems/01-matrix/) |
| As Far From Land as Possible | Medium | 🔴 Tier 3+ | [LC 1162](https://leetcode.com/problems/as-far-from-land-as-possible/) |

---

## 16. Pattern 12: Backtracking on Graphs

### Core Idea
DFS where we **undo** the visited marker after exploring. Used when we need ALL paths or ALL configurations.

### Key Difference from Standard DFS
- DFS marks visited and **never unmarks** → finds reachability.
- Backtracking marks, explores, then **unmarks** → finds all configurations.

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| All Paths from Source to Target | Medium | 🟡 Tier 2+ | [LC 797](https://leetcode.com/problems/all-paths-from-source-to-target/) |
| Word Search | Medium | 🟢 Tier 1+ | [LC 79](https://leetcode.com/problems/word-search/) |
| Word Search II | Hard | 🔴 Tier 3+ | [LC 212](https://leetcode.com/problems/word-search-ii/) |
| N-Queens | Hard | 🔴 Tier 3+ | [LC 51](https://leetcode.com/problems/n-queens/) |
| Sudoku Solver | Hard | 🔴 Tier 3+ | [LC 37](https://leetcode.com/problems/sudoku-solver/) |

---

## 17. Pattern 13: Advanced Topics

### A. Bridges and Articulation Points
Tarjan's algorithm with disc/low arrays.

### B. Eulerian Paths and Circuits
Path visiting every edge exactly once. Exists iff at most 2 vertices have odd degree.

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Reconstruct Itinerary | Hard | 🔴 Tier 3+ | [LC 332](https://leetcode.com/problems/reconstruct-itinerary/) |

### C. 0-1 BFS / Deque BFS
BFS variant for graphs with edge weights 0 or 1. O(V + E).

### D. Tree Algorithms

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Diameter of Binary Tree | Medium | 🟡 Tier 2+ | [LC 543](https://leetcode.com/problems/diameter-of-binary-tree/) |
| Sum of Distances in Tree | Hard | 🟣 Tier 4+ | [LC 834](https://leetcode.com/problems/sum-of-distances-in-tree/) |


---

## 18. Pattern 14: Tree/Graph Construction from Flat Data (Map-and-Wire)

### Core Idea

Given a **flat list** where each item has an `id` and a `parentId` (or a list of edges), build a **nested tree or graph structure**. The technique: create a HashMap of `id → object`, then wire parent-child (or neighbor) relationships using the map. Because objects are **references**, wiring at any depth automatically builds the nesting — **no recursion needed for construction**.

This is NOT a traversal pattern (no DFS/BFS). It's a **construction** pattern — you're *building* the structure, not *searching* it. The existing 13 patterns all assume the graph already exists; this one builds it.

### Why It's Its Own Pattern

| Existing patterns (1-13) | This pattern (14) |
|:--|:--|
| Graph already exists → traverse/query it | **Build** the graph/tree from raw flat data |
| DFS, BFS, Union-Find, Dijkstra, etc. | HashMap + reference wiring (no DFS/BFS) |
| O(V+E) traversal | **O(N) construction** — two flat loops |

### Template: Flat List → Nested Tree

```java
class Node {
    int id;
    Integer parentId;
    String text;
    List<Node> children;

    Node(int id, Integer parentId, String text) {
        this.id = id; this.parentId = parentId; this.text = text;
        this.children = new ArrayList<>();
    }
}

public List<Node> buildTree(List<Node> items) {
    // Pass 1: map every id → its object
    Map<Integer, Node> map = new HashMap<>();
    for (Node item : items) {
        map.put(item.id, item);
    }

    // Pass 2: wire each child into its parent's children list; collect roots
    List<Node> roots = new ArrayList<>();
    for (Node item : items) {
        if (item.parentId == null) {
            roots.add(item);                             // top-level (root)
        } else {
            map.get(item.parentId).children.add(item);   // wire child → parent
        }
    }
    return roots;  // the nested tree, built entirely by references
}
```

**Two passes, O(N), no recursion.** Pass 1 ensures every node exists before wiring; Pass 2 connects children to parents.

### Template: Clone Graph (Map-and-Wire on an Existing Graph)

The same skeleton, but for cloning an existing graph (LC 133):

```java
public Node cloneGraph(Node node) {
    if (node == null) return null;
    Map<Node, Node> map = new HashMap<>();  // old → clone

    Queue<Node> queue = new LinkedList<>();
    queue.offer(node);
    map.put(node, new Node(node.val));

    while (!queue.isEmpty()) {
        Node curr = queue.poll();
        for (Node neighbor : curr.neighbors) {
            if (!map.containsKey(neighbor)) {
                map.put(neighbor, new Node(neighbor.val));
                queue.offer(neighbor);
            }
            map.get(curr).neighbors.add(map.get(neighbor));  // wire clone → clone
        }
    }
    return map.get(node);
}
```

**Same skeleton:** (1) map old nodes → new objects, (2) wire relationships using the map.

### Why References Make It Work (the Key Insight)

When you put a Node into the map and later add it to a parent's `children` list, both the map and the parent hold a **reference to the same object in memory**. So when you later add grandchildren to that node, the change is automatically visible through the parent — the nesting builds itself.

```
map.get(1).children.add(node2);     // parent holds reference to node2
map.get(2).children.add(node4);     // node2's children updated → parent sees it too
// The whole nested tree EXISTS because every reference points to the same live object
```

This is the same principle as mutating `last.end = max(...)` in the merge-intervals code — the list "sees" the update because `last` and the list entry are the same object.

### DAG Variant (Multiple Parents)

When a node can have **multiple parents** (file in multiple folders, employee with two managers), the same node appears in multiple parents' children lists. Wire the same child into each parent:

```java
for (Edge edge : edges) {                              // each edge = (childId, parentId)
    map.get(edge.parentId).children.add(map.get(edge.childId));
}
```

When printing/traversing a DAG, use a **path set** (not global visited) with **backtracking** — so the node can appear under each parent without cycle issues.

### When to Use

- "Build a nested **comment thread** from a flat API response."
- "Construct an **org chart** from employee records with `managerId`."
- "Build a **file/folder tree** from a flat list with `parentFolderId`."
- "Build a **category hierarchy** from a database table."
- "**Clone a graph**" (LC 133 — map old → new, wire cloned neighbors).
- Any time the input is flat `(id, parentId)` data and the output is a nested structure.

### Complexity

- **Time: O(N)** — two passes, each O(1) per item (HashMap put/get + list append).
- **Space: O(N)** — the map holds N entries.

No sorting, no recursion during construction, no DFS/BFS.

### Critical Pitfalls

- ❌ **Scanning for children (O(N²))** — use HashMap for O(1) parent lookup.
- ❌ **Copying objects instead of using references** — if you copy, later wiring (grandchildren) doesn't propagate.
- ❌ **Child-before-parent in input** — two-pass approach handles it (all nodes exist before wiring begins).
- ❌ **Null children list** — always initialize `children = new ArrayList<>()`.
- ❌ **Orphan parentId** — guard with `map.containsKey(parentId)` or use `putIfAbsent` placeholders.
- ❌ **Using DFS/BFS to build** — overkill; map-and-wire is O(N) with no recursion.

### Problems

| Problem | Difficulty | Tier | LeetCode Link |
|---------|-----------|------|---------------|
| Build Comment Tree | Medium | 🟢 Tier 1+ | (Common practical interview) |
| Clone Graph | Medium | 🟡 Tier 2+ | [LC 133](https://leetcode.com/problems/clone-graph/) |
| Employee Importance | Medium | 🟢 Tier 1+ | [LC 690](https://leetcode.com/problems/employee-importance/) |
| Kill Process | Medium | 🟡 Tier 2+ | [LC 582](https://leetcode.com/problems/kill-process/) |
| All Ancestors in DAG | Medium | 🟡 Tier 2+ | [LC 2192](https://leetcode.com/problems/all-ancestors-of-a-node-in-a-directed-acyclic-graph/) |
| Build Org Chart / File Tree | Medium | 🟢 Tier 1+ | (Common system design coding) |
| Nested Category Menu | Medium | 🟢 Tier 1+ | (Common frontend interview) |

### Interview Phrase

> "This is a construction problem, not a traversal. I'll use a HashMap to map ids to objects, then wire parent-child in a second pass. Because objects are references, connecting at any depth propagates automatically — O(N), no recursion."

---

## 19. Suggested Study Order

### Phase 1: Foundations (1-2 weeks) — Tier 1+

1. **Grid Traversal (DFS)**: Number of Islands (200), Max Area of Island (695), Flood Fill (733).
2. **Grid Traversal (BFS)**: Rotting Oranges (994), 01 Matrix (542).
3. **Basic Graph DFS/BFS**: Find If Path Exists (1971), Connected Components (323), Clone Graph (133).
4. **Union-Find Basics**: Number of Provinces (547).
5. **Tree/Graph Construction**: Build Comment Tree, Clone Graph (133), Employee Importance (690).

### Phase 2: Intermediate (2-3 weeks) — Tier 2+

1. **Cycle Detection**: Course Schedule (207), Graph Valid Tree (261), Redundant Connection (684).
2. **Topological Sort**: Course Schedule II (210).
3. **Bipartite Check**: Is Graph Bipartite? (785), Possible Bipartition (886).
4. **Shortest Path (Unweighted)**: Shortest Path in Binary Matrix (1091), Word Ladder (127).
5. **Multi-Source BFS**: Walls and Gates (286), As Far From Land (1162).

### Phase 3: Advanced (2-3 weeks) — Tier 3+

1. **Dijkstra**: Network Delay Time (743), Path With Min Effort (1631), Cheapest Flights (787).
2. **Advanced Union-Find**: Accounts Merge (721), Number of Islands II (305).
3. **Advanced DFS/BFS**: Word Ladder II (126), Alien Dictionary (269), Minimum Height Trees (310).
4. **MST**: Min Cost to Connect All Points (1584).

### Phase 4: Mastery (1-2 weeks) — Tier 4+

1. Critical Connections (1192), Reconstruct Itinerary (332), Sum of Distances in Tree (834).

---

## 20. Templates and Cheat Sheets

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
| **Map-and-Wire** | **Flat data → nested tree/graph** | **O(N)** | **HashMap + references** |

### Boilerplate: Adjacency List from Edges
```java
Map<Integer, List<Integer>> graph = new HashMap<>();
for (int[] edge : edges) {
    graph.computeIfAbsent(edge[0], k -> new ArrayList<>()).add(edge[1]);
    graph.computeIfAbsent(edge[1], k -> new ArrayList<>()).add(edge[0]);  // undirected
}
```

### Boilerplate: Direction Arrays
```java
int[] dx = {0, 0, 1, -1};
int[] dy = {1, -1, 0, 0};
```

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
| "Build tree from flat list/DB" | **Map-and-Wire** |
| "Clone a graph" | **Map-and-Wire (old→new)** |

---

## 21. Interview Strategy

### How to Approach a Graph Question

1. **Identify** it's a graph problem (nodes, edges, connections, grid, dependencies, or flat-data-to-tree).
2. **Clarify** — directed? weighted? cycles? max V and E?
3. **Pick** the algorithm using the pattern recognition table.
4. **Choose** representation — adjacency list (default), matrix (dense), edge list (MST), implicit (grid), HashMap (construction).
5. **Code** carefully — visited markers, edge directions, bounds checks.
6. **Test** — single node, disconnected, cycle, empty input.
7. **Discuss** complexity and optimizations.

### Top Interview Phrases

> "This is a connected components problem, so I'll use DFS (or Union-Find)."

> "Since the graph is unweighted and we want shortest path, BFS is optimal."

> "I'll use Union-Find with path compression and union-by-rank for amortized O(α(N))."

> "For non-negative weighted shortest path, Dijkstra with a min-heap gives O((V+E)log V)."

> "I'd convert this to a topological sort problem since there are dependencies."

> "This is a construction problem, not a traversal. I'll map-and-wire with a HashMap in O(N)."

### Common Interview Mistakes

1. Not asking about graph properties (directed? weighted? cycles?).
2. Wrong representation (matrix for sparse graph).
3. BFS without level tracking → loses distance info.
4. Marking visited too late → infinite loops.
5. Not handling disconnected components.
6. Confusing DFS with backtracking (when to unmark).
7. Using Dijkstra with negative edges.
8. **Using DFS/BFS to build a tree from flat data when map-and-wire is O(N) and simpler.**

---

## Bonus: Top 30 Must-Solve Graph Problems

1. [LC 200 — Number of Islands](https://leetcode.com/problems/number-of-islands/) 🟢
2. [LC 207 — Course Schedule](https://leetcode.com/problems/course-schedule/) 🟢
3. [LC 133 — Clone Graph](https://leetcode.com/problems/clone-graph/) 🟡 *(Map-and-Wire)*
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
24. [LC 690 — Employee Importance](https://leetcode.com/problems/employee-importance/) 🟢 *(Map-and-Wire)*
25. [LC 1192 — Critical Connections](https://leetcode.com/problems/critical-connections-in-a-network/) 🟣
26. [LC 332 — Reconstruct Itinerary](https://leetcode.com/problems/reconstruct-itinerary/) 🔴
27. [LC 1631 — Path with Min Effort](https://leetcode.com/problems/path-with-minimum-effort/) 🔴
28. [LC 582 — Kill Process](https://leetcode.com/problems/kill-process/) 🟡 *(Map-and-Wire)*
29. [LC 827 — Making a Large Island](https://leetcode.com/problems/making-a-large-island/) 🔴
30. [LC 947 — Most Stones Removed](https://leetcode.com/problems/most-stones-removed-with-same-row-or-column/) 🔴

---

## TL;DR: The Seven Truths of Graph Interviews

1. **Most graph problems are DFS or BFS variants** — master these first.
2. **Representation matters** — adjacency list for sparse (default), matrix for dense.
3. **BFS for unweighted shortest path, Dijkstra for non-negative weighted.**
4. **Topological sort for DAG ordering** — Kahn's BFS or DFS post-order.
5. **Union-Find for "are they connected?" queries** — almost O(1) with optimizations.
6. **Always mark visited carefully** — before recursion for DFS, before enqueue for BFS.
7. **Flat data → nested tree? Map-and-Wire** — HashMap + references, O(N), no traversal needed.

---

*Master graphs and you've mastered ~25% of every coding interview at top companies.*
