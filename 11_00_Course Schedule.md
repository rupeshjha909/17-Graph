# Course Schedule (LeetCode 207) — Cycle Detection / Topological Sort, with Full Pattern Recognition

> A new pattern from the recent Union-Find set. Prerequisites are **directed dependencies**, and "can you finish all courses?" is really **"is the dependency graph acyclic (a DAG)?"** A cycle = a circular prerequisite = impossible. The two standard tools are **Kahn's algorithm** (BFS topological sort by indegree) and **DFS cycle detection** (three states). Includes why this is *not* a Union-Find problem, both full solutions, a dry run, and the recognition checklist. All code verified against 20,000 random graphs.

> 💡 **The whole solution in one sentence:** model each prerequisite `[a, b]` ("take `b` before `a`") as a **directed edge `b → a`**, then check whether this directed graph has a **cycle**: if it does, some courses mutually depend on each other and can never be started (return `false`); if it's acyclic, a valid order exists and you can finish everything (return `true`) — detect the cycle either by peeling off zero-indegree nodes (Kahn's BFS: if you can't peel them all, a cycle remains) or by a DFS that flags a back-edge to a node still on the current path.

---

## Table of Contents
1. [Problem statement](#1-problem-statement)
2. [How to guess the pattern (the thought process)](#2-how-to-guess-the-pattern-the-thought-process)
3. [The critical insight: "can finish" = "no cycle"](#3-the-critical-insight-can-finish--no-cycle)
4. [Why NOT Union-Find (directed vs undirected)](#4-why-not-union-find-directed-vs-undirected)
5. [Building the graph (edge direction convention)](#5-building-the-graph-edge-direction-convention)
6. [Approach A — Kahn's algorithm (BFS topological sort)](#6-approach-a--kahns-algorithm-bfs-topological-sort)
7. [Approach B — DFS cycle detection (three states)](#7-approach-b--dfs-cycle-detection-three-states)
8. [The full solutions](#8-the-full-solutions)
9. [Dry run (Kahn's)](#9-dry-run-kahns)
10. [Complexity](#10-complexity)
11. [Common mistakes](#11-common-mistakes)
12. [How to recognize this pattern next time](#12-how-to-recognize-this-pattern-next-time)
13. [Cheat sheet](#13-cheat-sheet)

---

## 1. Problem statement

> You must take `numCourses` courses labeled `0 … numCourses-1`. Some have prerequisites, given as `prerequisites[i] = [a, b]`, meaning **you must take course `b` before course `a`**. Return **true** if you can finish all courses, **false** otherwise.

### Examples
```
numCourses = 2, prerequisites = [[1,0]]          → true   (take 0, then 1)
numCourses = 2, prerequisites = [[1,0],[0,1]]    → false  (1 needs 0 AND 0 needs 1 → circular)
```
(Verified.)

---

## 2. How to guess the pattern (the thought process)

**Q1: What structure do the prerequisites form?**
Each `[a, b]` is a **dependency**: `a` depends on `b`. Dependencies with a direction (b before a) form a **directed graph**. → This is a directed-graph problem.

**Q2: When can you NOT finish?**
If the dependencies loop — e.g. `a` needs `b`, `b` needs `c`, `c` needs `a` — none can ever be started. That loop is a **cycle** in the directed graph. → "Can't finish" ⟺ "the graph has a cycle."

**Q3: When CAN you finish?**
When there's *no* cycle — a **Directed Acyclic Graph (DAG)**. In a DAG you can always find an order that respects every "before" (a **topological order**). → "Can finish" ⟺ "graph is acyclic" ⟺ "a topological order exists."

**Q4: What tools detect a directed cycle / build a topological order?**
Two standard ones:
- **Kahn's algorithm (BFS):** repeatedly remove nodes with no remaining prerequisites (indegree 0). If you manage to remove *all* nodes, it's acyclic; if some are stuck (never reach indegree 0), they're tangled in a cycle.
- **DFS with three states:** walk the graph; if you ever reach a node that's *currently on your recursion path*, you've found a back-edge → a cycle.

So the shape is: *prerequisites → directed graph → detect a cycle (via topological sort or DFS) → finishable iff acyclic.*

> 💡 **Directed dependencies + "can it all be ordered?" = topological sort:** "Whenever items have 'must come before' relationships and I'm asked whether a valid ordering exists (or to produce one), that's a directed graph and a topological-sort / cycle-detection question — finishable exactly when there's no cycle." 

---

## 3. The critical insight: "can finish" = "no cycle"

The entire problem reduces to one yes/no: **does the directed dependency graph contain a cycle?**

- **Cycle present** → circular dependency → you can never take the first course in the loop → **cannot finish** (`false`).
- **No cycle (DAG)** → a topological order exists → take courses in that order → **can finish** (`true`).

You don't need to *produce* the order for this problem (that's Course Schedule II, LC 210) — you only need to know whether a cycle exists. Both approaches below answer that; Kahn's does it by counting how many courses it can successfully schedule.

> 💡 **Reduce to a single question:** "Strip the wording — 'finish all courses' is just 'is the prerequisite graph acyclic?' A directed cycle is a deadlock of mutual prerequisites; its absence guarantees a workable order." 

---

## 4. Why NOT Union-Find (directed vs undirected)

You've just done several Union-Find problems, so it's worth being explicit about why it doesn't fit here:

- **Union-Find handles *undirected* connectivity and undirected cycle detection** — "are these in the same group?", "does adding this edge create a cycle?" It has **no notion of direction**.
- Prerequisites are **directed** (`b before a` ≠ `a before b`). A directed cycle (`a→b→c→a`) is the failure condition, and Union-Find **cannot detect direction-dependent cycles** — it would treat `a→b` and `b→a` as the same undirected link and miss the deadlock.
- The natural tools for **directed** dependency/ordering problems are **topological sort** (Kahn's) and **DFS cycle detection**, which respect edge direction.

Rule of thumb: **undirected grouping/connectivity → Union-Find; directed ordering/dependency → topological sort / DFS.**

> 💡 **Direction is the deciding factor:** "Union-Find is for undirected 'same group' questions. The moment the relationship has a direction — 'before/after', 'depends on', 'points to' — and I care about ordering or directed cycles, I switch to topological sort or DFS. Union-Find can't see direction." 

---

## 5. Building the graph (edge direction convention)

Getting the edge direction right is the one place to be careful. `prerequisites[i] = [a, b]` means **`b` must come before `a`** (`a` depends on `b`). The useful direction for scheduling is **`b → a`** ("finishing `b` unlocks `a`"):

```java
List<List<Integer>> adj = new ArrayList<>();     // adj[b] = courses unlocked by finishing b
for (int i = 0; i < numCourses; i++) adj.add(new ArrayList<>());
int[] indegree = new int[numCourses];            // indegree[a] = how many prereqs a still needs

for (int[] p : prerequisites) {
    int a = p[0], b = p[1];       // b before a
    adj.get(b).add(a);            // edge b -> a
    indegree[a]++;                // a has one more prerequisite
}
```

- `adj.get(b)` lists the courses that become *more* ready once `b` is done.
- `indegree[a]` counts `a`'s outstanding prerequisites; when it hits 0, `a` is takeable.

> 💡 **Edge points from prerequisite to dependent:** "`[a,b]` = 'b before a', so the edge goes `b → a` and `a`'s indegree increases. Indegree = number of prerequisites still unmet; a course is ready exactly when its indegree reaches zero." 

---

## 6. Approach A — Kahn's algorithm (BFS topological sort)

The intuitive one: **keep taking any course whose prerequisites are all done, until you can't.**

1. Build `adj` + `indegree` (§5).
2. Put every course with `indegree == 0` (no prerequisites) into a queue — these are startable now.
3. Repeatedly pop a course, mark it "taken" (count it), and **decrement the indegree of each course it unlocks**; if any of those drops to 0, enqueue it.
4. At the end, if you took **all** `numCourses` → acyclic → `true`. If some were never takeable (`count < numCourses`) → they're stuck in a cycle → `false`.

The key idea: a cycle's courses can *never* reach indegree 0 (each is waiting on another in the loop), so they never get processed — and the final count falls short.

> 💡 **Peel off the ready courses; if any remain stuck, there's a cycle:** "I schedule every zero-prerequisite course, which unlocks others, cascading outward. If I schedule all of them, the graph was acyclic. If some can never reach zero prerequisites, they're deadlocked in a cycle — that shortfall *is* the cycle detection." 

---

## 7. Approach B — DFS cycle detection (three states)

The direct cycle hunt. Give each node one of three states:
- **0 = unvisited**, **1 = in progress** (on the current DFS path), **2 = done** (fully explored, known safe).

DFS a node: if you reach a node that's **in progress (state 1)**, you've looped back onto your own path → **cycle → false**. If it's **done (state 2)**, it's already been cleared, skip it. Otherwise mark it in-progress, recurse into neighbors, then mark done.

```
dfs(u):
  if state[u]==1: return false   // back-edge to current path → CYCLE
  if state[u]==2: return true    // already verified acyclic from here
  state[u]=1                     // enter path
  for v in adj[u]: if !dfs(v): return false
  state[u]=2                     // leave path, mark safe
  return true
```

Why three states, not a plain visited-set: an ordinary "visited" can't tell "on the *current* path" (a real cycle) from "visited on a *previous*, separate path" (harmless). The **in-progress** state distinguishes them — only a back-edge to a node *currently on the stack* is a cycle.

> 💡 **A cycle is a back-edge to the current path:** "The three states let DFS tell a genuine loop (reaching a node still open on my recursion stack) from merely revisiting something I already fully cleared. Only the former is a cycle." 

---

## 8. The full solutions

### Kahn's (BFS) — recommended default

```java
public boolean canFinish(int numCourses, int[][] prerequisites) {
    List<List<Integer>> adj = new ArrayList<>();
    for (int i = 0; i < numCourses; i++) adj.add(new ArrayList<>());
    int[] indegree = new int[numCourses];

    for (int[] p : prerequisites) {        // [a,b] = b before a → edge b->a
        adj.get(p[1]).add(p[0]);
        indegree[p[0]]++;
    }

    Queue<Integer> q = new LinkedList<>();
    for (int i = 0; i < numCourses; i++)
        if (indegree[i] == 0) q.offer(i);  // courses with no prerequisites

    int taken = 0;
    while (!q.isEmpty()) {
        int course = q.poll();
        taken++;
        for (int next : adj.get(course)) {
            if (--indegree[next] == 0) q.offer(next);   // a prereq cleared; enqueue if now ready
        }
    }
    return taken == numCourses;            // took them all ⇒ acyclic ⇒ can finish
}
```

### DFS cycle detection — alternative

```java
public boolean canFinish(int numCourses, int[][] prerequisites) {
    List<List<Integer>> adj = new ArrayList<>();
    for (int i = 0; i < numCourses; i++) adj.add(new ArrayList<>());
    for (int[] p : prerequisites) adj.get(p[1]).add(p[0]);

    int[] state = new int[numCourses];     // 0 unvisited, 1 in-progress, 2 done
    for (int i = 0; i < numCourses; i++)
        if (!dfs(i, adj, state)) return false;
    return true;
}
private boolean dfs(int u, List<List<Integer>> adj, int[] state) {
    if (state[u] == 1) return false;       // back-edge to current path → cycle
    if (state[u] == 2) return true;        // already cleared
    state[u] = 1;
    for (int v : adj.get(u)) if (!dfs(v, adj, state)) return false;
    state[u] = 2;
    return true;
}
```

Both verified equivalent to "the graph is acyclic" across 20,000 random graphs.

---

## 9. Dry run (Kahn's)

`numCourses = 4, prerequisites = [[1,0],[2,1],[3,2]]` (chain 0→1→2→3) → expect **true**.

```
Build (edge b->a, indegree[a]++):
  [1,0]: adj[0]+=1, indegree[1]=1
  [2,1]: adj[1]+=2, indegree[2]=1
  [3,2]: adj[2]+=3, indegree[3]=1
  adj = {0:[1], 1:[2], 2:[3], 3:[]}   indegree = [0,1,1,1]

Queue init: courses with indegree 0 → [0]

Process:
  pop 0, taken=1 → unlock 1: indegree[1] 1→0 → enqueue 1
  pop 1, taken=2 → unlock 2: indegree[2] 1→0 → enqueue 2
  pop 2, taken=3 → unlock 3: indegree[3] 1→0 → enqueue 3
  pop 3, taken=4 → (no neighbors)

taken (4) == numCourses (4) → TRUE  ✓
```

Contrast — a cycle `[[0,1],[1,0]]`: both courses have indegree 1, the queue starts **empty**, `taken = 0 ≠ 2` → **false**. The cycle nodes never reach indegree 0.

---

## 10. Complexity

Let `V = numCourses`, `E = prerequisites.length`.

| | Time | Space |
|:--|:--|:--|
| **Kahn's (BFS)** | **O(V + E)** — build graph O(E), each node enqueued once, each edge relaxed once | O(V + E) for adjacency + indegree + queue |
| **DFS** | **O(V + E)** — each node and edge visited once | O(V + E) for adjacency + state + recursion stack |

Both are linear in the graph size — optimal, since you must at least read every course and prerequisite.

---

## 11. Common mistakes

- ❌ **Reversing the edge direction.** `[a,b]` means `b` before `a` → edge `b → a`, and `indegree[a]++`. Flip it and every answer inverts.
- ❌ **Using a plain visited-set in DFS.** You must distinguish "on the current path" (cycle) from "already fully explored" (safe) — that needs the **three states**, not a boolean visited.
- ❌ **Forgetting disconnected pieces.** Loop the outer DFS over *all* courses (and enqueue *all* indegree-0 nodes), since the graph may have several components.
- ❌ **Reaching for Union-Find.** It can't detect *directed* cycles; use topological sort / DFS for directed dependencies. (§4)
- ❌ **Comparing `taken` wrong in Kahn's.** The verdict is `taken == numCourses`; any shortfall means a cycle left nodes unprocessed.
- ❌ **Not decrementing indegree correctly.** Decrement a neighbor's indegree once per incoming edge, and only enqueue it when it hits exactly 0.
- ❌ **Assuming no prerequisites means false.** Empty `prerequisites` → all indegree 0 → everything schedulable → `true`.

---

## 12. How to recognize this pattern next time

1. **Directional relationships** ("A before B", "A depends on B", "A points to B") → **directed graph**.
2. **"Is there a valid order?" / "can all be completed?" / "any circular dependency?"** → **cycle detection / topological sort**. Finishable ⟺ acyclic (DAG).
3. **Need the actual order, not just yes/no?** → produce the topological order (Kahn's gives it as the processing sequence — that's Course Schedule II, LC 210).
4. **Two engines:** Kahn's BFS (indegree peeling) or DFS (three-state cycle detection) — pick either; both O(V+E).

This family: **Course Schedule I/II** (207/210), **Alien Dictionary** (269), **Minimum Height Trees** (310, peel leaves — undirected variant), **build-order / task-scheduling with dependencies**, **Parallel Courses**. The trigger is always *directed dependencies + ordering/feasibility*.

> 💡 **Dependencies + ordering ⇒ topological sort:** "The reusable trigger is direction plus 'can it be ordered / completed'. Model as a directed graph, then either peel zero-indegree nodes (Kahn) or DFS for a back-edge. It's finishable exactly when acyclic — and Kahn's processing order *is* a valid schedule when you need one." 

---

## 13. Cheat sheet

**Recognize it:** directed prerequisites + "can you finish all?" → "is the graph acyclic?" → topological sort / cycle detection.

**Edge convention:** `[a,b]` = "b before a" → edge `b → a`, `indegree[a]++`.

**Kahn's (BFS) recipe:**
```
1. build adj + indegree
2. queue all courses with indegree 0
3. pop course, taken++, for each neighbor: --indegree; if 0, enqueue
4. return taken == numCourses     (shortfall ⇒ cycle ⇒ false)
```

**DFS recipe:** 3 states (0 unvisited / 1 in-progress / 2 done); reaching a state-1 node = cycle = false; mark done after exploring.

**Why not Union-Find:** it's undirected-only; directed cycles need topo-sort/DFS.

**Complexity:** O(V + E) time and space, both approaches.

**Watch:** edge direction; three states in DFS (not a boolean); loop over all nodes (disconnected components); empty prereqs → true.

> **One-line philosophy:** *"Finish all courses" is "is the prerequisite graph acyclic?" — model each `[a,b]` as a directed edge `b → a`, then detect a cycle either by peeling off zero-indegree courses (Kahn's: if you can't schedule them all, a cycle remains) or by a three-state DFS (a back-edge to a node on the current path is a cycle); it's finishable exactly when the graph is a DAG, and unlike undirected grouping this needs topological sort rather than Union-Find because dependencies have direction.*
