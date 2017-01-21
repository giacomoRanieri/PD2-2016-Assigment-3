package it.polito.dp2.NFFG.sol3.client2;

import it.polito.dp2.NFFG.NffgReader;
import it.polito.dp2.NFFG.NffgVerifierException;
import it.polito.dp2.NFFG.PolicyReader;
import it.polito.dp2.NFFG.VerificationResultReader;

/**
 * Created by giacomo on 17/01/2017.
 */
public abstract class Policy extends NamedEntity implements PolicyReader {

	private NffgReader nffg;
	private VerificationResultReader result;
	private Boolean positive;

	protected Policy(String name, Nffg nffg, Boolean positive) throws NffgVerifierException {
		super(name);
		if(nffg == null)
			throw new NffgVerifierException("Policy nffg is null");
		this.nffg = nffg;
		this.positive = positive;
	}

	protected Policy(String name, Nffg nffg, Boolean positive, VerificationResult result) throws NffgVerifierException {
		this(name, nffg, positive);
		result.setPolicy(this);
		this.result = result;
	}

	@Override
	public NffgReader getNffg() {
		return nffg;
	}

	public void setNffg(NffgReader nffg) {
		if (nffg == null)
			throw new NullPointerException();
		this.nffg = nffg;
	}

	@Override
	public VerificationResultReader getResult() {
		return result;
	}

	public void setResult(VerificationResult result) {
		result.setPolicy(this);
		this.result = result;
	}

	@Override
	public Boolean isPositive() {
		return positive;
	}

	public void setPositive(Boolean positive) {
		if (positive == null)
			throw new NullPointerException();
		this.positive = positive;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Policy)
			return super.equals(obj);
		else
			return false;
	}

	@Override
	public int hashCode() {
		return 11 * super.hashCode();
	}
}
