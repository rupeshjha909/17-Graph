class Solution {
    // Direction arrays for 4 adjacent cells (up, down, left, right)
    private int[] dx = {1, -1, 0, 0};
    private int[] dy = {0, 0, 1, -1};
    
    private void dfs(int[][] grid, int curr, int curc, int r, int c, int color, int[][] vis) {
        // Color the current cell and mark as visited
        grid[curr][curc] = color;
        vis[curr][curc] = 1;
        
        // Check all 4 directions
        for (int k = 0; k < 4; k++) {
            int nx = curr + dx[k];
            int ny = curc + dy[k];
            
            // Check if the new position is valid, contains 1, and is unvisited
            if (nx >= 0 && nx < r && ny >= 0 && ny < c && grid[nx][ny] == 1 && vis[nx][ny] == 0) {
                dfs(grid, nx, ny, r, c, color, vis);
            }
        }
    }
    
    // Function to find the number of islands and color them
    public int numIslands(int[][] grid) {
        if (grid == null || grid.length == 0) {
            return 0;
        }
        
        int r = grid.length;
        int c = grid[0].length;
        
        // Create visited array initialized with 0s
        int[][] vis = new int[r][c];
        
        int ans = 0;
        
        // Traverse the entire grid
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                if (grid[i][j] == 1 && vis[i][j] == 0) {
                    ans++;
                    // Color this island with the current island number
                    dfs(grid, i, j, r, c, ans, vis);
                }
            }
        }
        
        return ans;
    }
}






//this question is based on printing the one island with one color and other island
// with other color
int dx[4] = {1, -1, 0, 0};
int dy[4] = {0, 0, 1, -1};
void dfs(vector<vector<int>> &grid, int curr, int curc, int r, int c, int color, vector<vector<int>> &vis) {
	grid[curr][curc] = color; //we are using this logic inplace of vis
	vis[curr][curc] = 1;
	for (int k = 0; k < 4; k++) {
		int nx = curr + dx[k];
		int ny = curc + dy[k];
		if (nx >= 0 and nx<r and ny >= 0 and ny < c and grid[nx][ny] == 1 and vis[nx][ny] == 0) {
			dfs(grid, nx, ny, r, c, color , vis);
		}
	}
}
//Function to find the number of islands.
int numIslands(vector<vector<int>>& grid)
{
	// Code here
	int r = grid.size();
	int c = grid[0].size();
	std::vector<vector<int> > vis(r, vector<int>(c, 0));
	int ans = 0;
	for (int i = 0; i < r; i++) {
		for (int j = 0; j < c; j++) {
			if (grid[i][j] == 1 and vis[i][j] == 0)
			{
				ans++;
				dfs(grid, i, j, r, c, ans, vis);
			}
		}
	}
	return ans;
}

int32_t main()
{
	ios_base::sync_with_stdio(false);
	cin.tie(NULL);
	cout.tie(NULL);
	int r, c;
	cin >> r >> c;
	vector<vector<int>> grid(r, vector<int>(c));
	for (int i = 0; i < r; i++ ) {
		for (int j = 0; j < c; j++) {
			cin >> grid[i][j];
		}
	}
	cout << numIslands(grid) << endl;

	for (int i = 0; i < r; i++ ) {
		for (int j = 0; j < c; j++) {
			cout << grid[i][j] << " ";
		}
		cout << endl;
	}
}
