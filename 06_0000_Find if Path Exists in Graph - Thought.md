# Find if Path Exists in Graph (LC 1971) — Thought Process

> **Problem.** There are `n` nodes labeled `0 .. n-1`. You're given a list of **undirected edges** (`edges[i] = [u, v]`), a `source`, and a `destination`. Return `true` if there's a path from `source` to `destination`.
>
> **Why this is new for you.** Your past problems gave the graph as a **grid** — adjacency was implicit (a cell's neighbors are the cells around it). Here the graph is given as a raw **edge list**: just a bag of `[u, v]` pairs with no structure. The first real skill is **converting that edge list into an adjacency list** so you can traverse it. After that, it's the same DFS/BFS reachability you already know.

---

## Table of Contents

1. [Understanding the Problem](#1-understanding-the-problem)
2. [The New Part: an Edge List Isn't Traversable Yet](#2-the-new-part-an-edge-list-isnt-traversable-yet)
3. [What an Adjacency List Is](#3-what-an-adjacency-list-is)
4. [Building the Adjacency List (the key step)](#4-building-the-adjacency-list-the-key-step)
5. [Why Undirected Means "Add Both Directions"](#5-why-undirected-means-add-both-directions)
6. [Now It's Just Reachability (DFS or BFS)](#6-now-its-just-reachability-dfs-or-bfs)
7. [The Algorithm](#7-the-algorithm)
8. [Worked Example](#8-worked-example)
9. [The Code (Java) — DFS and BFS](#9-the-code-java--dfs-and-bfs)
10. [How This Compares to Grid Problems](#10-how-this-compares-to-grid-problems)
11. [Complexity](#11-complexity)
12. [Edge Cases](#12-edge-cases)
13. [The Pattern and Its Siblings](#13-the-pattern-and-its-siblings)
14. [Common Mistakes](#14-common-mistakes)
15. [TL;DR](#15-tldr)

---

## 1. Understanding the Problem

`n` nodes, some connected by undirected edges. You want to know: starting at `source`, can you walk along edges and arrive at `destination`?

```
n = 6
edges = [[0,1],[0,2],[3,5],[5,4],[4,3]]
source = 0, destination = 5

picture:
  0 — 1
  |
  2            3 — 5
               |   |
               4 — (cycle among 3,4,5)

From 0 you can reach {0,1,2}. Node 5 is in a separate component {3,4,5}.
→ no path → false
```

It's a pure **reachability** question: is `destination` in the same connected component as `source`?

> 💡 **The mental model.** The nodes form one or more "islands of connectivity" (connected components). The answer is simply: *are source and destination on the same island?*

---

## 2. The New Part: an Edge List Isn't Traversable Yet

In a grid, you never had to *build* the graph — from cell `(r,c)` you just computed neighbors `(r±1,c)`, `(r,c±1)`. The adjacency was baked into the geometry.

Here you're handed `edges = [[0,1],[0,2],[3,5],...]` — a flat list of connections with **no quick way to ask "what are node 0's neighbors?"** To find node 0's neighbors from the raw list, you'd scan *every* edge looking for ones containing 0 — O(E) per lookup, far too slow if you do it repeatedly during a traversal.

So the **first step is always: turn the edge list into an adjacency list**, a structure that answers "who are X's neighbors?" in O(1). This build step is the part that's genuinely new compared to grid problems.

> 💡 **The principle.** Before you can traverse a graph, you need it in a form where "give me this node's neighbors" is cheap. An edge list isn't that form; an adjacency list is. Converting edge-list → adjacency-list is the standard opening move for almost every non-grid graph problem.

---

## 3. What an Adjacency List Is

An adjacency list maps **each node → the list of nodes it's directly connected to.**

```
For edges [[0,1],[0,2],[3,5],[5,4],[4,3]]:

  0 → [1, 2]
  1 → [0]
  2 → [0]
  3 → [5, 4]
  4 → [5, 3]
  5 → [3, 4]
```

Now "what are 0's neighbors?" is a single lookup: `adj[0] = [1, 2]`. In Java this is typically `List<List<Integer>>` or `Map<Integer, List<Integer>>`.

This is the **grid analogy made explicit**: in a grid, `adj[(r,c)]` would conceptually be "the up/down/left/right cells," but you compute it instead of storing it. For a general graph there's no formula — you must store the neighbor lists.

---

## 4. Building the Adjacency List (the key step)

Create an empty list per node, then walk the edges and record each connection:

```
adj = array of n empty lists
for each edge [u, v]:
    adj[u].add(v)
    adj[v].add(u)        // undirected → record BOTH directions
```

That's it — one pass over the edges, O(E) time. After this, the graph is ready to traverse.

```java
List<List<Integer>> adj = new ArrayList<>();
for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
for (int[] e : edges) {
    adj.get(e[0]).add(e[1]);
    adj.get(e[1]).add(e[0]);   // both ways, because edges are undirected
}
```

---

## 5. Why Undirected Means "Add Both Directions"

The edges are **undirected** — `[0,1]` means 0 and 1 are mutually connected; you can walk `0→1` *and* `1→0`. The adjacency list only stores one-way links, so for an undirected edge you must add **both**:

```
edge [0,1]  →  adj[0].add(1)   AND   adj[1].add(0)
```

If you only added `adj[0].add(1)`, you could go from 0 to 1 but not back — that would model a *directed* edge, giving wrong answers for this undirected problem.

> 💡 **Directed vs. undirected — the one-line difference.** Undirected edge `[u,v]` → add both `u→v` and `v→u`. Directed edge `u→v` → add only `u→v`. Forgetting the second add in an undirected problem is a classic bug. (For a directed problem like Course Schedule, you'd add only one direction.)

---

## 6. Now It's Just Reachability (DFS or BFS)

Once the adjacency list exists, this is the **same reachability traversal** you ran on grids — only the neighbor source changed (a stored list instead of computed offsets):

- Start at `source`, mark it visited.
- Explore neighbors (`adj[node]`), skipping visited ones.
- If you ever reach `destination`, return `true`.
- If the traversal finishes without reaching it, return `false`.

DFS or BFS both work — it's a yes/no reachability question, not shortest path, so either is fine.

> 💡 **The visited array is still essential.** Graphs have cycles (here, `3↔4↔5`); without marking visited you'd loop forever. Same role as in every grid traversal.

---

## 7. The Algorithm

```
if source == destination: return true        // trivially reachable

build adjacency list from edges (both directions, undirected)

seen = all false
DFS or BFS from source:
    mark node visited
    for each neighbor in adj[node]:
        if neighbor == destination: return true
        if not seen[neighbor]: recurse/enqueue
return false                                  // never reached destination
```

---

## 8. Worked Example

```
n = 6, edges = [[0,1],[0,2],[3,5],[5,4],[4,3]], source = 0, destination = 5
```

**Build adjacency list:**
```
0 → [1, 2]
1 → [0]
2 → [0]
3 → [5, 4]
4 → [5, 3]
5 → [3, 4]
```

**BFS from 0:**
```
queue: [0], seen{0}
pop 0 → neighbors 1,2 → neither is 5 → enqueue, seen{0,1,2}
pop 1 → neighbor 0 (seen) → nothing new
pop 2 → neighbor 0 (seen) → nothing new
queue empty → never reached 5 → return false
```

Node 5 lives in the component `{3,4,5}`, disconnected from `{0,1,2}` → **false**. (LeetCode example 2, verified.)

For `n=3, edges=[[0,1],[1,2],[2,0]], source=0, dest=2`: the adjacency list connects all three in a triangle, BFS from 0 reaches 2 → **true** (verified).

---

## 9. The Code (Java) — DFS and BFS

### DFS

```java
class Solution {
    public boolean validPath(int n, int[][] edges, int source, int destination) {
        if (source == destination) return true;

        // build adjacency list
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) {
            adj.get(e[0]).add(e[1]);
            adj.get(e[1]).add(e[0]);          // undirected → both directions
        }

        boolean[] seen = new boolean[n];
        return dfs(adj, source, destination, seen);
    }

    private boolean dfs(List<List<Integer>> adj, int node, int dest, boolean[] seen) {
        if (node == dest) return true;
        seen[node] = true;
        for (int nb : adj.get(node)) {
            if (!seen[nb] && dfs(adj, nb, dest, seen)) return true;
        }
        return false;
    }
}
```

### BFS (no recursion — safer for large graphs)

```java
class Solution {
    public boolean validPath(int n, int[][] edges, int source, int destination) {
        if (source == destination) return true;

        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) {
            adj.get(e[0]).add(e[1]);
            adj.get(e[1]).add(e[0]);
        }

        boolean[] seen = new boolean[n];
        Queue<Integer> queue = new ArrayDeque<>();
        queue.offer(source);
        seen[source] = true;

        while (!queue.isEmpty()) {
            int node = queue.poll();
            for (int nb : adj.get(node)) {
                if (nb == destination) return true;
                if (!seen[nb]) {
                    seen[nb] = true;
                    queue.offer(nb);
                }
            }
        }
        return false;
    }
}
```

(Both verified against a union-find oracle over 20k random graphs plus the LeetCode examples.)

> 💡 **Why BFS is often preferred here:** a long chain graph (`0-1-2-...-100000`) would recurse 100k deep with DFS and risk a stack overflow. BFS is iterative, so it sidesteps that. For correctness either works; for robustness on large `n`, BFS (or iterative DFS with an explicit stack).

---

## 10. How This Compares to Grid Problems

The traversal is identical to your grid DFS/BFS; only **where neighbors come from** changed:

| | Grid problems (what you've done) | This (edge-list graph) |
|:--|:--|:--|
| Nodes | grid cells `(r,c)` | integer labels `0..n-1` |
| Neighbors | **computed**: `(r±1,c), (r,c±1)` via `dx/dy` | **stored**: `adj[node]`, built from edges |
| Setup needed | none — adjacency is implicit | **build the adjacency list first** |
| Visited | `boolean[][]` or mark the grid | `boolean[n]` |
| Traversal | DFS/BFS | identical DFS/BFS |

> 💡 **The only genuinely new skill** is the build step: convert the edge list into an adjacency list (remembering both directions for undirected edges). Everything after that — visited array, DFS/BFS, reachability check — is exactly what you already do on grids. A grid is just a graph whose adjacency you never had to build.

---

## 11. Complexity

Let `V = n` (nodes) and `E = edges.length`.

- **Time: O(V + E)** — building the adjacency list is O(E); the DFS/BFS visits each node once (O(V)) and traverses each edge at most twice for undirected (O(E)). Total O(V + E).
- **Space: O(V + E)** — the adjacency list stores O(E) neighbor entries (×2 for undirected) plus O(V) node lists; the visited array and queue/stack add O(V).

> 💡 **O(V + E) is the signature cost of graph traversal.** Whenever you build an adjacency list and DFS/BFS it, this is your complexity. Stating it crisply signals you understand the representation.

---

## 12. Edge Cases

| Case | Result | Why |
|:-----|:-------|:----|
| `source == destination` | true | Trivially reachable; check first. |
| No edges, `source != destination` | false | Nothing connects them. |
| Self-evident single node `n=1, source=dest=0` | true | Same node. |
| Disconnected components | false if in different ones | BFS/DFS stays within source's component. |
| Cycle in the graph | handled | Visited array prevents infinite loops. |
| Multiple edges between same pair | handled | Extra entries in `adj`; visited dedups during traversal. |

---

## 13. The Pattern and Its Siblings

This is **graph reachability on an explicit adjacency list** — the bread-and-butter setup for non-grid graph problems. The recipe: build adjacency list → DFS/BFS → check reachability.

| Problem | Graph given as | Question |
|:--------|:---------------|:---------|
| **Find if Path Exists** (LC 1971, this) | undirected edge list | source → destination reachable? |
| **Number of Connected Components** (LC 323) | undirected edge list | count the components |
| **Clone Graph** (LC 133) | adjacency (node refs) | deep-copy the graph |
| **Course Schedule** (LC 207) | **directed** edge list | any cycle? (topological) |
| **Number of Islands** (LC 200) | grid (implicit) | count components (grid version of this) |

> 💡 **The unifying recipe for adjacency-list problems.** (1) Build `adj` from the edges — both directions if undirected, one if directed. (2) Pick DFS or BFS. (3) Track visited to handle cycles. (4) Answer the question (reachable? count components? cycle?). Almost every non-grid graph problem starts with these exact four steps; only step 4 varies.

---

## 14. Common Mistakes

- ❌ **Traversing the raw edge list directly** — finding a node's neighbors becomes O(E) each time; build an adjacency list first for O(1) neighbor lookups.
- ❌ **Forgetting to add both directions for undirected edges** — `adj[u].add(v)` *and* `adj[v].add(u)`; omitting the second makes it directed and breaks reachability.
- ❌ **No visited array** — cycles (`3↔4↔5`) cause infinite loops; always track visited.
- ❌ **Forgetting `source == destination`** — that's trivially true; handle before traversing.
- ❌ **Deep recursion on a long chain** — DFS can stack-overflow for large `n`; prefer BFS or iterative DFS.
- ❌ **Sizing the adjacency list wrong** — it has `n` node-lists (one per label `0..n-1`), independent of how many edges there are.

---

## 15. TL;DR

**Problem:** `n` nodes, undirected edge list; is there a path from `source` to `destination`?

**The new skill:** the graph comes as a raw **edge list**, which you can't traverse directly. **Build an adjacency list first** (`adj[node] = its neighbors`), adding **both directions** for each undirected edge.

**Then it's familiar:** reachability via DFS or BFS with a visited array — the same traversal as your grid problems, just with neighbors read from `adj[node]` instead of computed from `dx/dy`.

**Algorithm (O(V + E)):**
```
if source == destination: return true
build adj from edges (both directions)
DFS/BFS from source, marking visited:
    if you reach destination → true
queue/stack empties → false
```

**Worked:** `n=6, edges=[[0,1],[0,2],[3,5],[5,4],[4,3]]`, 0→5 → 5 is in a different component → **false**.

**The recipe for adjacency-list problems:** build adj (both directions if undirected) → DFS/BFS → track visited → answer the question.

**Siblings:** Number of Connected Components, Clone Graph, Course Schedule (directed) — all start by building an adjacency list.

**One-line philosophy:**
> A grid handed you the adjacency for free; an edge list doesn't — so the first move is always to build an adjacency list (both directions for undirected edges), and once "what are this node's neighbors?" is a cheap lookup, it's the same visited-guarded DFS/BFS reachability you already know.
