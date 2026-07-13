# Satisfiability of Equality Equations (LeetCode 990) — Union-Find, with Full Pattern Recognition

> Like Accounts Merge, the challenge is *spotting the pattern*. Here there are two kinds of statements — `==` (equal) and `!=` (not equal). The trick: **`==` is a transitive "same group" relation → connected components → Union-Find**, and **`!=` is a constraint you validate afterward**. The one subtlety unique to this problem is **order**: you must union *all* equalities first, then check *all* inequalities. Same map-based Union-Find style (parent map, `-1` = root, recursive `findSet` with path compression, `unionSet` by size). All code verified, including a demo of why the order matters.

> 💡 **The whole solution in one sentence:** treat each variable as a node; process every `==` equation by **unioning** its two variables (equality is transitive, so equal variables collapse into one set — an equivalence class); then scan every `!=` equation and **fail if its two variables landed in the same set**, because that would demand two things be simultaneously equal and unequal — and if no `!=` is violated, the assignment is possible, so return true.

---

## Table of Contents
1. [Problem statement](#1-problem-statement)
2. [How to guess the pattern (the thought process)](#2-how-to-guess-the-pattern-the-thought-process)
3. [The critical insight: equality is an equivalence relation](#3-the-critical-insight-equality-is-an-equivalence-relation)
4. [The other critical insight: order matters (two passes)](#4-the-other-critical-insight-order-matters-two-passes)
5. [The nodes (just 26 letters)](#5-the-nodes-just-26-letters)
6. [The plan](#6-the-plan)
7. [The Union-Find pieces](#7-the-union-find-pieces)
8. [The full solution](#8-the-full-solution)
9. [Dry run](#9-dry-run)
10. [Why interleaving breaks (a concrete failure)](#10-why-interleaving-breaks-a-concrete-failure)
11. [Complexity](#11-complexity)
12. [Common mistakes](#12-common-mistakes)
13. [How to recognize this pattern next time](#13-how-to-recognize-this-pattern-next-time)
14. [Cheat sheet](#14-cheat-sheet)

---

## 1. Problem statement

> You're given `equations`, an array of strings, each of length 4 in one of two forms: `"a==b"` or `"a!=b"`, where `a` and `b` are single lowercase letters (variables). Return **true** if it's possible to assign integer values to the variables so that **all** equations are satisfied simultaneously; otherwise **false**.

### Examples
```
["a==b","a!=b"]           → false   (a==b says equal; a!=b says not — impossible)
["b==a","a==b"]           → true
["a==b","b==c","a==c"]    → true    (all consistent)
["a==b","b!=c","c==a"]    → false   (a,b,c forced equal, but b!=c contradicts)
["c==c","b==d","x!=z"]    → true
```
(All verified.)

---

## 2. How to guess the pattern (the thought process)

The same recognition questions as Accounts Merge, adapted:

**Q1: Is there a "same group" relation, and is it transitive?**
`==` says two variables are equal. Equality is transitive: `a==b` and `b==c` force `a==c`. → **Transitive grouping ⇒ connected components ⇒ Union-Find.**

**Q2: What are the nodes?**
The **variables** (single lowercase letters). There are at most 26. Easy — the nodes are given directly.

**Q3: What's an edge (a union)?**
Each `==` equation. Union the two variables it names.

**Q4: What's *different* here from a plain grouping problem?**
There's a **second kind of statement** — `!=` — that isn't an edge; it's a **constraint to check**. `a!=b` is satisfiable **only if** `a` and `b` end up in **different** sets. So after grouping by equality, you validate every inequality against the groups.

**Q5: Does order matter?**
Yes (this is the unique subtlety) — you must build **all** the equality groups **before** checking any inequality, because a later `==` could merge two variables that an earlier `!=` referenced. → **Two passes: unions first, checks second.** (§4)

So the shape is: *equalities build equivalence classes (Union-Find); inequalities are consistency checks against those classes; process equalities fully before checking inequalities.*

> 💡 **Two statement types → two roles:** "`==` builds the groups (unions); `!=` tests the groups (a cross-group check). The recognition is 'transitive equality = connected components,' and the twist is that a second relation validates the result — done in a strict second pass." 

---

## 3. The critical insight: equality is an equivalence relation

Why is Union-Find *the* tool here (not a coincidence)? Because mathematical **equality is an equivalence relation** — it's reflexive (`a==a`), symmetric (`a==b ⇒ b==a`), and transitive (`a==b, b==c ⇒ a==c`). An equivalence relation partitions items into **equivalence classes** (disjoint groups where everything in a class is mutually equal). And **Union-Find's entire purpose is to maintain disjoint sets / equivalence classes.** They're the same concept.

So every `==` equation says "these two variables are in the same equivalence class" → union them. After processing all `==`, each Union-Find set is exactly one equivalence class: a maximal group of variables that must all hold the same value.

Then an inequality `a!=b` is satisfiable **iff `a` and `b` are in different classes** — because within one class every variable must be equal, so demanding two members be unequal is a flat contradiction.

> 💡 **Equality = equivalence relation = disjoint sets = Union-Find:** "Union-Find *is* the data structure for equivalence classes, and `==` is a textbook equivalence relation, so the mapping is exact: union equal variables, and each resulting set is one class of mutually-equal variables. An inequality inside a class is impossible." 

---

## 4. The other critical insight: order matters (two passes)

You **must process all `==` before checking any `!=`.** Here's why, with a concrete case (verified):

```
equations = ["a!=b", "a==c", "c==b"]
```
- The `a==c` and `c==b` equalities force **a, c, b into one class** (all equal).
- So `a!=b` is a **contradiction** → the answer is **false**.

But if you processed the list *in order, checking as you go*:
- You'd hit `"a!=b"` **first**, when `a` and `b` are still in separate sets (no unions yet) → the check passes.
- Then you'd union a-c and c-b — too late; you already wrongly accepted the inequality.
- You'd return **true** — **wrong.**

(Verified: interleaved processing returns `true`; the correct two-pass returns `false`.)

The fix is structural: **Pass 1 does all the unions; Pass 2 does all the checks.** Only after the equivalence classes are fully built can an inequality be judged, because the classes might still be growing.

> 💡 **Build all groups before testing any constraint:** "An inequality can only be judged against the *final* equivalence classes, and a class can keep growing as later equalities arrive — so I union every `==` first, then validate every `!=`. Checking mid-stream risks judging against a half-built group." This ordering point is the single most common way to get this problem wrong. 

---

## 5. The nodes (just 26 letters)

Variables are single lowercase letters `'a'`–`'z'`, so there are at most **26 nodes**. Map each letter to an index `0–25` via `c - 'a'`, and initialize a 26-entry Union-Find:

```java
for (int i = 0; i < 26; i++) { parent.put(i, -1); rank.put(i, 1); }
```

Every variable starts in its own set (its own value). Parsing each equation: it's exactly 4 characters — `equation.charAt(0)` and `charAt(3)` are the two variables, and `charAt(1)` is `'='` for `==` or `'!'` for `!=`.

---

## 6. The plan

```
1. INIT   26 letters, each its own set (parent = -1, rank = 1).
2. UNION  Pass 1 — for every "x==y", unionSet(x, y).   (build equivalence classes)
3. CHECK  Pass 2 — for every "x!=y", if findSet(x) == findSet(y) → return false.
4. If no inequality is violated → return true.
```

---

## 7. The Union-Find pieces

Same engine as the Provinces / Accounts-Merge docs — unchanged.

```java
private int findSet(int i, Map<Integer, Integer> parent) {
    if (parent.get(i) == -1) return i;
    int root = findSet(parent.get(i), parent);
    parent.put(i, root);                       // path compression
    return root;
}
private void unionSet(int x, int y,
                      Map<Integer, Integer> parent, Map<Integer, Integer> rank) {
    int s1 = findSet(x, parent), s2 = findSet(y, parent);
    if (s1 != s2) {
        if (rank.get(s1) < rank.get(s2)) { parent.put(s1, s2); rank.put(s2, rank.get(s1) + rank.get(s2)); }
        else                             { parent.put(s2, s1); rank.put(s1, rank.get(s1) + rank.get(s2)); }
    }
}
```

---

## 8. The full solution

```java
class Solution {
    public boolean equationsPossible(String[] equations) {
        Map<Integer, Integer> parent = new HashMap<>();
        Map<Integer, Integer> rank   = new HashMap<>();
        for (int i = 0; i < 26; i++) { parent.put(i, -1); rank.put(i, 1); }

        // PASS 1: process all "==" → union (build equivalence classes)
        for (String eq : equations) {
            if (eq.charAt(1) == '=') {                       // "x==y"
                int x = eq.charAt(0) - 'a';
                int y = eq.charAt(3) - 'a';
                unionSet(x, y, parent, rank);
            }
        }

        // PASS 2: process all "!=" → the two vars must be in DIFFERENT sets
        for (String eq : equations) {
            if (eq.charAt(1) == '!') {                       // "x!=y"
                int x = eq.charAt(0) - 'a';
                int y = eq.charAt(3) - 'a';
                if (findSet(x, parent) == findSet(y, parent)) {
                    return false;                            // contradiction: equal AND not equal
                }
            }
        }
        return true;                                         // all constraints satisfiable
    }

    private int findSet(int i, Map<Integer, Integer> parent) {
        if (parent.get(i) == -1) return i;
        int root = findSet(parent.get(i), parent);
        parent.put(i, root);
        return root;
    }
    private void unionSet(int x, int y, Map<Integer, Integer> parent, Map<Integer, Integer> rank) {
        int s1 = findSet(x, parent), s2 = findSet(y, parent);
        if (s1 != s2) {
            if (rank.get(s1) < rank.get(s2)) { parent.put(s1, s2); rank.put(s2, rank.get(s1) + rank.get(s2)); }
            else                             { parent.put(s2, s1); rank.put(s1, rank.get(s1) + rank.get(s2)); }
        }
    }
}
```

Verified against every LeetCode example and edge cases (`"a!=a"` → false; two separate classes with `!=` across them → true).

---

## 9. Dry run

`equations = ["a==b","b==c","a!=c"]` → expect **false**.

```
Init: a(0),b(1),c(2) each own set.  parent = {0:-1,1:-1,2:-1,...}

PASS 1 (unions on "=="):
  "a==b": unionSet(0,1) → parent{1:0}, rank{0:2}          set {a,b}
  "b==c": unionSet(1,2) → findSet(1)=0, findSet(2)=2, union → parent{2:0}, rank{0:3}   set {a,b,c}
  (no more "==")

PASS 2 (checks on "!="):
  "a!=c": findSet(0)=0, findSet(2)=0 → SAME set → return false   ✓
```

`equations = ["a==b","c==d","b!=c"]` → expect **true**:
```
PASS 1: union(a,b) → {a,b};  union(c,d) → {c,d}
PASS 2: "b!=c": findSet(b)=root{a,b}, findSet(c)=root{c,d} → DIFFERENT → ok
        return true   ✓
```

---

## 10. Why interleaving breaks (a concrete failure)

Restating §4 with the trace, because it's the crux:

`equations = ["a!=b", "a==c", "c==b"]` → correct answer **false** (a,c,b all forced equal, so a!=b is impossible).

| Approach | What happens | Result |
|:---------|:-------------|:-------|
| **Two-pass (correct)** | Pass 1 unions a-c and c-b → {a,b,c}. Pass 2 checks `a!=b`: same set → **false** | ✓ false |
| **Interleaved (wrong)** | Sees `a!=b` first, when a,b still separate → passes. Then unions. Never re-checks | ✗ true |

Verified. The lesson: **an inequality must be tested against the *completed* equivalence classes**, so all unions come first.

---

## 11. Complexity

Let `E` = number of equations.

| | Cost |
|:--|:--|
| **Time** | **O(E · α(26)) ≈ O(E)** — two linear passes; each Union-Find op is near-constant (path compression + union by size, over ≤ 26 nodes) |
| **Space** | **O(1)** — the Union-Find is fixed at 26 nodes regardless of input size |

Effectively linear in the number of equations, with constant extra space (only 26 possible variables).

---

## 12. Common mistakes

- ❌ **Checking `!=` in the same pass as `==` (interleaving).** The #1 bug — a later `==` can merge variables an earlier `!=` referenced. **Union all equalities first, then check.** (§4, §10)
- ❌ **Treating `!=` as a union.** Inequality is a *constraint to verify*, not an edge to merge. You never union on `!=`.
- ❌ **Parsing the operator wrong.** The string is 4 chars; `charAt(1)` is `'='` for `==` and `'!'` for `!=`; the variables are `charAt(0)` and `charAt(3)`.
- ❌ **Forgetting `a!=a`.** A variable can't differ from itself: `findSet(a)==findSet(a)` is always true → correctly returns false. (Don't special-case it away.)
- ❌ **Comparing raw variables instead of roots** in the `!=` check — must be `findSet(x) == findSet(y)`, not `x == y`.
- ❌ **Over-sizing the structure.** Only 26 nodes exist; no need for dynamic sizing.

---

## 13. How to recognize this pattern next time

Extending the checklist from Accounts Merge:

1. **Transitive "same/equal" relation?** → connected components / equivalence classes → **Union-Find**.
2. **A second relation that says "must be different / must not conflict"?** → that's a **constraint check** done *after* the grouping, not a union.
3. **Could grouping still change after a constraint appears?** → then **two passes**: build all groups first, validate constraints second.

This "group with `==`, then validate `!=`" shape shows up in constraint-consistency problems generally (equality/inequality systems, type unification, "are these merges consistent?"). The moment you see **equality statements mixed with inequality/conflict statements**, think: *union the equalities into classes, then check no conflict lands inside a class.*

> 💡 **Group the "must-be-same," then test the "must-differ":** "Positive relations (equal) build the sets; negative relations (not-equal) are validated against the finished sets. Recognizing which statements *build* structure vs. which *test* it — and doing them in that order — is the transferable idea." 

---

## 14. Cheat sheet

**Recognize it:** `==` is transitive equality → equivalence classes → Union-Find; `!=` is a constraint checked afterward.

**Recipe (two passes — order is essential):**
```
1. INIT   26 letters, each its own set.
2. PASS 1 union every "x==y".                         ← build all equivalence classes
3. PASS 2 for every "x!=y": if findSet(x)==findSet(y) → return false.
4. return true.
```

**Union-Find engine (unchanged):** parent map (`-1`=root), recursive `findSet` w/ path compression, `unionSet` by size.

**Watch:** never interleave (union all `==` before checking any `!=`); `!=` is a check, not a union; parse operator via `charAt(1)`; compare roots not letters; `a!=a` → false.

**Complexity:** O(E) time, O(1) space (26 fixed nodes).

> **One-line philosophy:** *Equality is an equivalence relation, and Union-Find is the data structure for equivalence classes — so union every `==` to collapse mutually-equal variables into disjoint classes, then validate every `!=` by checking its two variables landed in different classes; because a class can keep growing as more equalities arrive, you must build all the classes first and test the inequalities second — the reusable skill is seeing which statements build the groups and which merely test them, and honoring that order.*
