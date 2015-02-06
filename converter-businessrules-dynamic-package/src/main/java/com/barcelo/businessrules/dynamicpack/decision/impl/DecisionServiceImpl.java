package com.barcelo.businessrules.dynamicpack.decision.impl;

import java.util.ArrayList;
import java.util.List;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
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
import com.barcelo.integration.engine.model.api.response.pack.TOProductAvailabilityRS;

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
		log.info("Convirtiendo al modelo de hechos");
		DynamicPackage dynamicPackage = toFactModel(toProductAvailabilityRQ, toProductAvailabilityRS);
		log.info("Convirtiendo a lista de hechos");
		List<Object> factList = addFacts(dynamicPackage);
		log.info("Creando la sesion");
		KieSession kieSession = decisionManager.createKieSession();
		log.info("Insertando hechos");
		for (Object fact : factList) {
			kieSession.insert(fact);
		}
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
		log.info("Convirtiendo al modelo JAXB.");
		toApplicationModel(dynamicPackage, toProductAvailabilityRS);
	}

	public void calculatePreBookingPrices(TOProductAvailabilityRQ toProductAvailabilityRQ,
										  TOProductAvailabilityRS toProductAvailabilityRS) {
		log.info("Convirtiendo al modelo de hechos");
		DynamicPackage dynamicPackage = toFactModel(toProductAvailabilityRQ, toProductAvailabilityRS);
		log.info("Convirtiendo a lista de hechos");
		List<Object> factList = addFacts(dynamicPackage);
		log.info("Creando la sesion");
		KieSession kieSession = decisionManager.createKieSession();
		log.info("Insertando hechos");
		for (Object fact : factList) {
			kieSession.insert(fact);
		}
		log.info("Llamando a fireAllRules.");
		kieSession.fireAllRules();
		log.info("Eliminando la sesion.");
		kieSession.dispose();
		for (Traveller traveller : dynamicPackage.getTravellerList()) {
			log.info("Traveller : {}", traveller);
		}
		/*
		for (ComponentDistribution componentDistribution : dynamicPackage.getComponentDistributionList()) {
			componentDistribution.calculatePreBookingPrices();
		}
		*/
		log.info("Convirtiendo al modelo JAXB.");
		toApplicationModel(dynamicPackage, toProductAvailabilityRS);
	}

	private DynamicPackage toFactModel(TOProductAvailabilityRQ toProductAvailabilityRQ,
									   TOProductAvailabilityRS toProductAvailabilityRS) {
		FactModelConverterInterface factModelConverterInterface;
		if (factModelConverterInterfaceObjectFactory != null) {
			factModelConverterInterface = factModelConverterInterfaceObjectFactory.getObject();
		} else {
			log.warn("Spring factModelConverter ObjectFactory not autowired. Using manual configuration.");
			factModelConverterInterface = new FactModelConverterImpl();
		}
		return factModelConverterInterface.toModelInterface(toProductAvailabilityRQ, toProductAvailabilityRS);
	}

	private void toApplicationModel(DynamicPackage dynamicPackage, TOProductAvailabilityRS toProductAvailabilityRS) {
		ApiModelConverterInterface apiModelConverterInterface;
		if (apiModelConverterInterfaceObjectFactory != null) {
			apiModelConverterInterface = apiModelConverterInterfaceObjectFactory.getObject();
		} else {
			log.warn("Spring apiModelConverter ObjectFactory not autowired. Using manual configuration.");
			apiModelConverterInterface = new ApiModelConverterImpl();
		}
		apiModelConverterInterface.toApplicationModel(dynamicPackage, toProductAvailabilityRS);
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
