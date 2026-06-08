class Solution {
    // direction vectors: down, up, right, left
    int[][] dirs = {{1,0}, {-1,0}, {0,1}, {0,-1}};
    
    public boolean exist(char[][] board, String word) {
        int rows = board.length;
        int cols = board[0].length;
        boolean[][] visited = new boolean[rows][cols];
        
        // try every cell as the starting point
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (dfs(board, word, visited, r, c, 0)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean dfs(char[][] board, String word, boolean[][] visited, 
                        int r, int c, int idx) {
        // matched the whole word
        if (idx == word.length()) return true;
        
        // out of bounds
        if (r < 0 || r >= board.length || c < 0 || c >= board[0].length) return false;
        
        // already used this cell in the current path
        if (visited[r][c]) return false;
        
        // character doesn't match
        if (board[r][c] != word.charAt(idx)) return false;
        
        // --- backtracking: mark → explore → restore ---
        visited[r][c] = true;
        
        for (int[] d : dirs) {
            if (dfs(board, word, visited, r + d[0], c + d[1], idx + 1)) {
                return true;    // found it, stop early
            }
        }
        
        visited[r][c] = false;  // restore for other paths
        
        return false;
    }
}
