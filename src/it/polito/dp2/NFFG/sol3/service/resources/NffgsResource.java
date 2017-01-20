package it.polito.dp2.NFFG.sol3.service.resources;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import it.polito.dp2.NFFG.NffgReader;
import it.polito.dp2.NFFG.NffgVerifierException;
import it.polito.dp2.NFFG.PolicyReader;
import it.polito.dp2.NFFG.lab2.ServiceException;
import it.polito.dp2.NFFG.sol3.service.NffgsService;
import it.polito.dp2.NFFG.sol3.service.data.Nffg;
import it.polito.dp2.NFFG.sol3.service.data.ReachabilityPolicy;
import it.polito.dp2.NFFG.sol3.service.jaxb.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by giacomo on 16/01/2017.
 */
@Path("/nffgs")
@Api(value = "/nffgs", description = "Manage the stored nffgs")
public class NffgsResource {

	private ObjectFactory factory = new ObjectFactory();
	private NffgsService service = NffgsService.getService();
	private Logger logger;

	public NffgsResource() {
		logger = Logger.getLogger(PoliciesResource.class.getName());
	}

	@GET
	@ApiOperation(value = "Retrieve all stored nffgs", notes = "xml format")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	@Produces({MediaType.APPLICATION_XML})
	public Nffgs getNffgs() {
		Set<NffgReader> nffgs = service.getNffgs();
		Nffgs result = factory.createNffgs();
		List<RestrictedNffgType> nffgList = result.getNffg();
		for (NffgReader nffg : nffgs) {
			nffgList.add(((Nffg) nffg).toXMLObject());
		}
		return result;
	}

	//@DELETE
	//void removeNffgs();

	@POST
	@ApiOperation(value = "Add a new nffg", notes = "xml format")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	@Produces({MediaType.APPLICATION_XML})
	@Consumes({MediaType.APPLICATION_XML})
	public JAXBElement<RestrictedNffgType> addNffg(JAXBElement<RestrictedNffgType> nffg) {
		NffgReader newNffg;
		try {
			synchronized (service.getNffgSyncObj()) {
				newNffg = service.addNffg(nffg.getValue());
			}
		} catch (NffgVerifierException e) {
			logger.log(Level.SEVERE, "Nffg not valid: " + e.getMessage());
			throw new ForbiddenException("Nffg not valid: " + e.getMessage());
		} catch (ServiceException | NullPointerException e) {
			throw new InternalServerErrorException("Unable to load the nffg: "+e.getMessage());
		}
		return factory.createNffg(((Nffg) newNffg).toXMLObject());
	}

	@Path("{name}")
	@GET
	@ApiOperation(value = "Retrieve a nffg", notes = "xml format")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	@Produces({MediaType.APPLICATION_XML})
	public JAXBElement<RestrictedNffgType> getNffg(@PathParam("name") String name) {

		NffgReader nffg = service.getNffg(name);
		if (nffg == null) {
			logger.log(Level.SEVERE, "Nffg not found: " + name);
			throw new NotFoundException("Nffg not found");
		}
		return factory.createNffg(((Nffg) nffg).toXMLObject());
	}

	@Path("{name}/policies")
	@GET
	@ApiOperation(value = "Retrieve all policies of a nffg", notes = "xml format")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	@Produces({MediaType.APPLICATION_XML})
	public Policies getNffgPolicies(@PathParam("name") String name) {
		NffgReader nffg = service.getNffg(name);
		if (nffg == null) {
			logger.log(Level.SEVERE, "Nffg not found: " + name);
			throw new NotFoundException("Nffg not found");
		}
		Set<PolicyReader> related = service.getNffgPolicies(nffg);
		Policies policies = factory.createPolicies();
		List<EnhancedPolicyType> pols = policies.getPolicy();
		for (PolicyReader pol : related) {
			pols.add(((ReachabilityPolicy) pol).toXMLObject());
		}
		return policies;
	}

	@Path("{name}/policies")
	@DELETE
	@ApiOperation(value = "Remove all policies of a nffg", notes = "xml format")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	public Policies removeNffgPolicies(@PathParam("name") String name) {
		NffgReader nffg = service.getNffg(name);
		if (nffg == null) {
			logger.log(Level.SEVERE, "Nffg not found: " + name);
			throw new NotFoundException("Nffg not found");
		}
		Set<PolicyReader> deleted;
		synchronized (service.getSyncObj()) {
			deleted = service.removeNffgPolicies(nffg);
		}
		Policies policies = factory.createPolicies();
		List<EnhancedPolicyType> pols = policies.getPolicy();
		for (PolicyReader pol : deleted) {
			pols.add(((ReachabilityPolicy) pol).toXMLObject());
		}
		return policies;
	}

	@Path("{name}/policies")
	@POST
	@ApiOperation(value = "Add a new policy to the nffg", notes = "xml format")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	@Produces({MediaType.APPLICATION_XML})
	@Consumes({MediaType.APPLICATION_XML})
	public JAXBElement<EnhancedPolicyType> addNffgPolicy(@PathParam("name") String name, JAXBElement<EnhancedPolicyType> newPolicy) {
		NffgReader nffg = service.getNffg(name);
		if (nffg == null) {
			logger.log(Level.SEVERE, "Nffg not found: " + name);
			throw new NotFoundException("Nffg not found");
		}
		ReachabilityPolicy policy;
		synchronized (service.getSyncObj()) {
			try {
				policy = service.addNffgPolicy(nffg, newPolicy.getValue());
			} catch (NffgVerifierException e) {
				logger.log(Level.SEVERE, "Policy not valid: " + e.getMessage());
				throw new ForbiddenException("Policy not valid: " + e.getMessage());
			}
		}
		return factory.createPolicy(policy.toXMLObject());
	}
}
