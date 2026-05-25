import java.util.*;

public class BipartiteCheck {
    
    // Adjacency list — array of lists (Java equivalent of vector<int> gr[N])
    private static List<List<Integer>> gr;
    
    // Visited/color array — 0 = unvisited, 1 = color A, 2 = color B
    private static int[] vis;
    
    // Flag to indicate if an odd-length cycle was detected
    private static boolean oddCycle = false;
    
    /**
     * DFS to color the graph and detect odd-length cycles.
     * @param cur Current node being visited
     * @param par Parent node (the one we came from)
     * @param col Color to assign to current node (1 or 2)
     */
    private static void dfs(int cur, int par, int col) {
        vis[cur] = col;
        
        // Visit all neighbors of current node
        for (int child : gr.get(cur)) {
            if (vis[child] == 0) {
                // Unvisited: recurse with opposite color
                // 3 - col flips between 1 and 2 (3-1=2, 3-2=1)
                dfs(child, cur, 3 - col);
            } else if (child != par && col == vis[child]) {
                // Already visited AND same color as current → odd cycle found
                // (Back edge to a node of the same color = odd-length cycle)
                oddCycle = true;
            }
        }
    }
    
    public static void solve(Scanner scanner) {
        // n = number of vertices, m = number of edges
        int n = scanner.nextInt();
        int m = scanner.nextInt();
        
        // Initialize adjacency list (1-indexed, so size n+1)
        gr = new ArrayList<>();
        for (int i = 0; i <= n; i++) {
            gr.add(new ArrayList<>());
        }
        
        // Initialize visited array (1-indexed)
        vis = new int[n + 1];
        oddCycle = false;
        
        // Read m edges (undirected, so add both directions)
        for (int i = 0; i < m; i++) {
            int x = scanner.nextInt();
            int y = scanner.nextInt();
            gr.get(x).add(y);
            gr.get(y).add(x);
        }
        
        // Start DFS from node 1 with color 1 and parent 0 (sentinel)
        dfs(1, 0, 1);
        
        if (oddCycle) {
            System.out.println("not bipartite");
        } else {
            System.out.println("bipartite");
        }
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        solve(scanner);
        scanner.close();
    }
}




std::vector<int> gr[N];//array of vector
int vis[N];
bool odd_cycle = 0;
//dfs has current assignment,parent and colour
void dfs(int cur, int par, int col)
{
    vis[cur] = col;
    //lets go to all child in this graph
    for (auto child : gr[cur])
    {
        if (vis[child] == 0)//if visited[child]==0 means not visited then make cur as parent
            dfs(child, cur, 3 - col);
        else if (child != par and col == vis[child])//if already visited and colour of current vertex==visited of next vertex
        {
            //backedge and odd length cycle
            odd_cycle = 1;
        }
    }
    return;
}

void solve()
{
    //n--number of vertices
    //m--number of edges
    int n, m;
    cin >> n >> m;
    for (int i = 0; i < m; i++)
    {
        int x, y;
        cin >> x >> y;
        //assume birdirection edges
        gr[x].pb(y);
        gr[y].pb(x);
    }
    dfs(1, 0, 1);
    if (odd_cycle)
        cout << "not bipartite\n";
    else
        cout << "bipartite";
}

int main()
{
    ios_base::sync_with_stdio(false);
    cin.tie(NULL);
    cout.tie(NULL);
    solve();
}
