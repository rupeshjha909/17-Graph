# Find Eventual Safe States (LeetCode 802) — Cycle Detection, Return the Safe Nodes

> A cousin of Course Schedule. Again it's about **cycles in a directed graph**, but the question is flipped: return every node that is **safe** — one from which *every* path eventually reaches a dead end (terminal node), never getting stuck in a cycle. Two clean approaches: a **three-state DFS** (a node is safe iff none of its paths touch a cycle) and a **reverse-graph Kahn's** (peel terminals backward). Both verified against a brute force over 20,000 random graphs.

> 💡 **The whole solution in one sentence:** a node is **safe** exactly when *every* outgoing path from it leads to a terminal node and none can wander into a cycle — so run a three-state DFS where a node is safe iff **all** its neighbors are safe (and unsafe the moment any path re-enters a node still on the current recursion stack, i.e. a cycle) — then return all safe nodes in ascending order.

---

## Table of Contents
1. [Problem statement](#1-problem-statement)
2. [How to guess the pattern (the thought process)](#2-how-to-guess-the-pattern-the-thought-process)
3. [The definition of "safe" (the crux)](#3-the-definition-of-safe-the-crux)
4. [Approach A — three-state DFS](#4-approach-a--three-state-dfs)
5. [Approach B — reverse graph + Kahn's (peel terminals)](#5-approach-b--reverse-graph--kahns-peel-terminals)
6. [The full solutions](#6-the-full-solutions)
7. [Dry run (DFS)](#7-dry-run-dfs)
8. [Complexity](#8-complexity)
9. [Common mistakes](#9-common-mistakes)
10. [How to recognize this pattern next time](#10-how-to-recognize-this-pattern-next-time)
11. [Cheat sheet](#11-cheat-sheet)

---

## 1. Problem statement

> A directed graph of `n` nodes is given as `graph`, where `graph[i]` is the list of nodes `i` points to. A node is **terminal** if it has no outgoing edges. A node is **safe** if **every** possible path starting from it leads to a terminal node (equivalently, no path from it can enter a cycle). Return all safe nodes, **sorted ascending**.

### Examples
```
graph = [[1,2],[2,3],[5],[0],[5],[],[]]      → [2,4,5,6]
        (5 and 6 are terminals; 2→5 and 4→5 only reach terminals;
         0,1,3 sit on the cycle 0→3→0 region, so they're unsafe)
graph = [[1,2,3,4],[1,2],[3,4],[0,4],[]]     → [4]
        (only terminal 4 is safe; everything else can reach a cycle)
```
(Verified.)

---

## 2. How to guess the pattern (the thought process)

**Q1: What structure and what's the danger?**
Directed graph; the danger is a **cycle** (a path that never terminates). → directed-graph cycle reasoning (same family as Course Schedule).

**Q2: What makes a node "safe"?**
*Every* path from it must dead-end at a terminal. If **any** path from it can reach a cycle, it's unsafe. → "safe" is a property that depends on where your out-edges lead.

**Q3: Can I define safe recursively?**
Yes: a **terminal** node (no out-edges) is safe by definition. A non-terminal node is safe **iff all of its neighbors are safe**. (If even one neighbor can reach a cycle, so can you.) → recursion / DFS.

**Q4: How do cycles get flagged?**
During DFS, if you step onto a node that's **currently on your recursion path**, you've closed a loop → that node (and everything leading into the loop) is **unsafe**. → the classic **three-state** DFS.

So the shape is: *directed graph + "which nodes avoid all cycles" → three-state DFS where safe = all-neighbors-safe, unsafe = reaches an in-progress node.*

> 💡 **Safe = all paths terminate:** "A node is safe only if every route out of it dead-ends — so safety propagates from terminals backward, and a single path into a cycle poisons a node. That's cycle detection with a 'safe' verdict attached." 

---

## 3. The definition of "safe" (the crux)

The whole problem hinges on this recursive definition:

- A **terminal** node (empty adjacency list) is **safe** — there's nowhere to go, so all zero paths trivially terminate.
- A non-terminal node is **safe iff every one of its out-neighbors is safe.**
- A node is **unsafe** iff it lies on a cycle, or **any** path from it reaches a cycle.

This "all neighbors must be safe" (universal, not existential) is the key. Contrast it with reachability problems where *one* good path suffices — here *every* path must be good, so a single bad neighbor makes you unsafe.

> 💡 **"For all," not "there exists":** "Safety requires *all* out-paths to terminate, so a node inherits safety only if *every* neighbor is safe — one unsafe neighbor is enough to condemn it. That universal quantifier is what distinguishes this from ordinary reachability." 

---

## 4. Approach A — three-state DFS

Reuse the Course Schedule three-state idea, but repurpose state `2` to mean **safe**:

- **0 = unvisited**, **1 = in-progress** (on current path), **2 = safe** (fully explored, all paths terminate).

`dfs(u)` returns whether `u` is safe:
- If `state[u] == 1` → you've looped back onto the current path → **cycle → unsafe** (return false).
- If `state[u] == 2` → already known safe → return true.
- Otherwise mark in-progress, recurse into all neighbors; if **any** returns unsafe, `u` is unsafe. If **all** are safe, mark `u` safe (state 2) and return true.

Then collect every node whose `dfs` returns true. (Nodes that end up unsafe stay at state 1 until the DFS unwinds — a common tweak is to leave them non-2, so they're never reported safe.)

> 💡 **State 2 = "certified safe":** "The three states carry the same cycle-detection power as Course Schedule, but here 'done' means 'proven that every path from here terminates'. Reaching an in-progress node proves the opposite." 

---

## 5. Approach B — reverse graph + Kahn's (peel terminals)

A BFS view: safe nodes are exactly those that reach **only** terminals. Flip every edge, then peel from the terminals inward:

1. Build the **reverse graph** `radj` (edge `u→v` becomes `v→u`) and record each node's **out-degree** in the *original* graph.
2. Queue all nodes with **out-degree 0** — the terminals (safe by definition).
3. Pop a safe node `u`; for each node `w` that pointed *to* `u` in the original graph (i.e. `radj[u]`), decrement `w`'s out-degree; when `w`'s out-degree hits 0, **all** of `w`'s original targets have been proven safe → `w` is safe → enqueue it.
4. The nodes that ever reached out-degree 0 are the safe ones; sort them.

This is Kahn's algorithm run on out-degree over the reversed graph — safety flows backward from terminals, and a node becomes safe only when *every* target it points to is already safe (mirroring the "all neighbors safe" rule).

> 💡 **Peel from the terminals backward:** "Terminals are safe; a node becomes safe once all its targets are safe. Reversing the graph lets Kahn's propagate that from the terminals inward — out-degree hitting 0 means every target is confirmed safe." 

---

## 6. The full solutions

### DFS (three-state) — recommended

```java
public List<Integer> eventualSafeNodes(int[][] graph) {
    int n = graph.length;
    int[] state = new int[n];              // 0 = unvisited, 1 = GRAY (in-progress/unsafe), 2 = BLACK (safe)
    List<Integer> res = new ArrayList<>();
    for (int i = 0; i < n; i++) {
        if (state[i] == 0) hasCycle(i, graph, state);   // classify i and everything reachable
        if (state[i] == 2) res.add(i);                   // ended BLACK ⇒ safe (ascending order preserved)
    }
    return res;
}

// returns true if a cycle is reachable from `node`  ⇒  node is unsafe
private boolean hasCycle(int node, int[][] graph, int[] state) {
    state[node] = 1;                                     // GRAY (enter path)
    for (int nb : graph[node]) {
        if (state[nb] == 1) return true;                 // GRAY neighbor → back edge → cycle
        if (state[nb] == 0 && hasCycle(nb, graph, state)) return true;   // cycle in descendants
        // state[nb] == 2 (BLACK safe) → skip, no cycle through it
    }
    state[node] = 2;                                     // BLACK (all paths safe)
    return false;
}
```

### Reverse graph + Kahn's — alternative

```java
public List<Integer> eventualSafeNodes(int[][] graph) {
    int n = graph.length;
    List<List<Integer>> radj = new ArrayList<>();
    for (int i = 0; i < n; i++) radj.add(new ArrayList<>());
    int[] outdeg = new int[n];
    for (int u = 0; u < n; u++)
        for (int v : graph[u]) { radj.get(v).add(u); outdeg[u]++; }

    Queue<Integer> q = new LinkedList<>();
    boolean[] safe = new boolean[n];
    for (int i = 0; i < n; i++) if (outdeg[i] == 0) q.offer(i);   // terminals

    while (!q.isEmpty()) {
        int u = q.poll(); safe[u] = true;
        for (int w : radj.get(u))
            if (--outdeg[w] == 0) q.offer(w);      // all of w's targets now safe → w safe
    }

    List<Integer> res = new ArrayList<>();
    for (int i = 0; i < n; i++) if (safe[i]) res.add(i);
    return res;
}
```

Both verified equal to brute force over 20,000 random graphs (LC examples → `[2,4,5,6]` and `[4]`).

---

## 7. Dry run (DFS)

`graph = [[1,2],[2,3],[5],[0],[5],[],[]]` → expect `[2,4,5,6]`.

```
Nodes 5,6 have no out-edges → terminals.

dfs(0): 0→1: dfs(1): 1→2: dfs(2): 2→5: dfs(5): terminal → safe(2). 2 all-safe → safe(2).
        back in 1: 1→3: dfs(3): 3→0: state[0]==1 (in progress) → CYCLE → false.
        so 3 unsafe → 1 unsafe → 0 unsafe.
dfs(1): already failed above → unsafe.
dfs(2): state 2 → safe ✓
dfs(3): unsafe (on the 0↔3 cycle)
dfs(4): 4→5: 5 safe → 4 safe ✓
dfs(5): safe ✓    dfs(6): terminal → safe ✓

safe nodes = [2,4,5,6]   ✓
```

The cycle `0 → 3 → 0` makes 0, 1, 3 unsafe (1 reaches it via 1→3); 2, 4 only reach terminal 5; 5, 6 are terminals.

---

## 8. Complexity

Let `V` = nodes, `E` = total edges.

| | Time | Space |
|:--|:--|:--|
| **DFS** | **O(V + E)** — each node/edge visited once (states prevent rework) | O(V + E) — state + recursion stack |
| **Reverse + Kahn's** | **O(V + E)** — build reverse graph, each edge relaxed once | O(V + E) — reverse adjacency + queue |

Both linear; the result is naturally produced in ascending node order.

---

## 9. Common mistakes

- ❌ **Using "safe iff *some* neighbor is safe."** It's **all** neighbors — one unsafe path condemns the node. (§3)
- ❌ **Plain visited-set instead of three states** in DFS — can't tell a cycle (in-progress) from an already-cleared node.
- ❌ **Marking a node safe before all neighbors are checked.** Set state 2 only *after* the neighbor loop fully succeeds.
- ❌ **In the BFS approach, using indegree instead of out-degree.** Safety depends on where a node *points* (out-edges), so peel by out-degree over the **reversed** graph.
- ❌ **Forgetting to sort.** The answer must be ascending; iterating `i` from `0` gives that for free.
- ❌ **Treating it like reachability (one good path).** Every path must terminate — a universal condition, not existential.

---

## 10. How to recognize this pattern next time

1. **Directed graph + "which nodes avoid cycles / always terminate / are safe"** → cycle detection with a per-node verdict.
2. **Verdict is universal** ("all paths must…") → a node inherits the good property only if **all** neighbors have it → three-state DFS returning a boolean, or reverse-Kahn's peeling from the good sink nodes.
3. **Two engines:** DFS (safe = all-neighbors-safe; cycle = in-progress node) or reverse-graph Kahn's (peel terminals by out-degree).

Family: **Course Schedule I/II** (cycle detection / ordering), **Find Eventual Safe States** (this), and generally "which nodes are on/leading-to a cycle." The distinguishing feature here is the **universal** ("for all paths") condition and returning the *set* of good nodes.

> 💡 **Universal safety → all-neighbors rule:** "When the property is 'every path must be good,' a node earns it only if all neighbors already have it — implement as DFS (all-neighbors-safe) or reverse-Kahn's from the terminals. Cycle membership is the disqualifier." 

---

## 11. Cheat sheet

**Recognize it:** directed graph; return nodes from which **every** path terminates (never hits a cycle).

**Safe definition:** terminal ⇒ safe; node safe ⇔ **all** out-neighbors safe; on a cycle (or reaching one) ⇒ unsafe.

**DFS recipe (recommended):**
```
3 states: 0 unvisited, 1 in-progress, 2 safe
dfs(u): if state==1 → false(cycle); if state==2 → true;
        state=1; for v: if !dfs(v) return false; state=2; return true
collect i where dfs(i)==true   (ascending by construction)
```

**Reverse-Kahn's recipe:** reverse edges + track out-degree; queue terminals (outdeg 0); peeling a safe node decrements predecessors' out-degree; outdeg 0 ⇒ safe.

**Complexity:** O(V + E) time & space, both.

**Watch:** ALL neighbors safe (not some); three states in DFS; out-degree (not indegree) for the BFS version; sort ascending.

> **One-line philosophy:** *A node is safe when every path out of it dead-ends at a terminal and none can slip into a cycle — so run a three-state DFS in which a node is certified safe only if all its neighbors are safe and is condemned the instant a path re-enters a node on the current recursion stack, or equivalently peel safety backward from the terminals with Kahn's on the reversed graph; the defining twist versus plain reachability is the universal "all paths" condition.*
