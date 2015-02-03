package com.barcelo.businessrules.model.api.dynamicpack;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Class that represents a Fact with all factors that influence the pricing of a transport
 *
 * @author dag-vsf
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TransportDistribution extends ComponentDistribution {
	private String originGroup;
	private String routeType;
	private List<String> companyList;
	private String cabin;
	private int segmentCount;
	private int stayQuantity;
}
