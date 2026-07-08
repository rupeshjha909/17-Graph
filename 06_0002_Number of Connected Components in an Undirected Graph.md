# Number of Connected Components in an Undirected Graph (LC 323) — Thought Process

> **Problem.** `n` nodes labeled `0 .. n-1` and a list of **undirected edges**. Count how many **connected components** the graph has — i.e., how many separate "islands" of mutually reachable nodes.
>
> **What this builds on.** It's the same edge-list → adjacency-list setup as *Find if Path Exists*, but instead of checking one source→destination pair, you **count the islands**. Two clean ways to do it: (1) the **DFS-counting** approach you already know from Number of Islands — launch a fresh DFS per unvisited node and count the launches; and (2) **Union-Find** (Disjoint Set Union), a new technique that's the natural fit for "grouping / connectivity" problems.

---

## Table of Contents

1. [Understanding the Problem](#1-understanding-the-problem)
2. [This Is "Number of Islands" on a General Graph](#2-this-is-number-of-islands-on-a-general-graph)
3. [Approach A: DFS Counting](#3-approach-a-dfs-counting)
4. [Approach B: Union-Find (the new tool)](#4-approach-b-union-find-the-new-tool)
5. [How Union-Find Works, Step by Step](#5-how-union-find-works-step-by-step)
6. [The Two Optimizations: Path Compression + Union by Rank](#6-the-two-optimizations-path-compression--union-by-rank)
7. [The Algorithm (both)](#7-the-algorithm-both)
8. [Worked Example](#8-worked-example)
9. [The Code (Java)](#9-the-code-java)
10. [DFS vs. Union-Find: When to Use Which](#10-dfs-vs-union-find-when-to-use-which)
11. [Complexity](#11-complexity)
12. [Edge Cases](#12-edge-cases)
13. [The Pattern and Its Siblings](#13-the-pattern-and-its-siblings)
14. [Common Mistakes](#14-common-mistakes)
15. [TL;DR](#15-tldr)

---

## 1. Understanding the Problem

The nodes split into groups where everyone in a group can reach everyone else (directly or through others), and different groups have no connection. Count the groups.

```
n = 5, edges = [[0,1],[1,2],[3,4]]

  0 — 1 — 2        3 — 4

component 1: {0,1,2}    component 2: {3,4}
→ answer = 2
```

A node with no edges is its own component (a group of one).

> 💡 **The mental model.** Drop the nodes on a table and connect the ones with edges. The answer is how many separate clusters you end up with — counting lone nodes as clusters of one.

---

## 2. This Is "Number of Islands" on a General Graph

You already solved this shape on a grid: **Number of Islands** counts connected blobs of land. This is the *same question* on an arbitrary graph:

| Number of Islands (grid) | This problem (graph) |
|:--|:--|
| node = land cell | node = integer label |
| neighbor = adjacent land cell | neighbor = `adj[node]` (from edges) |
| count blobs of connected land | count connected components |

The recipe is identical: walk the whole structure, and **each time you find something not yet visited, that's a new component — flood it and increment the count.** The only difference is grid-adjacency vs. an adjacency list built from edges.

> 💡 **The reusable counting trick.** To count connected components: scan all nodes; for each *unvisited* one, increment the count and DFS/BFS to mark its entire component visited. The number of "fresh launches" equals the number of components. This is exactly Number of Islands' outer loop.

---

## 3. Approach A: DFS Counting

Build the adjacency list (both directions, undirected), then scan nodes `0 .. n-1`. Each unvisited node starts a new component:

```
build adj from edges
seen = all false
count = 0
for each node i in 0..n-1:
    if not seen[i]:
        count++              // found a new component
        DFS(i): mark every node reachable from i as seen
return count
```

Every node gets marked exactly once (inside whichever component's DFS reaches it). So the number of times the outer loop has to *start* a new DFS is precisely the number of components.

This is your familiar territory — adjacency list + DFS + visited, with a counter on the outer loop.

---

## 4. Approach B: Union-Find (the new tool)

Union-Find (a.k.a. Disjoint Set Union, DSU) is a data structure built specifically for **"are these two things in the same group, and merge groups"** questions. It's the natural fit for connectivity problems and worth learning here.

The idea: each node starts in its **own** group. For every edge `[u, v]`, **merge** the groups of `u` and `v`. After processing all edges, the number of distinct groups is the answer.

```
start: 5 nodes, 5 separate groups: {0} {1} {2} {3} {4}   → components = 5
edge [0,1]: merge → {0,1} {2} {3} {4}                     → components = 4
edge [1,2]: merge {0,1} with {2} → {0,1,2} {3} {4}        → components = 3
edge [3,4]: merge → {0,1,2} {3,4}                          → components = 2
answer = 2
```

The elegant counting trick: **start `components = n`, and every time a union actually merges two *different* groups, decrement by 1.** Edges within an already-connected group don't change the count.

> 💡 **Union-Find's whole job** is answering "same group?" and "merge groups" near-instantly. It shines for connectivity/grouping problems — counting components, detecting cycles in undirected graphs, Kruskal's MST, and "accounts merge" style problems. This problem is the cleanest introduction.

---

## 5. How Union-Find Works, Step by Step

Each node has a **parent**; following parents upward leads to the group's **root** (representative). Two nodes are in the same group iff they share a root.

```
parent[] starts as: parent[i] = i      (everyone is their own root)

find(x):  follow parent[x] up until you hit a node that is its own parent → that's the root
union(a, b):  find both roots; if different, point one root at the other (merging the groups)
```

- **`find(x)`** answers "which group is x in?" — returns the root.
- **`union(a, b)`** merges: if `find(a) != find(b)`, attach one root under the other → the two groups become one.

"Same component?" becomes "`find(a) == find(b)`?" — a couple of pointer hops.

---

## 6. The Two Optimizations: Path Compression + Union by Rank

Naive Union-Find can degrade into long parent-chains (O(n) per `find`). Two standard optimizations make it nearly O(1) amortized:

**Path compression (in `find`):** while walking up to the root, point nodes directly at the root (or their grandparent), flattening the tree for next time.
```java
parent[x] = parent[parent[x]];   // halve the path on the way up
```

**Union by rank/size (in `union`):** always attach the *smaller/shorter* tree under the *taller* one, keeping trees shallow.
```java
if (rank[ra] < rank[rb]) attach ra under rb; else attach rb under ra;
```

Together they give an amortized cost of **α(n)** (inverse Ackermann) — effectively constant. You don't need to over-explain α(n) in an interview; "near-constant amortized with path compression and union by rank" suffices.

> 💡 **Why both matter.** Path compression flattens trees over time; union-by-rank keeps them from getting tall in the first place. Either alone helps; together they give the famous near-constant bound. Mentioning both is the senior-level answer.

---

## 7. The Algorithm (both)

### DFS counting
```
build adjacency list (both directions)
seen = all false; count = 0
for i in 0..n-1:
    if not seen[i]: count++; DFS(i) marks its whole component
return count
```

### Union-Find
```
parent[i] = i for all i;  components = n
for each edge [u, v]:
    if find(u) != find(v): union them; components--
return components
```

---

## 8. Worked Example

```
n = 5, edges = [[0,1],[1,2],[3,4]]
```

**DFS counting:**
```
adj: 0→[1] 1→[0,2] 2→[1] 3→[4] 4→[3]
i=0 unvisited → count=1, DFS marks {0,1,2}
i=1 visited; i=2 visited
i=3 unvisited → count=2, DFS marks {3,4}
i=4 visited
→ 2
```

**Union-Find:**
```
components = 5,  parent = [0,1,2,3,4]
edge [0,1]: find0=0, find1=1, differ → union, components=4   parent[1]=0
edge [1,2]: find1=0, find2=2, differ → union, components=3   parent[2]=0
edge [3,4]: find3=3, find4=4, differ → union, components=2   parent[4]=3
→ 2
```

Both give **2**. (Verified against each other over 20k random graphs.)

---

## 9. The Code (Java)

### Approach A — DFS counting

```java
class Solution {
    public int countComponents(int n, int[][] edges) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) {
            adj.get(e[0]).add(e[1]);
            adj.get(e[1]).add(e[0]);          // undirected → both directions
        }

        boolean[] seen = new boolean[n];
        int count = 0;
        for (int i = 0; i < n; i++) {
            if (!seen[i]) {
                count++;                       // new component
                dfs(adj, i, seen);             // mark the whole component
            }
        }
        return count;
    }

    private void dfs(List<List<Integer>> adj, int node, boolean[] seen) {
        seen[node] = true;
        for (int nb : adj.get(node)) {
            if (!seen[nb]) dfs(adj, nb, seen);
        }
    }
}
```

### Approach B — Union-Find

```java
class Solution {
    private int[] parent, rank;

    public int countComponents(int n, int[][] edges) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;   // each node starts as its own root

        int components = n;
        for (int[] e : edges) {
            if (union(e[0], e[1])) components--;      // merged two groups → one fewer component
        }
        return components;
    }

    private int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]);   // full path compression: point straight to the root
        }
        return parent[x];
    }

    private boolean union(int a, int b) {
        int ra = find(a), rb = find(b);
        if (ra == rb) return false;          // already in the same group → nothing to merge

        // Attach the SHORTER tree under the TALLER tree's root (keeps trees shallow).
        if (rank[ra] < rank[rb]) {
            parent[ra] = rb;                 // ra's tree is shorter → hang it under rb
        } else if (rank[ra] > rank[rb]) {
            parent[rb] = ra;                 // rb's tree is shorter → hang it under ra
        } else {
            parent[rb] = ra;                 // equal height → attach either way...
            rank[ra]++;                      // ...and the merged tree grows one level taller
        }
        return true;
    }
}
```

(Both verified against each other over 20k random graphs plus the LeetCode examples.)

---

## 10. DFS vs. Union-Find: When to Use Which

| | DFS / BFS counting | Union-Find |
|:--|:--|:--|
| Mental model | flood each component, count launches | merge groups as edges arrive |
| Needs adjacency list? | yes (build from edges) | **no** — works directly on the edge list |
| Best when | you also need to *traverse* (visit nodes, compute per-component data) | edges arrive **incrementally / online**, or you only need grouping |
| Dynamic connectivity ("add edge, ask connected?") | re-run traversal each query (slow) | **ideal** — near-O(1) per operation |
| Code length | a bit more (build adj + recursion) | compact, no adjacency list |

> 💡 **The deciding question.** If edges are all known up front and you might want to walk the components, DFS is natural. If connectivity is **dynamic** (edges added over time, repeated "are these connected?" queries), Union-Find is the right tool — it answers each query in near-constant time without rebuilding anything. For this static problem either is fine; Union-Find is the slicker answer and signals you know DSU.

---

## 11. Complexity

Let `V = n`, `E = edges.length`.

- **DFS:** O(V + E) time (build adjacency list O(E), traverse O(V + E)); O(V + E) space.
- **Union-Find:** O(E · α(n)) ≈ **O(E)** time (each edge does near-constant union/find with the two optimizations); O(V) space (just the `parent`/`rank` arrays — no adjacency list needed).

Union-Find uses **less space** (no adjacency list) and is asymptotically as fast.

---

## 12. Edge Cases

| Case | Result | Why |
|:-----|:-------|:----|
| No edges | n | Every node is its own component. |
| All nodes in one chain/clique | 1 | Everything connects into a single component. |
| Single node `n=1` | 1 | One lone component. |
| Duplicate edges | unchanged | A second `[u,v]` is a no-op (same group already). |
| Self-loop `[u,u]` | unchanged | `find(u)==find(u)` → no merge. |

---

## 13. The Pattern and Its Siblings

This is the **connected-components** pattern — count or group mutually reachable nodes. Two tools cover the family: DFS/BFS flood-counting, and Union-Find.

| Problem | Tool of choice | Why |
|:--------|:---------------|:----|
| **Number of Connected Components** (LC 323, this) | either | static count |
| **Number of Islands** (LC 200) | DFS/BFS | grid traversal (the grid version of this) |
| **Find if Path Exists** (LC 1971) | either | single reachability query |
| **Graph Valid Tree** (LC 261) | Union-Find | exactly n-1 edges + 1 component, no cycle |
| **Accounts Merge** (LC 721) | Union-Find | merge groups by shared email |
| **Redundant Connection** (LC 684) | Union-Find | the edge that creates a cycle |

> 💡 **DFS for traversal, Union-Find for grouping.** When the question is "explore / visit / compute over components," reach for DFS/BFS. When it's "are these merged / how many groups / which edge creates a cycle," reach for Union-Find. Many connectivity problems accept both; recognizing which is *cleaner* is the skill.

---

## 14. Common Mistakes

- ❌ **Forgetting both directions when building the adjacency list** (DFS approach) — undirected needs `adj[u].add(v)` and `adj[v].add(u)`.
- ❌ **Decrementing the component count on every edge** (Union-Find) — only decrement when a union *actually merges two different groups*; same-group edges don't change the count.
- ❌ **Skipping path compression / union by rank** — without them `find` can degrade to O(n); with them it's near-constant.
- ❌ **Counting visited nodes instead of DFS launches** (DFS approach) — the component count is the number of fresh DFS starts, not nodes visited.
- ❌ **Initializing `parent[i] = 0`** instead of `parent[i] = i` — each node must start as its own root.
- ❌ **Recursive DFS on a huge graph** — risk of stack overflow; BFS or iterative DFS is safer for large `n`.

---

## 15. TL;DR

**Problem:** count connected components of an undirected graph (n nodes + edge list).

**This is Number of Islands on a general graph** — same "scan nodes, flood each unvisited one, count the launches" trick, with an adjacency list instead of grid adjacency.

**Two approaches:**
- **DFS counting:** build adjacency list (both directions); for each unvisited node, `count++` and DFS its whole component. O(V + E).
- **Union-Find:** start `components = n`; for each edge, if its endpoints are in different groups, union them and `components--`. O(E·α(n)) ≈ O(E), and **no adjacency list needed**.

**Union-Find essentials:** `parent[i]=i` to start; `find` returns the root (with **path compression**); `union` merges roots (with **union by rank**); decrement the count only on a real merge.

**Worked:** `n=5, edges=[[0,1],[1,2],[3,4]]` → components `{0,1,2}` and `{3,4}` → **2**.

**When to use which:** DFS if you also need to traverse; **Union-Find** if connectivity is dynamic or you only need grouping (it answers "same group?" in near-constant time).

**Siblings:** Number of Islands, Find if Path Exists, Graph Valid Tree, Accounts Merge, Redundant Connection.

**One-line philosophy:**
> Counting components is counting islands on a general graph — either flood each unvisited node with DFS and tally the launches, or start with n singletons and let Union-Find merge a group every time an edge joins two that weren't already together, the leftover group count being your answer.
