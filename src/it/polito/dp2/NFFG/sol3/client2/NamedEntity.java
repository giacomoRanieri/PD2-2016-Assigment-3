package it.polito.dp2.NFFG.sol3.client2;

import it.polito.dp2.NFFG.NamedEntityReader;
import it.polito.dp2.NFFG.NffgVerifierException;

/**
 * Created by giacomo on 17/01/2017.
 */
public abstract class NamedEntity implements NamedEntityReader {

	private String name;

	protected NamedEntity(String name) throws NffgVerifierException {
		if (name == null)
			throw new NffgVerifierException("Entity name is null");
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name == null)
			throw new NullPointerException("Entity name is null");
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NamedEntity)
			if (((NamedEntity) obj).getName().equals(this.name))
				return true;
			else
				return false;
		else
			return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
