package com.barcelo.businessrules.model.api.dynamicpack;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Class that represents each of the travellers that are going to make use of the DynamicPackage.
 *
 * @author dag-vsf
 */
@Data
public class Traveller {
	private DynamicPackage dynamicPackage;
	private int age;
	private String ageType;
	private String type;
}
