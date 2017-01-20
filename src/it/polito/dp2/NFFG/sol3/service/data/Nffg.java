package it.polito.dp2.NFFG.sol3.service.data;

import it.polito.dp2.NFFG.NffgReader;
import it.polito.dp2.NFFG.NffgVerifierException;
import it.polito.dp2.NFFG.NodeReader;
import it.polito.dp2.NFFG.sol3.service.NffgUtils;
import it.polito.dp2.NFFG.sol3.service.jaxb.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by giacomo on 17/01/2017.
 */
public class Nffg extends NamedEntity implements NffgReader {

	private Calendar updateTime;
	private Set<NodeReader> nodes;

	public Nffg(RestrictedNffgType newNffg) throws NffgVerifierException {
		super(newNffg == null ? null : newNffg.getName());
		this.updateTime = newNffg.getUpdateTime().toGregorianCalendar();
		Set<NodeReader> newNodes = new CopyOnWriteArraySet<>();
		for (NodeType node : newNffg.getNodes().getNode()) {
			newNodes.add(new Node(node));
		}
		this.nodes = newNodes;
		for (NodeType node : newNffg.getNodes().getNode()) {
			NodeReader source = getNode(node.getName());
			Set<Link> links = new CopyOnWriteArraySet<>();
			for (LinkType link : node.getLinks().getLink()) {
				NodeReader dest = getNode(link.getDestinationNode().getName());
				links.add(new Link(link.getName(), source, dest));
			}
			((Node) source).setLinks(links);
		}
	}

	public Nffg(String name) throws NffgVerifierException {
		super(name);
		this.updateTime = new GregorianCalendar();
		this.nodes = new CopyOnWriteArraySet<>();
	}

	public Nffg(String name, Calendar updateTime) throws NffgVerifierException {
		super(name);
		if (updateTime == null)
			this.updateTime = new GregorianCalendar();
		else
			this.updateTime = updateTime;
		this.nodes = new CopyOnWriteArraySet<>();
	}

	public Nffg(String name, Calendar updateTime, Set<Node> nodes) throws NffgVerifierException {
		this(name, updateTime);
		this.nodes = new CopyOnWriteArraySet<>(nodes);
	}


	@Override
	public Calendar getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Calendar updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public Set<NodeReader> getNodes() {
		return nodes;
	}

	public void setNodes(Set<Node> nodes) {
		this.nodes = new CopyOnWriteArraySet<>(nodes);
	}

	@Override
	public NodeReader getNode(String name) {
		for (NodeReader node : nodes) {
			if (node.getName().equals(name)) {
				return node;
			}
		}
		return null;
	}

	public void addNode(Node node) throws NffgVerifierException {
		if (getNode(node.getName()) == null) {
			nodes.add(node);
		} else
			throw new NffgVerifierException("Node already present");
	}

	public void removeNode(String name) {
		nodes.removeIf(no -> no.getName().equals(name));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Nffg)
			return super.equals(obj);
		else
			return false;
	}

	@Override
	public int hashCode() {
		return 7 * super.hashCode();
	}

	public RestrictedNffgType toXMLObject() {
		ObjectFactory factory = new ObjectFactory();
		RestrictedNffgType nffg = factory.createRestrictedNffgType();
		nffg.setName(this.getName());
		nffg.setUpdateTime(NffgUtils.convertToXMLCalendar(this.getUpdateTime()));
		RestrictedNffgType.Nodes nodes = factory.createRestrictedNffgTypeNodes();
		List<NodeType> nodeList = nodes.getNode();
		for (NodeReader node : this.nodes) {
			nodeList.add(((Node) node).toXMLObject());
		}
		nffg.setNodes(nodes);
		return nffg;
	}
}
