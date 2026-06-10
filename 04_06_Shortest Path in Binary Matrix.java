class Solution {
    public int shortestPathBinaryMatrix(int[][] grid) {
        int n = grid.length;

        // Corners must be open, else no clear path
        if (grid[0][0] != 0 || grid[n - 1][n - 1] != 0) return -1;

        // 8 directions
        int[][] dirs = {
            {-1,-1},{-1,0},{-1,1},
            { 0,-1},        { 0,1},
            { 1,-1},{ 1,0},{ 1,1}
        };

        Queue<int[]> queue = new ArrayDeque<>();
        queue.offer(new int[]{0, 0, 1});   // {row, col, distance in cells}
        grid[0][0] = 1;                    // mark visited (reuse the grid)

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int r = cur[0], c = cur[1], d = cur[2];

            if (r == n - 1 && c == n - 1) return d;   // first arrival = shortest

            for (int[] dir : dirs) {
                int nr = r + dir[0], nc = c + dir[1];
                if (nr >= 0 && nr < n && nc >= 0 && nc < n && grid[nr][nc] == 0) {
                    grid[nr][nc] = 1;                 // MARK on enqueue (avoids duplicates)
                    queue.offer(new int[]{nr, nc, d + 1});
                }
            }
        }
        return -1;   // queue drained without reaching the target
    }
}
