# BFS Graph Traversal — Visualization & Deep Explanation

## Visual Representation

```
                        BFS traversal from source node 0
                    Visit order: 0 → 1 → 4 → 2 → 3 → 5

                        (1) ────────── (2)
                       / L1            / L2 \
                      /               /      \
                    (0) L0          /        (3) L2
                      \            /        / \
                       \          /        /   \
                        (4) ─────         /    (5) L3
                          L1            /
                                       /

         Edges: 0—1, 0—4, 1—2, 2—4, 2—3, 3—4, 3—5
```

### SVG Diagram (Renders in GitHub / VSCode preview)
<img width="621" height="550" alt="image" src="https://github.com/user-attachments/assets/7ca86a4e-ceb5-438d-a686-84f4f0469923" />


<svg width="100%" viewBox="0 0 680 540" xmlns="http://www.w3.org/2000/svg">
  <text x="340" y="30" text-anchor="middle" font-family="sans-serif" font-size="16" font-weight="500">BFS traversal from source node 0</text>
  <text x="340" y="48" text-anchor="middle" font-family="sans-serif" font-size="13" fill="#666">Visit order: 0 → 1 → 4 → 2 → 3 → 5</text>

  <line x1="160" y1="140" x2="260" y2="100" stroke="#888" stroke-width="1"/>
  <line x1="160" y1="140" x2="160" y2="240" stroke="#888" stroke-width="1"/>
  <line x1="260" y1="100" x2="360" y2="180" stroke="#888" stroke-width="1"/>
  <line x1="160" y1="240" x2="360" y2="180" stroke="#888" stroke-width="1"/>
  <line x1="360" y1="180" x2="280" y2="280" stroke="#888" stroke-width="1"/>
  <line x1="160" y1="240" x2="280" y2="280" stroke="#888" stroke-width="1"/>
  <line x1="280" y1="280" x2="200" y2="380" stroke="#888" stroke-width="1"/>

  <circle cx="160" cy="140" r="26" fill="#FAEEDA" stroke="#BA7517" stroke-width="1.5"/>
  <text x="160" y="146" text-anchor="middle" font-family="sans-serif" font-size="14" font-weight="500" fill="#412402">0</text>

  <circle cx="260" cy="100" r="26" fill="#FAEEDA" stroke="#BA7517" stroke-width="1.5"/>
  <text x="260" y="106" text-anchor="middle" font-family="sans-serif" font-size="14" font-weight="500" fill="#412402">1</text>

  <circle cx="360" cy="180" r="26" fill="#FAEEDA" stroke="#BA7517" stroke-width="1.5"/>
  <text x="360" y="186" text-anchor="middle" font-family="sans-serif" font-size="14" font-weight="500" fill="#412402">2</text>

  <circle cx="280" cy="280" r="26" fill="#FAEEDA" stroke="#BA7517" stroke-width="1.5"/>
  <text x="280" y="286" text-anchor="middle" font-family="sans-serif" font-size="14" font-weight="500" fill="#412402">3</text>

  <circle cx="160" cy="240" r="26" fill="#FAEEDA" stroke="#BA7517" stroke-width="1.5"/>
  <text x="160" y="246" text-anchor="middle" font-family="sans-serif" font-size="14" font-weight="500" fill="#412402">4</text>

  <circle cx="200" cy="380" r="26" fill="#FAEEDA" stroke="#BA7517" stroke-width="1.5"/>
  <text x="200" y="386" text-anchor="middle" font-family="sans-serif" font-size="14" font-weight="500" fill="#412402">5</text>

  <text x="118" y="106" font-family="sans-serif" font-size="12" fill="#666">L0</text>
  <text x="296" y="68" font-family="sans-serif" font-size="12" fill="#666">L1</text>
  <text x="118" y="206" font-family="sans-serif" font-size="12" fill="#666">L1</text>
  <text x="396" y="148" font-family="sans-serif" font-size="12" fill="#666">L2</text>
  <text x="316" y="248" font-family="sans-serif" font-size="12" fill="#666">L2</text>
  <text x="236" y="346" font-family="sans-serif" font-size="12" fill="#666">L3</text>

  <rect x="450" y="80" width="200" height="380" rx="12" fill="none" stroke="#888" stroke-width="0.5" stroke-dasharray="4 3"/>
  <text x="550" y="105" text-anchor="middle" font-family="sans-serif" font-size="14" font-weight="500">Step-by-step trace</text>

  <text x="465" y="135" font-family="sans-serif" font-size="12" fill="#444">1. Start: queue=[0]</text>
  <text x="465" y="153" font-family="sans-serif" font-size="12" fill="#444">   visited={0}, print: 0</text>

  <text x="465" y="183" font-family="sans-serif" font-size="12" fill="#444">2. Poll 0 → enqueue 1, 4</text>
  <text x="465" y="201" font-family="sans-serif" font-size="12" fill="#444">   queue=[1,4], print: 1</text>

  <text x="465" y="231" font-family="sans-serif" font-size="12" fill="#444">3. Poll 1 → enqueue 2</text>
  <text x="465" y="249" font-family="sans-serif" font-size="12" fill="#444">   (0 already visited)</text>
  <text x="465" y="267" font-family="sans-serif" font-size="12" fill="#444">   queue=[4,2], print: 4</text>

  <text x="465" y="297" font-family="sans-serif" font-size="12" fill="#444">4. Poll 4 → enqueue 3</text>
  <text x="465" y="315" font-family="sans-serif" font-size="12" fill="#444">   (0,2 already visited)</text>
  <text x="465" y="333" font-family="sans-serif" font-size="12" fill="#444">   queue=[2,3], print: 2</text>

  <text x="465" y="363" font-family="sans-serif" font-size="12" fill="#444">5. Poll 2 → enqueue 5</text>
  <text x="465" y="381" font-family="sans-serif" font-size="12" fill="#444">   (1,4,3 visited)</text>
  <text x="465" y="399" font-family="sans-serif" font-size="12" fill="#444">   queue=[3,5], print: 3</text>

  <text x="465" y="429" font-family="sans-serif" font-size="12" fill="#444">6. Poll 3, then 5</text>
  <text x="465" y="447" font-family="sans-serif" font-size="12" fill="#444">   queue=[], DONE</text>

  <text x="340" y="500" text-anchor="middle" font-family="sans-serif" font-size="14" font-weight="500">BFS output: 0 1 4 2 3 5</text>
  <text x="340" y="520" text-anchor="middle" font-family="sans-serif" font-size="12" fill="#666">Level-by-level: closer nodes first, farther nodes later</text>
