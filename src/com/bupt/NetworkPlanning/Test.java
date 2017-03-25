package com.bupt.NetworkPlanning;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.*;

public class Test {

	public static void main(String[] args) throws Exception {
		/*BuildPath bp = new BuildPath();
		String str = bp.netPlanning(0.5);
		System.out.println(str);*/
		String controllerIp="10.103.27.253";
		Map<String, String> headers=new HashMap<String, String>(); 
		String encoding = new String(org.apache.commons.codec.binary.Base64.encodeBase64
	    		 (org.apache.commons.codec.binary.StringUtils.getBytesUtf8("admin:admin")));
		 headers.put("Content-Type","application/json");
		 headers.put("Authorization","Basic "+encoding);//Basic后要加空格
		 String URL="http://"+controllerIp+":8181/restconf/operational/network-topology:network-topology";
		 String topo=HTTPUtils.httpGet(URL, headers);
		
		//将Json文件数据形成JSONObject对象
		JSONObject jsonObject = new JSONObject(topo);
		JSONObject network_topology = jsonObject.getJSONObject("network-topology");
		JSONArray topology = network_topology.getJSONArray("topology");
		//topology数组，每个对象含有topology-id，node数组，link数组	
		JSONObject[] topologyObject= new JSONObject[topology.length()];
		//topology对象可有多个，此处只有一个
		for(int i=0;i<topology.length();i++){
			topologyObject[i]=topology.getJSONObject(i);//i等价topology-id
		    //获取JSONObject对象数据
		    JSONArray node = topologyObject[i].getJSONArray("node");
		    //节点数组，每个对象含有node-id，termination-point数组(tp-id)
		    JSONArray link = topologyObject[i].getJSONArray("link");
		    //连接数组，每个对象含有link-id，source对象(source-tp,source-node)，destination对象(dest-tp,dest-node)
		      
		    String[] node_name = new String[node.length()];//交换机名称
		    String[] link_source = new String[link.length()];//源交换机
		    String[] link_destination = new String[link.length()];//目的交换机
		    //获取节点名称
		    for(int j=0;j<node.length();j++){
		    	node_name[j]=node.getJSONObject(j).getString("node-id");	
		    }
		    //获取连接信息
		    for (int j=0;j<link.length();j++) {
			   link_source[j]=link.getJSONObject(j).getJSONObject("source").getString("source-node");//源openflow交换机
			   link_destination[j]=link.getJSONObject(j).getJSONObject("destination").getString("dest-node");//目的openflow交换机
		    }
		    
		    //构造邻接矩阵
		    int[][] A=new int[node.length()][node.length()];//邻接矩阵
		    int ktemp=0,mtemp=0;
		    //交换机间的连接
		    for(int j=0;j<link.length();j++){
		    	for(int k=0;k<node.length();k++){
				    if(link_source[j].equals(node_name[k])){//源交换机
						 ktemp=k;
					}
		    	}
				for(int m=0;m<node.length();m++){//目的交换机
					 if(link_destination[j].equals(node_name[m])){
						 mtemp=m;
					 }
				 }
				 A[ktemp][mtemp]=1;
		    	}  
		    
		    for(int j=0;j<node.length();j++){
		    	for(int k=0;k<node.length();k++){
		    		System.out.print(A[j][k]+" ");
		    	}
		    	System.out.println();
		    }
		    
			    int RMIN=1;
				int RMAX=node.length();
				int RSTEP=1;
				double mp=0.5;
				int H=2;
				double alpha=0.7;
				
				TopologyBuilding S=new TopologyBuilding(A,RMIN,RMAX,RSTEP,mp,H,alpha);
				int[][] Aadd=S.sdn_continuetop_improve();
				
				double apl2=S.average_path_length(Aadd);
				int[][] E=S.get_adjacency_matrix();
				double apl1=S.average_path_length(E);
				//输出原矩阵
				for(int k=0;k<node.length();k++) {
					for(int j=0;j<node.length();j++) {
					    System.out.print(E[k][j]+" ");
					 }
					System.out.println();
				}
				
				System.out.println(apl1);
//				System.out.println("=====================================");
				
				
				for(int k=0;k<node.length();k++) {
					for(int j=0;j<node.length();j++) {
						Aadd[k][j] ^= E[k][j];
//						System.out.print(Aadd[k][j]+" ");
					}
//					System.out.println();
		        }
				System.out.println(apl2);
				
	
	  /*下发流表
	   * 
	   */
	  
	 
				StringBuilder nodePair = new StringBuilder(); 
				GetTopo test1=new GetTopo();
				DeliverFlow dw = new DeliverFlow();
				for(int k=0;k<node.length();k++) {
					for(int j=k+1;j<node.length();j++) {
						if(Aadd[k][j] == 1){
							List<Vertex> pathList=test1.getTopo().getShortestPath(node_name[k], node_name[j]);
							nodePair.append(node_name[k]+ "," + node_name[j] + "/");

							try {
								dw.installPath(pathList, 1, 1);//参数的作用
							} catch (NoLinkException e) {
								e.printStackTrace();
							}
						}
					}
				}

				/*for(int k=0;k<node.length();k++) {
					for(int j=k+1;j<node.length();j++) {
						if(Aadd[k][j] == 1){
							List<Vertex> pathList=test1.getTopo().getShortestPath(node_name[k], node_name[j]);
							nodePair.append(node_name[k]+ "," +node_name[j] + ","+S.get_aplArr()[k][j]+"/");
//							System.out.println(S.get_aplArr()[k][j]);
							try {
								dw.installPath(pathList, 1, 1);//参数的作用
							} catch (NoLinkException e) {
								e.printStackTrace();
							}
						}
					}
				}*/
				System.out.println(nodePair.toString().split("/").length);
		     }
	}

}
