package com.barcelo.businessrules.dynamicpack.decision.impl;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.ReleaseId;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dag-vsf
 */
@Component(DecisionManager.SERVICENAME)
@Slf4j
public class DecisionManager {
	public static final String SERVICENAME = "decisionManager";

	private long pollingInterval = 600000L;

	public long getPollingInterval() {
		return pollingInterval;
	}

	public void setPollingInterval(long pollingInterval) {
		this.pollingInterval = pollingInterval;
	}

	public StatelessKieSession createKieSession() {
		long start = System.currentTimeMillis();

		KieServices ks = KieServices.Factory.get();
		ReleaseId releaseId = ks.newReleaseId("com.barcelo.businessrules", "pkg-dinamico", "LATEST");
		KieContainer kContainer = ks.newKieContainer(releaseId);
		// KieContainer kContainer = ks.getKieClasspathContainer();
		KieScanner kieScanner = ks.newKieScanner(kContainer);
		kieScanner.start(this.pollingInterval);
		KieBase kieBase = kContainer.getKieBase();
		log.info("Inicializando Drools en : " + (System.currentTimeMillis() - start));

		StatelessKieSession statelessKieSession = kieBase.newStatelessKieSession();
		KieRuntimeLogger logger = ks.getLoggers().newFileLogger(statelessKieSession, "kie_session");
		return statelessKieSession;
	}
}
