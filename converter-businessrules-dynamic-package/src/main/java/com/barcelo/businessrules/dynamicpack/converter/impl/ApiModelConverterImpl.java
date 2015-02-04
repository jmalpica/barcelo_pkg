package com.barcelo.businessrules.dynamicpack.converter.impl;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.barcelo.businessrules.dynamicpack.converter.ApiModelConverterInterface;
import com.barcelo.businessrules.model.api.dynamicpack.ComponentDistribution;
import com.barcelo.businessrules.model.api.dynamicpack.DynamicPackage;
import com.barcelo.integration.engine.model.api.response.pack.TOProductAvailabilityRS;
import com.barcelo.integration.engine.model.api.shared.Price;
import com.barcelo.integration.engine.model.api.shared.pack.TOPriceInformation;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dag-vsf
 */
@Service(ApiModelConverterInterface.SERVICENAME)
@Scope("prototype")
@Slf4j
public class ApiModelConverterImpl implements ApiModelConverterInterface {
	public void toApplicationModel(DynamicPackage dynamicPackage, TOProductAvailabilityRS toProductAvailabilityRS) {
		/* Note that the whole toProductAvailabilityRS is unnecessary for us, as we already keep relevant links to
		 * the price structures inside. We ask it only to remember the user that our data model doesn't contain all
		 * the required data so it is the responsibility of the caller to keep the uncorrupted toProductAvailabilityRS
		 * so we can make the required changes.
		 */
		for (ComponentDistribution componentDistribution : dynamicPackage.getComponentDistributionList()) {
			TOPriceInformation priceInformationRef =
					(TOPriceInformation) componentDistribution.getPriceInformationRef();
			String isoCurrency = priceInformationRef.getCommissionableAmount().getISOCurrency();

			Price totalAmount = new Price();
			totalAmount.setPrice(componentDistribution.getTotalAmount());
			totalAmount.setISOCurrency(isoCurrency);
			priceInformationRef.setTotalAmount(totalAmount);

			Price commissionAmount = new Price();
			commissionAmount.setPrice(componentDistribution.getCommissionAmount());
			commissionAmount.setISOCurrency(isoCurrency);
			priceInformationRef.setCommissionAmount(commissionAmount);

			Price commissionTaxesAmount = new Price();
			commissionTaxesAmount.setPrice(componentDistribution.getCommissionTaxesAmount());
			commissionTaxesAmount.setISOCurrency(isoCurrency);
			priceInformationRef.setCommissionTaxesAmount(commissionTaxesAmount);
		}
	}
}
