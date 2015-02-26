package com.barcelo.businessrules.dynamicpack.converter.impl;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
// import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.barcelo.businessrules.dynamicpack.converter.FactModelConverterInterface;
import com.barcelo.businessrules.model.api.dynamicpack.DynamicPackage;
import com.barcelo.integration.engine.model.api.request.pack.TOProductAvailabilityRQ;
import com.barcelo.integration.engine.model.api.response.pack.TOProductAvailabilityRS;

import lombok.extern.slf4j.Slf4j;

import static org.joda.time.Days.daysBetween;
import static org.junit.Assert.assertEquals;

/**
 * @author dag-vsf
 */
@Slf4j
public class FactModelConverterImplTest {
	@org.junit.Test
	public void testToModelInterface1() throws JAXBException {
		long start = System.currentTimeMillis();
		JAXBContext jaxbContextRQ = JAXBContext.newInstance(/* "com.barcelo.integration.engine.model.api" */ TOProductAvailabilityRQ.class);
		log.info("Inicializando contexto en : " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		JAXBContext jaxbContextRS = JAXBContext.newInstance(/* "com.barcelo.integration.engine.model.api" */ TOProductAvailabilityRS.class);
		log.info("Inicializando contexto en : " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		Unmarshaller jaxbUnmarshaller = jaxbContextRQ.createUnmarshaller();
		log.info("Creando Unmarshaller en : " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		File file1 = new File("src/test/resources/TOProductAvailabilityRQ.xml");
		TOProductAvailabilityRQ toProductAvailabilityRQ = (TOProductAvailabilityRQ) jaxbUnmarshaller.unmarshal(file1);
		log.info("Unmarshalling en : " + (System.currentTimeMillis() - start));

/*
		toProductAvailabilityRQ.setDebugTraces(true);

		start = System.currentTimeMillis();
		Marshaller jaxbMarshaller = jaxbContextRQ.createMarshaller();
		log.info("Creando Marshaller en : " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		File file2 = new File("src/test/resources/2.xml");
		jaxbMarshaller.marshal(toProductAvailabilityRQ, file2);
		log.info("Marshalling en : " + (System.currentTimeMillis() - start));
*/

		start = System.currentTimeMillis();
		jaxbUnmarshaller = jaxbContextRS.createUnmarshaller();
		log.info("Creando Unmarshaller en : " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		File file2 = new File("src/test/resources/TOProductAvailabilityRS.xml");
		TOProductAvailabilityRS toProductAvailabilityRS = (TOProductAvailabilityRS) jaxbUnmarshaller.unmarshal(file2);
		log.info("Unmarshalling en : " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		FactModelConverterInterface factModelConverter = new FactModelConverterImpl();
		DynamicPackage dynamicPackage = factModelConverter.toModelInterface(toProductAvailabilityRQ, toProductAvailabilityRS);
		log.info("To Model Interface en : " + (System.currentTimeMillis() - start));

		dynamicPackage.getComponentDistributionList().size();
	}

	/**
	 * From http://stackoverflow.com/questions/3802893/number-of-days-between-two-dates-in-joda-time
	 * @throws Exception
	 */
	@org.junit.Test
	public void testExampleDayDifference() {
		// 5am on the 20th to 1pm on the 21st, October 2013, Brazil
		DateTimeZone BRAZIL = DateTimeZone.forID("America/Sao_Paulo");
		DateTime start = new DateTime(2013, 10, 20, 5, 0, 0, BRAZIL);
		DateTime end = new DateTime(2013, 10, 21, 13, 0, 0, BRAZIL);

		assertEquals(0, daysBetween(start.withTimeAtStartOfDay(), end.withTimeAtStartOfDay()).getDays());

		assertEquals(1, daysBetween(start.toLocalDate(), end.toLocalDate()).getDays());
	}
}
