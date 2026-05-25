# Number of Islands — Complete Thought Process

A step-by-step deep dive into solving the classic Number of Islands problem. From problem understanding through DFS, BFS, and Union-Find — with the full reasoning at every stage.

---

## Table of Contents

1. [Problem Statement](#1-problem-statement)
2. [Understanding the Problem](#2-understanding-the-problem)
3. [First Instinct: How Do You Count Islands by Hand?](#3-first-instinct-how-do-you-count-islands-by-hand)
4. [Identifying the Pattern: Connected Components](#4-identifying-the-pattern-connected-components)
5. [Three Possible Approaches](#5-three-possible-approaches)
6. [Why DFS Is a Natural Fit](#6-why-dfs-is-a-natural-fit)
7. [Building the DFS Solution](#7-building-the-dfs-solution)
8. [The Direction Arrays Trick](#8-the-direction-arrays-trick)
9. [Why Mark Cells as '2' (Visited Tracking)](#9-why-mark-cells-as-2-visited-tracking)
10. [The Main Loop Logic](#10-the-main-loop-logic)
11. [Dry Run with Example](#11-dry-run-with-example)
12. [Alternative: BFS Solution](#12-alternative-bfs-solution)
13. [Alternative: Union-Find Solution](#13-alternative-union-find-solution)
14. [Edge Cases](#14-edge-cases)
15. [Complexity Analysis](#15-complexity-analysis)
16. [Common Mistakes](#16-common-mistakes)
17. [Related Problem: Max Area of Island (Largest Island)](#17-related-problem-max-area-of-island-largest-island)
18. [Related Problem: Coloring an Island](#18-related-problem-coloring-an-island)
19. [Related Problem: Biggest Island (with Diagonal)](#19-related-problem-biggest-island-with-diagonal)
20. [Related Problem: Making a Large Island](#20-related-problem-making-a-large-island)
21. [Comparison Table of All Island Problems](#21-comparison-table-of-all-island-problems)
22. [Other Variations & Follow-ups](#22-other-variations--follow-ups)
23. [Interview Tips](#23-interview-tips)

---

## 1. Problem Statement

> Given a 2D grid of `'1'`s (land) and `'0'`s (water), count the number of islands.
>
> An **island** is a group of `'1'`s connected horizontally or vertically (NOT diagonally).
> Assume all four edges of the grid are surrounded by water.

### Examples

#### Example 1
```
grid = [
  ["1","1","1","1","0"],
  ["1","1","0","1","0"],
  ["1","1","0","0","0"],
  ["0","0","0","0","0"]
]
Output: 1
```
All the 1s are connected (horizontally or vertically) → one big island.

#### Example 2
```
grid = [
  ["1","1","0","0","0"],
  ["1","1","0","0","0"],
  ["0","0","1","0","0"],
  ["0","0","0","1","1"]
]
Output: 3
```
Three separate clusters of 1s.

---

## 2. Understanding the Problem

Before any code, internalize three things.

### 2.1 What Defines an Island?
- A maximal group of `'1'`s where every cell is reachable from every other cell.
- Reachability is via horizontal/vertical neighbors (NOT diagonal).
- A single isolated `'1'` is its own island.

### 2.2 Adjacency Rules
A cell `(r, c)` has up to **4 neighbors**:
- Up: `(r-1, c)`
- Down: `(r+1, c)`
- Left: `(r, c-1)`
- Right: `(r, c+1)`

NOT diagonal. So `(r-1, c-1)` is NOT a neighbor.

### 2.3 Water Separates Islands
- `'0'` cells are barriers.
- Two `'1'`s separated by water (and unable to reach via 4-directional path) are **different** islands.

### 2.4 Edge Behavior
- Grid edges act like water (no neighbors beyond the boundary).
- An island at the corner is still an island.

---

## 3. First Instinct: How Do You Count Islands by Hand?

If you were doing this on paper, you'd probably:

1. **Scan top-to-bottom, left-to-right** looking for an unmarked `'1'`.
2. When you find one, **trace out** the entire island (highlight or circle all connected `'1'`s).
3. **Increment the count**.
4. Continue scanning, skipping cells you've already marked.

This natural process maps directly to an algorithm:

```
count = 0
for each cell (i, j):
    if grid[i][j] == '1' AND not yet visited:
        explore the entire connected island starting here
        mark all of it as visited
        count = count + 1
return count
```

The key question becomes: **how do we "explore the entire connected island"?**

That's where DFS or BFS comes in.

---

## 4. Identifying the Pattern: Connected Components

### The Underlying Problem
This is a classic **"Count Connected Components"** problem from graph theory:

- **Each `'1'` cell** = a graph node.
- **Adjacent `'1'`s** = an edge between them.
- **An island** = a connected component.

### Why Recognize the Pattern?
Once you see "connected components", you immediately know:
1. **Traversal algorithms apply**: DFS or BFS.
2. **Union-Find applies** (alternative approach).
3. **Time complexity** will be O(V + E) where V = cells and E = edges.

### Graph Terminology Mapping

| Grid Concept | Graph Concept |
|--------------|---------------|
| Cell with `'1'` | Vertex (node) |
| Adjacency between `'1'`s | Edge |
| Island | Connected component |
| Count islands | Count connected components |

Recognizing the abstract pattern unlocks the textbook solution.

---

## 5. Three Possible Approaches

### Approach A: DFS (Depth-First Search)
- Use recursion to dive deep into the island.
- Mark cells as visited as we go.
- **Pros**: clean code, intuitive.
- **Cons**: recursion depth can stack-overflow for huge grids (rare in practice).

### Approach B: BFS (Breadth-First Search)
- Use a queue to expand layer by layer.
- Mark cells as visited as we enqueue them.
- **Pros**: no recursion, safer for huge grids.
- **Cons**: slightly more verbose; needs a queue data structure.

### Approach C: Union-Find (Disjoint Set Union)
- Initially each `'1'` is its own set.
- For each pair of adjacent `'1'`s, union them.
- Count distinct sets containing `'1'`s.
- **Pros**: works for streaming/online variants (e.g., "Number of Islands II" where land is added incrementally).
- **Cons**: more code; overkill for this static version.

### Which to Pick?

For this problem (static grid, just count): **DFS or BFS** are equally good. The given code uses DFS — let's understand why.

---

## 6. Why DFS Is a Natural Fit

### The Intuition

When you find an unvisited `'1'`, you want to **mark its entire island as visited** before counting. DFS does this elegantly:

> "I'm at a `'1'`. Let me visit every reachable `'1'` from here recursively."

The recursion naturally handles the tree-like expansion of the island.

### Code Structure

```java
dfs(grid, i, j) {
    mark (i, j) as visited
    for each neighbor (ni, nj):
        if neighbor is land and not visited:
            dfs(grid, ni, nj)
}
```

That's it. The recursion does all the heavy lifting.

### Why Not Just a Simple Loop?

You might think: "Why recursion? Why not just iterate through neighbors?"

The issue: when you find a neighbor that's `'1'`, you need to also visit ITS neighbors, and THEIR neighbors, and so on. That's recursion (or its iterative equivalent using a stack/queue).

**Islands can have arbitrary shapes** — long snake-like, branching trees, big blobs. No simple loop can handle all shapes. We need traversal.

---

## 7. Building the DFS Solution

Let's build the solution step by step.

### Step 1: Outer Function — Scan the Grid

```java
public int numIslands(char[][] grid) {
    if (grid == null || grid.length == 0) return 0;
    
    int r = grid.length;
    int c = grid[0].length;
    int ans = 0;
    
    for (int i = 0; i < r; i++) {
        for (int j = 0; j < c; j++) {
            if (grid[i][j] == '1') {
                dfs(grid, i, j, r, c);   // explore the entire island
                ans++;                    // count it
            }
        }
    }
    
    return ans;
}
```

**Logic**: Scan every cell. When you find a `'1'` (an unvisited land cell), launch DFS to sink the whole island, then increment count.

### Step 2: DFS Function — Explore the Island

```java
private void dfs(char[][] grid, int curr, int curc, int r, int c) {
    grid[curr][curc] = '2';              // mark visited
    
    for (int k = 0; k < 4; k++) {        // try all 4 directions
        int nx = curr + dx[k];
        int ny = curc + dy[k];
        
        if (nx >= 0 && nx < r && ny >= 0 && ny < c && grid[nx][ny] == '1') {
            dfs(grid, nx, ny, r, c);
        }
    }
}
```

**Logic**:
1. Mark current cell as visited (change `'1'` → `'2'`).
2. For each of 4 directions, check if the neighbor is:
   - Within bounds.
   - A `'1'` (unvisited land).
3. If yes, recurse into that neighbor.

### Why Pass `r, c` as Parameters?

For bounds-checking. We could re-compute them as `grid.length` and `grid[0].length` inside DFS, but passing avoids that overhead. (Minor optimization.)

---

## 8. The Direction Arrays Trick

This is the **most elegant pattern** in grid traversal problems.

### The Pattern

```java
private int[] dx = {1, -1, 0, 0};
private int[] dy = {0, 0, 1, -1};
```

These two arrays together represent the 4 directions:
- `(dx[0], dy[0]) = (1, 0)`  → down
- `(dx[1], dy[1]) = (-1, 0)` → up
- `(dx[2], dy[2]) = (0, 1)`  → right
- `(dx[3], dy[3]) = (0, -1)` → left

### Using It in a Loop

```java
for (int k = 0; k < 4; k++) {
    int nx = curr + dx[k];
    int ny = curc + dy[k];
    // process (nx, ny)
}
```

One clean loop replaces 4 separate if-statements.

### Why This Is Better Than Manual If-Statements

#### Without direction arrays:
```java
// Check up
if (curr > 0 && grid[curr-1][curc] == '1') dfs(grid, curr-1, curc, r, c);
// Check down
if (curr < r-1 && grid[curr+1][curc] == '1') dfs(grid, curr+1, curc, r, c);
// Check left
if (curc > 0 && grid[curr][curc-1] == '1') dfs(grid, curr, curc-1, r, c);
// Check right
if (curc < c-1 && grid[curr][curc+1] == '1') dfs(grid, curr, curc+1, r, c);
```

Repetitive. Bug-prone. Hard to extend (8 directions? diagonal? 3D?).

#### With direction arrays:
```java
for (int k = 0; k < 4; k++) {
    int nx = curr + dx[k];
    int ny = curc + dy[k];
    if (nx >= 0 && nx < r && ny >= 0 && ny < c && grid[nx][ny] == '1') {
        dfs(grid, nx, ny, r, c);
    }
}
```

Concise. One bounds-check. Easy to extend.

### Variations of the Trick

#### For 8 directions (including diagonals)
```java
int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
int[] dy = {-1,  0,  1,-1, 1,-1, 0, 1};
```

#### For knight moves (chess)
```java
int[] dx = {-2, -2, -1, -1, 1, 1, 2, 2};
int[] dy = {-1, 1, -2, 2, -2, 2, -1, 1};
```

#### Alternative: 2D array of pairs
```java
int[][] dirs = {{1,0}, {-1,0}, {0,1}, {0,-1}};
for (int[] d : dirs) {
    int nx = curr + d[0];
    int ny = curc + d[1];
    // ...
}
```

Both are equally common. Pick your style.

### Why `dx` and `dy` Names?
- `dx` = "delta x" (change in row).
- `dy` = "delta y" (change in column).

Sometimes named `dr` (row) and `dc` (column) — both are conventions. Use what's clearest in your team.

---

## 9. Why Mark Cells as '2' (Visited Tracking)

This is the **second key trick** in the solution.

### The Problem: Infinite Loops

Without visited tracking, DFS would revisit the same cells forever:

```
At (0, 0) = '1'. Visit (0, 1) = '1'. From (0, 1), check neighbors:
   - Up (-1, 1): out of bounds. Skip.
   - Down (1, 1): visit if '1'.
   - Left (0, 0): '1' — visit it again! 
       → From (0, 0), visit (0, 1) again!
       → INFINITE LOOP!
```

We must **track which cells we've already visited**.

### Three Common Ways to Track Visited

#### Option 1: Modify the Grid In-Place (This Code's Choice)
```java
grid[curr][curc] = '2';  // or '0', or any non-'1' marker
```

**Pros**:
- No extra space (O(1) auxiliary memory).
- Clean code.

**Cons**:
- Mutates input — caller's grid changes.
- Need to restore if caller expects original grid.

#### Option 2: Separate Visited Array
```java
boolean[][] visited = new boolean[r][c];
// ... visited[i][j] = true
```

**Pros**:
- Doesn't modify input.

**Cons**:
- Extra O(r × c) space.

#### Option 3: Mark as '0' (Sink the Island)
```java
grid[curr][curc] = '0';  // treat as water
```

**Pros**:
- Same as Option 1 but reuses existing symbol.
- Subsequent scan won't pick this up as a `'1'`.

**Cons**:
- Mutates input.

### Why '2' Specifically?

The code chose `'2'` (not `'0'`) for visited marker. Why?

#### Mark as '0':
- Cell would look like water from then on.
- This works! Subsequent main loop won't trigger DFS here (because `grid[i][j] != '1'`).

#### Mark as '2':
- Same effect for THIS algorithm — it only triggers on `'1'`, ignores both `'0'` and `'2'`.
- But preserves distinction between original water and processed land.
- Useful if you need to restore the grid later (just change all `'2'` back to `'1'`).

For this specific problem, either works. The choice of `'2'` shows intent: "this was land but I've processed it".

### Why Mark BEFORE Recursing, Not After?

```java
// Correct: mark FIRST, then explore
dfs(grid, curr, curc) {
    grid[curr][curc] = '2';   // mark first
    for each direction: dfs(...)
}

// Wrong: explore first, then mark
dfs(grid, curr, curc) {
    for each direction: dfs(...)
    grid[curr][curc] = '2';   // mark last → INFINITE LOOP
}
```

If we mark AFTER recursing, neighbors will see this cell as still `'1'` and try to visit it → infinite recursion → stack overflow.

**Rule**: ALWAYS mark visited BEFORE exploring neighbors.

---

## 10. The Main Loop Logic

Re-examine the main loop:

```java
for (int i = 0; i < r; i++) {
    for (int j = 0; j < c; j++) {
        if (grid[i][j] == '1') {
            dfs(grid, i, j, r, c);
            ans++;
        }
    }
}
```

### Why This Works

Two phases per iteration:

#### Phase 1: Skip Water and Already-Visited
- If `grid[i][j] == '0'` (water): skip.
- If `grid[i][j] == '2'` (already part of a counted island): skip.

#### Phase 2: New Island Found
- If `grid[i][j] == '1'`: this is a NEW island we haven't seen before.
- Launch DFS to sink the entire island.
- After DFS returns, every cell in this island is now `'2'`.
- Increment count.

### Why DFS Doesn't Over-Count
After DFS, ALL cells in the island are `'2'`. As the outer loop continues, when we reach other cells of the same island, they're `'2'` not `'1'` → main loop's `if` skips them.

**Net effect**: each island triggers exactly ONE call to DFS, which counts it exactly once.

---

## 11. Dry Run with Example

Let's trace through:
```
grid = [
  ['1','1','0','0'],
  ['1','0','0','1'],
  ['0','0','1','1'],
  ['0','0','0','0']
]
```

### Iteration 1: Scanning Row 0

#### (0, 0) = '1' ✓
- Start DFS from (0, 0).
- Mark (0, 0) = '2'. Grid:
```
['2','1','0','0']
['1','0','0','1']
['0','0','1','1']
['0','0','0','0']
```
- Check 4 neighbors of (0, 0):
  - Down (1, 0) = '1' → recurse.
    - Mark (1, 0) = '2'.
    - Check neighbors of (1, 0):
      - Down (2, 0) = '0' → skip.
      - Up (0, 0) = '2' → skip (not '1').
      - Right (1, 1) = '0' → skip.
      - Left (1, -1) → out of bounds, skip.
    - Return.
  - Up (-1, 0) → out of bounds, skip.
  - Right (0, 1) = '1' → recurse.
    - Mark (0, 1) = '2'.
    - Check neighbors of (0, 1):
      - Down (1, 1) = '0' → skip.
      - Up (-1, 1) → out of bounds.
      - Right (0, 2) = '0' → skip.
      - Left (0, 0) = '2' → skip.
    - Return.
  - Left (0, -1) → out of bounds.
- DFS done. Grid:
```
['2','2','0','0']
['2','0','0','1']
['0','0','1','1']
['0','0','0','0']
```
- `ans = 1`.

#### (0, 1) = '2' → skip
#### (0, 2) = '0' → skip
#### (0, 3) = '0' → skip

### Iteration 2: Scanning Row 1

#### (1, 0) = '2' → skip
#### (1, 1) = '0' → skip
#### (1, 2) = '0' → skip
#### (1, 3) = '1' ✓
- Start DFS from (1, 3).
- Mark (1, 3) = '2'.
- Check neighbors:
  - Down (2, 3) = '1' → recurse.
    - Mark (2, 3) = '2'.
    - Check neighbors:
      - Down (3, 3) = '0' → skip.
      - Up (1, 3) = '2' → skip.
      - Right (2, 4) → out of bounds.
      - Left (2, 2) = '1' → recurse.
        - Mark (2, 2) = '2'.
        - Check neighbors:
          - Down (3, 2) = '0' → skip.
          - Up (1, 2) = '0' → skip.
          - Right (2, 3) = '2' → skip.
          - Left (2, 1) = '0' → skip.
        - Return.
    - Return.
  - Up (0, 3) = '0' → skip.
  - Right (1, 4) → out of bounds.
  - Left (1, 2) = '0' → skip.
- DFS done. Grid:
```
['2','2','0','0']
['2','0','0','2']
['0','0','2','2']
['0','0','0','0']
```
- `ans = 2`.

### Iterations 3-4: Rest of Grid
Everything is now either '0' or '2'. No more triggers.

### Final Answer
**2 islands** ✓

(First island: cells {(0,0), (0,1), (1,0)}. Second island: cells {(1,3), (2,2), (2,3)}.)

---

## 12. Alternative: BFS Solution

Same idea, using a queue instead of recursion.

```java
private void bfs(char[][] grid, int startR, int startC, int r, int c) {
    Queue<int[]> queue = new LinkedList<>();
    queue.offer(new int[]{startR, startC});
    grid[startR][startC] = '2';
    
    int[] dx = {1, -1, 0, 0};
    int[] dy = {0, 0, 1, -1};
    
    while (!queue.isEmpty()) {
        int[] cell = queue.poll();
        int curr = cell[0], curc = cell[1];
        
        for (int k = 0; k < 4; k++) {
            int nx = curr + dx[k];
            int ny = curc + dy[k];
            
            if (nx >= 0 && nx < r && ny >= 0 && ny < c && grid[nx][ny] == '1') {
                grid[nx][ny] = '2';  // mark BEFORE enqueue (avoid duplicate enqueues)
                queue.offer(new int[]{nx, ny});
            }
        }
    }
}
```

### Critical Difference from DFS
In BFS, mark cells as visited **when enqueuing**, not when dequeuing. Otherwise, the same cell can be enqueued multiple times before being processed → wrong behavior.

```java
// ✓ Correct: mark when adding to queue
if (grid[nx][ny] == '1') {
    grid[nx][ny] = '2';
    queue.offer(new int[]{nx, ny});
}

// ✗ Wrong: mark when dequeuing
// Cell can be added to queue multiple times before any is processed.
```

### When to Prefer BFS Over DFS
- **Huge grids** (millions of cells): DFS recursion can blow the stack.
- **Need shortest path** (not for this problem, but related).
- **Iterative preference**.

For interview, DFS is usually cleaner. Both score equally.

---

## 13. Alternative: Union-Find Solution

For this static grid problem, Union-Find is **overkill**. But useful to know for variations.

### The Idea
- Each `'1'` cell is initially its own component.
- For each `'1'` cell, union it with adjacent `'1'`s.
- Count distinct components at the end.

### Code Sketch

```java
class UnionFind {
    int[] parent, rank;
    int count;
    
    UnionFind(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
    }
    
    int find(int x) {
        if (parent[x] != x) parent[x] = find(parent[x]);
        return parent[x];
    }
    
    void union(int x, int y) {
        int px = find(x), py = find(y);
        if (px == py) return;
        // union by rank
        if (rank[px] < rank[py]) parent[px] = py;
        else if (rank[px] > rank[py]) parent[py] = px;
        else { parent[py] = px; rank[px]++; }
        count--;
    }
}

public int numIslands(char[][] grid) {
    int r = grid.length, c = grid[0].length;
    UnionFind uf = new UnionFind(r * c);
    
    // Count initial '1' cells
    int landCount = 0;
    for (int i = 0; i < r; i++)
        for (int j = 0; j < c; j++)
            if (grid[i][j] == '1') landCount++;
    uf.count = landCount;
    
    for (int i = 0; i < r; i++) {
        for (int j = 0; j < c; j++) {
            if (grid[i][j] == '1') {
                int id = i * c + j;
                // union with right and down neighbors
                if (j + 1 < c && grid[i][j+1] == '1') uf.union(id, id + 1);
                if (i + 1 < r && grid[i+1][j] == '1') uf.union(id, id + c);
            }
        }
    }
    
    return uf.count;
}
```

### When Union-Find Wins
- **"Number of Islands II"** (LC 305): land is added one at a time; report island count after each addition. Union-Find handles this incrementally; DFS would re-scan entire grid each time.

For static "count once" version: DFS is simpler.

---

## 14. Edge Cases

### 1. Empty Grid
```java
grid = []  // or null
```
Return 0. The code's null check handles this:
```java
if (grid == null || grid.length == 0) return 0;
```

### 2. Single Cell
```java
grid = [["1"]]  → 1
grid = [["0"]]  → 0
```

### 3. All Water
```java
grid = [["0","0"], ["0","0"]]  → 0
```

### 4. All Land
```java
grid = [["1","1"], ["1","1"]]  → 1 (everything connected)
```

### 5. Diagonal Only Connection
```java
grid = [["1","0"], ["0","1"]]  → 2 (diagonal doesn't count!)
```

Two separate islands. This trips up beginners — must use 4-directional, not 8.

### 6. Snake-Like Island
```java
grid = [
  ["1","1","1","1"],
  ["0","0","0","1"],
  ["1","1","1","1"],
  ["1","0","0","0"]
]
→ 1 (everything connected via the right edge then down)
```

DFS handles arbitrary shapes naturally.

### 7. Many Isolated 1s
```java
grid = [
  ["1","0","1","0","1"],
  ["0","1","0","1","0"],
  ["1","0","1","0","1"]
]
→ 8 (no '1' touches another '1' horizontally/vertically)
```

### 8. Single Row/Column
```java
grid = [["1","0","1","1","0","1"]]  → 3
grid = [["1"],["0"],["1"],["1"]]    → 2
```

### 9. Large Grid (Stack Overflow Risk)
For very large grids (say 1000×1000 of all `'1'`s), DFS recursion depth could reach 1M+ → **stack overflow**.

In Java, default stack size is ~512KB → ~10K-20K recursion depth.

For competitive programming with huge constraints: switch to BFS or iterative DFS using an explicit stack.

---

## 15. Complexity Analysis

### Time Complexity
**O(r × c)** where r = rows, c = columns.

Every cell is visited at most a constant number of times:
- Once by the outer scan.
- Once when first reached by DFS (then marked '2').

After being marked, the outer scan's `if` skips it, and DFS's bounds-check filters it out.

Total work: O(r × c).

### Space Complexity

#### Recursive Stack (DFS)
**O(r × c)** worst case.

For a snake-like island filling the entire grid, recursion depth = r × c.

For typical grids: O(min(r × c, path length of biggest island)).

#### Auxiliary Space
- Direction arrays: O(1).
- No extra grid (modifying in place): O(1).

**Total**: O(r × c) due to recursion stack in the worst case.

### Compare to BFS
- Same O(r × c) time.
- Queue space: up to O(r × c) in worst case (large frontier).
- BUT: avoids stack overflow on huge grids — controlled space.

### Compare to Union-Find
- Time: O(r × c × α(r × c)) where α is the inverse Ackermann function (effectively O(1)).
- Space: O(r × c) for parent and rank arrays.
- Slightly slower in practice for this problem; useful for streaming variants.

---

## 16. Common Mistakes

### Mistake 1: Forgetting to Mark Visited
- Result: infinite loop → stack overflow.
- Fix: ALWAYS mark cell before/while exploring.

### Mistake 2: Marking After Recursion
```java
// WRONG:
dfs(...) {
    for each direction: dfs(...)  // explores
    grid[i][j] = '2';              // marks (too late!)
}
```
- Neighbors recurse back into current cell while it's still `'1'`.
- Infinite loop.
- Fix: mark FIRST, then recurse.

### Mistake 3: Using `==` to Compare Strings (Other Languages)
In Java with `char`, `grid[i][j] == '1'` is correct (char equality).

In some pseudocode/languages, beginners write `grid[i][j] == 1` (int) when grid is `char`. This always returns false in Java. Watch types.

### Mistake 4: 8-Directional Movement
- Including diagonal moves changes the problem.
- For this problem: 4-directional only.

### Mistake 5: Bounds Check Errors
```java
if (nx >= 0 && nx < r && ny >= 0 && ny < c)  // ✓ correct
if (nx > 0 && nx < r && ny > 0 && ny < c)    // ✗ misses row 0 and col 0
if (nx >= 0 && nx <= r && ny >= 0 && ny <= c) // ✗ accesses out-of-bounds at r and c
```

Use `nx >= 0 && nx < r`. Always.

### Mistake 6: Modifying Input Without Restoring
- If interviewer expects original grid intact, modifying it is a problem.
- Use a separate `boolean[][] visited` array instead.

### Mistake 7: Counting in DFS Instead of Outer Loop
```java
// WRONG:
dfs(...) {
    ans++;          // counts every cell, not every island
    grid[i][j] = '2';
    for each neighbor: dfs(...)
}
```

- This counts cells, not islands.
- Fix: increment count ONCE per top-level DFS call (in outer loop).

### Mistake 8: Forgetting Null/Empty Check
- `grid` might be null or empty.
- Without check: `grid.length` throws NullPointerException.

### Mistake 9: Not Handling Single Row/Column Grids
- Edge cases of 1×n or n×1 grids.
- Most algorithms handle these naturally, but check.

---

## 17. Related Problem: Max Area of Island (Largest Island)

**LeetCode 695: Max Area of Island.**

> Given a 2D grid of 0s and 1s, return the **area (cell count)** of the largest island. If there are no islands, return 0.

### How It Differs from Number of Islands

| Number of Islands | Max Area of Island |
|-------------------|---------------------|
| Count how many islands | Find the **size** of the largest |
| DFS returns nothing | DFS **returns the count** of cells visited |
| Outer loop counts | Outer loop tracks **max** |

### The Key Modification

Instead of DFS being `void`, it returns an `int` representing how many cells are part of the current island. The outer loop tracks the maximum across all DFS launches.

### Java Solution

```java
class Solution {
    private int[] dx = {1, -1, 0, 0};
    private int[] dy = {0, 0, 1, -1};
    
    /**
     * DFS that returns the number of cells in the connected component
     * starting from (i, j).
     */
    private int dfs(int[][] grid, int i, int j, int r, int c) {
        // Out of bounds or water or already visited
        if (i < 0 || i >= r || j < 0 || j >= c || grid[i][j] != 1) {
            return 0;
        }
        
        grid[i][j] = -1;   // mark visited (any non-1 value works)
        
        int area = 1;       // count THIS cell
        for (int k = 0; k < 4; k++) {
            area += dfs(grid, i + dx[k], j + dy[k], r, c);
        }
        return area;
    }
    
    public int maxAreaOfIsland(int[][] grid) {
        if (grid == null || grid.length == 0) return 0;
        
        int r = grid.length;
        int c = grid[0].length;
        int maxArea = 0;
        
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                if (grid[i][j] == 1) {
                    maxArea = Math.max(maxArea, dfs(grid, i, j, r, c));
                }
            }
        }
        
        return maxArea;
    }
}
```

### Key Insights

#### 1. DFS Returns a Sum
The classic pattern: `1 (current cell) + sum of DFS results from 4 neighbors`.

```java
return 1 + dfs(up) + dfs(down) + dfs(left) + dfs(right);
```

Each recursive call returns the area of that subtree. Adding them all + 1 gives the total area.

#### 2. Base Case Returns 0
When DFS hits an invalid cell (out of bounds or water/visited), it returns 0 — contributing nothing to the area.

This is the same logic as "Number of Islands" — we just **return a value** instead of nothing.

#### 3. Note the Input Type Difference
- Number of Islands uses `char[][]` with `'0'` and `'1'`.
- Max Area of Island uses `int[][]` with `0` and `1`.
- Watch for this in the problem statement!

### Complexity
- **Time**: O(r × c) — each cell visited at most twice (outer scan + DFS).
- **Space**: O(r × c) for DFS recursion stack in worst case.

### Common Mistakes

1. **Returning area but not summing recursively** — only counts the starting cell.
2. **Marking with '0' on int grid** — works, but use `-1` or `2` for clarity (don't confuse with water).
3. **Not checking bounds before grid access** — IndexOutOfBoundsException.

---

## 18. Related Problem: Coloring an Island

**A common variation (similar to LeetCode 1391 / "Color the Borders" / "Flood Fill" LC 733).**

> Given a grid, an island, and a target color, **paint every cell of that island** with the target color.

This is the **flood fill** problem. Used in image editing (paint bucket tool), game grids, and many puzzle algorithms.

### Variant: Flood Fill (LC 733)

> Given an `image` (2D int array), starting position `(sr, sc)`, and `newColor`, flood-fill the connected region (cells with the same color as the starting cell, connected 4-directionally) with `newColor`.

### Java Solution

```java
class Solution {
    private int[] dx = {1, -1, 0, 0};
    private int[] dy = {0, 0, 1, -1};
    
    public int[][] floodFill(int[][] image, int sr, int sc, int newColor) {
        int originalColor = image[sr][sc];
        
        // Edge case: starting cell is already the new color → no change needed
        // (prevents infinite recursion!)
        if (originalColor == newColor) return image;
        
        dfs(image, sr, sc, originalColor, newColor);
        return image;
    }
    
    private void dfs(int[][] image, int i, int j, int oldColor, int newColor) {
        int r = image.length;
        int c = image[0].length;
        
        // Out of bounds or color doesn't match → stop
        if (i < 0 || i >= r || j < 0 || j >= c || image[i][j] != oldColor) {
            return;
        }
        
        image[i][j] = newColor;   // paint this cell
        
        for (int k = 0; k < 4; k++) {
            dfs(image, i + dx[k], j + dy[k], oldColor, newColor);
        }
    }
}
```

### Key Insights

#### 1. The "Same Color Connection" Rule
Unlike Number of Islands (which looks for `'1'` cells), Flood Fill looks for cells matching the **starting cell's color**. The check is `image[i][j] == oldColor`.

#### 2. The Critical Edge Case: `originalColor == newColor`
If the starting cell already has the target color, the DFS would **never make progress** (cells stay matching oldColor, get repainted to same color, recurse forever) → **infinite loop**.

**Always add the early-return check**:
```java
if (originalColor == newColor) return image;
```

This is the #1 bug in flood fill solutions.

#### 3. The Paint Marks the Visited Tracking
Unlike Number of Islands where we artificially mark with `'2'`, here the **new color naturally acts as the "visited" marker**:
- Cells with `newColor` no longer match `oldColor` → DFS skips them.
- No need for separate visited tracking.
- Beautiful self-documenting code.

### Variant: Color Distinct Islands

If you wanted to give each island a unique color (e.g., island 1 → color 2, island 2 → color 3, etc.):

```java
public int[][] colorIslands(int[][] grid) {
    int r = grid.length, c = grid[0].length;
    int color = 2;  // start at 2 to avoid conflict with 0/1
    
    for (int i = 0; i < r; i++) {
        for (int j = 0; j < c; j++) {
            if (grid[i][j] == 1) {
                dfs(grid, i, j, color);
                color++;
            }
        }
    }
    return grid;
}

private void dfs(int[][] grid, int i, int j, int color) {
    if (i < 0 || i >= grid.length || j < 0 || j >= grid[0].length || grid[i][j] != 1) return;
    grid[i][j] = color;
    int[] dx = {1, -1, 0, 0};
    int[] dy = {0, 0, 1, -1};
    for (int k = 0; k < 4; k++) {
        dfs(grid, i + dx[k], j + dy[k], color);
    }
}
```

After this runs:
- Island 1's cells all become `2`.
- Island 2's cells all become `3`.
- Island 3's cells all become `4`.
- etc.

**This coloring trick is the foundation of "Making a Large Island" (Section 20).**

### Complexity
- **Time**: O(r × c) — each cell visited at most once.
- **Space**: O(r × c) recursion stack worst case.

### Why This Matters
The "color the islands" pre-processing technique is the **key building block** for harder problems. Once islands are colored:
- Each island has a unique ID.
- You can look up the size of any island in O(1) (with a separate map).
- Answers questions like "are cells X and Y in the same island?" trivially.

---

## 19. Related Problem: Biggest Island (with Diagonal)

A variant where **diagonal connections also count** — making this 8-directional connectivity.

### Problem Variant

> Given a binary grid, find the size of the largest island where cells are connected horizontally, vertically, **OR diagonally**.

### How It Differs

The ONLY change: **8 directions instead of 4**.

```java
// 4-directional (standard)
int[] dx = {1, -1, 0, 0};
int[] dy = {0, 0, 1, -1};

// 8-directional (with diagonals)
int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
int[] dy = {-1,  0,  1,-1, 1,-1, 0, 1};
```

### Visualizing 8 Directions

```
      (-1,-1) (-1, 0) (-1, 1)
            \    |    /
             \   |   /
   (0,-1) ── (i, j) ── (0, 1)
             /   |   \
            /    |    \
      ( 1,-1) ( 1, 0) ( 1, 1)
```

All 8 surrounding cells are now potential neighbors.

### Java Solution

```java
class Solution {
    // 8 directions: includes 4 cardinals + 4 diagonals
    private int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
    private int[] dy = {-1,  0,  1,-1, 1,-1, 0, 1};
    
    private int dfs(int[][] grid, int i, int j, int r, int c) {
        if (i < 0 || i >= r || j < 0 || j >= c || grid[i][j] != 1) {
            return 0;
        }
        
        grid[i][j] = -1;   // mark visited
        
        int area = 1;
        for (int k = 0; k < 8; k++) {   // loop now goes 0..7
            area += dfs(grid, i + dx[k], j + dy[k], r, c);
        }
        return area;
    }
    
    public int biggestIsland(int[][] grid) {
        if (grid == null || grid.length == 0) return 0;
        
        int r = grid.length, c = grid[0].length;
        int maxArea = 0;
        
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                if (grid[i][j] == 1) {
                    maxArea = Math.max(maxArea, dfs(grid, i, j, r, c));
                }
            }
        }
        return maxArea;
    }
}
```

### Why This Trivial Change Has Big Implications

#### 1. Many More Islands Merge
With 8 directions, cells that were "diagonally separated" now connect:

```
4-directional:   8-directional:
1 0              1 0                 ← these
0 1              0 1                 ← are now ONE island

→ 2 islands      → 1 island
```

The **count drops drastically** when adding diagonals.

#### 2. Largest Island Grows
Long diagonal chains become single islands. In a grid full of `1`s and `0`s in a checkered pattern, 8-directional makes ALL the 1s one island.

#### 3. The Pattern Extends to Knight Moves, etc.
Once you understand direction arrays, you can adapt to ANY adjacency definition:
- Knight in chess: 8 L-shape moves.
- "L-shape" or "kite" patterns.
- Custom adjacencies for puzzles.

### When to Pick 4 vs 8 Directions

**Read the problem carefully**. The phrasing matters:
- "horizontally OR vertically" → 4 directions.
- "horizontally, vertically, OR diagonally" → 8 directions.
- "adjacent" (without qualifier) → usually 4, but verify.
- "touching" → could be either, ask.

When in doubt, **ask the interviewer**.

### Complexity
Same as 4-directional: **O(r × c)** time and space. The constant factor doubles (8 directions instead of 4), but asymptotically identical.

---

## 20. Related Problem: Making a Large Island

**LeetCode 827: Making A Large Island.**

> Given a binary grid, you may change **at most one** `0` to `1`. Return the size of the largest island after the change (or before if no change is better).

This is the **trickiest** of the variants — and the most interesting.

### Why This Is Hard

Naive approach: try changing each `0` to `1`, run "Max Area" DFS for each.
- Each DFS is O(r × c).
- Number of `0` cells: up to r × c.
- Total: **O(r² × c²)**.
- For 500×500 grid: 6.25 × 10^10 operations. **Way too slow.**

We need a smarter approach.

### The Key Insight: Pre-compute Island Sizes

If we knew the size of each island **in advance**, then for each `0`:
- Look at its 4 neighbors.
- If any neighbor is land, find which island it belongs to.
- Sum the sizes of all **distinct** adjacent islands + 1 (for the flipped cell).

This becomes the candidate maximum.

### The Trick: Color Each Island with a Unique ID

This is exactly the "Coloring Islands" technique from Section 18! We:
1. **Pass 1**: DFS through each island, assigning a unique ID (color). Record the size of each island.
2. **Pass 2**: For each `0` cell, sum the sizes of distinct adjacent islands (using their IDs).

### Java Solution

```java
class Solution {
    private int[] dx = {1, -1, 0, 0};
    private int[] dy = {0, 0, 1, -1};
    private int r, c;
    
    public int largestIsland(int[][] grid) {
        if (grid == null || grid.length == 0) return 0;
        
        r = grid.length;
        c = grid[0].length;
        
        // Map from island ID → size of that island
        Map<Integer, Integer> islandSize = new HashMap<>();
        
        // Start IDs at 2 (avoid conflict with 0/1 in grid)
        int islandId = 2;
        int maxIsland = 0;
        
        // PASS 1: Color each island with a unique ID, track sizes
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                if (grid[i][j] == 1) {
                    int size = dfs(grid, i, j, islandId);
                    islandSize.put(islandId, size);
                    maxIsland = Math.max(maxIsland, size);
                    islandId++;
                }
            }
        }
        
        // If the grid is ALL 1s, maxIsland is already the answer
        // (No 0 to flip — return existing largest)
        if (islandSize.isEmpty()) return 1;   // all zeros: flip one to get island of size 1
        
        // PASS 2: For each 0, find the merged island size if we flipped it
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                if (grid[i][j] == 0) {
                    Set<Integer> uniqueIslands = new HashSet<>();
                    
                    // Check 4 neighbors for adjacent islands
                    for (int k = 0; k < 4; k++) {
                        int ni = i + dx[k];
                        int nj = j + dy[k];
                        if (ni >= 0 && ni < r && nj >= 0 && nj < c && grid[ni][nj] > 1) {
                            uniqueIslands.add(grid[ni][nj]);
                        }
                    }
                    
                    // Sum sizes of distinct adjacent islands + 1 (the flipped cell)
                    int merged = 1;
                    for (int id : uniqueIslands) {
                        merged += islandSize.get(id);
                    }
                    
                    maxIsland = Math.max(maxIsland, merged);
                }
            }
        }
        
        return maxIsland;
    }
    
    /**
     * DFS that colors the island with the given ID and returns its size.
     */
    private int dfs(int[][] grid, int i, int j, int id) {
        if (i < 0 || i >= r || j < 0 || j >= c || grid[i][j] != 1) {
            return 0;
        }
        
        grid[i][j] = id;   // color with island ID
        
        int size = 1;
        for (int k = 0; k < 4; k++) {
            size += dfs(grid, i + dx[k], j + dy[k], id);
        }
        return size;
    }
}
```

### Why This Works

#### Pass 1: Color + Size
After this pass:
- All `1`s in the grid are replaced with unique IDs (2, 3, 4, ...).
- We have a map: `islandSize[id] = number of cells`.

```
Before:         After Pass 1:
1 1 0           2 2 0
1 0 1           2 0 3
0 1 1           0 3 3

islandSize: {2: 3, 3: 3}
```

#### Pass 2: Try Each 0
For each `0` cell:
- Look at 4 neighbors.
- Collect the **set** of distinct island IDs touching this cell.
- Sum their sizes + 1 (the flipped cell itself).

#### Why a `Set` for Unique Islands?

Consider:
```
0 0 0
2 . 2     ← single 0 cell with island 2 on BOTH left and right
0 0 0
```

If we flipped the center `0`, we'd be merging with island 2 — but only ONCE, not twice. A Set ensures we don't double-count.

Without the Set:
```
Wrong: 1 + size(2) + size(2) = 1 + 3 + 3 = 7  ← double-counted!
Right: 1 + size(2)           = 1 + 3     = 4
```

This is the **most common bug** in this problem.

### Edge Cases

#### 1. All Zeros
```
0 0 0
0 0 0
```
Flipping one `0` → island of size 1. Return **1**.

#### 2. All Ones
```
1 1
1 1
```
No `0` to flip. Return `maxIsland` from Pass 1 = 4.

#### 3. Single Cell `0` Surrounded by Many Islands
```
1 . 1
. 0 .
1 . 1
```
4 distinct islands of size 1 each. Flipping center: 1 + 1 + 1 + 1 + 1 = **5**.

### Complexity

- **Time**: O(r × c).
  - Pass 1: each cell visited once → O(r × c).
  - Pass 2: each `0` checks 4 neighbors → O(r × c) × 4 = O(r × c).
- **Space**: O(r × c) for `islandSize` map + recursion stack.

**Way better than the naive O((rc)²)**.

### The Pattern: Pre-compute + Lookup

This is a powerful pattern for "what if we modified X?" problems:
1. **Pre-compute** answer for the current state.
2. **Look up** quickly for each modification.

Other examples:
- Range sum queries (prefix sum).
- Range min queries (sparse table).
- "Max subarray after removing one element" (prefix + suffix).

### Common Mistakes

1. **Not using a Set** → double-counting same island.
2. **Treating colored cells (`> 1`) as water** → bug in Pass 2 neighbor check.
3. **Starting IDs at 1** → conflicts with original `1`s; start at 2 or higher.
4. **Forgetting all-zeros / all-ones edge cases**.
5. **Mutating grid then needing it intact** → use a copy if asked.

---

## 21. Comparison Table of All Island Problems

A quick reference for all the variants we've covered:

| Problem | Goal | Key Modification | Time | Space |
|---------|------|------------------|------|-------|
| **Number of Islands** (LC 200) | Count islands | DFS, increment counter | O(rc) | O(rc) |
| **Max Area of Island** (LC 695) | Largest island's size | DFS returns area; track max | O(rc) | O(rc) |
| **Flood Fill** (LC 733) | Paint connected region | DFS with new color | O(rc) | O(rc) |
| **Color Islands** | Assign unique IDs | DFS with incrementing color | O(rc) | O(rc) |
| **Biggest Island (8-dir)** | Largest with diagonals | 8 direction array | O(rc) | O(rc) |
| **Making a Large Island** (LC 827) | Max size after flipping 1 zero | 2 passes: color + neighbor sum | O(rc) | O(rc) |
| **Number of Islands II** (LC 305) | Count islands incrementally | Union-Find | O(k·α) | O(rc) |
| **Surrounded Regions** (LC 130) | Capture regions not on border | DFS from border | O(rc) | O(rc) |
| **Closed Islands** (LC 1254) | Count islands NOT touching border | DFS from border first | O(rc) | O(rc) |
| **Number of Enclaves** (LC 1020) | Cells unreachable from border | DFS from border | O(rc) | O(rc) |
| **Island Perimeter** (LC 463) | Compute perimeter | Count edges (4 per cell - shared) | O(rc) | O(1) |

### Pattern Family Recognition

Notice how almost every problem uses the **same template**:
- Outer loop scans the grid.
- DFS/BFS explores connected components.
- Direction arrays for neighbors.
- Visited tracking (mark in-place or separate array).

The differences are just:
- **What DFS returns** (nothing / area / set of cells).
- **What the outer loop tracks** (count / max / sum).
- **What pre-processing** is needed (none / coloring / multi-source).
- **Connectivity rule** (4-dir / 8-dir).

---

## 22. Other Variations & Follow-ups

### LC 463: Island Perimeter
Compute the perimeter of the one island in the grid.

**Trick**: each land cell contributes 4 to perimeter, minus 2 for each adjacent land cell.

```java
public int islandPerimeter(int[][] grid) {
    int perimeter = 0;
    int r = grid.length, c = grid[0].length;
    
    for (int i = 0; i < r; i++) {
        for (int j = 0; j < c; j++) {
            if (grid[i][j] == 1) {
                perimeter += 4;
                if (i > 0 && grid[i-1][j] == 1) perimeter -= 2;   // shared edge with above
                if (j > 0 && grid[i][j-1] == 1) perimeter -= 2;   // shared edge with left
            }
        }
    }
    return perimeter;
}
```

**O(1) extra space** — no DFS needed.

### LC 130: Surrounded Regions
Flip all `'O'` regions surrounded by `'X'` to `'X'`. But not those touching the border.

**Trick**: DFS from border `'O'`s, mark them as safe ('#'). Remaining `'O'`s are surrounded → flip to 'X'.

### LC 305: Number of Islands II (Online Version)
Land is added incrementally via a list of positions. Report island count after each addition.

Best solved with **Union-Find** — DFS would be O(land_count × grid_size) which is too slow.

### LC 1254: Number of Closed Islands
An island is "closed" if not touching the border.

**Trick**: DFS from border `0`s first (they're not closed). Then count remaining islands normally.

### LC 1020: Number of Enclaves
Count land cells that can't reach the border.

**Trick**: Same as Closed Islands but returns cell count instead of island count.

### LC 994: Rotting Oranges
Multi-source BFS variant. Track time taken for all "fresh" oranges to rot.

**Trick**: BFS with multiple starting points (all rotten oranges initially), tracking levels.

### Pattern: Almost All Grid Problems Use DFS/BFS

Recognize the pattern. Once you've done islands, you can do all of these.

---

## 23. Interview Tips

### How to Approach in Interview

1. **Restate the problem** to confirm understanding.
2. **Mention the abstract pattern**: "This is counting connected components in a grid graph."
3. **Propose DFS or BFS**: "I'll use DFS for clarity. BFS is also valid."
4. **Walk through the algorithm** before coding.
5. **Code it carefully**:
   - Direction arrays.
   - Bounds check.
   - Mark visited before recursing.
6. **Discuss complexity**: O(r × c) time and space.
7. **Mention alternatives**: BFS (avoid stack overflow), Union-Find (for online version).

### Discussion Points to Score Bonus

#### 1. Pattern Recognition
> "This is the classic 'count connected components' problem. We can use DFS, BFS, or Union-Find."

#### 2. Justify the Visited Strategy
> "I'll modify the grid in-place to mark visited cells, saving O(r × c) space. If the interviewer prefers we don't mutate input, I'd use a separate visited array."

#### 3. Direction Arrays Trick
> "I'll use dx/dy arrays — cleaner than 4 if-statements and easier to extend."

#### 4. Bounds Check Order
> "I'll check bounds before accessing the cell to avoid IndexOutOfBoundsException."

#### 5. Edge Cases
> "I'll handle null/empty grid first. Single-cell grids and all-water grids should work naturally."

#### 6. Stack Overflow Awareness
> "For very large grids, DFS recursion could overflow the stack. We'd switch to BFS or iterative DFS in that case."

### Likely Follow-Up Questions

#### Q: Can you do it without modifying the grid?
**A**: Use a `boolean[][] visited` array. Same logic, O(r × c) extra space.

#### Q: What if islands could connect diagonally?
**A**: Add 4 more directions to the direction arrays (8 total). Logic unchanged.

#### Q: What if the grid is very large (10^6 × 10^6)?
**A**: 
- Can't fit in memory at all → need streaming approach.
- Could shard the grid.
- For "very large but fits in memory": BFS instead of DFS (avoid stack overflow).

#### Q: What if you wanted the area of each island?
**A**: DFS returns count of cells visited. Track in outer loop.

#### Q: Can you do this with Union-Find?
**A**: Yes. Showcased in Section 13. Union adjacent land cells; count components.

### Patterns This Problem Demonstrates

1. **Grid traversal via DFS/BFS** — the most common interview pattern.
2. **Connected components**.
3. **Direction arrays** for cleaner neighbor iteration.
4. **In-place visited marking** for O(1) extra space.
5. **Two-tier algorithm**: outer loop scans, inner DFS expands.

---

## TL;DR

### The Mental Model

```
Outer loop: scan every cell.
    If cell == '1' (unvisited land):
        DFS to sink the entire connected island.
        Increment count.
    Else:
        Skip.

DFS:
    Mark current cell visited.
    For each of 4 neighbors:
        If neighbor is land (== '1') and in bounds:
            Recurse.
```

### The Key Insights

1. **Islands = connected components** in a grid graph.
2. **DFS or BFS** explores an island entirely.
3. **Mark visited BEFORE recursing** to avoid infinite loops.
4. **Direction arrays (dx, dy)** clean up neighbor iteration.
5. **Modify grid in-place** for O(1) extra space.
6. **Outer loop + DFS** = two-tier algorithm: scan + expand.

### Why DFS Specifically?
- Natural fit for "explore everything reachable from here".
- Recursion handles arbitrary shapes effortlessly.
- Simpler code than BFS (no explicit queue).

### Time/Space
- **Time**: O(r × c).
- **Space**: O(r × c) worst-case recursion depth.

### Common Pattern This Reinforces

Grid + traversal + "find / count something" → DFS/BFS template:
1. Outer loop scans for starting points.
2. DFS/BFS explores from each.
3. Mark visited to avoid revisits.
4. Aggregate result (count, max, sum).

This template solves dozens of grid problems with minor tweaks.

---

*The big takeaway: grid problems involving connectivity reduce to DFS/BFS. Master this template (outer scan + inner traversal + visited tracking + direction arrays) and you solve an entire category of problems.*
