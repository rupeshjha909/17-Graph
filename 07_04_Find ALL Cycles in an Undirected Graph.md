# Find ALL Cycles in an Undirected Graph — Thought Process (Detailed)

> **Problem.** Enumerate **every elementary cycle** (a loop visiting no vertex twice) in an *undirected* graph. E.g. for a square `0-1-2-3-0` with a diagonal `0-2`, the cycles are the two triangles `0-1-2`, `0-2-3` and the outer square `0-1-2-3`.
>
> **The key differences from directed.** The directed version had to dedup *rotations* (same cycle from each start vertex). Undirected adds **two more wrinkles**: (1) a cycle and its **reverse** are the *same* cycle (no direction), so we must dedup orientation too; and (2) "there and back along one edge" (`A-B-A`) is **not** a cycle — an undirected simple cycle needs **≥ 3 distinct vertices**. There's also a beautiful undirected-only concept: the **cycle basis**. As with directed, the output can be **exponential**, so worst-case exponential time is unavoidable.

---

## Table of Contents

1. [What "All Cycles" Means in an Undirected Graph](#1-what-all-cycles-means-in-an-undirected-graph)
2. [Two New Dedup Problems (Rotation AND Direction)](#2-two-new-dedup-problems-rotation-and-direction)
3. [Why Length Must Be ≥ 3](#3-why-length-must-be--3)
4. [The Output Is Exponential](#4-the-output-is-exponential)
5. [The Approach: DFS + Backtracking with Canonical Form](#5-the-approach-dfs--backtracking-with-canonical-form)
6. [The Three-Part Dedup Rule](#6-the-three-part-dedup-rule)
7. [The Algorithm](#7-the-algorithm)
8. [Worked Example](#8-worked-example)
9. [The Code (Java)](#9-the-code-java)
10. [The Cycle Basis (undirected-only insight)](#10-the-cycle-basis-undirected-only-insight)
11. [Complexity](#11-complexity)
12. [Edge Cases](#12-edge-cases)
13. [Common Mistakes](#13-common-mistakes)
14. [The Pattern and Its Siblings](#14-the-pattern-and-its-siblings)
15. [TL;DR](#15-tldr)

---

## 1. What "All Cycles" Means in an Undirected Graph

An **elementary cycle** is a closed loop that repeats no vertex (except start = end). In an undirected graph, edges have no direction, so a cycle is just a set of vertices connected in a ring.

```
square with a diagonal:
   0 ─── 1
   │ ╲   │
   │  ╲  │
   3 ─── 2     edges: 0-1, 1-2, 2-3, 3-0, 0-2

elementary cycles:
   triangle 0-1-2   (via 0-1, 1-2, 2-0)
   triangle 0-2-3   (via 0-2, 2-3, 3-0)
   square   0-1-2-3 (via 0-1, 1-2, 2-3, 3-0)
→ 3 cycles
```

Note the square and the two triangles are all distinct elementary cycles, even though the square "decomposes" into the triangles (more on that in the cycle basis section).

> 💡 **The mental model.** An undirected cycle is a ring of ≥ 3 vertices where each consecutive pair (and the wrap-around) is connected by an edge — with no notion of which way you traverse it.

---

## 2. Two New Dedup Problems (Rotation AND Direction)

In the *directed* version, the same cycle could be found starting from each of its vertices (rotations), and we fixed that with "only start from the smallest vertex." Undirected has the **same rotation problem plus a direction problem**:

```
The triangle on {0,1,2} can be written as:
   rotations:   0-1-2,  1-2-0,  2-0-1
   AND reversed: 0-2-1,  2-1-0,  1-0-2
→ SIX ways to write ONE undirected triangle!
```

Because an undirected edge `a-b` is the same as `b-a`, traversing the ring **clockwise or counter-clockwise** gives the identical cycle. So we must canonicalize **both**:
- **Rotation** → start from the smallest vertex (same trick as directed).
- **Direction** → of the two orientations, pick one canonical one (e.g., require the second vertex to be smaller than the last vertex).

> 💡 **Undirected = directed's dedup, doubled.** Directed cycles have one orientation (the arrow direction), so only rotations duplicate. Undirected cycles have no fixed orientation, so *each* cycle has twice as many spellings — rotations × 2 directions. We need a canonical form that collapses all of them to one.

---

## 3. Why Length Must Be ≥ 3

In a directed graph, a 2-cycle `u→v→u` is valid (two distinct edges). In an **undirected** graph, going `u - v - u` walks the **same single edge** back and forth — that's not a cycle, just a retrace.

```
undirected edge u — v:
   u → v → u   uses the edge (u,v) twice → NOT a cycle
```

So the smallest undirected elementary cycle is a **triangle (3 vertices)**. We enforce `length >= 3` when recording. (This mirrors the "skip the parent" rule from undirected cycle *detection* — both exist because undirected edges are bidirectional and the immediate back-step isn't a loop.)

> 💡 **The ≥ 3 rule is the undirected signature.** Directed allows length-1 (self-loop) and length-2 cycles; undirected simple cycles start at length 3. Forgetting this floods your output with fake "2-cycles" that are just edges traversed twice.

---

## 4. The Output Is Exponential

Just like the directed case, the number of elementary cycles can grow **exponentially**:

```
Complete undirected graph K_n (every pair connected):
   K3 →  1 cycle  (one triangle)
   K4 →  7 cycles (4 triangles + 3 quadrilaterals)
   K5 → 37 cycles
   ... grows super-polynomially
```

(Verified: K4 → 7.) Since you must output them all, **worst-case exponential time is inherent** — no algorithm escapes it. As with directed, judge by work *per cycle*, not overall polynomiality.

> 💡 **Same output-sensitive reality.** "List all cycles" is exponential-output in the worst case whether directed or undirected. The art is minimizing wasted work *between* cycles (the directed doc's Johnson's algorithm; undirected has analogous refined enumerators).

---

## 5. The Approach: DFS + Backtracking with Canonical Form

The structure is the familiar enumeration backtracking: from each start vertex, build a path; when the path can close back to the start, record it as a cycle (if it passes the canonical-form checks); mark vertices on the path and unmark on backtrack to keep cycles elementary.

```
dfs(start, node, path, onPath):
    for each neighbor nb of node:
        if nb == start and len(path) >= 3:   → candidate cycle; record IF canonical
        else if nb > start and not onPath[nb]:
            onPath[nb]=true; path.add(nb); dfs(...); path.pop(); onPath[nb]=false
```

This is the same skeleton as "all paths" and the directed all-cycles — only the *recording condition* carries the extra undirected dedup checks.

---

## 6. The Three-Part Dedup Rule

To record each undirected cycle exactly once, a candidate cycle (path that closes back to `start`) must pass **all three**:

```
1. start is the SMALLEST vertex on the cycle.
   → enforced by only walking to neighbors >= start (and starting DFS at each vertex).
   → kills ROTATION duplicates.

2. length >= 3.
   → an undirected cycle needs at least 3 distinct vertices.
   → kills the fake "back-and-forth on one edge" non-cycles.

3. path[1] < path[last].
   → of the two directions around the ring, keep only the one where the
     second vertex is smaller than the last vertex.
   → kills DIRECTION duplicates.
```

With all three, each undirected cycle is emitted once — from its smallest vertex, in its canonical orientation, with length ≥ 3.

> 💡 **One canonical fingerprint per cycle.** Rule 1 fixes *where* the cycle starts, Rule 3 fixes *which way* it goes, Rule 2 ensures it's actually a cycle. Together they define a single canonical spelling, so duplicates never get recorded.

---

## 7. The Algorithm

```
cycles = []

for start in 0..V-1:
    onPath = all false
    onPath[start] = true
    dfs(start, start, [start], onPath)

dfs(start, node, path, onPath):
    for each neighbor nb of node:
        if nb < start:                          continue        // rotation dedup
        if nb == start and path.size() >= 3:                     // closes a cycle
            if path[1] < path[path.size()-1]:   cycles.add(copy of path)   // direction dedup
        else if nb > start and not onPath[nb]:
            onPath[nb] = true
            path.add(nb)
            dfs(start, nb, path, onPath)
            path.pop()                          // backtrack
            onPath[nb] = false
```

---

## 8. Worked Example

```
square + diagonal:  edges 0-1, 1-2, 2-3, 3-0, 0-2

   0 ─── 1
   │ ╲   │
   3 ─── 2
```

**Root start = 0** (smallest, so all cycles are found here):
```
path=[0], onPath={0}
  0→1 (1>0): path=[0,1]
    1→2 (2>0): path=[0,1,2]
      2→0: ==start, len=3>=3, path[1]=1 < path[-1]=2 ✓ → RECORD [0,1,2]   (triangle)
      2→3 (3>0): path=[0,1,2,3]
        3→0: ==start, len=4>=4, path[1]=1 < path[-1]=3 ✓ → RECORD [0,1,2,3] (square)
      backtrack
    backtrack
  0→2 (2>0): path=[0,2]
    2→1: ==? no. 1>0 not on path: path=[0,2,1]
      1→0: ==start, len=3, path[1]=2 < path[-1]=1?  2 < 1 is FALSE → SKIP (reverse of triangle already recorded)
    2→3: path=[0,2,3]
      3→0: ==start, len=3, path[1]=2 < path[-1]=3 ✓ → RECORD [0,2,3]   (triangle)
  0→3 (3>0): symmetric, its cycles fail the direction check (already recorded)
```

**Result:** `[[0,1,2], [0,1,2,3], [0,2,3]]` → triangle `0-1-2`, square `0-1-2-3`, triangle `0-2-3`. Note `0-2-1` was correctly **skipped** as the reverse of `0-1-2`. (Verified against brute force.)

---

## 9. The Code (Java)

```java
import java.util.*;

class FindAllUndirectedCycles {
    private List<List<Integer>> cycles = new ArrayList<>();
    private List<List<Integer>> adj;

    public List<List<Integer>> findAllCycles(int V, List<List<Integer>> adj) {
        this.adj = adj;
        for (int start = 0; start < V; start++) {
            boolean[] onPath = new boolean[V];
            onPath[start] = true;
            List<Integer> path = new ArrayList<>();
            path.add(start);
            dfs(start, start, path, onPath);
        }
        return cycles;
    }

    private void dfs(int start, int node, List<Integer> path, boolean[] onPath) {
        for (int nb : adj.get(node)) {
            if (nb < start) continue;                       // (1) rotation dedup
            if (nb == start && path.size() >= 3) {          // (2) length >= 3 → real cycle
                if (path.get(1) < path.get(path.size() - 1)) {   // (3) direction dedup
                    cycles.add(new ArrayList<>(path));      // record a COPY
                }
            } else if (nb > start && !onPath[nb]) {
                onPath[nb] = true;
                path.add(nb);
                dfs(start, nb, path, onPath);
                path.remove(path.size() - 1);               // backtrack
                onPath[nb] = false;
            }
        }
    }
}
```

(Verified: produces exactly the brute-force set of undirected elementary cycles over 3k random graphs; K4 → 7 cycles as expected.)

> 💡 **The three dedup checks map to the three lines:** `nb < start` skip (rotation), `path.size() >= 3` (real cycle), `path.get(1) < path.get(last)` (direction). Plus the universal backtracking essentials: record a **copy**, and add/remove on `onPath[]`.

---

## 10. The Cycle Basis (undirected-only insight)

A beautiful concept that's special to undirected graphs: not all cycles are "independent." Some cycles are **combinations** of others.

```
In the square + diagonal:
   triangle 0-1-2  ⊕  triangle 0-2-3  =  square 0-1-2-3
   (the shared diagonal 0-2 cancels out)
```

The **cycle basis** is a minimal set of cycles from which *all* cycles can be built (via symmetric difference of edge sets). Its size — the **cyclomatic number** (a.k.a. circuit rank) — is:

```
cycle basis size = E - V + (number of connected components)

square + diagonal:  E=5, V=4, components=1  →  5 - 4 + 1 = 2 independent cycles
```

But the **total number of elementary cycles** (3 here: two triangles + square) can exceed the basis size (2). The basis is a *compact generating set*; the elementary cycles are *all* the loops you can form.

> 💡 **Basis vs. all cycles — two different questions.** "Find all elementary cycles" (this doc) can be exponential. "Find a cycle basis" is only `E - V + components` cycles and is computable in polynomial time (via a spanning tree: each non-tree edge creates exactly one fundamental cycle). If a problem really needs a *compact* representation of the cycle space, the basis — not the full enumeration — is often what's wanted. **Horton's algorithm** finds a *minimum-weight* cycle basis in polynomial time.

---

## 11. Complexity

Let `V` = vertices, `E` = edges, `C` = number of elementary cycles.

- **DFS backtracking enumeration:** worst-case **exponential** — `C` itself can be exponential (K_n), and the search can do extra work between cycles. Bounded below by Ω(C) since every cycle must be output.
- **Cycle *basis* (if that's all you need):** **O(V + E)** to build via a spanning tree — only `E - V + components` fundamental cycles, polynomial. (Minimum-weight basis via **Horton's algorithm**: polynomial, O(E³ V) ballpark.)
- **Space:** O(V + E) for the graph + O(V) for the recursion/path (excluding the exponential output).

> 💡 **Pick the right target.** If you truly need *every* elementary cycle, accept exponential output. If you need to *characterize* the cycle space compactly, compute the **cycle basis** in polynomial time instead — a far cheaper, often-sufficient answer.

---

## 12. Edge Cases

| Case | Result | Why |
|:-----|:-------|:----|
| Tree / forest (no cycle) | `[]` | No path closes back to start. |
| Single triangle | one cycle | The minimal undirected cycle (length 3). |
| Two edges `u-v` only | `[]` | Length-2 "cycle" rejected (≥ 3 rule). |
| Complete graph K_n | exponentially many | Expected — output is huge. |
| Multiple disjoint cycles | all of them | Each found from its own smallest vertex. |
| Self-loop / parallel edges | special handling | A simple-graph enumerator assumes neither; handle separately if present. |

> 💡 **The ≥ 3 rule quietly handles the "2-cycle" trap** — without it, every single edge would masquerade as a length-2 cycle.

---

## 13. Common Mistakes

- ❌ **Only deduping rotations (forgetting direction)** — each undirected cycle has two orientations; without the `path[1] < path[last]` check you record every cycle twice.
- ❌ **Allowing length-2 "cycles"** — `u-v-u` retraces one edge; require length ≥ 3.
- ❌ **Storing the path reference, not a copy** — the live `path` mutates during backtracking; use `new ArrayList<>(path)`.
- ❌ **Forgetting to unmark `onPath[]` on backtrack** — blocks legitimate other cycles through that vertex.
- ❌ **Confusing cycle basis with all cycles** — the basis (E−V+comp) is a small generating set; the full elementary-cycle count can be exponentially larger.
- ❌ **Expecting polynomial time for full enumeration** — impossible; the output is exponential. Use the cycle basis if you need polynomial.

---

## 14. The Pattern and Its Siblings

This is **exhaustive undirected cycle enumeration via backtracking with canonical-form dedup**, plus the **cycle basis** as the polynomial-time compact alternative.

| Problem | Relationship |
|:--------|:-------------|
| **Find all cycles (undirected, this)** | enumerate every elementary cycle; dedup rotation + direction, length ≥ 3 |
| **Find all cycles (directed)** | same backtracking; dedup rotation only (direction is fixed by arrows) |
| **Find one cycle (undirected)** | stop at first visited-non-parent neighbor; parent-pointer reconstruction |
| **Cycle basis / cyclomatic number** | E − V + components independent cycles; spanning-tree, polynomial |
| **Minimum cycle basis (Horton's)** | smallest-weight generating set of cycles, polynomial |
| **All Paths Source→Target** (LC 797) | same backtracking skeleton; target is a node, here it's "back to start" |

> 💡 **Directed vs undirected all-cycles in one line.** Both are exponential-output backtracking enumerations. Directed dedups *rotations* (one orientation, fixed by edge direction). Undirected dedups *rotations × directions* and enforces *length ≥ 3* — because edges are bidirectional and a single edge retraced isn't a loop. And only undirected has the elegant cycle-basis structure (E−V+comp) for a polynomial compact summary.

---

## 15. TL;DR

**Problem:** enumerate **all elementary cycles** in an undirected graph.

**Two undirected-specific wrinkles vs directed:**
1. **Direction dedup** — a cycle and its reverse are the same; canonicalize orientation (e.g., require `path[1] < path[last]`).
2. **Length ≥ 3** — `u-v-u` retraces one edge, not a cycle; undirected cycles need ≥ 3 vertices.
(Plus the shared rotation dedup: only start a cycle from its smallest vertex.)

**Approach — DFS + backtracking with a 3-part dedup rule:**
```
for each start: DFS building a path, only to neighbors >= start;
when you can close back to start AND length>=3 AND path[1]<path[last] → record (a copy);
mark/unmark onPath to keep cycles elementary.
```

**Worked:** square+diagonal `0-1-2-3-0, 0-2` → `[[0,1,2],[0,1,2,3],[0,2,3]]` (`0-2-1` skipped as the reverse of `0-1-2`).

**The output is exponential** (K4 → 7 cycles, K5 → 37...), so worst-case exponential time is unavoidable — judge by work per cycle.

**Undirected-only gem — the cycle basis:** a minimal generating set of `E − V + components` cycles (the cyclomatic number), computable in **polynomial** time via a spanning tree. The total elementary-cycle count can far exceed the basis size — so if you only need to *characterize* the cycle space, compute the basis, not the full enumeration. (Minimum-weight basis: Horton's algorithm.)

**Siblings:** directed all-cycles, undirected find-one-cycle, cycle basis / Horton's, All Paths Source→Target.

**One-line philosophy:**
> Enumerating all undirected cycles is the directed problem with the dedup doubled — collapse each loop's rotations *and* its two directions to one canonical spelling and demand at least three vertices — and since the full list can be exponential, remember that the cycle basis (E−V+components) gives a polynomial, compact summary of the cycle space when you don't truly need every loop.
