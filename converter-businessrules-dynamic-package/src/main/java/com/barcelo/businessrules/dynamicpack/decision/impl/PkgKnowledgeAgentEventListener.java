package com.barcelo.businessrules.dynamicpack.decision.impl;

import org.drools.event.knowledgeagent.*;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dag-vsf
 */
@Slf4j
public class PkgKnowledgeAgentEventListener implements KnowledgeAgentEventListener {
	private final DecisionManager decisionManager;

	public PkgKnowledgeAgentEventListener(DecisionManager decisionManager) {
		this.decisionManager = decisionManager;
	}

	public void beforeChangeSetApplied(BeforeChangeSetAppliedEvent event) {
		log.info("PkgKnowledgeAgentEventListener : beforeChangeSetApplied called : {}", event);
	}

	public void afterChangeSetApplied(AfterChangeSetAppliedEvent event) {
		log.info("PkgKnowledgeAgentEventListener : afterChangeSetApplied called : {}", event);
	}

	public void beforeChangeSetProcessed(BeforeChangeSetProcessedEvent event) {
		log.info("PkgKnowledgeAgentEventListener : beforeChangeSetProcessed called : {}", event);
	}

	public void afterChangeSetProcessed(AfterChangeSetProcessedEvent event) {
		log.info("PkgKnowledgeAgentEventListener : afterChangeSetProcessed called : {}", event);
	}

	public void beforeResourceProcessed(BeforeResourceProcessedEvent event) {
		log.info("PkgKnowledgeAgentEventListener : beforeResourceProcessed called : {}", event);
	}

	public void afterResourceProcessed(AfterResourceProcessedEvent event) {
		log.info("PkgKnowledgeAgentEventListener : afterResourceProcessed called : {}", event);
	}

	public void knowledgeBaseUpdated(KnowledgeBaseUpdatedEvent event) {
		decisionManager.setKnowledgeBase(event.getKnowledgeBase());
		log.info("PkgKnowledgeAgentEventListener : knowledgeBaseUpdated called : {}", event);
	}

	public void resourceCompilationFailed(ResourceCompilationFailedEvent event) {
		log.info("PkgKnowledgeAgentEventListener : resourceCompilationFailed called : {}", event);
	}
}
