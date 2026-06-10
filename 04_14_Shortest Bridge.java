class Solution {
    private int[] dr = {1, -1, 0, 0};
    private int[] dc = {0, 0, 1, -1};

    public int shortestBridge(int[][] grid) {
        int n = grid.length;
        Queue<int[]> queue = new ArrayDeque<>();

        // Phase 1: find the first island, flood-fill it (mark 2), enqueue its cells
        boolean found = false;
        for (int r = 0; r < n && !found; r++) {
            for (int c = 0; c < n && !found; c++) {
                if (grid[r][c] == 1) {       // caller checks the first cell is land
                    dfs(grid, r, c, n, queue);
                    found = true;
                }
            }
        }

        // Phase 2: multi-source BFS outward until we touch the second island
        int steps = 0;
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int s = 0; s < size; s++) {
                int[] cell = queue.poll();
                int r = cell[0], c = cell[1];
                for (int k = 0; k < 4; k++) {
                    int nr = r + dr[k], nc = c + dc[k];
                    if (nr >= 0 && nr < n && nc >= 0 && nc < n) {
                        if (grid[nr][nc] == 1) return steps;     // reached island B
                        if (grid[nr][nc] == 0) {                 // water → flip & enqueue
                            grid[nr][nc] = 2;
                            queue.offer(new int[]{nr, nc});
                        }
                    }
                }
            }
            steps++;
        }
        return -1;   // unreachable in valid input (problem guarantees 2 islands)
    }

    // Phase 1 DFS — check-before-recurse: the cell passed in is already land
    private void dfs(int[][] grid, int r, int c, int n, Queue<int[]> queue) {
        grid[r][c] = 2;                       // mark island A
        queue.offer(new int[]{r, c});         // collect as a BFS source

        for (int k = 0; k < 4; k++) {
            int nr = r + dr[k], nc = c + dc[k];
            if (nr >= 0 && nr < n && nc >= 0 && nc < n && grid[nr][nc] == 1) {  // validate first
                dfs(grid, nr, nc, n, queue);
            }
        }
    }
}
