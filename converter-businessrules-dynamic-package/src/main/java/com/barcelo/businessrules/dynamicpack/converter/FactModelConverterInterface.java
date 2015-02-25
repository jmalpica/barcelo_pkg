package com.barcelo.businessrules.dynamicpack.converter;

import com.barcelo.businessrules.model.api.dynamicpack.DynamicPackage;
import com.barcelo.integration.engine.model.api.request.pack.TOProductAvailabilityRQ;
import com.barcelo.integration.engine.model.api.request.pack.TOProductPreBookingRQ;
import com.barcelo.integration.engine.model.api.response.pack.TOProductAvailabilityRS;
import com.barcelo.integration.engine.model.api.response.pack.TOProductPreBookingRS;

/**
 * @author dag-vsf
 */
public interface FactModelConverterInterface {
	public static final String SERVICENAME = "factModelConverterInterface";

	DynamicPackage toModelInterface(TOProductAvailabilityRQ toProductAvailabilityRQ,
									TOProductAvailabilityRS toProductAvailabilityRS);

	DynamicPackage toModelInterface(TOProductPreBookingRQ toProductPreBookingRQ,
									TOProductPreBookingRS toProductPreBookingRS);
}
