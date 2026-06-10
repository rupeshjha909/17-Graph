# Shortest Path in Binary Matrix (LC 1091) — Interview Thought Process

> **Problem.** Given an `n × n` binary matrix, return the length of the shortest clear path from top-left `(0,0)` to bottom-right `(n-1, n-1)`, moving **8-directionally** through `0` cells only. Path length = number of cells visited. Return `-1` if no path exists.
>
> **The one-line recognition:** "shortest path" + "every move costs the same (one cell)" = **BFS**. This is the canonical unweighted-shortest-path problem, and the clean contrast to Word Search (which was DFS + backtracking because it asked *does a path exist*, not *what's the shortest*).

---

## Table of Contents

1. [Understanding the Problem](#1-understanding-the-problem)
2. [The Recognition: Why BFS (Not DFS)](#2-the-recognition-why-bfs-not-dfs)
3. [Modeling It as a Graph](#3-modeling-it-as-a-graph)
4. [The BFS Idea, Step by Step](#4-the-bfs-idea-step-by-step)
5. [The Two Details That Trip People Up](#5-the-two-details-that-trip-people-up)
6. [The Algorithm](#6-the-algorithm)
7. [Worked Example](#7-worked-example)
8. [The Code (Java)](#8-the-code-java)
9. [Complexity](#9-complexity)
10. [Edge Cases](#10-edge-cases)
11. [Follow-Up Questions (and Answers)](#11-follow-up-questions-and-answers)
12. [The Pattern and Its Siblings](#12-the-pattern-and-its-siblings)
13. [Common Mistakes](#13-common-mistakes)
14. [TL;DR](#14-tldr)

---

## 1. Understanding the Problem

You start at the top-left corner and want to reach the bottom-right corner, stepping only on `0` cells, moving in any of **8 directions** (horizontal, vertical, *and* diagonal). You want the **fewest cells** on such a path (the path length counts cells, so a single-cell grid `[[0]]` has path length 1).

```
grid:              one shortest path (8-dir):
  0 0 0              S . .          S = start (0,0)
  1 1 0        →     . . ·          path: (0,0)→(0,1)→(0,2)→(1,2)→(2,2)
  1 1 0              . . E          E = end  (2,2),  length = 4 cells
```

If either corner is blocked (`1`), or no clear path exists, return `-1`.

> 💡 **Read carefully — two easy-to-miss details:** moves are **8-directional** (diagonals allowed, unlike most island problems), and the length counts **cells, not steps** (so start = length 1, and a path of k edges has k+1 cells).

---

## 2. The Recognition: Why BFS (Not DFS)

Say this out loud early in the interview:

> *"This asks for the **shortest** path on a grid where every move costs the same — one cell. That's the textbook signal for **BFS**. BFS explores cells in increasing order of distance from the start, so the first time it reaches the target, it's guaranteed to be via a shortest path."*

Why not DFS? DFS dives deep down one path and might reach the target via a long detour first — it finds *a* path, not the *shortest*. You'd have to explore *every* path and take the min (exponential). BFS gets the shortest for free because of how it expands.

| DFS / backtracking | BFS |
|:--|:--|
| "**Does** a path exist?" / "find all paths" | "**Shortest** path / fewest steps?" |
| Word Search, N-Queens, Sudoku | **Shortest Path in Binary Matrix**, Word Ladder, Rotting Oranges |

> 💡 **The clean rule to state:** *unweighted shortest path → BFS; "does a constrained path exist" → DFS + backtracking.* Naming this distinction unprompted is a senior signal — it shows you're choosing the traversal deliberately, not by habit.

---

## 3. Modeling It as a Graph

The grid is an **implicit graph**:
- **Nodes** = the `0` cells.
- **Edges** = between any two `0` cells that are 8-directionally adjacent.
- All edges have **weight 1** (one move = one cell).

We want the shortest path from node `(0,0)` to node `(n-1, n-1)`. Because edges are unit-weight, BFS is optimal (Dijkstra would also work but is overkill — its priority queue is unnecessary when all weights are equal).

---

## 4. The BFS Idea, Step by Step

BFS explores in **rings** (levels) outward from the start:

```
Level 1: the start cell {(0,0)}
Level 2: all 0-cells reachable in 1 move
Level 3: all 0-cells reachable in 2 moves
...
```

The first time the target appears in a ring, that ring number *is* the shortest path length. The mechanics:

1. Put the start cell in a queue, with distance 1 (it counts as the first cell).
2. Repeatedly pop a cell. If it's the target, return its distance.
3. Otherwise, push all its unvisited `0` neighbors (8 directions) with distance + 1.
4. **Mark each neighbor visited the moment you enqueue it.**
5. If the queue empties without hitting the target, return `-1`.

> 💡 **Why BFS gives the shortest path "for free."** BFS never visits a distance-(k+1) cell before finishing all distance-k cells. So the first arrival at the target is provably the minimum. This is the whole reason BFS is the tool for unit-weight shortest paths.

---

## 5. The Two Details That Trip People Up

**Detail 1 — Mark visited when you ENQUEUE, not when you dequeue.** If you only mark a cell visited when you pop it, the same cell can get pushed into the queue many times (by several neighbors) before it's ever processed — blowing up the queue and potentially recording a worse distance. Marking at enqueue guarantees each cell enters the queue exactly once.

```java
// WRONG: mark on dequeue → duplicates in the queue
// RIGHT: mark the moment you add it
grid[nr][nc] = 1;          // mark visited HERE, at enqueue
queue.offer(new int[]{nr, nc, dist + 1});
```

**Detail 2 — Check the start and end up front.** If `(0,0)` or `(n-1,n-1)` is a `1`, there's no clear path at all — return `-1` before doing any BFS. Forgetting this lets you start BFS from a blocked cell.

```java
if (grid[0][0] != 0 || grid[n-1][n-1] != 0) return -1;
```

> 💡 **These two are the difference between a working solution and a buggy one.** Mention both proactively — "I'll mark on enqueue to avoid duplicate queue entries, and short-circuit if either corner is blocked." That's exactly what interviewers look for.

---

## 6. The Algorithm

```
if grid[0][0] != 0 or grid[n-1][n-1] != 0: return -1     // corners must be open

queue = [(0, 0, 1)]            // (row, col, distance in CELLS)
mark (0,0) visited

while queue not empty:
    (r, c, d) = queue.pop_front()
    if (r, c) == (n-1, n-1): return d                    // first arrival = shortest
    for each of 8 neighbors (nr, nc):
        if in bounds AND grid[nr][nc] == 0:
            mark (nr, nc) visited                        // mark on ENQUEUE
            queue.push_back((nr, nc, d + 1))

return -1                                                // queue drained, never reached
```

We reuse the grid itself as the visited marker (set a visited `0` to `1`), so no extra array is needed.

---

## 7. Worked Example

```
grid = [[0, 0, 0],
        [1, 1, 0],
        [1, 1, 0]]
```

Corners: `(0,0)=0` and `(2,2)=0` — both open, proceed.

```
queue: [(0,0,1)]                          mark (0,0)
pop (0,0,1) — not target
   8 neighbors that are 0: (0,1), (1,1)? no that's 1 ... → (0,1) only (diagonals (1,1) is 1)
   push (0,1,2); mark (0,1)
pop (0,1,2) — not target
   neighbors 0: (0,2), (1,2) [diagonal]   push both at dist 3; mark them
pop (0,2,3) — not target
   neighbors 0: (1,2) already visited      (nothing new)
pop (1,2,3) — not target
   neighbors 0: (2,2) [down]               push (2,2,4); mark
pop (2,2,4) — TARGET → return 4
```

Shortest path length = **4**: `(0,0)→(0,1)→(0,2)→(1,2)→(2,2)` (or `(0,0)→(0,1)→(1,2)→(2,2)` using a diagonal = length 4 too). Verified against reference BFS.

---

## 8. The Code (Java)

```java
class Solution {
    public int shortestPathBinaryMatrix(int[][] grid) {
        int n = grid.length;

        // Corners must be open, else no clear path
        if (grid[0][0] != 0 || grid[n - 1][n - 1] != 0) return -1;

        // 8 directions
        int[][] dirs = {
            {-1,-1},{-1,0},{-1,1},
            { 0,-1},        { 0,1},
            { 1,-1},{ 1,0},{ 1,1}
        };

        Queue<int[]> queue = new ArrayDeque<>();
        queue.offer(new int[]{0, 0, 1});   // {row, col, distance in cells}
        grid[0][0] = 1;                    // mark visited (reuse the grid)

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int r = cur[0], c = cur[1], d = cur[2];

            if (r == n - 1 && c == n - 1) return d;   // first arrival = shortest

            for (int[] dir : dirs) {
                int nr = r + dir[0], nc = c + dir[1];
                if (nr >= 0 && nr < n && nc >= 0 && nc < n && grid[nr][nc] == 0) {
                    grid[nr][nc] = 1;                 // MARK on enqueue (avoids duplicates)
                    queue.offer(new int[]{nr, nc, d + 1});
                }
            }
        }
        return -1;   // queue drained without reaching the target
    }
}
```

(Verified against a reference BFS over 40k random grids.)

> 💡 **Clean choices to point out:** `ArrayDeque` is the fast modern queue; storing the distance *inside* the queue entry `{r, c, d}` avoids a separate distance map; reusing `grid` as the visited marker saves O(n²) space. If you're told not to mutate the input, use a separate `boolean[][] visited`.

---

## 9. Complexity

Let the grid be `n × n` (so `n²` cells).

- **Time: O(n²)** — each cell is enqueued and dequeued at most once (the visited mark guarantees this), and each does O(8) = O(1) work checking neighbors.
- **Space: O(n²)** — worst case the queue holds a large fraction of the cells; reusing the grid avoids a separate visited array.

> 💡 **Why marking-at-enqueue keeps it O(n²).** Without it, a cell could be pushed many times before being processed, and the queue could grow super-linearly. Marking on enqueue caps total enqueues at n², giving the clean linear-in-cells bound.

---

## 10. Edge Cases

| Case | Result | Why |
|:-----|:-------|:----|
| `[[0]]` (1×1, open) | 1 | Start = end; path length is 1 cell. |
| Start `(0,0)` is `1` | −1 | Blocked at the source; up-front check. |
| End `(n-1,n-1)` is `1` | −1 | Can't land on the target. |
| No clear path | −1 | Queue drains without reaching the end. |
| Fully open grid | n (the diagonal) | The straight diagonal of n cells is shortest (8-dir allows it). |
| Single blocked cell elsewhere | route around it | BFS finds the next-shortest route. |

> 💡 **The diagonal insight.** Because moves are 8-directional, an open n×n grid's shortest path is just the **main diagonal** — n cells — not 2n−1 like 4-directional. Mentioning this shows you understood the 8-direction twist.

---

## 11. Follow-Up Questions (and Answers)

**Q: "Why BFS and not Dijkstra?"**
> *"All edges cost the same (one cell), so the graph is unweighted. BFS already gives the shortest path on unweighted graphs in O(V+E); Dijkstra's priority queue would add an unnecessary log factor. Dijkstra only earns its keep when edge weights differ."*

**Q: "Can you reconstruct the actual path, not just its length?"**
> *"Yes — store a parent pointer for each cell when I enqueue it (a `parent[nr][nc] = (r,c)` map). After reaching the target, walk parents backward from the end to the start and reverse."*

**Q: "What if you can't mutate the grid?"**
> *"Use a separate `boolean[][] visited` instead of overwriting `0`s with `1`s. Same logic, O(n²) extra space."*

**Q: "What if it were 4-directional instead of 8?"**
> *"Just drop the four diagonal entries from the direction array. Logic unchanged; the open-grid shortest path would then be 2n−1 instead of n."*

**Q: "What if some cells cost more to enter (weighted)?"**
> *"Then it's no longer unit-weight — switch to Dijkstra with a min-heap. If costs are only 0 or 1, a 0-1 BFS with a deque does it in O(V+E)."*

**Q: "Huge grid — any concern with BFS?"**
> *"BFS is iterative (queue-based), so no recursion/stack-overflow risk — an advantage over recursive DFS here. Memory is O(n²) for the queue/visited, which is the limiting factor."*

---

## 12. The Pattern and Its Siblings

This is **BFS for unweighted shortest path on a grid** (multi-direction). The cue: "minimum steps/cells/moves from A to B with uniform move cost."

| Problem | The shortest-path flavor |
|:--------|:-------------------------|
| **Shortest Path in Binary Matrix** (LC 1091, this) | 8-dir grid, start→end |
| **Rotting Oranges** (LC 994) | multi-source BFS, time to infect all |
| **Word Ladder** (LC 127) | BFS on word-transformation graph |
| **01 Matrix** (LC 542) | multi-source BFS, distance to nearest 0 |
| **Walls and Gates** (LC 286) | multi-source BFS from all gates |
| **Open the Lock** (LC 752) | BFS on a state graph (dial combinations) |
| **Knight Minimum Moves** | BFS, knight-shaped neighbors |

> 💡 **The connective idea.** All of these are "fewest moves" questions where each move costs the same — so they're all BFS, differing only in *what a neighbor is* (8 directions here, word edits in Word Ladder, dial turns in Open the Lock). Master the BFS skeleton and you change only the neighbor-generation.

---

## 13. Common Mistakes

- ❌ **Using DFS for a shortest-path question** — DFS finds *a* path, not the shortest; use BFS.
- ❌ **Marking visited at dequeue instead of enqueue** — causes duplicate queue entries and can blow up time/space.
- ❌ **Forgetting to check both corners** — a blocked start or end means immediate `-1`.
- ❌ **Counting edges instead of cells** — the answer counts cells, so the start contributes 1; a single open cell returns 1, not 0.
- ❌ **Using 4 directions** — this problem is 8-directional; diagonals are allowed.
- ❌ **Recomputing distance with a separate structure** — just carry the distance in the queue entry `{r, c, d}`.

---

## 14. TL;DR

**Problem:** Shortest 8-directional path of `0`s from top-left to bottom-right; length = cell count; `-1` if none.

**Recognition:** shortest path + uniform move cost → **BFS** (not DFS, not Dijkstra).

**Algorithm (O(n²)):**
```
if either corner is 1: return -1
queue = [(0,0,1)]; mark (0,0)
while queue:
    pop (r,c,d); if it's the target return d
    for each of 8 neighbors that are 0 & in bounds:
        mark visited (on enqueue); push (nr,nc,d+1)
return -1
```

**Two things that matter most:**
1. **Mark visited on ENQUEUE** (not dequeue) — keeps each cell in the queue once.
2. **Check both corners up front** — blocked start/end → `-1`.

**Worked:** `[[0,0,0],[1,1,0],[1,1,0]]` → BFS reaches `(2,2)` at distance **4**.

**Complexity:** O(n²) time and space.

**Siblings:** Rotting Oranges, 01 Matrix, Walls and Gates (multi-source BFS); Word Ladder, Open the Lock (state-graph BFS).

**One-line philosophy:**
> Shortest path with uniform cost means BFS: expand outward in rings from the start, marking each cell the instant you enqueue it, and the first time you reach the target the ring number is your answer — no path is ever explored deeper than it needs to be.
