# Accounts Merge (LeetCode 721) — Union-Find, with Full Pattern Recognition

> This problem is *hard to recognize* but *easy to code once you see it*. So most of this guide is about **how to figure out that it's a Union-Find / connected-components problem** — the mental steps to get from the messy word problem to "oh, this is just grouping connected things." Then we solve it in the same map-based Union-Find style (parent map with `-1` = root, recursive `findSet` with path compression, `unionSet` by size). All code verified.

> 💡 **The whole solution in one sentence:** the real "nodes" aren't the accounts — they're the **emails**; two emails belong to the same person if they ever appear in the same account, so you **union all emails within each account**, and because union merges transitively, every email that's directly-or-indirectly linked collapses into one set — then you **group emails by their root**, sort each group, and prepend the name to get one merged account per set.

---

## Table of Contents
1. [Problem statement](#1-problem-statement)
2. [Why it's hard to "see" the pattern](#2-why-its-hard-to-see-the-pattern)
3. [How to guess the pattern (the thought process)](#3-how-to-guess-the-pattern-the-thought-process)
4. [The critical insight: emails are the nodes, not accounts](#4-the-critical-insight-emails-are-the-nodes-not-accounts)
5. [The 4-step plan](#5-the-4-step-plan)
6. [The Union-Find pieces](#6-the-union-find-pieces)
7. [The full solution](#7-the-full-solution)
8. [Dry run](#8-dry-run)
9. [The tricky bits (same name, sorting, name lookup)](#9-the-tricky-bits-same-name-sorting-name-lookup)
10. [Complexity](#10-complexity)
11. [Common mistakes](#11-common-mistakes)
12. [DFS alternative (for contrast)](#12-dfs-alternative-for-contrast)
13. [How to recognize this pattern next time](#13-how-to-recognize-this-pattern-next-time)
14. [Cheat sheet](#14-cheat-sheet)

---

## 1. Problem statement

> You're given a list `accounts`, where `accounts[i] = [name, email1, email2, ...]`. Two accounts **definitely belong to the same person** if they share **at least one common email** — even if the name is the same, they might be different people, and even different accounts (with the same name) are the same person if they share any email. Merge the accounts: return a list where each entry is `[name, ...sorted unique emails...]`. Emails in each merged account must be in **sorted order**. The answer can be in any order.

### Example
```
accounts = [["John","johnsmith@mail.com","john_newyork@mail.com"],
            ["John","johnsmith@mail.com","john00@mail.com"],
            ["Mary","mary@mail.com"],
            ["John","johnnybravo@mail.com"]]

Output = [["John","john00@mail.com","john_newyork@mail.com","johnsmith@mail.com"],
          ["Mary","mary@mail.com"],
          ["John","johnnybravo@mail.com"]]
```
The first two Johns share `johnsmith@mail.com`, so they're one person (three emails merged). The `johnnybravo` John shares nothing → separate. Mary → separate. (Verified.)

---

## 2. Why it's hard to "see" the pattern

The problem is dressed up in real-world language — "accounts," "names," "merge" — and none of the usual graph words ("nodes," "edges," "connected," "components") appear. So it doesn't *look* like a graph problem. Two more things make it slippery:

- **The name is a decoy.** Same name doesn't mean same person; different-looking accounts can be the same person. So you can't group by name.
- **"Same person" is transitive.** If account A shares an email with B, and B shares a (different) email with C, then A, B, C are all the same person — even though A and C might share nothing directly. That word "transitive" is the tell.

Whenever "belongs to the same group" is **transitive** (A~B and B~C ⟹ A~C), you are almost certainly looking at **connected components**, and Union-Find (or DFS/BFS) is the tool.

> 💡 **"Transitive grouping" = connected components:** "The moment I notice that 'same person' spreads transitively through shared emails, I stop thinking about accounts and names and start thinking 'group everything that's transitively connected' — that's the definition of a connected component, which means Union-Find." 

---

## 3. How to guess the pattern (the thought process)

Here's the actual chain of questions to ask yourself — this is the reusable skill:

**Q1: Am I grouping things where membership is transitive?**
Yes — "same person" spreads through shared emails. → *This is a connected-components problem.* → *Use Union-Find or DFS.*

**Q2: What exactly are the "things" being grouped (the nodes)?**
This is the crux and where most people go wrong. Two candidates: **accounts** or **emails**. Ask: *what is the unit that connects two groups?* A **shared email** is what links two accounts. So the natural, clean choice is: **the emails are the nodes.** Two emails are "connected" if they appear together in some account. (You *can* make accounts the nodes, but then you need an email→account index to find edges — messier. Emails-as-nodes is cleaner; see §4.)

**Q3: What's an edge (when do I union two nodes)?**
Two emails in the **same account** are connected — they clearly belong to the same person. So within each account, union all its emails together.

**Q4: What does a connected component represent?**
All emails transitively reachable from each other = **one person's complete set of emails** = one merged account.

**Q5: What do I still need besides the grouping?**
- The **name** for each component (any email in it works, since all belong to one person).
- The emails **sorted** within each merged account (the problem demands it).

That's the entire solution, derived from five questions. The pattern is **"transitive grouping → connected components → Union-Find,"** and the only real puzzle is Q2 (choosing emails as nodes).

> 💡 **The recognition drill:** "Transitive 'same group'? → connected components. Then: *what are the nodes, and what's an edge?* Here nodes = emails, edge = 'share an account.' Everything else (name, sorting) is bookkeeping on top of the components." 

---

## 4. The critical insight: emails are the nodes, not accounts

This is the single thing that makes the problem click. Beginners instinctively treat each **account** as a node and try to connect accounts that share emails — but then finding those shared-email edges requires building an email→accounts index first, and you end up doing extra work.

Instead, treat each **email** as a node:
- Two emails in the same account → connect them (union). That's a direct, obvious edge, right there in the input.
- Transitivity does the rest: if `johnsmith` links account 1's emails together, and account 2 also contains `johnsmith`, then account 2's emails join the same component automatically — because `johnsmith` is the shared node bridging them.

So a **shared email is literally the bridge node** that merges two accounts, with zero extra indexing. The emails you were given *are* the graph.

```
account1: johnsmith — john_newyork          (union johnsmith, john_newyork)
account2: johnsmith — john00                 (union johnsmith, john00)
                ↑ shared node bridges the two accounts
result component: { johnsmith, john_newyork, john00 }   → one person
```

> 💡 **Let the shared email be the bridge:** "By making emails the nodes, a shared email is automatically the connecting node between two accounts — no separate edge-finding step. The input's own structure (emails grouped by account) is the edge list." 

---

## 5. The 4-step plan

```
STEP 1  Index: give every unique email an integer id; remember each email's name.
STEP 2  Union: for each account, union its first email with every other email in it.
STEP 3  Group: for each email, find its root; bucket emails by root (one bucket = one person).
STEP 4  Build: for each bucket → sort emails, prepend the name → one merged account.
```

- **Step 1** turns string emails into integer ids so we can reuse the integer-indexed Union-Find (`parent`/`rank` maps keyed by id). It also records `emailToName` (any account containing the email gives the name).
- **Step 2** is the graph construction: all emails in one account are mutually connected, and unioning the first with each of the others is enough (union is transitive, so they all end up in one set).
- **Step 3** collapses to components — group emails by `findSet(id)`.
- **Step 4** formats each component as required.

---

## 6. The Union-Find pieces

Same map-based style you've been using: `parent` with `-1` = root, `rank` = set size, recursive `findSet` with path compression, `unionSet` by size.

```java
private int findSet(int i, Map<Integer, Integer> parent) {
    if (parent.get(i) == -1) return i;             // base case: root
    int root = findSet(parent.get(i), parent);
    parent.put(i, root);                            // path compression
    return root;
}

private void unionSet(int x, int y,
                      Map<Integer, Integer> parent, Map<Integer, Integer> rank) {
    int s1 = findSet(x, parent), s2 = findSet(y, parent);
    if (s1 != s2) {                                 // only merge different sets
        if (rank.get(s1) < rank.get(s2)) {          // attach smaller under larger
            parent.put(s1, s2);
            rank.put(s2, rank.get(s1) + rank.get(s2));
        } else {
            parent.put(s2, s1);
            rank.put(s1, rank.get(s1) + rank.get(s2));
        }
    }
}
```

(These are identical to the Number-of-Provinces version — the Union-Find engine never changes; only *what you feed it* changes.)

---

## 7. The full solution

```java
class Solution {
    public List<List<String>> accountsMerge(List<List<String>> accounts) {
        // STEP 1: index emails -> ids, and remember each email's name
        Map<String, Integer> emailId = new HashMap<>();
        Map<String, String>  emailName = new HashMap<>();
        int nid = 0;
        for (List<String> acc : accounts) {
            String name = acc.get(0);
            for (int k = 1; k < acc.size(); k++) {
                String email = acc.get(k);
                if (!emailId.containsKey(email)) emailId.put(email, nid++);
                emailName.put(email, name);
            }
        }

        // init Union-Find over email ids
        Map<Integer, Integer> parent = new HashMap<>();
        Map<Integer, Integer> rank   = new HashMap<>();
        for (int i = 0; i < nid; i++) { parent.put(i, -1); rank.put(i, 1); }

        // STEP 2: union all emails within each account
        for (List<String> acc : accounts) {
            int first = emailId.get(acc.get(1));               // account's first email id
            for (int k = 2; k < acc.size(); k++) {
                unionSet(first, emailId.get(acc.get(k)), parent, rank);
            }
        }

        // STEP 3: group emails by their component root
        Map<Integer, List<String>> groups = new HashMap<>();
        for (Map.Entry<String, Integer> e : emailId.entrySet()) {
            int root = findSet(e.getValue(), parent);
            groups.computeIfAbsent(root, z -> new ArrayList<>()).add(e.getKey());
        }

        // STEP 4: build [name, sorted emails...] per component
        List<List<String>> result = new ArrayList<>();
        for (List<String> emails : groups.values()) {
            Collections.sort(emails);                          // emails ascending
            List<String> row = new ArrayList<>();
            row.add(emailName.get(emails.get(0)));             // name from any email in the group
            row.addAll(emails);
            result.add(row);
        }
        return result;
    }

    // findSet + unionSet as in §6
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

Verified: produces the expected merge on the sample, keeps same-name-but-unshared accounts separate, and merges transitive chains.

---

## 8. Dry run

`accounts = [["John","js@m","jny@m"], ["John","js@m","j00@m"], ["Mary","mary@m"], ["John","jb@m"]]`

**Step 1 — index emails, record names:**
```
emailId  = { js@m:0, jny@m:1, j00@m:2, mary@m:3, jb@m:4 }
emailName= { js@m:John, jny@m:John, j00@m:John, mary@m:Mary, jb@m:John }
parent   = { 0:-1,1:-1,2:-1,3:-1,4:-1 }   rank = all 1
```

**Step 2 — union within accounts:**
```
acc1 ["John",js,jny]:  union(0,1)  → parent{1:0}, rank{0:2}          set {0,1}
acc2 ["John",js,j00]:  union(0,2)  → find(0)=0,find(2)=2 → parent{2:0}, rank{0:3}   set {0,1,2}
acc3 ["Mary",mary]:    (only one email → no union)                    set {3}
acc4 ["John",jb]:      (only one email → no union)                    set {4}
```
Notice: account 2 shared `js@m` (id 0), so its `j00@m` joined component {0,1} automatically — the shared email was the bridge.

**Step 3 — group by root:**
```
find(0)=0, find(1)=0, find(2)=0 → group 0: [js@m, jny@m, j00@m]
find(3)=3               → group 3: [mary@m]
find(4)=4               → group 4: [jb@m]
```

**Step 4 — sort + name:**
```
group0 sorted: [j00@m, jny@m, js@m], name John → ["John","j00@m","jny@m","js@m"]
group3:        ["Mary","mary@m"]
group4:        ["John","jb@m"]
```
Matches the expected output. ✓

---

## 9. The tricky bits (same name, sorting, name lookup)

- **Same name ≠ same person.** Two "John" accounts with no shared email land in **different components** because nothing unions their emails — exactly what we want (verified). You must never group by name; the name is output-only.
- **Different name is impossible within a component.** All emails in one component came from accounts that chain through shared emails, all belonging to one person, so they all carry the same name — which is why grabbing the name from *any* email in the group is safe.
- **Sorting.** The problem requires emails in ascending order, so `Collections.sort(emails)` per group. (The name goes first and is *not* part of the sort.)
- **Transitivity for free.** A chain `1@m–2@m` (acc1) and `2@m–3@m` (acc2) merges all three via the shared `2@m`, with no special handling — union is transitive by nature (verified).

> 💡 **Name is output, emails are identity:** "Identity is decided entirely by shared emails; the name is just a label I attach at the end from any email in the component. That's why same-name accounts don't merge and why I never key on the name." 

---

## 10. Complexity

Let `N` = total number of emails across all accounts.

| | Cost |
|:--|:--|
| **Time** | **O(N · α(N)) for the unions/finds ≈ O(N)**, plus **O(N log N)** to sort emails within groups → dominated by the **sort: O(N log N)** |
| **Space** | **O(N)** — the id/name maps, the parent/rank maps, and the groups |

The Union-Find work is nearly linear (inverse-Ackermann per op, thanks to path compression + union by size); the sorting of emails is the real cost.

---

## 11. Common mistakes

- ❌ **Making accounts the nodes.** Works but forces an extra email→accounts index to find edges. Emails-as-nodes uses the input directly. (§4)
- ❌ **Grouping by name.** Same name can be different people; different accounts of one person all share the name but that's a *consequence*, not the key. Group by **component**, not name.
- ❌ **Forgetting to sort the emails.** The problem explicitly requires ascending order within each merged account.
- ❌ **Sorting the name into the emails.** The name must stay first; sort only the email list, then prepend the name.
- ❌ **Assigning an email id twice.** Guard with `containsKey` so a repeated email keeps its original id (and stays one node).
- ❌ **Unioning across accounts manually.** You don't compare accounts to each other — just union emails *within* each account and let the shared-email node bridge them transitively.
- ❌ **Reusing one email's name when components are wrong.** Fine here because identity = emails, but only *after* correct grouping; don't shortcut the union step.

---

## 12. DFS alternative (for contrast)

You can also build an explicit email graph (adjacency list: each email ↔ every other email in its account) and DFS each unvisited email to collect its component:

```
build graph: for each account, connect email[1] with email[k] both ways (adjacency list)
for each unvisited email: DFS to gather the whole component → sort → prepend name
```

Same connected-components idea, same O(N log N) (sorting dominates). Union-Find is usually the crisper choice for "merge groups"; DFS is fine if you prefer explicit traversal. The recognition step (§3) is identical either way — **the pattern is connected components; UF vs DFS is just the engine.**

---

## 13. How to recognize this pattern next time

A reusable checklist for spotting "this is Union-Find / connected components":

1. **Is grouping transitive?** ("A relates to B, B relates to C ⟹ A,B,C same group.") → connected components.
2. **What are the nodes?** Pick the unit such that the connection is a *direct, obvious edge in the input.* (Here: emails, because "share an account" is a direct link; accounts would need extra indexing.)
3. **What's an edge (a union)?** The condition that directly links two nodes. (Here: "in the same account.")
4. **What does a component mean, and what post-processing does the output need?** (Here: a person; then name + sorted emails.)

Problems in this exact family: **Number of Provinces** (cities), **Number of Connected Components** (edge list), **Redundant Connection**, **Most Stones Removed** (rows/cols as nodes), **Similar String Groups**. All are "transitive grouping → components." The only creativity is step 2 — *choosing the right nodes.*

> 💡 **The transferable skill is step 2:** "The hard, reusable move is choosing what the nodes are so that the connections are already in the input. Get the nodes right and the union edges become obvious; get them wrong and you drown in extra indexing." 

---

## 14. Cheat sheet

**Recognize it:** "same person" is **transitive** through shared emails → connected components → Union-Find.

**Nodes = emails** (not accounts), so a shared email is the bridge that merges accounts automatically.

**Recipe:**
```
1. INDEX  every unique email → an int id; record email→name.
2. UNION  within each account, union first email with every other.
3. GROUP  bucket emails by findSet(id)  → one bucket per person.
4. BUILD  each bucket: sort emails, prepend name → merged account.
```

**Union-Find engine (unchanged):** `parent` map (`-1`=root), recursive `findSet` w/ path compression, `unionSet` by size.

**Watch:** don't group by name (name is output only); sort emails ascending; assign each email an id once; let transitivity merge accounts (don't compare accounts to each other).

**Complexity:** O(N log N) (sorting dominates; UF is ~O(N)); O(N) space.

> **One-line philosophy:** *Accounts Merge is a connected-components problem in disguise: the nodes are the emails (not the accounts), an edge is "two emails share an account," and because "same person" is transitive, unioning every account's emails lets a single shared email bridge and merge whole accounts automatically — so grouping emails by their Union-Find root, sorting each group, and attaching the name yields exactly one merged account per person; the reusable skill is spotting transitive grouping and then choosing the nodes so the connections are already sitting in the input.*
