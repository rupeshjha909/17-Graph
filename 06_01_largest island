/**
 * Solution for Maximum Area of Island
 * Converted from C++ to Java
 * Uses DFS to find the largest connected component of 1s in a 2D grid
 */
public class MaxAreaOfIsland {
    
    // Global variable to count current island size
    private int currentSize;
    
    /**
     * DFS helper method to explore island and count its area
     * 
     * @param grid the 2D grid representing the map
     * @param visited 2D boolean array to track visited cells
     * @param i current row index
     * @param j current column index
     * @param rows total number of rows
     * @param cols total number of columns
     */
    private void dfs(int[][] grid, boolean[][] visited, int i, int j, int rows, int cols) {
        // Mark current cell as visited
        visited[i][j] = true;
        
        // Direction arrays for 4-directional movement
        // In this we are considering the island in 4 directions only
        int[] dx = {1, -1, 0, 0};  // Down, Up, Right, Left
        int[] dy = {0, 0, 1, -1};  // Down, Up, Right, Left
        
        // For considering diagonal also, modify the dx and dy arrays:
        // Include diagonal movements (8 directions total)
        // int[] dx = {0, 0, 1, -1, 1, 1, -1, -1}; // Right, left, down, up, down-right, down-left, up-right, up-left
        // int[] dy = {1, -1, 0, 0, 1, -1, 1, -1};
        
        // Check all 4 directions
        for (int k = 0; k < 4; k++) {
            int nx = i + dx[k];
            int ny = j + dy[k];
            
            // Check if new position is valid and contains land (1) and not visited
            if (nx >= 0 && ny >= 0 && nx < rows && ny < cols && 
                grid[nx][ny] == 1 && !visited[nx][ny]) {
                dfs(grid, visited, nx, ny, rows, cols);
                currentSize++;
            }
        }
    }
    
    /**
     * Find the maximum area of island in the given grid
     * 
     * @param grid 2D array representing the map (1 = land, 0 = water)
     * @return the maximum area of any island
     */
    public int maxAreaOfIsland(int[][] grid) {
        if (grid == null || grid.length == 0) {
            return 0;
        }
        
        int rows = grid.length;
        int cols = grid[0].length;
        
        // Create visited array to track visited cells
        boolean[][] visited = new boolean[rows][cols];
        
        int largest = 0;
        
        // Iterate through all cells
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // If cell is land (1) and not visited, start DFS
                if (!visited[i][j] && grid[i][j] == 1) {
                    currentSize = 1;  // Start with current cell
                    dfs(grid, visited, i, j, rows, cols);
                    largest = Math.max(largest, currentSize);
                }
            }
        }
        
        return largest;
    }
    
    /**
     * Alternative implementation using 8-directional movement (including diagonals)
     * 
     * @param grid 2D array representing the map
     * @return the maximum area of any island
     */
    public int maxAreaOfIsland8Directions(int[][] grid) {
        if (grid == null || grid.length == 0) {
            return 0;
        }
        
        int rows = grid.length;
        int cols = grid[0].length;
        boolean[][] visited = new boolean[rows][cols];
        
        int largest = 0;
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!visited[i][j] && grid[i][j] == 1) {
                    currentSize = 1;
                    dfs8Directions(grid, visited, i, j, rows, cols);
                    largest = Math.max(largest, currentSize);
                }
            }
        }
        
        return largest;
    }
    
    /**
     * DFS helper method for 8-directional movement
     */
    private void dfs8Directions(int[][] grid, boolean[][] visited, int i, int j, int rows, int cols) {
        visited[i][j] = true;
        
        // 8-directional movement arrays
        int[] dx = {0, 0, 1, -1, 1, 1, -1, -1}; // Right, left, down, up, down-right, down-left, up-right, up-left
        int[] dy = {1, -1, 0, 0, 1, -1, 1, -1};
        
        for (int k = 0; k < 8; k++) {
            int nx = i + dx[k];
            int ny = j + dy[k];
            
            if (nx >= 0 && ny >= 0 && nx < rows && ny < cols && 
                grid[nx][ny] == 1 && !visited[nx][ny]) {
                dfs8Directions(grid, visited, nx, ny, rows, cols);
                currentSize++;
            }
        }
    }
    
    /**
     * Print the grid for visualization
     * 
     * @param grid the 2D grid to print
     */
    public void printGrid(int[][] grid) {
        if (grid == null || grid.length == 0) {
            System.out.println("Empty grid");
            return;
        }
        
        System.out.println("Grid:");
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
    }
    
    /**
     * Find all islands and their areas
     * 
     * @param grid the 2D grid
     * @return list of island areas
     */
    public java.util.List<Integer> findAllIslandAreas(int[][] grid) {
        if (grid == null || grid.length == 0) {
            return new java.util.ArrayList<>();
        }
        
        int rows = grid.length;
        int cols = grid[0].length;
        boolean[][] visited = new boolean[rows][cols];
        java.util.List<Integer> areas = new java.util.ArrayList<>();
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!visited[i][j] && grid[i][j] == 1) {
                    currentSize = 1;
                    dfs(grid, visited, i, j, rows, cols);
                    areas.add(currentSize);
                }
            }
        }
        
        return areas;
    }
    
    public static void main(String[] args) {
        MaxAreaOfIsland solution = new MaxAreaOfIsland();
        
        // Test with the example grid from C++ code
        int[][] grid = {
            {1, 0, 0, 0, 1, 0, 0},
            {0, 1, 0, 0, 1, 1, 1},
            {1, 1, 0, 0, 0, 0, 0},
            {1, 0, 0, 1, 1, 0, 0},
            {1, 0, 0, 1, 0, 1, 1}
        };
        
        System.out.println("=== Maximum Area of Island Solution ===");
        
        // Print the grid
        solution.printGrid(grid);
        
        // Find maximum area using 4-directional movement
        int maxArea = solution.maxAreaOfIsland(grid);
        System.out.println("\nMaximum area of island (4 directions): " + maxArea);
        
        // Find maximum area using 8-directional movement
        int maxArea8Dir = solution.maxAreaOfIsland8Directions(grid);
        System.out.println("Maximum area of island (8 directions): " + maxArea8Dir);
        
        // Find all island areas
        java.util.List<Integer> allAreas = solution.findAllIslandAreas(grid);
        System.out.println("All island areas: " + allAreas);
        
        // Additional test cases
        System.out.println("\n=== Additional Test Cases ===");
        
        // Test case 1: Single island
        int[][] grid1 = {
            {1, 1, 0},
            {1, 1, 0},
            {0, 0, 0}
        };
        System.out.println("Test Case 1:");
        solution.printGrid(grid1);
        System.out.println("Max area: " + solution.maxAreaOfIsland(grid1));
        
        // Test case 2: Multiple islands
        int[][] grid2 = {
            {1, 0, 0, 0, 0},
            {1, 1, 0, 0, 0},
            {0, 0, 0, 1, 1},
            {0, 0, 0, 1, 1}
        };
        System.out.println("\nTest Case 2:");
        solution.printGrid(grid2);
        System.out.println("Max area: " + solution.maxAreaOfIsland(grid2));
        
        // Test case 3: No islands
        int[][] grid3 = {
            {0, 0, 0},
            {0, 0, 0},
            {0, 0, 0}
        };
        System.out.println("\nTest Case 3:");
        solution.printGrid(grid3);
        System.out.println("Max area: " + solution.maxAreaOfIsland(grid3));
        
        // Test case 4: Single cell island
        int[][] grid4 = {
            {0, 0, 0},
            {0, 1, 0},
            {0, 0, 0}
        };
        System.out.println("\nTest Case 4:");
        solution.printGrid(grid4);
        System.out.println("Max area: " + solution.maxAreaOfIsland(grid4));
        
        // Performance test
        System.out.println("\n=== Performance Test ===");
        int[][] largeGrid = new int[100][100];
        
        // Create a large grid with some islands
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                if (i % 10 == 0 && j % 10 == 0) {
                    // Create some islands
                    for (int di = 0; di < 3 && i + di < 100; di++) {
                        for (int dj = 0; dj < 3 && j + dj < 100; dj++) {
                            largeGrid[i + di][j + dj] = 1;
                        }
                    }
                }
            }
        }
    }
} 





