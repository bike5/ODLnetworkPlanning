package com.bupt.NetworkPlanning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.deser.impl.ExternalTypeHandler.Builder;

public class BuildPath {
	public static void main(String[] args) throws Exception {
		System.out.println(BuildPath.netPlanning(0.2));
	}
	public static String netPlanning(double a) throws JSONException{
		StringBuilder nodePair = new StringBuilder();
		String controllerIp="10.103.27.253";
		
		Map<String, String> headers=new HashMap<String, String>(); 
		String encoding = new String(org.apache.commons.codec.binary.Base64.encodeBase64
	    		 (org.apache.commons.codec.binary.StringUtils.getBytesUtf8("admin:admin")));
		 headers.put("Content-Type","application/json");
		 headers.put("Authorization","Basic "+encoding);//Basic��Ҫ�ӿո�
		 String URL="http://"+controllerIp+":8181/restconf/operational/network-topology:network-topology";
		 String topo=HTTPUtils.httpGet(URL, headers);
		
		//��Json�ļ������γ�JSONObject����
		JSONObject jsonObject = new JSONObject(topo);
		JSONObject network_topology = jsonObject.getJSONObject("network-topology");
		JSONArray topology = network_topology.getJSONArray("topology");
		//topology���飬ÿ��������topology-id��node���飬link����	
		JSONObject[] topologyObject= new JSONObject[topology.length()];
		//topology������ж�����˴�ֻ��һ��
		for(int i=0;i<topology.length();i++){
			topologyObject[i]=topology.getJSONObject(i);//i�ȼ�topology-id
		    //��ȡJSONObject��������
		    JSONArray node = topologyObject[i].getJSONArray("node");
		    //�ڵ����飬ÿ��������node-id��termination-point����(tp-id)
		    JSONArray link = topologyObject[i].getJSONArray("link");
		    //�������飬ÿ��������link-id��source����(source-tp,source-node)��destination����(dest-tp,dest-node)
		      
		    String[] node_name = new String[node.length()];//����������
		    String[] link_source = new String[link.length()];//Դ������
		    
		    String[] link_destination = new String[link.length()];//Ŀ�Ľ�����
		    //��ȡ�ڵ�����
		    for(int j=0;j<node.length();j++){
		    	node_name[j]=node.getJSONObject(j).getString("node-id");	
		    }
//		    for (int l = 0; l < node_name.length; l++)
//		    	System.out.println(node_name[l]);
		    //��ȡ������Ϣ
		    for (int j=0;j<link.length();j++) {
			   link_source[j]=link.getJSONObject(j).getJSONObject("source").getString("source-node");//Դopenflow������
			   link_destination[j]=link.getJSONObject(j).getJSONObject("destination").getString("dest-node");//Ŀ��openflow������
		    }
		    
		    //�����ڽӾ���
		    int[][] A=new int[node.length()][node.length()];//�ڽӾ���
		    int ktemp=0,mtemp=0;
		    //�������������
		    for(int j=0;j<link.length();j++){
		    	for(int k=0;k<node.length();k++){
				    if(link_source[j].equals(node_name[k])){//Դ������
						 ktemp=k;
					}
		    	}
				for(int m=0;m<node.length();m++){//Ŀ�Ľ�����
					 if(link_destination[j].equals(node_name[m])){
						 mtemp=m;
					 }
				 }
				 A[ktemp][mtemp]=1;
		    	}  
		    
			    final int RMIN=1;
				int RMAX=node.length();
				final int RSTEP=1;
				final double MP=0.5;
				int H=2;
				double alpha=a;
				
				TopologyBuilding S=new TopologyBuilding(A,RMIN,RMAX,RSTEP,MP,H,alpha);
				int[][] Aadd=S.sdn_continuetop_improve();
				
				double apl2=S.average_path_length(Aadd);
				int[][] E=S.get_adjacency_matrix();
				double apl1=S.average_path_length(E);
				
//				System.out.println(apl1);
//				System.out.println(apl2);
				for(int k=0;k<node.length();k++) {
					for(int j=0;j<node.length();j++) {
						Aadd[k][j] ^= E[k][j];
//						System.out.print(Aadd[k][j]+" ");
					}
//					System.out.println();
		        }
//				System.out.println(apl2);
				
				ArrayList<String> temp = new ArrayList<>();
				String[] portAndApl;
				nodePair.append(""+","+""+","+String.format("%.2f", apl1)+"/");
				
				//apl����
				for(int k=0;k<node.length();k++) {
					for(int j=k+1;j<node.length();j++) {
						if(Aadd[k][j] == 1){
							temp.add(S.get_aplArr()[k][j]+":"+k+":"+j);
//							System.out.println(S.get_aplArr()[k][j]);
						}
					}
				}
				Collections.sort(temp);
				
	/*
	 * �·�����
	 */
				for (int m = temp.size() - 1; m >= 0; m--) {
					portAndApl = temp.get(m).split(":");
					int k = Integer.parseInt(portAndApl[1]);
					int j = Integer.parseInt(portAndApl[2]);
					String apl = portAndApl[0];
					nodePair.append(node_name[k]+ "," +node_name[j] + ","+apl+"/");
				}
		}
		return nodePair.toString();
	}
}