</svg>

---

## Step-by-Step Execution Trace

| Step | Action | Queue State | Visited Set | Output |
|------|--------|-------------|-------------|--------|
| 1 | Start: enqueue source 0, mark visited | `[0]` | `{0}` | — |
| 2 | Poll 0 → check neighbors [1, 4] → enqueue both | `[1, 4]` | `{0, 1, 4}` | `0` |
| 3 | Poll 1 → check neighbors [0, 2] → 0 visited, enqueue 2 | `[4, 2]` | `{0, 1, 4, 2}` | `0 1` |
| 4 | Poll 4 → check neighbors [0, 2, 3] → 0,2 visited, enqueue 3 | `[2, 3]` | `{0, 1, 4, 2, 3}` | `0 1 4` |
| 5 | Poll 2 → check neighbors [1, 4, 3] → all visited | `[3]` | `{0, 1, 4, 2, 3}` | `0 1 4 2` |
| 6 | Poll 3 → check neighbors [2, 4, 5] → 2,4 visited, enqueue 5 | `[5]` | `{0, 1, 4, 2, 3, 5}` | `0 1 4 2 3` |
| 7 | Poll 5 → check neighbors [3] → all visited | `[]` | `{0, 1, 4, 2, 3, 5}` | `0 1 4 2 3 5` |
| 8 | Queue empty → DONE | — | — | **`0 1 4 2 3 5`** |

---

## Deep Explanation of BFS

### What BFS Actually Does

BFS (Breadth-First Search) explores a graph **level by level** — visiting all nodes at distance 1 from the source first, then all nodes at distance 2, and so on. Think of it as ripples expanding outward from a stone dropped in water.

The key insight: **a Queue (FIFO) is what makes this level-by-level behavior emerge naturally.** If you used a Stack (LIFO) instead, you'd get DFS — diving deep before exploring siblings.

### Your Graph Structure

