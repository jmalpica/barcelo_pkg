package com.barcelo.businessrules.dynamicpack.decision.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;

import com.barcelo.businessrules.dynamicpack.decision.DecisionSessionInterface;

/**
 * @author dag-vsf
 */
public class DecisionSessionImpl implements DecisionSessionInterface {
	private List<String> agendaGroupList;
	private StatefulKnowledgeSession session;
	private PkgAgendaEventListener agendaEventListener;
	private PkgWorkingMemoryEventListener workingMemoryEventListener;

	public DecisionSessionImpl(StatefulKnowledgeSession session) {
		this.session = session;
		this.agendaGroupList = new ArrayList<String>();
	}

	public void configureMonitorization(boolean keepRuleList, boolean countActivations, boolean logObjects) {
		removeCurrentListeners();
		if (keepRuleList || countActivations) {
			this.agendaEventListener = new PkgAgendaEventListener(keepRuleList, countActivations);
			this.session.addEventListener(this.agendaEventListener);
		}
		if (logObjects) {
			this.workingMemoryEventListener = new PkgWorkingMemoryEventListener();
			this.session.addEventListener(this.workingMemoryEventListener);
			/* KnowledgeRuntimeLogger logger = */
			KnowledgeRuntimeLoggerFactory.newFileLogger(this.session, "kie_session");
		}
	}

	public void insert(Object fact) {
		this.session.insert(fact);
	}

	public void scheduleAgendaGroup(String... agendaGroups) {
		Collections.addAll(agendaGroupList, agendaGroups);
	}

	public int fireAllRules() {
		for (int i = agendaGroupList.size() - 1; i >= 0 ; i--) {
			String agendaGroup = agendaGroupList.get(i);
			this.session.getAgenda().getAgendaGroup(agendaGroup).setFocus();
		}
		return this.session.fireAllRules();
	}

	public void dispose() {
		removeCurrentListeners();
		this.session.dispose();
		this.session = null;
	}

	public Map<String, Integer> getCountingMap() {
		if (this.agendaEventListener == null) {
			return null;
		}
		return this.agendaEventListener.getCountingMap();
	}

	private void removeCurrentListeners() {
		if (this.agendaEventListener != null) {
			this.session.removeEventListener(this.agendaEventListener);
			this.agendaEventListener = null;
		}
		if (this.workingMemoryEventListener != null) {
			this.session.removeEventListener(this.workingMemoryEventListener);
			this.workingMemoryEventListener = null;
		}
	}
}
