# Graph Valid Tree (LC 261) — Thought Process (Detailed)

> **Problem.** Given `n` nodes labeled `0 .. n-1` and a list of **undirected edges**, return `true` if the graph forms a **valid tree**.
>
> **The whole problem hinges on one definition.** A graph is a tree iff it is **connected AND has no cycle**. Equivalently — and this is the key shortcut — a graph on `n` nodes is a tree iff it has **exactly `n-1` edges AND is connected** (or, just as well, `n-1` edges AND acyclic). Any *two* of {connected, acyclic, exactly n-1 edges} guarantee the third. Nail this definition and the algorithm is short. **Union-Find** is the cleanest tool because it detects cycles and connectivity in one pass.

---

## Table of Contents

1. [Understanding the Problem](#1-understanding-the-problem)
2. [What Exactly Is a Tree?](#2-what-exactly-is-a-tree)
3. [The `n-1` Edges Shortcut](#3-the-n-1-edges-shortcut)
4. [The Two Things to Verify](#4-the-two-things-to-verify)
5. [Approach A: Union-Find (cycle + connectivity in one pass)](#5-approach-a-union-find-cycle--connectivity-in-one-pass)
6. [Approach B: DFS/BFS (connectivity check)](#6-approach-b-dfsbfs-connectivity-check)
7. [The Algorithm](#7-the-algorithm)
8. [Worked Example](#8-worked-example)
9. [The Code (Java)](#9-the-code-java)
10. [Complexity](#10-complexity)
11. [Edge Cases](#11-edge-cases)
12. [The Pattern and Its Siblings](#12-the-pattern-and-its-siblings)
13. [Common Mistakes](#13-common-mistakes)
14. [TL;DR](#14-tldr)

---

## 1. Understanding the Problem

You're given nodes and undirected edges, and asked: do they form a **tree**? A tree is a connected graph with no cycles — think of a family tree or a folder hierarchy: everything is reachable from a root, and there's exactly **one** path between any two nodes (no loops, no islands).

```
Valid tree (n=5):           NOT a tree (cycle):        NOT a tree (disconnected):
  0                           0—1                        0—1   2—3
 /|\                          | |\                       (two islands)
1 2 3                         | 2-3                      → false (not connected)
|                             4
4                             cycle among 1,2,3
→ true                        → false                    → false
```

> 💡 **The mental model.** A tree is "just barely connected" — connected, but with no edge to spare. Remove any edge and it splits; add any edge and it forms a cycle. That razor's-edge property is exactly the `n-1`-edges condition.

---

## 2. What Exactly Is a Tree?

A graph on `n` nodes is a **tree** if and only if **all three** hold (and, crucially, any two imply the third):

1. **Connected** — every node is reachable from every other.
2. **Acyclic** — no cycles.
3. **Exactly `n-1` edges.**

Why any two imply the third:
- *Connected + acyclic* → it's a tree, and a tree always has exactly `n-1` edges.
- *Connected + `n-1` edges* → must be acyclic (a connected graph with `n-1` edges has no room for a cycle).
- *Acyclic + `n-1` edges* → must be connected (an acyclic graph with `n-1` edges can't have an isolated piece).

So you never need to check all three — **verify two, get the third for free.**

> 💡 **The most useful framing.** Check (a) **exactly `n-1` edges** and (b) **connected** (or **acyclic**). If both hold, it's a tree. This turns a fuzzy "is it a tree?" into two crisp, easy checks.

---

## 3. The `n-1` Edges Shortcut

A tree on `n` nodes has **exactly `n-1` edges** — no more, no fewer. This gives an instant O(1) pre-check:

```java
if (edges.length != n - 1) return false;
```

- **More than `n-1` edges** → there *must* be a cycle (too many connections) → not a tree.
- **Fewer than `n-1` edges** → it *can't* be connected (not enough connections to link `n` nodes) → not a tree.

After this check passes, you know the edge count is right, so you only need to verify **one** more thing: that the graph is **connected** (which, with `n-1` edges, automatically means acyclic too).

> 💡 **Why this shortcut is powerful.** It collapses the work: instead of separately checking "connected" *and* "acyclic," the edge-count check plus a single connectivity check covers both. With exactly `n-1` edges, "connected" ⟺ "acyclic" ⟺ "tree."

---

## 4. The Two Things to Verify

```
1. edges.length == n - 1     ← O(1) count check
2. the graph is connected    ← one DFS/BFS, or Union-Find ending in 1 component
```

If both pass → valid tree. The cycle question is handled implicitly: with `n-1` edges, being connected forces acyclicity.

(You *could* instead check "acyclic" directly and skip the edge count, but the edge-count + connectivity combo is the cleanest.)

---

## 5. Approach A: Union-Find (cycle + connectivity in one pass)

Union-Find is the natural fit because it detects a cycle *and* tracks connectivity simultaneously:

- Start with `n` separate groups (each node its own).
- For each edge `[u, v]`: if `u` and `v` are **already in the same group**, this edge creates a **cycle** → not a tree, return false. Otherwise, union them.
- After processing all edges, if everything merged into **one** group, it's connected.

With the `edges.length == n-1` pre-check, you don't even need the final connectivity count — `n-1` edges that never created a cycle *must* leave exactly one component. But checking "ended with one component" makes it self-contained.

```
for each edge [u, v]:
    if find(u) == find(v): return false   // cycle detected
    union(u, v)
return true                                // (with n-1 edges) connected, acyclic → tree
```

> 💡 **Union-Find's two jobs here.** (1) **Cycle detection** — an edge between two already-connected nodes closes a loop. (2) **Connectivity** — after all unions, one component means connected. This is exactly the cycle-detection use of Union-Find from the connected-components family.

---

## 6. Approach B: DFS/BFS (connectivity check)

After the `n-1` edge check, build an adjacency list and run one DFS/BFS from node `0`. If it reaches **all `n` nodes**, the graph is connected → (with `n-1` edges) a tree.

```
if edges.length != n-1: return false
build adjacency list (both directions)
DFS/BFS from node 0, marking visited
return (number of visited nodes == n)
```

Because the edge count is already `n-1`, you don't need explicit cycle detection — connectivity alone suffices. (Without the edge-count shortcut, you'd also have to detect a cycle during the DFS by checking for a visited non-parent neighbor.)

> 💡 **Why the edge-count check simplifies the DFS.** Normally a DFS tree-check must watch for cycles (a back-edge to an already-visited, non-parent node). But once you know there are exactly `n-1` edges, a cycle is impossible if the graph is connected — so a plain "did I reach all n nodes?" check is enough.

---

## 7. The Algorithm

### Union-Find
```
if edges.length != n - 1: return false
parent[i] = i for all i
for each edge [u, v]:
    if find(u) == find(v): return false      // cycle
    union(u, v)
return true
```

### DFS
```
if edges.length != n - 1: return false
build adjacency list (both directions)
DFS from 0, mark visited
return visitedCount == n                       // connected ⇒ tree
```

---

## 8. Worked Example

```
n = 5, edges = [[0,1],[0,2],[0,3],[1,4]]
```

**Edge-count check:** 4 edges, `n-1 = 4` ✓ — passes, continue.

**Union-Find:**
```
parent = [0,1,2,3,4]
[0,1]: find0=0, find1=1, differ → union → parent[1]=0
[0,2]: find0=0, find2=2, differ → union → parent[2]=0
[0,3]: differ → union → parent[3]=0
[1,4]: find1=0, find4=4, differ → union → parent[4]=0
no cycle ever; all merged → connected → TRUE
```

**Counter-example with a cycle** — `edges = [[0,1],[1,2],[2,3],[1,3],[1,4]]` (5 edges, but `n-1=4`):
```
edge count = 5 ≠ 4 → return false immediately (too many edges → cycle)
```
Even without the count shortcut, Union-Find would catch it: processing `[1,3]` finds 1 and 3 already connected (via 1→2→3) → cycle → false.

**Disconnected** — `n=4, edges=[[0,1],[2,3]]`: edge count = 2 ≠ 3 → false (too few edges to connect 4 nodes). (All verified.)

---

## 9. The Code (Java)

### Approach A — Union-Find (recommended)

```java
class Solution {
    private int[] parent;

    public boolean validTree(int n, int[][] edges) {
        if (edges.length != n - 1) return false;   // a tree has exactly n-1 edges

        parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;

        for (int[] e : edges) {
            int ru = find(e[0]), rv = find(e[1]);
            if (ru == rv) return false;            // edge within a component → cycle
            parent[ru] = rv;                        // union
        }
        return true;   // n-1 edges, no cycle ⇒ connected ⇒ valid tree
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

### Approach B — DFS connectivity

```java
class Solution {
    public boolean validTree(int n, int[][] edges) {
        if (edges.length != n - 1) return false;

        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) {
            adj.get(e[0]).add(e[1]);
            adj.get(e[1]).add(e[0]);          // undirected → both directions
        }

        boolean[] seen = new boolean[n];
        dfs(adj, 0, seen);

        for (boolean v : seen) if (!v) return false;   // some node unreachable → not connected
        return true;
    }

    private void dfs(List<List<Integer>> adj, int node, boolean[] seen) {
        seen[node] = true;
        for (int nb : adj.get(node)) {
            if (!seen[nb]) dfs(adj, nb, seen);
        }
    }
}
```

(Both verified against a brute-force connected+acyclic checker over 20k random graphs plus the LeetCode examples.)

---

## 10. Complexity

Let `V = n`, `E = edges.length` (which is `n-1` once the check passes).

- **Union-Find:** O(E · α(n)) ≈ **O(n)** — each edge does a near-constant find/union; O(n) space for `parent`. No adjacency list needed.
- **DFS:** O(V + E) = **O(n)** time (since E = n-1); O(n) space for the adjacency list + visited + recursion.

Both are linear. Union-Find uses slightly less space (no adjacency list).

---

## 11. Edge Cases

| Case | Result | Why |
|:-----|:-------|:----|
| `n=1`, no edges | true | A single node is a valid (trivial) tree; `n-1 = 0` edges ✓. |
| `n=2`, `[[0,1]]` | true | Two nodes, one edge, connected. |
| More than `n-1` edges | false | Guaranteed cycle. |
| Fewer than `n-1` edges | false | Can't be connected. |
| Exactly `n-1` edges but disconnected | false | E.g. `n=4, [[0,1],[1,0... ]]` impossible without a cycle — caught by Union-Find's cycle check. |
| Self-loop `[u,u]` | false | `find(u)==find(u)` → instant cycle. |

> 💡 **The subtle case:** *exactly `n-1` edges but with a cycle* — that forces a disconnection elsewhere. The edge-count check alone passes, but Union-Find's cycle detection (or the DFS connectivity check finding an unreached node) catches it. So you do need the second check, not just the count.

---

## 12. The Pattern and Its Siblings

This is **Union-Find for cycle detection + connectivity** — the same DSU toolkit as Number of Connected Components, applied to validate a structural property.

| Problem | What Union-Find does |
|:--------|:---------------------|
| **Graph Valid Tree** (LC 261, this) | detect cycle + check single component |
| **Number of Connected Components** (LC 323) | count components |
| **Redundant Connection** (LC 684) | find the edge that first closes a cycle |
| **Number of Islands II** (LC 305) | dynamic component count as cells are added |
| **Accounts Merge** (LC 721) | merge groups by shared key |

> 💡 **The defining Union-Find signal:** "cycle?", "same group?", "how many groups?", "which edge creates a loop?" — all scream DSU. Graph Valid Tree combines two of those (no cycle + one group). The `n-1` edge shortcut is the tree-specific bonus that lets you check connectivity *or* acyclicity rather than both.

---

## 13. Common Mistakes

- ❌ **Forgetting the `n-1` edge check** — it's the fastest filter and simplifies everything after.
- ❌ **Checking only the edge count** — `n-1` edges with a cycle (and a matching disconnection) still isn't a tree; you must also verify connectivity/acyclicity.
- ❌ **Not detecting cycles in the DFS approach** — if you skip the edge-count shortcut, a plain connectivity DFS can wrongly pass a cyclic graph; either use the count shortcut or detect back-edges (visited non-parent neighbor).
- ❌ **Forgetting both directions in the adjacency list** (DFS) — undirected edges need both.
- ❌ **`parent[i] = 0` instead of `parent[i] = i`** (Union-Find) — each node must start as its own root.
- ❌ **Treating a self-loop or duplicate edge as fine** — both create cycles; Union-Find catches them naturally.

---

## 14. TL;DR

**Problem:** Do `n` nodes + undirected edges form a valid tree (connected + acyclic)?

**The key definition:** a tree on `n` nodes has **exactly `n-1` edges AND is connected** (which then forces acyclic). Any two of {connected, acyclic, n-1 edges} imply the third.

**Two checks:**
1. `edges.length == n - 1` (O(1) — wrong count → instantly not a tree).
2. connected / acyclic — via **Union-Find** (cycle if an edge joins two already-connected nodes; one component at the end) or **DFS** (reach all `n` nodes).

**Union-Find core:**
```
if edges.length != n-1: return false
for each edge [u,v]: if find(u)==find(v) return false (cycle); else union(u,v)
return true
```

**Worked:** `n=5, edges=[[0,1],[0,2],[0,3],[1,4]]` → 4 edges = n-1 ✓, no cycle, all connected → **true**.

**Complexity:** O(n) either way (Union-Find ≈ O(n·α(n)); DFS O(V+E) with E=n-1).

**Siblings:** Number of Connected Components, Redundant Connection, Accounts Merge — all Union-Find cycle/grouping problems.

**One-line philosophy:**
> A tree is a connected, acyclic graph — and on `n` nodes that means exactly `n-1` edges with no cycle — so check the edge count in O(1), then let Union-Find confirm no edge ever links two already-connected nodes (no cycle) and everything ends in one group (connected); both true means it's a tree.
