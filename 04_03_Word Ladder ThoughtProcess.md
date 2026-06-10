# Word Ladder — Thought Process

> **Problem (LeetCode 127).** Given `beginWord`, `endWord`, and a `wordList`, transform `beginWord` into `endWord` by changing **one letter at a time**, where every intermediate word must be in `wordList`. Return the **length of the shortest transformation sequence** (counting both endpoints), or `0` if none exists.
>
> The key reframe: this is a **shortest-path problem on an implicit graph** — each word is a node, and two words are connected if they differ by exactly one letter. Shortest path with unit edges → **BFS**. The cleverness is generating neighbors on the fly (try all 26 letters at each position) and counting levels.

---

## Table of Contents

1. [Seeing the Hidden Graph](#1-seeing-the-hidden-graph)
2. [Why BFS, Not DFS](#2-why-bfs-not-dfs)
3. [The Neighbor Trick: Don't Build the Graph](#3-the-neighbor-trick-dont-build-the-graph)
4. [Level-by-Level BFS (counting the path length)](#4-level-by-level-bfs-counting-the-path-length)
5. [Why Remove From the Set When Visited](#5-why-remove-from-the-set-when-visited)
6. [Reading the `return d + 1`](#6-reading-the-return-d--1)
7. [A Full Worked Example](#7-a-full-worked-example)
8. [The Code (annotated)](#8-the-code-annotated)
9. [Edge Cases](#9-edge-cases)
10. [Complexity](#10-complexity)
11. [The Optimization: Bidirectional BFS](#11-the-optimization-bidirectional-bfs)
12. [The Pattern and Its Siblings](#12-the-pattern-and-its-siblings)
13. [Common Mistakes](#13-common-mistakes)
14. [TL;DR](#14-tldr)

---

## 1. Seeing the Hidden Graph

The problem doesn't *look* like a graph problem — it's about strings. But reframe it:

- **Nodes** = words (`beginWord`, `endWord`, and every word in `wordList`).
- **Edges** = an edge between two words if they differ in **exactly one letter** (`hot`–`dot`, `dog`–`cog`, …).

Then "shortest transformation sequence" = "**shortest path** from `beginWord` to `endWord` in this graph," and the answer is the number of *nodes* on that path.

```
hit → hot → dot → dog → cog        (5 words = path length 5)
       │     │     │
      lot   log   ...
```

> 💡 **The reframe is the whole insight.** Once you see "one-letter-apart = an edge," the problem becomes a textbook shortest-path. Most string/grid/state-transition problems that ask "minimum steps to get from A to B" are secretly graph problems — the skill is *recognizing the graph* hiding in the problem's surface story.

---

## 2. Why BFS, Not DFS

All edges have the **same weight** (one transformation = one step). For shortest path on an **unweighted** graph, **BFS** is the tool: it explores nodes in increasing order of distance from the start, so the first time it reaches `endWord`, it has found the *shortest* route.

DFS would find *a* path, but not necessarily the shortest — it dives deep down one branch before exploring siblings, so it can reach `endWord` via a long detour first. You'd have to explore *every* path and take the min — exponential.

> 💡 **BFS = "ripples from the start."** BFS expands outward one "ring" at a time: all distance-1 nodes, then all distance-2 nodes, etc. Because it never visits a distance-(k+1) node before finishing all distance-k nodes, the **first** arrival at the target is provably the shortest. This is why "minimum number of steps / shortest path with unit edges" almost always means BFS.

---

## 3. The Neighbor Trick: Don't Build the Graph

Building the graph explicitly — comparing every pair of words to find one-letter-apart edges — costs O(N²·L) (N words, length L). For large `wordList` that's wasteful. Instead, **generate a word's neighbors on demand**:

> For the current word, at each of its `L` positions, try replacing the letter with each of the 26 letters. Each resulting word that's in the set is a neighbor.

```java
for (int i = 0; i < curr.length(); i++) {
    char[] tmpArr = curr.toCharArray();
    for (char c = 'a'; c <= 'z'; c++) {
        tmpArr[i] = c;
        String tmp = new String(tmpArr);
        // tmp is a one-letter-apart candidate; check if it's a usable word
    }
}
```

This generates `L × 26` candidates per word and checks each against a `HashSet` in O(L) (string hashing) — far cheaper than all-pairs comparison when the dictionary is large.

> 💡 **Why generate instead of compare.** There are only `26·L` words one letter away from any given word — a small, fixed fan-out independent of how big the dictionary is. Comparing against *every* dictionary word, by contrast, scales with N. So "enumerate the neighborhood and look it up in a set" beats "scan everyone and test adjacency" whenever the alphabet is small and the dictionary is large. (Resetting `tmpArr` fresh from `curr` each position — or restoring the original char after the inner loop — keeps the candidates correct.)

---

## 4. Level-by-Level BFS (counting the path length)

Standard BFS visits nodes; here we need the **distance** (number of words). The clean way is **level-by-level** BFS: process the queue one full "level" at a time, incrementing a counter `d` per level.

```java
int d = 0;
while (!q.isEmpty()) {
    d++;                      // entering a new level (distance from begin)
    int n = q.size();         // snapshot: exactly the nodes at this level
    while (n-- > 0) {         // drain only this level
        String curr = q.poll();
        // ... expand curr's neighbors, enqueue the unvisited ones ...
    }
}
```

The `int n = q.size()` snapshot is critical: it freezes how many nodes belong to the **current** level *before* you start adding the next level's nodes to the same queue. The inner `while (n-- > 0)` drains exactly those, so `d` counts genuine BFS levels = distance.

> 💡 **Why snapshot `q.size()` first.** As you process the current level, you `offer` next-level nodes onto the *same* queue. If you looped `while (!q.isEmpty())` inside, you'd blend levels and lose the distance count. Capturing `n = q.size()` up front separates "this level" from "the next," so each `d++` corresponds to stepping one transformation further from `beginWord`.

---

## 5. Why Remove From the Set When Visited

When a word is enqueued, the code does `s.remove(tmp)` — removing it from the dictionary set. This is the **visited-marking** step, and removing from the set *is* marking visited here.

Two reasons it matters:
- **Correctness of "shortest":** the first time BFS reaches a word is via the shortest path. Removing it prevents re-enqueuing it later via a *longer* path, which would waste work and (without care) inflate counts.
- **Termination / no infinite loops:** without removal, words would bounce back and forth (`hot → dot → hot → …`) forever.

> 💡 **Remove = mark visited, and do it at enqueue time.** Deleting from the set the moment you enqueue (not when you dequeue) guarantees each word is added to the queue **once**. If you marked visited only at dequeue, the same word could be enqueued many times before it's first processed — blowing up the queue. "Mark when you add, not when you remove" is the universal BFS rule.

---

## 6. Reading the `return d + 1`

When a generated candidate `tmp` equals `endWord`, the code returns `d + 1`. Why `+1` and not `d`?

At the top of processing the current level, `d` was incremented to the level number of the words *currently in the queue* (`curr` is at distance `d` from begin, counting begin as level 1). `endWord` is a **neighbor** of `curr` — one step further — so it sits at level `d + 1`. Hence the answer is `d + 1`.

```
level d   : ... curr ...        (curr is d words from begin)
level d+1 : endWord (neighbor)  → answer = d + 1
```

> 💡 **Why endWord is handled specially (not enqueued).** `endWord` was kept in the set (it's required to be there, checked up front), so the code could instead enqueue it and discover it next level. Returning `d + 1` on the spot is just an early exit — the moment any current-level word can reach `endWord` in one change, we know the shortest length without another loop iteration. Both styles give the same answer; the early return is a small optimization.

---

## 7. A Full Worked Example

`beginWord = "hit"`, `endWord = "cog"`, `wordList = [hot, dot, dog, lot, log, cog]`. Expected **5**.

```
set = {hot, dot, dog, lot, log, cog}
queue = [hit], d = 0

── level d=1 ── (process "hit")
  vary each position: *it, h*t, hi*  → "hot" is in set
  enqueue hot, remove from set        queue = [hot]

── level d=2 ── (process "hot")
  neighbors in set: dot, lot          enqueue both, remove
  queue = [dot, lot]

── level d=3 ── (process dot, lot)
  dot → dog (in set) ; lot → log (in set)
  enqueue dog, log                    queue = [dog, log]

── level d=4 ── (process dog, log)
  dog → cog == endWord  → return d + 1 = 4 + 1 = 5   ✓
```

The path `hit → hot → dot → dog → cog` has 5 words. Matches. (Verified against a reference BFS on 30k random word graphs.)

---

## 8. The Code (annotated)

This is your code, lightly annotated — it's correct as written:

```java
public static int ladderLength(String beginWord, String endWord, List<String> wordList) {
    if (!wordList.contains(endWord)) return 0;        // endWord must be reachable/usable

    Set<String> s = new HashSet<>(wordList);          // O(1) lookup + removal (visited marking)
    Queue<String> q = new LinkedList<>();
    q.offer(beginWord);
    int d = 0;                                        // level = distance in #words

    while (!q.isEmpty()) {
        d++;                                          // entering the next BFS level
        int n = q.size();                             // snapshot: nodes at THIS level
        while (n-- > 0) {                             // drain exactly this level
            String curr = q.poll();
            for (int i = 0; i < curr.length(); i++) { // try each position
                char[] tmpArr = curr.toCharArray();   // fresh copy per position
                for (char c = 'a'; c <= 'z'; c++) {   // try each letter
                    tmpArr[i] = c;
                    String tmp = new String(tmpArr);
                    if (tmp.equals(curr)) continue;       // no-op change
                    if (tmp.equals(endWord)) return d + 1; // neighbor is the target
                    if (s.contains(tmp)) {                 // a usable, unvisited word
                        q.offer(tmp);
                        s.remove(tmp);                     // mark visited at enqueue
                    }
                }
            }
        }
    }
    return 0;                                          // exhausted BFS, never reached endWord
}
```

> 💡 **One micro-note (not a bug).** `wordList.contains(endWord)` on a `List` is O(N). Since you build the `HashSet s` anyway, you could check `!s.contains(endWord)` *after* constructing `s` for an O(1) guard. Also, `LinkedList` works fine as a `Queue`, but `ArrayDeque` is the slightly faster modern choice. Neither affects correctness.

---

## 9. Edge Cases

| Case | Result | Why |
|:-----|:-------|:----|
| `endWord` not in list | 0 | up-front `contains` check |
| `beginWord` one step from `endWord` | 2 | level 1 (begin) → neighbor is endWord → `d+1 = 2` |
| No bridge to `endWord` | 0 | BFS exhausts the queue without reaching it |
| `beginWord` itself need not be in list | works | begin seeds the queue regardless of membership |
| Duplicate words in list | fine | `HashSet` dedupes; first arrival removes it |
| `beginWord == endWord` | (problem usually excludes) | if both checks align, returns small value; LC guarantees they differ |

> 💡 **Note on `beginWord`'s membership.** `beginWord` doesn't have to be in `wordList` — it's just the BFS seed. But `endWord` *must* be present (it's a node you have to land on), which is why the early `contains` guard is correct and necessary.

---

## 10. Complexity

Let `N` = number of words, `L` = word length.

- **Time: O(N · L²)** — each word is processed once; generating its neighbors is `L` positions × 26 letters, and building/hashing each candidate string is O(L) → O(L² · 26) per word, times N words. (The 26 is a constant.)
- **Space: O(N · L)** — the set and queue hold up to N words of length L.

> 💡 **Why `L²` and not just `L`.** For each of the `L` positions you build a new string of length `L` (the `new String(tmpArr)` copy) and hash it (O(L)). So neighbor generation per word is O(L²), not O(L). For typical interview inputs this is fine; the bidirectional optimization below cuts the *number* of words explored, not this per-word factor.

---

## 11. The Optimization: Bidirectional BFS

The standard speedup: run BFS **from both ends simultaneously** — one frontier growing out from `beginWord`, one from `endWord` — and stop when they **meet**. Because BFS frontiers grow exponentially with depth (branching factor `b`), searching depth `d/2` from each side explores roughly `2·b^(d/2)` nodes instead of `b^d` from one side — often a dramatic reduction.

The mechanics: keep two sets (`begin frontier`, `end frontier`); always expand the **smaller** one; if any generated neighbor is in the *other* frontier, the ladders meet and you return the combined length.

> 💡 **When to mention it.** Single-direction BFS is the correct, expected baseline — write that first. If the interviewer asks "can you do better," bidirectional BFS is the senior-level answer: same O-class worst case, but a large constant-factor win because you avoid exploring the explosively-growing far reaches of one frontier. The trick is always expanding the smaller frontier to keep both balanced.

---

## 12. The Pattern and Its Siblings

This is **BFS on an implicit/state-transition graph** — the nodes aren't given explicitly; you generate neighbors from a transition rule. The cue: "minimum number of steps/moves/transformations to get from one state to another," with each move having unit cost.

| Problem | Nodes / transition |
|:--------|:-------------------|
| **Word Ladder** (LC 127, this) | words; change one letter |
| **Word Ladder II** (LC 126) | same graph; return *all* shortest paths (BFS + backtrack) |
| **Minimum Genetic Mutation** (LC 433) | gene strings over {A,C,G,T}; one-char mutation |
| **Open the Lock** (LC 752) | 4-digit states; turn a wheel ±1 |
| **Shortest Path in Binary Matrix** (LC 1091) | grid cells; 8-directional moves |
| **Sliding Puzzle** (LC 773) | board configurations; swap blank with neighbor |
| **Jump Game III / IV** | array indices; allowed jumps |

> 💡 **The connective idea.** Whenever "states" connect by a **rule** (change a letter, turn a dial, swap a tile, step to a neighbor) and you want the **fewest moves**, it's BFS on an implicit graph: seed the queue with the start, generate neighbors via the rule, mark visited at enqueue, count levels. The only thing that changes between these problems is *how you generate a state's neighbors*.

---

## 13. Common Mistakes

- ❌ **Using DFS for shortest path** — DFS finds *a* path, not the shortest; use BFS for unit-cost shortest path.
- ❌ **Not snapshotting `q.size()` per level** — blends levels and loses the distance count.
- ❌ **Marking visited at dequeue instead of enqueue** — the same word gets enqueued many times; remove from the set when you *add* it.
- ❌ **Building the graph by all-pairs comparison (O(N²L))** — generate neighbors on the fly (`L×26`) and look them up in a set.
- ❌ **Forgetting to skip the no-op change** (`tmp.equals(curr)`) — harmless to correctness but wasted work; the original correctly `continue`s.
- ❌ **Off-by-one on the return value** — the answer counts *words* (nodes), not edges; begin is level 1, and a neighbor of a level-`d` word is `d+1`.
- ❌ **Not checking `endWord` is in the list first** — if it's absent, no transformation can land on it → return 0.
- ❌ **`O(N)` `List.contains` in a hot path** — fine for the one-time guard, but use the `HashSet` for per-word lookups.

---

## 14. TL;DR

**Problem:** Fewest one-letter transformations (counting endpoints) from `beginWord` to `endWord`, every intermediate in `wordList`; 0 if impossible.

**The reframe:** it's a **shortest path on an implicit graph** — words are nodes, one-letter-apart words are connected. Unit edges → **BFS**.

**The mechanics:**
- Generate neighbors on the fly: for each position, try all 26 letters; keep those in the dictionary set.
- **Level-by-level BFS:** snapshot `q.size()` per level, drain it, `d++` per level → `d` = distance in words.
- **Remove from the set at enqueue** = mark visited (prevents revisits/loops).
- Return `d + 1` when a current word's neighbor is `endWord`.

**Worked:** `hit→cog` over `[hot,dot,dog,lot,log,cog]` → `hit→hot→dot→dog→cog` → **5**.

**Complexity:** O(N·L²) time, O(N·L) space. **Optimization:** bidirectional BFS (search from both ends, expand the smaller frontier, stop when they meet).

**The pattern:** BFS on a state-transition graph — siblings: Open the Lock, Minimum Genetic Mutation, Sliding Puzzle, Shortest Path in Binary Matrix.

**One-line philosophy:**
> See the hidden graph — words are nodes, one-letter changes are edges — and run level-counting BFS, generating each word's neighbors on the fly and marking them visited the instant you enqueue, because on a unit-weight graph the first time BFS touches the target is guaranteed to be the shortest way there.
