package com.barcelo.decision.bom;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Root class of the fact tree that will represent all items whose price could be modified
 *
 * @author dag-vsf
 */
@Data
public class DynamicPackage {
	private String brand;
	private String channel;
	private String subChannel;
	private String managementGroup;
	private String agency;
	private String branchOffice;
	private String bookingDate;
	private int bookingWeekday;
	private List<ComponentDistribution> componentDistributionList;
}
