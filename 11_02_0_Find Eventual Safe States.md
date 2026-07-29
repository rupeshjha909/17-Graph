# Find Eventual Safe States (LeetCode 802) — Explained Simply

The whole problem in one line: **a node is "safe" if you can never get stuck walking in circles from it — every route out eventually hits a dead end.** Find all such nodes.

---

## Table of Contents
1. [The picture in your head](#1-the-picture-in-your-head)
2. [What "safe" really means](#2-what-safe-really-means)
3. [The one trick: three colours](#3-the-one-trick-three-colours)
4. [The algorithm in plain English](#4-the-algorithm-in-plain-english)
5. [The code](#5-the-code)
6. [Watch it run (step by step)](#6-watch-it-run-step-by-step)
7. [The one rule people get wrong](#7-the-one-rule-people-get-wrong)
8. [Complexity](#8-complexity)
9. [30-second recap](#9-30-second-recap)

---

## 1. The picture in your head

Forget graphs for a second. Think of an **escape room building**:

- Every **room** is a node.
- Every **one-way door** is an arrow (`graph[i]` = the doors leading out of room `i`).
- A room with **no doors out** is an **exit** (a "terminal" node).

Now the question:

> Standing in a room, if you keep walking through doors, are you *guaranteed* to reach an exit — no matter which doors you pick? Or is there some route that traps you walking in a loop forever?

- If **every** possible walk leads you to an exit → the room is **SAFE**.
- If **even one** walk can trap you in a loop → the room is **UNSAFE**.

That's the entire problem. We just need to find every safe room.

```
Example building:

   0 ──► 1 ──► 3
   │     │     │
   ▼     ▼     │
   2     2     ▼
   │           0     ◄── door from 3 goes back to 0  (LOOP!)
   ▼
   5 (exit)          4 ──► 5 (exit)      6 (exit)

graph = [[1,2], [2,3], [5], [0], [5], [], []]
```

Rooms **5** and **6** have no doors → they're exits → safe.
Room **0 → 3 → 0** is a loop. Anyone who can reach that loop is unsafe.

Answer: safe rooms are **[2, 4, 5, 6]**.

---

## 2. What "safe" really means

Here is the recursive definition — this is the heart of everything:

1. An **exit** (no doors out) is **safe**. Nowhere to walk, so you can't get trapped.
2. Any other room is **safe *only if every single door* leads to a safe room.**
3. A room is **unsafe** if it sits on a loop, or if *any* door eventually reaches a loop.

Read rule 2 again slowly, because it's the part that trips everyone up:

> A room needs **ALL** its doors to be safe. Not *one* of them — **all** of them.

Why? Because "safe" means *every* walk must escape. If even one door leads somewhere dangerous, you could pick that door and get trapped. One bad door poisons the whole room.

```
Room A has 3 doors → B (safe), C (safe), D (UNSAFE)
  Is A safe?  NO.
  Because you could choose door D and get trapped.
  One bad exit is enough to condemn A.
```

Keep this "**ALL doors, not ANY door**" idea in your pocket. It's the single most important line in the problem.

---

## 3. The one trick: three colours

To check a room, we walk out of it (that's DFS — go deep down one path, then back up). While walking, we need to answer one question: **"have I looped back onto my own path?"** Because that means a cycle.

We track this by painting each room one of **three colours**:

| Colour | Meaning | Plain English |
|--------|---------|---------------|
| **WHITE** (0) | unvisited | "haven't looked at this room yet" |
| **GRAY** (1) | on my current path | "I'm standing here right now / it's a breadcrumb on my current walk" |
| **BLACK** (2) | done, proven safe | "already fully checked — this room is safe" |

The magic is the GRAY colour. Think of GRAY rooms as **breadcrumbs you drop as you walk forward**:

```
Start walk:   drop a breadcrumb (GRAY) in every room as you enter it
Reach exit:   great, walk back, turn those rooms BLACK (safe)
Hit a GRAY:   STOP — you just walked into a room that still has YOUR
              breadcrumb → you walked in a circle → LOOP → unsafe!
```

- Walk into a **GRAY** room → you looped → **cycle → unsafe.**
- Walk into a **BLACK** room → already known safe → no danger there, keep going.
- Walk into a **WHITE** room → unknown → go explore it.

That's why a plain "visited / not visited" flag isn't enough: it can't tell the difference between "a room I already finished and proved safe" (BLACK) and "a room still on my current path" (GRAY). Only the GRAY vs BLACK distinction reveals the loop.

---

## 4. The algorithm in plain English

To check one room:

```
isSafe(room):
    1. If it's GRAY  → I've looped back onto myself → NOT safe. Stop.
    2. If it's BLACK → already proven safe → return safe. Stop.
    3. Otherwise (WHITE): paint it GRAY (drop a breadcrumb, "I'm exploring this").
    4. Try every door:
         if any door leads to a room that is NOT safe → this room is NOT safe.
    5. Survived all doors → paint it BLACK (safe) → return safe.
```

Then just run `isSafe` on every room and collect the ones that come back safe. Because we loop `i = 0, 1, 2, …`, the answer comes out sorted for free.

Notice how clean this is: **the function literally answers the question "Is this room safe?"** — `true` means safe, `false` means unsafe. No mental gymnastics.

---

## 5. The code

```java
public List<Integer> eventualSafeNodes(int[][] graph) {
    int n = graph.length;
    int[] color = new int[n];              // 0 = WHITE, 1 = GRAY, 2 = BLACK
    List<Integer> res = new ArrayList<>();
    for (int i = 0; i < n; i++) {
        if (isSafe(i, graph, color)) {     // ask the question for every room
            res.add(i);                    // safe → keep it (already in ascending order)
        }
    }
    return res;
}

// returns TRUE if `node` is safe, FALSE if it's unsafe
private boolean isSafe(int node, int[][] graph, int[] color) {
    if (color[node] == 1) return false;    // GRAY: looped back onto my own path → unsafe
    if (color[node] == 2) return true;     // BLACK: already proven safe

    color[node] = 1;                       // paint GRAY: "I'm exploring this one"
    for (int door : graph[node]) {
        if (!isSafe(door, graph, color)) { // one door leads somewhere unsafe...
            return false;                  // ...so this whole room is unsafe
        }
    }
    color[node] = 2;                       // all doors safe → paint BLACK (safe)
    return true;
}
```

Read it top to bottom and it matches the plain-English version line for line. The function name `isSafe` and its return value mean the same thing everywhere: **true = safe.** That consistency is what makes this version easy to hold in your head.

> **Note on the earlier "returns true = cycle" version:** that one worked too, but it flipped the meaning of the return value (true meant *unsafe*), which forced you to collect answers by checking colours afterward instead of trusting the return value. That flip is exactly what made it confusing. This `isSafe` version keeps one consistent meaning, so prefer it while you're learning.

---

## 6. Watch it run (step by step)

`graph = [[1,2],[2,3],[5],[0],[5],[],[]]` → expect `[2,4,5,6]`.

We call `isSafe` for room 0 first. Watch the colours change. `[W W W W W W W]` = all white to start.

```
isSafe(0): paint 0 GRAY          colours: [G W W W W W W]
  door 0→1:
  isSafe(1): paint 1 GRAY        colours: [G G W W W W W]
    door 1→2:
    isSafe(2): paint 2 GRAY      colours: [G G G W W W W]
      door 2→5:
      isSafe(5): 5 has NO doors  → loop does nothing → paint 5 BLACK, return SAFE
                                 colours: [G G G W W B W]
      2's only door was safe → paint 2 BLACK, return SAFE
                                 colours: [G G B W W B W]
    door 1→3:
    isSafe(3): paint 3 GRAY      colours: [G G B G W B W]
      door 3→0:
      isSafe(0): color[0] == GRAY  → LOOP! return UNSAFE  ✗
      3 has an unsafe door → 3 is UNSAFE, return false   (3 stays GRAY, never turns BLACK)
    1 has an unsafe door (door to 3) → 1 is UNSAFE, return false
  0 has an unsafe door (door to 1) → 0 is UNSAFE, return false

Result so far: 0 unsafe, 1 unsafe, 3 unsafe. 2 and 5 are BLACK (safe).
```

Now the outer loop continues to the next rooms:

```
isSafe(1): still GRAY from before → returns false → unsafe   (skip)
isSafe(2): BLACK → returns true  → SAFE ✓   → add 2
isSafe(3): still GRAY → returns false → unsafe   (skip)
isSafe(4): paint GRAY, door 4→5 is BLACK/safe → paint 4 BLACK → SAFE ✓ → add 4
isSafe(5): BLACK → SAFE ✓ → add 5
isSafe(6): no doors → SAFE ✓ → add 6
```

Collected safe rooms: **[2, 4, 5, 6]** ✓

The key moment is the line `isSafe(0): color[0] == GRAY → LOOP!`. We were walking `0 → 1 → 3` and door `3 → 0` sent us back to a room whose breadcrumb (GRAY) was still down. That's the loop `0 → 3 → 0`, and it poisons everyone who can reach it: rooms 0, 1, and 3.

---

## 7. The one rule people get wrong

The mistake that breaks most first attempts:

> ❌ "A room is safe if **some** door leads to safety."
> ✅ "A room is safe only if **every** door leads to safety."

This problem is *not* like "can I reach the exit?" (where one good path is enough). Here **every** path must be good, because you don't control which door gets taken — if any door can trap you, the room is unsafe.

In the code, this shows up as: the moment `isSafe(door)` returns false for *any* door, we immediately `return false`. We only paint the room BLACK (safe) *after* the loop finishes with zero bad doors.

A couple of other slip-ups worth remembering:

- Using a plain visited flag instead of three colours — you lose the GRAY/BLACK distinction and can't detect the loop.
- Painting a room BLACK too early (inside the loop, before all doors are checked) — mark it safe only *after* every door passes.

---

## 8. Complexity

Let `V` = rooms (nodes), `E` = doors (edges).

- **Time: O(V + E).** Each room gets painted GRAY once and BLACK at most once; each door is walked once. The colours act as memoization — a BLACK room is never re-explored.
- **Space: O(V + E).** The colour array plus the recursion stack (which can be as deep as V in a long chain).

---

## 9. 30-second recap

- **Goal:** find rooms where *every* walk is guaranteed to reach a dead-end exit (no loops).
- **Safe rule:** a room is safe ⇔ **all** its doors lead to safe rooms. Exits are safe. Loops are unsafe.
- **Three colours:** WHITE = unseen, GRAY = on my current path (breadcrumb), BLACK = proven safe.
- **The loop check:** walking into a GRAY room = you circled back = cycle = unsafe.
- **`isSafe(node)` returns true = safe.** Collect every room that returns true; they come out sorted.

```
isSafe(node):
    GRAY?  → false (looped → unsafe)
    BLACK? → true  (already safe)
    paint GRAY
    for each door: if !isSafe(door) → return false   // ALL doors must be safe
    paint BLACK
    return true
```
