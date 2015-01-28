package com.barcelo.businessrules.model.api;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Class that represents a Fact with all factors that influence the pricing of an Hotel
 *
 * @author dag-vsf
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HotelDistribution extends ComponentDistribution {
	private String chain;
	private String hotel;
	private String hotelType;
	private String category;
	private String accommodationType;
	private int nightQuantity;
}
