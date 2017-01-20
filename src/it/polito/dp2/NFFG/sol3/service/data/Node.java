package it.polito.dp2.NFFG.sol3.service.data;

import it.polito.dp2.NFFG.FunctionalType;
import it.polito.dp2.NFFG.LinkReader;
import it.polito.dp2.NFFG.NffgVerifierException;
import it.polito.dp2.NFFG.NodeReader;
import it.polito.dp2.NFFG.sol3.service.jaxb.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by giacomo on 17/01/2017.
 */
public class Node extends NamedEntity implements NodeReader {

	private FunctionalType type;
	private Set<LinkReader> links;

	public Node(NodeType newNode) throws NffgVerifierException {
		super(newNode == null?null:newNode.getName());
		this.type = FunctionalType.valueOf(newNode.getFuncType().name());
		this.links = new CopyOnWriteArraySet<>();
	}

	public Node(String name, FunctionalType type) throws NffgVerifierException {
		super(name);
		if(type == null)
			throw new NffgVerifierException("FunctionalType is null");
		this.type = type;
		links = new HashSet<>();
	}

	public Node(String name, FunctionalType type, Set<Link> links) throws NffgVerifierException {
		this(name, type);
		this.links = new CopyOnWriteArraySet<>(links);
	}

	@Override
	public FunctionalType getFuncType() {
		return type;
	}

	public void setFunkType(FunctionalType type) {
		if(type == null)
			throw new NullPointerException("Type is null");
		this.type = type;
	}

	@Override
	public Set<LinkReader> getLinks() {
		return links;
	}

	public void setLinks(Set<Link> links) {
		if (links == null)
			throw new NullPointerException("Links are null");
		for (Link link : links) {
			link.setSourceNode(this);
		}
		this.links = new CopyOnWriteArraySet<>(links);
	}

	public LinkReader getLink(String name) {
		for (LinkReader link : links) {
			if (link.getName().equals(name)) {
				return link;
			}
		}
		return null;
	}

	public void addLink(Link link) throws NffgVerifierException {
		if (getLink(link.getName()) == null) {
			link.setSourceNode(this);
			links.add(link);
		} else
			throw new NffgVerifierException("Link already present");
	}

	public void removeLink(String name) {
		links.removeIf(lk -> lk.getName().equals(name));
	}


	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Node)
			return super.equals(obj);
		else
			return false;
	}

	@Override
	public int hashCode() {
		return 5 * super.hashCode();
	}

	public NodeType toXMLObject() {
		ObjectFactory factory = new ObjectFactory();
		NodeType node = factory.createNodeType();
		node.setName(this.getName());
		node.setFuncType(FunctionalTypeType.valueOf(this.getFuncType().name()));
		NodeType.Links links = factory.createNodeTypeLinks();
		List<LinkType> linkList = links.getLink();
		for (LinkReader link : this.links) {
			linkList.add(((Link) link).toXMLObject());
		}
		node.setLinks(links);
		return node;
	}
}
