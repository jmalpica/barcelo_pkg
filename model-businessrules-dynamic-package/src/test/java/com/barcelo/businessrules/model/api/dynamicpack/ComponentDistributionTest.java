package com.barcelo.businessrules.model.api.dynamicpack;

import java.math.BigDecimal;

import org.junit.Assert;

/**
 * Test of the calculations, using data from one of the excels as validation.
 *
 * @author dag-vsf
 */
public class ComponentDistributionTest {
	@org.junit.Test
	public void testCalculatePrices() throws Exception {
		ComponentDistribution componentDistribution;

		componentDistribution = new HotelDistribution();
		componentDistribution.setCommissionableAmount(new BigDecimal("448.70"));
		componentDistribution.setNonCommissionableAmount(BigDecimal.ZERO);
		componentDistribution.setTaxRate(new BigDecimal("0.21"));
		componentDistribution.setCommissionRate(new BigDecimal("0.12"));
		componentDistribution.setOverCommissionAmount(new BigDecimal("10.00"));
		componentDistribution.setProfitabilityRate(new BigDecimal("0.06"));
		componentDistribution.setProfitabilityAmount(BigDecimal.ZERO);
		componentDistribution.calculatePrices();
		Assert.assertEquals(new BigDecimal("559.39"), componentDistribution.getTotalAmount());
		Assert.assertEquals(new BigDecimal("559.39"), componentDistribution.getTotalCommissionableAmount());
		Assert.assertEquals(new BigDecimal("77.13"), componentDistribution.getCommissionAmount());
		Assert.assertEquals(new BigDecimal("16.20"), componentDistribution.getCommissionTaxesAmount());
	}

	@org.junit.Test
	public void testCalculatePrices2() throws Exception {
		ComponentDistribution componentDistribution;

		componentDistribution = new TransportDistribution();
		componentDistribution.setCommissionableAmount(new BigDecimal("1190.00"));
		componentDistribution.setNonCommissionableAmount(new BigDecimal("727.46"));
		componentDistribution.setTaxRate(new BigDecimal("0.21"));
		componentDistribution.setCommissionRate(new BigDecimal("0.12"));
		componentDistribution.setOverCommissionAmount(BigDecimal.ZERO);
		componentDistribution.setProfitabilityRate(new BigDecimal("0.025"));
		componentDistribution.setProfitabilityAmount(BigDecimal.ZERO);
		componentDistribution.calculatePrices();
		Assert.assertEquals(new BigDecimal("2140.54"), componentDistribution.getTotalAmount());
		Assert.assertEquals(new BigDecimal("1413.08"), componentDistribution.getTotalCommissionableAmount());
		Assert.assertEquals(new BigDecimal("169.57"), componentDistribution.getCommissionAmount());
		Assert.assertEquals(new BigDecimal("35.61"), componentDistribution.getCommissionTaxesAmount());
	}

	@org.junit.Test
	public void testCalculatePrices3() throws Exception {
		ComponentDistribution componentDistribution;

		componentDistribution = new HotelDistribution();
		componentDistribution.setCommissionableAmount(new BigDecimal("140.08"));
		componentDistribution.setNonCommissionableAmount(BigDecimal.ZERO);
		componentDistribution.setTaxRate(new BigDecimal("0.21"));
		componentDistribution.setCommissionRate(new BigDecimal("0.09"));
		componentDistribution.setOverCommissionAmount(new BigDecimal("7.00"));
		componentDistribution.setProfitabilityRate(new BigDecimal("0.02"));
		componentDistribution.setProfitabilityAmount(BigDecimal.ZERO);
		componentDistribution.calculatePrices();
		Assert.assertEquals(new BigDecimal("165.26"), componentDistribution.getTotalAmount());
		Assert.assertEquals(new BigDecimal("165.26"), componentDistribution.getTotalCommissionableAmount());
		Assert.assertEquals(new BigDecimal("21.87"), componentDistribution.getCommissionAmount());
		Assert.assertEquals(new BigDecimal("4.59"), componentDistribution.getCommissionTaxesAmount());
	}

	// @org.junit.Ignore("Fails because of rounding")
	@org.junit.Test
	public void testCalculatePrices4() throws Exception {
		ComponentDistribution componentDistribution;

		componentDistribution = new TransportDistribution();
		componentDistribution.setCommissionableAmount(new BigDecimal("82.78"));
		componentDistribution.setNonCommissionableAmount(new BigDecimal("61.16"));
		componentDistribution.setTaxRate(new BigDecimal("0.21"));
		componentDistribution.setCommissionRate(new BigDecimal("0.12"));
		componentDistribution.setOverCommissionAmount(BigDecimal.ZERO);
		componentDistribution.setProfitabilityRate(new BigDecimal("0.005"));
		componentDistribution.setProfitabilityAmount(BigDecimal.ZERO);
		componentDistribution.calculatePrices();
		Assert.assertEquals(new BigDecimal("156.12"), componentDistribution.getTotalAmount());
		Assert.assertEquals(new BigDecimal("94.96"), componentDistribution.getTotalCommissionableAmount());
		Assert.assertEquals(new BigDecimal("11.39"), componentDistribution.getCommissionAmount());
		Assert.assertEquals(new BigDecimal("2.39"), componentDistribution.getCommissionTaxesAmount());
	}
}
