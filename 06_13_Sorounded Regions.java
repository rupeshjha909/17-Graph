class Solution {
    public void solve(char[][] board) {
        if (board == null || board.length == 0) return;
        int R = board.length, C = board[0].length;

        // Phase 1: mark every border-connected 'O' as safe ('#')
        for (int r = 0; r < R; r++) {
            for (int c = 0; c < C; c++) {
                boolean onBorder = (r == 0 || r == R-1 || c == 0 || c == C-1);
                if (onBorder && board[r][c] == 'O') {
                    markSafe(board, r, c, R, C);
                }
            }
        }

        // Phase 2: capture the interior 'O's, restore the safe '#'s
        for (int r = 0; r < R; r++) {
            for (int c = 0; c < C; c++) {
                if (board[r][c] == 'O') {
                    board[r][c] = 'X';          // surrounded → capture
                } else if (board[r][c] == '#') {
                    board[r][c] = 'O';          // safe → restore
                }
            }
        }
    }

    private int[] dr = {1, -1, 0, 0};
    private int[] dc = {0, 0, 1, -1};

    private void markSafe(char[][] board, int r, int c, int R, int C) {
        board[r][c] = '#';                      // mark this 'O' as safe
        for (int k = 0; k < 4; k++) {
            int nr = r + dr[k], nc = c + dc[k];
            if (nr >= 0 && nr < R && nc >= 0 && nc < C && board[nr][nc] == 'O') {
                markSafe(board, nr, nc, R, C);
            }
        }
    }
}
