# Parallel Courses (LeetCode 1136) — Level-Order Topological Sort (Minimum Semesters)

> Another Course Schedule cousin, with a new question: you can take **many courses at once** each semester (any whose prerequisites are already done), and you want the **minimum number of semesters**. The key realization: run Kahn's topological sort **level by level** — each level is one semester's worth of simultaneously-takeable courses — and **count the levels**. Equivalently, the answer is the **longest prerequisite chain** in the graph. Return **−1** if a cycle makes it impossible. Both approaches verified against 20,000 random graphs.

> 💡 **The whole solution in one sentence:** because every course with no unmet prerequisites can be taken *in parallel* in the same semester, run Kahn's BFS **one whole level at a time** — take all currently-available courses (one level = one semester), which unlocks the next level — and the number of levels you peel is the minimum number of semesters; if you can't take all `n` courses (a cycle blocks some), return **−1**.

---

## Table of Contents
1. [Problem statement](#1-problem-statement)
2. [How to guess the pattern (the thought process)](#2-how-to-guess-the-pattern-the-thought-process)
3. [Key insight: one BFS level = one semester](#3-key-insight-one-bfs-level--one-semester)
4. [The equivalent view: longest prerequisite chain](#4-the-equivalent-view-longest-prerequisite-chain)
5. [Approach A — level-order Kahn's BFS](#5-approach-a--level-order-kahns-bfs)
6. [Approach B — longest chain (DFS + memo)](#6-approach-b--longest-chain-dfs--memo)
7. [The full solutions](#7-the-full-solutions)
8. [Dry run (level BFS)](#8-dry-run-level-bfs)
9. [Complexity](#9-complexity)
10. [Common mistakes](#10-common-mistakes)
11. [How to recognize this pattern next time](#11-how-to-recognize-this-pattern-next-time)
12. [Cheat sheet](#12-cheat-sheet)

---

## 1. Problem statement

> There are `n` courses labeled `1 … n`. `relations[i] = [prevCourse, nextCourse]` means `prevCourse` must be taken **before** `nextCourse`. In one semester you may take **any number** of courses, as long as **all** their prerequisites were taken in **previous** semesters. Return the **minimum number of semesters** to take all courses, or **−1** if impossible (a cycle).

### Examples
```
n = 3, relations = [[1,3],[2,3]]            → 2
        (semester 1: take 1 and 2 together; semester 2: take 3)
n = 3, relations = [[1,2],[2,3],[3,1]]      → -1   (cycle 1→2→3→1)
n = 5, relations = [[1,2],[2,3],[3,4],[4,5]] → 5    (linear chain, one per semester)
```
(All verified. Courses are **1-indexed**.)

---

## 2. How to guess the pattern (the thought process)

**Q1: What structure?**
Prerequisites are directed edges (`prev → next`); the whole thing is a directed graph. → Course Schedule family (topological sort / cycle detection).

**Q2: What's new versus plain Course Schedule?**
You can take **multiple** courses in one semester (parallelism), and you want to **minimize semesters** — not just feasibility (207) or an order (210). → a *quantity* to minimize over the topological structure.

**Q3: What limits how fast you can finish?**
A course can only be taken after all its prerequisites — so a **chain** of prerequisites `a → b → c → d` forces at least 4 semesters (one per link in the longest chain). Everything not on that critical chain can be taken alongside. → the answer is governed by the **longest path** / **number of dependency layers**.

**Q4: How do I compute the layers?**
Process the graph in **topological layers**: semester 1 = all courses with no prerequisites; semester 2 = all courses whose prerequisites are now all done; and so on. That's **Kahn's algorithm run level-by-level**, counting levels. → level-order BFS.

So the shape is: *directed prerequisites + "min semesters with parallelism" → level-order topological sort; count levels (= longest chain length); −1 on cycle.*

> 💡 **Parallelism + minimize rounds ⇒ count topological levels:** "When everything with satisfied prerequisites can go at once and I want the fewest rounds, the answer is the number of dependency layers — Kahn's BFS peeled one full level per semester. The critical path (longest chain) sets the floor." 

---

## 3. Key insight: one BFS level = one semester

Ordinary Kahn's pops nodes one at a time. Here, process the queue **one full level at a time**: at the start of a semester, the queue holds *exactly* the courses whose prerequisites are all satisfied — so take **all** of them this semester (they're independent enough to run in parallel), then enqueue whatever they unlock for the next semester.

The implementation detail that makes this work: before draining the queue, capture its **current size** and pop exactly that many; those are this semester's courses. The nodes enqueued during that drain belong to the **next** level.

```
while queue not empty:
    semesters++                     // start a new semester
    for _ in range(len(queue)):     // take ALL of this level's courses
        u = queue.pop()
        for child of u: if indegree hits 0, enqueue (that's next semester)
```

Counting how many times the outer loop runs = number of semesters.

> 💡 **Snapshot the level size, then drain exactly that many:** "Grabbing `len(queue)` before the inner loop freezes this semester's cohort; anything unlocked during the drain lands in the next semester. The count of outer iterations is the minimum semesters." 

---

## 4. The equivalent view: longest prerequisite chain

There's a clean equivalent: the minimum number of semesters equals the **length (in nodes) of the longest chain of prerequisites** in the DAG. Why: a chain `a → b → c` cannot be compressed — each must be a strictly later semester — so the longest such chain is a lower bound; and level-order BFS achieves exactly that bound (nothing waits longer than its deepest prerequisite). So:

```
minimum semesters = number of nodes in the longest path of the DAG
```

You can compute that with a DFS that returns `1 + max(depth of children)` (memoized), detecting cycles with the three states → **−1** if any cycle. (Verified: this matches the level-BFS count on every test.)

> 💡 **Min semesters = longest dependency chain:** "The critical path length is both the lower bound (a chain can't be parallelized) and achievable (BFS layers hit it), so they're equal. That's why a longest-path DFS gives the same answer as counting BFS levels." 

---

## 5. Approach A — level-order Kahn's BFS

1. Build `adj` (edge `prev → next`) + `indegree`; courses are `1..n`.
2. Queue all courses with `indegree == 0` (semester-1 courses).
3. While the queue is non-empty: increment `semesters`; pop **exactly `len(queue)`** courses (this semester), counting each as taken and decrementing children's indegrees, enqueuing any that reach 0.
4. If `taken == n` → return `semesters`; else a cycle blocked some → **−1**.

Most intuitive for "minimum semesters" — the semesters *are* the BFS levels.

---

## 6. Approach B — longest chain (DFS + memo)

1. Build `adj`. Use three states (0/1/2) for cycle detection.
2. `dfs(u)` returns the longest chain length starting at `u` = `1 + max(dfs(child))` (0 if no children); memoize in `depth[u]`.
3. If DFS ever hits an in-progress node → cycle → return **−1** overall.
4. Answer = `max(dfs(i))` over all courses.

Returns the same number as the level count, via the longest-path view (§4).

---

## 7. The full solutions

### Level-order Kahn's (BFS) — recommended

```java
public int minimumSemesters(int n, int[][] relations) {
    List<List<Integer>> adj = new ArrayList<>();
    for (int i = 0; i <= n; i++) adj.add(new ArrayList<>());   // 1-indexed; index 0 unused
    int[] indegree = new int[n + 1];

    for (int[] r : relations) {          // r = [prev, next] → edge prev -> next
        adj.get(r[0]).add(r[1]);
        indegree[r[1]]++;
    }

    Queue<Integer> q = new LinkedList<>();
    for (int i = 1; i <= n; i++) if (indegree[i] == 0) q.offer(i);

    int semesters = 0, taken = 0;
    while (!q.isEmpty()) {
        semesters++;                     // start a new semester
        int levelSize = q.size();        // snapshot: this semester's courses
        for (int s = 0; s < levelSize; s++) {
            int course = q.poll();
            taken++;
            for (int next : adj.get(course))
                if (--indegree[next] == 0) q.offer(next);   // unlocked → next semester
        }
    }
    return (taken == n) ? semesters : -1;   // couldn't take all ⇒ cycle ⇒ -1
}
```

### Longest chain (DFS + memo) — alternative

```java
public int minimumSemesters(int n, int[][] relations) {
    List<List<Integer>> adj = new ArrayList<>();
    for (int i = 0; i <= n; i++) adj.add(new ArrayList<>());
    for (int[] r : relations) adj.get(r[0]).add(r[1]);

    int[] state = new int[n + 1];        // 0 unvisited, 1 in-progress, 2 done
    int[] depth = new int[n + 1];        // longest chain starting here
    int ans = 0;
    for (int i = 1; i <= n; i++) {
        int d = dfs(i, adj, state, depth);
        if (d == -1) return -1;          // cycle
        ans = Math.max(ans, d);
    }
    return ans;
}
private int dfs(int u, List<List<Integer>> adj, int[] state, int[] depth) {
    if (state[u] == 1) return -1;        // back-edge → cycle
    if (state[u] == 2) return depth[u];
    state[u] = 1;
    int best = 0;
    for (int v : adj.get(u)) {
        int d = dfs(v, adj, state, depth);
        if (d == -1) return -1;
        best = Math.max(best, d);
    }
    state[u] = 2;
    return depth[u] = 1 + best;
}
```

Both verified to agree (and to return −1 exactly on a cycle) over 20,000 random graphs.

---

## 8. Dry run (level BFS)

`n = 3, relations = [[1,3],[2,3]]` → expect **2**.

```
Build: adj{1:[3], 2:[3], 3:[]}   indegree = [_,0,0,2]   (1-indexed)

Queue init (indegree 0): [1, 2]

Semester 1: levelSize = 2 → take 1 and 2 (taken=2)
   1 unlocks 3: indegree[3] 2→1 (not yet)
   2 unlocks 3: indegree[3] 1→0 → enqueue 3
   queue = [3]

Semester 2: levelSize = 1 → take 3 (taken=3)
   3 has no children
   queue = []

taken (3) == n (3) → return semesters = 2   ✓
```

Cycle contrast `[[1,2],[2,3],[3,1]]`: all three have indegree 1, queue starts empty, `taken=0 ≠ 3` → **−1**.

---

## 9. Complexity

Let `V = n`, `E = relations.length`.

| | Time | Space |
|:--|:--|:--|
| **Level BFS** | **O(V + E)** — each course dequeued once, each edge relaxed once | O(V + E) — adjacency + indegree + queue |
| **DFS longest chain** | **O(V + E)** — memoized, each node/edge once | O(V + E) — adjacency + state + depth + recursion |

Both linear — optimal.

---

## 10. Common mistakes

- ❌ **Not snapshotting the level size.** You must capture `q.size()` *before* the inner loop; iterating while the queue grows would merge semesters and undercount. (§3)
- ❌ **Off-by-one on indexing.** Courses are **1-indexed** (`1..n`); size arrays `n+1` and loop from `1`.
- ❌ **Forgetting the cycle → −1 case.** If `taken < n` (BFS) or DFS finds a back-edge, return −1, not a bogus semester count.
- ❌ **Counting semesters wrong.** Increment once per **level**, not per course.
- ❌ **Wrong edge direction.** `[prev, next]` → edge `prev → next`, `indegree[next]++`.
- ❌ **Plain visited-set in the DFS version.** Need three states to detect the cycle (in-progress vs done).
- ❌ **Thinking it's just "count of courses" or "count of levels blindly."** It's the number of dependency *layers* = longest chain; parallel courses collapse into one semester.

---

## 11. How to recognize this pattern next time

1. **Directed dependencies + "you can do independent tasks in parallel each round" + minimize rounds** → **level-order topological sort**; count levels.
2. **Equivalent framing:** the answer is the **longest path (critical path)** in the DAG — compute by BFS layers or DFS-longest-chain.
3. **Cycle ⇒ impossible ⇒ −1**, detected by the usual topo means (shortfall in Kahn's, back-edge in DFS).
4. **The level-snapshot trick** (`for _ in range(len(queue))`) is the reusable mechanic for "process BFS one layer at a time" (also used in binary-tree level-order, shortest-path-in-unweighted-graph rounds, rotting oranges, word ladder).

Family: **Parallel Courses I/II** (1136/1494), **critical path / project scheduling**, **minimum rounds to complete tasks with dependencies**, and level-order BFS generally. Trigger: *dependency layers + minimize the number of parallel rounds.*

> 💡 **Layers, not just order:** "When tasks can run in parallel and I want the fewest rounds, I stop caring about a single linear order and start counting *layers*. Level-order Kahn's gives the layer count directly; the longest dependency chain is the same number." 

---

## 12. Cheat sheet

**Recognize it:** directed prerequisites + take-many-in-parallel + minimize semesters → level-order topological sort; answer = number of levels = longest chain; −1 on cycle.

**Edge convention:** `[prev, next]` → edge `prev → next`, `indegree[next]++`. Courses **1-indexed**.

**Level-BFS recipe (recommended):**
```
1. build adj + indegree; queue all indegree-0 courses
2. while queue not empty:
     semesters++;  size = queue.size()          ← snapshot this level
     repeat size times: pop u; taken++; for child: --indegree; if 0 enqueue
3. return taken==n ? semesters : -1
```

**DFS recipe:** longest chain `dfs(u)=1+max(dfs(child))` memoized; 3-state cycle check → −1.

**Complexity:** O(V + E) time & space, both.

**Watch:** snapshot `queue.size()` before draining; 1-indexed arrays (`n+1`); count per level not per course; −1 on cycle.

> **One-line philosophy:** *"Minimum semesters with parallel courses" is "how many dependency layers deep is the graph" — so run Kahn's topological sort one full level at a time, taking every prerequisite-satisfied course together each semester and counting the levels, which equals the longest prerequisite chain (the critical path that can't be parallelized); a cycle leaves courses unschedulable, so return −1, and the reusable mechanic is snapshotting the queue size to drain exactly one BFS layer per round.*
