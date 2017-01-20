package it.polito.dp2.NFFG.sol3.service.data;

import it.polito.dp2.NFFG.NffgReader;
import it.polito.dp2.NFFG.NffgVerifier;
import it.polito.dp2.NFFG.NffgVerifierException;
import it.polito.dp2.NFFG.PolicyReader;

import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by giacomo on 17/01/2017.
 */
public class NffgVerifierImpl implements NffgVerifier {
	private Map<String,PolicyReader> policies;
	private Map<String,NffgReader> nffgs;

	public NffgVerifierImpl() {
		policies = new ConcurrentHashMap<>();
		nffgs = new ConcurrentHashMap<>();
	}

	public NffgVerifierImpl(Map<String,Policy> policies, Map<String,Nffg> nffgs) {
		this.policies = new ConcurrentHashMap<>(policies);
		this.nffgs = new ConcurrentHashMap<>(nffgs);
	}

	public Map<String,PolicyReader> getPoliciesMap(){
		return policies;
	}

	public Map<String, NffgReader> getNffgsMap(){
		return nffgs;
	}

	@Override
	public Set<NffgReader> getNffgs() {
		Set<NffgReader> nffgs = new CopyOnWriteArraySet<>();
		for(Map.Entry<String,NffgReader> entry: this.nffgs.entrySet()){
			nffgs.add(entry.getValue());
		}
		return nffgs;
	}

	private void setNffgs(Map<String,Nffg> nffgs) {
		if (nffgs == null)
			throw new NullPointerException("Nffgs are null");
		this.nffgs = new ConcurrentHashMap<>(nffgs);
	}

	@Override
	public NffgReader getNffg(String name) {
		return nffgs.get(name);
	}

	public void addNffg(Nffg nffg) throws NffgVerifierException {
		if (nffgs.containsKey(nffg.getName()))
			throw new NffgVerifierException("Nffg already present");
		else
			nffgs.put(nffg.getName(),nffg);
	}

	public NffgReader removeNffg(String name) {
		return nffgs.remove(name);
	}

	@Override
	public Set<PolicyReader> getPolicies() {
		Set<PolicyReader> pols = new CopyOnWriteArraySet<>();
		for(Map.Entry<String,PolicyReader> entry: this.policies.entrySet()){
			pols.add(entry.getValue());
		}
		return pols;
	}

	public PolicyReader getPolicy(String name) {
		return policies.get(name);
	}

	public void setPolicies(Map<String,Policy> policies) {
		if (policies == null)
			throw new NullPointerException("Policies are null");
		this.policies = new ConcurrentHashMap<>(policies);
	}

	@Override
	public Set<PolicyReader> getPolicies(String nffgName) {
		Set<PolicyReader> res = new CopyOnWriteArraySet<>();
		for (Entry<String,PolicyReader> pol : policies.entrySet()) {
			if (pol.getValue().getNffg().getName().equals(nffgName))
				res.add(pol.getValue());
		}
		return res;
	}

	@Override
	public Set<PolicyReader> getPolicies(Calendar startDate) {
		Set<PolicyReader> res = new CopyOnWriteArraySet<>();
		for (Entry<String,PolicyReader> pol : policies.entrySet()) {
			if (pol.getValue().getResult() != null && pol.getValue().getResult().getVerificationTime().compareTo(startDate) >= 0)
				res.add(pol.getValue());
		}
		return res;
	}


	public void addPolicy(ReachabilityPolicy pol) throws NffgVerifierException {
		if (policies.containsKey(pol.getName()))
			throw new NffgVerifierException("Policy already present");
		else
			policies.put(pol.getName(),pol);
	}


	public PolicyReader removePolicy(String name) {
		return policies.remove(name);
	}

	public Set<PolicyReader> removePolicies(){
		Set<PolicyReader> deleted = getPolicies();
		this.policies.clear();
		return deleted;
	}

	public PolicyReader updatePolicy(ReachabilityPolicy pol){
		policies.put(pol.getName(),pol);
		return pol;
	}
}
