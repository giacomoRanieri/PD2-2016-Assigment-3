package it.polito.dp2.NFFG.sol3.client2;

import it.polito.dp2.NFFG.NffgReader;
import it.polito.dp2.NFFG.NffgVerifier;
import it.polito.dp2.NFFG.NffgVerifierException;
import it.polito.dp2.NFFG.PolicyReader;
import it.polito.dp2.NFFG.sol3.client2.data.Nffg;
import it.polito.dp2.NFFG.sol3.client2.data.ReachabilityPolicy;
import it.polito.dp2.NFFG.sol3.jaxb.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by giaco on 20/01/2017.
 */
public class NffgVerifierFactory extends it.polito.dp2.NFFG.NffgVerifierFactory {


	private WebTarget target;

	@Override
	public NffgVerifier newNffgVerifier() throws NffgVerifierException {


		Client client = ClientBuilder.newClient();
		String URL = System.getProperty("it.polito.dp2.NFFG.lab3.URL");
		if (URL == null) {
			URL = "http://localhost:8080/NffgService/rest/";
		}
		try {
			target = client.target(new URI(URL));
		} catch (URISyntaxException ex) {
			throw new NffgVerifierException("Service URL is not a valid URL");
		}

		Map<String, NffgReader> nffgs = getNffgs();
		Map<String, PolicyReader> policies = getPolicies(nffgs);

		return new NffgVerifierImpl(nffgs, policies);
	}

	private Map<String, NffgReader> getNffgs() throws NffgVerifierException {
		Map<String, NffgReader> loaded = new ConcurrentHashMap<>();

		Nffgs result;
		try {
			result = target.path("nffgs")
					.request(MediaType.APPLICATION_XML)
					.get(Nffgs.class);
		} catch (WebApplicationException ex) {
			throw new NffgVerifierException("Unable to retreive the nffgs list");
		} catch (Exception ex) {
			throw new NffgVerifierException("Something went wrong during the retrieval the nffgs list");
		}
		for (RestrictedNffgType nffg : result.getNffg()) {
			Nffg newNffg = new Nffg(nffg);
			loaded.put(newNffg.getName(), newNffg);
		}

		return loaded;
	}

	private Map<String, PolicyReader> getPolicies(Map<String, NffgReader> nffgs) throws NffgVerifierException {
		Map<String, PolicyReader> loaded = new ConcurrentHashMap<>();

		Policies result;
		try {
			result = target.path("policies")
					.request(MediaType.APPLICATION_XML)
					.get(Policies.class);
		} catch (WebApplicationException ex) {
			throw new NffgVerifierException("Unable to retreive the policies list");
		} catch (Exception ex) {
			throw new NffgVerifierException("Something went wrong during the retrieval the policies list");
		}
		for (EnhancedPolicyType policy : result.getPolicy()) {
			Nffg nffg = (Nffg) nffgs.get(policy.getNffg());
			ReachabilityPolicy newPolicy = new ReachabilityPolicy(nffg, policy);
			loaded.put(newPolicy.getName(), newPolicy);
		}

		return loaded;
	}
}
