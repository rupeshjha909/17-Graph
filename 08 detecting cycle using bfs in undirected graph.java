import java.util.*;
import java.io.*;

class Graph {
    public boolean isCyclicBfs(ArrayList<ArrayList<Integer>> adjList, int src, int n) {
        Queue<Integer> q = new ArrayDeque<>();
        boolean[] vis = new boolean[n];
        int[] parent = new int[n];
        Arrays.fill(parent, -1);
        
        q.add(src);
        vis[src] = true;
        
        while (!q.isEmpty()) {
            int node = q.poll();
            for (int nbr : adjList.get(node)) {
                if (!vis[nbr]) {
                    vis[nbr] = true;
                    parent[nbr] = node;
                    q.add(nbr);
                } else if (parent[node] != nbr) {
                    return true;
                }
            }
        }
        return false;
    }
}

public class CycleDetection {
    public static void main(String[] args) {
        try {
            // Redirect input and output (Java equivalent)
            System.setIn(new FileInputStream("input.txt"));
            System.setOut(new PrintStream(new FileOutputStream("output.txt")));
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        }
        
        // Define edges directly as a list of arrays
        int[][] edges = {
            {0, 1},
            {0, 2},
            {0, 3},
            {1, 2},
            {3, 4}
        };
        
        int n = 5; // Number of nodes (0-4)
        ArrayList<ArrayList<Integer>> adjList = new ArrayList<>();
        
        // Initialize adjacency list
        for (int i = 0; i < n; i++) {
            adjList.add(new ArrayList<>());
        }
        
        // Build adjacency list from edges
        for (int[] edge : edges) {
            adjList.get(edge[0]).add(edge[1]);
            adjList.get(edge[1]).add(edge[0]); // For undirected graph
        }
        
        Graph g = new Graph();
        boolean hasCycle = g.isCyclicBfs(adjList, 0, n);
        System.out.println("Is cycle present? " + (hasCycle ? "Yes" : "No"));
    }
}




class graph
{
	public:
	bool isCyclicBfs(vector<vector<int>>& adjList, int src, int n)
	{
		queue<int> q;
		vector<bool> vis(n, false);
		vector<int> parent(n, -1);
		
		q.push(src);
		vis[src] = true;
		
		while (!q.empty())
		{
			auto node = q.front();
			q.pop();
			for (auto nbr : adjList[node])
			{
				if (!vis[nbr])
				{
					vis[nbr] = true;
					parent[nbr] = node;
					q.push(nbr);
				}
				else if (parent[node] != nbr)	
				{
					return true;
				}
			}
			
		}
		return false;
	}
};

int main()
{
	// Redirect input and output
	freopen("input.txt", "r", stdin);
	freopen("output.txt", "w", stdout);
	
	// Define edges directly as a vector of vectors
	vector<vector<int>> edges = {
		{0, 1},
		{0, 2},
		{0, 3},
		{1, 2},
		{3, 4}
	};
	
	int n = 5; // Number of nodes (0-4)
	vector<vector<int>> adjList(n); // n is number of nodes
	for (auto &edge : edges)
	{
		adjList[edge[0]].push_back(edge[1]);
		adjList[edge[1]].push_back(edge[0]); // For undirected graph
	}
	graph g;
	cout << "Is cycle present? " << (g.isCyclicBfs(adjList, 0, n) ? "Yes" : "No") << endl;

	return 0;
}
