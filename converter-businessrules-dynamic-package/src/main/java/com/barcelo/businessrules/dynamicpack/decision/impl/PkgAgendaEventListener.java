package com.barcelo.businessrules.dynamicpack.decision.impl;

import org.drools.event.rule.*;
import org.drools.runtime.rule.Activation;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dag-vsf
 */
@Slf4j
public class PkgAgendaEventListener implements AgendaEventListener {
	public void activationCreated(ActivationCreatedEvent event) {
		Activation activation = event.getActivation();
		log.info("Scheduled rule {} on {}.", activation.getRule().getName(), activation.getDeclarationIDs());
	}

	public void activationCancelled(ActivationCancelledEvent event) {
		Activation activation = event.getActivation();
		log.info("Cancelled rule {} on {}.", activation.getRule().getName(), activation.getDeclarationIDs());
	}

	public void beforeActivationFired(BeforeActivationFiredEvent event) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void afterActivationFired(AfterActivationFiredEvent event) {
		Activation activation = event.getActivation();
		log.info("Fired rule {} on {}.", activation.getRule().getName(), activation.getDeclarationIDs());
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
