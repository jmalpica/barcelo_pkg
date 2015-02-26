package com.barcelo.businessrules.dynamicpack.decision.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.barcelo.businessrules.dynamicpack.converter.ApiModelConverterInterface;
import com.barcelo.businessrules.dynamicpack.converter.FactModelConverterInterface;
import com.barcelo.businessrules.dynamicpack.converter.impl.ApiModelConverterImpl;
import com.barcelo.businessrules.dynamicpack.converter.impl.FactModelConverterImpl;
import com.barcelo.businessrules.dynamicpack.decision.DecisionServiceInterface;
import com.barcelo.businessrules.dynamicpack.decision.DecisionSessionInterface;
import com.barcelo.businessrules.model.api.dynamicpack.ComponentDistribution;
import com.barcelo.businessrules.model.api.dynamicpack.DynamicPackage;
import com.barcelo.businessrules.model.api.dynamicpack.Traveller;
import com.barcelo.integration.engine.model.api.request.BarMasterRQ;
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

	public void calculatePrices(BarMasterRQ barMasterRQ, BarMasterRS barMasterRS) {
		if (barMasterRQ instanceof TOProductAvailabilityRQ && barMasterRS instanceof TOProductAvailabilityRS) {
			calculatePrices((TOProductAvailabilityRQ) barMasterRQ, (TOProductAvailabilityRS) barMasterRS);
		} else if (barMasterRQ instanceof TOProductPreBookingRQ && barMasterRS instanceof TOProductPreBookingRS) {
			calculatePreBookingPrices((TOProductPreBookingRQ) barMasterRQ, (TOProductPreBookingRS) barMasterRS);
		} else {
			throw new IllegalArgumentException("The inputs are not instances of the right classes");
		}
	}

	public void calculatePrices(TOProductAvailabilityRQ toProductAvailabilityRQ,
								TOProductAvailabilityRS toProductAvailabilityRS) {
		long start = System.currentTimeMillis();

		log.info("Convirtiendo al modelo de hechos");
		FactModelConverterInterface converter = getFactConverter();
		DynamicPackage dynamicPackage = converter.toModelInterface(toProductAvailabilityRQ, toProductAvailabilityRS);

		calculatePricesOnFacts(dynamicPackage, false, false);

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
	Map<String, Integer> calculatePricesOnFacts(DynamicPackage dynamicPackage, boolean countActivations, boolean logObjects) {
		log.info("Convirtiendo a lista de hechos");
		List<Object> factList = addFacts(dynamicPackage);
		log.info("Creando la sesion");
		DecisionSessionInterface kieSession = decisionManager.createKieSession();

		kieSession.configureMonitorization(dynamicPackage.isDebugTraces(), countActivations, logObjects);

		log.info("Insertando hechos");
		for (Object fact : factList) {
			kieSession.insert(fact);
			if (logObjects) {
				log.info("Insertando hecho : {}.", fact);
			}
		}

		log.info("Programando agendas...");
		kieSession.scheduleAgendaGroup("setup");
		kieSession.scheduleAgendaGroup("commission_and_markup");

		log.info("Llamando a fireAllRules.");
		kieSession.fireAllRules();

		Map<String,Integer> result = null;
		if (countActivations) {
			result = kieSession.getCountingMap();
		}

		log.info("Eliminando la sesion.");
		kieSession.dispose();

		return result;
	}

	public void calculatePreBookingPrices(TOProductPreBookingRQ toProductPreBookingRQ,
										  TOProductPreBookingRS toProductPreBookingRS) {
		long start = System.currentTimeMillis();

		log.info("Convirtiendo al modelo de hechos");
		FactModelConverterInterface converter = getFactConverter();
		DynamicPackage dynamicPackage = converter.toModelInterface(toProductPreBookingRQ, toProductPreBookingRS);

		calculatePricesOnFacts(dynamicPackage, false, false);

		log.info("Convirtiendo al modelo JAXB.");
		toApplicationModel(dynamicPackage, toProductPreBookingRS);

		log.info("calculatePrices run in : {} ms.", System.currentTimeMillis() - start);
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
