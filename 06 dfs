import java.util.*;

/**
 * Graph Implementation with DFS in Java
 * Converted from C++ template class to Java using Integer
 * Supports bidirectional edges and DFS traversal
 */
public class GraphDFS {
    
    // Map to store adjacency list: node -> list of neighbors
    private Map<Integer, List<Integer>> adjacencyList;
    
    /**
     * Constructor
     */
    public GraphDFS() {
        this.adjacencyList = new HashMap<>();
    }
    
    /**
     * Add bidirectional edge between nodes x and y
     * 
     * @param x first node
     * @param y second node
     */
    public void addEdge(int x, int y) {
        // Add y to x's adjacency list
        adjacencyList.computeIfAbsent(x, k -> new ArrayList<>()).add(y);
        
        // Add x to y's adjacency list (bidirectional)
        adjacencyList.computeIfAbsent(y, k -> new ArrayList<>()).add(x);
    }
    
    /**
     * DFS helper method using recursion
     * 
     * @param src current node to visit
     * @param visited map to track visited nodes
     */
    private void dfsHelper(int src, Map<Integer, Boolean> visited) {
        // Print current node
        System.out.print(src + " ");
        
        // Mark current node as visited
        visited.put(src, true);
        
        // Get neighbors of current node
        List<Integer> neighbors = adjacencyList.get(src);
        if (neighbors != null) {
            // Go to all neighbors of that node one by one that are not visited
            for (int neighbor : neighbors) {
                if (!visited.getOrDefault(neighbor, false)) {
                    // If not visited then visit it
                    dfsHelper(neighbor, visited); // Go and visit the neighbor
                }
            }
        }
    }
    
    /**
     * Depth First Search traversal starting from source node
     * 
     * @param src source node to start DFS from
     */
    public void dfs(int src) {
        Map<Integer, Boolean> visited = new HashMap<>();
        
        // Mark all the nodes as not visited in the beginning
        for (Map.Entry<Integer, List<Integer>> entry : adjacencyList.entrySet()) {
            int node = entry.getKey();
            visited.put(node, false);
        }
        
        // Call the helper function
        System.out.print("DFS traversal starting from " + src + ": ");
        dfsHelper(src, visited);
        System.out.println();
    }
    
    public static void main(String[] args) {
        // Create graph instance (same as C++ example)
        GraphDFS g = new GraphDFS();
        
        // Add edges (same as C++ example)
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        g.addEdge(4, 5);
        g.addEdge(3, 0);
        
        // Perform DFS (same as C++ example)
        g.dfs(0);
    }
} 






template < typename T>
class graph
{
	map < T, list<T> >l;
public:
	void addEdge(int x, int y)//assume edges are bidirectional
	{
		l[x].push_back(y);
		l[y].push_back(x);
	}

	void dfs_helper(T src, map<T, bool> &visited)
	{
		//recursive function that will traverse the graph
		cout << src << " ";
		visited[src] = true;
		//go to all nbr of that node one by one that is not visited in the nbr of src
		for (auto nbr : l[src])
		{
			if (!visited[nbr])//if not visited then visit it
			{
				dfs_helper(nbr, visited);//go and visit the neighbour
			}
		}
	}

	void dfs(T src)
	{
		map<T, bool> visited;
		//mark all the nodes as not visited in the begining
		for (auto p : l)//this initialization is done only once but the function should be recursive so we have made dfs helper function
		{
			T node = p.first;
			visited[node] = false;
		}
		//call the helper function
		dfs_helper(src, visited);
	}
};

int main()
{
	ios_base::sync_with_stdio(false);
	cin.tie(NULL);
	cout.tie(NULL);
	graph<int> g;
	g.addEdge(0, 1);
	g.addEdge(1, 2);
	g.addEdge(2, 3);
	g.addEdge(3, 4);
	g.addEdge(4, 5);
	g.addEdge(3, 0);
	g.dfs(0);
}
