package it.polito.dp2.NFFG.sol3.client2;

import it.polito.dp2.NFFG.NffgReader;
import it.polito.dp2.NFFG.NffgVerifier;
import it.polito.dp2.NFFG.NffgVerifierException;
import it.polito.dp2.NFFG.PolicyReader;

import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by giacomo on 17/01/2017.
 */
public class NffgVerifierImpl implements NffgVerifier {
	private Map<String, PolicyReader> policies;
	private Map<String, NffgReader> nffgs;

	public NffgVerifierImpl() {
		policies = new ConcurrentHashMap<>();
		nffgs = new ConcurrentHashMap<>();
	}

	public NffgVerifierImpl(Map<String, NffgReader> nffgs, Map<String, PolicyReader> policies) {
		this.policies = new ConcurrentHashMap<>(policies);
		this.nffgs = new ConcurrentHashMap<>(nffgs);
	}

	@Override
	public Set<NffgReader> getNffgs() {
		Set<NffgReader> nffgs = new CopyOnWriteArraySet<>();
		for (Entry<String, NffgReader> entry : this.nffgs.entrySet()) {
			nffgs.add(entry.getValue());
		}
		return nffgs;
	}

	@Override
	public NffgReader getNffg(String name) {
		return nffgs.get(name);
	}

	@Override
	public Set<PolicyReader> getPolicies() {
		Set<PolicyReader> pols = new CopyOnWriteArraySet<>();
		for (Entry<String, PolicyReader> entry : this.policies.entrySet()) {
			pols.add(entry.getValue());
		}
		return pols;
	}

	@Override
	public Set<PolicyReader> getPolicies(String nffgName) {
		Set<PolicyReader> res = new CopyOnWriteArraySet<>();
		for (Entry<String, PolicyReader> pol : policies.entrySet()) {
			if (pol.getValue().getNffg().getName().equals(nffgName))
				res.add(pol.getValue());
		}
		return res;
	}

	@Override
	public Set<PolicyReader> getPolicies(Calendar startDate) {
		Set<PolicyReader> res = new CopyOnWriteArraySet<>();
		for (Entry<String, PolicyReader> pol : policies.entrySet()) {
			if (pol.getValue().getResult() != null && pol.getValue().getResult().getVerificationTime().compareTo(startDate) >= 0)
				res.add(pol.getValue());
		}
		return res;
	}
}
