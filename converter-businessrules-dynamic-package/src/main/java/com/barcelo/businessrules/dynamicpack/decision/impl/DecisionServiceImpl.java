package com.barcelo.businessrules.dynamicpack.decision.impl;

import java.util.ArrayList;
import java.util.List;

import org.drools.runtime.StatefulKnowledgeSession;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.barcelo.businessrules.dynamicpack.converter.ApiModelConverterInterface;
import com.barcelo.businessrules.dynamicpack.converter.FactModelConverterInterface;
import com.barcelo.businessrules.dynamicpack.converter.impl.ApiModelConverterImpl;
import com.barcelo.businessrules.dynamicpack.converter.impl.FactModelConverterImpl;
import com.barcelo.businessrules.dynamicpack.decision.DecisionServiceInterface;
import com.barcelo.businessrules.model.api.dynamicpack.ComponentDistribution;
import com.barcelo.businessrules.model.api.dynamicpack.DynamicPackage;
import com.barcelo.businessrules.model.api.dynamicpack.Traveller;
import com.barcelo.integration.engine.model.api.request.pack.TOProductAvailabilityRQ;
import com.barcelo.integration.engine.model.api.request.pack.TOProductPreBookingRQ;
import com.barcelo.integration.engine.model.api.response.BarMasterRS;
import com.barcelo.integration.engine.model.api.response.pack.TOProductAvailabilityRS;
import com.barcelo.integration.engine.model.api.response.pack.TOProductPreBookingRS;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dag-vsf
 */
@Service(DecisionServiceInterface.SERVICENAME)
@Slf4j
public class DecisionServiceImpl implements DecisionServiceInterface {
	@Autowired
	private ObjectFactory<FactModelConverterInterface> factModelConverterInterfaceObjectFactory;
	// private Provider<FactModelConverterInterface> factModelConverterInterfaceProvider;

	@Autowired
	private ObjectFactory<ApiModelConverterInterface> apiModelConverterInterfaceObjectFactory;
	// private Provider<ApiModelConverterInterface> apiModelConverterInterfaceProvider;

	@Autowired
	private DecisionManager decisionManager;

	public void calculatePrices(TOProductAvailabilityRQ toProductAvailabilityRQ,
								TOProductAvailabilityRS toProductAvailabilityRS) {
		long start = System.currentTimeMillis();

		log.info("Convirtiendo al modelo de hechos");
		DynamicPackage dynamicPackage = toFactModel(toProductAvailabilityRQ, toProductAvailabilityRS);

		calculatePricesOnFacts(dynamicPackage);

		log.info("Convirtiendo al modelo JAXB.");
		toApplicationModel(dynamicPackage, toProductAvailabilityRS);

		log.info("calculatePrices run in : {} ms.", System.currentTimeMillis() - start);
	}

	/**
	 * The purpose of this function is, on one hand to allow calculatePrices and calculatePreBookingPrices to share
	 * code. And in the other, provide a handy invocation for JUnit tests that doen't require making complex and
	 * very, very fragile assertions on a changing JaxB external model (Testing of the JaxB conversion routines should
	 * be made in another JUnit test using several round-trips over a known XML)
	 * @param dynamicPackage The package that holds all the Facts to evaluate the rules over.
	 */
	void calculatePricesOnFacts(DynamicPackage dynamicPackage) {
		log.info("Convirtiendo a lista de hechos");
		List<Object> factList = addFacts(dynamicPackage);
		log.info("Creando la sesion");
		StatefulKnowledgeSession kieSession = decisionManager.createKieSession();
		log.info("Insertando hechos");
		for (Object fact : factList) {
			kieSession.insert(fact);
			log.info("Insertando hecho : {}.", fact);
		}
/*
		kieSession.getAgenda().getAgendaGroup("commission_and_markup").setFocus();
		kieSession.getAgenda().getAgendaGroup("setup").setFocus();
*/
		log.info("Llamando a fireAllRules.");
		kieSession.fireAllRules();

		log.info("Eliminando la sesion.");
		kieSession.dispose();
		for (Traveller traveller : dynamicPackage.getTravellerList()) {
			log.info("Traveller : {}", traveller);
		}
		/*
		for (ComponentDistribution componentDistribution : dynamicPackage.getComponentDistributionList()) {
			componentDistribution.calculatePrices();
		}
		*/
	}

	public void calculatePreBookingPrices(TOProductPreBookingRQ toProductAvailabilityRQ,
										  TOProductPreBookingRS toProductAvailabilityRS) {
		long start = System.currentTimeMillis();

		log.info("Convirtiendo al modelo de hechos");
		DynamicPackage dynamicPackage = toFactModel(toProductAvailabilityRQ, toProductAvailabilityRS);

		calculatePricesOnFacts(dynamicPackage);

		log.info("Convirtiendo al modelo JAXB.");
		toApplicationModel(dynamicPackage, toProductAvailabilityRS);

		log.info("calculatePrices run in : {} ms.", System.currentTimeMillis() - start);
	}

	private DynamicPackage toFactModel(TOProductAvailabilityRQ request, TOProductAvailabilityRS response) {
		return getFactConverter().toModelInterface(request, response);
	}

	private DynamicPackage toFactModel(TOProductPreBookingRQ request, TOProductPreBookingRS response) {
		return getFactConverter().toModelInterface(request, response);
	}

	private FactModelConverterInterface getFactConverter() {
		FactModelConverterInterface factModelConverterInterface;
		if (factModelConverterInterfaceObjectFactory != null) {
			factModelConverterInterface = factModelConverterInterfaceObjectFactory.getObject();
		} else {
			log.warn("Spring factModelConverter ObjectFactory not autowired. Using manual configuration.");
			factModelConverterInterface = new FactModelConverterImpl();
		}
		return factModelConverterInterface;
	}

	private void toApplicationModel(DynamicPackage dynamicPackage, BarMasterRS response) {
		getApiConverter().toApplicationModel(dynamicPackage, response);
	}

	private ApiModelConverterInterface getApiConverter() {
		ApiModelConverterInterface apiModelConverterInterface;
		if (apiModelConverterInterfaceObjectFactory != null) {
			apiModelConverterInterface = apiModelConverterInterfaceObjectFactory.getObject();
		} else {
			log.warn("Spring apiModelConverter ObjectFactory not autowired. Using manual configuration.");
			apiModelConverterInterface = new ApiModelConverterImpl();
		}
		return apiModelConverterInterface;
	}

	private List<Object> addFacts(DynamicPackage dynamicPackage) {
		List<Object> result = new ArrayList<Object>();

		result.add(dynamicPackage);

		for (Traveller traveller : dynamicPackage.getTravellerList()) {
			result.add(traveller);
		}

		for (ComponentDistribution componentDistribution : dynamicPackage.getComponentDistributionList()) {
			result.add(componentDistribution);
		}

		return result;
	}
}
