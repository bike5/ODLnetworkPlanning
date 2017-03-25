package com.bupt.NetworkPlanning;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

public class DeliverFlow {
	String controllerIp = "10.103.27.253";
	String userPassword= "admin:admin";
	
	
	public void addFlow(Flow f, String srcMac, String dstMac) {
	    Vertex v = f.getNode();
	    try {
	      StringBuilder urlString = new StringBuilder();

	      urlString.append("http://"+controllerIp+":8181")
	      .append("/restconf/config/opendaylight-inventory:nodes/node/")
	        .append(v.getId())
	        .append("/table/0/flow/")
	        .append(f.getFlowId());
	      URL url = new URL(urlString.toString());

	      String  authEncodedString = new String(org.apache.commons.codec.binary.Base64.encodeBase64(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(userPassword)));

	      HttpURLConnection connection = (HttpURLConnection)url.openConnection();

	      connection.setDoOutput(true);
	      connection.setRequestMethod("PUT");

	      connection.setRequestProperty("Authorization", "Basic " + authEncodedString);
	      connection.setRequestProperty("Content-Type", "application/xml");
	      connection.setRequestProperty("Accept", "application/json");

	      OutputStreamWriter out = new OutputStreamWriter(
	        connection.getOutputStream());

	      out.write(f.toXmlString(srcMac, dstMac));
	      out.close();

	      connection.getInputStream();
	    }
	    catch (Exception e) {
	      e.printStackTrace();
	    }

//	    System.out.println("Flow installed on vertex " + v);
//	    System.out.println(f.toXmlString(srcMac, dstMac));
	  }
	public void installPath(List<Vertex> sp, int ingressPort, int outputPort, 
			    String destination) throws NoLinkException {
			    
			    Iterator<Vertex> i = sp.listIterator();

			    // Setup
			    Vertex prev = null;
			    Vertex v = i.next();
			    Vertex next = i.next();
			    Flow f = null;

			    int prevPort;
			    int nextPort;

			    // First step
			    f = new Flow(v, destination);
			    // prevPort = v.getIncomingPortTo(prev); Not the first step.
			    prevPort = ingressPort;
			    nextPort = v.getPortTo(next);

			    f.setIngressPort(prevPort);
			    f.addOutputAction(nextPort);
			    f.setOutputPort(nextPort);

			    this.addFlow(f, v.getMac(), next.getMac());

			    // Intermediate steps
			    while (i.hasNext()) {
			      // Rotating values one step
			      prev = v;
			      v = next;
			      next = i.next();

			      // Creating a flow for the "v" vertex, with ingressPort and outputPort
			      f = new Flow(v, destination);

			      prevPort = v.getIncomingPortTo(prev);
			      nextPort = v.getPortTo(next);

			      f.setIngressPort(prevPort);
			      f.addOutputAction(nextPort);
			      f.setOutputPort(nextPort);

			      this.addFlow(f, v.getMac(), next.getMac());
			    }

			    // Last step
			    prev = v;
			    v = next;
			    // next = null; Not needed

			    f = new Flow(v, destination);
			    prevPort = v.getIncomingPortTo(prev);
			    nextPort = outputPort;

			    f.setIngressPort(prevPort);
			    f.addOutputAction(nextPort);
			    f.setOutputPort(nextPort);
			    this.addFlow(f, v.getMac(), next.getMac());
			  }

	public void installPath(List<Vertex> sp, int ingressPort, int outputPort) throws NoLinkException {
			    this.installPath(sp, ingressPort, outputPort, null);
			  }

}
