package it.polito.dp2.NFFG.sol3.client1;

import it.polito.dp2.NFFG.LinkReader;
import it.polito.dp2.NFFG.NffgReader;
import it.polito.dp2.NFFG.NodeReader;
import it.polito.dp2.NFFG.ReachabilityPolicyReader;
import it.polito.dp2.NFFG.sol3.jaxb.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

/**
 * Created by giaco on 20/01/2017.
 */
public class ClientUtils {
	private static ObjectFactory factory = new ObjectFactory();

	public static RestrictedNffgType copyNffg(NffgReader graph) {
		RestrictedNffgType nffg = factory.createRestrictedNffgType();

		nffg.setName(graph.getName());
		nffg.setUpdateTime(convertToXMLCalendar(graph.getUpdateTime()));
		nffg.setNodes(copyNodes(graph.getNodes()));

		return nffg;
	}

	public static RestrictedNffgType.Nodes copyNodes(Set<NodeReader> nodeset) {
		RestrictedNffgType.Nodes nodes = factory.createRestrictedNffgTypeNodes();

		List<NodeType> nodelist = nodes.getNode();
		for (NodeReader node : nodeset) {
			NodeType nodeT = new NodeType();
			nodeT.setName(node.getName());
			nodeT.setFuncType(FunctionalTypeType.valueOf(node.getFuncType().name()));
			nodeT.setLinks(copyLinks(node.getLinks()));

			nodelist.add(nodeT);
		}

		return nodes;
	}

	public static NodeType.Links copyLinks(Set<LinkReader> linkset) {
		NodeType.Links links = new NodeType.Links();
		List<LinkType> linklist = links.getLink();

		for (LinkReader link : linkset) {

			LinkType linkT = new LinkType();
			linkT.setName(link.getName());
			NamedEntityType dest = new NamedEntityType();
			dest.setName(link.getDestinationNode().getName());
			linkT.setDestinationNode(dest);

			linklist.add(linkT);
		}

		return links;
	}

	public static XMLGregorianCalendar convertToXMLCalendar(Calendar c) {
		XMLGregorianCalendar calendar;
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(c.getTime());
		gc.setTimeZone(c.getTimeZone());
		try {
			calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
		} catch (DatatypeConfigurationException e) {
			throw new Error(e);
		}
		return calendar;
	}

	public static EnhancedPolicyType copyPolicy(String name, boolean isPositive, String srcNodeName, String dstNodeName) {
		EnhancedPolicyType policy = factory.createEnhancedPolicyType();

		NamedEntityType dest = new NamedEntityType();
		dest.setName(dstNodeName);

		NamedEntityType sour = new NamedEntityType();
		sour.setName(srcNodeName);

		policy.setName(name);
		policy.setPositive(isPositive);
		policy.setDestinationNode(dest);
		policy.setSourceNode(sour);

		return policy;
	}

	public static EnhancedPolicyType copyPolicy(ReachabilityPolicyReader pol) {

		EnhancedPolicyType policy = factory.createEnhancedPolicyType();

		NamedEntityType dest = new NamedEntityType();
		dest.setName(pol.getDestinationNode().getName());

		NamedEntityType sour = new NamedEntityType();
		sour.setName(pol.getSourceNode().getName());

		policy.setName(pol.getName());
		policy.setPositive(pol.isPositive());
		policy.setDestinationNode(dest);
		policy.setSourceNode(sour);

		if (pol.getResult() != null) {
			VerificationResultType result = factory.createVerificationResultType();
			result.setVerificationTime(convertToXMLCalendar(pol.getResult().getVerificationTime()));
			result.setVerificationResultMsg(pol.getResult().getVerificationResultMsg());
			result.setVerificationResult(pol.getResult().getVerificationResult());
			policy.setResult(result);
		}

		return policy;
	}
}
