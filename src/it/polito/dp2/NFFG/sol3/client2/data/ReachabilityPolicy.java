package it.polito.dp2.NFFG.sol3.client2.data;

import it.polito.dp2.NFFG.NffgVerifierException;
import it.polito.dp2.NFFG.NodeReader;
import it.polito.dp2.NFFG.ReachabilityPolicyReader;
import it.polito.dp2.NFFG.sol3.jaxb.EnhancedPolicyType;

/**
 * Created by giacomo on 17/01/2017.
 */
public class ReachabilityPolicy extends Policy implements ReachabilityPolicyReader {

	private NodeReader source;
	private NodeReader destination;

	public ReachabilityPolicy(Nffg nffg, EnhancedPolicyType newPolicy) throws NffgVerifierException {
		super(newPolicy.getName(), nffg, newPolicy.isPositive());
		NodeReader source = nffg.getNode(newPolicy.getSourceNode().getName());
		if (source == null)
			throw new NffgVerifierException("Source node does not exist");
		NodeReader dest = nffg.getNode(newPolicy.getDestinationNode().getName());
		if (dest == null)
			throw new NffgVerifierException("Destination node does not exist");
		this.source = source;
		this.destination = dest;
		if (newPolicy.getResult() != null) {
			this.setResult(new VerificationResult(this, newPolicy.getResult()));
		}
	}

	public ReachabilityPolicy(String name, Nffg nffg, Boolean positive, Node source, Node destination) throws NffgVerifierException {
		super(name, nffg, positive);
		if (source == null)
			throw new NffgVerifierException("Source is null");
		this.source = source;
		if (destination == null)
			throw new NffgVerifierException("Destination is null");
		this.destination = destination;
	}

	public ReachabilityPolicy(String name, Nffg nffg, Boolean positive, Node source, Node destination, VerificationResult result) throws NffgVerifierException {
		super(name, nffg, positive, result);
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

	public void setSourceNode(Node source) {
		if (source == null)
			throw new NullPointerException("Source is null");
		this.source = source;
	}

	@Override
	public NodeReader getDestinationNode() {
		return destination;
	}

	public void setDestinationNode(Node destination) {
		if (destination == null)
			throw new NullPointerException("Source is null");
		this.destination = destination;
	}


	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NamedEntity)
			return super.equals(obj);
		else
			return false;
	}

	@Override
	public int hashCode() {
		return 13 * super.hashCode();
	}
}
