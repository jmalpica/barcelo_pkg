package com.barcelo.businessrules.model.api.dynamicpack;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Class that represents a Fact with all factors that influence the pricing of an Hotel
 *
 * @author dag-vsf
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HotelDistribution extends ComponentDistribution {
	private String chain;
	private String hotel;
	private String hotelType;
	private String category;
	private String mealPlan;
	private int nightQuantity;

	/* It seems Guvnor has some problems with inheritance in methods. The following ones are to help it find them. */

	@Override
	public void addCommissionRate(BigDecimal delta) {
		super.addCommissionRate(delta);
	}

	@Override
	public void addOverCommissionAmount(BigDecimal delta) {
		super.addOverCommissionAmount(delta);
	}

	@Override
	public void addProfitabilityRate(BigDecimal delta) {
		super.addProfitabilityRate(delta);
	}

	@Override
	public void calculatePrices() {
		super.calculatePrices();
	}

	@Override
	public void calculatePreBookingPrices() {
		super.calculatePreBookingPrices();
	}
}
