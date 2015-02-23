package com.barcelo.businessrules.model.api.dynamicpack;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Class that represents a Fact with all factors that influence the pricing of a transport
 *
 * @author dag-vsf
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TransportDistribution extends ComponentDistribution {
	private String originGroup;
	private String routeType;
	private List<String> companyList;
	private String cabin;
	private int segmentCount;
	private int stayQuantity;

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
