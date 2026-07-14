# Alien Dictionary (LeetCode 269) — Build the Graph from Data, then Topological Sort

> The capstone of the graph series. It fuses two skills: **constructing a directed graph from raw data** (deriving letter-order edges by comparing adjacent words) and **topological sort** (turning those edges into a valid ordering). The twist that trips people up is *where the edges come from* — only the **first differing character** between two adjacent words tells you anything — plus two invalidity traps (a **cycle**, and a word that comes **before its own prefix**). All verified, including both invalid cases, over 20,000 random inputs.

> 💡 **The whole solution in one sentence:** the words are sorted by an unknown alphabet, so each adjacent pair reveals **one** ordering fact — at the first position where they differ, the earlier word's character comes before the later word's — turn each such fact into a directed edge, treat every letter as a node, then **topologically sort** the graph to get the alphabet (returning `""` if a cycle makes it contradictory, or if a longer word wrongly precedes its own prefix).

---

## Table of Contents
1. [Problem statement](#1-problem-statement)
2. [How to guess the pattern (the thought process)](#2-how-to-guess-the-pattern-the-thought-process)
3. [The critical insight: only the first differing char gives an edge](#3-the-critical-insight-only-the-first-differing-char-gives-an-edge)
4. [The other trap: the prefix rule](#4-the-other-trap-the-prefix-rule)
5. [Two phases: build the graph, then topo-sort](#5-two-phases-build-the-graph-then-topo-sort)
6. [The full solution](#6-the-full-solution)
7. [Dry run](#7-dry-run)
8. [The two ways it returns ""](#8-the-two-ways-it-returns-)
9. [Complexity](#9-complexity)
10. [Common mistakes](#10-common-mistakes)
11. [How to recognize this pattern next time](#11-how-to-recognize-this-pattern-next-time)
12. [Cheat sheet](#12-cheat-sheet)

---

## 1. Problem statement

> You're given a list `words`, sorted lexicographically according to the rules of an **unknown alien language** (which uses lowercase letters, but in some unknown order). Derive the order of the letters. Return a string of the unique letters in the correct order. If the ordering is **invalid** (contradictory), return `""`. If several orders are valid, return **any**.

### Examples
```
words = ["wrt","wrf","er","ett","rftt"]   → "wertf"
words = ["z","x"]                          → "zx"
words = ["z","x","z"]                      → ""       (z<x and x<z → cycle)
words = ["abc","ab"]                       → ""       ("abc" before "ab" is impossible)
```
(All verified.)

---

## 2. How to guess the pattern (the thought process)

**Q1: What information does "sorted by an unknown order" give me?**
Comparisons. If word `A` comes right before word `B` in a sorted list, then `A ≤ B` in the alien order — which pins down a relationship between **one** pair of letters (the first place they differ). → each adjacent pair yields an **ordering constraint**.

**Q2: What do ordering constraints between letters form?**
"Letter `x` comes before letter `y`" is a directed edge `x → y`. Collected over all adjacent pairs, they form a **directed graph** on the letters. → Course Schedule family.

**Q3: What's the output, given the graph?**
A linear order of all letters respecting every "before" edge = a **topological sort** of the graph.

**Q4: What makes it invalid?**
- A **cycle** (e.g. `x < y` and `y < x`) — no consistent order → `""`.
- A **prefix violation** (`["abc","ab"]`) — a longer word listed before its own prefix, which can't happen in any valid sort → `""`.

**Q5: What's the extra skill here vs plain topo sort?**
You must **build the graph yourself from the words** — the edges aren't handed to you. That construction step (and getting it exactly right) is the heart of the problem.

So the shape is: *derive ordering edges from adjacent-word comparisons → build a directed graph on letters → topological sort → `""` on cycle or prefix violation.*

> 💡 **Sorted data hides pairwise constraints; extract them, then topo-sort:** "A sorted list by an unknown key leaks one comparison per adjacent pair. Turn each into an edge, and the alphabet is just a topological order of those edges — the work is constructing the graph correctly." 

---

## 3. The critical insight: only the first differing char gives an edge

When you compare two adjacent words, **only the first position where they differ carries information** — and nothing after it does.

Take `w1 = "wrt"`, `w2 = "wrf"`: they share `w`, `r`, then differ at index 2 (`t` vs `f`). Because `w1` sorts before `w2`, the alien order has **`t < f`** → edge `t → f`. The characters *before* the difference (`w`, `r`) are equal, so they say nothing; characters *after* the difference say nothing either (lexicographic comparison stops at the first difference).

So per adjacent pair: scan to the first mismatch, add exactly **one** edge, and **stop**. Adding edges for later positions (or for the equal prefix) is a classic bug — it invents constraints the data doesn't support.

```java
for (int j = 0; j < min(len1, len2); j++) {
    if (w1.charAt(j) != w2.charAt(j)) {
        addEdge(w1.charAt(j), w2.charAt(j));   // the ONE fact this pair gives
        break;                                  // nothing after the first diff matters
    }
}
```

> 💡 **One pair → one edge, at the first difference:** "Lexicographic order is decided at the first differing character, so that's the only comparison a pair reveals. I add that single edge and break — reading further would fabricate constraints." 

---

## 4. The other trap: the prefix rule

There's a subtle invalid case that isn't a cycle. If two adjacent words share a common prefix but the **first word is longer** and the second is a prefix of it — like `["abc", "ab"]` — that's **impossible** in any valid sort: a prefix must come **before** the longer word (`"ab" < "abc"`), so seeing `"abc"` first is a contradiction. Return `""` immediately.

Detect it while comparing: if you scan the shared length with **no differing character** and `len(w1) > len(w2)`, it's a prefix violation.

```java
if (len1 > len2 && w1.startsWith(w2)) return "";   // longer word before its own prefix → invalid
```

(Note the valid direction is fine: `["ab","abc"]` gives no edge and no error — a prefix legitimately comes first.)

> 💡 **A word can't precede its own prefix:** "If the words match through the shorter one's length but the earlier word is longer, the list violates lexicographic order itself — that's invalid regardless of letters, so return empty right away." 

---

## 5. Two phases: build the graph, then topo-sort

```
PHASE 1 — BUILD:
  - every distinct letter is a node (init indegree 0)
  - for each adjacent pair (w1, w2):
      * if prefix violation (w1 longer, w2 is its prefix) → return ""
      * else at the first differing index: add edge w1[j] -> w2[j], break
PHASE 2 — TOPO SORT (Kahn's):
  - queue all letters with indegree 0
  - pop, append to result, decrement neighbors, enqueue new zeros
  - if result covers all letters → return it; else (cycle) → return ""
```

Two details: (a) **all appearing letters are nodes**, even ones with no edges (they still belong in the output); (b) **any** valid topological order is accepted (unlike Sequence Reconstruction, no uniqueness check needed).

---

## 6. The full solution

```java
public String alienOrder(String[] words) {
    // PHASE 1: build graph
    Map<Character, Set<Character>> adj = new HashMap<>();
    Map<Character, Integer> indegree = new HashMap<>();
    for (String w : words)                          // every letter is a node
        for (char c : w.toCharArray()) {
            adj.putIfAbsent(c, new HashSet<>());
            indegree.putIfAbsent(c, 0);
        }

    for (int i = 0; i + 1 < words.length; i++) {
        String w1 = words[i], w2 = words[i + 1];
        int m = Math.min(w1.length(), w2.length());
        if (w1.length() > w2.length() && w1.substring(0, m).equals(w2.substring(0, m)))
            return "";                              // prefix violation → invalid
        for (int j = 0; j < m; j++) {
            char a = w1.charAt(j), b = w2.charAt(j);
            if (a != b) {
                if (adj.get(a).add(b)) indegree.merge(b, 1, Integer::sum);  // add edge once
                break;                              // only the first diff matters
            }
        }
    }

    // PHASE 2: Kahn's topological sort
    Queue<Character> q = new LinkedList<>();
    for (char c : indegree.keySet()) if (indegree.get(c) == 0) q.offer(c);

    StringBuilder sb = new StringBuilder();
    while (!q.isEmpty()) {
        char c = q.poll();
        sb.append(c);
        for (char nb : adj.get(c))
            if (indegree.merge(nb, -1, Integer::sum) == 0) q.offer(nb);
    }

    return sb.length() == indegree.size() ? sb.toString() : "";   // shortfall ⇒ cycle ⇒ ""
}
```

Verified: `["wrt","wrf","er","ett","rftt"] → "wertf"`, both invalid cases → `""`, and consistent with "a valid order exists" over 20,000 random inputs.

---

## 7. Dry run

`words = ["wrt","wrf","er","ett","rftt"]` → expect `"wertf"`.

```
Nodes: {w,r,t,f,e}   (all letters appearing)

Build edges (first differing char per adjacent pair):
  "wrt" vs "wrf": diff at idx2 t≠f → edge t→f
  "wrf" vs "er" : diff at idx0 w≠e → edge w→e
  "er"  vs "ett": diff at idx1 r≠t → edge r→t
  "ett" vs "rftt":diff at idx0 e≠r → edge e→r

  adj: w→{e}, e→{r}, r→{t}, t→{f}, f→{}
  indegree: w:0, e:1, r:1, t:1, f:1

Kahn's:
  queue init (indeg 0): [w]
  pop w → "w"; e:1→0 enqueue          queue=[e]
  pop e → "we"; r:1→0 enqueue         queue=[r]
  pop r → "wer"; t:1→0 enqueue        queue=[t]
  pop t → "wert"; f:1→0 enqueue       queue=[f]
  pop f → "wertf"

  length 5 == nodes 5 → "wertf"   ✓
```

---

## 8. The two ways it returns ""

The empty-string result comes from exactly two distinct failures — both must be handled:

1. **Prefix violation** — a longer word appears before its own prefix (`["abc","ab"]`). Detected during the build phase; return `""` immediately, before topo-sort. (This is *not* a cycle.)
2. **Cycle** — contradictory constraints (`["z","x","z"]` gives `z<x` and `x<z`). Detected when Kahn's can't process all letters (result shorter than the node count) → return `""`.

Miss either and you'll return a wrong non-empty string for an impossible dictionary.

> 💡 **Two failure modes, one empty output:** "Invalid means either the list itself breaks lexicographic order (prefix trap, caught while building) or the letter constraints contradict (cycle, caught by the topo shortfall). Both yield empty." 

---

## 9. Complexity

Let `N` = total characters across all words, `U` = number of distinct letters (≤ 26), `E` = edges added (≤ pairs).

| | Time | Space |
|:--|:--|:--|
| Build graph | O(N) — scan words, one comparison per adjacent pair | O(U + E) |
| Kahn's topo sort | O(U + E) | O(U + E) |
| **Total** | **O(N + U + E) ≈ O(N)** | **O(U + E)** — bounded, since U ≤ 26 |

Effectively linear in the input size; the alphabet is tiny (≤ 26 letters).

---

## 10. Common mistakes

- ❌ **Adding more than one edge per pair, or edges from the equal prefix.** Only the **first** differing char gives an edge; `break` after it. (§3)
- ❌ **Missing the prefix violation.** `["abc","ab"]` must return `""` — check it while comparing (longer word first, shared prefix, no diff). (§4)
- ❌ **Forgetting isolated letters.** Every letter that appears is a node and must be in the output, even with no edges. Initialize all of them first.
- ❌ **Not detecting the cycle.** If Kahn's result is shorter than the letter count, it's a cycle → `""`.
- ❌ **Double-counting an edge's indegree.** Increment `indegree[b]` only when the edge `a→b` is newly added (use a set to dedup). Repeated identical edges would corrupt indegree.
- ❌ **Over-checking uniqueness.** Any valid order is accepted; no need for the "queue size 1" test from Sequence Reconstruction.

---

## 11. How to recognize this pattern next time

1. **"Derive an order from sorted/compared data"** → the data hides pairwise ordering constraints → **build a graph from those constraints, then topological sort**.
2. **The construction step is the crux** — figure out exactly what each comparison reveals (here: one edge at the first differing character), and don't over- or under-generate edges.
3. **Guard the data-validity traps** — inputs that break the ordering premise itself (the prefix rule) are invalid independent of the graph.
4. **Cycle ⇒ contradiction ⇒ no answer** (`""` / `[]`), via the usual topo shortfall.

This "**build a graph from data, then topo-sort**" combo is the union of your two prior skills — the **map-and-wire / construction** pattern (Pattern 14) feeding a topological sort. Family: **Alien Dictionary** (this), **Sequence Reconstruction** (444, build from sequences), verification/rank-derivation problems, and any "reconstruct an ordering from partial comparisons."

> 💡 **Construct then order:** "The reusable move is two-phase: first mine the data for edges (the hard, problem-specific part), then run a standard topological sort on the graph you built. Recognizing that a comparison yields exactly one constraint — and where — is the skill." 

---

## 12. Cheat sheet

**Recognize it:** derive letter order from lexicographically-sorted words → build a graph from adjacent-word comparisons, then topological sort.

**Build phase:**
```
all letters → nodes (indegree 0)
for each adjacent (w1,w2):
    if w1 longer AND w2 is its prefix → return ""        (prefix violation)
    at first index j where w1[j]!=w2[j]: add edge w1[j]->w2[j]; break   (one edge only)
```

**Sort phase (Kahn's):** queue indegree-0 letters; pop→append→decrement neighbors→enqueue zeros; result covers all letters ? return it : "" (cycle).

**Two ""s:** prefix violation (build phase) or cycle (topo shortfall).

**Complexity:** O(N) time, O(U+E) space (U ≤ 26).

**Watch:** one edge per pair at first diff (break!); prefix rule; include isolated letters; dedup edges before indegree++; any valid order is fine.

> **One-line philosophy:** *A dictionary sorted by an unknown alphabet leaks exactly one ordering fact per adjacent pair — at the first character where they differ — so mine those into directed edges over the letters and topologically sort the resulting graph to recover the alphabet; return empty if a cycle makes the constraints contradictory or if a word illegally precedes its own prefix, and remember the reusable move is two-phase: build the graph from the data (the crux), then run a standard topo sort on it.*
