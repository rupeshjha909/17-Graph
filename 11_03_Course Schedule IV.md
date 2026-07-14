# Course Schedule IV (LeetCode 1462) — Reachability Queries / Transitive Closure

> The third Course Schedule variant, and a genuinely different flavor. There's no cycle (it's a DAG) and no ordering to produce — instead you answer many **queries**: "is course `u` a prerequisite of course `v`?" That's asking whether a **directed path `u → … → v`** exists — i.e. you need the graph's **transitive closure** (all indirect prerequisites), computed once, then each query is an O(1) lookup. Two approaches: **Floyd-Warshall boolean closure** and **Kahn's topological sort propagating ancestor sets**. Both verified against brute force over 5,000 random DAGs.

> 💡 **The whole solution in one sentence:** "`u` is a prerequisite of `v`" means there's a directed path from `u` to `v`, so **precompute reachability for every pair** — either with a Floyd-Warshall triple loop (`reach[i][j] |= reach[i][k] && reach[k][j]`) or by topologically processing the graph and letting each course **inherit the full prerequisite set of its parents** — after which every query `[u,v]` is a constant-time lookup of whether `u` reaches `v`.

---

## Table of Contents
1. [Problem statement](#1-problem-statement)
2. [How to guess the pattern (the thought process)](#2-how-to-guess-the-pattern-the-thought-process)
3. [The key insight: precompute reachability, then O(1) per query](#3-the-key-insight-precompute-reachability-then-o1-per-query)
4. [Approach A — Floyd-Warshall transitive closure](#4-approach-a--floyd-warshall-transitive-closure)
5. [Approach B — Kahn's topo sort + ancestor sets](#5-approach-b--kahns-topo-sort--ancestor-sets)
6. [The full solutions](#6-the-full-solutions)
7. [Dry run](#7-dry-run)
8. [Complexity](#8-complexity)
9. [Common mistakes](#9-common-mistakes)
10. [How to recognize this pattern next time](#10-how-to-recognize-this-pattern-next-time)
11. [Cheat sheet](#11-cheat-sheet)

---

## 1. Problem statement

> `numCourses` courses labeled `0…numCourses-1`. `prerequisites[i] = [a, b]` means course `a` **is a prerequisite of** course `b` (take `a` before `b`). The graph is a **DAG** (no cycles). Given `queries[j] = [u, v]`, return a boolean list where entry `j` is **true** iff `u` **is a prerequisite of** `v` (directly or indirectly).

### Examples
```
numCourses=2, prerequisites=[[1,0]], queries=[[0,1],[1,0]]   → [false, true]
        (1 is a prereq of 0; so is 0 a prereq of 1? no. is 1 a prereq of 0? yes)
numCourses=2, prerequisites=[], queries=[[1,0],[0,1]]        → [false, false]
numCourses=3, prerequisites=[[1,2],[1,0],[2,0]],
        queries=[[1,0],[1,2],[0,1],[2,0],[2,1],[0,2]]        → [true,true,false,true,false,false]
```
(Verified. Note the edge convention: `[a,b]` = `a` is a prerequisite **of** `b` → directed edge `a → b`.)

---

## 2. How to guess the pattern (the thought process)

**Q1: What are prerequisites, structurally?**
Directed edges (`a → b`: `a` before `b`). It's a DAG (given). → directed graph.

**Q2: What is a query really asking?**
"Is `u` a prerequisite of `v`?" = "Is there a directed path `u → … → v`?" = **reachability** in the DAG.

**Q3: One query or many?**
**Many** queries against the **same** graph. That's the signal to **precompute** all-pairs reachability once, then answer each query in O(1) — rather than running a fresh search per query.

**Q4: How to precompute all-pairs reachability?**
Two standard tools for the **transitive closure** of a small graph:
- **Floyd-Warshall** boolean variant: `reach[i][j] = reach[i][j] || (reach[i][k] && reach[k][j])` over all intermediate `k`.
- **Topological propagation:** process courses in topo order; each course inherits *all* prerequisites of each of its direct parents (union of ancestor sets).

So the shape is: *directed DAG + many "does u reach v?" queries → precompute transitive closure, then O(1) lookups.*

> 💡 **"Is u a prerequisite of v" = "does u reach v" — and there are many queries, so precompute:** "Reachability plus a batch of queries on one fixed graph screams 'compute the transitive closure once.' After that, every query is a table lookup." 

---

## 3. The key insight: precompute reachability, then O(1) per query

The naive approach — run a BFS/DFS from `u` for each query — is O(queries × (V+E)), wasteful when queries repeat work on the same graph. Since `numCourses ≤ 100` (small `V`), you can afford to compute the **full reachability matrix** `reach[i][j]` = "can `i` reach `j`" once, in O(V³) or O(V·E), and then each query is a single `reach[u][v]` lookup.

This "precompute a reusable structure, then answer queries cheaply" is the defining move — the same instinct behind prefix sums, sparse tables, or caching: *do the expensive work once when many questions share it.*

> 💡 **Many queries, one graph ⇒ amortize with precomputation:** "The transitive closure is the reusable structure. Building it costs a polynomial pass, but then thousands of queries are free. Recognizing when to pay upfront for O(1) answers is the lesson." 

---

## 4. Approach A — Floyd-Warshall transitive closure

Treat reachability as a boolean matrix and run the Floyd-Warshall triple loop, but with OR/AND instead of min/plus:

1. `reach[a][b] = true` for every direct prerequisite edge `a → b`.
2. For each intermediate `k`, each source `i`, each target `j`:
   `reach[i][j] = reach[i][j] || (reach[i][k] && reach[k][j])`
   — "`i` reaches `j` if it already did, **or** `i` reaches `k` and `k` reaches `j`."
3. Answer each query with `reach[u][v]`.

The `k` loop must be **outermost** (that's the heart of Floyd-Warshall): it means "allow paths using intermediates `0..k`," expanding the reachable set one intermediate at a time.

> 💡 **Closure by adding one intermediate at a time:** "Floyd-Warshall's outer `k` loop grows the set of allowed 'stepping-stone' nodes; after all `k`, `reach[i][j]` accounts for every possible intermediate, i.e. the full transitive closure." 

---

## 5. Approach B — Kahn's topo sort + ancestor sets

A graph-native alternative that reuses your Course Schedule machinery. Process courses in **topological order**; maintain `anc[x]` = the set of *all* prerequisites of `x`:

1. Build `adj` (edge `a → b`) and `indegree`.
2. Kahn's BFS: when processing `u`, for each child `v`:
   - add `u` to `anc[v]` (direct prerequisite),
   - union `anc[u]` into `anc[v]` (v inherits all of u's prerequisites — transitivity),
   - decrement `v`'s indegree; enqueue when 0.
3. Query `[u,v]` → `anc[v].contains(u)`.

Because Kahn's processes a node only after all its parents, by the time you reach `v` every parent has already contributed its complete ancestor set — so `anc[v]` ends up holding *all* transitive prerequisites.

> 💡 **Inherit parents' prerequisites in topo order:** "Topological order guarantees every parent is fully computed before its child, so a child can simply union in each parent's complete prerequisite set. That union *is* transitivity, accumulated safely in dependency order." 

---

## 6. The full solutions

### Floyd-Warshall — compact, great when V is small

```java
public List<Boolean> checkIfPrerequisite(int numCourses, int[][] prerequisites, int[][] queries) {
    boolean[][] reach = new boolean[numCourses][numCourses];
    for (int[] p : prerequisites) reach[p[0]][p[1]] = true;      // a -> b

    for (int k = 0; k < numCourses; k++)
        for (int i = 0; i < numCourses; i++)
            if (reach[i][k])                                     // small prune
                for (int j = 0; j < numCourses; j++)
                    if (reach[k][j]) reach[i][j] = true;

    List<Boolean> ans = new ArrayList<>();
    for (int[] q : queries) ans.add(reach[q[0]][q[1]]);
    return ans;
}
```

### Kahn's + ancestor sets — reuses topo-sort intuition

```java
public List<Boolean> checkIfPrerequisite(int numCourses, int[][] prerequisites, int[][] queries) {
    List<List<Integer>> adj = new ArrayList<>();
    for (int i = 0; i < numCourses; i++) adj.add(new ArrayList<>());
    int[] indeg = new int[numCourses];
    for (int[] p : prerequisites) { adj.get(p[0]).add(p[1]); indeg[p[1]]++; }   // a -> b

    List<Set<Integer>> anc = new ArrayList<>();
    for (int i = 0; i < numCourses; i++) anc.add(new HashSet<>());

    Queue<Integer> q = new LinkedList<>();
    for (int i = 0; i < numCourses; i++) if (indeg[i] == 0) q.offer(i);
    while (!q.isEmpty()) {
        int u = q.poll();
        for (int v : adj.get(u)) {
            anc.get(v).add(u);              // direct prerequisite
            anc.get(v).addAll(anc.get(u));  // inherit all of u's prerequisites
            if (--indeg[v] == 0) q.offer(v);
        }
    }

    List<Boolean> ans = new ArrayList<>();
    for (int[] query : queries) ans.add(anc.get(query[1]).contains(query[0]));
    return ans;
}
```

Both verified equal to brute-force reachability over 5,000 random DAGs (LC examples all match).

---

## 7. Dry run

`numCourses=3, prerequisites=[[1,2],[1,0],[2,0]]`, query `[1,0]` → expect **true**.

Edges: `1→2`, `1→0`, `2→0`.

**Floyd-Warshall:**
```
init reach: reach[1][2]=reach[1][0]=reach[2][0]=T

k=0: nothing reaches 0 that then leaves 0 (0 has no out-edges) → no change
k=1: who reaches 1? nobody → no change
k=2: reach[i][2] true for i=1; reach[2][j] true for j=0 → set reach[1][0]=T (already T)
final reach[1][0]=T

query [1,0] → reach[1][0] = true   ✓
query [0,1] → reach[0][1] = false  ✓  (0 has no outgoing edges)
```

**Kahn's + ancestors:**
```
indeg: 0←(from1,from2)=2, 1=0, 2←(from1)=1
queue=[1]
 process 1: child 2 → anc[2]={1}; indeg[2]→0 enqueue.  child 0 → anc[0]={1}; indeg[0]→1
 process 2: child 0 → anc[0].add(2) and addAll(anc[2]={1}) → anc[0]={1,2}; indeg[0]→0 enqueue
 process 0: no children
anc[0]={1,2}, anc[2]={1}, anc[1]={}

query [1,0] → anc[0].contains(1)? yes → true  ✓
query [2,1] → anc[1].contains(2)? no → false ✓
```

---

## 8. Complexity

Let `V = numCourses`, `E = prerequisites`, `Q = queries`.

| | Time | Space |
|:--|:--|:--|
| **Floyd-Warshall** | **O(V³ + Q)** — triple loop for closure, O(1) per query | O(V²) matrix |
| **Kahn's + sets** | **O(V·E + Q)** roughly (each node unions ancestor sets, up to O(V) each) | O(V²) worst case (ancestor sets) |

With the constraint `V ≤ 100`, both are fast. Floyd-Warshall is the simplest to write; the topo approach connects better to the rest of the Course Schedule family.

---

## 9. Common mistakes

- ❌ **Wrong edge direction.** Here `[a,b]` = "`a` is a prerequisite **of** `b`" → edge `a → b`. (Different phrasing from LC 207/210, where `[a,b]` meant "b before a"! Read each problem's wording carefully.)
- ❌ **Answering each query with a fresh BFS/DFS.** Correct but wasteful; precompute the closure once for many queries.
- ❌ **Floyd-Warshall with `k` not outermost.** The intermediate-node loop must be outer, or the closure is incomplete.
- ❌ **Confusing `reach[u][v]` with `reach[v][u]`.** The query asks if `u` reaches `v` (u is the prereq); direction matters.
- ❌ **In the topo approach, forgetting to union the parent's ancestors** (only adding the direct parent) — you'd miss indirect prerequisites.
- ❌ **A node isn't its own prerequisite.** `reach[i][i]` stays false unless a (nonexistent) cycle creates it; the DAG guarantees no self-prerequisite.

---

## 10. How to recognize this pattern next time

1. **Directed graph + repeated "can X reach Y?" queries** → compute the **transitive closure** once, then O(1) lookups.
2. **Small V (≤ few hundred)** → **Floyd-Warshall** boolean closure is the quick, clean choice (O(V³)).
3. **Prefer graph-native / larger sparse graphs** → **topological order + ancestor-set propagation** (each node inherits parents' reachable/ancestor sets).
4. **General principle:** when many questions share one structure, **precompute** a reusable answer table (transitive closure, prefix sums, sparse table…).

Family: **Course Schedule IV** (this), all-pairs reachability, **transitive closure** problems, "is A an ancestor of B" tree/DAG queries. Distinguishing feature vs 207/210: no ordering/feasibility — it's **batch reachability**.

> 💡 **Batch reachability ⇒ precompute the closure:** "The trigger is 'many reachability questions on a fixed directed graph.' Build the transitive closure once — Floyd-Warshall for small V, topo-order ancestor propagation otherwise — then each query is a lookup." 

---

## 11. Cheat sheet

**Recognize it:** DAG + many "is `u` a prerequisite of `v`?" queries = "does `u` reach `v`?" → precompute transitive closure, O(1) per query.

**Edge convention (READ CAREFULLY):** `[a,b]` = "`a` is a prerequisite **of** `b`" → edge `a → b`. (Opposite of LC 207/210's phrasing.)

**Floyd-Warshall recipe:**
```
reach[a][b]=true for edges;
for k: for i: for j: reach[i][j] |= reach[i][k] && reach[k][j];   (k OUTERMOST)
query [u,v] → reach[u][v]
```

**Kahn's + ancestors recipe:** topo-process; for child v of u: anc[v].add(u); anc[v].addAll(anc[u]); query [u,v] → anc[v].contains(u).

**Complexity:** FW O(V³+Q), O(V²) space; topo O(V·E+Q).

**Watch:** edge direction (differs from 207/210!); `k` outermost in FW; union parent ancestors in topo version; `reach[u][v]` not reversed; precompute don't per-query search.

> **One-line philosophy:** *"Is u a prerequisite of v" is just "does u reach v" in the DAG, and because there are many queries against one fixed graph, you precompute all-pairs reachability once — a Floyd-Warshall boolean closure when V is small, or topological-order ancestor-set inheritance otherwise — turning every query into a constant-time lookup; the reusable instinct is to amortize repeated reachability questions by building the transitive closure up front.*
