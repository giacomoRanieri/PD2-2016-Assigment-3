package it.polito.dp2.NFFG.sol3.service.neo4j;

import com.sun.jersey.api.client.Client;
import it.polito.dp2.NFFG.NffgReader;
import it.polito.dp2.NFFG.PolicyReader;
import it.polito.dp2.NFFG.sol3.service.ServiceException;
import it.polito.dp2.NFFG.sol3.service.data.VerificationResult;
import it.polito.dp2.NFFG.sol3.service.jaxrs.Localhost_Neo4JXMLRest;
import it.polito.dp2.NFFG.sol3.service.jaxrs.Localhost_Neo4JXMLRest.Resource;

import javax.ws.rs.WebApplicationException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by giaco on 19/01/2017.
 */
public class Neo4JService {

	static final String NAME_PROP = "name";
	static final String BELONG_RELATION = "belongs";
	static final String LINK_TYPE = "Link";
	static final String CORE_LABEL = "NFFG";
	static final String DEFAULT_URL = "http://localhost:8080/Neo4JXML/rest";

	private static Neo4JService service = new Neo4JService();


	private Localhost_Neo4JXMLRest.Resource resource;
	private Map<String, Neo4JNffg> nffgMap;
	private Boolean initialized = false;

	private Neo4JService() {
		String URL = System.getProperty("it.polito.dp2.NFFG.lab3.NEO4JURL");
		if (URL == null) {
			URL = DEFAULT_URL;
		}
		Client client = Localhost_Neo4JXMLRest.createClient();
		try {
			resource = Localhost_Neo4JXMLRest.resource(client, new URI(URL));
		} catch (URISyntaxException ex) {
			resource = Localhost_Neo4JXMLRest.resource(client);
		}
		nffgMap = new HashMap<>();
		initialized = cleanGraph();
	}

	private Boolean cleanGraph() {
		//deleting all nodes
		Resource.Nodes nodesCall = resource.nodes();
		try {
			nodesCall.deleteAsXml(String.class);
			return true;
		} catch (WebApplicationException ex) {
			return false;
		}
	}


	public static Neo4JService getService() {
		return service;
	}

	public void loadNFFG(NffgReader nffg) throws NullPointerException, ServiceException {

		synchronized (this) {
			if (!initialized) {
				initialized = cleanGraph();
				if (!initialized)
					throw new ServiceException("Unable to clean nodes");
			}
		}

		if (nffg == null) {
			throw new NullPointerException("The NFFG is null.");
		}

		Neo4JNffg jNffg = new Neo4JNffg(nffg, resource);
		this.nffgMap.put(nffg.getName(), jNffg);
	}

	public VerificationResult testReachability(PolicyReader policy) throws ServiceException {
		Neo4JNffg nffg = this.nffgMap.get(policy.getNffg().getName());
		return nffg.testReachability(policy);
	}

}
