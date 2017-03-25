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
	//���캯��
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
	//�������
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
		double[] k=new double[n];//���ֶȵĴ�С
		double[] delta_k=new double[n];//�ȵ�������
		int[] randmp=new int[n];//�������Ӹ���
		int[] I=new int[n];//�����Ԫ����ԭ�������е�λ��
		double[] Q=new double[n];
		double[] M=new double[n];
		
		double a=average_path_length(G);//��ʼƽ��·��
		//System.out.println(a);
		int dis=all_path_length();//�ڵ���ܾ���(����)
		int[][] d=get_D_matrix();
		
		while(rinit<=RMAX){//���뾶֮��
		     Q=get_matrix_by_radius_Q(rinit);
		     M=get_matrix_by_radius_M(rinit);
		    //�ȵ�Ӱ������
		    //����Ȳ�����
		    for(int i=0;i<n;i++){
		    	if(Math.random()<mp){
		            randmp[i]=1;
		    	}
		        else{
		        	randmp[i]=0;
		    	}//�Ը���mp����
		        delta_k[i]=M[i]+Q[i];//�ȵ�����������һ�������
		        k[i]=k[i]+randmp[i]*H*delta_k[i];//�����Ӻ�Ľ��
		    }
		    //��������
		    for(int m=0;m<n;m++){
		        int index=0;
		        for(int j=1;j<n-m;j++){
		        	if(k[j]<k[index]){
		        		index=j;
		        	}
		        }
		        I[n-m-1]=index;//����ǰ�Ľڵ����
		        double temp=k[n-1-m];
		        k[n-1-m]=k[index];
		        k[index]=temp;
		    }
//		    aplStr.add(e)
//		    int numAddLink = 0;
		    for(int i=0;i<n;i++){
		        int itemp=I[i];//�Ӷ����Ľڵ㿪ʼ     �������·��ȱ仯����ô�죿������
		        double temp=delta_k[i];
		        if(temp>0){
		            temp=randmp[i]*H*delta_k[i];
		        }
		        if(temp==0){//��û������
		        	continue;
		        }
		        for(int j=0;j<n;j++){ 
		            if(G[itemp][j]==1){//�����Ѵ���
		                continue;
		            }             
		            if(d[j][itemp]>=rinit && d[j][itemp]<rinit+RSTEP){  
		                // �ڵ���RMIN��RMAX֮��
		                // ����cost-effective
		                double apl1=average_path_length(G);//�������ǰ
		                double aplTemp = average_path_length(Atemp);
		                Atemp[itemp][j]=1;
		                Atemp[j][itemp]=1;
		                double apl2=average_path_length(Atemp);//�ٶ����Ӻ�
		                
		                //double oc=W[itemp][j]/dis;
		                double oc=d[itemp][j]/dis;
		                double sc=(apl1-apl2)/a;
		                double costeffective=oc/sc;
		                //����µ���·
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
		 rinit=rinit+RSTEP;//�뾶����    
		} 
		return Aadd;
	}
	public String[][] get_aplArr(){
		return aplArr;
	}
	//ƽ�����·��
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
			d[i]=simple_dijkstra(G,i);//Dijkstra�㷨
		}
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				sumd=sumd+d[i][j];//������
			}
		}
	    apl=(double)sumd/(n*n-n);//��������
	    return apl;
     }
	//�ڵ���ܾ���
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
			d[i]=simple_dijkstra(A,i);//Dijkstra�㷨
		}
    	for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				sumd=sumd+d[i][j];//������
			}
		}
    	return sumd;
     }
    public int[][] get_D_matrix(){
    	int n=A.length;
    	int[][] d=new int[n][n];
    	for(int i=0;i<n;i++){
			d[i]=simple_dijkstra(A,i);//Dijkstra�㷨
		}
    	return d;
    }
    //����M
    public double[] get_matrix_by_radius_M(int r){
 		int n=A.length;
 		int[] cnt=new int[n];
 		int[] inr=new int[n];
 		double[] M=new double[n];
 		int[][] D=get_D_matrix();
 		
 		for(int i=0;i<n;i++){
 		    inr[i]=1;
 		    for(int j=0;j<n;j++){
 		        if(D[i][j]<=r){ //i to j ����С�ڰ뾶
 		           cnt[i]=cnt[i]+1;//iΪ���ģ��뾶r�ڽڵ���
 		           inr[j]=1;//j �ڰ뾶��
 		        }
 		    }
 		}
 		for(int i=0;i<n;i++){
 		    M[i]=cnt[i]/n;//��һ��
 		}
 		return M;
 	}
    //����Q
 	public double[] get_matrix_by_radius_Q(int r){
 			int n=A.length;
 			int[][][] Ar=new int[n][n][n];//�Ե�һ��nΪ���ģ�ָ���뾶�ڵ��ڽӾ���
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
 			        if(d[i][j]<=r){ //i to j ����С�ڰ뾶
 			            inr[j]=1;//j �ڰ뾶��
 			        }
 			    }
 			    //�뾶r�ڽڵ���ڽӾ���
 			    for(int w=0;w<n;w++){
 			        if(inr[w]==1){ //w �ڰ뾶��
 			            for(int s=0;s<n;s++){
 			                if(inr[s]==1){ //s �ڰ뾶��
 			                   Ar[i][w][s]=A[w][s];
 			                }
 			            }
 			        }
 			    }
 			}
 			//Q1
 			int[] D=cal_degree();//�ڵ��
 			for(int i=0;i<n;i++){
 			    D[i]=D[i]+1; //��֤��Ϊ0
 			}
 			for(int i=0;i<n;i++) {
 	            sumD=sumD+D[i];
 	        }
 			for(int i=0;i<n;i++){
 			    Q1[i]=D[i]/sumD;//��һ��
 			}
            //Q2
 			for(int i=0;i<n;i++){				
 				for(int w=0;w<n;w++){
 			        for(int s=0;s<n;s++){
 			            B[w][s]=Ar[i][w][s];
 			        }
 				}
 			    double c=closeness(B,i);
 			    C[i]=c+1;//��֤��Ϊ0
 			}
 			for(int i=0;i<n;i++) {
 	            sumC=sumC+C[i];
 	        }
 			for(int i=0;i<n;i++){
 			    Q2[i]=C[i]/sumC;//��һ��
 			}
 			//Q
 			for(int i=0;i<n;i++){
 			    Q[i]=Q1[i]*Q2[i];
 			}
 			return Q;
 	}
 	//�ȼ���
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
 	//�����Լ���
    public double closeness(int[][] adj,int node){
 		int[] d=simple_dijkstra(adj,node);
 		double C=0;
 	    for(int j=0;j<adj.length;j++) {
 	             C=C+d[j];
 	    }
 		if(C==0){//%��ĸΪ0
 		   C=0;
 		}
 		else{
 		    C=1/C;
 		}
 		return C;
     }
    //Dijkstra�㷨
    public int[] simple_dijkstra(int[][] adj,int node) {
 		 int n = adj.length;      //�������
 		 int[] shortPath = new int[n];//����node��������������·�� 
 		 int[][] Tempadj=new int[n][n];
 		 for(int i=0;i<n;i++){
 			 for(int j=0;j<n;j++){
 				 Tempadj[i][j]=adj[i][j];
 				 if(adj[i][j]==0 && i!=j){
 				    Tempadj[i][j]=Integer.MAX_VALUE;
 				 }
 			 }
 		 }
 		
 		 int[] visited = new int[n];//��ǵ�ǰ�ö�������·���Ƿ��Ѿ����,1��ʾ����� 
 		 //��ʼ������һ�������Ѿ����
 		 shortPath[node] = 0;
 		 visited[node] = 1;
 		  
 		 for(int count=1;count<n;count++){//Ҫ����n-1������
 		     int k=0;        
 		     int dmin=Integer.MAX_VALUE;
 		     //ѡ��һ�������ʼ����start�����δ��Ƕ��� 
 		     for(int i=0;i<n;i++) {
 		         if(visited[i]==0 && Tempadj[node][i]<dmin && Tempadj[node][i]>0) {
 		         dmin=Tempadj[node][i];
 		         k=i;
 		         }
 		     }
 		    //����ѡ���Ķ�����Ϊ��������·�����ҵ�node�����·������dmin 
 		     shortPath[k] = dmin;
 		     visited[k] = 1;
 		    //��kΪ�м�㣬������node��δ���ʸ���ľ��� 
 		     for(int i=0;i<n;i++) {
 		         if(visited[i]==0 && Tempadj[node][k]+Tempadj[k][i]<Tempadj[node][i] && Tempadj[node][k]+Tempadj[k][i]>0 ){
 		        	 Tempadj[node][i] = Tempadj[node][k] + Tempadj[k][i];
 		         }
 		     }
 		 }
 		 return shortPath;
   }
}



