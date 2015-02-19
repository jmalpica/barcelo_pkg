package com.barcelo.businessrules.dynamicpack.decision.impl;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.barcelo.businessrules.dynamicpack.decision.DecisionServiceInterface;
import com.barcelo.integration.engine.model.api.request.pack.TOProductAvailabilityRQ;
import com.barcelo.integration.engine.model.api.response.pack.TOProductAvailabilityRS;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dag-vsf
 */
//@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-config.xml")
@Slf4j
public class DecisionServiceImplTest {
	@Autowired
	private DecisionServiceInterface decisionServiceInterface;

	private TOProductAvailabilityRQ toProductAvailabilityRQ;
	private TOProductAvailabilityRS toProductAvailabilityRS;

	@Before
	public void setUp() throws Exception {
		log.info("Starting setup...");

		long start = System.currentTimeMillis();
		JAXBContext jaxbContextRQ = JAXBContext.newInstance(TOProductAvailabilityRQ.class);
		log.info("Inicializando contexto en : " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		JAXBContext jaxbContextRS = JAXBContext.newInstance(TOProductAvailabilityRS.class);
		log.info("Inicializando contexto en : " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		Unmarshaller jaxbUnmarshaller = jaxbContextRQ.createUnmarshaller();
		log.info("Creando Unmarshaller en : " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		File file1 = new File("src/test/resources/1.xml");
		this.toProductAvailabilityRQ = (TOProductAvailabilityRQ) jaxbUnmarshaller.unmarshal(file1);
		log.info("Unmarshalling en : " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		jaxbUnmarshaller = jaxbContextRS.createUnmarshaller();
		log.info("Creando Unmarshaller en : " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		File file2 = new File("src/test/resources/ej_PD_JOL_F_Mallorca_optimizada.xml");
		this.toProductAvailabilityRS = (TOProductAvailabilityRS) jaxbUnmarshaller.unmarshal(file2);
		log.info("Unmarshalling en : " + (System.currentTimeMillis() - start));

		log.info("Setup finished...");
	}

	@After
	public void tearDown() throws Exception {
		log.info("Starting teardown...");
	}

	@Test
	public void testCalculatePrices() throws Exception {
		Assert.assertNotNull(this.decisionServiceInterface);
		Assert.assertNotNull(this.toProductAvailabilityRQ);
		Assert.assertNotNull(this.toProductAvailabilityRS);
		decisionServiceInterface.calculatePrices(this.toProductAvailabilityRQ, this.toProductAvailabilityRS);
/*
		for (int ii = 0; ii < 10; ii++) {
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				log.warn("While sleeping", e);
			}
		}
*/
	}
}
