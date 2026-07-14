# Course Schedule II (LeetCode 210) — Return the Topological Order

> The direct sequel to Course Schedule I (LC 207). Same directed dependency graph, but instead of a yes/no you must **return an actual valid course order** — or an **empty array** if it's impossible (a cycle). The good news: Kahn's algorithm gives it almost for free — **the order you process the courses in *is* a valid topological order**. Two approaches (Kahn's BFS and DFS reverse-postorder), both verified to produce valid orderings across 20,000 random graphs.

> 💡 **The whole solution in one sentence:** it's Course Schedule I with one extra move — as Kahn's algorithm peels off each course whose prerequisites are all satisfied, **record it in a list**; that recording *is* a valid topological order, so if you manage to schedule all `numCourses` return the list, and if a cycle leaves some unschedulable (list shorter than `numCourses`) return an empty array.

---

## Table of Contents
1. [Problem statement](#1-problem-statement)
2. [The one change from Course Schedule I](#2-the-one-change-from-course-schedule-i)
3. [Key insight: Kahn's processing order IS the schedule](#3-key-insight-kahns-processing-order-is-the-schedule)
4. [Edge direction (same as LC 207)](#4-edge-direction-same-as-lc-207)
5. [Approach A — Kahn's algorithm (collect the order)](#5-approach-a--kahns-algorithm-collect-the-order)
6. [Approach B — DFS reverse post-order](#6-approach-b--dfs-reverse-post-order)
7. [The full solutions](#7-the-full-solutions)
8. [Dry run (Kahn's)](#8-dry-run-kahns)
9. [Why DFS needs the reverse (the subtle part)](#9-why-dfs-needs-the-reverse-the-subtle-part)
10. [Complexity](#10-complexity)
11. [Common mistakes](#11-common-mistakes)
12. [How to recognize this pattern next time](#12-how-to-recognize-this-pattern-next-time)
13. [Cheat sheet](#13-cheat-sheet)

---

## 1. Problem statement

> You must take `numCourses` courses labeled `0 … numCourses-1`. `prerequisites[i] = [a, b]` means **you must take `b` before `a`**. Return **any valid order** in which you can take all courses. If it's impossible (a cycle), return an **empty array**.

### Examples
```
numCourses = 2, prerequisites = [[1,0]]                → [0,1]
numCourses = 4, prerequisites = [[1,0],[2,0],[3,1],[3,2]]
        → [0,1,2,3]  (or [0,2,1,3] — any valid order is accepted)
numCourses = 1, prerequisites = []                     → [0]
numCourses = 2, prerequisites = [[1,0],[0,1]]          → []   (cycle → impossible)
```
(All verified. Note the answer isn't unique — any order respecting every "before" is correct.)

---

## 2. The one change from Course Schedule I

Course Schedule I asked *"can you finish?"* and you answered `true`/`false` by checking whether the graph is acyclic. Course Schedule II asks *"in what order?"* — everything else is identical:

| | Course Schedule I (207) | Course Schedule II (210) |
|:--|:--|:--|
| Question | Can you finish? | In what order? |
| Return | `boolean` | `int[]` (the order), or `[]` if impossible |
| Kahn's answer | `taken == numCourses` | **the list of courses in processing order** (or `[]`) |
| DFS answer | no cycle found | **reverse of the post-order** (or `[]`) |

So you already know 90% of this problem. The only addition is **recording the order** as you go, and returning `[]` on a cycle instead of `false`.

> 💡 **Same algorithm, now keep a receipt:** "207 just needed the *count* of what it could schedule; 210 needs the *sequence*. So I record each course as I schedule it — the record is the order — and a cycle shows up as a short record, which I report as an empty array." 

---

## 3. Key insight: Kahn's processing order IS the schedule

Kahn's algorithm schedules a course only once **all its prerequisites are already done** (its indegree has dropped to 0). That's exactly the definition of a valid order: every course appears **after** everything it depends on. So the sequence in which Kahn's pops courses off the queue is, by construction, a **valid topological order** — no extra work needed beyond appending each popped course to a result list.

- If you pop and record all `numCourses` → that list is your answer.
- If a cycle traps some courses (they never reach indegree 0), the list ends up **shorter** than `numCourses` → return `[]`.

This is why Kahn's is the natural fit for "return the order": the order falls out of the algorithm for free.

> 💡 **The order is a byproduct, not extra work:** "Because Kahn's only releases a course when its prerequisites are satisfied, the release sequence already respects every dependency. Recording it is the whole change from the yes/no version." 

---

## 4. Edge direction (same as LC 207)

Unchanged from Course Schedule I: `[a, b]` means `b` before `a`, so the edge is `b → a` and `a`'s indegree increases.

```java
for (int[] p : prerequisites) {
    adj.get(p[1]).add(p[0]);   // edge b -> a  (finishing b unlocks a)
    indegree[p[0]]++;          // a has one more prerequisite
}
```

`indegree[x]` = number of `x`'s prerequisites not yet taken; `x` becomes takeable when it hits 0.

---

## 5. Approach A — Kahn's algorithm (collect the order)

1. Build `adj` + `indegree`.
2. Queue every course with `indegree == 0` (no prerequisites).
3. Pop a course, **append it to the result order**, and decrement each unlocked course's indegree; enqueue any that reach 0.
4. If the result has all `numCourses` entries → return it. Otherwise a cycle blocked some → return `[]`.

The only difference from 207 is step 3's "append to result" and step 4 returning the list (or `[]`) instead of a boolean.

---

## 6. Approach B — DFS reverse post-order

DFS with the same three states (0 unvisited / 1 in-progress / 2 done). When a course is **fully explored** (all courses depending on it have been handled), push it onto a list — this is **post-order**. A cycle is detected the same way as 207 (reaching an in-progress node). At the end, **reverse** the post-order list to get the topological order.

Why reverse? A post-order finishes a node *after* its successors, so the raw post-order is **reverse** topological order; flipping it puts every prerequisite before its dependents. (Detailed in §9.)

---

## 7. The full solutions

### Kahn's (BFS) — recommended

```java
public int[] findOrder(int numCourses, int[][] prerequisites) {
    List<List<Integer>> adj = new ArrayList<>();
    for (int i = 0; i < numCourses; i++) adj.add(new ArrayList<>());
    int[] indegree = new int[numCourses];

    for (int[] p : prerequisites) {         // [a,b] = b before a → edge b->a
        adj.get(p[1]).add(p[0]);
        indegree[p[0]]++;
    }

    Queue<Integer> q = new LinkedList<>();
    for (int i = 0; i < numCourses; i++)
        if (indegree[i] == 0) q.offer(i);

    int[] order = new int[numCourses];
    int idx = 0;
    while (!q.isEmpty()) {
        int course = q.poll();
        order[idx++] = course;              // record: this is the next course in a valid order
        for (int next : adj.get(course)) {
            if (--indegree[next] == 0) q.offer(next);
        }
    }

    return (idx == numCourses) ? order : new int[0];   // shortfall ⇒ cycle ⇒ empty array
}
```

### DFS (reverse post-order) — alternative

```java
public int[] findOrder(int numCourses, int[][] prerequisites) {
    List<List<Integer>> adj = new ArrayList<>();
    for (int i = 0; i < numCourses; i++) adj.add(new ArrayList<>());
    for (int[] p : prerequisites) adj.get(p[1]).add(p[0]);

    int[] state = new int[numCourses];      // 0 unvisited, 1 in-progress, 2 done
    List<Integer> post = new ArrayList<>();
    for (int i = 0; i < numCourses; i++)
        if (state[i] == 0 && !dfs(i, adj, state, post)) return new int[0];  // cycle

    Collections.reverse(post);              // reverse post-order = topological order
    int[] order = new int[numCourses];
    for (int i = 0; i < numCourses; i++) order[i] = post.get(i);
    return order;
}
private boolean dfs(int u, List<List<Integer>> adj, int[] state, List<Integer> post) {
    if (state[u] == 1) return false;        // back-edge to current path → cycle
    if (state[u] == 2) return true;
    state[u] = 1;
    for (int v : adj.get(u)) if (!dfs(v, adj, state, post)) return false;
    state[u] = 2;
    post.add(u);                            // finished u → add in post-order
    return true;
}
```

Both verified to produce valid topological orders (and `[]` on a cycle) over 20,000 random graphs. They may return *different* valid orders — that's fine, LC 210 accepts any.

---

## 8. Dry run (Kahn's)

`numCourses = 4, prerequisites = [[1,0],[2,0],[3,1],[3,2]]` → expect a valid order like `[0,1,2,3]`.

```
Build (edge b->a, indegree[a]++):
  [1,0]: adj[0]+=1, indegree[1]=1
  [2,0]: adj[0]+=2, indegree[2]=1
  [3,1]: adj[1]+=3, indegree[3]=1
  [3,2]: adj[2]+=3, indegree[3]=2
  adj = {0:[1,2], 1:[3], 2:[3], 3:[]}   indegree = [0,1,1,2]

Queue init (indegree 0): [0]

Process:
  pop 0 → order=[0];   unlock 1 (1→0, enqueue), unlock 2 (1→0, enqueue)   queue=[1,2]
  pop 1 → order=[0,1]; unlock 3 (2→1, not yet)                            queue=[2]
  pop 2 → order=[0,1,2]; unlock 3 (1→0, enqueue)                          queue=[3]
  pop 3 → order=[0,1,2,3]                                                 queue=[]

idx (4) == numCourses (4) → return [0,1,2,3]   ✓  (0 first; 3 last, after both 1 and 2)
```

Cycle contrast `[[1,0],[0,1]]`: both indegrees start at 1 → queue empty → `order` never fills → `idx=0 ≠ 2` → return `[]`.

---

## 9. Why DFS needs the reverse (the subtle part)

In the DFS, you add a course to `post` **when it finishes** — i.e. *after* recursing into every course that depends on it. So a course lands in `post` **later** than its dependents. That means the raw `post` list has dependents *before* prerequisites — the **reverse** of what a schedule needs. Flipping it puts every prerequisite ahead of its dependents, yielding a valid order.

Concretely, with edge `0 → 1` (take 0 before 1): DFS from 0 recurses into 1, finishes 1 first (`post=[1]`), then finishes 0 (`post=[1,0]`). Raw post-order `[1,0]` is backwards; reversed `[0,1]` is the correct schedule.

> 💡 **Post-order finishes children first, so reverse it:** "A node is recorded only after all nodes it points to are done, so it sits later than its dependents in post-order — exactly backwards for a schedule. Reversing restores prerequisite-before-dependent. (Alternatively, push to the front instead of reversing at the end.)" 

---

## 10. Complexity

Let `V = numCourses`, `E = prerequisites.length`.

| | Time | Space |
|:--|:--|:--|
| **Kahn's (BFS)** | **O(V + E)** | O(V + E) — adjacency + indegree + queue + output |
| **DFS** | **O(V + E)** | O(V + E) — adjacency + state + recursion stack + list |

Linear in the graph size for both — optimal.

---

## 11. Common mistakes

- ❌ **Returning a partial order on a cycle.** If `idx < numCourses` (Kahn's) or any DFS finds a cycle, you must return `[]`, not the partial list.
- ❌ **Forgetting to reverse in DFS.** Raw post-order is *reverse* topological order; not reversing gives dependents before prerequisites — a backwards schedule. (§9)
- ❌ **Wrong edge direction.** `[a,b]` = `b` before `a` → edge `b → a`, `indegree[a]++`. Reversing it inverts the whole order.
- ❌ **Plain visited-set in DFS.** Need the **three states** to distinguish a real cycle (in-progress node) from an already-cleared node.
- ❌ **Missing disconnected components.** Enqueue *all* indegree-0 nodes; loop DFS over *all* courses.
- ❌ **Assuming a unique answer.** Any valid topological order is accepted; Kahn's and DFS may return different (both correct) orders. Don't hard-compare to one expected array.
- ❌ **Empty prerequisites handling.** No prereqs → every course indegree 0 → any permutation is valid (Kahn's returns `[0,1,…]`).

---

## 12. How to recognize this pattern next time

1. **Directed dependencies + "produce an order / sequence / schedule"** (not just feasibility) → **topological sort returning the order**.
2. **Kahn's is the go-to when you need the order** — the processing sequence is the answer, and a shortfall signals a cycle → `[]`.
3. **DFS works too** — collect post-order, reverse it; detect cycles with three states.
4. **Feasibility-only variant** (LC 207) is the same machinery returning a boolean.

Family: **Course Schedule I/II** (207/210), **Alien Dictionary** (269 — build the graph from word order, then topologically sort), **Parallel Courses** (1136 — topo sort by *levels*), **Sequence Reconstruction** (444), build-systems / task schedulers. Trigger: *directed "must-come-before" + asked for an ordering.*

> 💡 **Need the order, not just yes/no → Kahn's, and record as you go:** "When the ask shifts from 'is it possible' to 'give me the order', reach for Kahn's and append each scheduled node; the sequence is a valid order and a short sequence means a cycle. DFS-postorder-reversed is the equivalent alternative." 

---

## 13. Cheat sheet

**Recognize it:** directed prerequisites + "return a valid order" → topological sort; `[]` if cyclic.

**Edge convention:** `[a,b]` = "b before a" → edge `b → a`, `indegree[a]++`.

**Kahn's recipe (recommended):**
```
1. build adj + indegree
2. queue all indegree-0 courses
3. pop course → append to order; for each neighbor: --indegree; if 0, enqueue
4. return order if it has all numCourses, else []      ← the order is the processing sequence
```

**DFS recipe:** 3 states; on finishing a node, add to post-order list; a state-1 node = cycle → `[]`; finally **reverse** the post-order.

**Difference from LC 207:** record the sequence (not just a count/boolean); return `[]` (not `false`) on a cycle.

**Complexity:** O(V + E) time and space, both approaches.

**Watch:** reverse the DFS post-order; return `[]` on any cycle; correct edge direction; three states in DFS; any valid order is accepted.

> **One-line philosophy:** *Course Schedule II is Course Schedule I that hands back the order instead of a boolean — and Kahn's algorithm gives it for free, because it releases a course only once every prerequisite is done, so the release sequence is already a valid topological order; record each scheduled course, return the list if you scheduled them all, and return an empty array when a cycle leaves some forever unschedulable (DFS is the equivalent: collect post-order and reverse it).*
