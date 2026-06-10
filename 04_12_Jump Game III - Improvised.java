class Solution {
    public int minJumpsToZero(int[] arr, int start) {
        int n = arr.length;
        if (arr[start] == 0) return 0;                  // already on a 0

        boolean[] seen = new boolean[n];
        seen[start] = true;
        Queue<int[]> queue = new ArrayDeque<>();
        queue.offer(new int[]{start, 0});               // {index, jumps}

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int i = cur[0], jumps = cur[1];

            for (int next : new int[]{i + arr[i], i - arr[i]}) {
                if (next >= 0 && next < n && !seen[next]) {
                    if (arr[next] == 0) return jumps + 1;   // first arrival = fewest jumps
                    seen[next] = true;                       // mark on enqueue
                    queue.offer(new int[]{next, jumps + 1});
                }
            }
        }
        return -1;                                       // no 0 reachable
    }
}
