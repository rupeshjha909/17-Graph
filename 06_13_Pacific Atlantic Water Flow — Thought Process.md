# Pacific Atlantic Water Flow — Thought Process

> **Problem (LeetCode 417).** An `m × n` island borders the Pacific (top + left edges) and Atlantic (bottom + right edges). `heights[r][c]` is a cell's height. Water flows to a neighbor (N/S/E/W) if that neighbor's height is **≤** the current cell's height. Return all cells from which water can reach **both** oceans.
>
> **The killer insight: reverse the flow.** Instead of asking "from each cell, can water flow *down* to each ocean?" (expensive — a separate search per cell), start *at* each ocean and climb *uphill* to find every cell that can drain into it. Two multi-source searches (one per ocean), then intersect. This is the same boundary-processing family as Closed Islands / Surrounded Regions, with a reversal twist.

---

## Table of Contents

1. [Understanding the Flow](#1-understanding-the-flow)
2. [The Naive Approach (and Why It's Slow)](#2-the-naive-approach-and-why-its-slow)
3. [The Key Insight: Reverse the Flow](#3-the-key-insight-reverse-the-flow)
4. [Why "Climb Uphill" Is the Reversed Rule](#4-why-climb-uphill-is-the-reversed-rule)
5. [The Two-Search-Then-Intersect Strategy](#5-the-two-search-then-intersect-strategy)
6. [The Algorithm](#6-the-algorithm)
7. [A Full Worked Example](#7-a-full-worked-example)
8. [The Code (Java)](#8-the-code-java)
9. [Relationship to the Boundary-Processing Family](#9-relationship-to-the-boundary-processing-family)
10. [Edge Cases](#10-edge-cases)
11. [Complexity](#11-complexity)
12. [The Pattern and Its Siblings](#12-the-pattern-and-its-siblings)
13. [Common Mistakes](#13-common-mistakes)
14. [TL;DR](#14-tldr)

---

## 1. Understanding the Flow

Water sits on each cell. It flows to an adjacent cell (north/south/east/west) **if that neighbor is at the same height or lower** (`neighbor ≤ current`). Water that reaches a cell on the Pacific border (top or left edge) drains into the Pacific; a cell on the Atlantic border (bottom or right edge) drains into the Atlantic.

We want the cells where water can reach **both** oceans.

```
Pacific touches:  top edge + left edge
Atlantic touches: bottom edge + right edge

       Pacific
      ┌─────────┐
 P    │         │
 a    │  island  │   A t l a n t i c
 c    │         │
      └─────────┘
       Atlantic
```

A cell drains to an ocean if there's a path of **non-increasing** heights from it to that ocean's border.

> 💡 **The picture.** Imagine pouring water on a cell. It rolls downhill (or across flat ground) toward the edges. The question: which cells have a downhill path to *both* the top/left edges AND the bottom/right edges?

---

## 2. The Naive Approach (and Why It's Slow)

The direct approach: for **each cell**, run a search following the downhill flow, and check whether it reaches the Pacific border and whether it reaches the Atlantic border.

```
for each cell (r, c):
    if DFS-downhill(r,c) reaches Pacific AND reaches Atlantic:
        add (r, c) to result
```

This works, but it's **O((m·n)²)**: you run a full grid search from every one of the `m·n` cells, and each search can touch all `m·n` cells. For a 200×200 grid that's 40,000 searches over 40,000 cells — far too slow.

The problem: tons of repeated work. Many cells share the same downhill paths, but the naive approach re-explores them from scratch every time.

---

## 3. The Key Insight: Reverse the Flow

Flip the question around. Instead of "from each cell, can water flow **down** to the ocean?", ask:

> **"Starting from the ocean, which cells can water have flowed **down** from?"**

If you stand at an ocean-border cell and walk *backwards* up the flow, you reach every cell that could drain into that ocean. Since the forward rule is "flow to `neighbor ≤ current`," the reversed rule (walking backward) is "move to `neighbor ≥ current`" — climb uphill or stay level.

So:
- **Pacific-reachable set** = all cells reachable by climbing uphill starting from the **top row + left column**.
- **Atlantic-reachable set** = all cells reachable by climbing uphill starting from the **bottom row + right column**.
- **Answer** = cells in **both** sets (the intersection).

This turns `m·n` separate searches into just **two** searches (one per ocean), each O(m·n). Total: **O(m·n)** instead of O((m·n)²).

> 💡 **Why reversing is the whole trick.** Forward search asks the question once per cell (expensive, redundant). Reverse search asks it once per *ocean* (cheap, shared). The two ocean-searches together visit each cell a constant number of times, and the intersection gives the answer. Reversing a search to start from the "goal" instead of the "source" is a classic optimization — it's the same idea as multi-source BFS from all targets at once.

---

## 4. Why "Climb Uphill" Is the Reversed Rule

This is the part that trips people up, so let's nail it.

**Forward flow:** water moves from cell `A` to neighbor `B` if `height[B] ≤ height[A]` (downhill or flat).

**Reverse traversal:** we're standing at `B` (near the ocean) and asking "could water have come *from* `A` into me?" That's true exactly when the forward rule held: `height[B] ≤ height[A]`, i.e., `height[A] ≥ height[B]`. So from `B`, we move to neighbor `A` if `height[A] ≥ height[B]` — **the neighbor is higher or equal.**

```
Forward:  A ──(if B ≤ A)──► B        water flows A to B
Reverse:  B ──(if A ≥ B)──► A        we walk B to A (uphill)
```

So in the reverse DFS, the condition is `heights[neighbor] >= heights[current]` — climb to higher-or-equal cells. Each ocean border cell trivially drains to its ocean, so it's the starting point.

> 💡 **The mental flip.** "Water flows downhill into the ocean" becomes "from the ocean, reachability climbs uphill." The `≤` in the forward rule becomes `≥` in the reverse traversal. Getting this comparison backwards is the #1 bug in this problem.

---

## 5. The Two-Search-Then-Intersect Strategy

```
1. Pacific search:  multi-source DFS/BFS from ALL top-row and left-column cells,
                    climbing uphill, marking every reachable cell as pacific[r][c]=true.

2. Atlantic search: multi-source DFS/BFS from ALL bottom-row and right-column cells,
                    climbing uphill, marking every reachable cell as atlantic[r][c]=true.

3. Intersect:       any cell where pacific[r][c] AND atlantic[r][c] → add to result.
```

```
heights ──► [climb from Pacific edges → pacific set]
        ──► [climb from Atlantic edges → atlantic set]  ──► [intersect both sets] ──► result
```

Two independent reachability maps, then the overlap is the answer.

---

## 6. The Algorithm

```
pacific[][]  = all false
atlantic[][] = all false

// seed Pacific from top row and left column
for each column c:  dfs(0, c, pacific)        // top row
for each row r:      dfs(r, 0, pacific)        // left column

// seed Atlantic from bottom row and right column
for each column c:  dfs(R-1, c, atlantic)     // bottom row
for each row r:      dfs(r, C-1, atlantic)     // right column

dfs(r, c, ocean):
    ocean[r][c] = true
    for each of 4 neighbors (nr, nc):
        if in bounds AND not ocean[nr][nc] AND heights[nr][nc] >= heights[r][c]:
            dfs(nr, nc, ocean)                 // climb uphill

result = all (r,c) where pacific[r][c] AND atlantic[r][c]
```

The same `dfs` serves both oceans — you just pass which `ocean` boolean grid to fill.

---

## 7. A Full Worked Example

```
heights = [[1,2,2,3,5],
           [3,2,3,4,4],
           [2,4,5,3,1],
           [6,7,1,4,5],
           [5,1,1,2,4]]
```

**Pacific search** starts from the top row `(0,*)` and left column `(*,0)`, climbing uphill. **Atlantic search** starts from the bottom row `(4,*)` and right column `(*,4)`, climbing uphill.

For example, cell `(2,2)` with height 5:
- Pacific: `(2,2)=5 → (2,1)=4 → (1,1)=2 → (0,1)=2 → ...` wait, we climb uphill from the *ocean*. The Pacific DFS reaches `(2,2)` because there's an uphill chain from a Pacific-border cell up to it. ✓
- Atlantic: similarly reachable from the bottom/right via an uphill chain. ✓

The cells reachable from **both** (verified against brute force):
```
[[0,4], [1,3], [1,4], [2,2], [3,0], [3,1], [4,0]]
```

These are the cells where water can drain to both oceans. Notice the four *corners* are always in the result: the top-left corner `(0,0)` touches both Pacific edges trivially... actually `(0,0)` touches Pacific only — but `(0,4)` (top-right) touches Pacific (top) AND Atlantic (right), so it's always included. Each corner touches one edge of each ocean's pair, making corners natural members of the result.

---

## 8. The Code (Java)

```java
class Solution {
    private int[] dRow = {1, -1, 0, 0};
    private int[] dCol = {0, 0, 1, -1};

    public List<List<Integer>> pacificAtlantic(int[][] heights) {
        List<List<Integer>> result = new ArrayList<>();
        if (heights == null || heights.length == 0) return result;

        int rows = heights.length, cols = heights[0].length;
        boolean[][] pacific  = new boolean[rows][cols];
        boolean[][] atlantic = new boolean[rows][cols];

        // Seed Pacific from top row + left column
        // Seed Atlantic from bottom row + right column
        for (int col = 0; col < cols; col++) {
            dfs(heights, 0,        col, pacific,  rows, cols);   // top row    → Pacific
            dfs(heights, rows - 1, col, atlantic, rows, cols);   // bottom row → Atlantic
        }
        for (int row = 0; row < rows; row++) {
            dfs(heights, row, 0,        pacific,  rows, cols);   // left col   → Pacific
            dfs(heights, row, cols - 1, atlantic, rows, cols);   // right col  → Atlantic
        }

        // Cells reachable from both oceans
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (pacific[row][col] && atlantic[row][col]) {
                    result.add(Arrays.asList(row, col));
                }
            }
        }
        return result;
    }

    private void dfs(int[][] heights, int row, int col, boolean[][] ocean, int rows, int cols) {
        ocean[row][col] = true;
        for (int k = 0; k < 4; k++) {
            int newRow = row + dRow[k];
            int newCol = col + dCol[k];
            if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols
                && !ocean[newRow][newCol]
                && heights[newRow][newCol] >= heights[row][col]) {   // climb uphill
                dfs(heights, newRow, newCol, ocean, rows, cols);
            }
        }
    }
}
```

(Verified against brute force on 5k random grids.)

> 💡 **The `>=` is the heart of it.** In normal flow water goes to `<=` neighbors; here we walk *backward from the ocean*, so we go to `>=` neighbors. If you write `<=` by mistake, you'd be flowing downhill *away* from the ocean — completely wrong. The `!ocean[nr][nc]` check is the visited guard (prevents infinite loops on equal-height plateaus).

---

## 9. Relationship to the Boundary-Processing Family

This belongs to the same family as **Closed Islands** and **Surrounded Regions** that you've been documenting — all start their search **from the border inward**. But Pacific Atlantic has three distinguishing twists:

| Aspect | Closed Islands / Surrounded Regions | Pacific Atlantic |
|:-------|:------------------------------------|:-----------------|
| Borders | one notion of "border" (all 4 edges) | **two** different borders (Pacific = top/left, Atlantic = bottom/right) |
| Searches | one search from the border | **two** searches (one per ocean) |
| Combine | count / flip what survives | **intersect** the two reachable sets |
| Movement rule | plain adjacency (same value) | **height comparison** (reversed: climb uphill) |

> 💡 **Same skeleton, richer combine.** Closed Islands and Surrounded Regions do "border DFS, then look at the result." Pacific Atlantic does "border DFS *twice* (different borders, height-aware), then *intersect*." The "start from the boundary" idea is identical; the novelty is two boundaries + a set intersection + the reversed-flow comparison. If you understand why Closed Islands floods from the border, you understand why Pacific Atlantic floods from each ocean — it's the same move, doubled and intersected.

---

## 10. Edge Cases

| Case | Result | Why |
|:-----|:-------|:----|
| Single cell `[[5]]` | `[[0,0]]` | Touches all four edges → both oceans trivially |
| Single row `[[1,2,3]]` | every cell | Each cell touches top (Pacific) and bottom (Atlantic) since 1 row |
| Single column | every cell | Each cell touches left (Pacific) and right (Atlantic) |
| All equal heights | every cell | Water flows freely (≤ and ≥ both hold) → all reach both |
| Empty grid | `[]` | Guard returns early |
| Corners | always included | Top-right touches Pacific (top) + Atlantic (right); bottom-left touches Pacific (left) + Atlantic (bottom) |
| Strictly increasing toward center | center reaches both | High center can drain both ways |

> 💡 **The four corners are always in the answer.** Top-right `(0, C-1)` is on the Pacific's top edge AND the Atlantic's right edge — so it trivially reaches both. Same logic puts all four corners in the result.

---

## 11. Complexity

Let `R × C` = grid size.

- **Time: O(R × C)** — two DFS passes (Pacific + Atlantic), each visiting every cell at most once (the `ocean[][]` boolean prevents revisits). The final intersection scan is also O(R×C). The reversed-flow trick turns the naive O((R·C)²) into O(R·C).
- **Space: O(R × C)** — two boolean grids + recursion stack (worst case the whole grid is one uphill chain).

The speedup over naive (O((R·C)²) → O(R·C)) is the entire point of the reverse-flow insight.

---

## 12. The Pattern and Its Siblings

This is **multi-source DFS/BFS from the boundary, with reachability-set intersection** — a sophisticated variant of border-processing.

| Problem | Border treatment | Combine |
|:--------|:-----------------|:--------|
| **Pacific Atlantic** (LC 417, this) | climb uphill from each ocean's edges | **intersect** two reachable sets |
| **Closed Islands** (LC 1254) | sink from all edges | count remaining regions |
| **Surrounded Regions** (LC 130) | protect from all edges | flip the rest |
| **Number of Enclaves** (LC 1020) | sink from all edges | count remaining cells |
| **Walls and Gates** (LC 286) | multi-source BFS from all gates | distance to nearest gate |
| **01 Matrix** (LC 542) | multi-source BFS from all 0s | distance to nearest 0 |
| **Rotting Oranges** (LC 994) | multi-source BFS from all rotten | time to rot all |

> 💡 **Two big ideas combine here.** (1) *Reverse the search* — start from the destination (ocean) instead of the source (each cell), turning per-cell work into per-ocean work. (2) *Multi-source* — seed the search from *all* border cells at once (every top/left cell is a Pacific source). The intersection at the end is what makes "reaches BOTH" tractable. Recognizing "reaches multiple targets → search from each target and intersect" is the transferable lesson.

---

## 13. Common Mistakes

- ❌ **Searching forward from every cell (O((mn)²))** — reverse the flow and search from the oceans instead (O(mn)).
- ❌ **Using `<=` instead of `>=` in the reverse DFS** — when walking backward from the ocean, you climb to *higher-or-equal* neighbors; `<=` would flow the wrong way.
- ❌ **Forgetting the `!ocean[nr][nc]` visited check** — equal-height neighbors cause infinite recursion without it.
- ❌ **Only seeding from one cell per ocean** — every cell on the top row AND left column is a Pacific source; you must seed from all of them (multi-source).
- ❌ **Mixing up which ocean is which** — Pacific = top + left, Atlantic = bottom + right. Swapping them gives a wrong (often mirrored) answer.
- ❌ **Not intersecting** — the answer is cells reaching *both*; reporting either set alone is wrong.

---

## 14. TL;DR

**Problem:** Find cells from which water can flow to **both** the Pacific (top/left edges) and Atlantic (bottom/right edges); water flows to neighbors with height ≤ current.

**The killer trick — reverse the flow:** instead of "can each cell reach the ocean?" (O((mn)²)), ask "which cells can the ocean reach by climbing uphill?" (O(mn)). The forward rule `neighbor ≤ current` reverses to `neighbor ≥ current` (climb up/equal).

**Algorithm (O(R×C)):**
```
Pacific  set = multi-source DFS climbing uphill from top row + left column
Atlantic set = multi-source DFS climbing uphill from bottom row + right column
answer = cells in BOTH sets (intersection)
```

**Worked:** for the 5×5 example → `[[0,4],[1,3],[1,4],[2,2],[3,0],[3,1],[4,0]]` (the four corners always qualify).

**The key comparison:** reverse DFS uses `heights[neighbor] >= heights[current]` — getting this backwards (`<=`) is the #1 bug.

**Relationship to your other docs:** same "search from the boundary" family as Closed Islands / Surrounded Regions, but with **two** borders (the two oceans), **two** searches, an **intersection** to combine, and a **height-aware reversed** movement rule.

**One-line philosophy:**
> Don't ask whether each cell can reach the ocean — ask which cells the ocean can reach by climbing uphill, do it once per ocean, and intersect; reversing the search collapses per-cell work into per-ocean work and turns a quadratic scan into a linear one.
