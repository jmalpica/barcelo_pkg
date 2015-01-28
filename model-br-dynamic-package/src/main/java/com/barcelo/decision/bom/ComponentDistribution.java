package com.barcelo.decision.bom;

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
	private Date departureDateTime;
	private Date arrivalDateTime;
	private int departureWeekday;
	private int arrivalWeekday;
	private int daysInAdvance;
	private String travellerType;
	private String travellerAgeType;
	private String provider;

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
	private ProfitabilityWarning profitabilityWarning;

	/**
	 * Reference kept to be able to streamline the JAXB model modification
 	 */
	private TOPriceInformation priceInformationRef;
}
