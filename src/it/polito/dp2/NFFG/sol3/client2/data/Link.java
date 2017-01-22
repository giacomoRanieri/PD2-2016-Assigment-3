package it.polito.dp2.NFFG.sol3.client2.data;

import it.polito.dp2.NFFG.LinkReader;
import it.polito.dp2.NFFG.NffgVerifierException;
import it.polito.dp2.NFFG.NodeReader;

/**
 * Created by giacomo on 17/01/2017.
 */
public class Link extends NamedEntity implements LinkReader {

	private NodeReader source;
	private NodeReader destination;

	public Link(String name, NodeReader source, NodeReader destination) throws NffgVerifierException {
		super(name);
		if (source == null)
			throw new NffgVerifierException("Source is null");
		this.source = source;
		if (destination == null)
			throw new NffgVerifierException("Destination is null");
		this.destination = destination;
	}

	@Override
	public NodeReader getSourceNode() {
		return source;
	}

	public void setSourceNode(NodeReader source) {
		if (source == null)
			throw new NullPointerException("Source is null");
		this.source = source;
	}

	@Override
	public NodeReader getDestinationNode() {
		return destination;
	}

	public void setDestinationNode(NodeReader destination) {
		if (destination == null)
			throw new NullPointerException("Destination is null");
		this.destination = destination;
	}


	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Link)
			return super.equals(obj);
		else
			return false;
	}

	@Override
	public int hashCode() {
		return 3 * super.hashCode();
	}

}
