package it.polito.dp2.NFFG.sol3.client1;

import it.polito.dp2.NFFG.NffgReader;
import it.polito.dp2.NFFG.NffgVerifier;
import it.polito.dp2.NFFG.PolicyReader;
import it.polito.dp2.NFFG.ReachabilityPolicyReader;
import it.polito.dp2.NFFG.lab3.AlreadyLoadedException;
import it.polito.dp2.NFFG.lab3.NFFGClient;
import it.polito.dp2.NFFG.lab3.ServiceException;
import it.polito.dp2.NFFG.lab3.UnknownNameException;
import it.polito.dp2.NFFG.sol3.jaxb.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.Set;

/**
 * Created by giaco on 20/01/2017.
 */
public class NFFGClientImpl implements NFFGClient {

	private NffgVerifier verifier;
	private ObjectFactory factory;
	private WebTarget target;

	private final static QName _Nffg_QNAME = new QName("http://www.example.org/nffgVerifier", "nffg");
	private final static QName _Policy_QNAME = new QName("http://www.example.org/nffgVerifier", "policy");

	public NFFGClientImpl(NffgVerifier verifier, WebTarget target) {
		this.verifier = verifier;
		this.target = target;
		this.factory = new ObjectFactory();
	}

	@Override
	public void loadNFFG(String name) throws UnknownNameException, AlreadyLoadedException, ServiceException {
		NffgReader nffg = verifier.getNffg(name);
		if (nffg == null) {
			throw new UnknownNameException("The nffg with name \"" + name + "\" does not exists");
		}
		loadOneNFFG(nffg);
	}

	@Override
	public void loadAll() throws AlreadyLoadedException, ServiceException {
		Set<NffgReader> nffgs = verifier.getNffgs();
		for (NffgReader nffg : nffgs) {
			loadOneNFFG(nffg);
		}

		Set<PolicyReader> pols = verifier.getPolicies();
		for (PolicyReader pol : pols) {
			try {
				loadOnePolicy((ReachabilityPolicyReader) pol);
			} catch (UnknownNameException ex) {
				throw new ServiceException(ex);
			}
		}
	}

	private void loadOneNFFG(NffgReader nffg) throws AlreadyLoadedException, ServiceException {
		RestrictedNffgType serviceNffg = ClientUtils.copyNffg(nffg);
		JAXBElement<RestrictedNffgType> xml = factory.createNffg(serviceNffg);
		try {
			target.path("nffgs")
					.request(MediaType.APPLICATION_XML)
					.post(Entity.entity(xml, MediaType.APPLICATION_XML), RestrictedNffgType.class);

		} catch (WebApplicationException ex) {
			if (ex.getResponse().getStatus() == 403)
				throw new AlreadyLoadedException("Nffg already loaded in the service", ex);
			if (ex.getResponse().getStatus() == 400)
				throw new ServiceException("Nffg malformed", ex);
			else
				throw new ServiceException("Nffg not loaded, something went wrong", ex);
		} catch (Exception ex) {
			throw new ServiceException("An error occured during the loading of the nffg", ex);
		}
	}

	private void loadOnePolicy(ReachabilityPolicyReader pol) throws AlreadyLoadedException, ServiceException, UnknownNameException {
		EnhancedPolicyType policy = ClientUtils.copyPolicy(pol);
		JAXBElement<EnhancedPolicyType> xml = factory.createPolicy(policy);
		try {
			target.path("nffgs")
					.path(pol.getNffg().getName())
					.path("policies")
					.request(MediaType.APPLICATION_XML)
					.post(Entity.entity(xml, MediaType.APPLICATION_XML), EnhancedPolicyType.class);
		} catch (WebApplicationException ex) {
			if (ex.getResponse().getStatus() == 404)
				throw new UnknownNameException("The nffg \"" + pol.getNffg().getName() + "\" is not loaded in the service", ex);
			if (ex.getResponse().getStatus() == 400)
				throw new ServiceException("Policy malformed", ex);
			else
				throw new ServiceException("Policy not loaded, something went wrong", ex);
		} catch (Exception ex) {
			throw new ServiceException("An error occured during the loading of the policy", ex);
		}
	}

	@Override
	public void loadReachabilityPolicy(String name, String nffgName, boolean isPositive, String srcNodeName, String dstNodeName) throws UnknownNameException, ServiceException {
		EnhancedPolicyType policy = ClientUtils.copyPolicy(name, isPositive, srcNodeName, dstNodeName);
		JAXBElement<EnhancedPolicyType> xml = factory.createPolicy(policy);
		try {
			target.path("nffgs")
					.path(nffgName)
					.path("policies")
					.request(MediaType.APPLICATION_XML)
					.post(Entity.entity(xml, MediaType.APPLICATION_XML), EnhancedPolicyType.class);
		} catch (WebApplicationException ex) {
			if (ex.getResponse().getStatus() == 404)
				throw new UnknownNameException("The nffg \"" + nffgName + "\" is not loaded in the service", ex);
			if (ex.getResponse().getStatus() == 400)
				throw new ServiceException("Policy malformed", ex);
			else
				throw new ServiceException("Policy not loaded, something went wrong", ex);
		} catch (Exception ex) {
			throw new ServiceException("An error occured during the loading of the policy", ex);
		}
	}

	@Override
	public void unloadReachabilityPolicy(String name) throws UnknownNameException, ServiceException {
		try {
			target.path("policies")
					.path(name)
					.request(MediaType.APPLICATION_XML)
					.delete(EnhancedPolicyType.class);
		} catch (WebApplicationException ex) {
			if (ex.getResponse().getStatus() == 404)
				throw new UnknownNameException("The Policy \"" + name + "\" is not loaded in the service", ex);
			else
				throw new ServiceException("Policy not unloaded, something went wrong", ex);
		} catch (Exception ex) {
			throw new ServiceException("An error occured during the unloading of the policy", ex);
		}
	}

	@Override
	public boolean testReachabilityPolicy(String name) throws UnknownNameException, ServiceException {
		Results results;
		try {
			results = target.path("policies")
					.path(name)
					.path("verificationResult")
					.request(MediaType.APPLICATION_XML)
					.post(null, Results.class);
		} catch (WebApplicationException ex) {
			if (ex.getResponse().getStatus() == 404)
				throw new UnknownNameException("The Policy \"" + name + "\" is not loaded in the service", ex);
			else
				throw new ServiceException("Policy not verified, something went wrong", ex);
		} catch (Exception ex) {
			throw new ServiceException("An error occured during the verification of the policy", ex);
		}
		VerificationResultType res = results.getVerificationResult().get(0);
		return res.isVerificationResult();
	}
}
