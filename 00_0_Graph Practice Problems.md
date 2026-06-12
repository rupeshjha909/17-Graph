# Graph Practice Problems — Complete Roadmap

A curated practice list for the entire graph series, organized by **technique**. Each problem links to LeetCode and maps to the thought-process doc that covers its core idea. Work top-to-bottom within a section — each builds on the last.

---

## Table of Contents

1. [Grid Traversal (DFS / BFS Flood Fill)](#1-grid-traversal-dfs--bfs-flood-fill)
2. [Implicit & State-Graph BFS (Shortest Path)](#2-implicit--state-graph-bfs-shortest-path)
3. [Adjacency-List Build + Traversal](#3-adjacency-list-build--traversal)
4. [Union-Find (Connectivity & Undirected Cycles)](#4-union-find-connectivity--undirected-cycles)
5. [Directed Cycle Detection & Topological Sort](#5-directed-cycle-detection--topological-sort)
6. [Cycle Reconstruction & Enumeration](#6-cycle-reconstruction--enumeration)
7. [Minimum Spanning Tree & Shortest Path (Weighted)](#7-minimum-spanning-tree--shortest-path-weighted)
8. [Advanced (SCC, Bridges, Articulation, Bipartite)](#8-advanced-scc-bridges-articulation-bipartite)
9. [Suggested Study Order](#9-suggested-study-order)
10. [Classification Cheat Sheet](#10-classification-cheat-sheet)

---

## 1. Grid Traversal (DFS / BFS Flood Fill)

A grid is a graph with implicit 4- or 8-directional adjacency. Flood fill, component counting, and multi-source BFS.

| Problem | Difficulty | Link |
|:--------|:-----------|:-----|
| Number of Islands | Medium | [LC 200](https://leetcode.com/problems/number-of-islands/) |
| Flood Fill | Easy | [LC 733](https://leetcode.com/problems/flood-fill/) |
| Max Area of Island | Medium | [LC 695](https://leetcode.com/problems/max-area-of-island/) |
| Number of Closed Islands | Medium | [LC 1254](https://leetcode.com/problems/number-of-closed-islands/) |
| Surrounded Regions | Medium | [LC 130](https://leetcode.com/problems/surrounded-regions/) |
| Pacific Atlantic Water Flow | Medium | [LC 417](https://leetcode.com/problems/pacific-atlantic-water-flow/) |
| Making A Large Island | Hard | [LC 827](https://leetcode.com/problems/making-a-large-island/) |
| Island Perimeter | Easy | [LC 463](https://leetcode.com/problems/island-perimeter/) |
| Number of Enclaves | Medium | [LC 1020](https://leetcode.com/problems/number-of-enclaves/) |
| 01 Matrix | Medium | [LC 542](https://leetcode.com/problems/01-matrix/) |
| Rotting Oranges | Medium | [LC 994](https://leetcode.com/problems/rotting-oranges/) |
| Walls and Gates | Medium | [LC 286](https://leetcode.com/problems/walls-and-gates/) |
| Shortest Bridge | Medium | [LC 934](https://leetcode.com/problems/shortest-bridge/) |

---

## 2. Implicit & State-Graph BFS (Shortest Path)

"Shortest / fewest moves" on an unweighted graph → BFS. The graph is often hidden (array indices, lock dials, board configurations).

| Problem | Difficulty | Link |
|:--------|:-----------|:-----|
| Shortest Path in Binary Matrix | Medium | [LC 1091](https://leetcode.com/problems/shortest-path-in-binary-matrix/) |
| Jump Game III | Medium | [LC 1306](https://leetcode.com/problems/jump-game-iii/) |
| Jump Game IV | Hard | [LC 1345](https://leetcode.com/problems/jump-game-iv/) |
| Open the Lock | Medium | [LC 752](https://leetcode.com/problems/open-the-lock/) |
| Sliding Puzzle | Hard | [LC 773](https://leetcode.com/problems/sliding-puzzle/) |
| Word Ladder | Hard | [LC 127](https://leetcode.com/problems/word-ladder/) |
| Minimum Genetic Mutation | Medium | [LC 433](https://leetcode.com/problems/minimum-genetic-mutation/) |
| Bus Routes | Hard | [LC 815](https://leetcode.com/problems/bus-routes/) |

---

## 3. Adjacency-List Build + Traversal

Given an edge list, build an adjacency list, then DFS/BFS. Reachability, all-paths, cloning.

| Problem | Difficulty | Link |
|:--------|:-----------|:-----|
| Find if Path Exists in Graph | Easy | [LC 1971](https://leetcode.com/problems/find-if-path-exists-in-graph/) |
| All Paths From Source to Target | Medium | [LC 797](https://leetcode.com/problems/all-paths-from-source-to-target/) |
| Clone Graph | Medium | [LC 133](https://leetcode.com/problems/clone-graph/) |
| Keys and Rooms | Medium | [LC 841](https://leetcode.com/problems/keys-and-rooms/) |
| Reorder Routes to Make All Paths Lead to City Zero | Medium | [LC 1466](https://leetcode.com/problems/reorder-routes-to-make-all-paths-lead-to-the-city-zero/) |

---

## 4. Union-Find (Connectivity & Undirected Cycles)

Disjoint Set Union: "same group?", "how many groups?", "which edge closes a cycle?". The undirected-connectivity workhorse.

| Problem | Difficulty | Link |
|:--------|:-----------|:-----|
| Number of Connected Components | Medium | [LC 323](https://leetcode.com/problems/number-of-connected-components-in-an-undirected-graph/) |
| Graph Valid Tree | Medium | [LC 261](https://leetcode.com/problems/graph-valid-tree/) |
| Redundant Connection | Medium | [LC 684](https://leetcode.com/problems/redundant-connection/) |
| Number of Provinces | Medium | [LC 547](https://leetcode.com/problems/number-of-provinces/) |
| Accounts Merge | Medium | [LC 721](https://leetcode.com/problems/accounts-merge/) |
| Satisfiability of Equality Equations | Medium | [LC 990](https://leetcode.com/problems/satisfiability-of-equality-equations/) |
| Number of Operations to Make Network Connected | Medium | [LC 1319](https://leetcode.com/problems/number-of-operations-to-make-network-connected/) |
| Number of Islands II | Hard | [LC 305](https://leetcode.com/problems/number-of-islands-ii/) |
| Largest Component Size by Common Factor | Hard | [LC 952](https://leetcode.com/problems/largest-component-size-by-common-factor/) |

---

## 5. Directed Cycle Detection & Topological Sort

Directed graphs: cycle detection via 3-state coloring or Kahn's; topological ordering for dependency problems.

| Problem | Difficulty | Link |
|:--------|:-----------|:-----|
| Course Schedule | Medium | [LC 207](https://leetcode.com/problems/course-schedule/) |
| Course Schedule II | Medium | [LC 210](https://leetcode.com/problems/course-schedule-ii/) |
| Find Eventual Safe States | Medium | [LC 802](https://leetcode.com/problems/find-eventual-safe-states/) |
| Parallel Courses | Medium | [LC 1136](https://leetcode.com/problems/parallel-courses/) |
| Course Schedule IV | Medium | [LC 1462](https://leetcode.com/problems/course-schedule-iv/) |
| Sequence Reconstruction | Medium | [LC 444](https://leetcode.com/problems/sequence-reconstruction/) |
| Alien Dictionary | Hard | [LC 269](https://leetcode.com/problems/alien-dictionary/) |
| Minimum Height Trees | Medium | [LC 310](https://leetcode.com/problems/minimum-height-trees/) |
| Redundant Connection II | Hard | [LC 685](https://leetcode.com/problems/redundant-connection-ii/) |
| Strange Printer II | Hard | [LC 1591](https://leetcode.com/problems/strange-printer-ii/) |

> **Note on directionality:** LC 685 (Redundant Connection **II**) and LC 802 (Find Eventual Safe States) are **directed**-graph problems — keep them in this section, not with the undirected Union-Find problems.

---

## 6. Cycle Reconstruction & Enumeration

Beyond detection: return the actual cycle, all cycles, or the shortest cycle. Mostly concept-level — the techniques appear inside harder problems above.

| Topic | Technique |
|:------|:----------|
| Find one cycle (directed) | 3-state DFS + parent pointers, walk back |
| Find one cycle (undirected) | parent-tracking DFS + parent pointers |
| Find all cycles (directed) | DFS backtracking + min-start dedup; Johnson's algorithm |
| Find all cycles (undirected) | backtracking + rotation/direction dedup; cycle basis (E−V+comp) |
| Shortest cycle / girth (undirected) | BFS from every vertex; `dist[u]+dist[w]+1` |

---

## 7. Minimum Spanning Tree & Shortest Path (Weighted)

Weighted graphs: MST (Kruskal/Prim) and weighted shortest path (Dijkstra/Bellman-Ford).

| Problem | Difficulty | Link |
|:--------|:-----------|:-----|
| Min Cost to Connect All Points | Medium | [LC 1584](https://leetcode.com/problems/min-cost-to-connect-all-points/) |
| Connecting Cities With Minimum Cost | Medium | [LC 1135](https://leetcode.com/problems/connecting-cities-with-minimum-cost/) |
| Network Delay Time | Medium | [LC 743](https://leetcode.com/problems/network-delay-time/) |
| Path With Minimum Effort | Medium | [LC 1631](https://leetcode.com/problems/path-with-minimum-effort/) |
| Cheapest Flights Within K Stops | Medium | [LC 787](https://leetcode.com/problems/cheapest-flights-within-k-stops/) |
| Swim in Rising Water | Hard | [LC 778](https://leetcode.com/problems/swim-in-rising-water/) |
| Find Critical and Pseudo-Critical Edges in MST | Hard | [LC 1489](https://leetcode.com/problems/find-critical-and-pseudo-critical-edges-in-minimum-spanning-tree/) |

---

## 8. Advanced (SCC, Bridges, Articulation, Bipartite)

Senior-level structure using Tarjan's low-link / DFS timestamps, and 2-coloring.

| Problem | Difficulty | Link |
|:--------|:-----------|:-----|
| Critical Connections in a Network (bridges) | Hard | [LC 1192](https://leetcode.com/problems/critical-connections-in-a-network/) |
| Is Graph Bipartite? | Medium | [LC 785](https://leetcode.com/problems/is-graph-bipartite/) |
| Possible Bipartition | Medium | [LC 886](https://leetcode.com/problems/possible-bipartition/) |
| Minimum Number of Days to Disconnect Island | Hard | [LC 1568](https://leetcode.com/problems/minimum-number-of-days-to-disconnect-island/) |
| Tree Diameter | Medium | [LC 1245](https://leetcode.com/problems/tree-diameter/) |

> **Concept-level (no single LC number):** Strongly Connected Components (Tarjan's / Kosaraju's) — underpins Johnson's all-cycles; Articulation Points (Tarjan's low-link) — cut vertices.

---

## 9. Suggested Study Order

A dependency-respecting path through the sections:

```
1. Grids (§1):        200 → 733 → 1254 → 130 → 417 → 994 → 934
2. BFS shortest (§2): 1091 → 1306 → 752 → 773 → 127
3. Adjacency (§3):    1971 → 797 → 133 → 841
4. Union-Find (§4):   323 → 261 → 684 → 547 → 721 → 305
5. Directed/topo (§5):207 → 210 → 802 → 1136 → 269 → 685
6. MST/weighted (§7): 1584 → 743 → 787 → 778
7. Advanced (§8):     785 → 1192 → SCC (Tarjan/Kosaraju) → articulation points
```

Sections 1–5 cover roughly 90% of interview graph questions. Section 6 (cycle reconstruction/enumeration) is conceptual depth. Sections 7–8 are the senior differentiators.

---

## 10. Classification Cheat Sheet

Pick the technique from the *phrasing* of the problem:

| The problem says… | Reach for… | Section |
|:------------------|:-----------|:--------|
| "count islands / regions / components" | DFS/BFS flood fill, or Union-Find | §1, §4 |
| "shortest / fewest steps" (unweighted) | BFS (single- or multi-source) | §1, §2 |
| "can we reach / does a path exist" | DFS or BFS reachability | §2, §3 |
| "all paths / all ways" | DFS backtracking | §3 |
| "same group? / how many groups? / merge" | Union-Find | §4 |
| "is it a tree / one cycle / redundant edge" (undirected) | Union-Find | §4 |
| "order / can-finish / prerequisites / dependencies" | topological sort (directed) | §5 |
| "detect a cycle" (directed) | 3-state DFS or Kahn's | §5 |
| "minimum cost to connect everything" | MST (Kruskal/Prim) | §7 |
| "shortest path with weights" | Dijkstra / Bellman-Ford | §7 |
| "remove an edge/vertex to disconnect" | bridges / articulation points | §8 |
| "two groups / 2-color / no odd cycle" | bipartite check (BFS/DFS 2-coloring) | §8 |

> 💡 **The rule of thumb:** *undirected + "connected / same group / one cycle"* → Union-Find. *directed + "order / dependencies"* → topological sort. *"remove to disconnect"* → bridges/articulation. Getting the family right tells you the tool before you write a line.
