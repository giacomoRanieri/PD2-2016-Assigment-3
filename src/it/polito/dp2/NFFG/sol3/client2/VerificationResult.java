package it.polito.dp2.NFFG.sol3.client2;

import it.polito.dp2.NFFG.PolicyReader;
import it.polito.dp2.NFFG.VerificationResultReader;
import it.polito.dp2.NFFG.sol3.jaxb.VerificationResultType;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by giacomo on 17/01/2017.
 */
public class VerificationResult implements VerificationResultReader {

	private Boolean result;
	private PolicyReader policy;
	private String message;
	private Calendar time;

	public VerificationResult(PolicyReader policy, VerificationResultType result) {
		this(policy, result.isVerificationResult(), result.getVerificationTime().toGregorianCalendar(), result.getVerificationResultMsg());
	}

	public VerificationResult(PolicyReader policy, Boolean result) {
		this.policy = policy;
		this.result = result;
		this.message = "";
		this.time = new GregorianCalendar();
	}

	public VerificationResult(PolicyReader policy, Boolean result, GregorianCalendar time) {
		this(policy, result);
		this.time = time;
	}

	public VerificationResult(PolicyReader policy, Boolean result, GregorianCalendar time, String message) {
		this(policy, result, time);
		this.message = message;
	}

	@Override
	public PolicyReader getPolicy() {
		return policy;
	}

	public void setPolicy(Policy policy) {
		if (policy == null)
			throw new NullPointerException();
		this.policy = policy;
	}

	@Override
	public Boolean getVerificationResult() {
		return result;
	}

	public void setVerificationResult(Boolean result) {
		if (result == null)
			throw new NullPointerException();
		this.result = result;
	}

	@Override
	public String getVerificationResultMsg() {
		return message;
	}

	public void setVerificationResultMsg(String message) {
		if (message == null)
			throw new NullPointerException();
		this.message = message;
	}

	@Override
	public Calendar getVerificationTime() {
		return time;
	}

	public void setVerificationTime(Calendar time) {
		if (time == null)
			throw new NullPointerException();
		this.time = time;
	}


	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return 23 * super.hashCode();
	}

}
