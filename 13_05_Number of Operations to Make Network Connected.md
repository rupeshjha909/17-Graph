# Number of Operations to Make Network Connected (LeetCode 1319) — Union-Find, with Full Pattern Recognition

> The Union-Find idea (count connected components) is the same as before, but this problem adds two new moves: a **feasibility pre-check** (do you even have enough cables?) and a **formula on the component count** (the answer is `components − 1`, not the count itself). The pretty part is the math showing *why* `components − 1` is always achievable when you have enough cables. Same map-based Union-Find style (parent map, `-1` = root, recursive `findSet`, `unionSet` by size). All code and the math are verified.

> 💡 **The whole solution in one sentence:** to connect `n` computers you need at least `n − 1` cables — so if there are fewer, it's impossible (return `−1`); otherwise, use Union-Find to count how many separate **connected components** the network currently has, and the answer is **`components − 1`**, because bridging `c` components into one takes exactly `c − 1` re-plugged cables, and having `≥ n − 1` cables guarantees you always have that many redundant (cycle-forming) cables free to move.

---

## Table of Contents
1. [Problem statement](#1-problem-statement)
2. [How to guess the pattern (the thought process)](#2-how-to-guess-the-pattern-the-thought-process)
3. [Insight 1: "connected" means one component](#3-insight-1-connected-means-one-component)
4. [Insight 2: the two-part answer (feasibility + formula)](#4-insight-2-the-two-part-answer-feasibility--formula)
5. [The math: why components − 1, and why n − 1 cables suffice](#5-the-math-why-components--1-and-why-n--1-cables-suffice)
6. [The plan](#6-the-plan)
7. [The Union-Find pieces](#7-the-union-find-pieces)
8. [The full solution](#8-the-full-solution)
9. [Dry run](#9-dry-run)
10. [Complexity](#10-complexity)
11. [Common mistakes](#11-common-mistakes)
12. [How to recognize this pattern next time](#12-how-to-recognize-this-pattern-next-time)
13. [Cheat sheet](#13-cheat-sheet)

---

## 1. Problem statement

> There are `n` computers numbered `0…n-1`, connected by cables given as `connections`, where `connections[i] = [a, b]` is a cable directly linking computers `a` and `b`. You may **unplug any cable and re-plug it between any two computers**. Return the **minimum number of such operations** to make *every* computer connected (directly or indirectly). If it's impossible, return **−1**.

### Examples
```
n = 4, connections = [[0,1],[0,2],[1,2]]           → 1
        (component {0,1,2} plus lone {3}; move the redundant cable in the triangle to reach 3)
n = 6, connections = [[0,1],[0,2],[0,3],[1,2],[1,3]] → 2
        ({0,1,2,3}, {4}, {5} → 3 components → 2 moves)
n = 6, connections = [[0,1],[0,2],[0,3],[1,2]]       → -1
        (only 4 cables, but connecting 6 computers needs at least 5)
```
(All verified.)

---

## 2. How to guess the pattern (the thought process)

**Q1: What does "make every computer connected" mean in graph terms?**
The whole network becomes **one connected component**. So the current state is "some number of components," and the goal is "one." → This is a **connected-components** problem → **Union-Find** (or DFS).

**Q2: What operation changes the structure, and what does it cost?**
Moving a cable = removing an edge somewhere and adding an edge elsewhere, costing 1. To *join* two separate components you add one bridging cable. So merging `c` components into 1 needs `c − 1` bridging cables.

**Q3: Where do the bridging cables come from?**
From **redundant** cables — ones already inside a component forming a cycle (removing them doesn't disconnect anything). So the real question becomes: *do I have enough redundant cables to supply the `c − 1` bridges?*

**Q4: When is it outright impossible?**
Connecting `n` computers requires a minimum of `n − 1` cables total (a tree). If `connections.length < n − 1`, you can never connect them, no matter how you rearrange → **−1**.

**Q5: If it's feasible, is the answer just `c − 1`?**
Yes — and the neat fact (proved in §5) is that **whenever total cables ≥ n − 1, you *always* have at least `c − 1` redundant cables**, so the answer is exactly `components − 1`. You don't even have to count the redundant cables; the feasibility check guarantees enough.

So the shape is: *feasibility check on cable count, then count components with Union-Find, then answer = components − 1.*

> 💡 **Two questions, in order:** "First, 'is it even possible?' — need ≥ n−1 cables. Then, 'how many bridges?' — that's (components − 1). Union-Find gives the component count; the feasibility check guarantees the spare cables exist." 

---

## 3. Insight 1: "connected" means one component

"Every computer connected" is exactly "the graph has **one** connected component." Right now the cables partition the `n` computers into some number of components `c` (isolated computers count as their own single-node component). Union-Find counts `c` directly: start with `c = n` (everyone alone) and **decrement each time a cable actually merges two different components**.

Cables that link two computers *already* in the same component are **redundant** — they don't reduce `c` (the `unionSet` returns "no merge"). Those are precisely the cables you're free to move.

> 💡 **Count components; redundant cables reveal themselves:** "Every cable that *fails* to merge (both ends already in one component) is a redundant, movable cable. So the same Union-Find pass that counts components also implicitly identifies the spare cables." 

---

## 4. Insight 2: the two-part answer (feasibility + formula)

Unlike the earlier component-counting problems where the answer *was* the count, here the answer is a small computation on the count, gated by a feasibility check:

```
if (connections.length < n - 1)  →  return -1        // not enough cables to ever connect n nodes
else                             →  return components - 1
```

- **Feasibility:** a connected graph on `n` nodes needs **at least `n − 1`** edges (that's a spanning tree). Fewer cables than that and it's hopeless regardless of arrangement.
- **Formula:** given feasibility, merging `c` components into one costs `c − 1` cable moves.

Both parts are essential — miss the feasibility check and you'd return a bogus positive number for impossible inputs; miss the formula and you'd return the raw count (off by one).

---

## 5. The math: why components − 1, and why n − 1 cables suffice

This is the part worth understanding, not memorizing.

**Why `c − 1` moves merge `c` components:** each move plugs one cable between two currently-separate components, reducing the component count by exactly 1. Starting at `c` and ending at `1` takes `c − 1` such moves. (You can't do better — each move reduces the count by at most 1.)

**Why `≥ n − 1` cables always provide enough spares:** Let `m` = total cables, `c` = components, `n` = computers. A **forest** (all components are trees) on `n` nodes with `c` components uses exactly `n − c` edges. Any cables beyond that are **redundant**:

```
redundant cables = m − (n − c)
```

We need `c − 1` bridges. Is `redundant ≥ c − 1`?
```
m − (n − c) ≥ c − 1
m − n + c   ≥ c − 1
m           ≥ n − 1          ✓  (exactly the feasibility condition!)
```

So **the moment you have `m ≥ n − 1` cables, you automatically have at least `c − 1` redundant cables** — enough to supply every bridge. That's why you never need to *count* the redundant cables: passing the feasibility check *is* the guarantee. (Verified numerically over 100,000 random `(n, m, c)` with `m ≥ n−1`.)

> 💡 **Feasibility and sufficiency are the same inequality:** "`m ≥ n−1` isn't just 'possible in principle' — the same inequality algebraically guarantees there are ≥ c−1 redundant cables to actually do the c−1 bridges. So once feasible, the answer is cleanly `components − 1`, no spare-counting needed." 

---

## 6. The plan

```
1. FEASIBILITY  if connections.length < n - 1 → return -1.
2. INIT         n computers, each its own component (parent=-1, rank=1); components = n.
3. UNION        for each cable, unionSet(a, b); if it merged, components--.
4. ANSWER       return components - 1.
```

---

## 7. The Union-Find pieces

Same engine as the whole series — but note `unionSet` now returns a boolean so we can count merges.

```java
private int findSet(int i, Map<Integer, Integer> parent) {
    if (parent.get(i) == -1) return i;
    int root = findSet(parent.get(i), parent);
    parent.put(i, root);                       // path compression
    return root;
}
private boolean unionSet(int x, int y,
                         Map<Integer, Integer> parent, Map<Integer, Integer> rank) {
    int s1 = findSet(x, parent), s2 = findSet(y, parent);
    if (s1 == s2) return false;                // already connected → redundant cable, no merge
    if (rank.get(s1) < rank.get(s2)) { parent.put(s1, s2); rank.put(s2, rank.get(s1) + rank.get(s2)); }
    else                             { parent.put(s2, s1); rank.put(s1, rank.get(s1) + rank.get(s2)); }
    return true;                               // a real merge happened
}
```

---

## 8. The full solution

```java
class Solution {
    public int makeConnected(int n, int[][] connections) {
        // 1) FEASIBILITY: n computers need at least n-1 cables to ever be connected
        if (connections.length < n - 1) return -1;

        // 2) init: each computer its own component
        Map<Integer, Integer> parent = new HashMap<>();
        Map<Integer, Integer> rank   = new HashMap<>();
        for (int i = 0; i < n; i++) { parent.put(i, -1); rank.put(i, 1); }
        int components = n;

        // 3) union each cable; count how many components remain
        for (int[] c : connections) {
            if (unionSet(c[0], c[1], parent, rank)) {
                components--;                  // this cable merged two components
            }
        }

        // 4) merging `components` groups into one costs (components - 1) cable moves
        return components - 1;
    }

    private int findSet(int i, Map<Integer, Integer> parent) {
        if (parent.get(i) == -1) return i;
        int root = findSet(parent.get(i), parent);
        parent.put(i, root);
        return root;
    }
    private boolean unionSet(int x, int y, Map<Integer, Integer> parent, Map<Integer, Integer> rank) {
        int s1 = findSet(x, parent), s2 = findSet(y, parent);
        if (s1 == s2) return false;
        if (rank.get(s1) < rank.get(s2)) { parent.put(s1, s2); rank.put(s2, rank.get(s1) + rank.get(s2)); }
        else                             { parent.put(s2, s1); rank.put(s1, rank.get(s1) + rank.get(s2)); }
        return true;
    }
}
```

Verified against all sample cases and edge cases (fully-connected → 0; too few cables → −1).

---

## 9. Dry run

`n = 6, connections = [[0,1],[0,2],[0,3],[1,2],[1,3]]` → expect **2**.

```
1) FEASIBILITY: cables = 5, n-1 = 5 → 5 >= 5 OK (not impossible)

2) init: components = 6   (each of 0..5 alone)

3) union each cable:
   [0,1]: merge → components 5      set {0,1}
   [0,2]: merge → components 4      set {0,1,2}
   [0,3]: merge → components 3      set {0,1,2,3}
   [1,2]: find(1)=find(2) same set → NO merge (redundant cable)   components stays 3
   [1,3]: find(1)=find(3) same set → NO merge (redundant cable)   components stays 3
   final components = 3   → {0,1,2,3}, {4}, {5}
   (2 redundant cables found — exactly the spares we'll move)

4) answer = components - 1 = 3 - 1 = 2   ✓
```

The two cables that failed to merge (`[1,2]`, `[1,3]`) are the redundant ones — precisely the 2 we re-plug to reach `{4}` and `{5}`.

---

## 10. Complexity

Let `m` = number of cables.

| | Cost |
|:--|:--|
| **Time** | **O(m · α(n)) ≈ O(m)** — one pass over the cables, each Union-Find op near-constant (path compression + union by size). Plus O(1) for the feasibility check. |
| **Space** | **O(n)** — parent/rank maps, one entry per computer. |

Essentially linear in the number of cables.

---

## 11. Common mistakes

- ❌ **Skipping the feasibility check.** Without `if (connections.length < n-1) return -1`, impossible inputs return a wrong positive number. It must come first.
- ❌ **Returning `components` instead of `components - 1`.** Merging `c` groups needs `c-1` bridges, not `c`. Off-by-one.
- ❌ **Trying to count/track redundant cables explicitly.** Unnecessary — the feasibility inequality *guarantees* there are enough (§5). Just count components and subtract one.
- ❌ **`unionSet` not signaling a merge.** You need it to return whether a real merge happened, so you can decrement the component count only on actual merges (not on redundant cables).
- ❌ **Counting a redundant cable as a merge.** A cable between two already-connected computers must **not** reduce the component count — that's what the `if (s1 == s2) return false` guards.
- ❌ **Using cable count as the component count.** Cables and components are different; components come from the Union-Find merges.

---

## 12. How to recognize this pattern next time

Building on the earlier Union-Find docs:

1. **Goal is "make everything one connected thing" / "how many groups"?** → connected components → **Union-Find** (count components: start at `n`, decrement per real merge).
2. **Is there a feasibility floor?** Many "connect/spanning" problems need ≥ `n−1` edges. Check it first and bail with a sentinel (`-1`) if unmet.
3. **Is the answer a formula on the component count** rather than the count itself? Here, bridging `c` components costs `c − 1`. Recognize "merge k groups into one → k−1 operations."
4. **Is there a resource that must cover the operations?** Prove (or trust) that the resource suffices — here the redundant-cable count is guaranteed by the same feasibility inequality.

This "count components, then `components − 1` to merge them, gated by a minimum-edges feasibility check" shape appears in network/graph-connection problems (redundant connections, spanning-tree-adjacent questions, "minimum links to connect").

> 💡 **Components → merges → feasibility:** "The transferable move is: reduce to a component count, translate the goal into a formula on that count (merge c groups = c−1 ops), and put a feasibility floor in front (enough edges to connect at all). Union-Find handles the counting; a little algebra handles the rest." 

---

## 13. Cheat sheet

**Recognize it:** "make network connected" = one connected component; answer is a formula on the **component count**, gated by a **minimum-cable feasibility** check.

**Recipe:**
```
1. if connections.length < n - 1        → return -1        (impossible: need ≥ n-1 cables)
2. init components = n; union every cable; decrement components on each REAL merge
3. return components - 1
```

**Union-Find engine:** parent map (`-1`=root), recursive `findSet` w/ path compression, `unionSet` by size — with `unionSet` returning a **boolean** so you count only real merges.

**Why it works:** merging `c` components needs `c−1` bridges; and `m ≥ n−1` algebraically guarantees ≥ `c−1` redundant cables to supply them (feasibility = sufficiency).

**Watch:** feasibility check first; return `components − 1` (not `components`); redundant cables (same-set unions) don't decrement the count and don't need to be counted.

**Complexity:** O(m) time, O(n) space.

> **One-line philosophy:** *"Make the network connected" is "turn c components into one," which costs exactly c−1 re-plugged cables — so count the components with Union-Find and return c−1, but first reject the impossible case (fewer than n−1 cables total), because that same inequality both makes connection possible and guarantees you have enough redundant, movable cables to do every bridge; the reusable skill is reducing to a component count, expressing the goal as a formula on it, and front-loading a feasibility floor.*
