package com.barcelo.businessrules.dynamicpack.decision;

import java.util.Map;

/**
 * @author dag-vsf
 */
public interface DecisionSessionInterface {
	void configureMonitorization(boolean keepRuleList, boolean countActivations, boolean logObjects);

	void insert(Object fact);

	void scheduleAgendaGroup(String... agendaGroups);

	int fireAllRules();

	void dispose();

	Map<String, Integer> getCountingMap();
}
