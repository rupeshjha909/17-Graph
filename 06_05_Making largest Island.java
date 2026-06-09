import java.util.*;

/**
 * Making a Large Island (LeetCode 827)
 *
 * Given an n x n binary grid (0 = water, 1 = land), you may change AT MOST ONE
 * 0 to 1. Return the size of the largest island possible. Cells are part of the
 * same island if 4-directionally adjacent.
 *
 * Approach: "color connected components, then query" — two passes, O(n^2).
 *   Pass 1: flood-fill each island with a unique color id (>= 2), record its size.
 *   Pass 2: for each 0, sum the sizes of its DISTINCT neighbor islands + 1.
 *   Answer: max(largest existing island, best Pass-2 candidate).
 *
 * Key points:
 *   - Color ids start at 2 so they never collide with water(0) or unvisited land(1);
 *     this lets the color double as the visited marker (no separate visited[][] needed).
 *   - A Set deduplicates neighbor islands so a 0 touching the same island on multiple
 *     sides (U/L shapes) is not double-counted.
 *   - Edge cases: all water -> 1; all land -> largest existing island.
 */
public class MakingLargeIsland {

    private int n;
    private final int[] dx = {0, 0, 1, -1};
    private final int[] dy = {1, -1, 0, 0};

    public int largestIsland(int[][] grid) {
        n = grid.length;
        Map<Integer, Integer> sizeOf = new HashMap<>();
        int color = 2;                          // start at 2: avoid clash with water(0)/land(1)

        // Pass 1: color each island, record its size
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] == 1) {
                    sizeOf.put(color, dfs(grid, i, j, color));
                    color++;
                }
            }
        }

        // If there are no islands at all, flipping any 0 yields a single cell
        if (sizeOf.isEmpty()) return 1;

        // Start with the largest island as-is (covers the "no useful flip" case)
        int best = Collections.max(sizeOf.values());

        // Pass 2: try flipping each 0; sum the DISTINCT neighbor islands
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
                        candidate += sizeOf.get(id);
                    }
                    best = Math.max(best, candidate);
                }
            }
        }
        return best;
    }

    /** Flood-fill the island starting at (i, j), color its cells, return the size. */
    private int dfs(int[][] grid, int i, int j, int color) {
        if (i < 0 || i >= n || j < 0 || j >= n || grid[i][j] != 1) return 0;
        grid[i][j] = color;                     // color = visited marker (>= 2)
        int size = 1;
        for (int k = 0; k < 4; k++) {
            size += dfs(grid, i + dx[k], j + dy[k], color);
        }
        return size;
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
