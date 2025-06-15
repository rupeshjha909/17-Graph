class graph
{
public:
	int dfsHelper(vector<vector<int>> &adjList, vector<bool> &vis, int node, int parent)
	{
		vis[node] = true;
		for (auto nbr : adjList[node])
		{
			if (!vis[nbr])
			{
				if (dfsHelper(adjList, vis, nbr, node))
				{
					return true;
				}
			}
			else if (nbr != parent)
			{
				return true;
			}
		}
		return false;
	}

	bool dfs(vector<vector<int>> &adjList, int v)
	{
		vector<bool> vis(v, false);
		for (auto i = 0; i < v; i++)
		{
			if (!vis[i])
			{
				if (dfsHelper(adjList, vis, i, -1))
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
		 {3, 4}};

	int n = 5;							  // Number of nodes (0-4)
	vector<vector<int>> adjList(n); // n is number of nodes
	for (auto &edge : edges)
	{
		adjList[edge[0]].push_back(edge[1]);
		adjList[edge[1]].push_back(edge[0]); // For undirected graph
	}
	graph g;
	cout << "Is cycle present? " << (g.dfs(adjList, n) ? "Yes" : "No") << endl;

	return 0;
}
