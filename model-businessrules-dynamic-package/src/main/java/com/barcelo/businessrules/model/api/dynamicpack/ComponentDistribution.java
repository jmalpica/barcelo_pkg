package com.barcelo.businessrules.model.api.dynamicpack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

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
	private BigDecimal commissionTaxesAmount;
	private BigDecimal totalAmount;
	private BigDecimal totalCommissionableAmount;
	private boolean profitabilityWarning;

	/**
	 * Reference kept to be able to streamline the JAXB model modification
 	 */
	private Object priceInformationRef;

	private void calculate() {
		BigDecimal ttooCost = this.commissionableAmount.add(this.nonCommissionableAmount);
		this.commissionAmount = this.nonCommissionableAmount.multiply(this.commissionRate);
		this.commissionAmount = this.commissionAmount.add(this.overCommissionAmount);
		BigDecimal suggestedRetailPriceDenominator = this.profitabilityRate.subtract(BigDecimal.ONE);
		suggestedRetailPriceDenominator = suggestedRetailPriceDenominator.add(this.commissionRate);
		BigDecimal suggestedRetailPriceNumerator = this.commissionAmount.subtract(ttooCost);
		this.totalAmount = suggestedRetailPriceNumerator.divide(suggestedRetailPriceDenominator, 2,
				RoundingMode.HALF_EVEN);
		BigDecimal agencyNetPriceSubtrahend = this.taxRate.add(BigDecimal.ONE).multiply(this.commissionAmount);
		this.totalCommissionableAmount = this.totalAmount.subtract(agencyNetPriceSubtrahend);
		this.commissionTaxesAmount = this.commissionAmount.multiply(this.taxRate);
	}

	private void calculate2() {
		BigDecimal ttooCost = this.commissionableAmount.add(this.nonCommissionableAmount);
		BigDecimal totalAmountNumerator = this.nonCommissionableAmount.multiply(this.commissionRate);
		totalAmountNumerator = totalAmountNumerator.subtract(this.overCommissionAmount);
		totalAmountNumerator = totalAmountNumerator.subtract(ttooCost);
		BigDecimal totalAmountDenominator = this.profitabilityRate.subtract(BigDecimal.ONE);
		totalAmountDenominator = totalAmountDenominator.add(this.commissionRate);
		this.totalAmount = totalAmountNumerator.divide(totalAmountDenominator, 3,
				RoundingMode.HALF_UP);
		this.totalCommissionableAmount = this.totalAmount.subtract(this.nonCommissionableAmount);
		this.commissionAmount = this.totalCommissionableAmount.multiply(this.commissionRate);
		this.commissionAmount = this.commissionAmount.add(this.overCommissionAmount);
		this.commissionTaxesAmount = this.commissionAmount.multiply(this.taxRate);

		// Rounding stage
		this.totalAmount = this.totalAmount.setScale(2, RoundingMode.HALF_UP);
		this.totalCommissionableAmount = this.totalCommissionableAmount.setScale(2, RoundingMode.HALF_UP);
		this.commissionAmount = this.commissionAmount.setScale(2, RoundingMode.HALF_UP);
		this.commissionTaxesAmount = this.commissionTaxesAmount.setScale(2, RoundingMode.HALF_UP);
	}

	public void calculatePrices() {
		// Temporary implementations, until we get the differences between the two processes
		calculate2();
	}

	public void calculatePreBookingPrices() {
		// Temporary implementations, until we get the differences between the two processes
		calculate2();
	}

	public void addCommissionRate(BigDecimal delta) {
		if (this.commissionRate == null) {
			this.commissionRate = delta;
		} else {
			this.commissionRate = this.commissionRate.add(delta);
		}
	}

	public void addOverCommissionAmount(BigDecimal delta) {
		if (this.overCommissionAmount == null) {
			this.overCommissionAmount = delta;
		} else {
			this.overCommissionAmount = this.overCommissionAmount.add(delta);
		}
	}

	public void addProfitabilityRate(BigDecimal delta) {
		if (this.profitabilityRate == null) {
			this.profitabilityRate = delta;
		} else {
			this.profitabilityRate = this.profitabilityRate.add(delta);
		}
	}
}
