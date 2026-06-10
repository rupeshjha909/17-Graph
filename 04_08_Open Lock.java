class Solution {
    public int openLock(String[] deadends, String target) {
        Set<String> dead = new HashSet<>(Arrays.asList(deadends));
        if (dead.contains("0000")) return -1;     // can't even start
        if (target.equals("0000")) return 0;      // already open

        Queue<String> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        queue.offer("0000");
        visited.add("0000");

        int moves = 0;
        while (!queue.isEmpty()) {
            int size = queue.size();              // process one BFS level at a time
            for (int s = 0; s < size; s++) {
                String state = queue.poll();
                if (state.equals(target)) return moves;

                // generate the 8 neighbors: each wheel up and down
                for (int i = 0; i < 4; i++) {
                    int d = state.charAt(i) - '0';
                    for (int delta : new int[]{1, 9}) {        // +1 (up) and +9 (= -1, down)
                        int nd = (d + delta) % 10;
                        String next = state.substring(0, i) + (char)('0' + nd)
                                                            + state.substring(i + 1);
                        if (!visited.contains(next) && !dead.contains(next)) {
                            visited.add(next);                 // mark on enqueue
                            queue.offer(next);
                        }
                    }
                }
            }
            moves++;                              // finished a level → one more move deep
        }
        return -1;                                // target never reached
    }
}
