package com.barcelo.businessrules.dynamicpack.converter;

import com.barcelo.businessrules.model.api.dynamicpack.DynamicPackage;
import com.barcelo.integration.engine.model.api.response.pack.TOProductAvailabilityRS;

/**
 * @author dag-vsf
 */
public interface ApiModelConverterInterface {
	public static final String SERVICENAME = "apiModelConverterInterface";

	public void toApplicationModel(DynamicPackage dynamicPackage, TOProductAvailabilityRS toProductAvailabilityRS);
}
