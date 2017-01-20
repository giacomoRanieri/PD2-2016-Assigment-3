package it.polito.dp2.NFFG.sol3.service;

import javax.ws.rs.InternalServerErrorException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by giacomo on 18/01/2017.
 */
public class NffgUtils {

	public static XMLGregorianCalendar convertToXMLCalendar(Calendar c) {
		XMLGregorianCalendar calendar;
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(c.getTime());
		gc.setTimeZone(c.getTimeZone());
		try {
			calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
		} catch (DatatypeConfigurationException e) {
			throw new InternalServerErrorException("Can't convert calendar");
		}
		return calendar;
	}
}
