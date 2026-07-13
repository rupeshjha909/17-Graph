# Number of Provinces (LeetCode 547) — Union-Find Solution

> A complete guide to solving "Number of Provinces" with **Union-Find (Disjoint Set Union)** in the map-based style: a `Map<Integer,Integer> parent` where `-1` marks a root, a **recursive `findSet` with path compression**, and a **`unionSet` with union by size**. The problem is really "count the connected components of a graph," and Union-Find counts them cleanly by merging connected cities and counting the roots that remain. All code verified.

> 💡 **The whole solution in one sentence:** treat each city as its own set, then for every pair of directly-connected cities call `unionSet` to merge their sets — with `findSet` compressing paths as it locates roots and union-by-size keeping trees shallow — and when all edges are processed, the **number of remaining roots is the number of provinces**, because every group of directly-or-indirectly connected cities has collapsed into exactly one set.

---

## Table of Contents
1. [Problem statement](#1-problem-statement)
2. [The key realization: it's connected components](#2-the-key-realization-its-connected-components)
3. [Why Union-Find fits](#3-why-union-find-fits)
4. [The data structure: parent map + rank(size) map](#4-the-data-structure-parent-map--ranksize-map)
5. [findSet — recursive path compression](#5-findset--recursive-path-compression)
6. [unionSet — union by size](#6-unionset--union-by-size)
7. [The full solution](#7-the-full-solution)
8. [Dry run](#8-dry-run)
9. [Complexity](#9-complexity)
10. [Common mistakes](#10-common-mistakes)
11. [DFS alternative (for contrast)](#11-dfs-alternative-for-contrast)
12. [Cheat sheet](#12-cheat-sheet)

---

## 1. Problem statement

> There are `n` cities. Some are connected. You're given an `n × n` matrix `isConnected` where `isConnected[i][j] == 1` means city `i` and city `j` are **directly** connected, and `0` means they're not. A **province** is a group of cities that are directly *or indirectly* connected. Return the total number of provinces.

### Example
```
isConnected = [[1,1,0],
               [1,1,0],
               [0,0,1]]
→ 2 provinces:  {city0, city1}  and  {city2}
```
(Verified: this returns 2. All-isolated `[[1,0,0],[0,1,0],[0,0,1]]` → 3; all-connected → 1.)

---

## 2. The key realization: it's connected components

Strip away the "cities/provinces" wording and this is a pure graph problem:

| Problem's word | Graph term |
|:---------------|:-----------|
| city | vertex/node |
| `isConnected[i][j] == 1` | an edge between `i` and `j` |
| the `isConnected` matrix | the **adjacency matrix** (graph already given) |
| province | **connected component** |
| return total provinces | **count the connected components** |

Two things to notice:
- The graph is **already built** for you as an adjacency matrix — there's no construction step, you go straight to counting.
- The graph is **undirected** (the matrix is symmetric: `isConnected[i][j] == isConnected[j][i]`), which is why we only need to look at the upper triangle (`j > i`).

> 💡 **Rename the problem to see it:** "A province is just a connected component, and the matrix is the graph. So the task is 'count connected components' — and Union-Find is built for exactly that: merge things that connect, count the groups left." 

---

## 3. Why Union-Find fits

Union-Find (Disjoint Set Union) maintains a collection of disjoint sets and supports two near-O(1) operations:
- **find(x)** — which set does `x` belong to? (returns the set's representative/root)
- **union(x, y)** — merge the sets containing `x` and `y`.

That maps perfectly onto this problem: start with every city in its **own** set, then **union** every pair of directly-connected cities. Indirect connections take care of themselves — if 0–1 and 1–2 are edges, unioning both puts 0, 1, 2 in one set automatically. When done, **each remaining set is one province**, so the answer is the number of sets (roots).

Union-Find is often preferred here over DFS/BFS when you're incrementally merging connectivity — it's the natural "grouping" tool. (DFS also works; §11.)

> 💡 **"Merge what connects, count what's left":** "I don't traverse — I merge. Every edge unions two cities' sets; connectivity, direct or transitive, collapses each province into a single set; the count of sets is the answer." 

---

## 4. The data structure: parent map + rank(size) map

Two maps drive everything:

```java
Map<Integer, Integer> parent = new HashMap<>();   // node -> its parent; -1 means "I am a root"
Map<Integer, Integer> rank   = new HashMap<>();    // for a ROOT, how many nodes are in its set (size)
```

Conventions in this style:
- **`parent.get(i) == -1` means `i` is a root** (the representative of its set). Non-roots point upward toward their root.
- **`rank` here stores the set's size** (this variant is "union by size"). Only a root's rank is meaningful — it equals the number of nodes in that set.

Initialization — every city starts alone:
```java
for (int i = 0; i < n; i++) {
    parent.put(i, -1);   // its own root
    rank.put(i, 1);      // set of size 1
}
```

> 💡 **`-1` = root; rank = size:** "Each node starts as its own root (`parent = -1`) in a set of size 1. As I union, roots get re-pointed under other roots, and the surviving root's `rank` accumulates the total size of the merged set." 

---

## 5. findSet — recursive path compression

`findSet` returns the **root** of `i`'s set, and flattens the path on the way back so future lookups are O(1).

```java
private int findSet(int i, Map<Integer, Integer> parent) {
    // Base case: if i's parent is -1, i is the root of its set
    if (parent.get(i) == -1) {
        return i;
    }
    // Recursive case: find the root, then apply path compression
    // (point i directly to the root for future O(1) lookups)
    int root = findSet(parent.get(i), parent);
    parent.put(i, root);   // path compression
    return root;
}
```

How it works, in plain terms: "If I'm a root (`parent == -1`), return myself. Otherwise, find my parent's root, **re-point myself straight at that root**, and return it." The line `parent.put(i, root)` is **full path compression** — after the call, `i` (and every node on the path) points directly to the root, so the tree gets flatter every time you search it, making later `findSet` calls nearly instant.

> 💡 **Find and flatten in one shot:** "Recursion walks up to the root, and as it unwinds, every node on the path is re-pointed directly at the root. So `findSet` doesn't just answer 'which set?' — it also shortens the tree for next time." 

---

## 6. unionSet — union by size

`unionSet` merges the two sets, always attaching the **smaller** set under the **larger** one to keep trees shallow.

```java
private void unionSet(int x, int y,
                      Map<Integer, Integer> parent,
                      Map<Integer, Integer> rank) {
    int s1 = findSet(x, parent);
    int s2 = findSet(y, parent);

    if (s1 != s2) {                            // only merge if they're in DIFFERENT sets
        if (rank.get(s1) < rank.get(s2)) {
            // s2's set is larger → attach s1 under s2
            parent.put(s1, s2);
            rank.put(s2, rank.get(s1) + rank.get(s2));   // new root's size = sum of both
        } else {
            // s1's set is equal or larger → attach s2 under s1
            parent.put(s2, s1);
            rank.put(s1, rank.get(s1) + rank.get(s2));
        }
    }
}
```

Three things to understand:
- **`if (s1 != s2)`** — if both are already in the same set, do nothing (they're already connected; merging would be a no-op and could corrupt sizes).
- **Attach smaller under larger** — comparing `rank` (size) and hanging the smaller root under the larger root keeps the combined tree short, which keeps `findSet` fast. (This is the reason for the size comparison — see the "why attach small-under-large" reasoning: the shorter tree tucks under the taller root and adds no depth.)
- **`rank` accumulates** — the surviving root's size becomes the **sum** of the two sets' sizes, so `rank[root]` always equals the true size of its set. (Verified: after merging cities 0 and 1, the root's rank is 2.)

> 💡 **Union by size keeps it flat:** "I always hang the smaller set under the larger set's root and add their sizes, so trees stay shallow and `find` stays near-constant. Skipping the size check would risk tall, slow chains." 

---

## 7. The full solution

```java
class Solution {
    public int findCircleNum(int[][] isConnected) {
        int n = isConnected.length;
        Map<Integer, Integer> parent = new HashMap<>();
        Map<Integer, Integer> rank   = new HashMap<>();

        // 1) Every city starts as its own set of size 1
        for (int i = 0; i < n; i++) {
            parent.put(i, -1);
            rank.put(i, 1);
        }

        // 2) Union every pair of directly-connected cities (upper triangle: matrix is symmetric)
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (isConnected[i][j] == 1) {
                    unionSet(i, j, parent, rank);
                }
            }
        }

        // 3) Count the roots — each surviving set is one province
        int provinces = 0;
        for (int i = 0; i < n; i++) {
            if (parent.get(i) == -1) {     // a root ⇒ one distinct set
                provinces++;
            }
        }
        return provinces;
    }

    private int findSet(int i, Map<Integer, Integer> parent) {
        if (parent.get(i) == -1) return i;
        int root = findSet(parent.get(i), parent);
        parent.put(i, root);   // path compression
        return root;
    }

    private void unionSet(int x, int y,
                          Map<Integer, Integer> parent,
                          Map<Integer, Integer> rank) {
        int s1 = findSet(x, parent);
        int s2 = findSet(y, parent);
        if (s1 != s2) {
            if (rank.get(s1) < rank.get(s2)) {
                parent.put(s1, s2);
                rank.put(s2, rank.get(s1) + rank.get(s2));
            } else {
                parent.put(s2, s1);
                rank.put(s1, rank.get(s1) + rank.get(s2));
            }
        }
    }
}
```

**Counting the provinces:** after all unions, a node `i` with `parent.get(i) == -1` is a root, and there's exactly one root per set — so counting roots counts provinces. (Equivalently, count distinct `findSet(i)` over all `i`; both give the same number — verified.)

---

## 8. Dry run

`isConnected = [[1,1,0],[1,1,0],[0,0,1]]` → expect **2**.

```
Init:  parent = {0:-1, 1:-1, 2:-1}   rank = {0:1, 1:1, 2:1}   (3 separate sets)

Scan upper triangle (j > i):
  i=0,j=1: isConnected[0][1]=1 → unionSet(0,1)
     findSet(0)=0, findSet(1)=1, different.
     rank[0](1) < rank[1](1)? no (equal) → attach s2 under s1:
        parent[1] = 0
        rank[0] = 1 + 1 = 2
     state: parent = {0:-1, 1:0, 2:-1}   rank = {0:2, ...}   sets: {0,1}, {2}
  i=0,j=2: isConnected[0][2]=0 → skip
  i=1,j=2: isConnected[1][2]=0 → skip

Count roots (parent == -1):  city0 (-1) ✓, city1 (0) ✗, city2 (-1) ✓  → 2 roots

Answer = 2  ✓   (provinces {0,1} and {2})
```

---

## 9. Complexity

| | Cost |
|:--|:--|
| **Time** | **O(n²)** — you must read every cell of the `n × n` matrix; each `unionSet`/`findSet` is near-O(1) amortized (inverse-Ackermann α(n), effectively constant, thanks to path compression + union by size) |
| **Space** | **O(n)** — the `parent` and `rank` maps hold one entry per city |

The O(n²) is unavoidable and optimal: the input itself is n² cells, so any correct solution must at least read them all. The Union-Find operations add only a near-constant factor on top.

> 💡 **The matrix sets the floor:** "It's O(n²) because the adjacency matrix has n² entries I have to scan — the Union-Find work is essentially free on top (near-constant per operation), so the matrix read dominates." 

---

## 10. Common mistakes

- ❌ **Scanning the whole matrix instead of the upper triangle.** The matrix is symmetric, so `j` from `i+1` avoids doing every edge twice (harmless to correctness but wasteful; also don't union `i` with itself on the diagonal).
- ❌ **Forgetting the `if (s1 != s2)` check.** Unioning two nodes already in the same set must be a no-op; without the check you can corrupt the size accounting and mis-attach roots.
- ❌ **Comparing `x` and `y` instead of their roots.** Union must operate on `findSet(x)` and `findSet(y)` (the roots), not the raw nodes.
- ❌ **Not applying path compression.** Without `parent.put(i, root)` in `findSet`, trees can grow tall and `find` degrades toward O(n) per call.
- ❌ **Counting nodes with `parent == -1` before all unions finish.** Count roots only *after* processing every edge.
- ❌ **Using `0` as the "no parent" sentinel.** City `0` is a real node, so `-1` (a value that can't be a valid city id) is the correct "root" marker — matching this style.
- ❌ **Recursion depth worry.** With union by size + path compression trees stay shallow, so recursive `findSet` is safe here; only for pathological huge inputs without these optimizations would you switch to an iterative find.

---

## 11. DFS alternative (for contrast)

The same problem via DFS — for each unvisited city, flood-fill its whole component and increment the count:

```java
public int findCircleNum(int[][] M) {
    int n = M.length;
    boolean[] seen = new boolean[n];
    int provinces = 0;
    for (int i = 0; i < n; i++)
        if (!seen[i]) { provinces++; dfs(M, seen, i); }   // each new start = one province
    return provinces;
}
private void dfs(int[][] M, boolean[] seen, int i) {
    seen[i] = true;
    for (int j = 0; j < M.length; j++)
        if (M[i][j] == 1 && !seen[j]) dfs(M, seen, j);
}
```

Both are O(n²). Choose **Union-Find** when you like the "merge and count sets" model (and when connectivity might arrive incrementally); choose **DFS/BFS** when you prefer straightforward traversal. Both are fully acceptable answers.

---

## 12. Cheat sheet

**Recognize it:** "province" = connected component; `isConnected` = adjacency matrix (graph already given); answer = count of components.

**Union-Find recipe:**
```
1. parent[i] = -1 (own root), rank[i] = 1 (size)   for all cities
2. for each pair i<j with isConnected[i][j]==1:  unionSet(i, j)
3. answer = number of roots (parent[i] == -1), i.e. number of sets left
```

**findSet (recursive, path compression):** if `parent[i]==-1` return `i`; else `root=findSet(parent[i])`, set `parent[i]=root`, return `root`.

**unionSet (union by size):** find both roots; if different, hang the **smaller-rank** root under the **larger**, and set the survivor's `rank = size1 + size2`.

**Complexity:** O(n²) time (must read the matrix), O(n) space; each UF op ~O(α(n)) ≈ constant.

**Watch:** union roots not raw nodes; keep `if (s1 != s2)`; count roots only after all unions; `-1` = root because `0` is a real city.

> **One-line philosophy:** *A "province" is a connected component and `isConnected` is the graph handed to you as an adjacency matrix — so start each city in its own set, union every directly-connected pair (with path compression flattening lookups and union-by-size keeping trees shallow), and because direct and indirect connections all collapse each province into a single set, the number of surviving roots is the number of provinces.*
