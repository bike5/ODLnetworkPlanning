package com.bupt.NetworkPlanning;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.bupt.NetworkPlanning.HTTPUtils;
import com.bupt.NetworkPlanning.Vertex;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
public class GetTopo {
	
	String controllerIp="10.103.27.253";
	String userPassword= "admin:admin";
	String srcMac=null;
	String dstMac=null;
	Graph graph=null;
	Map<String, Vertex> vertexes=null;
	
	
	public String getSrcMac() {
		return srcMac;
	}

	public void setSrcMac(String srcMac) {
		this.srcMac = srcMac;
	}

	public String getDstMac() {
		return dstMac;
	}

	public void setDstMac(String dstMac) {
		this.dstMac = dstMac;
	}

	public String getMac(String id){
		String mac=null;
		 Map<String, String> headers=new HashMap<String, String>(); 		 
	     String encoding = new String(org.apache.commons.codec.binary.Base64.encodeBase64
	    		 (org.apache.commons.codec.binary.StringUtils.getBytesUtf8(userPassword)));
	    // System.out.println(encoding);
		 headers.put("Content-Type","application/json");
		 headers.put("Authorization","Basic "+encoding);		
		 String URL="http://"+controllerIp+":8181/restconf/operational/opendaylight-inventory:nodes/node/"
		 		+ id+"/node-connector/"+id+":LOCAL";
		 String nodeConnector=HTTPUtils.httpGet(URL, headers);
		 ObjectMapper mapper = new ObjectMapper();
		 try {
			JsonNode rootNode=mapper.readTree(nodeConnector);
		    mac=rootNode.path("node-connector").get(0).path("flow-node-inventory:hardware-address").asText();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mac;
	}

	
/*	public String getFlow(String node){
 		 Map<String, String> headers=new HashMap<String, String>();
 		 
	     String encoding = new String(org.apache.commons.codec.binary.Base64.encodeBase64
	    		 (org.apache.commons.codec.binary.StringUtils.getBytesUtf8(userPassword)));
	     System.out.println(encoding);
		 headers.put("Content-Type","application/json");
		 headers.put("Authorization","Basic "+encoding);
		 String URL="http://"+controllerIp+
				 ":8181/restconf/operational/opendaylight-inventory:nodes/node/"+node+"/table/0/";
		 String flow=HTTPUtils.httpGet(URL, headers);
		 System.out.println(flow);
		return flow;
	}*/
	
	public Graph getTopo(){
		 Map<String, String> headers=new HashMap<String, String>(); 		 
	     String encoding = new String(org.apache.commons.codec.binary.Base64.encodeBase64
	    		 (org.apache.commons.codec.binary.StringUtils.getBytesUtf8(userPassword)));
		 headers.put("Content-Type","application/json");
		 headers.put("Authorization","Basic "+encoding);
		 String URL="http://"+controllerIp+":8181/restconf/operational/network-topology:network-topology";
		 String topo=HTTPUtils.httpGet(URL, headers);

		 ObjectMapper mapper = new ObjectMapper();
		 int nodeSize=0;
		 int linkSize=0;
		 JsonNode nodeRootNode=null;
		 JsonNode linkRootNode=null;
       try {
			JsonNode rootNode = mapper.readTree(topo);
			nodeRootNode=rootNode.path("network-topology").path("topology").get(0).path("node");
			nodeSize=nodeRootNode.size();
			
			/*for(int i=0;i<nodeSize;i++){
				System.out.println(nodeRootNode.get(i).path("node-id"));
			}*/
			
			vertexes=new HashMap<>();
			linkRootNode=rootNode.path("network-topology").path("topology").get(0).path("link");
			linkSize=linkRootNode.size();
			for(int i=0;i<linkSize;i++){
				String linkId=linkRootNode.get(i).path("link-id").asText();
				String srcNode=linkRootNode.get(i).path("source").path("source-node").asText();
				String srcTp=linkRootNode.get(i).path("source").path("source-tp").asText();
				String dstNode=linkRootNode.get(i).path("destination").path("dest-node").asText();
				String dstTp=linkRootNode.get(i).path("destination").path("dest-tp").asText();
//				System.out.println(srcTp);

				if(linkId.length()<22){//区别主机和交换机，交换机与交换机连接的id短  交换机与主机连接的id长
					String outgoingPort=srcTp.substring(srcNode.length()+1);
					int inPort=Integer.parseInt(outgoingPort);
					String targetPort=dstTp.substring(dstNode.length()+1);
					int dstPort=Integer.parseInt(targetPort);
	
					if(!vertexes.containsKey(srcNode)){
						Vertex srcVertex=new Vertex(srcNode);
						srcVertex.setMac(this.getMac(srcNode));
						vertexes.put(srcNode, srcVertex);
					}
					if(!vertexes.containsKey(dstNode)){
						Vertex dstVertex=new Vertex(dstNode);
						dstVertex.setMac(this.getMac(dstNode));
						vertexes.put(dstNode, dstVertex);
					}				
					vertexes.get(srcNode).addEdge(vertexes.get(dstNode),1,inPort,dstPort);								
				}else{
					String nodePortToHostString=null;
					int nodePortToHost=0;
					String hostPortToNodeString=null;
					int hostPortToNode=0;
					if(!vertexes.containsKey(srcNode)&&(!srcNode.equals(srcTp))){
						Vertex srcVertex=new Vertex(srcNode);
						srcVertex.setMac(this.getMac(srcNode));
						vertexes.put(srcNode, srcVertex);
					}
					if(!vertexes.containsKey(dstNode)&&(!dstNode.equals(dstTp))){
						Vertex dstVertex=new Vertex(dstNode);
						dstVertex.setMac(this.getMac(dstNode));
						vertexes.put(dstNode, dstVertex);
					}
					if(!srcNode.equals(srcTp)){
						nodePortToHostString=srcTp.substring(srcNode.length()+1);
						nodePortToHost=Integer.parseInt(nodePortToHostString);
						vertexes.get(srcNode).addOutgoingHost(nodePortToHost, dstNode);
					}
					if(!dstNode.equals(dstTp)){
						hostPortToNodeString=dstTp.substring(dstNode.length()+1);
						hostPortToNode=Integer.parseInt(hostPortToNodeString);
						vertexes.get(dstNode).addIncomingHost(hostPortToNode, srcNode);

					}

				}
			}
			graph=new Graph(vertexes);//返回全网的拓扑	
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return graph;
	}


	
}
