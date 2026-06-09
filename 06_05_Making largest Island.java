import java.util.*;

/**
 * Making a Large Island (LeetCode 827)
 *
 * Given an n x n binary grid (0 = water, 1 = land), you may change AT MOST ONE
 * 0 to 1. Return the size of the largest island possible. Cells are part of the
 * same island if 4-directionally adjacent.
 *
 * Approach: "color connected components, then query" — two passes, O(n^2).
 *   Pass 1: flood-fill each island with a unique color id (>= 2). The DFS is VOID:
 *           it colors each cell and increments colCnt[color] as it goes, so after
 *           the fill, colCnt[color] holds that island's size.
 *   Pass 2: for each 0, sum the sizes of its DISTINCT neighbor islands + 1.
 *   Answer: max(largest existing island, best Pass-2 candidate).
 *
 * Key points:
 *   - Color ids start at 2 so they never collide with water(0) or unvisited land(1);
 *     this lets the color double as the visited marker (no separate visited[][] needed).
 *   - colCnt[color] tracks each island's size (filled inside the void DFS).
 *   - A Set deduplicates neighbor islands so a 0 touching the same island on multiple
 *     sides (U/L shapes) is not double-counted.
 *   - Edge cases: all water -> 1; all land -> largest existing island.
 */
public class MakingLargeIsland {

    private int n;
    private int[] colCnt;                    // colCnt[color] = size of that island
    private final int[] dx = {0, 0, 1, -1};
    private final int[] dy = {1, -1, 0, 0};

    public int largestIsland(int[][] grid) {
        n = grid.length;
        colCnt = new int[n * n + 2];         // colors range over [2 .. n*n+1]

        // Pass 1: color each island; the void DFS fills colCnt[color] with its size
        int color = 2;                       // start at 2: avoid clash with water(0)/land(1)
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] == 1) {
                    dfs(grid, i, j, color);
                    color++;
                }
            }
        }

        // Largest existing island (also covers the "all land, no useful flip" case)
        int best = 0;
        for (int c = 2; c < color; c++) {
            best = Math.max(best, colCnt[c]);
        }

        // Pass 2: try flipping each 0; sum the DISTINCT neighbor islands + 1
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] == 0) {
                    Set<Integer> neighborIslands = new HashSet<>();
                    for (int k = 0; k < 4; k++) {
                        int ni = i + dx[k], nj = j + dy[k];
                        if (ni >= 0 && ni < n && nj >= 0 && nj < n && grid[ni][nj] >= 2) {
                            neighborIslands.add(grid[ni][nj]);   // Set dedups same island
                        }
                    }
                    int candidate = 1;                          // the flipped cell itself
                    for (int id : neighborIslands) {
                        candidate += colCnt[id];
                    }
                    best = Math.max(best, candidate);
                }
            }
        }
        return best;
    }

    /** Flood-fill: color this cell, count it toward its island, recurse on land neighbors. */
    private void dfs(int[][] grid, int i, int j, int color) {
        grid[i][j] = color;                  // color = visited marker (>= 2)
        colCnt[color]++;                     // count this cell toward the island's size

        for (int k = 0; k < 4; k++) {
            int ni = i + dx[k], nj = j + dy[k];
            if (ni >= 0 && ni < n && nj >= 0 && nj < n && grid[ni][nj] == 1) {
                dfs(grid, ni, nj, color);    // only unvisited land is still == 1
            }
        }
    }

    // ---------------------------------------------------------------------
    // Test harness
    // ---------------------------------------------------------------------
    public static void main(String[] args) {
        MakingLargeIsland sol = new MakingLargeIsland();

        int[][][] grids = {
            {{1, 1, 0}, {1, 0, 1}, {0, 1, 1}},   // expected 7
            {{1, 0}, {0, 1}},                    // expected 3
            {{1, 1}, {1, 0}},                    // expected 4
            {{1, 1}, {1, 1}},                    // expected 4
            {{0, 0}, {0, 0}},                    // expected 1
            {{1}},                               // expected 1
            {{0}},                               // expected 1
        };
        int[] expected = {7, 3, 4, 4, 1, 1, 1};

        for (int t = 0; t < grids.length; t++) {
            // deep-copy because largestIsland mutates the grid
            int[][] copy = new int[grids[t].length][];
            for (int r = 0; r < grids[t].length; r++) {
                copy[r] = grids[t][r].clone();
            }
            int result = sol.largestIsland(copy);
            String status = (result == expected[t]) ? "PASS" : "FAIL";
            System.out.println("Test " + (t + 1) + ": got " + result
                + ", expected " + expected[t] + " -> " + status);
        }
    }
}
