class Solution {
    // Precomputed swap-neighbors for each blank index on a flattened 2x3 board:
    //   0 1 2
    //   3 4 5
    private static final int[][] NEIGHBORS = {
        {1, 3},      // index 0
        {0, 2, 4},   // index 1
        {1, 5},      // index 2
        {0, 4},      // index 3
        {1, 3, 5},   // index 4
        {2, 4}       // index 5
    };

    public int slidingPuzzle(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : board)
            for (int v : row) sb.append(v);
        String start = sb.toString();
        String target = "123450";
        if (start.equals(target)) return 0;

        Set<String> seen = new HashSet<>();
        seen.add(start);
        Queue<String> queue = new ArrayDeque<>();
        queue.offer(start);

        int moves = 0;
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int s = 0; s < size; s++) {          // process one BFS level
                String state = queue.poll();
                int zero = state.indexOf('0');
                for (int nb : NEIGHBORS[zero]) {
                    String next = swap(state, zero, nb);
                    if (next.equals(target)) return moves + 1;
                    if (!seen.contains(next)) {
                        seen.add(next);               // mark on enqueue
                        queue.offer(next);
                    }
                }
            }
            moves++;
        }
        return -1;   // target unreachable from this start
    }

    // swap two characters of the string and return the new string
    private String swap(String s, int i, int j) {
        char[] a = s.toCharArray();
        char tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
        return new String(a);
    }
}
