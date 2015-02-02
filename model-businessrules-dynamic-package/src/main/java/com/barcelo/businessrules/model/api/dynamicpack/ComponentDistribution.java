package com.barcelo.businessrules.model.api.dynamicpack;

import java.math.BigDecimal;
import java.util.Date;

import com.barcelo.integration.engine.model.api.shared.pack.TOPriceInformation;

import lombok.Data;

/**
 * Base class for the Fact model for Drools rule set.
 *
 * @author dag-vsf
 */
@Data
public abstract class ComponentDistribution {
	private DynamicPackage dynamicPackage;
	private String rateType;
	private String destinationGroup;
	private Date startDateTime;
	private Date endDateTime;
	private int startWeekday;
	private int endWeekday;
	private int daysInAdvance;
	private String travellerType;
	private String travellerAgeType;
	private String provider;
	private String season;

	// The following ones will be filled by the business rules

	private BigDecimal commissionableAmount;
	private BigDecimal nonCommissionableAmount;
	private BigDecimal taxRate;
	private BigDecimal commissionRate;
	private BigDecimal overCommissionAmount;
	private BigDecimal commissionAmount;
	private BigDecimal profitabilityRate;
	private BigDecimal profitabilityAmount;
	private BigDecimal taxAmount;
	private boolean maxCommissionExceeded;
	private boolean profitabilityWarning;

	/**
	 * Reference kept to be able to streamline the JAXB model modification
 	 */
	private TOPriceInformation priceInformationRef;
}
