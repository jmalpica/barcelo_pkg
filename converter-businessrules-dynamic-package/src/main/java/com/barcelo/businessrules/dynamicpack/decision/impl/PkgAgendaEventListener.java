package com.barcelo.businessrules.dynamicpack.decision.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.event.rule.*;
import org.drools.runtime.rule.Activation;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dag-vsf
 */
@Slf4j
public class PkgAgendaEventListener implements AgendaEventListener {
	private List<String> ruleActivationList;
	private Map<String, Integer> countingMap;

	public PkgAgendaEventListener(boolean keepRuleList, boolean countActivations) {
		if (keepRuleList) {
			this.ruleActivationList = new ArrayList<String>();
		}
		if (countActivations) {
			this.countingMap = new HashMap<String, Integer>();
		}
	}

	public List<String> getRuleActivationList() {
		return ruleActivationList;
	}

	public void setRuleActivationList(List<String> ruleActivationList) {
		this.ruleActivationList = ruleActivationList;
	}

	public Map<String, Integer> getCountingMap() {
		return countingMap;
	}

	public void setCountingMap(Map<String, Integer> countingMap) {
		this.countingMap = countingMap;
	}

	public int getActivations(String name) {
		Integer result = 0;
		if (countingMap != null) {
			result = countingMap.get(name);
			result = result == null ? 0 : result;
		}
		return result;
	}

	public void activationCreated(ActivationCreatedEvent event) {
		Activation activation = event.getActivation();
		log.debug("Scheduled rule {} on {}.", activation.getRule().getName(), activation.getDeclarationIDs());
	}

	public void activationCancelled(ActivationCancelledEvent event) {
		Activation activation = event.getActivation();
		log.debug("Cancelled rule {} on {}.", activation.getRule().getName(), activation.getDeclarationIDs());
	}

	public void beforeActivationFired(BeforeActivationFiredEvent event) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void afterActivationFired(AfterActivationFiredEvent event) {
		Activation activation = event.getActivation();
		String name = activation.getRule().getName();
		log.info("Fired rule {} on {}.", name, activation.getObjects());
		if (ruleActivationList != null) {
			ruleActivationList.add(name);
		}
		if (countingMap != null) {
			Integer activations = countingMap.get(name);
			activations = activations == null ? 1 : activations + 1;
			countingMap.put(name, activations);
		}
	}

	public void agendaGroupPopped(AgendaGroupPoppedEvent event) {
		log.info("Popped AgendaGroup {}.", event.getAgendaGroup().getName());
	}

	public void agendaGroupPushed(AgendaGroupPushedEvent event) {
		log.info("Pushed AgendaGroup {}.", event.getAgendaGroup().getName());
	}

	public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
