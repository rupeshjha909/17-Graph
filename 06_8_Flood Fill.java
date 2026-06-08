class Solution {
    void dfs(int [][]image,int x,int y,int r,int c,int color,int originalColour){
        image[x][y]=color;
        int dx[]={1,0,-1,0};
        int dy[]={0,1,0,-1};
        for(int k=0;k<4;k++){
            int nx=x+dx[k];
            int ny=y+dy[k];
            if(nx>=0 && nx<r && ny>=0 && ny<c && image[nx][ny] == originalColour){
                dfs(image,nx,ny,r,c,color,originalColour);
            }
        }
    }
    
    public int[][] floodFill(int[][] image, int sr, int sc, int color) {
        int r=image.length;
        int c=image[0].length;
        int originalColour=image[sr][sc];
        if(sr>=0 && sr<r && sc>=0 && sc<c && image[sr][sc] != color){
            dfs(image,sr,sc,r,c,color,originalColour);
        }
        return image;
    }
}
