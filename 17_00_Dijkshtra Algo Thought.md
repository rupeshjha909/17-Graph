# Dijkstra's Shortest Path — Complete Deep Dive

A line-by-line, in-depth explanation of Dijkstra's Single Source Shortest Path (SSSP) algorithm using a sorted set / priority queue. This guide covers the theory, the algorithm, every code design choice, and the Java conversion from the original C++ code.

---

## Table of Contents

1. [Problem Statement](#1-problem-statement)
2. [What Is Shortest Path?](#2-what-is-shortest-path)
3. [The Big Idea Behind Dijkstra's](#3-the-big-idea-behind-dijkstras)
4. [Why Greedy Works (Key Invariant)](#4-why-greedy-works-key-invariant)
5. [The Relaxation Operation](#5-the-relaxation-operation)
6. [Why a Sorted Set or Priority Queue?](#6-why-a-sorted-set-or-priority-queue)
7. [Set vs Priority Queue: Two Implementation Styles](#7-set-vs-priority-queue-two-implementation-styles)
8. [Walking Through the Code Section by Section](#8-walking-through-the-code-section-by-section)
9. [C++ to Java Conversion Notes](#9-c-to-java-conversion-notes)
10. [Visual Examples](#10-visual-examples)
11. [Detailed Dry Run with Diagrams](#11-detailed-dry-run-with-diagrams)
12. [Edge Cases](#12-edge-cases)
13. [Complexity Analysis](#13-complexity-analysis)
14. [Common Mistakes](#14-common-mistakes)
15. [Why Dijkstra's Fails on Negative Edges](#15-why-dijkstras-fails-on-negative-edges)
16. [Dijkstra vs Other Shortest Path Algorithms](#16-dijkstra-vs-other-shortest-path-algorithms)
17. [Real-World Applications](#17-real-world-applications)
18. [Variations and Follow-ups](#18-variations-and-follow-ups)
19. [Complete Java Code](#19-complete-java-code)
20. [Interview Tips](#20-interview-tips)

---

## 1. Problem Statement

> Given a **weighted graph** with **non-negative edge weights**, and a **source vertex** `src`, find the **shortest distance** from `src` to **every other vertex** in the graph.

### Input
- Graph G = (V, E) with weights w(u, v) ≥ 0.
- Source vertex `src`.

### Output
- `dist[v]` = shortest distance from `src` to `v` for every vertex `v`.
- If a vertex is unreachable, `dist[v] = ∞`.

### Example

```
Graph:
        1
   (1)─────(2)
    │      ╱│
   4│  1 ╱  │
    │   ╱   │
   (3)─┘    │
    │   2   │
    │       │7
   (3)────(4)

Edges (undirected, weighted):
  1-2: 1
  1-3: 4
  2-3: 1
  3-4: 2
  1-4: 7

From source 1:
  dist[1] = 0
  dist[2] = 1   (direct: 1→2)
  dist[3] = 2   (via 2: 1→2→3, weight 1+1=2)
  dist[4] = 4   (via 2,3: 1→2→3→4, weight 1+1+2=4)
```

### What "Shortest" Means

In weighted graphs, "shortest" means **minimum total edge weight**, not minimum number of edges.

```
Graph:
  A ──(10)── B
  A ──(1)── C ──(1)── D ──(1)── B

Path A→B (direct): 1 edge, weight 10.
Path A→C→D→B: 3 edges, weight 3.

The "shortest" path is A→C→D→B with weight 3, even though it has more edges.
```

---

## 2. What Is Shortest Path?

### Single Source Shortest Path (SSSP)
The problem this code solves: shortest distances from ONE source to ALL other vertices.

### Other Shortest Path Variants
- **Single Pair**: shortest path between two specific vertices.
- **All Pairs**: shortest path between every pair (Floyd-Warshall, n × Dijkstra).
- **Single Destination**: shortest path FROM every vertex TO one destination.

### Properties of Shortest Paths

#### Property 1: Optimal Substructure
> If the shortest path from A to C goes through B, then the sub-path A to B is also the shortest A→B path.

This enables dynamic programming approaches.

#### Property 2: Triangle Inequality
> dist(A, C) ≤ dist(A, B) + dist(B, C)

The shortest path can't be longer than going through any intermediate vertex.

#### Property 3: No Negative Cycles
- If a cycle has total negative weight, shortest paths through it become **-∞**.
- Dijkstra's assumes no negative cycles AND no negative edges at all.

---

## 3. The Big Idea Behind Dijkstra's

### The Greedy Strategy

> "Maintain a set of vertices with **known shortest distances** (settled). Repeatedly pick the unsettled vertex with the **smallest known distance**, settle it, and update its neighbors."

### Two Sets of Vertices

At any moment, vertices are partitioned into:
1. **Settled (S)**: vertices whose shortest distance is finalized.
2. **Unsettled (U)**: vertices whose distance might still improve.

We start with S = {} and U = all vertices.

### The Greedy Choice

> "Among unsettled vertices, pick the one with the **smallest tentative distance**."

This vertex's tentative distance is **guaranteed to be its final shortest distance** (proof in Section 4).

### Iterative Process

```
1. Initialize dist[src] = 0, dist[other] = ∞.
2. While there are unsettled vertices:
   a. Pick the unsettled vertex u with smallest dist[u].
   b. Settle u (move from U to S).
   c. For each neighbor v of u:
        If dist[u] + w(u, v) < dist[v]:
          dist[v] = dist[u] + w(u, v)
3. Return dist[].
```

This is **Dijkstra's algorithm** in essence.

### Why "Greedy"?

At each step, we make the locally optimal choice (settle the closest unsettled vertex). Crucially, **this local greedy choice leads to a globally optimal solution** — but only when edge weights are non-negative.

---

## 4. Why Greedy Works (Key Invariant)

### The Invariant

> When we extract a vertex u from the unsettled set, **dist[u] is its true shortest distance from src**.

### Why?

Suppose we extract u with current `dist[u] = d`. We claim no shorter path to u exists.

**Proof by contradiction**: suppose there's a shorter path P from src to u with weight d' < d.

This path P must pass through some vertex (possibly multiple) before reaching u. Let v be the **first vertex on P that's still unsettled** (note: src is settled, so v exists later).

Since v is on a shortest path to u, dist[v] ≤ d' (because the path from src to v is shorter than from src to u, by P being prefix-shorter).

But:
- d' < d (assumed)
- dist[v] ≤ d'

So dist[v] < d = dist[u].

But we just claimed u has the smallest dist among unsettled vertices! Contradiction.

Therefore, no shorter path exists. dist[u] is correct.

### Why This Fails for Negative Edges

If edges can be negative, the "first unsettled vertex" v might have a shorter path through some not-yet-considered (because it has higher current `dist`) settled vertex — because adding a negative weight could decrease total. The invariant breaks.

This is why Dijkstra's needs **non-negative weights**.

---

## 5. The Relaxation Operation

### What Is Relaxation?

> "If I can reach `v` via `u` cheaper than my current best, update `dist[v]`."

```java
if (dist[u] + weight(u, v) < dist[v]) {
    dist[v] = dist[u] + weight(u, v);
}
```

This is THE core operation of all shortest path algorithms.

### Visual

```
Current state:
  dist[u] = 5
  dist[v] = 12
  edge(u, v) = 3

Question: can we reach v better via u?
  5 + 3 = 8 < 12 ✓ YES

Update: dist[v] = 8.
```

### Why "Relaxation"?

The name comes from "relaxing" a constraint. The constraint is "v can only be reached this far cheaply"; we relax it to "v can be reached cheaper".

### Where Does It Happen in Dijkstra's?

```python
for each neighbor v of u:
    if dist[u] + weight(u, v) < dist[v]:
        dist[v] = dist[u] + weight(u, v)  # RELAXATION
        # update PQ / set
```

Every time we settle a vertex, we relax all its outgoing edges.

---

## 6. Why a Sorted Set or Priority Queue?

### The Core Need

At each iteration, we need: "the unsettled vertex with smallest dist[v]".

#### Naive: scan all unsettled vertices each iteration.
- Per iteration: O(V) to find minimum.
- For V iterations: **O(V²)** total.

This works! Called "Dijkstra with array".

#### Better: use a min-heap or sorted set.
- Per iteration: O(log V) to find minimum.
- For V iterations: O(V log V).
- Plus O(E log V) for edge relaxations.
- **Total: O((V + E) log V)**.

For sparse graphs (E ≈ V), this is much faster than O(V²).

### Sorted Set vs Priority Queue

Both serve the same purpose. The C++ code uses `std::set` (a sorted set). Java has both:
- `TreeSet` (sorted set, equivalent to std::set).
- `PriorityQueue` (binary heap, more idiomatic in Java).

We'll cover both.

---

## 7. Set vs Priority Queue: Two Implementation Styles

### Style A: Sorted Set (Eager Removal)

The C++ code uses this style.

```
Maintain a set of (dist, vertex) pairs, sorted by dist.

When relaxing edge to vertex v:
  - Remove old (oldDist, v) from set.
  - Insert new (newDist, v).
  
When extracting:
  - Pop smallest (dist, v).
  - Process.

Set always contains AT MOST ONE entry per vertex.
```

**Pros**: clean state, no stale entries.
**Cons**: removal is O(log V) — requires knowing the old distance.

### Style B: Priority Queue (Lazy Deletion)

More common in competitive programming.

```
Push (dist, vertex) to PQ.

When relaxing edge to vertex v:
  - Don't bother removing old entry.
  - Just push new (newDist, v).

When extracting:
  - Pop smallest (dist, v).
  - If dist > current_dist[v]: STALE. Skip.
  - Otherwise: process.

PQ can contain MULTIPLE entries per vertex (most stale).
```

**Pros**: simpler — just push and pop.
**Cons**: PQ can grow up to O(E) entries.

### Which to Use?

For Java:
- **PriorityQueue (lazy)** is most idiomatic and competitive.
- **TreeSet (eager)** is closer to the C++ original.

For interviews: either is fine; PQ is more common.

### Time Complexity

| Style | Time |
|-------|------|
| Sorted Set / PQ with decrease-key | O((V + E) log V) |
| PQ with lazy deletion | O(E log E) ≈ O(E log V) |
| Array (no heap) | O(V²) |

For sparse graphs: PQ wins. For dense graphs: array wins.

---

## 8. Walking Through the Code Section by Section

Let me walk through every part of the Java code.

### Section A: Class & Adjacency List

```java
public class DijkstraGraph<T> {
    private Map<T, List<Object[]>> adj;
    
    public DijkstraGraph() {
        this.adj = new HashMap<>();
    }
}
```

#### Why Generic `<T>`?

The C++ code uses `template <typename T>` for flexibility — vertices can be int, string, etc.

Java's equivalent is `<T>` generics. So you can do:
- `DijkstraGraph<Integer>` for int vertices.
- `DijkstraGraph<String>` for string vertices (e.g., city names).

#### Why `Map<T, List<Object[]>>`?

For each vertex, store a list of edges. Each edge is `Object[]{neighbor, weight}`.

**Why `Object[]` instead of a typed pair?**
- Java lacks `pair<T, int>` natively.
- Options:
  - `Object[]` (used here for simplicity — but type-unsafe).
  - Custom `Edge` class (cleaner, type-safe).
  - `Map.Entry<T, Integer>` (verbose).

For interview brevity, `Object[]` works. For production, a custom class is cleaner.

### Section B: addEdge

```java
public void addEdge(T u, T v, int dist, boolean bidir) {
    adj.computeIfAbsent(u, k -> new ArrayList<>()).add(new Object[]{v, dist});
    if (bidir) {
        adj.computeIfAbsent(v, k -> new ArrayList<>()).add(new Object[]{u, dist});
    }
}
```

#### `computeIfAbsent`

This is Java's elegant way to say: "If the key doesn't exist, create the value; then return it."

Equivalent to:
```java
if (!adj.containsKey(u)) {
    adj.put(u, new ArrayList<>());
}
adj.get(u).add(...);
```

`computeIfAbsent` is more concise.

#### Bidirectional Default

C++:
```cpp
void addEdge(T u, T v, int dist, bool bidir = true)
```

C++ supports default arguments. Java doesn't, so we need:
```java
public void addEdge(T u, T v, int dist, boolean bidir) { ... }
public void addEdge(T u, T v, int dist) { addEdge(u, v, dist, true); }
```

Method overloading mimics the default behavior.

### Section C: printAdj

```java
public void printAdj() {
    for (Map.Entry<T, List<Object[]>> entry : adj.entrySet()) {
        T node = entry.getKey();
        System.out.print(node + " --> ");
        for (Object[] nbr : entry.getValue()) {
            T city = (T) nbr[0];
            int dist = (int) nbr[1];
            System.out.print("(" + city + "," + dist + ")");
        }
        System.out.println();
    }
}
```

Standard iteration over the map. Note the casting from `Object[]` to `T` and `int`.

### Section D: dijkstraSSSP (TreeSet Version)

```java
public void dijkstraSSSP(T src) {
    // Step 1: Initialize all distances to infinity
    Map<T, Integer> dist = new HashMap<>();
    for (T node : adj.keySet()) {
        dist.put(node, Integer.MAX_VALUE);
    }
    
    // Step 2: TreeSet ordered by (distance, vertex)
    TreeSet<Object[]> set = new TreeSet<>((a, b) -> {
        int distCompare = Integer.compare((int) a[0], (int) b[0]);
        if (distCompare != 0) return distCompare;
        return Integer.compare(a[1].hashCode(), b[1].hashCode());
    });
    
    dist.put(src, 0);
    set.add(new Object[]{0, src});
    
    while (!set.isEmpty()) {
        Object[] first = set.first();
        set.remove(first);
        
        int nodeDist = (int) first[0];
        T node = (T) first[1];
        
        for (Object[] childPair : adj.getOrDefault(node, new ArrayList<>())) {
            T child = (T) childPair[0];
            int childDist = (int) childPair[1];
            
            if (nodeDist + childDist < dist.get(child)) {
                // Remove old entry from set
                Object[] oldEntry = new Object[]{dist.get(child), child};
                set.remove(oldEntry);
                
                // Update distance
                dist.put(child, nodeDist + childDist);
                
                // Insert new entry
                set.add(new Object[]{dist.get(child), child});
            }
        }
    }
    
    for (Map.Entry<T, Integer> d : dist.entrySet()) {
        System.out.println(d.getKey() + " is located at distance of " + d.getValue());
    }
}
```

#### Why a Custom Comparator for TreeSet?

The C++ `std::set<pair<int, T>>` uses pair's natural ordering: compare first elements, break ties on second.

Java's `TreeSet<Object[]>` doesn't have natural ordering for `Object[]`. We must provide a comparator.

The comparator: compare distances first, break ties by hashCode (so distinct vertices with same distance don't get treated as equal).

#### Why `set.first()` Then `set.remove(first)`?

The C++ uses `s.begin()` to get the smallest, then `s.erase(s.begin())` to remove.

Java's TreeSet uses `set.first()` for the smallest, then `set.remove(first)`.

Alternatively: `set.pollFirst()` does both in one call. Cleaner:
```java
Object[] first = set.pollFirst();
```

#### Eager Removal Pattern

When we relax (improve) an edge:
```java
set.remove(oldEntry);  // remove old (dist, vertex)
dist.put(child, newDist);
set.add(new Object[]{newDist, child});  // insert new
```

This keeps the set "clean" with exactly one entry per vertex.

The trick: `set.remove(oldEntry)` finds and removes the entry by value (using the comparator).

⚠️ **Important**: the `Object[]` arrays must compare equal via the comparator. The current implementation might have issues if `Object[]` is compared by reference. Let me check...

Actually, `TreeSet.remove(obj)` uses the comparator's `compare(obj, existingElement) == 0` to find the matching element. So as long as the comparator returns 0 for "same vertex, same distance", removal works.

### Section E: dijkstraSSSPwithPQ (PriorityQueue Version)

```java
public Map<T, Integer> dijkstraSSSPwithPQ(T src) {
    Map<T, Integer> dist = new HashMap<>();
    for (T node : adj.keySet()) {
        dist.put(node, Integer.MAX_VALUE);
    }
    dist.put(src, 0);
    
    PriorityQueue<Object[]> pq = new PriorityQueue<>((a, b) -> 
        Integer.compare((int) a[0], (int) b[0])
    );
    pq.offer(new Object[]{0, src});
    
    while (!pq.isEmpty()) {
        Object[] curr = pq.poll();
        int nodeDist = (int) curr[0];
        T node = (T) curr[1];
        
        // Lazy deletion
        if (nodeDist > dist.get(node)) continue;
        
        for (Object[] childPair : adj.getOrDefault(node, new ArrayList<>())) {
            T child = (T) childPair[0];
            int childDist = (int) childPair[1];
            
            int newDist = nodeDist + childDist;
            if (newDist < dist.get(child)) {
                dist.put(child, newDist);
                pq.offer(new Object[]{newDist, child});
            }
        }
    }
    
    return dist;
}
```

#### Differences from TreeSet Version

**Lazy deletion vs Eager removal**:
- TreeSet: explicitly removes old entries.
- PQ: just pushes new entries; stale ones skipped on extraction.

```java
if (nodeDist > dist.get(node)) continue;
```

This is the lazy deletion check: if the popped distance is greater than the current known distance, this entry is stale.

#### Why It's Simpler

- No need to know the old distance to remove (the C++ code does this).
- Just push and pop.
- The "skip stale" check is one line.

This pattern is so common in Java/Python competitive programming that you should make it your default.

---

## 9. C++ to Java Conversion Notes

### Differences and Translations

#### 1. Template → Generic

**C++**:
```cpp
template <typename T>
class graph { ... };
```

**Java**:
```java
public class DijkstraGraph<T> { ... }
```

Same idea, slightly different syntax.

#### 2. unordered_map → HashMap

**C++**:
```cpp
unordered_map<T, list<pair<T, int>>> m;
```

**Java**:
```java
Map<T, List<Object[]>> adj;
```

Different collection types but equivalent.

#### 3. pair → Object[] or Custom Class

**C++**:
```cpp
pair<T, int>  // {first, second}
make_pair(v, dist)
```

**Java options**:
- `Object[]{v, dist}` — simple but type-unsafe.
- Custom `Edge` class — safer.
- `Map.Entry<T, Integer>` — verbose.

We chose `Object[]` for brevity.

#### 4. set → TreeSet (or PriorityQueue)

**C++**:
```cpp
set<pair<int, T>> s;
s.insert(make_pair(0, src));
s.erase(s.begin());
```

**Java**:
```java
TreeSet<Object[]> set = new TreeSet<>(comparator);
set.add(new Object[]{0, src});
set.remove(set.first());
// OR cleaner:
set.pollFirst();
```

C++'s `set` is a red-black tree, sorted. `TreeSet` is Java's equivalent.

#### 5. Default Arguments

**C++**:
```cpp
void addEdge(T u, T v, int dist, bool bidir = true)
```

**Java** (uses overloading):
```java
public void addEdge(T u, T v, int dist, boolean bidir) { ... }
public void addEdge(T u, T v, int dist) {
    addEdge(u, v, dist, true);
}
```

Java doesn't support default arguments.

#### 6. INT_MAX

**C++**:
```cpp
dist[p.first] = INT_MAX;
```

**Java**:
```java
dist.put(node, Integer.MAX_VALUE);
```

Equivalent.

⚠️ **Overflow**: when computing `nodeDist + childDist`, if `nodeDist == Integer.MAX_VALUE`, addition overflows to a negative number. Defense:
```java
if (dist.get(node) == Integer.MAX_VALUE) continue;
```

In our code, we start from src with dist 0 and only process reachable nodes, so this isn't an issue, but worth noting.

#### 7. Range-Based For

**C++**:
```cpp
for (auto p : m) {
    auto node = p.first;
    // ...
}
```

**Java**:
```java
for (Map.Entry<T, List<Object[]>> entry : adj.entrySet()) {
    T node = entry.getKey();
    // ...
}
```

#### 8. begin() / erase

**C++**:
```cpp
auto p = *(s.begin());
s.erase(s.begin());
```

**Java**:
```java
Object[] first = set.first();
set.remove(first);
// Or:
Object[] first = set.pollFirst();
```

#### 9. Map Access

**C++**:
```cpp
m[node]
```

**Java**:
```java
adj.get(node)
// Or with default:
adj.getOrDefault(node, new ArrayList<>())
```

Java doesn't auto-create entries on access like C++ does.

#### 10. Printing

**C++**:
```cpp
cout << node << "-->";
```

**Java**:
```java
System.out.print(node + " --> ");
```

---

## 10. Visual Examples

### Example 1: Original C++ Example

```
Graph:
        1
   (1)─────(2)
    │      ╱│
   4│  1 ╱  │
    │   ╱   │
   (3)─┘    │
    │   2   │
    │       │7
   (4)──────┘

Edges: 1-2(1), 1-3(4), 2-3(1), 3-4(2), 1-4(7)

From source 1:
  Direct paths:
    1 → 2: weight 1
    1 → 3: weight 4
    1 → 4: weight 7
  
  Better paths via 2:
    1 → 2 → 3: weight 2 (better than 4!)
    1 → 2 → 3 → 4: weight 4 (better than 7!)

Final distances:
  dist[1] = 0
  dist[2] = 1
  dist[3] = 2
  dist[4] = 4
```

### Example 2: Wikipedia Classic

```
Graph (6 vertices):

         7        9
    (1)─────(2)─────(3)
     │       │       │
     │14    10│      │11
     │       │       │
    (6)─────(5)─────(4)
        9       6

Plus edge: 3-6 (weight 2)

Edges:
  1-2: 7, 1-3: 9, 1-6: 14
  2-3: 10, 2-4: 15
  3-4: 11, 3-6: 2
  4-5: 6
  5-6: 9

From source 1:
  dist[1] = 0
  dist[2] = 7
  dist[3] = 9
  dist[6] = 11  (via 3: 9 + 2 = 11, better than direct 14)
  dist[4] = 20  (via 3: 9 + 11 = 20)
  dist[5] = 20  (via 6: 11 + 9 = 20)
```

### Example 3: Negative Edge (FAILS for Dijkstra)

```
        2
   A ────── B
   │       │
  5│      -4
   │       │
   C ──────┘
       3

Dijkstra from A:
  Settles A (dist 0).
  Settles B (dist 2)  ← but wait!
  
  Better path: A → C → B = 5 + (-4) = 1.
  But Dijkstra already settled B and won't reconsider!

Result: dist[B] = 2 (WRONG — actual shortest is 1).

This is why Dijkstra fails for negative edges.
```

---

## 11. Detailed Dry Run with Diagrams

Let's trace through the original C++ example.

```
Graph:
  Edges (undirected):
    1-2: weight 1
    1-3: weight 4
    2-3: weight 1
    3-4: weight 2
    1-4: weight 7

Adjacency list:
  1: [(2,1), (3,4), (4,7)]
  2: [(1,1), (3,1)]
  3: [(1,4), (2,1), (4,2)]
  4: [(1,7), (3,2)]
```

### Initial State

```
dist = {1: ∞, 2: ∞, 3: ∞, 4: ∞}
set = {}

Then: dist[1] = 0; set = {(0, 1)}
```

### Iteration 1: Extract (0, 1)

```
Extract (0, 1). 
  set = {}
  node = 1, nodeDist = 0

Process neighbors of 1:
  - Neighbor 2, weight 1:
      newDist = 0 + 1 = 1
      1 < ∞ → relax!
      dist[2] = 1
      set.insert((1, 2))
  - Neighbor 3, weight 4:
      newDist = 0 + 4 = 4
      4 < ∞ → relax!
      dist[3] = 4
      set.insert((4, 3))
  - Neighbor 4, weight 7:
      newDist = 0 + 7 = 7
      7 < ∞ → relax!
      dist[4] = 7
      set.insert((7, 4))

State after iteration 1:
  dist = {1: 0, 2: 1, 3: 4, 4: 7}
  set = {(1, 2), (4, 3), (7, 4)}
```

### Iteration 2: Extract (1, 2)

```
Extract (1, 2).
  set = {(4, 3), (7, 4)}
  node = 2, nodeDist = 1

Process neighbors of 2:
  - Neighbor 1, weight 1:
      newDist = 1 + 1 = 2
      2 < 0? No (dist[1] = 0). Don't relax.
  - Neighbor 3, weight 1:
      newDist = 1 + 1 = 2
      2 < 4 → relax!
      Remove old (4, 3) from set.
      dist[3] = 2
      set.insert((2, 3))

State after iteration 2:
  dist = {1: 0, 2: 1, 3: 2, 4: 7}
  set = {(2, 3), (7, 4)}
```

### Iteration 3: Extract (2, 3)

```
Extract (2, 3).
  set = {(7, 4)}
  node = 3, nodeDist = 2

Process neighbors of 3:
  - Neighbor 1, weight 4:
      newDist = 2 + 4 = 6
      6 < 0? No. Don't relax.
  - Neighbor 2, weight 1:
      newDist = 2 + 1 = 3
      3 < 1? No. Don't relax.
  - Neighbor 4, weight 2:
      newDist = 2 + 2 = 4
      4 < 7 → relax!
      Remove old (7, 4) from set.
      dist[4] = 4
      set.insert((4, 4))

State after iteration 3:
  dist = {1: 0, 2: 1, 3: 2, 4: 4}
  set = {(4, 4)}
```

### Iteration 4: Extract (4, 4)

```
Extract (4, 4).
  set = {}
  node = 4, nodeDist = 4

Process neighbors of 4:
  - Neighbor 1, weight 7:
      newDist = 4 + 7 = 11
      11 < 0? No. Don't relax.
  - Neighbor 3, weight 2:
      newDist = 4 + 2 = 6
      6 < 2? No. Don't relax.

State after iteration 4:
  dist = {1: 0, 2: 1, 3: 2, 4: 4}
  set = {}
```

### Termination

```
Set is empty. Algorithm terminates.

Final shortest distances from source 1:
  1 → 0
  2 → 1
  3 → 2
  4 → 4

Verified! ✓
```

---

## 12. Edge Cases

### 1. Single Vertex (No Edges)

```
n = 1, edges = []
Initial: dist[0] = 0, set = {(0, 0)}.
Extract (0, 0). No neighbors. Set empty.
Result: dist[0] = 0. ✓
```

### 2. Disconnected Graph

```
Vertices: 1, 2, 3, 4
Edges: (1, 2, 5)
Source: 1.

dist[1] = 0, dist[2] = 5, dist[3] = ∞, dist[4] = ∞.

Vertices 3 and 4 are unreachable → distance ∞ (Integer.MAX_VALUE in Java).
```

### 3. Multiple Edges Between Same Vertices

```
Edges: (1, 2, 10), (1, 2, 3)
Adjacency list of 1 includes both (2, 10) and (2, 3).

Dijkstra processes both relaxations. dist[2] becomes 3 (the smaller).
Result: dist[2] = 3.
```

Dijkstra automatically picks the cheapest of parallel edges.

### 4. Self-Loop

```
Edges: (1, 1, 5)
Adjacency list of 1 includes (1, 5).

When processing 1, relax (1, 1, 5): newDist = 0 + 5 = 5. 
5 < 0? No (dist[1] is 0). Don't relax.

Self-loops never decrease distance (would require negative weight).
```

### 5. Negative Edge Weight (Failure Case)

```
Edges: (1, 2, 5), (2, 3, -10)
Source: 1.

Dijkstra:
  Extract (0, 1). Relax 2: dist[2] = 5.
  Extract (5, 2). Relax 3: dist[3] = -5.
  Extract (-5, 3).

But wait — dist[3] = -5 is fine here actually.

The real problem: when there's a NEGATIVE-WEIGHT EDGE pointing BACK to settled nodes.

Example:
  1 → 2: 5
  2 → 3: 1
  3 → 2: -10
  
  Dijkstra from 1:
    Settle 1 (dist 0).
    Relax 2: dist[2] = 5. Settle.
    Relax 3: dist[3] = 6. Settle.
    Relax 2 (via 3, -10): newDist = 6 + (-10) = -4 < 5!
    BUT 2 is already settled — Dijkstra won't reconsider.
  
  Result: dist[2] = 5 (WRONG — actual shortest is -4).

⚠️ Dijkstra REQUIRES non-negative edge weights.
```

### 6. Source Not in Adjacency Map

```
g.addEdge(2, 3, 5);
g.dijkstraSSSP(1);  // 1 doesn't appear in any edge!

If adj has no entry for 1, dist[1] won't be initialized to 0.
Need to handle: if source isn't in adj, initialize dist[src] = 0 specially.
```

In the code: `dist.put(src, 0)` handles this (overwrites any earlier value).

### 7. Very Large Graph

```
V = 10^5, E = 10^6
Total operations: O((V + E) log V) ≈ 10^7.
Easily fits in 1-2 seconds.

Memory: O(V + E) ≈ 10^6 entries. Manageable.
```

---

## 13. Complexity Analysis

### Time Complexity

#### Operations
- Each vertex extracted once: V extractions.
- Each edge relaxed at most once (in directed) or twice (undirected): E relaxations.

#### Per-Operation Cost
- Extract minimum from set/PQ: O(log V).
- Relaxation (with set update): O(log V).

#### Total
**O((V + E) log V)** with binary heap (PriorityQueue) or sorted set (TreeSet).

#### Best Case
If all vertices are far from src (no shorter paths found), no updates → O(V log V + E).

#### Worst Case
Many updates → O((V + E) log V).

### With Different Data Structures

| Data Structure | Time |
|----------------|------|
| Array (linear scan) | O(V²) |
| Binary heap | O((V + E) log V) |
| Fibonacci heap | O(E + V log V) |

Binary heap is the standard choice. Fibonacci heap is theoretically better but rarely used in practice due to constant factors.

### Space Complexity

- Adjacency list: O(V + E).
- Distance map: O(V).
- Set/PQ: O(V) for sorted set (eager), O(E) for PQ (lazy).

**Total: O(V + E)**.

---

## 14. Common Mistakes

### Mistake 1: Using Dijkstra with Negative Edges

```java
// WRONG: this fails for negative weights!
g.addEdge(1, 2, -5);
g.dijkstraSSSP(1);

// FIX: use Bellman-Ford for negative weights.
```

### Mistake 2: Integer Overflow

```java
// If dist[node] == Integer.MAX_VALUE, adding any weight overflows
int newDist = dist.get(node) + edgeWeight;  // overflow → negative!

// FIX: check before adding
if (dist.get(node) == Integer.MAX_VALUE) continue;
int newDist = dist.get(node) + edgeWeight;
```

In our code, we start from src (dist 0) and only process reachable nodes, so this isn't an issue.

### Mistake 3: Not Skipping Stale Entries (PQ Version)

```java
// WRONG: process every popped entry
Object[] curr = pq.poll();
// ... process directly

// RIGHT: skip stale
if ((int) curr[0] > dist.get((T) curr[1])) continue;
```

Without the skip, you'd reprocess vertices many times.

### Mistake 4: Eager Removal When Old Distance Doesn't Match

```java
// WRONG: try to remove with wrong distance
set.remove(new Object[]{wrongDist, vertex});
// → doesn't find anything, no-op

// RIGHT: pass the CURRENT distance (before update)
set.remove(new Object[]{dist.get(vertex), vertex});
dist.put(vertex, newDist);
set.add(new Object[]{newDist, vertex});
```

The removal must use the OLD distance to find the entry.

### Mistake 5: Marking "Settled" and Skipping Re-Processing

The naive Dijkstra has a `visited` array. Mistake: only updating dist if neighbor not visited.

```java
// WRONG:
if (!visited[neighbor]) {
    // relax
}

// RIGHT: always try to relax. The "skip stale" handles correctness.
```

Actually, "visited" can work IF you only mark visited on extract (not on push). But the cleaner pattern is lazy deletion.

### Mistake 6: Wrong Source Initialization

```java
// WRONG: forget to set dist[src] = 0
dist.put(src, Integer.MAX_VALUE);  // initialized to infinity like everything else
pq.offer(new Object[]{0, src});

// Without dist[src] = 0, the first iteration would compute weird things.

// RIGHT:
dist.put(src, 0);
pq.offer(new Object[]{0, src});
```

### Mistake 7: Comparator Returning Wrong Comparison

```java
// WRONG: max-heap behavior
PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> b[0] - a[0]);

// RIGHT: min-heap
PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0]));
```

For Dijkstra, you MUST extract the minimum, so it's a min-heap.

### Mistake 8: Forgetting Undirected Edges

```java
// WRONG: only one direction
adj.get(u).add(new Object[]{v, w});
// adj.get(v) doesn't know about this edge!

// RIGHT: both ways for undirected
adj.get(u).add(new Object[]{v, w});
adj.get(v).add(new Object[]{u, w});
```

### Mistake 9: Initializing Distance Map Incorrectly

```java
// WRONG: only initialize vertices in adj
for (T node : adj.keySet()) {
    dist.put(node, Integer.MAX_VALUE);
}
// What about an isolated vertex not in any edge?

// RIGHT: initialize all V vertices, or handle "not in map" with Integer.MAX_VALUE default.
```

For the given problem, all vertices appear in edges, so this is fine.

---

## 15. Why Dijkstra's Fails on Negative Edges

### The Concrete Failure

```
Graph (directed):
  A → B: 1
  A → C: 100
  C → B: -200
  
From A, what's the shortest path to B?
  A → B direct: 1
  A → C → B: 100 + (-200) = -100 ← SHORTER!

Dijkstra from A:
  Settle A (dist 0).
  Extract B (dist 1). Settle B.
  Extract C (dist 100). Try to relax B: newDist = 100 + (-200) = -100. Update!
  But B is already settled — Dijkstra won't reprocess.

Final: dist[B] = 1 (WRONG).
Actual shortest: dist[B] = -100.
```

### Why This Happens

Dijkstra's greedy invariant: "When we extract a vertex, its current distance is final."

This assumes you can't get to that vertex any cheaper later. With negative edges, you CAN get cheaper via a longer detour.

### The Math

Dijkstra's correctness requires:
> For any vertex u settled at time t, no future relaxation can improve dist[u].

This holds when all edge weights are ≥ 0:
- Once we extract u with smallest current dist, any other path to u must go through some unsettled vertex v with dist[v] ≥ dist[u].
- Adding more edges (all ≥ 0) only increases distance.
- So dist[u] is final.

With negative edges, "adding more edges" can DECREASE distance, breaking the invariant.

### Solution: Use Bellman-Ford

For graphs with negative edges:
- **Bellman-Ford**: O(VE), handles negative edges, detects negative cycles.
- **Johnson's algorithm**: combines Bellman-Ford + Dijkstra for all-pairs with negative edges.

---

## 16. Dijkstra vs Other Shortest Path Algorithms

### Comparison Table

| Algorithm | Time | Space | Handles Negative Edges? | Detects Negative Cycles? |
|-----------|------|-------|------------------------|--------------------------|
| **BFS** | O(V + E) | O(V) | N/A (unweighted) | N/A |
| **Dijkstra** | O((V+E) log V) | O(V + E) | NO | NO |
| **Bellman-Ford** | O(V × E) | O(V) | YES | YES |
| **Floyd-Warshall** | O(V³) | O(V²) | YES | YES |
| **A\*** | O(E) avg with good heuristic | O(V) | NO | NO |

### When to Use Each

| Scenario | Algorithm |
|----------|-----------|
| Unweighted graph | BFS |
| Non-negative weights, single source | **Dijkstra** |
| Negative weights, single source | Bellman-Ford |
| All-pairs shortest paths, small V | Floyd-Warshall |
| Single source-target with heuristic | A* |
| DAG | Topological sort + relaxation (O(V+E)) |

### Why Dijkstra Is Most Common

- Most real-world weights are non-negative (distances, costs, times).
- Time complexity is good: O((V+E) log V).
- Implementation is straightforward.

---

## 17. Real-World Applications

### 1. GPS Navigation
Google Maps, Waze, etc. use Dijkstra (with heuristics like A*) to find shortest routes.

### 2. Network Routing
OSPF (Open Shortest Path First) routing protocol uses Dijkstra to compute routing tables.

### 3. Robotics
Path planning in known environments.

### 4. Game AI
NPC pathfinding in tile-based games.

### 5. Computer Graphics
Shortest paths in meshes (for various optimization problems).

### 6. Social Networks
Shortest connection paths (degrees of separation, but typically unweighted → BFS).

### 7. Telecommunications
Routing data packets through minimum-cost paths.

### 8. Operations Research
Supply chain optimization, project scheduling.

---

## 18. Variations and Follow-ups

### Variation 1: Path Reconstruction

Track the previous vertex on the shortest path:

```java
Map<T, T> previous = new HashMap<>();

// When relaxing:
if (newDist < dist.get(child)) {
    dist.put(child, newDist);
    previous.put(child, node);  // record parent
    pq.offer(new Object[]{newDist, child});
}

// To reconstruct path from src to dest:
List<T> path = new ArrayList<>();
for (T v = dest; v != null; v = previous.get(v)) {
    path.add(v);
}
Collections.reverse(path);
```

### Variation 2: Single Pair Dijkstra (Early Termination)

If you only need shortest path from src to ONE destination, terminate when destination is extracted:

```java
while (!pq.isEmpty()) {
    Object[] curr = pq.poll();
    T node = (T) curr[1];
    if (node.equals(target)) {
        return (int) curr[0];  // found target
    }
    // ... relaxation
}
```

### Variation 3: K Shortest Paths

Find the k-th shortest path. Various algorithms (Yen's algorithm, etc.).

### Variation 4: Constrained Shortest Path

Find shortest path subject to constraints:
- "Max K stops" (LC 787 — Cheapest Flights Within K Stops).
- "Specific edges must be used".

### Variation 5: Multi-Source Dijkstra

Initialize PQ with multiple sources at distance 0. Useful for "shortest distance to nearest source" problems.

### Related LeetCode Problems

| Problem | Difficulty | Link |
|---------|-----------|------|
| Network Delay Time | Medium | [LC 743](https://leetcode.com/problems/network-delay-time/) |
| Cheapest Flights Within K Stops | Medium | [LC 787](https://leetcode.com/problems/cheapest-flights-within-k-stops/) |
| Path with Minimum Effort | Medium | [LC 1631](https://leetcode.com/problems/path-with-minimum-effort/) |
| Path with Maximum Probability | Medium | [LC 1514](https://leetcode.com/problems/path-with-maximum-probability/) |
| Find the City with Smallest Number of Neighbors | Medium | [LC 1334](https://leetcode.com/problems/find-the-city-with-the-smallest-number-of-neighbors-at-a-threshold-distance/) |
| Reachable Nodes In Subdivided Graph | Hard | [LC 882](https://leetcode.com/problems/reachable-nodes-in-subdivided-graph/) |
| Swim in Rising Water | Hard | [LC 778](https://leetcode.com/problems/swim-in-rising-water/) |
| Minimum Cost to Make at Least One Valid Path in a Grid | Hard | [LC 1368](https://leetcode.com/problems/minimum-cost-to-make-at-least-one-valid-path-in-a-grid/) |

---

## 19. Complete Java Code

### Faithful Conversion (TreeSet — Matches C++ Style)

See `DijkstraGraph.java` file. The key parts:

```java
public void dijkstraSSSP(T src) {
    Map<T, Integer> dist = new HashMap<>();
    for (T node : adj.keySet()) {
        dist.put(node, Integer.MAX_VALUE);
    }
    
    TreeSet<Object[]> set = new TreeSet<>(comparator);
    dist.put(src, 0);
    set.add(new Object[]{0, src});
    
    while (!set.isEmpty()) {
        Object[] first = set.pollFirst();
        // ... process and relax
    }
}
```

### Production-Ready Version (PriorityQueue with Lazy Deletion)

```java
import java.util.*;

class Solution {
    /**
     * Dijkstra's shortest path using min-heap (lazy deletion).
     * 
     * @param n Number of vertices (assumed 0-indexed)
     * @param edges int[][]{u, v, w} for each edge
     * @param src Source vertex
     * @return Array of distances from src to each vertex
     */
    public int[] dijkstra(int n, int[][] edges, int src) {
        // Build adjacency list
        List<List<int[]>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) {
            adj.get(e[0]).add(new int[]{e[1], e[2]});
            adj.get(e[1]).add(new int[]{e[0], e[2]});  // undirected
        }
        
        // Distance array
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;
        
        // Min-heap of {distance, node}
        PriorityQueue<int[]> pq = new PriorityQueue<>(
            (a, b) -> Integer.compare(a[0], b[0])
        );
        pq.offer(new int[]{0, src});
        
        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int d = curr[0], u = curr[1];
            
            // Lazy deletion: skip stale entries
            if (d > dist[u]) continue;
            
            for (int[] edge : adj.get(u)) {
                int v = edge[0], w = edge[1];
                int newDist = d + w;
                if (newDist < dist[v]) {
                    dist[v] = newDist;
                    pq.offer(new int[]{newDist, v});
                }
            }
        }
        
        return dist;
    }
    
    public static void main(String[] args) {
        Solution sol = new Solution();
        
        // Test: vertices 0-3, edges from original C++ example (0-indexed)
        int[][] edges = {{0,1,1}, {0,2,4}, {1,2,1}, {2,3,2}, {0,3,7}};
        int[] dist = sol.dijkstra(4, edges, 0);
        
        for (int i = 0; i < dist.length; i++) {
            System.out.println(i + " → distance " + dist[i]);
        }
        // Expected: 0→0, 1→1, 2→2, 3→4
    }
}
```

---

## 20. Interview Tips

### How to Approach in Interview

1. **Restate the problem**: "Find shortest path from src to all other vertices in a weighted graph with non-negative weights."
2. **Mention Dijkstra**: "I'll use Dijkstra's algorithm with a priority queue."
3. **Walk through the algorithm**: "Initialize distances to infinity. Push source. Repeatedly extract minimum-distance vertex, relax its edges."
4. **Code carefully**: PQ, distance map, lazy deletion, relaxation.
5. **Discuss complexity**: O((V + E) log V).
6. **Handle edge cases**: negative weights (use Bellman-Ford), disconnected graph (unreachable = ∞).

### Discussion Points to Score Bonus

#### 1. The Greedy Insight
> "Dijkstra works because of a key invariant: when we extract a vertex with the smallest current distance, that distance is final. This holds because of non-negative weights — adding more edges can only increase distance."

#### 2. Why a Priority Queue?
> "I need the unsettled vertex with smallest distance at each step. A min-heap gives me this in O(log V) instead of scanning all vertices in O(V)."

#### 3. Lazy Deletion Pattern
> "I use lazy deletion: when I pop a vertex, I check if its distance matches the current known distance. If not, it's a stale entry — skip it."

#### 4. Why Non-Negative Weights Required?
> "If weights can be negative, the greedy invariant breaks — a later relaxation could shorten a 'settled' vertex's distance. Use Bellman-Ford instead for negative weights."

#### 5. Edge Cases
> "I handle unreachable vertices by leaving their distance as Integer.MAX_VALUE. Disconnected graphs: only reachable vertices get finite distances."

### Likely Follow-Up Questions

#### Q: What if you have negative edges?
**A**: Use Bellman-Ford (O(VE)) which handles negative edges and detects negative cycles. Or Johnson's algorithm for all-pairs with negative edges.

#### Q: How would you find the actual path, not just the distance?
**A**: Track `previous[v]` = the parent of v on the shortest path. Reconstruct by backtracking from destination.

#### Q: What if you only need shortest path to ONE destination?
**A**: Terminate early when destination is extracted (no need to process all vertices).

#### Q: Time complexity?
**A**: O((V + E) log V) with binary heap. O(V²) with array (better for dense graphs). O(E + V log V) with Fibonacci heap (rarely used).

#### Q: How to handle huge graphs (V = 10^7)?
**A**: 
- Memory: O(V + E) might be too much. Consider on-demand graph generation.
- Use array for distances (not HashMap) for cache efficiency.
- Consider A* with a heuristic for further speedup.

#### Q: Difference from Prim's?
**A**: Both use PQ + greedy. **Prim**'s PQ stores edges by EDGE weight (for building MST). **Dijkstra**'s PQ stores by PATH length (for finding shortest paths). The relaxation logic differs.

#### Q: Can Dijkstra detect cycles?
**A**: No — Dijkstra just finds shortest paths. Cycle detection requires DFS or other algorithms.

### Common Interview Mistakes

1. Trying Dijkstra with negative edges.
2. Forgetting lazy deletion (slow).
3. Not handling unreachable vertices.
4. Integer overflow on large distances.
5. Confusing Dijkstra (path) with Prim (edge for MST).

---

## TL;DR

### The Mental Model

```
Dijkstra's = "Greedily settle the closest unsettled vertex; relax its edges."

Maintain dist[v] = current best distance from src to v.
Use a min-heap to find the closest unsettled vertex efficiently.
For each settled vertex u, try to improve dist[v] for all neighbors v.
```

### The Algorithm in 30 Seconds

```
1. dist[src] = 0; all others = ∞.
2. PQ = {(0, src)}.
3. While PQ not empty:
   - Pop (d, u). If d > dist[u]: skip (stale).
   - For each edge (u, v, w):
       If d + w < dist[v]:
           dist[v] = d + w.
           Push (dist[v], v).
4. Return dist.
```

### The Five Key Insights

1. **Greedy works** because of non-negative weights — once settled, a vertex's distance is final.
2. **Relaxation** is the core operation: "can I reach v via u cheaper?"
3. **Priority queue** finds the next vertex to settle in O(log V).
4. **Lazy deletion** simplifies code: push freely, skip stale on pop.
5. **Fails with negative edges** — use Bellman-Ford instead.

### C++ to Java Cheatsheet

| C++ | Java |
|-----|------|
| `template <typename T>` | `<T>` generic |
| `unordered_map<T, list<pair<T,int>>>` | `Map<T, List<Object[]>>` |
| `set<pair<int,T>>` | `TreeSet<Object[]>` with comparator |
| `INT_MAX` | `Integer.MAX_VALUE` |
| `s.begin()` | `set.first()` or `set.pollFirst()` |
| `s.erase(...)` | `set.remove(...)` |
| `priority_queue<..., greater<>>` | `PriorityQueue<>((a,b)->a[0]-b[0])` |
| `make_pair(a, b)` | `new Object[]{a, b}` or `new int[]{a, b}` |
| `bidir = true` (default) | Method overloading |

### Final Code Snippet to Memorize

```java
public int[] dijkstra(int n, List<List<int[]>> adj, int src) {
    int[] dist = new int[n];
    Arrays.fill(dist, Integer.MAX_VALUE);
    dist[src] = 0;
    
    PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]);
    pq.offer(new int[]{0, src});
    
    while (!pq.isEmpty()) {
        int[] curr = pq.poll();
        int d = curr[0], u = curr[1];
        if (d > dist[u]) continue;  // stale
        
        for (int[] edge : adj.get(u)) {
            int v = edge[0], w = edge[1];
            if (d + w < dist[v]) {
                dist[v] = d + w;
                pq.offer(new int[]{dist[v], v});
            }
        }
    }
    return dist;
}
```

### When This Problem Appears

| Tier | Frequency | Companies |
|------|-----------|-----------|
| Tier 1 | Sometimes | Basic version |
| Tier 2 | Often | Paytm, Flipkart (LC 743) |
| Tier 3 | Very often | Google, Amazon, Meta |
| Tier 4 | Variations | Top quant, advanced problems |

Dijkstra's is one of the **most-asked** graph algorithms in interviews.

---

*Master Dijkstra's and you've internalized one of the most important algorithms in computer science. The same pattern (greedy + priority queue + relaxation) extends to Prim's MST, A* search, and many other problems. The "lazy deletion" trick is also reusable across countless problems.*
