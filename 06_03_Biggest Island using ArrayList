// Global array to count cells in each island (equivalent to col_cnt)
private static int[] colCnt = new int[1000];

// Direction arrays for 4-directional movement
private static int[] dx = {1, -1, 0, 0};
private static int[] dy = {0, 0, 1, -1};

private void dfs(ArrayList<ArrayList<Integer>> grid, int curr, int curc, int r, int c, int color, ArrayList<ArrayList<Integer>> vis) {
    // Mark current cell with color and as visited
    grid.get(curr).set(curc, color);
    vis.get(curr).set(curc, 1);
    colCnt[color]++;
    
    // Check all 4 directions
    for (int k = 0; k < 4; k++) {
        int nx = curr + dx[k];
        int ny = curc + dy[k];
        
        // Boundary check and unvisited land cell check
        if (nx >= 0 && nx < r && ny >= 0 && ny < c && 
            grid.get(nx).get(ny) == 1 && vis.get(nx).get(ny) == 0) {
            dfs(grid, nx, ny, r, c, color, vis);
        }
    }
}

// Function to find the number of islands
public int numIslands(ArrayList<ArrayList<Integer>> grid) {
    int r = grid.size();
    int c = grid.get(0).size();
    
    // Initialize visited array
    ArrayList<ArrayList<Integer>> vis = new ArrayList<>();
    for (int i = 0; i < r; i++) {
        ArrayList<Integer> row = new ArrayList<>();
        for (int j = 0; j < c; j++) {
            row.add(0);
        }
        vis.add(row);
    }
    
    int ans = 0;
    
    // Traverse the grid
    for (int i = 0; i < r; i++) {
        for (int j = 0; j < c; j++) {
            if (grid.get(i).get(j) == 1 && vis.get(i).get(j) == 0) {
                ans++;
                dfs(grid, i, j, r, c, ans, vis);
            }
        }
    }
    
    return ans;
}



//this problem is noting but find the total size of the island found
// colored with one color
int col_cnt[1000];

int dx[4] = {1, -1, 0, 0};
int dy[4] = {0, 0, 1, -1};
void dfs(vector<vector<int>> &grid, int curr, int curc, int r, int c, int color, vector<vector<int>> &vis) {
	grid[curr][curc] = color; //we are using this logic inplace of vis
	vis[curr][curc] = 1;
	col_cnt[color]++;
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
	int ans = numIslands(grid);
	cout << ans << endl;
	for (int i = 0; i < r; i++ ) {
		for (int j = 0; j < c; j++) {
			cout << grid[i][j] << " ";
		}
		cout << endl;
	}
	for (int i = 1; i <= ans; i++) {
		cout << i << " " << col_cnt[i] << endl;
	}
}