```
Edges added:
0—1, 0—4, 1—2, 2—4, 2—3, 3—4, 3—5

Adjacency list:
0 → [1, 4]
1 → [0, 2]
2 → [1, 4, 3]
3 → [2, 4, 5]
4 → [0, 2, 3]
5 → [3]
```

Every edge appears in **both** directions because `addEdge(x, y)` adds `y` to `x`'s list AND `x` to `y`'s list. This is what "bidirectional" means.

### Why the Output is `0 1 4 2 3 5` (Not `0 1 2 3 4 5`)

This is the most important thing to internalize. The output isn't sorted by node number — it's sorted by **distance from source 0**:

| Distance from 0 | Nodes | Why |
|-----------------|-------|-----|
| 0 (source) | 0 | Starting node |
| 1 (direct neighbors) | 1, 4 | Edges 0—1 and 0—4 |
| 2 | 2, 3 | 2 reached via 1; 3 reached via 4 |
| 3 | 5 | 5 reached via 3 |

BFS guarantees **shortest path in unweighted graphs** — this is its superpower.

### The Three Critical Data Structures

**1. Queue (`ArrayDeque`) — FIFO ordering**
- New nodes added at the back, processed from the front
- This guarantees siblings are processed before grandchildren
- `ArrayDeque` is faster than `LinkedList` because it's array-backed (no node allocation per element)

**2. Visited Set (`HashSet`) — prevents infinite loops**
- Without this, a cycle like `0 → 1 → 0 → 1 ...` would never terminate
- O(1) `contains()` and `add()` operations

**3. Adjacency List (`HashMap<T, List<T>>`) — graph representation**
- O(1) to find a node's neighbors
- O(V + E) total space, much better than adjacency matrix's O(V²) for sparse graphs

### The Critical "Mark Visited When Enqueuing" Rule

Look carefully at this part of your code:

```java
if (!visited.contains(neighbor)) {
    queue.add(neighbor);
    visited.add(neighbor);   // ← marked here, NOT when polled
}
```

You mark a node as visited **when you add it to the queue**, not when you poll it. Why?

**Wrong way (mark on poll):**
- Suppose 0's neighbors are [1, 4]. You enqueue both.
- Now you process 1, whose neighbors include 2.
- You also process 4 next, whose neighbors include 2.
- If you only mark "visited" on poll, **2 gets added to the queue twice** — wasteful at best, infinite loop in larger graphs at worst.

**Right way (mark on enqueue):**
- When 1 polls and sees neighbor 2, it adds 2 to queue and immediately marks visited.
- When 4 polls later and sees neighbor 2, the visited check blocks the duplicate add.

This is a classic interview gotcha. **Always mark visited at enqueue time in BFS.**

### Time and Space Complexity

| Metric | Complexity | Why |
|--------|-----------|-----|
| Time | O(V + E) | Each vertex polled once (V), each edge examined twice (2E in undirected) |
| Space | O(V) | Queue + visited set, each holds at most V nodes |

For your graph: V=6, E=7, so total operations ≈ 6 + 14 = 20.

### Subtle Point About Output Order

The output `0 1 4 2 3 5` depends on the **insertion order in the adjacency list**. Since `0`'s list is `[1, 4]` (1 added first because `addEdge(0,1)` came before `addEdge(0,4)`), 1 is enqueued before 4.

If you had reversed the edge addition order, you'd get `0 4 1 3 2 5` instead — still a valid BFS, just different sibling ordering. **BFS guarantees level order, not a unique sequence.**

---

## Why This Matters at 4-YOE Interview Level

When asked about BFS in interviews, you should be able to articulate:

### 1. Why Queue, not Stack?
> "Queue gives FIFO, which produces level-by-level traversal. Stack would produce DFS."

### 2. Why mark visited at enqueue?
> "To prevent the same node from being added to the queue multiple times via different parents."

### 3. What does BFS guarantee?
> "Shortest path in unweighted graphs. Each node is visited at its minimum hop distance from source."

### 4. When would you use BFS over DFS?
> "Shortest path, level-order traversal, finding nodes within K distance, web crawling at limited depth, multi-source problems like rotting oranges."

### 5. Where does this break?
> "Weighted graphs need Dijkstra (also queue-based, but priority queue). BFS treats every edge as cost 1."

---

## Improved Implementation (Interview-Grade)

