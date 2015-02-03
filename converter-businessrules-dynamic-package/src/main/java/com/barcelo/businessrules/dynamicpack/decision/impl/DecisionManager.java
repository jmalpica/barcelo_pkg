package com.barcelo.businessrules.dynamicpack.decision.impl;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dag-vsf
 */
@Slf4j
public class DecisionManager {
	public StatelessKieSession createKieSession() {
		long start = System.currentTimeMillis();

		KieServices ks = KieServices.Factory.get();
		// ReleaseId releaseId = ks.newReleaseId("com.barcelo.integration", "droolstest", "0.0.1-SNAPSHOT");
		// KieContainer kContainer = ks.newKieContainer(releaseId);
		KieContainer kContainer = ks.getKieClasspathContainer();
		KieBase kieBase = kContainer.getKieBase();
		log.info("Inicializando Drools en : " + (System.currentTimeMillis() - start));

		StatelessKieSession statelessKieSession = kieBase.newStatelessKieSession();
		KieRuntimeLogger logger = ks.getLoggers().newFileLogger(statelessKieSession, "kie_session");
		return statelessKieSession;
	}
}
