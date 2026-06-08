# Number of Closed Islands — Thought Process

> **Problem (LeetCode 1254).** Given a 2D grid where `0` = land and `1` = water, a **closed island** is a connected group of `0`s that is **entirely surrounded by `1`s** — none of its cells touch the grid boundary. Return the number of closed islands.
>
> **The key insight:** any island touching the boundary is NOT closed (it "leaks" to the outside). So **eliminate the boundary-touching islands first, then count what's left.** Two phases: sink the boundary, count the interior.

---

## Table of Contents

1. [Understanding "Closed"](#1-understanding-closed)
2. [The Two-Phase Strategy](#2-the-two-phase-strategy)
3. [Why Boundary-First Makes It Clean](#3-why-boundary-first-makes-it-clean)
4. [The Algorithm](#4-the-algorithm)
5. [A Full Worked Example](#5-a-full-worked-example)
6. [The Code — Approach 1: Two-Phase (Java)](#6-the-code--approach-1-two-phase-java)
7. [The Code — Approach 2: Single DFS with Flag (Java)](#7-the-code--approach-2-single-dfs-with-flag-java)
8. [The Subtle Bug in Approach 2 (Don't Short-Circuit!)](#8-the-subtle-bug-in-approach-2-dont-short-circuit)
9. [Edge Cases](#9-edge-cases)
10. [Complexity](#10-complexity)
11. [The Pattern and Its Siblings](#11-the-pattern-and-its-siblings)
12. [Common Mistakes](#12-common-mistakes)
13. [TL;DR](#13-tldr)

---

## 1. Understanding "Closed"

A **closed** island is a blob of land (`0`s) where **no cell** in the blob sits on the grid's boundary (top row, bottom row, left column, or right column). If even ONE cell touches the edge, the island is "open" — it connects to the ocean.

```
Closed:                    NOT closed:
  1 1 1 1 1                 0 0 1 0 0    ← 0s on the boundary!
  1 0 0 0 1                 0 1 0 1 0
  1 0 1 0 1                 0 1 1 1 0
  1 0 0 0 1
  1 1 1 1 1
  (inner 0s don't touch     (border 0s leak to the edge)
   any edge → closed)
```

> 💡 **The mental picture.** Imagine the grid boundary is a coastline. Any land touching the coast is the mainland (not an island). Only land completely enclosed by water, with no path to the coast, is a true closed island.

---

## 2. The Two-Phase Strategy

```
Phase 1: ELIMINATE boundary-touching land
         → DFS from every 0 on the border, sink all connected 0s to 1s.
         → These are NOT closed islands. Destroy them.

Phase 2: COUNT the remaining islands
         → Every surviving 0 is interior land, fully enclosed.
         → Count connected components of 0s = count of closed islands.
```

```
grid ──► [Phase 1: sink all border-connected 0s] ──► [Phase 2: count remaining 0-components] ──► answer
```

After Phase 1 destroys the non-closed land, Phase 2 is just vanilla **Number of Islands** on the cleaned grid.

---

## 3. Why Boundary-First Makes It Clean

The alternative — running DFS from every 0, checking *during* the DFS whether any cell hits the boundary — works but is error-prone (you need a flag, and the flag logic has a subtle bug — Section 8). The two-phase approach **separates concerns**:

- Phase 1 handles the boundary question (which land touches the edge?)
- Phase 2 handles the counting question (how many islands remain?)

After Phase 1, you don't need to think about boundaries anymore — every surviving 0 is guaranteed interior. Phase 2 is a problem you already know how to solve.

> 💡 **The reframe.** "Count closed islands" = "destroy all non-closed land first, then count what's left." It's easier to eliminate the invalid than to validate the valid.

---

## 4. The Algorithm

### Phase 1: Sink boundary land

```
for each cell on the four borders of the grid:
    if the cell is 0 (land):
        DFS from it: turn all connected 0s into 1s (sink the island)
```

This eliminates every island that has even one cell on the boundary.

### Phase 2: Count remaining islands

```
count = 0
for each cell in the entire grid:
    if the cell is 0:
        count++                              // found a new closed island
        DFS from it: sink all connected 0s   // mark this island as counted
return count
```

The same `sink` DFS function is used in both phases — the only difference is *where* you start it (border vs interior) and *what* you do when you find a new component (discard vs count).

---

## 5. A Full Worked Example

```
Grid (0=land, 1=water):
  0 0 1 0 0
  0 1 0 1 0
  0 1 1 1 0
```

Expected: **1** closed island (the single 0 at position (1,2)).

**Phase 1 — Sink boundary land:**

Walk the border and DFS from every border `0`:

```
Border 0s: (0,0),(0,1),(0,3),(0,4),(1,0),(1,4),(2,0),(2,4)

DFS from (0,0): sinks (0,0) → neighbor (0,1) also 0 → sink
                → neighbor (1,0) also 0 → sink → (2,0) also 0 → sink
DFS from (0,3): sinks (0,3) → (0,4) → (1,4) → (2,4)

After Phase 1:
  1 1 1 1 1
  1 1 0 1 1      ← only (1,2) remains as 0
  1 1 1 1 1
```

All the border-touching land is now water. The only survivor is `(1,2)` — it was never connected to the boundary.

**Phase 2 — Count remaining islands:**

Scan the grid for 0s:
- `(1,2)` is 0 → count = 1, sink it.
- No more 0s.

Answer: **1** ✓

The 0 at `(1,2)` was the only true closed island — completely surrounded by water with no path to the boundary.

---

## 6. The Code — Approach 1: Two-Phase (Java)

```java
class Solution {
    public int closedIsland(int[][] grid) {
        int R = grid.length, C = grid[0].length;

        // Phase 1: sink all land connected to the boundary
        for (int r = 0; r < R; r++) {
            for (int c = 0; c < C; c++) {
                if ((r == 0 || r == R-1 || c == 0 || c == C-1) && grid[r][c] == 0) {
                    sink(grid, r, c, R, C);
                }
            }
        }

        // Phase 2: count remaining islands (these are closed)
        int count = 0;
        for (int r = 0; r < R; r++) {
            for (int c = 0; c < C; c++) {
                if (grid[r][c] == 0) {
                    count++;
                    sink(grid, r, c, R, C);   // mark this island as counted
                }
            }
        }
        return count;
    }

    private int[] dr = {1, -1, 0, 0};
    private int[] dc = {0, 0, 1, -1};
    
    private void sink(int[][] grid, int r, int c, int R, int C) {
        grid[r][c] = 1;                        // turn land into water
    
        for (int k = 0; k < 4; k++) {
            int nr = r + dr[k];
            int nc = c + dc[k];
            if (nr >= 0 && nr < R && nc >= 0 && nc < C && grid[nr][nc] == 0) {
                sink(grid, nr, nc, R, C);
            }
        }
    }
}
```

> 💡 **One `sink` function for both phases.** The DFS is identical — "turn this 0 and all connected 0s into 1s." In Phase 1 we call it from border cells (to destroy non-closed islands); in Phase 2 we call it from interior cells (to count and mark closed islands). Reusing the same function keeps the code clean.

---

## 7. The Code — Approach 2: Single DFS with Flag (Java)

Instead of two phases, run one DFS per component. During the DFS, track whether any cell is on the boundary. If not, it's a closed island.

```java
class Solution {
    public int closedIsland(int[][] grid) {
        int R = grid.length, C = grid[0].length;
        boolean[][] visited = new boolean[R][C];
        int count = 0;

        for (int r = 0; r < R; r++) {
            for (int c = 0; c < C; c++) {
                if (grid[r][c] == 0 && !visited[r][c]) {
                    if (dfs(grid, visited, r, c, R, C)) {
                        count++;   // this component never touched the boundary → closed
                    }
                }
            }
        }
        return count;
    }

    // Returns true if the component is CLOSED (never goes off-grid)
    private boolean dfs(int[][] grid, boolean[][] visited, int r, int c, int R, int C) {
        if (r < 0 || r >= R || c < 0 || c >= C) return false;   // went off-grid → NOT closed
        if (grid[r][c] == 1 || visited[r][c]) return true;       // water or already seen → fine

        visited[r][c] = true;

        // ⚠️ MUST explore ALL 4 directions — do NOT short-circuit with &&
        boolean down  = dfs(grid, visited, r+1, c, R, C);
        boolean up    = dfs(grid, visited, r-1, c, R, C);
        boolean right = dfs(grid, visited, r, c+1, R, C);
        boolean left  = dfs(grid, visited, r, c-1, R, C);

        return down && up && right && left;   // closed only if ALL directions stayed in bounds
    }
}
```

---

## 8. The Subtle Bug in Approach 2 (Don't Short-Circuit!)

This is the trap that makes Approach 2 error-prone, and understanding it is a senior signal:

```java
// ❌ WRONG — short-circuits and doesn't visit the whole component:
return dfs(r+1,c) && dfs(r-1,c) && dfs(r,c+1) && dfs(r,c-1);
```

If the first call returns `false` (touched boundary), Java's `&&` **short-circuits** — the remaining calls never run, so part of the component is **never marked visited**. That unvisited land gets found again later and counted as a separate (possibly "closed") island → **wrong count**.

```java
// ✓ CORRECT — evaluate ALL four, THEN combine:
boolean d = dfs(r+1,c);
boolean u = dfs(r-1,c);
boolean ri = dfs(r,c+1);
boolean le = dfs(r,c-1);
return d && u && ri && le;
```

By storing each result in a separate variable, all four DFS calls **always run**, the entire component is always fully marked, and the results are combined afterward.

> 💡 **This bug is exactly why Approach 1 (two-phase) is usually the better answer in interviews.** It doesn't have the short-circuit trap, it's easier to reason about, and it cleanly reuses the Number of Islands pattern you already know.

---

## 9. Edge Cases

| Case | Result | Why |
|:-----|:-------|:----|
| All water `[[1,1],[1,1]]` | 0 | No land at all |
| All land `[[0,0],[0,0]]` | 0 | Every cell is on the boundary → no closed island |
| 1×1 grid `[[0]]` | 0 | Single cell is on the boundary |
| Single interior 0 | 1 | One closed island of size 1 |
| All border 0s, one interior 0 | 1 | Phase 1 sinks the border; one survives |
| Multiple closed islands | count each | Phase 2 counts them separately |
| Large grid (100×100) | works | O(R×C) is efficient enough |

> 💡 **The "all land" trap.** `[[0,0],[0,0]]` returns **0**, not 1 — every cell touches the boundary, so nothing is closed. Phase 1 sinks everything; Phase 2 finds no 0s.

---

## 10. Complexity

- **Time: O(R × C)** — each cell is visited at most twice (once in Phase 1's border scan, once in Phase 2's island count). The DFS only visits cells once (they're sunk to 1 immediately).
- **Space: O(R × C)** — recursion stack in the worst case (entire grid is one big blob). Approach 2 also uses a `visited` array. Approach 1 modifies the grid in place (no extra array).

---

## 11. The Pattern and Its Siblings

This is the **"process boundary first, then process interior"** pattern — a variant of grid DFS where the boundary gets special treatment. The recognition cue: "surrounded by," "enclosed by," "not touching the border."

| Problem | Boundary treatment | Then what |
|:--------|:-------------------|:----------|
| **Closed Islands** (LC 1254, this) | Sink boundary land | Count remaining islands |
| **Surrounded Regions** (LC 130) | Mark boundary-connected `O`s as safe | Flip the rest to `X` |
| **Number of Enclaves** (LC 1020) | Sink boundary land | Count remaining land **cells** (not islands) |
| **Pacific Atlantic Water Flow** (LC 417) | BFS inward from each ocean boundary | Find cells reachable from both |
| **Number of Islands** (LC 200) | No boundary treatment | Count all connected components |

> 💡 **The family.** Closed Islands, Surrounded Regions, and Number of Enclaves are essentially the **same problem** with different final questions: count closed *islands* (components), flip enclosed *cells*, or count enclosed *cells*. All three use the same "sink boundary, process interior" two-phase approach. If you can do one, you can do all three.

---

## 12. Common Mistakes

- ❌ **Forgetting Phase 1** — counting all islands (including boundary-touching ones) overcounts.
- ❌ **Only checking if the starting cell is on the boundary** — a cell in the interior might connect *through other cells* to the boundary; you must DFS from the boundary to catch the entire connected region.
- ❌ **Short-circuiting `&&` in Approach 2** — leaves part of the component unmarked (Section 8).
- ❌ **Not sinking during Phase 2** — without sinking counted islands, you'd re-count the same island from a different starting cell.
- ❌ **Confusing 0/1 with Number of Islands** — in this problem `0` is land and `1` is water (opposite of some other problems). Read the problem statement carefully.
- ❌ **Thinking a closed island must be rectangular** — it can be any shape; "closed" means no cell touches the boundary, regardless of shape.

---

## 13. TL;DR

**Problem:** Count islands of `0`s that are completely surrounded by `1`s (no cell on the grid boundary).

**The insight:** boundary-touching islands are NOT closed. Eliminate them first, then count what's left.

**Algorithm (two-phase, O(R×C)):**
```
Phase 1: for each 0 on the border → DFS sink all connected 0s (turn to 1)
Phase 2: for each remaining 0     → count++ and DFS sink (standard Number of Islands)
return count
```

**Worked:** `[[0,0,1,0,0],[0,1,0,1,0],[0,1,1,1,0]]` → Phase 1 sinks all border 0s → only `(1,2)` survives → count = **1**.

**The trap in the single-DFS approach:** `&&` short-circuits, leaving part of the component unvisited. Store each DFS result in a variable, THEN combine with `&&`.

**Why two-phase is cleaner:** separates "which is boundary-touching?" from "how many remain?" and reuses the same `sink` function for both. Phase 2 is just Number of Islands.

**The family:** Closed Islands, Surrounded Regions (LC 130), Number of Enclaves (LC 1020) — all use "sink boundary, process interior."

**One-line philosophy:**
> Destroy what touches the coast, then count what's left — any land that survived the boundary purge is, by definition, a closed island.
