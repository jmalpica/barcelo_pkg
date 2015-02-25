package com.barcelo.businessrules.dynamicpack.decision;

import com.barcelo.integration.engine.model.api.request.pack.TOProductAvailabilityRQ;
import com.barcelo.integration.engine.model.api.request.pack.TOProductPreBookingRQ;
import com.barcelo.integration.engine.model.api.response.pack.TOProductAvailabilityRS;
import com.barcelo.integration.engine.model.api.response.pack.TOProductPreBookingRS;

/**
 * @author dag-vsf
 */
public interface DecisionServiceInterface {
	public static final String SERVICENAME = "decisionServiceInterface";

	void calculatePrices(TOProductAvailabilityRQ toProductAvailabilityRQ,
						 TOProductAvailabilityRS toProductAvailabilityRS);

	void calculatePreBookingPrices(TOProductPreBookingRQ toProductPreBookingRQ,
								   TOProductPreBookingRS toProductPreBookingRS);
}
