package com.barcelo.businessrules.model.api.dynamicpack;

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
}
