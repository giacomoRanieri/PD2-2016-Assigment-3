package it.polito.dp2.NFFG.sol3.service.resources;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import it.polito.dp2.NFFG.NffgVerifierException;
import it.polito.dp2.NFFG.PolicyReader;
import it.polito.dp2.NFFG.lab3.ServiceException;
import it.polito.dp2.NFFG.sol3.service.NffgsService;
import it.polito.dp2.NFFG.sol3.service.data.Policy;
import it.polito.dp2.NFFG.sol3.service.data.ReachabilityPolicy;
import it.polito.dp2.NFFG.sol3.service.data.VerificationResult;
import it.polito.dp2.NFFG.sol3.jaxb.*;

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
@Path("/policies")
@Api(value = "/policies", description = "Manage the stored policies")
public class PoliciesResource {

	private NffgsService service = NffgsService.getService();
	private ObjectFactory factory = new ObjectFactory();
	private Logger logger;

	public PoliciesResource() {
		logger = Logger.getLogger(PoliciesResource.class.getName());
	}

	@GET
	@ApiOperation(value = "Retrieve all stored policies", notes = "xml format")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	@Produces({MediaType.APPLICATION_XML})
	public Policies getPolicies() {
		Set<PolicyReader> allPol = service.getPolicies();
		Policies policies = factory.createPolicies();
		List<EnhancedPolicyType> pols = policies.getPolicy();
		for (PolicyReader pol : allPol) {
			pols.add(((ReachabilityPolicy) pol).toXMLObject());
		}
		return policies;
	}

	@DELETE
	@ApiOperation(value = "Delete all stored policies", notes = "xml format")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	@Produces({MediaType.APPLICATION_XML})
	public Policies deletePolicies() {
		Set<PolicyReader> deleted;
		synchronized (service.getSyncObj()) {
			deleted = service.removePolicies();
		}
		Policies policies = factory.createPolicies();
		List<EnhancedPolicyType> pols = policies.getPolicy();
		for (PolicyReader pol : deleted) {
			pols.add(((ReachabilityPolicy) pol).toXMLObject());
		}
		return policies;
	}

	@Path("{name}")
	@PUT
	@ApiOperation(value = "Update a policy", notes = "xml format")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	@Consumes({MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_XML})
	public JAXBElement<EnhancedPolicyType> updatePolicy(@PathParam("name") String policyName, JAXBElement<EnhancedPolicyType> policy) {

		ReachabilityPolicy newPol;
		synchronized (service.getSyncObj()) {
			PolicyReader oldPol = service.getPolicy(policyName);
			if (oldPol == null) {
				logger.log(Level.SEVERE, "Policy not found");
				throw new NotFoundException("Policy not found");
			}
			try {
				newPol = (ReachabilityPolicy) service.updatePolicy(oldPol, policy.getValue());
			} catch (NffgVerifierException e) {
				logger.log(Level.SEVERE, "Unable to update Policy:" + e.getMessage());
				throw new BadRequestException("Unable to update Policy:" + e.getMessage());
			}
		}
		return factory.createPolicy(newPol.toXMLObject());
	}

	@Path("{name}")
	@GET
	@ApiOperation(value = "Retrieve a policy", notes = "xml format")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	@Produces({MediaType.APPLICATION_XML})
	public JAXBElement<EnhancedPolicyType> getPolicy(@PathParam("name") String policyName) {
		ReachabilityPolicy pol = (ReachabilityPolicy) service.getPolicy(policyName);
		if (pol == null) {
			logger.log(Level.SEVERE, "Policy not found");
			throw new NotFoundException("Policy not found");
		}
		return factory.createPolicy(pol.toXMLObject());
	}

	@Path("{name}")
	@DELETE
	@ApiOperation(value = "Delete a policy", notes = "xml format")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	@Produces({MediaType.APPLICATION_XML})
	public JAXBElement<EnhancedPolicyType> deletePolicy(@PathParam("name") String policyName) {
		PolicyReader pol;
		synchronized (service.getSyncObj()) {
			pol = service.getPolicy(policyName);
			if (pol == null) {
				logger.log(Level.SEVERE, "Policy not found");
				throw new NotFoundException("Policy not found");
			}
			pol = service.removePolicy(pol);
		}
		return factory.createPolicy(((ReachabilityPolicy) pol).toXMLObject());
	}

	@Path("{name}/verificationResult")
	@POST
	@ApiOperation(value = "Request verification of one or more policy", notes = "xml format")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	@Produces({MediaType.APPLICATION_XML})
	public Results verifyPolicies(@PathParam("name") String policyName, @QueryParam("names") List<String> otherPolicies) {
		otherPolicies.add(0, policyName);
		Results res = factory.createResults();
		List<EnhancedVerificationResultType> reslist = res.getVerificationResult();
		for (String s : otherPolicies) {
			synchronized (service.getSyncObj()) {
				PolicyReader pol = service.getPolicy(s);
				if (pol == null) {
					throw new NotFoundException("Policy not found");
				}
				VerificationResult result;
				try {
					result = (VerificationResult) service.verifyPolicy(pol);
				} catch (ServiceException e) {
					throw new InternalServerErrorException("Unable to verify the policy");
				}
				service.updateVerificationResult(result, (Policy) pol);
				reslist.add(result.toXMLObject());
			}
		}
		return res;
	}

	@Path("{name}/verificationResult")
	@GET
	@ApiOperation(value = "Retrieve verification result of one policy", notes = "xml format")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	@Produces({MediaType.APPLICATION_XML})
	public JAXBElement<EnhancedVerificationResultType> getVerificationResult(@PathParam("name") String policyName) {
		PolicyReader pol = service.getPolicy(policyName);
		if (pol == null)
			throw new NotFoundException("Policy not found");
		VerificationResult res = (VerificationResult) pol.getResult();
		if (res == null)
			return null;
		return factory.createVerificationResult(res.toXMLObject());
	}
}
