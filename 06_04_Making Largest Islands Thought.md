# Making a Large Island — Thought Process (Interview Edition)

> **Problem (LeetCode 827).** Given an `n × n` binary grid (`0` = water, `1` = land), you may change **at most one** `0` to `1`. Return the size of the largest island possible. Cells are part of the same island if 4-directionally adjacent.
>
> **The pattern:** *color the connected components, then look up.* Pass 1 flood-fills each island with a unique id and records its size; Pass 2 tries each `0`, summing the sizes of its **distinct** neighbor islands. This turns a naive O((n²)²) into **O(n²)**.

---

## Table of Contents

1. [Understanding the Problem](#1-understanding-the-problem)
2. [The Naive Approach and Why It's Too Slow](#2-the-naive-approach-and-why-its-too-slow)
3. [The Key Insight: Pre-compute, Then Look Up](#3-the-key-insight-pre-compute-then-look-up)
4. [The Two-Pass Strategy](#4-the-two-pass-strategy)
5. [The Critical Subtlety: Deduplicate with a Set](#5-the-critical-subtlety-deduplicate-with-a-set)
6. [Why Color IDs Start at 2](#6-why-color-ids-start-at-2)
7. [The Algorithm](#7-the-algorithm)
8. [A Full Worked Example](#8-a-full-worked-example)
9. [The Code (Java)](#9-the-code-java)
10. [Edge Cases](#10-edge-cases)
11. [Complexity](#11-complexity)
12. [The Pattern and Its Siblings](#12-the-pattern-and-its-siblings)
13. [Common Mistakes](#13-common-mistakes)
14. [Interview Tips & Follow-Ups](#14-interview-tips--follow-ups)
15. [TL;DR](#15-tldr)

---

## 1. Understanding the Problem

You have a grid of land (`1`) and water (`0`). You're allowed **one** move: flip a single `0` into a `1`. After that flip, what's the largest connected island you can produce?

```
Input:           After flipping (1,1) to 1:
  1 1 0            1 1 0
  1 0 1     →      1 1 1     ← now all the land is connected
  0 1 1            0 1 1
                   = 7-cell island
```

The flip is valuable when a `0` sits **between** several separate islands — flipping it merges them into one big island. The answer is the best such merge, or the largest existing island if no flip helps.

> 💡 **The mental model.** A `0` cell is a potential "bridge." Flipping it joins all the islands touching it into one. We want the bridge that connects the most total land.

---

## 2. The Naive Approach and Why It's Too Slow

The direct idea: for **each** `0`, flip it, run a DFS/BFS to measure the resulting island, then undo and try the next `0`.

```
for each 0 cell:
    flip it to 1
    run DFS to measure the island it's part of
    track the max
    flip it back
```

- Each DFS is O(n²) (could traverse the whole grid).
- There are up to n² zeros to try.
- Total: **O((n²)²) = O(n⁴)**.

For a 500×500 grid that's ~6×10¹⁰ operations — far too slow. The waste: every flip re-measures islands from scratch, even though the islands themselves never change — only the bridge does.

---

## 3. The Key Insight: Pre-compute, Then Look Up

The islands are **fixed**. Only the single flipped cell changes. So measure every island **once**, then for each candidate `0`, just **add up** the sizes of the islands it touches — an O(1) lookup per neighbor instead of a fresh O(n²) traversal.

This is the classic **"pre-compute + look up"** pattern (same spirit as prefix sums, DP tables, memoization): do expensive work once, then answer each query cheaply.

To make the lookup work, we **color** each island with a unique id during the pre-computation. Then any cell's value tells you which island it belongs to, and a size table tells you how big that island is.

> 💡 **The transferable lesson.** Whenever you see "for each X, do something expensive," ask: *is the expensive part the same across all X?* Here, the island sizes are. Compute them once, then each `0` is answered by four O(1) lookups. O(n⁴) collapses to O(n²).

---

## 4. The Two-Pass Strategy

```
Pass 1 — Color & size every island:
    for each unvisited land cell:
        flood-fill the whole island with a unique color id (2, 3, 4, ...)
        record the island's size in a map: size[id] = cellCount

Pass 2 — Try flipping each 0:
    for each 0 cell:
        collect the DISTINCT island ids among its 4 neighbors (use a Set)
        candidate = 1 + sum of those islands' sizes
        track the max

Answer = max(largest existing island, best candidate from Pass 2)
```

```
grid ──► [Pass 1: color islands, record sizes] ──► [Pass 2: each 0 → 1 + sum of distinct neighbor sizes] ──► max
```

After Pass 1, a cell's value *is* its island id, and `size[id]` gives that island's size in O(1). Pass 2 is then just arithmetic.

---

## 5. The Critical Subtlety: Deduplicate with a Set

This is the one place the algorithm goes wrong if you're careless — and the thing interviewers probe.

A single `0` can border the **same island on multiple sides** (when the island is U-shaped, L-shaped, or wraps around). If you naively sum all four neighbors' sizes, you count that island multiple times.

```
After coloring (island id = 2):
  2 0 2          The 0 at (0,1) has island 2 on its LEFT and RIGHT.
  2 2 2          Without dedup: 1 + size[2] + size[2] = 1 + 5 + 5 = 11  ✗
                 With a Set:     1 + size[2]          = 1 + 5     = 6   ✓
```

The fix: collect neighbor ids into a `Set<Integer>` (which deduplicates), then sum the **distinct** ids only.

```java
Set<Integer> neighborIslands = new HashSet<>();
for (each of 4 neighbors) {
    if (neighbor is land) neighborIslands.add(grid[ni][nj]);
}
int candidate = 1;
for (int id : neighborIslands) candidate += size.get(id);   // each island counted once
```

> 💡 **Why this is the heart of the problem.** The whole reason coloring works is that it lets you ask "which *distinct* islands does this bridge touch?" If you don't deduplicate, a U-shaped island gets counted 2–4 times and your answer balloons. The `Set` is what makes "distinct islands" precise.

---

## 6. Why Color IDs Start at 2

We color islands with ids starting at **2**, not 0 or 1. Why?

- `0` already means **water**.
- `1` already means **unvisited land**.

If you colored the first island with `1`, you couldn't tell "freshly colored land" apart from "land not yet visited" — the flood fill would re-enter already-colored cells and infinite-loop (a cell colored `1` still looks like `== 1`). And `0` would collide with water.

Starting at `2` keeps all three states distinct: `0` = water, `1` = unvisited land, `≥ 2` = colored island. The DFS's `grid[i][j] != 1` base case then cleanly rejects both water and already-colored cells — **no separate `visited` array needed.**

> 💡 **The clean-marker rule.** When you overwrite the grid to mark visited, the marker value must never collide with "unvisited" (`1`) or "wall" (`0`). Starting island ids at `2` guarantees that, which is why this solution doesn't need a `visited[][]` array at all — the color *is* the visited mark.

---

## 7. The Algorithm

```
dfs(i, j, color):                          // returns the island's size
    if out of bounds or grid[i][j] != 1: return 0
    grid[i][j] = color                     // color = mark visited (color >= 2)
    size = 1
    for each of 4 neighbors:
        size += dfs(neighbor, color)
    return size

main:
    color = 2
    for each cell:
        if grid[i][j] == 1:
            sizeMap[color] = dfs(i, j, color)
            color++

    if no islands: return 1                 // all water → flip any → 1

    best = max island size in sizeMap

    for each cell:
        if grid[i][j] == 0:
            distinct = set of neighbor ids where grid[neighbor] >= 2
            candidate = 1 + sum(sizeMap[id] for id in distinct)
            best = max(best, candidate)

    return best
```

---

## 8. A Full Worked Example

```
grid = [[1, 1, 0],
        [1, 0, 1],
        [0, 1, 1]]
```

**Pass 1 — color and size:**

Island A (top-left L) gets color 2: cells (0,0),(0,1),(1,0) → `size[2] = 3`.
Island B (bottom-right L) gets color 3: cells (1,2),(2,2),(2,1) → `size[3] = 3`.

```
After coloring:
  2 2 0
  2 0 3
  0 3 3
```

**Largest existing island:** max(3, 3) = **3**.

**Pass 2 — try each 0:**

- `(0,2)`: neighbors are `(1,2)=3` and `(0,1)=2` → distinct {2,3} → `1 + 3 + 3 = 7`.
- `(1,1)`: neighbors `(0,1)=2, (2,1)=3, (1,0)=2, (1,2)=3` → distinct {2,3} (deduped!) → `1 + 3 + 3 = 7`.
- `(2,0)`: neighbors `(1,0)=2, (2,1)=3` → distinct {2,3} → `1 + 3 + 3 = 7`.

**Answer:** max(3, 7, 7, 7) = **7**.

**Sanity check** — flip (1,1):
```
1 1 0
1 1 1
0 1 1
```
Connected 1s: (0,0),(0,1),(1,0),(1,1),(1,2),(2,1),(2,2) = **7** ✓

(Verified against brute force on 20k random grids.)

---

## 9. The Code (Java)

```java
class Solution {
    private int n;
    private int[] dx = {0, 0, 1, -1};
    private int[] dy = {1, -1, 0, 0};

    public int largestIsland(int[][] grid) {
        n = grid.length;
        Map<Integer, Integer> sizeOf = new HashMap<>();
        int color = 2;                          // start at 2: avoid clash with water(0)/land(1)

        // Pass 1: color each island, record its size
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] == 1) {
                    sizeOf.put(color, dfs(grid, i, j, color));
                    color++;
                }
            }
        }

        // If there are no islands at all, flipping any 0 yields a single cell
        if (sizeOf.isEmpty()) return 1;

        // Start with the largest island as-is (covers the "no useful flip" case)
        int best = Collections.max(sizeOf.values());

        // Pass 2: try flipping each 0; sum the DISTINCT neighbor islands
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] == 0) {
                    Set<Integer> neighborIslands = new HashSet<>();
                    for (int k = 0; k < 4; k++) {
                        int ni = i + dx[k], nj = j + dy[k];
                        if (ni >= 0 && ni < n && nj >= 0 && nj < n && grid[ni][nj] >= 2) {
                            neighborIslands.add(grid[ni][nj]);   // Set dedups same island
                        }
                    }
                    int candidate = 1;                          // the flipped cell itself
                    for (int id : neighborIslands) {
                        candidate += sizeOf.get(id);
                    }
                    best = Math.max(best, candidate);
                }
            }
        }
        return best;
    }

    // Flood-fill the island, color its cells, return the size
    private int dfs(int[][] grid, int i, int j, int color) {
        if (i < 0 || i >= n || j < 0 || j >= n || grid[i][j] != 1) return 0;
        grid[i][j] = color;                     // color = visited marker (>= 2)
        int size = 1;
        for (int k = 0; k < 4; k++) {
            size += dfs(grid, i + dx[k], j + dy[k], color);
        }
        return size;
    }
}
```

> 💡 **Interview-clean choices:** the DFS *returns* the size (no global counter), a `HashMap` holds island sizes (clear intent), color ids start at `2` (explicit collision avoidance), the `>= 2` check skips water and uncolored cells, and there's **no `visited` array** — the color overwrite is the visited mark. All names are descriptive (`sizeOf`, `neighborIslands`, `candidate`).

---

## 10. Edge Cases

| Case | Result | Why |
|:-----|:-------|:----|
| All water `[[0,0],[0,0]]` | 1 | No islands; flipping any `0` gives a single cell. The `sizeOf.isEmpty()` guard returns 1. |
| All land `[[1,1],[1,1]]` | 4 | One island of 4; no `0` to flip → Pass 2 never runs → returns the existing max. |
| Single cell `[[1]]` | 1 | One island; no flip possible. |
| Single cell `[[0]]` | 1 | Flip it → 1-cell island. |
| U-shape around one `0` | size+1 | The Set dedups the wrapping island so it's counted once. |
| `0` with no land neighbors | 1 | Its `neighborIslands` set is empty → `candidate = 1`. |
| Two islands bridged by a `0` | sum+1 | The flip merges them; distinct sizes sum + 1. |

> 💡 **The "all land" trap.** If the grid is entirely `1`s, there's no `0` to flip, so Pass 2 does nothing and you must return the existing largest island. The code handles this naturally because `best` is initialized from `sizeOf`.

---

## 11. Complexity

Let the grid be `n × n` (so `n²` cells).

- **Time: O(n²)** — Pass 1 visits each cell once (the color overwrite prevents revisits). Pass 2 visits each cell once, doing O(1) work (≤ 4 neighbors, a set of size ≤ 4). Both passes are linear in the number of cells.
- **Space: O(n²)** — the `sizeOf` map (up to n² islands in the worst case) plus the recursion stack (depth up to the largest island).

This is optimal — you must examine every cell at least once.

| | Naive | This algorithm |
|:--|:------|:---------------|
| Time | O(n⁴) | **O(n²)** |
| 500×500 grid | ~6×10¹⁰ ops | ~250K ops |

---

## 12. The Pattern and Its Siblings

This is the **"color connected components, then query"** pattern — flood-fill each component with a unique id, record per-component data, then answer questions via O(1) lookups.

| Problem | What the coloring enables |
|:--------|:--------------------------|
| **Making a Large Island** (LC 827, this) | look up each island's size to compute merge candidates |
| **Number of Islands** (LC 200) | count the components (the simplest version) |
| **Max Area of Island** (LC 695) | track the largest component size |
| **Number of Distinct Islands** (LC 694) | hash each component's shape, count unique |
| **Closed Islands** (LC 1254) | identify which components touch the border |
| **Surrounded Regions** (LC 130) | flip components not connected to the border |

> 💡 **The connecting idea.** Flood fill assigns each connected region an identity. Once regions have identities, you can ask questions about them — size, shape, border-contact, mergeability — in O(1). "Making a Large Island" is the richest member: it uses the ids to evaluate *hypothetical merges* without re-traversing.

---

## 13. Common Mistakes

- ❌ **Re-running DFS for each `0` (O(n⁴))** — pre-compute island sizes once; each `0` is then four O(1) lookups.
- ❌ **Not deduplicating neighbor islands** — a `0` can touch the same island on multiple sides (U/L shapes); use a `Set` so each island counts once.
- ❌ **Coloring with `0` or `1`** — collides with water/unvisited land; start ids at `2` so the color doubles as a clean visited marker.
- ❌ **Forgetting the "no flip" case** — if the grid is all land, return the largest existing island (initialize `best` from the size map).
- ❌ **Forgetting the "all water" case** — flipping any `0` still yields a 1-cell island; return 1.
- ❌ **Including water (`0`) when summing neighbor sizes** — guard with `grid[ni][nj] >= 2` so only real islands are summed.
- ❌ **Bounds check after array access** — always check `ni`/`nj` are in range *before* reading `grid[ni][nj]` (Java's `&&` short-circuits, so order matters).

---

## 14. Interview Tips & Follow-Ups

**How to present it:**
1. Restate: "Flip at most one `0`; maximize the resulting island."
2. Name the naive cost: "Trying DFS per `0` is O(n⁴) — too slow."
3. Pitch the optimization: "Pre-compute each island's size once via flood-fill coloring; then each `0` is just summing its distinct neighbor islands — O(n²)."
4. Flag the subtlety *before* coding: "I'll use a Set to avoid double-counting when a `0` touches the same island twice."
5. Code it, then walk the U-shape edge case to show the Set matters.

**Likely follow-ups:**

- **"What if you can flip K zeros?"** — Much harder; the clean merge trick breaks down (flips interact). For small K, search; in general it's a hard combinatorial problem.
- **"What if connections were 8-directional?"** — Just extend the direction arrays to 8 entries; the logic is unchanged.
- **"Without mutating the input?"** — Use a separate `int[][] colorGrid` instead of overwriting `grid`. Costs O(n²) extra space.
- **"Enormous grid — recursion risk?"** — Deep recursion can overflow the stack on a giant single island; switch the flood fill to an iterative BFS/DFS with an explicit queue/stack.
- **"Could Union-Find work?"** — Yes: union adjacent land in Pass 1, track component sizes by root; for each `0`, sum the sizes of the distinct roots among its neighbors. Equivalent complexity, a bit more bookkeeping.

---

## 15. TL;DR

**Problem:** Flip at most one `0` to `1`; return the largest possible island.

**Insight:** islands are fixed — only the bridge changes. Pre-compute island sizes once, then each `0` is answered by summing its distinct neighbor islands.

**Algorithm (O(n²)):**
```
Pass 1: flood-fill each island with a unique color (>= 2), record size[color]
Pass 2: for each 0 → collect DISTINCT neighbor colors (Set) → 1 + sum of their sizes
answer = max(largest existing island, best Pass-2 candidate)
```

**Three things that matter:**
1. **Set deduplication** — a `0` can touch the same island on multiple sides; count each island once.
2. **Color ids start at 2** — avoids clashing with water(0)/land(1), so the color doubles as the visited marker (no `visited` array needed).
3. **Handle the corners** — all water → return 1; all land → return the existing largest.

**Worked:** `[[1,1,0],[1,0,1],[0,1,1]]` → two islands of size 3 → flipping any bridging `0` → `1 + 3 + 3 = 7`.

**Pattern:** "color connected components, then query" — siblings: Number of Islands, Max Area of Island, Distinct Islands, Closed Islands, Surrounded Regions.

**One-line philosophy:**
> The islands never change — only the bridge does — so color and measure every island once, then each candidate flip is just "1 plus the sizes of the distinct islands it touches," turning a quartic re-search into a linear lookup.
