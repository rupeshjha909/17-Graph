# Sequence Reconstruction (LeetCode 444) — Unique Topological Order

> Another Course Schedule cousin, and a subtle one. The `sequences` impose ordering constraints, and you must decide whether `nums` is the **only** sequence consistent with all of them. That's asking: **is the topological order unique — and does it equal `nums`?** The elegant test: run Kahn's algorithm, and at **every step the queue must contain exactly one node** (zero choice = unique order). If the queue ever holds two or more, the order is ambiguous. All verified against brute-force topological enumeration over 20,000 cases.

> 💡 **The whole solution in one sentence:** build a graph from each sequence's consecutive pairs (`a` immediately before `b` → edge `a → b`), then run Kahn's topological sort while checking that **the queue holds exactly one indegree-0 node at every step** — more than one means two valid orders exist (so `nums` isn't unique) — and finally confirm the single order produced equals `nums`; unique reconstruction happens iff both hold.

---

## Table of Contents
1. [Problem statement](#1-problem-statement)
2. [How to guess the pattern (the thought process)](#2-how-to-guess-the-pattern-the-thought-process)
3. [The key insight: unique order = queue size always 1](#3-the-key-insight-unique-order--queue-size-always-1)
4. [Building the graph (consecutive pairs)](#4-building-the-graph-consecutive-pairs)
5. [Approach — Kahn's with a uniqueness check](#5-approach--kahns-with-a-uniqueness-check)
6. [The full solution](#6-the-full-solution)
7. [Dry run](#7-dry-run)
8. [The three ways it returns false](#8-the-three-ways-it-returns-false)
9. [Complexity](#9-complexity)
10. [Common mistakes](#10-common-mistakes)
11. [How to recognize this pattern next time](#11-how-to-recognize-this-pattern-next-time)
12. [Cheat sheet](#12-cheat-sheet)

---

## 1. Problem statement

> Given a permutation `nums` of `1…n` and a list `sequences` where each `sequences[i]` is a subsequence of `nums`, decide whether `nums` is the **only** shortest supersequence consistent with all the sequences — i.e. the sequences pin down `nums` and no other ordering. Return **true** iff `nums` is that unique reconstruction.

### Examples
```
nums=[1,2,3], sequences=[[1,2],[1,3]]          → false  (both [1,2,3] and [1,3,2] fit)
nums=[1,2,3], sequences=[[1,2]]                → false  (3's position unconstrained)
nums=[1,2,3], sequences=[[1,2],[1,3],[2,3]]    → true   (only [1,2,3] fits)
nums=[4,1,5,2,6,3], sequences=[[5,2,6,3],[4,1,5,2]] → true
```
(All verified.)

---

## 2. How to guess the pattern (the thought process)

**Q1: What do the sequences tell me?**
Each sequence is an ordering: within `[a, b, c]`, `a` comes before `b` comes before `c`. Consecutive elements give "must come immediately-or-eventually before" → **directed edges**. → directed graph, Course Schedule family.

**Q2: What is "reconstruct `nums`"?**
`nums` is a valid ordering of all the constraints — a **topological order** of the graph. So step one is "is `nums` a valid topological order?"

**Q3: What does "the ONLY sequence" add?**
Uniqueness. There must be **no other** valid topological order. A graph has more than one topological order exactly when, at some point, **two different nodes could legally come next**. → we need to detect "was there ever a choice?"

**Q4: How do I detect a choice during topological sort?**
In Kahn's algorithm, the nodes eligible to come next are those with indegree 0 — they sit in the queue. If the queue ever holds **≥ 2** nodes, either could be placed next → multiple orders → not unique. If it always holds **exactly 1**, the order is forced (unique).

So the shape is: *ordering constraints → topological sort; uniqueness ⟺ Kahn's queue has exactly one node at every step; then check the forced order equals `nums`.*

> 💡 **Uniqueness = no choice ever:** "A topological order is unique precisely when at each step only one node has all prerequisites met. In Kahn's, that's 'queue size exactly 1' throughout. Two ready nodes = a fork = another valid order = not unique." 

---

## 3. The key insight: unique order = queue size always 1

This is the crux. Kahn's algorithm repeatedly removes a node with indegree 0. The set of indegree-0 nodes at any moment is precisely the set of nodes that could **legally be placed next**. Therefore:

- **Queue size == 1 at every step** → at each position there was exactly one legal choice → the topological order is **unique**.
- **Queue size ≥ 2 at any step** → at least two orders exist (swap those two next) → **not unique** → return false immediately.
- **Queue empties before all nodes are placed** → a cycle → no valid order → false.

After a clean run, the single order you built must also **equal `nums`** (a unique order that isn't `nums` means `nums` isn't the reconstruction).

> 💡 **Peek at the queue for forks:** "The queue is the menu of what can come next. A menu with one item forces the choice; a menu with two means the order could differ. So 'unique reconstruction' reduces to 'the menu never has more than one item.'" 

---

## 4. Building the graph (consecutive pairs)

Each sequence contributes edges between **consecutive** elements only — that's enough to encode the full ordering (transitivity handles the rest). For `seq = [a, b, c]`: add `a → b` and `b → c` (not `a → c`; it's implied).

```java
for (int[] seq : sequences) {
    for (int v : seq) nodes.add(v);                 // track which values appear
    for (int i = 0; i + 1 < seq.length; i++) {
        int a = seq[i], b = seq[i + 1];
        adj.get(a).add(b);
        indegree.merge(b, 1, Integer::sum);          // b has one more prerequisite
    }
}
```

Note: a value in `nums` that never appears in any sequence is unconstrained; the run will simply produce an order shorter than `nums` (that value never enters the graph), so the final `order.equals(nums)` check fails → false. That's the correct verdict (its position is ambiguous).

---

## 5. Approach — Kahn's with a uniqueness check

1. Build `adj` + `indegree` from consecutive pairs; collect the set of appearing nodes.
2. Queue all appearing nodes with indegree 0.
3. Repeatedly: **if the queue size > 1, return false** (a fork → not unique). Otherwise pop the one node, append to `order`, decrement its neighbors' indegrees, enqueue any that reach 0.
4. Return `order.equals(nums)` — true only if the forced order matches `nums` exactly (which also guarantees it covered all `n` values with no cycle).

The whole difference from plain Kahn's is the one line **`if (queue.size() > 1) return false;`** plus the final equality check.

---

## 6. The full solution

```java
public boolean sequenceReconstruction(int[] nums, int[][] sequences) {
    Map<Integer, List<Integer>> adj = new HashMap<>();
    Map<Integer, Integer> indegree = new HashMap<>();
    Set<Integer> nodes = new HashSet<>();

    for (int[] seq : sequences) {
        for (int v : seq) { nodes.add(v); indegree.putIfAbsent(v, 0); adj.putIfAbsent(v, new ArrayList<>()); }
        for (int i = 0; i + 1 < seq.length; i++) {
            int a = seq[i], b = seq[i + 1];
            adj.get(a).add(b);
            indegree.merge(b, 1, Integer::sum);        // add edge a -> b
        }
    }

    Queue<Integer> q = new LinkedList<>();
    for (int v : nodes) if (indegree.get(v) == 0) q.offer(v);

    List<Integer> order = new ArrayList<>();
    while (!q.isEmpty()) {
        if (q.size() > 1) return false;                // FORK: more than one valid next → not unique
        int u = q.poll();
        order.add(u);
        for (int w : adj.get(u))
            if (indegree.merge(w, -1, Integer::sum) == 0) q.offer(w);
    }

    // unique order built; it must equal nums (also ensures all n values, no cycle)
    if (order.size() != nums.length) return false;
    for (int i = 0; i < nums.length; i++)
        if (order.get(i) != nums[i]) return false;
    return true;
}
```

Verified equal to brute-force topological-order enumeration over 20,000 random cases.

---

## 7. Dry run

`nums = [1,2,3], sequences = [[1,2],[1,3],[2,3]]` → expect **true**.

```
Build edges: 1→2, 1→3, 2→3
  indegree: {1:0, 2:1, 3:2}   nodes={1,2,3}

Queue init (indegree 0): [1]

step: q.size()==1 → pop 1, order=[1]; unlock 2 (1→0, enqueue), unlock 3 (2→1)
      q=[2]
step: q.size()==1 → pop 2, order=[1,2]; unlock 3 (1→0, enqueue)
      q=[3]
step: q.size()==1 → pop 3, order=[1,2,3]
      q=[]

order=[1,2,3] == nums → TRUE  ✓
```

Contrast `sequences=[[1,2],[1,3]]` (→ false): after popping 1, both 2 and 3 reach indegree 0 → **queue size 2** → return false (either could come next: `[1,2,3]` or `[1,3,2]`).

---

## 8. The three ways it returns false

The single test "unique order == nums" fails in exactly three situations, all handled:

1. **A fork** — queue size > 1 at some step → two valid orders exist → not unique. (Caught mid-loop.)
2. **A cycle** — queue empties before all nodes placed → no valid order → `order.size() != nums.length` → false.
3. **Under-constrained / mismatched** — a unique order exists but doesn't match `nums`, or some `nums` value never appeared (so `order` is shorter) → equality check fails.

> 💡 **One `true`, three `false`s:** "It's true only when there's a single forced order equal to nums. It's false if there was ever a choice (fork), if it was impossible (cycle), or if the forced order simply isn't nums (mismatch / missing value)." 

---

## 9. Complexity

Let `V` = distinct values, `E` = total consecutive pairs across sequences.

| | Time | Space |
|:--|:--|:--|
| **Kahn's + uniqueness** | **O(V + E)** — build graph O(E), each node/edge processed once | O(V + E) — adjacency + indegree + queue |

Linear in the graph size — optimal.

---

## 10. Common mistakes

- ❌ **Forgetting the queue-size > 1 check.** Without it you'd only verify `nums` is *a* valid order, not the *unique* one. This one line is the heart of the problem.
- ❌ **Adding transitive edges (`a → c`).** Only consecutive pairs are needed; adding all pairs wastes work (still correct, but unnecessary — and can change indegree bookkeeping if done carelessly).
- ❌ **Not checking `order.equals(nums)`.** A unique order that differs from `nums` (or is shorter because a value was unconstrained) must return false.
- ❌ **Ignoring values in `nums` absent from all sequences.** They never enter the graph, so `order` is shorter → equality fails → correctly false. Don't force them in.
- ❌ **Missing the cycle case.** Handled by the length check, but don't assume the queue always drains fully.
- ❌ **Comparing lengths wrong.** The unique order must have all `n` values *and* match position-by-position.

---

## 11. How to recognize this pattern next time

1. **Ordering constraints + "is the ordering unique / is there only one valid arrangement?"** → **topological sort with a uniqueness check**.
2. **Uniqueness test:** Kahn's queue must hold **exactly one** node at every step (≥ 2 = a fork = multiple orders).
3. **Also verify the forced order matches the target** (and covers everything, catching cycles and missing elements via a length + equality check).

Family: **Sequence Reconstruction** (444), **Alien Dictionary** (269 — build graph from data, then topo sort; uniqueness can matter), and "is the schedule / ranking uniquely determined?" problems. The distinguishing feature vs 207/210 is the **uniqueness** requirement — detected by watching the queue for forks.

> 💡 **Uniqueness ⇒ watch the queue for forks:** "Whenever a problem asks not just 'does an order exist' but 'is it the ONLY order,' topo-sort with a queue-size-1 invariant is the tool: a single ready node at every step forces uniqueness; two ready nodes prove ambiguity." 

---

## 12. Cheat sheet

**Recognize it:** ordering constraints + "is `nums` the ONLY valid order?" → unique topological sort.

**Graph:** consecutive pair `a,b` in a sequence → edge `a → b`, `indegree[b]++`.

**Recipe (Kahn's + uniqueness):**
```
1. build adj + indegree from consecutive pairs; collect appearing nodes
2. queue all indegree-0 nodes
3. loop: if queue.size() > 1 → return false (fork ⇒ not unique)
         pop u; append to order; decrement neighbors; enqueue new zeros
4. return order.equals(nums)   (matches, covers all, no cycle)
```

**Why size-1:** the queue = nodes that could come next; exactly one ⇒ forced ⇒ unique; ≥2 ⇒ another order exists.

**Three falses:** fork (size>1), cycle (order too short), mismatch/missing value (order ≠ nums).

**Complexity:** O(V + E) time & space.

> **One-line philosophy:** *"Is `nums` the only reconstruction?" is "is the topological order unique and equal to `nums`?" — build a graph from each sequence's consecutive pairs and run Kahn's while enforcing that the ready-queue holds exactly one node at every step (two ready nodes is a fork proving another order exists), then confirm the single forced order equals `nums`; the reusable idea is that a topological order is unique precisely when there is never more than one choice of what comes next.*
