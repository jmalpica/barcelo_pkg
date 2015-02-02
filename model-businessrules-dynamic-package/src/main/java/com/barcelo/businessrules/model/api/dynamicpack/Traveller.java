package com.barcelo.businessrules.model.api.dynamicpack;

import lombok.Data;

/**
 * Class that represents each of the travellers that are going to make use of the DynamicPackage.
 *
 * @author dag-vsf
 */
@Data
public class Traveller {
	private int age;
	private String ageType;
	private String type;
}
