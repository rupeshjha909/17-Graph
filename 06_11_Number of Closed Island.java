class Solution {
    private int[] dr = {1, -1, 0, 0};
    private int[] dc = {0, 0, 1, -1};

    public int closedIsland(int[][] grid) {
        if (grid == null || grid.length == 0 || grid[0].length == 0) return 0;
        int rows = grid.length, cols = grid[0].length;

        // Phase 1: sink land connected to the border
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                boolean onBorder = (r == 0 || r == rows - 1 || c == 0 || c == cols - 1);
                if (onBorder && grid[r][c] == 0) {
                    sink(grid, r, c, rows, cols);
                }
            }
        }

        // Phase 2: count remaining (closed) islands
        int count = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == 0) {
                    count++;
                    sink(grid, r, c, rows, cols);
                }
            }
        }
        return count;
    }

    private void sink(int[][] grid, int r, int c, int rows, int cols) {
        grid[r][c] = 1;
        for (int k = 0; k < 4; k++) {
            int nr = r + dr[k];
            int nc = c + dc[k];
            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && grid[nr][nc] == 0) {
                sink(grid, nr, nc, rows, cols);
            }
        }
    }
}
