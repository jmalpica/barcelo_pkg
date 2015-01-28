package com.barcelo.decision.bom;

import lombok.Data;

/**
 * Class that represents the fact that a component is unprofitable.
 *
 * @author dag-vsf
 */
@Data
public class ProfitabilityWarning {
	private ComponentDistribution componentDistribution;
}
