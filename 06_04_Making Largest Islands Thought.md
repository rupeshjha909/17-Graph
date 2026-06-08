# Flood Fill — Making a Large Island (Competitive Style)

A line-by-line deep dive into the Flood Fill solution for the "Making a Large Island" problem. Explains **every design choice**, why this particular structure was used, and the algorithm patterns at play.

---

## Table of Contents

1. [Problem Statement](#1-problem-statement)
2. [The Big Idea: Color, Then Try Each 0](#2-the-big-idea-color-then-try-each-0)
3. [Why "Flood Fill" Is the Right Name](#3-why-flood-fill-is-the-right-name)
4. [Walking Through the Code Section by Section](#4-walking-through-the-code-section-by-section)
5. [The Global Variables — Why Each One Exists](#5-the-global-variables--why-each-one-exists)
6. [The `floodFill()` Function — Deep Dive](#6-the-floodfill-function--deep-dive)
7. [The Main Function — Three Phases](#7-the-main-function--three-phases)
8. [Why a `Set<Integer>` for Unique Colors](#8-why-a-setinteger-for-unique-colors)
9. [The Subtle Trick: `uniqueColors.add(a[ii][jj])`](#9-the-subtle-trick-uniquecolorsaddaiijj)
10. [Dry Run with Example](#10-dry-run-with-example)
11. [Edge Cases](#11-edge-cases)
12. [Complexity Analysis](#12-complexity-analysis)
13. [Common Mistakes (and How This Code Avoids Them)](#13-common-mistakes-and-how-this-code-avoids-them)
14. [Style Notes: Competitive vs Interview Code](#14-style-notes-competitive-vs-interview-code)
15. [Improvements for Production Use](#15-improvements-for-production-use)
16. [Why This Algorithm Is Powerful](#16-why-this-algorithm-is-powerful)
17. [Interview Tips](#17-interview-tips)

---

## 1. Problem Statement

> Given an `n × m` binary grid (`0` = water, `1` = land). You may change **at most one** `0` to `1`. Return the size of the largest island possible.
>
> Two cells are part of the same island if they're 4-directionally adjacent (NOT diagonal).

### Example

```
Input:
3 3
1 0 1
0 0 0
1 0 1

Output:
3
```

By flipping the center `0` to `1`, we connect the center with up to 4 neighboring islands. In this case, the center has 4 distinct 1-cell islands as neighbors → flipping makes a 5-cell cluster? Actually no — flipping center to 1 gives us a + shape: center connects to (0,0)? No, (0,0) is diagonally adjacent. Let me redo: (0,0), (0,2), (2,0), (2,2) are corners. Center (1,1) is NOT adjacent to any of them (they're 2 steps away).

So flipping (1,1) makes just a single-cell island. Bad example.

### Better Example

```
Input:
2 2
1 0
0 1

Output:
3
```

- Two islands: top-left (size 1) and bottom-right (size 1).
- Flip either (0,1) or (1,0) → connects both islands + the flipped cell.
- New size: 1 + 1 + 1 = **3**.

### Another Example

```
Input:
2 2
1 1
1 1

Output:
4
```

Already one island of 4. No need to flip. Answer: 4.

---

## 2. The Big Idea: Color, Then Try Each 0

### The Two-Pass Strategy

This algorithm uses a **2-pass** approach:

#### Pass 1: Identify and Size Every Island
- Walk through the grid.
- When you find an unvisited `1` cell, **flood fill** the entire island.
- Assign each island a **unique color/ID** (1, 2, 3, ...).
- Record the **size** of each colored island.

#### Pass 2: Try Flipping Each 0
- For each `0` cell, look at its 4 neighbors.
- Collect the **distinct island colors** touching this cell.
- If we flipped this cell to 1: new island size = sum of distinct neighbor island sizes + 1.
- Track the maximum across all `0` cells.

#### Answer
The maximum of:
- The largest existing island (Pass 1's largest).
- The best possible merger when flipping a `0` (Pass 2's max).

### Why This Is Smart

#### Naive Approach (Too Slow)
For each `0` cell, flip it and run BFS/DFS to compute the new island size. Reset, try next `0`.

- Time: O(number of 0s × grid size) = O((nm)²).
- For 500×500 grid: ~6 × 10^10 operations. **Way too slow**.

#### Smart Approach (This Code)
- Pre-compute island sizes ONCE in Pass 1.
- For each `0`, look at 4 neighbors and look up their island sizes — O(1) each.
- Total: O(nm).

**Reduction**: from O((nm)²) to O(nm) — a massive speedup.

---

## 3. Why "Flood Fill" Is the Right Name

The technique here is **flood fill** — a classic algorithm in:
- Image editing (paint bucket tool: click on a pixel, all connected same-color pixels flood with new color).
- Game development (region marking, territory calculation).
- Grid problems (connected component labeling).

### The Flood Fill Operation

> Starting from a cell, recursively visit all 4-connected cells matching a condition, and **change them** in some way (color, mark visited, etc.).

In this code, "flood filling" an island means visiting all cells of one island and **changing their value from `1` to a unique color (1, 2, 3, ...)**.

### Why Color Each Island Uniquely?

After flood fill:
- Island 1's cells are all `1` (a coincidence — the FIRST island happens to get color 1).
- Island 2's cells are all `2`.
- Island 3's cells are all `3`.
- And so on.

Now we can:
- Tell which island any cell belongs to (just look at its value).
- Look up the size of any island (via `colCnt[color]`).
- Check if two cells are in the same island (compare their values).

This **coloring is the key transformation** that enables Pass 2's fast lookups.

---

## 4. Walking Through the Code Section by Section

Let's go through the code top to bottom, explaining each section.

### Section A: Class-Level Globals

```java
private static final int N = 100;
private static int[][] a = new int[N][N];
private static int[][] vis = new int[N][N];
private static int[] colCnt = new int[N];
private static int n, m;
```

This is **competitive-programming-style code**. Globals avoid:
- Passing parameters everywhere.
- Allocation overhead inside recursive functions.

For interviews/production, you'd encapsulate these inside the class non-statically.

### Section B: Direction Arrays

```java
private static int[] dx = {0, 0, 1, -1};
private static int[] dy = {1, -1, 0, 0};
```

The 4 cardinal directions:
- (0, 1) → right
- (0, -1) → left
- (1, 0) → down
- (-1, 0) → up

Used in the for-loop `for (int k = 0; k < 4; k++)` to iterate over neighbors.

### Section C: The Flood Fill Function

```java
private static void floodFill(int x, int y, int col) {
    a[x][y] = col;
    colCnt[col]++;
    vis[x][y] = 1;
    
    for (int i = 0; i < 4; i++) {
        int xx = x + dx[i];
        int yy = y + dy[i];
        
        if (xx >= 0 && xx < n && yy >= 0 && yy < m && 
            vis[xx][yy] == 0 && a[xx][yy] == 1) {
            floodFill(xx, yy, col);
        }
    }
}
```

The recursive heart of the algorithm. Details in Section 6.

### Section D: Main — Read Input
```java
n = scanner.nextInt();
m = scanner.nextInt();
for (int i = 0; i < n; i++) {
    for (int j = 0; j < m; j++) {
        a[i][j] = scanner.nextInt();
    }
}
```

Standard competitive programming input style. Reads dimensions then the grid.

### Section E: Main — Pass 1 (Flood Fill All Islands)

```java
int totalCount = 0;
for (int i = 0; i < n; i++) {
    for (int j = 0; j < m; j++) {
        if (a[i][j] == 1 && vis[i][j] == 0) {
            totalCount++;
            floodFill(i, j, totalCount);
        }
    }
}
```

Scan the grid. For each unvisited `1`:
- Increment `totalCount` (this is the new island's ID).
- Flood-fill the island, assigning this ID.

After this pass:
- `a[i][j]` contains the island ID (or 0 for water).
- `colCnt[id]` = size of island with that ID.
- `totalCount` = total number of islands.

### Section F: Main — Find Largest Existing Island

```java
int largestIsland = 0;
for (int i = 1; i <= totalCount; i++) {
    largestIsland = Math.max(largestIsland, colCnt[i]);
}
```

Find the biggest island BEFORE any flipping. This is the answer if flipping no `0` improves things.

### Section G: Main — Pass 2 (Try Each 0)

```java
for (int i = 0; i < n; i++) {
    for (int j = 0; j < m; j++) {
        if (a[i][j] == 0) {
            Set<Integer> uniqueColors = new HashSet<>();
            
            for (int k = 0; k < 4; k++) {
                int ii = i + dx[k];
                int jj = j + dy[k];
                
                if (ii >= 0 && ii < n && jj >= 0 && jj < m) {
                    uniqueColors.add(a[ii][jj]);
                }
            }
            
            int ans = 1;
            for (int color : uniqueColors) {
                ans += colCnt[color];
            }
            
            largestIsland = Math.max(largestIsland, ans);
        }
    }
}

System.out.println(largestIsland);
```

For each `0` cell:
1. Find unique neighboring island colors.
2. Compute what the new island would be: 1 (the flipped cell) + sum of distinct neighbor island sizes.
3. Update max.

---

## 5. The Global Variables — Why Each One Exists

### `int[][] a`
The grid itself. Mutated to hold island colors after Pass 1.

Why mutate the original grid? **Space efficiency**. No need for a separate `color[][]` array.

After Pass 1:
- `a[i][j] = 0` → water (unchanged).
- `a[i][j] = k` → part of island k (where k is 1, 2, 3, ...).

### `int[][] vis`
Tracks which cells have been processed by flood fill.

#### Wait — Why Need This?
After flood fill, `a[x][y]` becomes the color (not `1` anymore). The condition `a[xx][yy] == 1` in flood fill already prevents revisiting (since colored cells aren't `1` anymore).

So technically `vis[][]` is **redundant** in this specific algorithm! Both checks (`vis[xx][yy] == 0` and `a[xx][yy] == 1`) ensure non-revisitation.

Why keep both? **Defensive coding**. If you later modify the algorithm (e.g., colors could be 1), `vis[][]` is a safety net. In competitive programming, both checks add robustness.

You could remove `vis[][]` and just rely on `a[x][y] == 1` — the algorithm still works.

### `int[] colCnt`
`colCnt[k]` = number of cells in island `k`.

Filled in during flood fill: every time we visit a cell, `colCnt[col]++`.

After Pass 1:
- `colCnt[1]` = size of island 1.
- `colCnt[2]` = size of island 2.
- ...
- `colCnt[0]` = unused (water has no count).

This is the **lookup table** that makes Pass 2 fast.

### `int n, m`
Grid dimensions. Pulled out as globals so the recursive function doesn't need them as parameters (cleaner signature, less stack overhead).

### Why `N = 100`?
Hardcoded upper bound. Common in competitive programming where problem constraints are known.

For an interview, you'd use dynamic sizing: `new int[n][m]` after reading dimensions.

---

## 6. The `floodFill()` Function — Deep Dive

```java
private static void floodFill(int x, int y, int col) {
    a[x][y] = col;
    colCnt[col]++;
    vis[x][y] = 1;
    
    for (int i = 0; i < 4; i++) {
        int xx = x + dx[i];
        int yy = y + dy[i];
        
        if (xx >= 0 && xx < n && yy >= 0 && yy < m && 
            vis[xx][yy] == 0 && a[xx][yy] == 1) {
            floodFill(xx, yy, col);
        }
    }
}
```

### Line-by-Line

#### Line 1: `a[x][y] = col;`
**Color this cell** with the island's ID.

Critical: this also serves as the visited marker. Once a cell is colored (any value other than 0 or 1), it won't be revisited.

#### Line 2: `colCnt[col]++;`
**Increment the size counter** for this island.

By the time flood fill finishes, this counter holds the total cells in the island.

#### Line 3: `vis[x][y] = 1;`
**Mark visited** explicitly.

As discussed, this is redundant given line 1, but harmless.

#### Lines 5-12: Recurse on 4 Neighbors
```java
for (int i = 0; i < 4; i++) {
    int xx = x + dx[i];
    int yy = y + dy[i];
    
    if (xx >= 0 && xx < n && yy >= 0 && yy < m &&    // bounds check
        vis[xx][yy] == 0 &&                            // not visited
        a[xx][yy] == 1) {                              // is land
        floodFill(xx, yy, col);
    }
}
```

The condition combines three checks (all must be true):
1. **In bounds** (`xx ≥ 0 && xx < n && yy ≥ 0 && yy < m`).
2. **Not visited** (`vis[xx][yy] == 0`).
3. **Is land** (`a[xx][yy] == 1`).

If all three: recurse with the same color `col`.

### Why `col` (Not `i`) as the Color?
The color is passed from the caller (main function). All cells in this DFS call chain get the same color. This is how cells of one island all get the same ID.

### What If We Used DFS Without Returning?
This function returns `void`. It doesn't return the count — it accumulates in `colCnt[]`.

An alternative: have `floodFill()` return an int (the size), like the Max Area of Island problem. But maintaining a global counter is simpler in competitive style.

### What If The Grid Has a Million Cells?
The DFS recursion depth = the size of the largest island.

For a 1000×1000 all-1s grid: depth ≈ 1,000,000 → **stack overflow** in Java (default stack size ~512KB).

Mitigation:
- Use iterative DFS with explicit stack.
- Use BFS with queue.
- Increase JVM stack size: `java -Xss10m`.

For typical competitive constraints (N ≤ 100 or 500), this works fine.

---

## 7. The Main Function — Three Phases

### Phase 1: Color All Islands

```java
int totalCount = 0;
for (int i = 0; i < n; i++) {
    for (int j = 0; j < m; j++) {
        if (a[i][j] == 1 && vis[i][j] == 0) {
            totalCount++;
            floodFill(i, j, totalCount);
        }
    }
}
```

**Key insight**: `totalCount` is used as **both** the island count AND the color/ID.

- First island found gets color 1.
- Second island found gets color 2.
- etc.

After this loop:
- `totalCount` = total number of islands.
- `a[i][j]` has color values (1, 2, 3, ...) or 0 (water).
- `colCnt[k]` has size of island k.

### Phase 2: Find Largest Existing Island

```java
int largestIsland = 0;
for (int i = 1; i <= totalCount; i++) {
    largestIsland = Math.max(largestIsland, colCnt[i]);
}
```

Simple loop over all island sizes, tracking max.

**Why start at `i = 1`?** Because color 0 is water; we never assigned color 0 to any island.

### Phase 3: Try Flipping Each 0

```java
for (int i = 0; i < n; i++) {
    for (int j = 0; j < m; j++) {
        if (a[i][j] == 0) {
            Set<Integer> uniqueColors = new HashSet<>();
            
            for (int k = 0; k < 4; k++) {
                int ii = i + dx[k];
                int jj = j + dy[k];
                
                if (ii >= 0 && ii < n && jj >= 0 && jj < m) {
                    uniqueColors.add(a[ii][jj]);
                }
            }
            
            int ans = 1;
            for (int color : uniqueColors) {
                ans += colCnt[color];
            }
            
            largestIsland = Math.max(largestIsland, ans);
        }
    }
}
```

For each water cell `(i, j)`:

1. **Collect unique neighbor colors** in a set.
2. **Sum sizes**: 1 (flipped cell) + colCnt of each unique color.
3. **Update max**.

### Why "Try Each 0"?

Because the problem says we can change "at most one" 0 to 1. The optimal answer comes from:
- The best `0` to flip (the one with the largest merged island).
- OR: not flipping anything (if the existing largest is already the best).

So we try every `0` and pick the best.

### Why Only Consider `0` Cells?

Flipping a `1` to `1` does nothing. Flipping a `1` to `0` would shrink. The problem allows flipping a `0` to `1` — that's our only useful move.

---

## 8. Why a `Set<Integer>` for Unique Colors

This is the **most important subtlety** in the algorithm. Get this wrong and you double-count.

### The Problem Without a Set

Consider this grid (after Pass 1's coloring):

```
1 0 1     ← cells (0,0) and (0,2) are island 1 and 2
0 0 0     ← bottom row is water
0 0 0
```

Wait, that's two separate islands. Let me use a clearer case.

```
1 1     → island 1
1 0     ← the 0 has cells of island 1 on TOP and LEFT
```

The `0` at (1,1) has:
- Up (0,1) = color 1.
- Left (1,0) = color 1.
- Down (2,1) = out of bounds.
- Right (1,2) = out of bounds.

**Without a set**:
- Naive sum: 1 (flipped) + colCnt[1] (from up) + colCnt[1] (from left) = 1 + 3 + 3 = **7**.
- WRONG! There are only 3 land cells + 1 flipped = 4. We **double-counted** island 1.

**With a set**:
- `uniqueColors = {1}` (we add color 1 twice, but the set deduplicates).
- Sum: 1 + colCnt[1] = 1 + 3 = **4**. ✓

### When Does This Happen?

A `0` cell can have the **same island on multiple sides** when an island wraps around (think L-shape, C-shape, U-shape).

```
0 1 1
0 1 0
1 1 1     ← The center 0 has the same island on left, right, top, bottom!
```

The center `0` has island 1 (the surrounding U) on multiple sides. Without a set, we'd count island 1's size **4 times**.

### Why HashSet Specifically?

- Add: O(1) average.
- Lookup (implicit during add): O(1) average.
- Iteration: O(unique count).

For up to 4 neighbors, a `Set<Integer>` is overkill but clean. Alternatives:
- Manual deduplication (compare pairs). Faster constants but uglier code.
- `int[4]` array, then deduplicate. Same idea.

For competitive programming, HashSet is fine.

### What About Adding Color 0 (Water)?

Look at the code:
```java
if (ii >= 0 && ii < n && jj >= 0 && jj < m) {
    uniqueColors.add(a[ii][jj]);
}
```

This adds `a[ii][jj]` regardless of whether it's water (0) or land (a color).

If a neighbor is water, `0` gets added to the set. Later:
```java
for (int color : uniqueColors) {
    ans += colCnt[color];
}
```

We add `colCnt[0]` to the sum. But `colCnt[0]` was never incremented (only colors 1, 2, 3, ... were used). So `colCnt[0] = 0` and adding it is a no-op.

**This is a clever way to avoid an extra `if` check** — adding water's "color" 0 doesn't hurt because its count is 0.

---

## 9. The Subtle Trick: `uniqueColors.add(a[ii][jj])`

Building on the previous section — let's appreciate this small but elegant choice.

### The "Obvious" Way (More Code)

```java
if (ii >= 0 && ii < n && jj >= 0 && jj < m) {
    if (a[ii][jj] != 0) {           // only add LAND colors
        uniqueColors.add(a[ii][jj]);
    }
}
```

Explicitly skip water. More obvious, but more code.

### This Code's Way (Implicit)

```java
if (ii >= 0 && ii < n && jj >= 0 && jj < m) {
    uniqueColors.add(a[ii][jj]);    // add whatever (even 0)
}
```

Just add. Water's `0` is added but contributes 0 to the sum.

### Why It Works

`colCnt[0]` is **never incremented** in flood fill (only `colCnt[col]` for `col ≥ 1`).

So `colCnt[0] = 0` always. Summing it: no effect.

### Style Tradeoff

- **Pro**: Less code, more elegant.
- **Con**: Slightly harder to read at first (why does it add water's 0?).

For an interview, you might prefer the explicit check for clarity. For competitive code, this trick saves a few lines.

---

## 10. Dry Run with Example

Let's trace through:

```
Input:
3 3
1 1 0
1 0 1
0 1 1
```

### Initial State

```
a[][]:
1 1 0
1 0 1
0 1 1

vis[][] = all 0s
colCnt[] = all 0s
```

### Pass 1: Flood Fill Islands

**Scan (0, 0): a[0][0] = 1, vis[0][0] = 0** → new island!
- `totalCount = 1`. Call `floodFill(0, 0, 1)`.
- Color (0,0) with 1, count = 1. Check neighbors:
  - (0, 1) = 1, unvisited → recurse `floodFill(0, 1, 1)`.
    - Color (0,1) with 1, count = 2. Check neighbors:
      - (0, 2) = 0 → skip.
      - (-1, 1) → out of bounds.
      - (1, 1) = 0 → skip.
      - (0, 0) = already colored (visited) → skip.
  - (-1, 0) → out of bounds.
  - (1, 0) = 1, unvisited → recurse `floodFill(1, 0, 1)`.
    - Color (1,0) with 1, count = 3. Check neighbors:
      - (1, 1) = 0 → skip.
      - (1, -1) → out of bounds.
      - (2, 0) = 0 → skip.
      - (0, 0) = visited → skip.

After flood fill of island 1:
```
a[][]:
1 1 0
1 0 1     ← (1, 2) is still 1, untouched
0 1 1

colCnt[1] = 3
```

**Continue scanning. Skip cells with `a > 1` or `a = 0`.**

**Reach (1, 2): a = 1, vis = 0** → new island!
- `totalCount = 2`. Call `floodFill(1, 2, 2)`.
- Color (1,2) with 2, count = 1. Check neighbors:
  - (1, 3) → out of bounds.
  - (1, 1) = 0 → skip.
  - (2, 2) = 1, unvisited → recurse `floodFill(2, 2, 2)`.
    - Color (2,2) with 2, count = 2. Check neighbors:
      - (2, 3) → out of bounds.
      - (2, 1) = 1, unvisited → recurse `floodFill(2, 1, 2)`.
        - Color (2,1) with 2, count = 3. Check neighbors:
          - (2, 2) = visited.
          - (2, 0) = 0 → skip.
          - (3, 1) → out of bounds.
          - (1, 1) = 0 → skip.
      - (3, 2) → out of bounds.
      - (1, 2) = visited.
  - (0, 2) = 0 → skip.

After flood fill of island 2:
```
a[][]:
1 1 0
1 0 2
0 2 2

colCnt[1] = 3, colCnt[2] = 3
```

### Phase 2: Largest Existing Island
```
largestIsland = max(colCnt[1], colCnt[2]) = max(3, 3) = 3
```

### Phase 3: Try Each 0

**Cell (0, 2):**
- Neighbors:
  - (-1, 2) → out of bounds.
  - (1, 2) = 2.
  - (0, 1) = 1.
  - (0, 3) → out of bounds.
- `uniqueColors = {2, 1}`.
- ans = 1 + colCnt[2] + colCnt[1] = 1 + 3 + 3 = **7**.

**Cell (1, 1):**
- Neighbors:
  - (0, 1) = 1.
  - (2, 1) = 2.
  - (1, 0) = 1.
  - (1, 2) = 2.
- `uniqueColors = {1, 2}` (deduplicated!).
- ans = 1 + colCnt[1] + colCnt[2] = 1 + 3 + 3 = **7**.

**Cell (2, 0):**
- Neighbors:
  - (1, 0) = 1.
  - (3, 0) → out of bounds.
  - (2, 1) = 2.
  - (2, -1) → out of bounds.
- `uniqueColors = {1, 2}`.
- ans = 1 + 3 + 3 = **7**.

### Final Answer
`largestIsland = max(3, 7, 7, 7) = 7`.

### Sanity Check
After flipping (1, 1) to 1:
```
1 1 0
1 1 1
0 1 1
```

Counting connected 1s: (0,0), (0,1), (1,0), (1,1), (1,2), (2,1), (2,2) = **7 cells**. ✓

---

## 11. Edge Cases

### 1. All Zeros
```
0 0
0 0
```
- No islands → `totalCount = 0`, `largestIsland = 0` after Phase 2.
- Phase 3: each 0 has only 0-colored neighbors. `uniqueColors = {0}`. ans = 1 + colCnt[0] = 1 + 0 = 1.
- Final: **1** (flipping any 0 gives a 1-cell island).

### 2. All Ones
```
1 1
1 1
```
- One island of size 4.
- No 0s to flip.
- Phase 3 loop doesn't execute.
- Final: **4**.

### 3. Single Cell
```
Grid = [[0]]  → answer: 1 (flip)
Grid = [[1]]  → answer: 1 (no flip possible)
```

### 4. Single Row
```
Grid = [[1, 0, 1, 1, 0, 1]]
```
- Islands: {(0)}, {(2), (3)}, {(5)}. Sizes: 1, 2, 1.
- Largest existing: 2.
- Try flipping each 0:
  - (0,1): neighbors (0,0) and (0,2) → distinct colors. ans = 1 + 1 + 2 = 4.
  - (0,4): neighbors (0,3) and (0,5) → distinct colors. ans = 1 + 2 + 1 = 4.
- Final: **4**.

### 5. Diagonal Connections
The problem uses 4-directional. Cells diagonally adjacent are **separate islands**.

```
1 0
0 1
```
- Two islands of size 1 each.
- Flipping any 0:
  - (0,1): neighbors (0,0) and (1,1). ans = 1 + 1 + 1 = 3.
  - (1,0): same. ans = 3.
- Final: **3**.

### 6. U-Shaped Island
```
1 0 1
1 1 1
```
- One C-shaped island of size 5.
- The single 0 at (0,1) has THREE neighbors that are part of the same island.
  - With `Set`: uniqueColors = {1}. ans = 1 + 5 = 6.
  - Without `Set`: would be 1 + 5 + 5 + 5 = 16 (wrong by 3x).
- Final: **6**.

This is exactly the case the `Set` protects against.

### 7. Two Islands Reachable via Multiple Sides
```
1 0 1
0 0 0
1 0 1
```
- Four islands of size 1 each (corners).
- Flipping center (1,1): neighbors are 4 different islands? Wait, (1,1)'s neighbors are (0,1), (1,0), (1,2), (2,1) — all 0s. So center has NO land neighbors.
- Different cell: (0,1) has neighbors (0,0), (0,2), (1,1), (-1,1). The land ones are (0,0) and (0,2).
- ans = 1 + 1 + 1 = 3.

### 8. Large Grid Stress Test
For N = 100, the grid has 10,000 cells. Algorithm runs in ~40,000 operations (cell × 4 neighbors). Fast.

For N = 1000, ~4 million operations. Still fast.

For N = 10000, hardcoded `N = 100` would fail. Allocate dynamically in production code.

---

## 12. Complexity Analysis

### Time Complexity: O(n × m)

#### Phase 1: Flood Fill
- Each cell visited at most ONCE by floodFill (thanks to visited check).
- Each visit does O(4) = O(1) work.
- Total: O(n × m).

#### Phase 2: Largest Island
- Loop over `totalCount` islands. Bounded by O(n × m) but typically much smaller.

#### Phase 3: Try Each 0
- Loop over all cells, O(n × m).
- For each 0 cell: check 4 neighbors, build set of size ≤ 4, iterate set.
- Per cell: O(1).
- Total: O(n × m).

**Grand total**: O(n × m).

For 500×500 grid: 250,000 cells. Milliseconds.

### Space Complexity: O(n × m)

- `a[][]`: O(n × m).
- `vis[][]`: O(n × m).
- `colCnt[]`: O(N) (hardcoded 100, but in principle O(n × m / 1) since max islands ≤ n × m).
- Recursion stack: up to O(n × m) for snake-like islands.

**Total**: O(n × m).

### Comparing to Naive Approach

| | Naive | This Algorithm |
|-|-------|----------------|
| Time | O((n × m)²) | O(n × m) |
| Space | O(n × m) | O(n × m) |
| For 500×500 | 62 billion ops | 250K ops |
| For 1000×1000 | 10^12 ops (impossible) | 1M ops (fast) |

**Speedup**: ~100,000x for typical grids.

---

## 13. Common Mistakes (and How This Code Avoids Them)

### Mistake 1: Double-Counting Same Island
**The trap**: A `0` cell can have the same island on multiple sides. Without a set, you'd count that island multiple times.

**How this code avoids it**: `Set<Integer> uniqueColors` deduplicates.

### Mistake 2: Using Color 0 or 1 (Conflict)
**The trap**: If you start coloring from 0 or 1, you conflict with water (0) or original land (1). The check `a[xx][yy] == 1` would treat colored cells as uncolored.

**How this code avoids it**: `totalCount` starts at 0, becomes 1 for first island. Colors used: 1, 2, 3, ... These don't conflict because:
- Water is 0.
- Once a cell is colored (≥1), the `a[xx][yy] == 1` check FAILS for already-processed cells (because their value is e.g. 1, 2, 3, ... not the literal `1` check... wait).

Hmm, actually look closely. The first island gets color 1, and the check is `a[xx][yy] == 1`. So if I'm processing cells of island 1, neighbors that are ALSO color 1 (already processed in this island) would still pass the `a[xx][yy] == 1` check!

This would re-recurse forever. UNLESS the `vis[][]` check stops it.

**This is exactly why `vis[][]` exists despite seeming redundant!** Without it, the first island's flood fill would infinitely recurse since:
- Color 1 cells satisfy `a[xx][yy] == 1`.
- We'd revisit them forever.

So `vis[][]` IS necessary for the first island. For subsequent islands (color 2+), the `a[xx][yy] == 1` check is what stops recursion (their original `1` value was overwritten).

### Mistake 3: Not Counting Cells in Flood Fill
**The trap**: Forgetting to increment `colCnt[col]` during flood fill.

**How this code avoids it**: `colCnt[col]++` on line 2 of `floodFill()`.

### Mistake 4: Stack Overflow on Large Inputs
**The trap**: Java's default stack is small; recursive DFS on large grids can overflow.

**How this code handles it**: For competitive constraints (N ≤ 100), no problem. For larger grids, use iterative DFS or BFS.

### Mistake 5: Off-by-One in Loop Bounds
**The trap**: Using `i <= totalCount` (correct since islands are 1-indexed) vs `i < totalCount`.

**How this code handles it**: `for (int i = 1; i <= totalCount; i++)` is correct.

### Mistake 6: Forgetting Edge Cases
**The trap**: All zeros (no islands), all ones (no 0s to flip).

**How this code handles it**: 
- All zeros: `largestIsland` stays 0 after Phase 2. Phase 3 finds at least 1 (flipping any 0). Final: 1.
- All ones: Phase 3 loop doesn't execute. Final: `largestIsland` from Phase 2.

Both work correctly.

### Mistake 7: Boundary Checks Out of Order
**The trap**: Checking `vis[xx][yy]` BEFORE bounds check → ArrayIndexOutOfBoundsException.

**How this code avoids it**: Bounds checked FIRST in the `if`.

```java
if (xx >= 0 && xx < n && yy >= 0 && yy < m &&     // bounds FIRST
    vis[xx][yy] == 0 && a[xx][yy] == 1) {          // then array access
```

Java's short-circuit `&&` ensures the array access only happens if bounds pass.

---

## 14. Style Notes: Competitive vs Interview Code

This code is written in **competitive programming style**. Let's contrast with **interview/production style**.

### Competitive Style (This Code)
- Static globals.
- Hardcoded array sizes (`N = 100`).
- Single-letter or short variable names (`a`, `n`, `m`, `xx`, `yy`).
- All logic in `main`.
- Goal: write fast, run fast.

### Interview Style
- Class-based.
- Dynamic sizing.
- Descriptive names (`grid`, `visited`, `islandSize`).
- Helper methods.
- Goal: communicate clearly.

### Interview Refactor

```java
class Solution {
    private int n, m;
    private int[] dx = {0, 0, 1, -1};
    private int[] dy = {1, -1, 0, 0};
    
    public int largestIsland(int[][] grid) {
        n = grid.length;
        m = grid[0].length;
        
        Map<Integer, Integer> islandSize = new HashMap<>();
        int color = 2;  // start at 2 to avoid conflict with 0/1
        
        // Pass 1: color islands
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (grid[i][j] == 1) {
                    int size = dfs(grid, i, j, color);
                    islandSize.put(color, size);
                    color++;
                }
            }
        }
        
        // If no islands or all water
        if (islandSize.isEmpty()) return 1;
        
        // Initial max = largest existing island
        int maxArea = Collections.max(islandSize.values());
        
        // Pass 2: try each 0
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (grid[i][j] == 0) {
                    Set<Integer> neighbors = new HashSet<>();
                    for (int k = 0; k < 4; k++) {
                        int ni = i + dx[k];
                        int nj = j + dy[k];
                        if (ni >= 0 && ni < n && nj >= 0 && nj < m && grid[ni][nj] > 1) {
                            neighbors.add(grid[ni][nj]);
                        }
                    }
                    int area = 1;
                    for (int c : neighbors) area += islandSize.get(c);
                    maxArea = Math.max(maxArea, area);
                }
            }
        }
        
        return maxArea;
    }
    
    private int dfs(int[][] grid, int i, int j, int color) {
        if (i < 0 || i >= n || j < 0 || j >= m || grid[i][j] != 1) return 0;
        grid[i][j] = color;
        int size = 1;
        for (int k = 0; k < 4; k++) {
            size += dfs(grid, i + dx[k], j + dy[k], color);
        }
        return size;
    }
}
```

Key differences:
- Returns size from DFS (no global counter).
- Uses `HashMap` for size lookup (cleaner than array).
- Starts color from 2 (explicit avoidance of 0/1 conflict).
- Explicit `> 1` check (skip water/un-colored).
- No `vis[][]` (DFS uses color overwrite as visited marker).

---

## 15. Improvements for Production Use

If this code were going into production, here's what I'd change:

### 1. Encapsulate State
Move from globals to a class with instance variables.

### 2. Use BFS for Large Inputs
Avoid stack overflow:
```java
private int bfsFloodFill(int[][] grid, int startR, int startC, int color) {
    Queue<int[]> queue = new LinkedList<>();
    queue.offer(new int[]{startR, startC});
    grid[startR][startC] = color;
    int size = 0;
    
    while (!queue.isEmpty()) {
        int[] cell = queue.poll();
        size++;
        for (int k = 0; k < 4; k++) {
            int ni = cell[0] + dx[k];
            int nj = cell[1] + dy[k];
            if (ni >= 0 && ni < n && nj >= 0 && nj < m && grid[ni][nj] == 1) {
                grid[ni][nj] = color;
                queue.offer(new int[]{ni, nj});
            }
        }
    }
    return size;
}
```

### 3. Don't Mutate Input
If callers need the original grid:
```java
int[][] coloredGrid = new int[n][m];
for (int i = 0; i < n; i++) coloredGrid[i] = grid[i].clone();
// then mutate coloredGrid
```

### 4. Validate Input
```java
if (grid == null || grid.length == 0 || grid[0].length == 0) return 0;
```

### 5. Use Streams for Readability (Optional)
```java
int maxArea = islandSize.values().stream().mapToInt(Integer::intValue).max().orElse(0);
```

### 6. Add Unit Tests
Various corner cases (single cell, all zeros, U-shape, etc.).

---

## 16. Why This Algorithm Is Powerful

### The General Pattern: "Pre-compute + Look Up"

This algorithm exemplifies a powerful general pattern:
1. **Pre-compute** answers to a related, simpler problem.
2. **Use the precomputed data** to answer the actual problem in O(1) per query.

Examples elsewhere:
- **Prefix sums**: O(n) preprocessing → O(1) range sum queries.
- **Sparse tables**: O(n log n) preprocessing → O(1) range min queries.
- **DP tables**: O(state space) → O(1) per state lookup.
- **Hash map caching**: O(n) preprocessing → O(1) per lookup.

When you see "for each X, do something expensive", ask: **can I pre-compute it?**

### The Coloring/ID Trick

Assigning unique IDs to connected components is a tool that solves MANY problems:
- **This problem**: lookup which island a cell belongs to.
- **Number of distinct islands**: hash the shape, count unique hashes.
- **Connected components count after edge deletions**: process in reverse.
- **Bipartite graph detection**: 2-coloring.
- **Map regions to features in image processing**.

Master this trick. It's a category, not a one-off.

### Why DFS for This

DFS suits this because:
- We need to visit EVERY cell of a component.
- Order doesn't matter (no shortest-path requirement).
- Stack depth is bounded by island size, which is bounded by grid size.

For shortest-path problems on grids, BFS would be the choice instead.

---

## 17. Interview Tips

### How to Approach in Interview

1. **Restate the problem**: "I need to find the max island size after possibly flipping one 0 to 1."
2. **Naive observation**: "Try every 0 and run DFS — but that's O((nm)²)."
3. **Better idea**: "Pre-compute island sizes once. Then for each 0, sum the sizes of distinct neighbor islands."
4. **State the algorithm**:
   - Pass 1: color each island, record size.
   - Pass 2: for each 0, sum unique neighbor island sizes + 1.
5. **Walk through the special case**: same island on multiple sides → need Set to deduplicate.
6. **Code it**.
7. **Discuss complexity**: O(nm).
8. **Test edge cases**: all 0s, all 1s, U-shape.

### Discussion Points That Score Bonus

#### 1. Why Pre-compute
> "The naive approach of running DFS for each 0 is O((nm)²). By pre-computing island sizes, we reduce to O(nm) — a massive speedup."

#### 2. The Coloring Trick
> "I'll assign each island a unique ID using flood fill. This lets me look up sizes in O(1) and identify which island a cell belongs to."

#### 3. The Set Deduplication
> "A 0 cell can have the same island on multiple sides (U-shapes). I'll use a Set to avoid double-counting."

#### 4. Why Start Colors at 2 (or counter from 1)
> "I want to avoid conflict with 0 (water) and 1 (original land). Starting colors at 2 keeps the distinction clear."

#### 5. The Edge Case
> "If the grid is all 1s, no 0 to flip — return the existing largest. If all 0s, flipping any cell gives a 1-cell island."

### Likely Follow-Up Questions

#### Q: What if you could flip K zeros, not just 1?
**A**: Much harder. Becomes an NP-hard problem in general. For small K, exhaustive search; for larger K, approximation or specialized algorithms.

#### Q: What if connections were 8-directional?
**A**: Just change the direction arrays. Logic unchanged.

#### Q: Can you do this without mutating the input?
**A**: Yes, use a separate `int[][] color` array of the same size. Slightly more memory.

#### Q: What if the grid is enormous (10^6 cells)?
**A**: Use BFS instead of DFS (avoid stack overflow). Memory still O(nm) which might be the limiting factor.

#### Q: Could you use Union-Find here?
**A**: Yes. Union all adjacent 1s in Pass 1. Then for each 0, find the unique parents of neighbor 1s, sum their sizes. Slightly more complex but valid.

---

## TL;DR

### The Mental Model

```
Pass 1 — Color & Size:
    for each unvisited 1:
        flood fill this island with a unique color
        record the size

Pass 2 — Try Each 0:
    for each 0 cell:
        find distinct neighbor island colors (use a Set!)
        new_size = 1 + sum of those island sizes
        update max

Answer: max of (existing largest, all new_sizes)
```

### The Five Key Insights

1. **Pre-compute island sizes ONCE** to avoid O((nm)²) blowup.
2. **Color each island uniquely** so you can identify them by cell value.
3. **Use a Set** to prevent counting the same island multiple times.
4. **Start colors at 2** (or use a counter starting from 1) to avoid conflict with 0/1.
5. **`colCnt[0]` is unused** — adding water's "color" is harmless because count is 0.

### Why This Style Works

The code is **competitive style** — fast to write, fast to run, but globals and shorthand might confuse newcomers. For interviews, prefer the class-based refactor in Section 14.

### Pattern Family This Belongs To

**"Coloring connected components + per-cell lookup"** — solves:
- Making a Large Island (LC 827).
- Number of Distinct Islands (LC 694).
- Counting Closed Islands (LC 1254).
- Surrounded Regions (LC 130).
- Many "modify graph then re-query" problems.

Master flood fill + coloring + lookup, and you crack a whole genre of problems.

---

*The big takeaway: when a problem says "for each X, compute Y" and Y is expensive, look for pre-computation opportunities. Color the islands once, then look up answers. That's the heart of this elegant algorithm.*
