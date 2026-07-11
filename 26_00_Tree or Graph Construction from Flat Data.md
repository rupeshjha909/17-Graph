# Pattern 14: Tree/Graph Construction from Flat Data (Map-and-Wire)

> A complete study guide for the **construction** pattern: turning a flat list (ids + parentIds, or an edge list) into a nested tree or wired graph — and cloning an existing structure — using a **HashMap of id→object + reference wiring**. No DFS/BFS needed to *build*; the nesting builds itself because objects are references. Includes the templates, the key insight, a dry run, six worked problems, complexity, and the mistakes that bite.

> 💡 **The whole pattern in one sentence:** make a `Map<id, object>` so every node exists and is findable in O(1), then do a second pass that **wires relationships through the map** (`map.get(parentId).children.add(child)`) — because both the map and the parent hold the *same reference* to each object, attaching a grandchild deep in the tree is automatically visible from the root, so the entire nested structure materializes from two flat O(N) loops with no recursion.

---

## Table of Contents
1. [Core idea](#1-core-idea)
2. [Why it's its own pattern (construction, not traversal)](#2-why-its-its-own-pattern-construction-not-traversal)
3. [The key insight: references make nesting build itself](#3-the-key-insight-references-make-nesting-build-itself)
4. [Template A — flat list → nested tree](#4-template-a--flat-list--nested-tree)
5. [Template B — build a graph (adjacency) from edges](#5-template-b--build-a-graph-adjacency-from-edges)
6. [Template C — clone an existing graph (map old→new, then wire)](#6-template-c--clone-an-existing-graph-map-oldnew-then-wire)
7. [Dry run of the tree build](#7-dry-run-of-the-tree-build)
8. [When to reach for this pattern](#8-when-to-reach-for-this-pattern)
9. [Worked problems](#9-worked-problems)
10. [Complexity](#10-complexity)
11. [Common mistakes](#11-common-mistakes)
12. [Cheat sheet](#12-cheat-sheet)

---

## 1. Core idea

You're given **flat data** — a list where each item knows its own `id` and its `parentId` (or a list of `edges`) — and you must build a **nested tree** or a **wired graph**.

The technique has exactly two steps:
1. **Map:** put every object into a `HashMap<id, object>` so any node can be found in O(1).
2. **Wire:** loop again and connect each item to its parent/neighbor **through the map** (`map.get(parentId).children.add(item)`).

That's it. Two flat passes, O(N), **no recursion, no DFS/BFS** to construct.

> 💡 **Map first, wire second — and never wire before everything exists:** "I do a full pass to register every node in a map *before* I wire anything, so that when I attach a child to `map.get(parentId)`, that parent is guaranteed to already be in the map regardless of the order the flat data arrived in." 

---

## 2. Why it's its own pattern (construction, not traversal)

Most graph/tree patterns assume the structure **already exists** and you *search* it. This one **builds** it.

| Traversal patterns (DFS/BFS/Union-Find/Dijkstra…) | Map-and-Wire (this pattern) |
|:--------------------------------------------------|:----------------------------|
| Graph already exists → traverse/query it | Build the graph/tree from raw flat data |
| The work is *visiting* nodes | The work is *creating and connecting* nodes |
| O(V+E) traversal | O(N) construction — two flat loops |
| Recursion / queue / stack | HashMap + reference assignment |

Recognizing the difference matters in interviews: when the input is "a list of `(id, parentId)`" or "a list of edges" and the ask is "build the tree / clone the graph / return the roots," you should **not** start writing DFS — you should reach for the map-and-wire skeleton. DFS/BFS only enters *after* the structure exists (or, in the clone case, to *walk* the original while you build the copy).

> 💡 **"This is a build problem, not a search problem":** "The input is flat and the output is a structure, so my first move isn't traversal — it's a map from id to object, then a wiring pass. Traversal, if any, comes only to walk the source." 

---

## 3. The key insight: references make nesting build itself

This is *why* the pattern works with no recursion. When you put a `Node` into the map and later add it to a parent's `children` list, **both the map entry and the parent's list hold a reference to the same object in memory.** So a change made through one is visible through the other.

```java
map.get(1).children.add(node2);   // parent(1) now references node2
map.get(2).children.add(node4);   // node2's children updated...
// ...and because parent(1).children[0] IS node2 (same object),
// the grandchild node4 is automatically reachable from the root.
```

You never have to "re-insert" node2 into its parent after adding node4 — there's only **one** node2 in memory, and everyone points at it. The whole nested tree *exists* the moment all the wiring assignments are done, at whatever order.

> 💡 **One object, many pointers:** "There's exactly one instance of each node; the map and every parent list just hold references to it. So wiring at any depth mutates the single shared object, and the nesting is visible everywhere it's referenced — that's the same principle as mutating `last.end` in merge-intervals and seeing the list update, because `last` and the list entry are the same object." This reference-aliasing insight is the thing to be able to articulate.

---

## 4. Template A — flat list → nested tree

```java
class Node {
    int id;
    Integer parentId;         // Integer (nullable) so a root can have parentId == null
    String text;
    List<Node> children;
    Node(int id, Integer parentId, String text) {
        this.id = id; this.parentId = parentId; this.text = text;
        this.children = new ArrayList<>();
    }
}

public List<Node> buildTree(List<Node> items) {
    // Pass 1: register every id -> its object (so parents exist before we wire)
    Map<Integer, Node> map = new HashMap<>();
    for (Node item : items) {
        map.put(item.id, item);
    }
    // Pass 2: wire each child into its parent's children list; collect roots
    List<Node> roots = new ArrayList<>();
    for (Node item : items) {
        if (item.parentId == null) {
            roots.add(item);                              // top-level node (a root)
        } else {
            map.get(item.parentId).children.add(item);    // wire child -> parent
        }
    }
    return roots;   // the nested tree, built entirely by references
}
```

Verified: on `[(1,null),(2,1),(3,1),(4,2),(5,2),(6,3)]` this returns one root (id 1) whose children are {2,3}, and node 2's children are {4,5} — the grandchildren are visible through the root purely via references.

Notes:
- `parentId` is `Integer` (not `int`) so a root can be `null`. (Or use a sentinel like `-1`.)
- Returns a **list** of roots — a forest is possible (multiple `null` parents), which is more general and costs nothing.
- If children order matters, sort each `children` list after the wiring pass (or process `items` in the desired order).

---

## 5. Template B — build a graph (adjacency) from edges

The graph version of the same idea: the "map" is `node → list of neighbors`, and wiring means appending to adjacency lists.

```java
public Map<Integer, List<Integer>> buildAdj(int n, int[][] edges, boolean directed) {
    Map<Integer, List<Integer>> adj = new HashMap<>();
    for (int i = 0; i < n; i++) adj.put(i, new ArrayList<>());   // every node exists first
    for (int[] e : edges) {
        adj.get(e[0]).add(e[1]);
        if (!directed) adj.get(e[1]).add(e[0]);                  // undirected: wire both ways
    }
    return adj;
}
```

Verified: undirected wires both directions (`adj[0]={1,2}`, `adj[1]={0,3}`); directed wires one way (`adj[0]={1,2}`, `adj[1]={3}`, `adj[3]={}`).

This is the construction step that *precedes* almost every graph traversal problem (Course Schedule, Number of Islands on a graph, clone, topological sort…). The traversal is a different pattern; **this** is how the graph came to exist.

> 💡 **Initialize every node before wiring edges:** "I pre-create an empty list for all `n` nodes so an isolated node (no edges) still appears, and so `adj.get(x)` never returns null during wiring. Then edges just append — both directions if undirected." 

---

## 6. Template C — clone an existing graph (map old→new, then wire)

Same skeleton, but the map is **old node → new (clone) node**, and you walk the original (BFS/DFS) to discover all nodes while wiring the clones together. (LeetCode 133.)

```java
public Node cloneGraph(Node node) {
    if (node == null) return null;
    Map<Node, Node> map = new HashMap<>();   // old -> clone
    Queue<Node> queue = new LinkedList<>();
    queue.offer(node);
    map.put(node, new Node(node.val));        // clone the start node

    while (!queue.isEmpty()) {
        Node curr = queue.poll();
        for (Node neighbor : curr.neighbors) {
            if (!map.containsKey(neighbor)) {
                map.put(neighbor, new Node(neighbor.val));  // first time seen -> clone it
                queue.offer(neighbor);
            }
            map.get(curr).neighbors.add(map.get(neighbor)); // wire CLONE -> CLONE
        }
    }
    return map.get(node);
}
```

Verified on a 4-cycle (1-2-3-4-1): the clone is a fully new set of objects with identical structure, and shared/cyclic neighbors are handled because the map guarantees **one clone per original** (the `containsKey` check prevents re-cloning and infinite loops on cycles).

> 💡 **The map does double duty in a clone:** "It's both the 'have I cloned this node yet?' visited-set *and* the old→new lookup I wire through. That single map is what makes cloning a cyclic graph terminate and stay consistent — every original maps to exactly one clone, and I always wire clone-to-clone, never clone-to-original." 

---

## 7. Dry run of the tree build

Flat input (id, parentId, text):
```
(1, null, "root")
(2, 1,    "a")
(3, 1,    "b")
(4, 2,    "c")
(5, 2,    "d")
(6, 3,    "e")
```

**Pass 1 — build the map** (every id → its object, all `children` empty):
```
map = { 1:N1, 2:N2, 3:N3, 4:N4, 5:N5, 6:N6 }
```

**Pass 2 — wire (in list order):**
```
item 1: parentId null      -> roots = [N1]
item 2: parentId 1         -> map.get(1).children += N2   → N1.children = [N2]
item 3: parentId 1         -> map.get(1).children += N3   → N1.children = [N2, N3]
item 4: parentId 2         -> map.get(2).children += N4   → N2.children = [N4]
item 5: parentId 2         -> map.get(2).children += N5   → N2.children = [N4, N5]
item 6: parentId 3         -> map.get(3).children += N6   → N3.children = [N6]
```

**Result tree** (all via references — N2's children were filled *after* N2 was already attached to N1, yet N1 sees them):
```
1 "root"
├── 2 "a"
│   ├── 4 "c"
│   └── 5 "d"
└── 3 "b"
    └── 6 "e"
```

Notice item 4 was wired *after* item 2 was already added to the root — no problem, because `N2` in the root's list and `N2` in the map are the same object.

---

## 8. When to reach for this pattern

Signals in the problem statement:
- Input is a **flat list of `(id, parentId)`**, or a **list of edges/pairs**, and the output is a **nested/linked structure** or the **roots**.
- "**Build** / **construct** / **assemble** the tree/graph."
- "**Clone** / **deep-copy** a graph, tree, or a linked list with extra pointers."
- Real-world flavors: **threaded comments**, **org charts**, **file/folder trees**, **category/menu hierarchies**, **DOM/JSON from a flat table**, **dependency graphs from a manifest**.

If instead the structure already exists and you must *find/count/shortest-path*, that's a traversal pattern (1–13), not this one.

---

## 9. Worked problems

All solutions below are verified.

### Problem 1 — Build tree from a flat list (the canonical case)
*Given `List<Node>` with `id`/`parentId`, return the list of root nodes with children wired.* → **Template A** exactly. Result verified: root 1, children {2,3}, node 2's children {4,5}.

### Problem 2 — Build adjacency list from edges
*Given `n` and `edges`, build the graph.* → **Template B**. The construction step under Course Schedule (LC 207/210), clone, topo-sort, etc. Verified for directed and undirected.

### Problem 3 — LC 133 Clone Graph
*Deep-copy a connected undirected graph (possibly cyclic).* → **Template C**. The old→clone map is also the visited-set, so cycles terminate. Verified: all-new objects, structure preserved on a 4-cycle.

### Problem 4 — LC 138 Copy List with Random Pointer
Map-and-wire on a **linked list** — proof the pattern isn't only for trees. Each node has `next` and a `random` pointer to any node (or null). Two passes: clone all nodes into `map[old]=new`, then wire both pointers through the map.

```java
public Node copyRandomList(Node head) {
    if (head == null) return null;
    Map<Node, Node> map = new HashMap<>();
    for (Node cur = head; cur != null; cur = cur.next)      // pass 1: clone every node
        map.put(cur, new Node(cur.val));
    for (Node cur = head; cur != null; cur = cur.next) {    // pass 2: wire next + random
        map.get(cur).next   = map.get(cur.next);            // map.get(null) == null → safe
        map.get(cur).random = map.get(cur.random);
    }
    return map.get(head);
}
```
Verified: `next` and `random` on the copy point to **clones**, never to originals; `random` to self and to head both handled. The trick that makes it clean: `map.get(null)` returns `null`, so end-of-list and null-random need no special case.

### Problem 5 — LC 1490 Clone N-ary Tree
A tree can't have cycles, so cloning is a clean recursion — create a fresh node, recursively clone each child. (You can still use a map, but for an acyclic tree it's unnecessary.)

```java
public Node cloneTree(Node root) {
    if (root == null) return null;
    Node copy = new Node(root.val);
    for (Node child : root.children) copy.children.add(cloneTree(child));
    return copy;
}
```
Verified: deep copy, every node a new object.
> Contrast with Problem 3: a **graph** needs the map (cycles/shared nodes); a **tree** doesn't (no cycles), so recursion suffices. Knowing which you're facing is the judgment call.

### Problem 6 — LC 690 Employee Importance
*Given employees `(id, importance, subordinateIds)`, return total importance under a given id.* The **map step is the map-and-wire** (`id → employee`); then a light DFS sums. Shows the pattern as the *setup* for a traversal.

```java
public int getImportance(List<Employee> employees, int id) {
    Map<Integer, Employee> map = new HashMap<>();     // map-and-wire: id -> object
    for (Employee e : employees) map.put(e.id, e);
    return dfs(map, id);
}
private int dfs(Map<Integer, Employee> map, int id) {
    Employee e = map.get(id);
    int total = e.importance;
    for (int sub : e.subordinates) total += dfs(map, sub);  // subordinates given by id → look up in map
    return total;
}
```
Verified: sums to 11 for the root, 3 for a leaf. The subordinates are stored **by id**, so you must look them up in the map to traverse — that lookup *is* the wiring.

---

## 10. Complexity

| Step | Time | Space |
|:-----|:-----|:------|
| Pass 1 (build map) | O(N) | O(N) for the map |
| Pass 2 (wire) | O(N) (or O(N+E) for graphs, one append per edge) | O(N) for the structure |
| **Total (tree/list)** | **O(N)** | **O(N)** |
| **Total (graph/clone)** | **O(V + E)** | **O(V)** for map + O(V+E) structure |

Construction is **linear** — you touch each item a constant number of times. That's the payoff over any approach that repeatedly searches the list for children (which would be O(N²)).

---

## 11. Common mistakes

- ❌ **Wiring before all nodes are mapped.** If you build and wire in one pass, `map.get(parentId)` can be null when a child appears before its parent in the flat list. **Always do the full map pass first.**
- ❌ **Creating duplicate objects.** In a clone, forgetting the `containsKey` check clones the same node twice (and loops forever on a cycle). The map must guarantee **one clone per original**.
- ❌ **Wiring clone→original instead of clone→clone.** In Template C you must add `map.get(neighbor)` (the clone), never `neighbor` (the original) — otherwise your "copy" is entangled with the source.
- ❌ **Forgetting the forest case.** More than one node can have `parentId == null`; return a **list** of roots, not a single root.
- ❌ **NPE on null pointers in the list clone.** `map.get(cur.next)` works because `map.get(null)` is `null` — but if you special-case it wrong you'll crash; rely on the map returning null.
- ❌ **Using `int parentId` with `0`/`-1` ambiguity.** If a real id can be `0`, don't use `0` as "no parent." Use `Integer` + `null`, or a sentinel that can't collide with a real id.
- ❌ **Reaching for DFS to *build*.** Construction is the map+wire loops; DFS/BFS only walks a source (clone) or the finished structure.

---

## 12. Cheat sheet

**Recognize it:** flat `(id, parentId)` or edge list in, nested tree / wired graph / clone out. "Build," "construct," "clone," "deep-copy."

**Do it (2 steps):**
```
1. MAP  — put every node into a HashMap (id→object, or old→clone). Do this FULLY first.
2. WIRE — second pass: connect each node to parent/neighbor THROUGH the map.
          tree : map.get(parentId).children.add(item)   (parentId null → it's a root)
          graph: adj.get(a).add(b)  (+ adj.get(b).add(a) if undirected)
          clone: map.get(cur).neighbors.add(map.get(neighbor))   (clone→clone)
```

**Why it works:** one object per node; map and parents all hold the *same reference*, so wiring at any depth is visible everywhere → nesting builds itself, no recursion to construct.

**The map's roles:** (1) O(1) lookup to wire; (2) in a clone, also the visited-set that makes cycles terminate and guarantees one-clone-per-original.

**Complexity:** O(N) tree/list, O(V+E) graph. Beats O(N²) "search for children each time."

**Tree vs graph clone:** graph → need the map (cycles/shared); acyclic tree → plain recursion is enough.

> **One-line philosophy:** *Construction is not traversal: when raw flat data must become a structure, register every node in a map so it's findable in O(1), then wire relationships through that map in a second pass — and because every node is a single shared object that the map and its parents all reference, the entire nested tree or wired graph materializes from two linear loops, with the same map serving as the visited-set and old→new dictionary whenever you're cloning.*
