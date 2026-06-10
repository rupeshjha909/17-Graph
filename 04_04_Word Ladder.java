// https://leetcode.com/problems/word-ladder/description/

// A transformation sequence from word beginWord to word endWord using a dictionary wordList is a sequence of words 
// beginWord -> s1 -> s2 -> ... -> sk such that:

// Every adjacent pair of words differs by a single letter.
// Every si for 1 <= i <= k is in wordList. Note that beginWord does not need to be in wordList.
// sk == endWord
// Given two words, beginWord and endWord, and a dictionary wordList, 
// return the number of words in the shortest transformation sequence from beginWord to endWord, 
// or 0 if no such sequence exists.

public static int ladderLength(String beginWord, String endWord, List<String> wordList) {
        // If endWord is not present in wordList, return 0
        if (!wordList.contains(endWord)) {
            return 0;
        }

        // Add all words to a set for O(1) lookup and removal
        Set<String> s = new HashSet<>(wordList);

        // BFS queue
        Queue<String> q = new LinkedList<>();
        q.offer(beginWord);

        int d = 0; // Level counter

        while (!q.isEmpty()) {
            d++; // Increase level for every level
            int n = q.size();

            while (n-- > 0) {
                String curr = q.poll();

                for (int i = 0; i < curr.length(); i++) {
                    char[] tmpArr = curr.toCharArray();

                    for (char c = 'a'; c <= 'z'; c++) {
                        tmpArr[i] = c;
                        String tmp = new String(tmpArr);

                        // If this is the same as the current string, skip
                        if (tmp.equals(curr)) {
                            continue;
                        }

                        // If we have found endWord, return d+1
                        if (tmp.equals(endWord)) {
                            return d + 1;
                        }

                        // If it is present in the set, push in queue and remove from set
                        if (s.contains(tmp)) {
                            q.offer(tmp);
                            s.remove(tmp);
                        }
                    }
                }
            }
        }
        return 0;
    }



// int ladderLength(string beginWord, string endWord, vector<string>& wordList) {
//         //if endword is not present in wordlist then return 0
//         if(find(wordList.begin(),wordList.end(),endWord)==wordList.end())
//             return 0;
//         set<string> s;
//         for(auto i:wordList)
//             s.insert(i);
//         queue<string> q;
//         q.push(beginWord);
//         int d=0;
//         while(!q.empty())
//         {
//             d++;//increasing level for every level
//             int n=q.size();
//             while(n--)
//             {
//                 string curr=q.front();
//                 q.pop();
//                 for(int i=0;i<curr.length();i++)
//                 {
//                     string tmp=curr;
//                     for(char c='a';c<='z';c++)
//                     {
//                         //replace with each character check if
//                         tmp[i]=c;
//                         //if this is popped string then leave
//                         if(tmp==curr)
//                             continue;
//                         //if we have found endword then return d+1
//                         if(tmp==endWord)
//                             return d+1;
//                         //if it is present then push in queue and remove temp from set
//                         if(s.find(tmp)!=s.end())
//                         {
//                             q.push(tmp);
//                             s.erase(tmp);
//                         }
//                     }
//                 }
//             }
//         }
//         return 0;
//     }
