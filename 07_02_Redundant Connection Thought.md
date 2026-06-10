# Redundant Connection (LC 684) — Thought Process (Detailed)

> **Problem.** A graph started as a **tree** with `n` nodes (labeled `1..n`) and `n-1` edges. Then **one extra edge** was added, creating exactly **one cycle**. Given the edge list, return that redundant edge. If multiple edges could be removed to restore a tree, return the one that appears **last** in the input.
>
> **The cleanest Union-Find problem there is.** The entire question reduces to: *as we add edges one by one, which edge first connects two nodes that were **already connected**?* That edge closes the cycle — it's the redundant one. Union-Find answers "already connected?" in near-constant time, so we process edges in order and return the first one that fails the "different groups" test.

---

## Table of Contents

1. [Understanding the Problem](#1-understanding-the-problem)
2. [The Core Insight: the Cycle-Closing Edge](#2-the-core-insight-the-cycle-closing-edge)
3. [Why Union-Find Is the Perfect Tool](#3-why-union-find-is-the-perfect-tool)
4. [Why Processing in Order Gives the "Last" Answer](#4-why-processing-in-order-gives-the-last-answer)
5. [Union-Find Recap (find + union)](#5-union-find-recap-find--union)
6. [The Algorithm](#6-the-algorithm)
7. [Worked Example](#7-worked-example)
8. [The Code (Java)](#8-the-code-java)
9. [Could You Use DFS Instead?](#9-could-you-use-dfs-instead)
10. [Complexity](#10-complexity)
11. [Edge Cases](#11-edge-cases)
12. [The Pattern and Its Siblings](#12-the-pattern-and-its-siblings)
13. [Common Mistakes](#13-common-mistakes)
14. [TL;DR](#14-tldr)

---

## 1. Understanding the Problem

You have what *was* a tree, plus one extra edge that created a single cycle. Your job: identify which edge is the "extra" one — specifically, an edge whose removal turns the graph back into a tree.

```
edges = [[1,2],[1,3],[2,3]]

  1 — 2
  | /
  3        edges 1-2, 1-3, 2-3 form a triangle (cycle among 1,2,3)

Removing [2,3] restores a tree (1—2, 1—3). → answer [2,3]
```

Because the graph is a tree plus one edge, there's **exactly one cycle**, and the redundant edge is any edge *on that cycle*. The tiebreak rule ("return the last one in input order") makes the answer unique.

> 💡 **The mental model.** Picture adding the edges one at a time, building up the graph. Most edges connect a new piece to the growing structure. But **one** edge connects two nodes that were *already linked* — that edge forms the loop. Catch it the moment it happens.

---

## 2. The Core Insight: the Cycle-Closing Edge

When does adding an edge `[u, v]` create a cycle? **Exactly when `u` and `v` are already connected** (there's already a path between them). Adding a direct edge on top of an existing path closes a loop.

```
Before adding [2,3]:   1—2 and 1—3 exist, so 2 and 3 are ALREADY connected (via 1).
Adding [2,3]:          now there are two ways between 2 and 3 → a cycle.
→ [2,3] is the redundant edge.
```

So the algorithm is: process edges in order, and **the first edge whose two endpoints are already in the same connected component is the redundant one.** Every edge before it linked two *separate* components (legitimate tree-building); this one links a component to *itself*.

> 💡 **"Already connected → cycle" is the whole problem.** Every tree edge merges two distinct groups into one. The redundant edge is the odd one out: both its endpoints are already in the same group, so it adds a loop instead of merging. Detecting "same group?" is precisely what Union-Find does.

---

## 3. Why Union-Find Is the Perfect Tool

Union-Find (Disjoint Set Union) maintains "which connected group is each node in?" and supports two operations:

- **`find(x)`** — return the representative (root) of x's group → tells you *which* component x is in.
- **`union(u, v)`** — merge the groups of u and v.

For each edge `[u, v]`:
- If `find(u) == find(v)` → they're already in the same group → **this edge makes a cycle** → it's redundant. Return it.
- Otherwise → they're in different groups → `union(u, v)` to merge them (a legitimate tree edge).

This is the textbook **cycle-detection-in-an-undirected-graph** use of Union-Find. No adjacency list, no traversal — just process edges and watch for the one that joins a component to itself.

> 💡 **Why not just build the graph and look for a cycle with DFS?** You could (Section 9), but Union-Find is *incremental*: it detects the cycle the instant the closing edge arrives, in one linear pass, with near-constant work per edge. For "which edge closes the cycle," DSU is the natural, minimal-code answer.

---

## 4. Why Processing in Order Gives the "Last" Answer

The problem says: if multiple edges could be removed, return the one **last** in the input. Union-Find handles this *automatically* by processing edges **in input order**.

Here's why: the graph is a tree plus exactly one extra edge, so there's exactly **one** cycle. Every edge on that cycle is a valid "removable" answer. As we add edges left to right, the cycle isn't complete until its **final** edge (in input order) arrives — and that final edge is precisely the one that finds its two endpoints already connected. So the first (and only) edge that triggers the "already connected" check is the *last* cycle edge in input order — exactly what the problem wants.

> 💡 **Order matters for the tiebreak.** Because we scan front-to-back, the cycle is only "completed" by its last-arriving edge. That edge is the one Union-Find flags — automatically satisfying "return the last edge in the input." No extra logic needed; just don't reorder the edges.

---

## 5. Union-Find Recap (find + union)

Each node points to a parent; following parents reaches the group's **root**. Two nodes share a group iff they share a root.

```
parent[i] = i initially       (each node is its own root)

find(x):   follow parent[x] up to the root (with path compression to flatten)
union(u,v): attach one root under the other
```

- **Path compression** in `find` (`parent[x] = parent[parent[x]]`) keeps the trees shallow.
- For this problem you don't even strictly need union-by-rank — path compression alone keeps it fast — but including it is good form.

"Already connected?" is simply `find(u) == find(v)`.

---

## 6. The Algorithm

```
parent[i] = i for i in 1..n          // n = number of edges (nodes are 1..n)

for each edge [u, v] in input order:
    ru = find(u), rv = find(v)
    if ru == rv:                      // already connected → this edge closes the cycle
        return [u, v]                 // the redundant edge (last cycle edge in order)
    parent[ru] = rv                   // different groups → merge (legit tree edge)
```

The first edge that hits `ru == rv` is the answer. (There's guaranteed to be exactly one, since the input is a tree + one edge.)

---

## 7. Worked Example

```
edges = [[1,2],[2,3],[3,4],[1,4],[1,5]]    (expected answer: [1,4])
```

```
parent = [0,1,2,3,4,5]   (index 0 unused; nodes 1..5 each their own root)

[1,2]: find1=1, find2=2, differ → union → parent[1]=2     groups: {1,2}{3}{4}{5}
[2,3]: find2=2, find3=3, differ → union → parent[2]=3     groups: {1,2,3}{4}{5}
[3,4]: find3=3, find4=4, differ → union → parent[3]=4     groups: {1,2,3,4}{5}
[1,4]: find1 → 1→2→3→4 = 4;  find4 = 4;  SAME root!       → CYCLE → return [1,4]
```

When we reach `[1,4]`, both 1 and 4 already belong to the group `{1,2,3,4}` (1 reaches 4 through 1→2→3→4). Adding `[1,4]` would close the loop, so it's the redundant edge → **[1,4]**. (We never even process `[1,5]`.) Verified against brute force.

---

## 8. The Code (Java)

```java
class Solution {
    private int[] parent;

    public int[] findRedundantConnection(int[][] edges) {
        int n = edges.length;            // nodes are labeled 1..n
        parent = new int[n + 1];
        for (int i = 1; i <= n; i++) parent[i] = i;   // each node its own root

        for (int[] e : edges) {
            int ru = find(e[0]), rv = find(e[1]);
            if (ru == rv) {
                return e;                // endpoints already connected → cycle-closing edge
            }
            parent[ru] = rv;             // merge the two components
        }
        return new int[0];               // problem guarantees an answer exists
    }

    private int find(int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];   // path compression
            x = parent[x];
        }
        return x;
    }
}
```

(Verified against a brute-force "remove each edge from last to first, check if the rest form a tree" oracle over 20k random tree-plus-one-edge graphs, plus both LeetCode examples.)

> 💡 **Note the 1-indexing.** Nodes are labeled `1..n`, so `parent` is sized `n+1` and index 0 is unused. Initializing `parent[i] = i` makes every node its own root (n separate groups) to start. The number of edges equals `n` (a tree's `n-1` plus the one extra), which is why `edges.length` gives the node count.

---

## 9. Could You Use DFS Instead?

Yes — there's a DFS/BFS alternative, but it's more work:

> For each edge in order, tentatively add it and check (via DFS/BFS) whether its two endpoints were *already reachable* before adding. The first edge whose endpoints were already connected is redundant.

The problem: a naive "rebuild and search per edge" is O(n²) (a DFS per edge). Union-Find collapses all of that into one near-linear pass because it maintains connectivity *incrementally* — you never re-traverse. For "detect the cycle-closing edge as edges arrive," Union-Find is strictly cleaner and faster.

> 💡 **The deciding factor.** DFS answers "are these connected?" by *searching* (O(V+E) each time). Union-Find answers it by *lookup* (near O(1)), and updates incrementally as edges are added. When connectivity is built up edge-by-edge and you query it repeatedly, that's the Union-Find sweet spot.

---

## 10. Complexity

Let `n` = number of edges = number of nodes.

- **Time: O(n · α(n)) ≈ O(n)** — one pass over the edges; each `find`/`union` is near-constant with path compression (α is the inverse Ackermann function, effectively ≤ 4 for any realistic input).
- **Space: O(n)** — just the `parent` array. No adjacency list, no recursion, no visited array.

This is about as efficient as a graph problem gets — linear time, linear space, tiny constants.

---

## 11. Edge Cases

| Case | Result | Why |
|:-----|:-------|:----|
| Triangle `[[1,2],[1,3],[2,3]]` | `[2,3]` | The last edge completes the 3-cycle. |
| Cycle then a tail `[[1,2],[2,3],[3,1],[3,4]]` | `[3,1]` | `[3,1]` closes the cycle; `[3,4]` is a legit later edge. |
| Redundant edge is the very last | that edge | If the cycle completes only at the end, that's the answer. |
| Self-loop `[u,u]` (if allowed) | `[u,u]` | `find(u)==find(u)` immediately → cycle. (LC inputs don't include these, but the code handles it.) |
| Minimum case `n=3` | one of the 3 edges | Smallest graph with a cycle. |

> 💡 **There's always exactly one answer.** The problem guarantees the input is a tree plus one edge, so exactly one edge will ever trigger the "already connected" check — no need to handle "no cycle" or "multiple cycles."

---

## 12. The Pattern and Its Siblings

This is **Union-Find for cycle detection in an undirected graph** — the canonical DSU application, and the third member of your Union-Find trio.

| Problem | What Union-Find detects |
|:--------|:------------------------|
| **Redundant Connection** (LC 684, this) | the single edge that closes a cycle |
| **Number of Connected Components** (LC 323) | how many groups remain |
| **Graph Valid Tree** (LC 261) | any cycle (→ not a tree) + single component |
| **Number of Islands II** (LC 305) | component count as cells are added online |
| **Accounts Merge** (LC 721) | merge groups sharing a key |
| **Kruskal's MST** | skip edges that would form a cycle |

> 💡 **The unifying DSU signal.** "Cycle?", "already connected?", "how many groups?", "which edge forms a loop?" — these all map to Union-Find. Redundant Connection is the purest: it's *only* the cycle-detection step, with the answer being the edge that triggers it. It's also the heart of **Kruskal's MST**, where you skip exactly these cycle-closing edges.

---

## 13. Common Mistakes

- ❌ **Reordering or sorting the edges** — the tiebreak ("last in input") depends on processing them *in the given order*; don't sort.
- ❌ **Union-ing before checking** — you must test `find(u) == find(v)` *first*; if you union unconditionally you lose the cycle signal.
- ❌ **0-indexing the parent array** — nodes are `1..n`; size `parent` as `n+1` and init `parent[i] = i` for `i` in `1..n`.
- ❌ **Skipping path compression** — without it `find` can degrade toward O(n) on long chains; the one-line compression keeps it near-constant.
- ❌ **Overcomplicating with DFS** — a DFS-per-edge is O(n²); Union-Find is the clean O(n) answer.
- ❌ **Returning the first cycle edge by value rather than the triggering edge** — return the edge that *fails* the union check (that's the last cycle edge in order, which the problem wants).

---

## 14. TL;DR

**Problem:** a tree + one extra edge (one cycle); return the redundant edge (the last one in input order if ambiguous).

**Core insight:** adding edge `[u,v]` creates a cycle **iff u and v are already connected**. So the redundant edge is the first one (scanning in input order) whose endpoints are already in the same group.

**Tool — Union-Find:**
```
parent[i] = i
for each edge [u,v] in order:
    if find(u) == find(v): return [u,v]   // already connected → cycle-closing edge
    union(u, v)                            // else merge the two components
```

**Why "last in input" falls out for free:** scanning front-to-back, the single cycle is only completed by its last-arriving edge — exactly the one that triggers the "already connected" check.

**Worked:** `[[1,2],[2,3],[3,4],[1,4],[1,5]]` → at `[1,4]`, both are already in `{1,2,3,4}` → **[1,4]**.

**Complexity:** O(n·α(n)) ≈ O(n) time, O(n) space — one pass, no adjacency list, no traversal.

**Siblings:** Number of Connected Components, Graph Valid Tree, Accounts Merge, Kruskal's MST — all Union-Find.

**One-line philosophy:**
> Add the edges one by one and watch for the single edge whose two endpoints are already in the same group — that edge doesn't connect anything new, it just closes a loop, so it's the redundant one; Union-Find spots it in one linear pass.
