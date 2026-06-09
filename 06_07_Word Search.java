class Solution {
    private int[] dx = {1, -1, 0, 0};
    private int[] dy = {0, 0, 1, -1};

    public boolean exist(char[][] board, String word) {
        int rows = board.length;
        int cols = board[0].length;
        boolean[][] visited = new boolean[rows][cols];

        // try every cell whose char matches the FIRST letter as a starting point
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (board[r][c] == word.charAt(0)
                    && dfs(board, word, visited, r, c, 0, rows, cols)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean dfs(char[][] board, String word, boolean[][] visited,
                        int r, int c, int idx, int rows, int cols) {
        // current cell already matches word[idx] (the caller checked before calling)
        if (idx == word.length() - 1) return true;   // matched the LAST char → whole word found

        visited[r][c] = true;                        // mark

        boolean found = false;
        for (int k = 0; k < 4; k++) {
            int nr = r + dx[k];
            int nc = c + dy[k];
            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols
                && !visited[nr][nc]
                && board[nr][nc] == word.charAt(idx + 1)) {   // check next char BEFORE recursing
                if (dfs(board, word, visited, nr, nc, idx + 1, rows, cols)) {
                    found = true;
                    break;
                }
            }
        }

        visited[r][c] = false;                       // restore (backtrack)
        return found;
    }
}
