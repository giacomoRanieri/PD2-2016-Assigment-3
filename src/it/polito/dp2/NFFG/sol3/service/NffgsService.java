package it.polito.dp2.NFFG.sol3.service;

import it.polito.dp2.NFFG.NffgReader;
import it.polito.dp2.NFFG.NffgVerifierException;
import it.polito.dp2.NFFG.PolicyReader;
import it.polito.dp2.NFFG.VerificationResultReader;
import it.polito.dp2.NFFG.lab3.ServiceException;
import it.polito.dp2.NFFG.sol3.service.data.*;
import it.polito.dp2.NFFG.sol3.jaxb.EnhancedPolicyType;
import it.polito.dp2.NFFG.sol3.jaxb.RestrictedNffgType;
import it.polito.dp2.NFFG.sol3.service.neo4j.Neo4JService;

import java.util.Set;

/**
 * Created by giacomo on 16/01/2017.
 */
public class NffgsService {

	private NffgVerifierImpl verifier;
	private Neo4JService neo4j = Neo4JService.getService();

	private static NffgsService service = new NffgsService();

	public static NffgsService getService() {
		return service;
	}

	private NffgsService() {
		verifier = new NffgVerifierImpl();
	}

	public Object getSyncObj() {
		return verifier.getPoliciesMap();
	}

	public Object getNffgSyncObj() {
		return verifier.getNffgsMap();
	}

	public Set<NffgReader> getNffgs() {
		return verifier.getNffgs();
	}

	public NffgReader getNffg(String name) {
		return verifier.getNffg(name);
	}

	public NffgReader addNffg(RestrictedNffgType newNffg) throws NffgVerifierException, NullPointerException, ServiceException {
		Nffg nffg = new Nffg(newNffg);
		this.neo4j.loadNFFG(nffg);
		verifier.addNffg(nffg);
		return nffg;
	}

	public Set<PolicyReader> getNffgPolicies(NffgReader nffg) {
		return verifier.getPolicies(nffg.getName());
	}

	public Set<PolicyReader> removeNffgPolicies(NffgReader nffg) {
		Set<PolicyReader> pols = verifier.getPolicies(nffg.getName());
		for (PolicyReader pol : pols) {
			verifier.removePolicy(pol.getName());
		}
		return pols;
	}

	public ReachabilityPolicy addNffgPolicy(NffgReader nffg, EnhancedPolicyType newPolicy) throws NffgVerifierException {
		ReachabilityPolicy policy = new ReachabilityPolicy((Nffg) nffg, newPolicy);
		verifier.addPolicy(policy);
		return policy;
	}

	public Set<PolicyReader> getPolicies() {
		return verifier.getPolicies();
	}

	public Set<PolicyReader> removePolicies() {
		return verifier.removePolicies();
	}

	public PolicyReader getPolicy(String name) {
		return verifier.getPolicy(name);
	}

	public PolicyReader updatePolicy(PolicyReader oldPol, EnhancedPolicyType newPol) throws NffgVerifierException {
		Nffg nffg;
		newPol.setName(oldPol.getName());
		if (newPol.getNffg() == null || newPol.getNffg().equals(oldPol.getNffg().getName()))
			nffg = (Nffg) oldPol.getNffg();
		else
			nffg = (Nffg) getNffg(newPol.getNffg());
		return verifier.updatePolicy(new ReachabilityPolicy(nffg, newPol));

	}

	public PolicyReader removePolicy(PolicyReader remove) {
		return verifier.removePolicy(remove.getName());
	}

	public VerificationResultReader verifyPolicy(PolicyReader policy) throws ServiceException {
		return neo4j.testReachability(policy);
	}

	public VerificationResultReader updateVerificationResult(VerificationResult result, Policy pol) {
		pol.setResult(result);
		return result;
	}
}
