class Solution {
    public int closedIsland(int[][] grid) {
        int R = grid.length, C = grid[0].length;

        // Phase 1: sink all land connected to the boundary
        for (int r = 0; r < R; r++) {
            for (int c = 0; c < C; c++) {
                if ((r == 0 || r == R-1 || c == 0 || c == C-1) && grid[r][c] == 0) {
                    sink(grid, r, c, R, C);
                }
            }
        }

        // Phase 2: count remaining islands (these are closed)
        int count = 0;
        for (int r = 0; r < R; r++) {
            for (int c = 0; c < C; c++) {
                if (grid[r][c] == 0) {
                    count++;
                    sink(grid, r, c, R, C);   // mark this island as counted
                }
            }
        }
        return count;
    }

    private int[] dr = {1, -1, 0, 0};
    private int[] dc = {0, 0, 1, -1};
    
    private void sink(int[][] grid, int r, int c, int R, int C) {
        grid[r][c] = 1;                        // turn land into water
    
        for (int k = 0; k < 4; k++) {
            int nr = r + dr[k];
            int nc = c + dc[k];
            if (nr >= 0 && nr < R && nc >= 0 && nc < C && grid[nr][nc] == 0) {
                sink(grid, nr, nc, R, C);
            }
        }
    }
}
