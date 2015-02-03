package com.barcelo.businessrules.model.api.dynamicpack;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
	private String provider;
	private BigDecimal commissionableAmount;
	private BigDecimal nonCommissionableAmount;
	private BigDecimal taxRate;

	// The following ones will be filled by the business rules

	private String season;
	private BigDecimal commissionRate;
	private BigDecimal overCommissionAmount;
	private BigDecimal commissionAmount;
	private BigDecimal profitabilityRate;
	private BigDecimal profitabilityAmount;
	private boolean maxCommissionExceeded;
	private BigDecimal taxAmount;
	private BigDecimal suggestedRetailPrice;
	private BigDecimal agencyNetPrice;
	private boolean profitabilityWarning;

	/**
	 * Reference kept to be able to streamline the JAXB model modification
 	 */
	private TOPriceInformation priceInformationRef;

	private void calculate() {
		BigDecimal ttooCost = this.commissionableAmount.add(this.nonCommissionableAmount);
		this.commissionAmount = this.nonCommissionableAmount.multiply(this.commissionRate);
		this.commissionAmount = this.commissionAmount.add(this.overCommissionAmount);
		BigDecimal suggestedRetailPriceDenominator = this.profitabilityRate.subtract(BigDecimal.ONE);
		suggestedRetailPriceDenominator = suggestedRetailPriceDenominator.add(this.commissionRate);
		BigDecimal suggestedRetailPriceNumerator = this.commissionAmount.subtract(ttooCost);
		this.suggestedRetailPrice = suggestedRetailPriceNumerator.divide(suggestedRetailPriceDenominator, 2,
				RoundingMode.HALF_EVEN);
		BigDecimal agencyNetPriceSubtrahend = this.taxRate.add(BigDecimal.ONE).multiply(this.commissionAmount);
		this.agencyNetPrice = this.suggestedRetailPrice.subtract(agencyNetPriceSubtrahend);
		this.taxAmount = this.commissionAmount.multiply(this.taxRate);
	}

	private void calculate2() {
		BigDecimal ttooCost = this.commissionableAmount.add(this.nonCommissionableAmount);
		BigDecimal nonCommissionAmount = this.nonCommissionableAmount.multiply(this.commissionRate);
		nonCommissionAmount = nonCommissionAmount.subtract(this.overCommissionAmount);
		BigDecimal suggestedRetailPriceDenominator = this.profitabilityRate.subtract(BigDecimal.ONE);
		suggestedRetailPriceDenominator = suggestedRetailPriceDenominator.add(this.commissionRate);
		BigDecimal suggestedRetailPriceNumerator = nonCommissionAmount.subtract(ttooCost);
		this.suggestedRetailPrice = suggestedRetailPriceNumerator.divide(suggestedRetailPriceDenominator, 3,
				RoundingMode.HALF_UP);
		this.commissionAmount = this.suggestedRetailPrice.subtract(this.nonCommissionableAmount);
		this.commissionAmount = this.commissionAmount.multiply(this.commissionRate);
		this.commissionAmount = this.commissionAmount.add(this.overCommissionAmount);
		BigDecimal agencyNetPriceSubtrahend = this.taxRate.add(BigDecimal.ONE).multiply(this.commissionAmount);
		this.agencyNetPrice = this.suggestedRetailPrice.subtract(agencyNetPriceSubtrahend);
		this.taxAmount = this.commissionAmount.multiply(this.taxRate);

		// Rounding stage
		this.suggestedRetailPrice = this.suggestedRetailPrice.setScale(2, RoundingMode.HALF_UP);
		this.commissionAmount = this.commissionAmount.setScale(2, RoundingMode.HALF_UP);
		this.agencyNetPrice = this.agencyNetPrice.setScale(2, RoundingMode.HALF_UP);
		this.taxAmount = this.taxAmount.setScale(2, RoundingMode.HALF_UP);
	}

	public void calculatePrices() {
		// Temporary implementations, until we get the differences between the two processes
		calculate2();
	}

	public void calculatePreBookingPrices() {
		// Temporary implementations, until we get the differences between the two processes
		calculate2();
	}
}
