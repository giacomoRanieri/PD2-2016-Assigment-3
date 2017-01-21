package it.polito.dp2.NFFG.sol3.client1;

import it.polito.dp2.NFFG.NffgVerifier;
import it.polito.dp2.NFFG.NffgVerifierException;
import it.polito.dp2.NFFG.NffgVerifierFactory;
import it.polito.dp2.NFFG.lab3.NFFGClient;
import it.polito.dp2.NFFG.lab3.NFFGClientException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by giaco on 20/01/2017.
 */
public class NFFGClientFactory extends it.polito.dp2.NFFG.lab3.NFFGClientFactory {
	@Override
	public NFFGClient newNFFGClient() throws NFFGClientException {
		NffgVerifier verifier;
		WebTarget target;

		Client client = ClientBuilder.newClient();
		String URL = System.getProperty("it.polito.dp2.NFFG.lab3.URL");
		if (URL == null) {
			URL = "http://localhost:8080/NffgService/rest/";
		}
		try {
			target = client.target(new URI(URL));
		} catch (URISyntaxException ex) {
			throw new NFFGClientException("Service URL is not a valid URL");
		}

		try {
			NffgVerifierFactory factory = NffgVerifierFactory.newInstance();
			verifier = factory.newNffgVerifier();
		} catch (NffgVerifierException ex) {
			throw new NFFGClientException("Unable to load nffg verifier");
		}

		return new NFFGClientImpl(verifier, target);
	}
}
