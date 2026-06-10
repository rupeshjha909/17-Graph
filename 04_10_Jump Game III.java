//dfs approach
class Solution {
    public boolean canReach(int[] arr, int start) {
        if (arr[start] == 0) return true;            // caller checks the first index
        boolean[] seen = new boolean[arr.length];
        return dfs(arr, start, seen);
    }

    private boolean dfs(int[] arr, int i, boolean[] seen) {
        seen[i] = true;                              // (i) is valid land; mark it

        int[] nexts = { i + arr[i], i - arr[i] };    // the two jumps
        for (int next : nexts) {
            if (next >= 0 && next < arr.length && !seen[next]) {   // validate BEFORE recursing
                if (arr[next] == 0) return true;     // neighbor is a goal → done
                if (dfs(arr, next, seen)) return true;
            }
        }
        return false;
    }
}

//bfs approach
class Solution {
    public boolean canReach(int[] arr, int start) {
        if (arr[start] == 0) return true;
        int n = arr.length;
        boolean[] seen = new boolean[n];
        Queue<Integer> queue = new ArrayDeque<>();
        queue.offer(start);
        seen[start] = true;

        while (!queue.isEmpty()) {
            int i = queue.poll();
            for (int next : new int[]{i + arr[i], i - arr[i]}) {
                if (next >= 0 && next < n && !seen[next]) {
                    if (arr[next] == 0) return true;
                    seen[next] = true;
                    queue.offer(next);
                }
            }
        }
        return false;
    }
}


//without extra space
class Solution {
    public boolean canReach(int[] arr, int start) {
        if (arr[start] == 0) return true;          // caller checks the first index
        return dfs(arr, start);
    }

    private boolean dfs(int[] arr, int i) {
        int step = arr[i];                         // save before marking (we lose it after)
        arr[i] = -arr[i];                          // negative = visited

        int[] nexts = { i + step, i - step };      // the two jumps
        for (int next : nexts) {
            if (next >= 0 && next < arr.length && arr[next] >= 0) {   // in bounds & not visited
                if (arr[next] == 0) return true;   // neighbor is a goal → done
                if (dfs(arr, next)) return true;
            }
        }
        return false;
    }
}
