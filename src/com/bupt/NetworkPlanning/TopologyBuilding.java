package com.bupt.NetworkPlanning;


public class TopologyBuilding {

	private int[][] A;
	private String[][] aplArr;
//	private ArrayList<String> aplStr;
	//private double[][] W;
	private int RMIN;
	private int RMAX;
	private int RSTEP;
	private double mp;
	private int H;
	private double alpha;
	//构造函数
	public TopologyBuilding(int[][] a2,int r1,int r2,int rstep,double m,int h,double al){
		this.A=a2;
//		this.aplStr = new ArrayList<>();
		this.aplArr = new String[A.length][A.length];
		//this.W=w2;
		this.RMIN=r1;
		this.RMAX=r2;
		this.RSTEP=rstep;
		this.mp=m;
		this.H=h;
		this.alpha=al;
	}
	//get
	public int[][] get_adjacency_matrix(){
		return this.A;
	}
	//public double[][] get_weigh_matrix(){
		//return this.W;
	//}
	public int getRMIN(){
		return this.RMIN;
	}
	public int getRMAX(){
		return this.RMAX;
	}
	public int getRSTEP(){
		return this.RSTEP;
	}
	public double getmp(){
		return this.mp;
	}
	public int getH(){
		return this.H;
	}
	public double getalpha(){
		return this.alpha;
	}
	//添加链接
	public int[][] sdn_continuetop_improve(){
		int n=A.length;
		int[][] G=new int[n][n];
		int[][] Aadd=new int[n][n];
		int[][] Atemp=new int[n][n];
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				G[i][j]=A[i][j];
				Aadd[i][j]=A[i][j];		
				Atemp[i][j]=A[i][j];
			}
		}
		int rinit=RMIN;
		double[] k=new double[n];//体现度的大小
		double[] delta_k=new double[n];//度的增加量
		int[] randmp=new int[n];//体现连接概率
		int[] I=new int[n];//排序后元素在原来矩阵中的位置
		double[] Q=new double[n];
		double[] M=new double[n];
		
		double a=average_path_length(G);//初始平均路径
		//System.out.println(a);
		int dis=all_path_length();//节点间总距离(跳数)
		int[][] d=get_D_matrix();
		
		while(rinit<=RMAX){//最大半径之内
		     Q=get_matrix_by_radius_Q(rinit);
		     M=get_matrix_by_radius_M(rinit);
		    //度的影响因子
		    //计算度并排序
		    for(int i=0;i<n;i++){
		    	if(Math.random()<mp){
		            randmp[i]=1;
		    	}
		        else{
		        	randmp[i]=0;
		    	}//以概率mp连接
		        delta_k[i]=M[i]+Q[i];//度的增加量，归一化处理过
		        k[i]=k[i]+randmp[i]*H*delta_k[i];//度增加后的结果
		    }
		    //降序排序
		    for(int m=0;m<n;m++){
		        int index=0;
		        for(int j=1;j<n-m;j++){
		        	if(k[j]<k[index]){
		        		index=j;
		        	}
		        }
		        I[n-m-1]=index;//排序前的节点序号
		        double temp=k[n-1-m];
		        k[n-1-m]=k[index];
		        k[index]=temp;
		    }
//		    aplStr.add(e)
//		    int numAddLink = 0;
		    for(int i=0;i<n;i++){
		        int itemp=I[i];//从度最大的节点开始     但添加链路后度变化了怎么办？？？？
		        double temp=delta_k[i];
		        if(temp>0){
		            temp=randmp[i]*H*delta_k[i];
		        }
		        if(temp==0){//度没有增加
		        	continue;
		        }
		        for(int j=0;j<n;j++){ 
		            if(G[itemp][j]==1){//连接已存在
		                continue;
		            }             
		            if(d[j][itemp]>=rinit && d[j][itemp]<rinit+RSTEP){  
		                // 节点在RMIN和RMAX之间
		                // 计算cost-effective
		                double apl1=average_path_length(G);//添加链接前
		                double aplTemp = average_path_length(Atemp);
		                Atemp[itemp][j]=1;
		                Atemp[j][itemp]=1;
		                double apl2=average_path_length(Atemp);//假定连接后
		                
		                //double oc=W[itemp][j]/dis;
		                double oc=d[itemp][j]/dis;
		                double sc=(apl1-apl2)/a;
		                double costeffective=oc/sc;
		                //添加新的链路
		                if(costeffective<alpha && itemp!=j){
		                	if(aplTemp>apl2){
		                		Aadd[itemp][j]=1;
				                Aadd[j][itemp]=1;
				                aplArr[itemp][j] = String.format("%.2f", apl2);
				                aplArr[j][itemp] = String.format("%.2f", apl2);
//				                numAddLink++;
//				                if (numAddLink==2) 
				                break;
		                	}
		                   
		                }
		                 
		                }                  
		            }
		        
		    }    
		 rinit=rinit+RSTEP;//半径扩大    
		} 
		return Aadd;
	}
	public String[][] get_aplArr(){
		return aplArr;
	}
	//平均最短路径
	public double average_path_length(int[][] adj){
	    int n=adj.length;
	    int[][] G=new int[n][n];
	    for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				G[i][j]=adj[i][j];
			}
	    }
	    double apl=0;
		int[][] d=new int[n][n];
		int sumd=0;
		
		for(int i=0;i<n;i++){
			d[i]=simple_dijkstra(G,i);//Dijkstra算法
		}
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				sumd=sumd+d[i][j];//总跳数
			}
		}
	    apl=(double)sumd/(n*n-n);//数据类型
	    return apl;
     }
	//节点间总距离
    public int all_path_length(){
		/*int n=A.length;
		double L=0;
		for(int i=0;i<n;i++){
		    for(int j=0;j<n;j++){
		        if(A[i][j]>0){
		           L=L+W[i][j];
		        }
		    }
		}
		return L;*/
    	int n=A.length;
    	int[][] d=new int[n][n];
		int sumd=0;
		
		for(int i=0;i<n;i++){
			d[i]=simple_dijkstra(A,i);//Dijkstra算法
		}
    	for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				sumd=sumd+d[i][j];//总跳数
			}
		}
    	return sumd;
     }
    public int[][] get_D_matrix(){
    	int n=A.length;
    	int[][] d=new int[n][n];
    	for(int i=0;i<n;i++){
			d[i]=simple_dijkstra(A,i);//Dijkstra算法
		}
    	return d;
    }
    //参数M
    public double[] get_matrix_by_radius_M(int r){
 		int n=A.length;
 		int[] cnt=new int[n];
 		int[] inr=new int[n];
 		double[] M=new double[n];
 		int[][] D=get_D_matrix();
 		
 		for(int i=0;i<n;i++){
 		    inr[i]=1;
 		    for(int j=0;j<n;j++){
 		        if(D[i][j]<=r){ //i to j 距离小于半径
 		           cnt[i]=cnt[i]+1;//i为中心，半径r内节点数
 		           inr[j]=1;//j 在半径内
 		        }
 		    }
 		}
 		for(int i=0;i<n;i++){
 		    M[i]=cnt[i]/n;//归一化
 		}
 		return M;
 	}
    //参数Q
 	public double[] get_matrix_by_radius_Q(int r){
 			int n=A.length;
 			int[][][] Ar=new int[n][n][n];//以第一个n为中心，指定半径内的邻接矩阵。
 			int[][] B=new int[n][n];
 			int[] inr=new int[n];
 			double[] Q1=new double[n];
 			double[] Q2=new double[n];
 			double[] Q=new double[n];
 			double[] C=new double[n];
 			double sumC=0;
 			int sumD=0;
 			int[][] d=get_D_matrix();
 			
 			for(int i=0;i<n;i++){
 			    inr[i]=1;
 			    for(int j=0;j<n;j++){
 			        if(d[i][j]<=r){ //i to j 距离小于半径
 			            inr[j]=1;//j 在半径内
 			        }
 			    }
 			    //半径r内节点的邻接矩阵
 			    for(int w=0;w<n;w++){
 			        if(inr[w]==1){ //w 在半径内
 			            for(int s=0;s<n;s++){
 			                if(inr[s]==1){ //s 在半径内
 			                   Ar[i][w][s]=A[w][s];
 			                }
 			            }
 			        }
 			    }
 			}
 			//Q1
 			int[] D=cal_degree();//节点度
 			for(int i=0;i<n;i++){
 			    D[i]=D[i]+1; //保证不为0
 			}
 			for(int i=0;i<n;i++) {
 	            sumD=sumD+D[i];
 	        }
 			for(int i=0;i<n;i++){
 			    Q1[i]=D[i]/sumD;//归一化
 			}
            //Q2
 			for(int i=0;i<n;i++){				
 				for(int w=0;w<n;w++){
 			        for(int s=0;s<n;s++){
 			            B[w][s]=Ar[i][w][s];
 			        }
 				}
 			    double c=closeness(B,i);
 			    C[i]=c+1;//保证不为0
 			}
 			for(int i=0;i<n;i++) {
 	            sumC=sumC+C[i];
 	        }
 			for(int i=0;i<n;i++){
 			    Q2[i]=C[i]/sumC;//归一化
 			}
 			//Q
 			for(int i=0;i<n;i++){
 			    Q[i]=Q1[i]*Q2[i];
 			}
 			return Q;
 	}
 	//度计算
 	public int[] cal_degree(){
 		int n=A.length;
 		int[] D=new int[n];
 		for(int i=0;i<n;i++){
 		    for(int j=0;j<n;j++){
 		        if(i!=j && A[i][j]>0){
 		           D[i]=D[i]+1;
 		        }
 		    }
 		}
        return D;
     }
 	//中心性计算
    public double closeness(int[][] adj,int node){
 		int[] d=simple_dijkstra(adj,node);
 		double C=0;
 	    for(int j=0;j<adj.length;j++) {
 	             C=C+d[j];
 	    }
 		if(C==0){//%分母为0
 		   C=0;
 		}
 		else{
 		    C=1/C;
 		}
 		return C;
     }
    //Dijkstra算法
    public int[] simple_dijkstra(int[][] adj,int node) {
 		 int n = adj.length;      //顶点个数
 		 int[] shortPath = new int[n];//保存node到其他各点的最短路径 
 		 int[][] Tempadj=new int[n][n];
 		 for(int i=0;i<n;i++){
 			 for(int j=0;j<n;j++){
 				 Tempadj[i][j]=adj[i][j];
 				 if(adj[i][j]==0 && i!=j){
 				    Tempadj[i][j]=Integer.MAX_VALUE;
 				 }
 			 }
 		 }
 		
 		 int[] visited = new int[n];//标记当前该顶点的最短路径是否已经求出,1表示已求出 
 		 //初始化，第一个顶点已经求出
 		 shortPath[node] = 0;
 		 visited[node] = 1;
 		  
 		 for(int count=1;count<n;count++){//要加入n-1个顶点
 		     int k=0;        
 		     int dmin=Integer.MAX_VALUE;
 		     //选出一个距离初始顶点start最近的未标记顶点 
 		     for(int i=0;i<n;i++) {
 		         if(visited[i]==0 && Tempadj[node][i]<dmin && Tempadj[node][i]>0) {
 		         dmin=Tempadj[node][i];
 		         k=i;
 		         }
 		     }
 		    //将新选出的顶点标记为已求出最短路径，且到node的最短路径就是dmin 
 		     shortPath[k] = dmin;
 		     visited[k] = 1;
 		    //以k为中间点，修正从node到未访问各点的距离 
 		     for(int i=0;i<n;i++) {
 		         if(visited[i]==0 && Tempadj[node][k]+Tempadj[k][i]<Tempadj[node][i] && Tempadj[node][k]+Tempadj[k][i]>0 ){
 		        	 Tempadj[node][i] = Tempadj[node][k] + Tempadj[k][i];
 		         }
 		     }
 		 }
 		 return shortPath;
   }
}



