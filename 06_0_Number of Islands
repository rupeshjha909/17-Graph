== Flood fill algorithm

class Solution {
    // Direction arrays for 4 adjacent cells (up, down, left, right)
    private int[] dx = {1, -1, 0, 0};
    private int[] dy = {0, 0, 1, -1};
    
    private void dfs(char[][] grid, int curr, int curc, int r, int c) {
        // Mark current cell as visited by changing '1' to '2'
        grid[curr][curc] = '2';
        
        // Check all 4 directions
        for (int k = 0; k < 4; k++) {
            int nx = curr + dx[k];
            int ny = curc + dy[k];
            
            // Check if the new position is valid and contains '1'
            if (nx >= 0 && nx < r && ny >= 0 && ny < c && grid[nx][ny] == '1') {
                dfs(grid, nx, ny, r, c);
            }
        }
    }
    
    // Function to find the number of islands
    public int numIslands(char[][] grid) {
        if (grid == null || grid.length == 0) {
            return 0;
        }
        
        int r = grid.length;
        int c = grid[0].length;
        
        int ans = 0;
        
        // Traverse the entire grid
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                if (grid[i][j] == '1') {
                    // Found a new island, perform DFS to mark all connected cells
                    dfs(grid, i, j, r, c);
                    ans++;
                }
            }
        }
        
        return ans;
    }
}



class Solution {
public:
    int dx[4] = {1, -1, 0, 0};
    int dy[4] = {0, 0, 1, -1};
    void dfs(vector<vector<char>> &grid, int curr, int curc, int r, int c) {
        grid[curr][curc] = 2; //we are using this logic inplace of vis
        for (int k = 0; k < 4; k++) {
            int nx = curr + dx[k];
            int ny = curc + dy[k];
            if (nx >= 0 and nx<r and ny >= 0 and ny < c and grid[nx][ny] == '1') {
                dfs(grid, nx, ny, r, c);
            }
        }
    }
    //Function to find the number of islands.
    int numIslands(vector<vector<char>>& grid)
    {
        // Code here
        int r = grid.size();
        int c = grid[0].size();

        int ans = 0;
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                if (grid[i][j] == '1')
                {
                    dfs(grid, i, j, r, c);
                    ans++;
                }
            }
        }
        return ans;
    }
};
