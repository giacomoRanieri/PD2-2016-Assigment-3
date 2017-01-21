package it.polito.dp2.NFFG.sol3.service;


import org.xml.sax.SAXException;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

/**
 * Created by giacomo on 18/01/2017.
 */
@Provider
@Consumes({"application/xml"})
public class NffgServiceValidatorProvider implements MessageBodyReader<JAXBElement<?>> {
	private final String jaxbPackage = "it.polito.dp2.NFFG.sol3.jaxb";
	private Unmarshaller unmarshaller;
	private Logger logger;


	public NffgServiceValidatorProvider() {
		logger = Logger.getLogger(NffgServiceValidatorProvider.class.getName());

		try {
			InputStream schemaStream = NffgServiceValidatorProvider.class.getResourceAsStream("/xsd/nffgVerifier.xsd");
			InputStream secondSchemaStream = NffgServiceValidatorProvider.class.getResourceAsStream("/xsd/nffgInfo.xsd");
			if (schemaStream == null || secondSchemaStream == null) {
				logger.log(Level.SEVERE, "xml schema file Not found.");
				throw new IOException();
			}
			JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
			unmarshaller = jc.createUnmarshaller();
			SchemaFactory sf = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
			StreamSource[] sources = new StreamSource[2];
			sources[0] = new StreamSource(secondSchemaStream);
			sources[1] = new StreamSource(schemaStream);
			Schema schema = sf.newSchema(sources);
			unmarshaller.setSchema(schema);

			logger.log(Level.INFO, "NffgServiceValidator initialized successfully");
		} catch (SAXException | JAXBException | IOException se) {
			logger.log(Level.SEVERE, "Error parsing xml directory file. Service will not work properly.", se);
		}
	}

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return JAXBElement.class.equals(type) || jaxbPackage.equals(type.getPackage().getName());
	}

	@Override
	public JAXBElement<?> readFrom(Class<JAXBElement<?>> type, Type genericType, Annotation[] annotations, MediaType mediaType,
								   MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {
		try {
			return (JAXBElement<?>) unmarshaller.unmarshal(entityStream);
		} catch (JAXBException ex) {
			logger.log(Level.WARNING, "Request body validation error.", ex);
			throw new BadRequestException("Request body validation error");
		}
	}

}