class Solution {
public:
    int cs;
    void dfs(vector<vector<int>>& grid,vector<vector<bool>> &visited,int i,int j,int r,int c){
        visited[i][j]=true;

//in this we are considering the island in 4 direction in only 4 direction
        int dx[]={1,-1,0,0};
        int dy[]={0,0,1,-1};
//for considering the diagonal also need modify the dx and dy
// Include diagonal movements (8 directions total)
//int dx[] = {0, 0, 1, -1, 1, 1, -1, -1}; // Right, left, down, up, down-right, down-left, up-right, up-left
//int dy[] = {1, -1, 0, 0, 1, -1, 1, -1};
    
        for(int k=0;k<4;k++){
            int nx=i+dx[k];
            int ny=j+dy[k];
            
            if(nx>=0 and ny>=0 and nx<r and ny<c and grid[nx][ny]==1 and visited[nx][ny]== 0){
                dfs(grid,visited,nx,ny,r,c);
                cs++;
            }
        }
        
        return;
    }
    
    int maxAreaOfIsland(vector<vector<int>>& grid) {
        int r=grid.size();
        int c=grid[0].size();
        vector<vector<bool>> visited(r,vector<bool>(c,false));
        int largest=0;
        
        for(int i=0;i<r;i++){
            for(int j=0;j<c;j++){
                if(!visited[i][j] and grid[i][j]==1){
                    cs=1;
                    dfs(grid,visited,i,j,r,c);
                    largest=max(largest,cs);
                }
            }
        }
        return largest;
    }
};



//example
vector<vector<int>> adj = {
		 {1, 0, 0, 0, 1, 0, 0},
		 {0, 1, 0, 0, 1, 1, 1},
		 {1, 1, 0, 0, 0, 0, 0},
		 {1, 0, 0, 1, 1, 0, 0},
		 {1, 0, 0, 1, 0, 1, 1}};
