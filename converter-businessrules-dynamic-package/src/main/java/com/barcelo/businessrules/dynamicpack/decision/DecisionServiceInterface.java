package com.barcelo.businessrules.dynamicpack.decision;

import com.barcelo.integration.engine.model.api.request.pack.TOProductAvailabilityRQ;
import com.barcelo.integration.engine.model.api.response.pack.TOProductAvailabilityRS;

/**
 * @author dag-vsf
 */
public interface DecisionServiceInterface {
	public static final String SERVICENAME = "decisionServiceInterface";

	void calculatePrices(TOProductAvailabilityRQ toProductAvailabilityRQ,
						 TOProductAvailabilityRS toProductAvailabilityRS);

	void calculatePreBookingPrices(TOProductAvailabilityRQ toProductAvailabilityRQ,
								   TOProductAvailabilityRS toProductAvailabilityRS);
}
