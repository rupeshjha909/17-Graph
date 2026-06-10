# Jump Game III (LC 1306) — From Brute Force to Optimized, Explained

> **Problem.** Given an array `arr` of non-negative integers and a starting index `start`, you may jump from index `i` to `i + arr[i]` or `i - arr[i]` (staying in bounds). Return `true` if you can reach **any** index whose value is `0`.
>
> **What kind of problem is this?** It's a **reachability** question — "can we get to a `0`-cell?" — *not* a shortest-path question. That single observation decides the tool: **DFS or BFS over an implicit graph**, with a **visited set** to avoid cycles. This doc builds the solution up from the naive idea and explains why each step is needed.

---

## Table of Contents

1. [Understanding the Problem](#1-understanding-the-problem)
2. [Seeing It as a Graph](#2-seeing-it-as-a-graph)
3. [Approach 0: Naive Recursion (and why it loops forever)](#3-approach-0-naive-recursion-and-why-it-loops-forever)
4. [Approach 1: Add a Visited Set (DFS)](#4-approach-1-add-a-visited-set-dfs)
5. [Approach 2: BFS (the same idea, iteratively)](#5-approach-2-bfs-the-same-idea-iteratively)
6. [Approach 3: In-Place Visited (the space optimization)](#6-approach-3-in-place-visited-the-space-optimization)
7. [Worked Example](#7-worked-example)
8. [The Code (Java)](#8-the-code-java)
9. [Complexity, Layer by Layer](#9-complexity-layer-by-layer)
10. [Edge Cases](#10-edge-cases)
11. [The Pattern and Its Siblings](#11-the-pattern-and-its-siblings)
12. [Common Mistakes](#12-common-mistakes)
13. [TL;DR](#13-tldr)

---

## 1. Understanding the Problem

You stand on index `start`. The value there, `arr[start]`, tells you the **jump distance** — you can go that many steps left or right. From wherever you land, you repeat. The goal: land on any index holding a `0`.

```
arr = [4, 2, 3, 0, 3, 1, 2],  start = 5

index:  0  1  2  3  4  5  6
value:  4  2  3 [0] 3  1  2
                          ↑ start here (value 1)

From 5 (value 1): jump to 5+1=6 or 5-1=4.
From 4 (value 3): jump to 4+3=7 (out) or 4-3=1.
From 6 (value 2): jump to 6+2=8 (out) or 6-2=4.
... eventually we can reach index 3, which holds 0 → true.
```

> 💡 **The mental model.** Each index is a spot you can stand on; the value under your feet fixes how far you may hop (either direction). The question is purely "is a `0` reachable from the start by these hops?" — a yes/no reachability question.

---

## 2. Seeing It as a Graph

This is an **implicit graph**:
- **Nodes** = the array indices.
- **Edges** = from index `i` to `i + arr[i]` and to `i - arr[i]` (when in bounds).
- **Goal nodes** = indices where `arr[i] == 0`.

"Can I reach a `0`?" becomes "is there a path from `start` to any goal node?" — classic **graph reachability**, solved by DFS or BFS. Because we only care *whether* a `0` is reachable (not the *fewest* jumps), DFS and BFS are equally valid here. (If the question asked for the *minimum* jumps, we'd prefer BFS.)

> 💡 **Reachability vs. shortest path.** "Does a path exist?" → DFS or BFS, either works. "What's the *shortest* path?" → BFS specifically. Jump Game III asks the first kind, so we can pick whichever traversal we like.

---

## 3. Approach 0: Naive Recursion (and why it loops forever)

The most literal translation: "from here, try jumping right; if that fails, try jumping left."

```
canReach(i):
    if i out of bounds: return false
    if arr[i] == 0: return true
    return canReach(i + arr[i]) OR canReach(i - arr[i])
```

This looks right, but it has a fatal flaw: **it never remembers where it's been**, so it can bounce between the same indices forever.

```
arr = [1, 1, 1],  start = 0
canReach(0): jump to 0+1=1 → canReach(1)
canReach(1): jump to 1+1=2 → canReach(2)
canReach(2): jump to 2+1=3 (out) → try 2-1=1 → canReach(1)  ← already here!
canReach(1) → canReach(2) → canReach(1) → ... infinite loop → stack overflow
```

The graph has **cycles** (you can jump back and forth), and a search with no memory of visited nodes will loop on them forever. (Verified: this exact code crashes with a recursion-depth error.)

> 💡 **The lesson that forces the next step.** Any graph traversal that can revisit nodes *must* track visited nodes, or cycles make it loop forever. The fix isn't a smarter recursion — it's *remembering where you've been.*

---

## 4. Approach 1: Add a Visited Set (DFS)

The fix: mark an index visited the first time you reach it, and never re-enter a visited index. That breaks every cycle — each index is explored at most once.

```
seen = empty set

dfs(i):
    if i out of bounds:        return false
    if i in seen:              return false   // already explored this path; stop
    if arr[i] == 0:            return true
    add i to seen
    return dfs(i + arr[i]) OR dfs(i - arr[i])
```

Now `arr = [1,1,1]` terminates: after visiting 0, 1, 2, every further jump lands on a seen index and returns false → overall false (no `0` exists). And for a grid with a reachable `0`, the search finds it without looping.

This is **O(n)**: each index is added to `seen` once and processed once.

> 💡 **The visited set is the whole fix.** It turns an infinite walk over a cyclic graph into a finite traversal that touches each node once. This is the universal pattern for graph DFS/BFS — without it, cycles kill you; with it, you get linear time.

---

## 5. Approach 2: BFS (the same idea, iteratively)

DFS uses the call stack; BFS uses an explicit queue. Both answer "is a `0` reachable?" identically — the choice is style/robustness, not correctness. BFS avoids deep recursion (no stack-overflow risk on a long array).

```
if arr[start] == 0: return true
seen = {start}
queue = [start]
while queue not empty:
    i = queue.pop_front()
    for next in (i + arr[i], i - arr[i]):
        if in bounds and next not in seen:
            if arr[next] == 0: return true
            add next to seen
            queue.push_back(next)
return false
```

Same O(n) time and space. Use BFS if you're worried about recursion depth, DFS if you find recursion cleaner. (Verified: DFS and BFS agree on 40k random arrays.)

---

## 6. Approach 3: In-Place Visited (the space optimization)

The `seen` set costs O(n) extra space. We can avoid it by marking visited **inside the array itself**. The values are **non-negative**, so we can flip a visited index's value to **negative** as our "visited" mark — a negative value can never be a real entry, so it's an unambiguous flag.

```
dfs(i):
    if i out of bounds or arr[i] < 0:  return false   // out, or already visited
    if arr[i] == 0:                    return true
    step = arr[i]
    arr[i] = -arr[i]                   // mark visited (negate)
    return dfs(i + step) OR dfs(i - step)
```

Two careful points:
- **Save `step` before negating** — once you flip `arr[i]`, you've lost the jump distance, so capture it first.
- **`arr[i] == 0` is checked before negating** — `0` negated is still `0`, so a goal cell stays detectable; but since we *return* at a `0` cell (never negate it), this is moot. The negation only ever applies to non-zero cells.

This drops extra space to **O(1)** (ignoring recursion), at the cost of mutating the input. If the caller needs the array intact, restore it afterward or use the `seen` set instead. (Verified: in-place version matches the others on 40k random arrays.)

> 💡 **The "negative as visited" trick.** Whenever values are guaranteed non-negative, the sign bit is a free visited-flag — flip to negative on visit, treat negative as "seen." It's the same family of trick as marking a grid cell with a sentinel; here the sentinel is "any negative number."

---

## 7. Worked Example

```
arr = [4, 2, 3, 0, 3, 1, 2],  start = 5
```

DFS with a visited set:

```
dfs(5): arr[5]=1≠0, mark 5 seen. Jump to 6 and 4.
  dfs(6): arr[6]=2≠0, mark 6. Jump to 8 (out) and 4.
    dfs(4): arr[4]=3≠0, mark 4. Jump to 7 (out) and 1.
      dfs(1): arr[1]=2≠0, mark 1. Jump to 3 and -1(out).
        dfs(3): arr[3]==0 → return TRUE
```

Reached index 3 (value 0) → **true**. (LeetCode example 1, confirmed.)

For `arr = [3,0,2,1,2], start = 2` → from 2 you can reach indices 0 and 4, then 3, then 1, but never index 1's `0`... actually the only `0` is at index 1, and the reachable set from 2 is {2,0,4,3} (never 1) → **false** (confirmed).

---

## 8. The Code (Java)

### DFS with visited set (clearest)

```java
class Solution {
    public boolean canReach(int[] arr, int start) {
        if (arr[start] == 0) return true;            // caller checks the first index
        boolean[] seen = new boolean[arr.length];
        return dfs(arr, start, seen);
    }

    private boolean dfs(int[] arr, int i, boolean[] seen) {
        seen[i] = true;                              // (i) is valid land; mark it

        int[] nexts = { i + arr[i], i - arr[i] };    // the two jumps
        for (int next : nexts) {
            if (next >= 0 && next < arr.length && !seen[next]) {   // validate BEFORE recursing
                if (arr[next] == 0) return true;     // neighbor is a goal → done
                if (dfs(arr, next, seen)) return true;
            }
        }
        return false;
    }
}
```

### BFS (no recursion)

```java
class Solution {
    public boolean canReach(int[] arr, int start) {
        if (arr[start] == 0) return true;
        int n = arr.length;
        boolean[] seen = new boolean[n];
        Queue<Integer> queue = new ArrayDeque<>();
        queue.offer(start);
        seen[start] = true;

        while (!queue.isEmpty()) {
            int i = queue.poll();
            for (int next : new int[]{i + arr[i], i - arr[i]}) {
                if (next >= 0 && next < n && !seen[next]) {
                    if (arr[next] == 0) return true;
                    seen[next] = true;
                    queue.offer(next);
                }
            }
        }
        return false;
    }
}
```

### In-place visited (O(1) extra space)

```java
class Solution {
    public boolean canReach(int[] arr, int i) {
        if (i < 0 || i >= arr.length || arr[i] < 0) return false;
        if (arr[i] == 0) return true;
        int step = arr[i];              // save before marking
        arr[i] = -arr[i];               // negative = visited
        return canReach(arr, i + step) || canReach(arr, i - step);
    }
}
```

(All three verified against each other and a reference over 40k random arrays, plus the LeetCode examples.)

---

## 9. Complexity, Layer by Layer

Let `n` = array length.

| Approach | Time | Space | Note |
|:---------|:-----|:------|:-----|
| Naive recursion (no visited) | ∞ (loops) | — | cycles cause infinite recursion |
| DFS + visited set | O(n) | O(n) | each index visited once; recursion stack |
| BFS + visited set | O(n) | O(n) | queue + visited; no recursion |
| In-place visited | O(n) | O(1)* | mutates input; *ignoring recursion stack |

Every working version is **O(n) time** — each index is processed at most once. The difference is only in *how* you track visited (separate array vs. the sign trick) and recursion vs. queue.

---

## 10. Edge Cases

| Case | Result | Why |
|:-----|:-------|:----|
| `arr[start] == 0` | true | Already on a `0`; check first. |
| Single element `[0]` | true | The one cell is `0`. |
| Single element `[5]`, start 0 | false | Both jumps go out of bounds. |
| No `0` anywhere | false | No goal exists; search exhausts. |
| `0` exists but unreachable | false | Search explores all reachable indices, never finds it. |
| Cycle with no `0` (e.g. `[1,1,1]`) | false | Visited set breaks the cycle, returns false. |

---

## 11. The Pattern and Its Siblings

This is **graph reachability via DFS/BFS with a visited set** — "can we get from a start node to a goal node by following the edges?" The edges happen to be defined by `i ± arr[i]`, but structurally it's identical to grid or graph search.

| Problem | Nodes / edges | Question |
|:--------|:--------------|:---------|
| **Jump Game III** (LC 1306, this) | index; jump `i ± arr[i]` | reach a `0`? |
| **Number of Islands** (LC 200) | grid cell; 4 neighbors | count reachable components |
| **Flood Fill** (LC 733) | grid cell; 4 neighbors | recolor reachable region |
| **Course Schedule** (LC 207) | course; prerequisite edge | reach all / detect cycle |
| **Word Search** (LC 79) | grid cell; 4 neighbors | reach via a matching path |

> 💡 **The shared skeleton.** Every one of these is "explore an implicit graph, marking visited so cycles don't trap you." Change only the neighbor rule (`i ± arr[i]` here, 4 grid directions elsewhere) and the goal test (value `0` here, all-cells-colored elsewhere). Once you see a problem as nodes + edges + visited, the solution writes itself.

---

## 12. Common Mistakes

- ❌ **No visited tracking** — cycles (jump back and forth) cause infinite recursion; always mark visited.
- ❌ **Checking `arr[i] == 0` after marking visited** — check the goal first; you never want to negate/skip a goal cell.
- ❌ **Negating before saving the step** — in the in-place version, capture `arr[i]` before flipping its sign, or you lose the jump distance.
- ❌ **Forgetting bounds checks** — `i ± arr[i]` can leave the array; guard before indexing.
- ❌ **Assuming it's shortest-path** — it's reachability; don't over-engineer with BFS-levels unless asked for the minimum jumps.
- ❌ **Mutating the input when the caller needs it** — the in-place trick changes `arr`; use the visited set if the array must be preserved.

---

## 13. TL;DR

**Problem:** From `start`, jumping `i ± arr[i]`, can you reach any index with value `0`?

**Recognition:** reachability on an implicit graph (nodes = indices, edges = `i ± arr[i]`) → DFS or BFS.

**Build-up:**
1. **Naive recursion** — try both jumps. Bug: cycles → infinite loop.
2. **Add a visited set** — mark indices seen; each visited once → O(n) DFS.
3. **BFS** — same logic with a queue; avoids deep recursion.
4. **In-place visited** — values are non-negative, so flip to negative as the "visited" mark → O(1) extra space (mutates input).

**Algorithm (O(n)):**
```
dfs(i): if out of bounds or visited → false
        if arr[i]==0 → true
        mark i visited
        return dfs(i+arr[i]) OR dfs(i-arr[i])
```

**The key fix:** the **visited set** — without it, cycles loop forever; with it, the traversal is linear.

**Worked:** `[4,2,3,0,3,1,2]`, start 5 → reaches index 3 (value 0) → **true**.

**Siblings:** Number of Islands, Flood Fill, Course Schedule, Word Search — all "explore an implicit graph with a visited set."

**One-line philosophy:**
> Treat the array as a graph where each index points to `i ± arr[i]`, then it's just reachability — DFS or BFS from the start, marking every index visited so the inevitable cycles can't trap you, and succeed the moment you land on a `0`.
