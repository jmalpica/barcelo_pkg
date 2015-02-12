package com.barcelo.businessrules.dynamicpack.decision.impl;

import javax.annotation.PostConstruct;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.ReleaseId;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
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

	private long pollingInterval = 10000L;
	private KieServices ks;
	private KieContainer kContainer;
	private KieScanner kieScanner;

	public long getPollingInterval() {
		return pollingInterval;
	}

	public void setPollingInterval(long pollingInterval) {
		this.pollingInterval = pollingInterval;
		if (this.kieScanner != null) {
			this.kieScanner.stop();
			this.kieScanner.start(this.pollingInterval);
		}
	}

	@PostConstruct
	public void postConstruct() {
		long start = System.currentTimeMillis();

		ks = KieServices.Factory.get();

		// http://stackoverflow.com/questions/21186570/loading-drools-kie-workbench-artifacts-directly-from-the-repository
        // String url = "http://192.168.6.184:8080/kiewb/maven2/com/barcelo/businessrules/pkg-dinamico/1.0/pkg-dinamico-1.0.jar";
		// String url = "http://192.168.6.184:8080/kiewb/maven2/";
		// ks.getResources().newUrlResource(url);

		ReleaseId releaseId = ks.newReleaseId("com.barcelo.businessrules", "pkg-dinamico", "LATEST");
		kContainer = ks.newKieContainer(releaseId);
		// KieContainer kContainer = ks.getKieClasspathContainer();
		kieScanner = ks.newKieScanner(kContainer);
		kieScanner.start(this.pollingInterval);
		log.info("Inicializando Drools en : " + (System.currentTimeMillis() - start));
	}

	public KieSession createKieSession() {
		if (this.ks == null) {
			log.warn("Drools is unitialized. If you are not using Spring, call postConstruct() manually");
			postConstruct();
		}

		KieBase kieBase = kContainer.getKieBase();
		// StatelessKieSession statelessKieSession = kieBase.newStatelessKieSession();
		KieSession kieSession = kieBase.newKieSession();
		KieRuntimeLogger logger = ks.getLoggers().newFileLogger(kieSession, "kie_session");
		// statelessKieSession.getAgenda().getAgendaGroup( "Group A" ).setFocus();
		kieSession.getAgenda().getAgendaGroup( "setup-pax" ).setFocus();
		kieSession.getAgenda().getAgendaGroup( "setup" ).setFocus();
		return kieSession;
	}
}