Your current code prints during the BFS loop, which couples traversal logic with output. For interview-grade code, separate concerns:

```java
public List<T> bfs(T src) {
    List<T> order = new ArrayList<>();
    Set<T> visited = new HashSet<>();
    Queue<T> queue = new ArrayDeque<>();
    
    queue.add(src);
    visited.add(src);
    
    while (!queue.isEmpty()) {
        T node = queue.poll();
        order.add(node);
        
        List<T> neighbors = adjacencyList.get(node);
        if (neighbors != null) {
            for (T neighbor : neighbors) {
                if (visited.add(neighbor)) {  // returns true if added
                    queue.add(neighbor);
                }
            }
        }
    }
    return order;
}
```

### Two Micro-Optimizations

1. **Returns the traversal order instead of printing** — easier to test, easier to compose with other code.

2. **Uses `visited.add(neighbor)` which returns `true` only if the element wasn't already present** — combines the check + add into one atomic operation. Same time complexity, but cleaner and avoids the subtle bug of forgetting to mark visited after adding.

---

## BFS Variants You Should Know

### Level-Order BFS (Track Distance)

When you need to know the **distance** from source to each node:

```java
public Map<T, Integer> bfsWithDistance(T src) {
    Map<T, Integer> dist = new HashMap<>();
    Queue<T> queue = new ArrayDeque<>();
    
    queue.add(src);
    dist.put(src, 0);
    
    while (!queue.isEmpty()) {
        T node = queue.poll();
        int currentDist = dist.get(node);
        
        for (T neighbor : adjacencyList.getOrDefault(node, List.of())) {
            if (!dist.containsKey(neighbor)) {
                dist.put(neighbor, currentDist + 1);
                queue.add(neighbor);
            }
        }
    }
    return dist;
}
```

### Level-by-Level BFS (Track Each Level Separately)

When you need to process nodes one level at a time (e.g., binary tree level order, word ladder):

```java
public List<List<T>> bfsByLevel(T src) {
    List<List<T>> levels = new ArrayList<>();
    Set<T> visited = new HashSet<>();
    Queue<T> queue = new ArrayDeque<>();
    
    queue.add(src);
    visited.add(src);
    
    while (!queue.isEmpty()) {
        int size = queue.size();  // ← snapshot current level
        List<T> currentLevel = new ArrayList<>();
        
        for (int i = 0; i < size; i++) {
            T node = queue.poll();
            currentLevel.add(node);
            
            for (T neighbor : adjacencyList.getOrDefault(node, List.of())) {
                if (visited.add(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }
        levels.add(currentLevel);
    }
    return levels;
}
```

For your graph, this would output: `[[0], [1, 4], [2, 3], [5]]`

---

## Common BFS Interview Problems

| Problem | Pattern | Why BFS |
|---------|---------|---------|
| [Word Ladder](https://leetcode.com/problems/word-ladder/) | BFS on word graph | Shortest transformation |
| [Rotting Oranges](https://leetcode.com/problems/rotting-oranges/) | Multi-source BFS | Time = distance |
| [Number of Islands](https://leetcode.com/problems/number-of-islands/) | Grid BFS | Connected components |
| [Binary Tree Level Order](https://leetcode.com/problems/binary-tree-level-order-traversal/) | Level-by-level BFS | Process levels separately |
| [01 Matrix](https://leetcode.com/problems/01-matrix/) | Multi-source BFS | Shortest distance to nearest 0 |
| [Snake and Ladder](https://leetcode.com/problems/snakes-and-ladders/) | BFS shortest path | Min moves on board |
| [Open the Lock](https://leetcode.com/problems/open-the-lock/) | BFS state space | Shortest sequence |
| [Course Schedule II](https://leetcode.com/problems/course-schedule-ii/) | Kahn's BFS (Topo Sort) | Dependency ordering |

---

## Summary

BFS is one of the most fundamental graph algorithms and forms the basis for:
- Shortest path in unweighted graphs
- Level-order traversal of trees
- Multi-source distance problems
- Topological sort (Kahn's algorithm)
- Bipartite graph checking
- Web crawling and social network analysis

Mastering the **Queue + Visited Set + Adjacency List** triad and understanding **why we mark visited at enqueue time** is the foundation for solving 30+ FAANG-level graph problems.
