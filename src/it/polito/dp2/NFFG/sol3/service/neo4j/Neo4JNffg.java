package it.polito.dp2.NFFG.sol3.service.neo4j;

import it.polito.dp2.NFFG.*;
import it.polito.dp2.NFFG.lab3.ServiceException;
import it.polito.dp2.NFFG.sol3.service.data.ReachabilityPolicy;
import it.polito.dp2.NFFG.sol3.service.data.VerificationResult;
import it.polito.dp2.NFFG.sol3.service.jaxrs.*;
import it.polito.dp2.NFFG.sol3.service.jaxrs.Localhost_Neo4JXMLRest.Resource;
import org.glassfish.jersey.client.ClientResponse;
import org.jvnet.ws.wadl.Response;

import javax.ws.rs.WebApplicationException;
import java.util.*;

/**
 * Created by giaco on 19/01/2017.
 */
public class Neo4JNffg {
	private Map<String, Node> nodeMap;
	private Node core;
	private NffgReader nffg;
	private Resource resource;
	private ObjectFactory objFac;

	public Neo4JNffg(NffgReader nffg, Resource resource) throws ServiceException {
		if (resource == null)
			throw new NullPointerException("Resource is null");
		this.resource = resource;
		if (nffg == null)
			throw new NullPointerException("Nffg is null");
		this.nffg = nffg;
		this.nodeMap = new HashMap<>();
		this.objFac = new ObjectFactory();

		loadNffg();
	}

	private void loadNffg() throws ServiceException {
		Resource.Node nodeCall = resource.node();

		Node coreNodeReq = objFac.createNode();
		//Set the property
		List<Property> coreProps = coreNodeReq.getProperty();
		Property coreNameProp = new Property();
		coreNameProp.setName(Neo4JService.NAME_PROP);
		coreNameProp.setValue(nffg.getName());
		coreProps.add(coreNameProp);
		//Load the node into Neo4J
		try {
			core = nodeCall.postXmlAsNode(coreNodeReq);
		} catch (WebApplicationException ex) {
			throw new ServiceException("Unable to load core node", ex);
		}

		Resource.NodeNodeidLabel labelCall = resource.nodeNodeidLabel(core.getId());
		Labels lbls = objFac.createLabels();
		List<String> labelList = lbls.getValue();
		labelList.add(Neo4JService.CORE_LABEL);
		try {
			labelCall.postXml(lbls, com.sun.jersey.api.client.ClientResponse.class);
		} catch (WebApplicationException ex) {
			throw new ServiceException("Unable to load core node label", ex);
		}


		Set<NodeReader> nodes = nffg.getNodes();
		for (NodeReader noder : nodes) {
			//For each node of the graph
			//Create a JAXRS node
			Node node = objFac.createNode();
			//Set the property
			List<Property> props = node.getProperty();
			Property nameProp = new Property();
			nameProp.setName(Neo4JService.NAME_PROP);
			nameProp.setValue(noder.getName());
			props.add(nameProp);
			//Load the node into Neo4J
			Node resp;
			try {
				resp = nodeCall.postXmlAsNode(node);
				nodeMap.put(noder.getName(), resp);
			} catch (WebApplicationException ex) {
				throw new ServiceException("Unable to load node \"" + noder.getName() + "\"", ex);
			}

			Resource.NodeNodeidRelationship putRelationCall = resource.nodeNodeidRelationship(core.getId());

			Relationship relation = objFac.createRelationship();
			relation.setType(Neo4JService.BELONG_RELATION);
			relation.setDstNode(resp.getId());
			try {
				putRelationCall.postXmlAsRelationship(relation);
			} catch (WebApplicationException ex) {
				throw new ServiceException("Unable to load relation between \"" + noder.getName() + "\" and the core node", ex);
			}
		}

		//adding all relationship

		for (String source : nodeMap.keySet()) {
			//For each node of the graph
			//define the source Node
			Node sourceNode = nodeMap.get(source);

			for (LinkReader link : nffg.getNode(source).getLinks()) {
				//For each link of the source node
				NodeReader dest = link.getDestinationNode();
				Node destNode = nodeMap.get(dest.getName());

				Resource.NodeNodeidRelationship putRelationCall = resource.nodeNodeidRelationship(sourceNode.getId());

				Relationship relation = objFac.createRelationship();
				relation.setType(Neo4JService.LINK_TYPE);
				relation.setDstNode(destNode.getId());
				try {
					putRelationCall.postXmlAsRelationship(relation);
				} catch (WebApplicationException ex) {
					throw new ServiceException("Unable to load relation between \"" + source + "\" and \"" + dest.getName() + "\"", ex);
				}
			}

		}
	}

	public VerificationResult testReachability(PolicyReader policy) throws ServiceException {
		String source = ((ReachabilityPolicy) policy).getSourceNode().getName();
		Node sourceNode = nodeMap.get(source);
		String dest = ((ReachabilityPolicy) policy).getDestinationNode().getName();
		Node destNode = nodeMap.get(dest);

		Resource.NodeNodeidPaths nodePathCall = resource.nodeNodeidPaths(sourceNode.getId());
		Paths resp;
		try {
			resp = nodePathCall.getAsPaths(destNode.getId());
		} catch (WebApplicationException ex) {
			throw new ServiceException("Error finding path between \"" + source + "\" and \"" + dest + "\"", ex);
		}
		Boolean reachable = resp.getPath().size() > 0;
		Boolean result;
		String message;
		if (policy.isPositive())
			result = reachable;
		else
			result = !reachable;
		if (reachable)
			message = "The node \"" + source + "\" is reachable from the node \"" + dest + "\"";
		else
			message = "The node \"" + source + "\" is not reachable from the node \"" + dest + "\"";
		return new VerificationResult(policy, result, new GregorianCalendar(), message);
	}
}
