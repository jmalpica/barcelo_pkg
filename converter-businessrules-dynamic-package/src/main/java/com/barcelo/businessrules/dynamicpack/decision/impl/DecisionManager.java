package com.barcelo.businessrules.dynamicpack.decision.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.drools.KnowledgeBase;
import org.drools.agent.KnowledgeAgent;
import org.drools.agent.KnowledgeAgentConfiguration;
import org.drools.agent.KnowledgeAgentFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceChangeNotifier;
import org.drools.io.ResourceChangeScanner;
import org.drools.io.ResourceChangeScannerConfiguration;
import org.drools.io.ResourceFactory;
import org.drools.io.impl.ClassPathResource;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dag-vsf
 */
@Component(DecisionManager.SERVICENAME)
@Slf4j
public class DecisionManager {
	public static final String SERVICENAME = "decisionManager";
	private int pollingInterval = 60;
	private KnowledgeAgent kagent;
	// private KnowledgeBase kbase;
	private ResourceChangeScanner scannerService;

	public long getPollingInterval() {
		return pollingInterval;
	}

	public void setPollingInterval(int pollingInterval) {
		this.pollingInterval = pollingInterval;
		if (this.scannerService != null) {
			this.scannerService.stop();
			this.scannerService.setInterval(this.pollingInterval);
			this.scannerService.start();
		}
	}

	@PostConstruct
	public void postConstruct() {
		long start = System.currentTimeMillis();

		KnowledgeAgentConfiguration agentConfiguration = KnowledgeAgentFactory.newKnowledgeAgentConfiguration();
		kagent = KnowledgeAgentFactory.newKnowledgeAgent("KnowledgeAgent", agentConfiguration);
		kagent.addEventListener(new PkgKnowledgeAgentEventListener());
		kagent.monitorResourceChangeEvents(true);

		ClassPathResource resource = (ClassPathResource) ResourceFactory
				.newClassPathResource("KnowledgeAgentChangeSet.xml");
		resource.setResourceType(ResourceType.CHANGE_SET);
		kagent.applyChangeSet(resource);
		// kbase = kagent.getKnowledgeBase();

		scannerService = ResourceFactory.getResourceChangeScannerService();
		Properties resourceChangeScannerProperties = new Properties();
		try {
			InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("KnowledgeAgent.properties");
			resourceChangeScannerProperties.load(inputStream);
			inputStream.close();
		} catch (IOException e) {
			log.error("While loading the Properties file to configure scanning: ", e);
		}
		ResourceChangeScannerConfiguration scannerConfiguration;
		scannerConfiguration = scannerService.newResourceChangeScannerConfiguration(resourceChangeScannerProperties);
		log.info("Polling interval: {}", scannerConfiguration.getProperty("drools.resource.scanner.interval"));
		// scannerConfiguration.setProperty("drools.resource.scanner.interval", Integer.toString(this.pollingInterval));
		scannerService.configure(scannerConfiguration);
		scannerService.start();

		ResourceChangeNotifier resourceChangeNotifierService = ResourceFactory.getResourceChangeNotifierService();
		resourceChangeNotifierService.start();

		log.info("Inicializando Drools en : " + (System.currentTimeMillis() - start));
	}

	public StatefulKnowledgeSession createKieSession() {
		if (this.kagent == null) {
			log.warn("Drools is unitialized. If you are not using Spring, call postConstruct() manually");
			postConstruct();
		}

		KnowledgeBase kieBase = kagent.getKnowledgeBase();
		// StatelessKieSession statelessKieSession = kieBase.newStatelessKieSession();
		StatefulKnowledgeSession kieSession = kieBase.newStatefulKnowledgeSession();
		KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory.newFileLogger(kieSession, "kie_session");
		// statelessKieSession.getAgenda().getAgendaGroup( "Group A" ).setFocus();
		kieSession.getAgenda().getAgendaGroup("commission_and_markup").setFocus();
		kieSession.getAgenda().getAgendaGroup("setup").setFocus();
		return kieSession;
	}
}
