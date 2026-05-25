import java.util.*;

public class GraphCycleDSU {
    
    // Inner class representing the graph
    static class Graph {
        int V;                          // Number of vertices
        List<int[]> edgeList;           // Edge list (each edge is int[]{u, v})
        
        public Graph(int V) {
            this.V = V;
            this.edgeList = new ArrayList<>();
        }
        
        /**
         * Add an edge between u and v to the edge list.
         */
        public void addEdge(int u, int v) {
            edgeList.add(new int[]{u, v});
        }
        
        /**
         * FIND operation with path compression.
         * Finds the root (representative) of the set containing i.
         * @param i Element to find root for
         * @param parent Parent map (parent.get(i) = parent of i; -1 means i is root)
         * @return Root of the set containing i
         */
        private int findSet(int i, Map<Integer, Integer> parent) {
            // Base case: if i's parent is -1, i is the root of its set
            if (parent.get(i) == -1) {
                return i;
            }
            // Recursive case: find root and apply path compression
            // (point i directly to the root for future O(1) lookups)
            int root = findSet(parent.get(i), parent);
            parent.put(i, root);  // path compression
            return root;
        }
        
        /**
         * UNION operation with union by rank (size).
         * Merges the sets containing x and y.
         * @param x Root of first set
         * @param y Root of second set
         * @param parent Parent map
         * @param rank Rank map (used for union by size)
         */
        private void unionSet(int x, int y,
                              Map<Integer, Integer> parent,
                              Map<Integer, Integer> rank) {
            int s1 = findSet(x, parent);
            int s2 = findSet(y, parent);
            
            if (s1 != s2) {
                if (rank.get(s1) < rank.get(s2)) {
                    // s2 has larger rank → attach s1 under s2
                    parent.put(s1, s2);
                    rank.put(s2, rank.get(s1) + rank.get(s2));
                } else {
                    // s1 has equal or larger rank → attach s2 under s1
                    parent.put(s2, s1);
                    rank.put(s1, rank.get(s1) + rank.get(s2));
                }
            }
        }
        
        /**
         * Check if the graph contains a cycle using DSU (Disjoint Set Union).
         * @return true if cycle exists, false otherwise
         */
        public boolean containsCycle() {
            // Initialize parent and rank maps
            Map<Integer, Integer> parent = new HashMap<>();
            Map<Integer, Integer> rank = new HashMap<>();
            
            for (int i = 0; i < V; i++) {
                parent.put(i, -1);  // -1 means this node is its own parent (root)
                rank.put(i, 1);     // initial rank/size is 1
            }
            
            // Iterate over each edge
            for (int[] edge : edgeList) {
                int u = edge[0];
                int v = edge[1];
                
                int s1 = findSet(u, parent);
                int s2 = findSet(v, parent);
                
                if (s1 != s2) {
                    // Different sets → safe to union (no cycle)
                    unionSet(s1, s2, parent, rank);
                } else {
                    // Same set → adding this edge creates a cycle!
                    System.out.println("same parents " + s1 + " and " + s2);
                    return true;
                }
            }
            
            return false;  // No cycle detected
        }
    }
    
    public static void main(String[] args) {
        // Test 1: Graph WITHOUT cycle
        Graph g = new Graph(4);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        // g.addEdge(3, 0);  // Uncomment to add cycle
        
        System.out.println("Test 1 (no cycle expected): " + g.containsCycle());
        
        // Test 2: Graph WITH cycle
        Graph g2 = new Graph(4);
        g2.addEdge(0, 1);
        g2.addEdge(1, 2);
        g2.addEdge(2, 3);
        g2.addEdge(3, 0);
        
        System.out.println("Test 2 (cycle expected): " + g2.containsCycle());
        
        // Test 3: Triangle
        Graph g3 = new Graph(3);
        g3.addEdge(0, 1);
        g3.addEdge(1, 2);
        g3.addEdge(2, 0);
        
        System.out.println("Test 3 (cycle expected): " + g3.containsCycle());
        
        // Test 4: Disconnected with cycle
        Graph g4 = new Graph(5);
        g4.addEdge(0, 1);
        g4.addEdge(2, 3);
        g4.addEdge(3, 4);
        g4.addEdge(4, 2);
        
        System.out.println("Test 4 (cycle expected): " + g4.containsCycle());
    }
}
