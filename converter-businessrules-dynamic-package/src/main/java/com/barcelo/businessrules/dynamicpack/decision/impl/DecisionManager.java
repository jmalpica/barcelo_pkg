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
import org.drools.runtime.StatefulKnowledgeSession;
import org.springframework.stereotype.Component;

import com.barcelo.businessrules.dynamicpack.decision.DecisionSessionInterface;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dag-vsf
 */
@Component(DecisionManager.SERVICENAME)
@Slf4j
public class DecisionManager {
	public static final String SERVICENAME = "decisionManager";
	private int pollingInterval = 60;
	private KnowledgeAgent knowledgeAgent;
	private KnowledgeBase knowledgeBase;
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

	public KnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
	}

	public void setKnowledgeBase(KnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}

	@PostConstruct
	public void postConstruct() {
		long start = System.currentTimeMillis();

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

		KnowledgeAgentConfiguration agentConfiguration = KnowledgeAgentFactory.newKnowledgeAgentConfiguration();
		knowledgeAgent = KnowledgeAgentFactory.newKnowledgeAgent("KnowledgeAgent", agentConfiguration);

		ClassPathResource resource = (ClassPathResource) ResourceFactory
				.newClassPathResource("KnowledgeAgentChangeSet.xml");
		resource.setResourceType(ResourceType.CHANGE_SET);
		knowledgeAgent.applyChangeSet(resource);
		knowledgeBase = knowledgeAgent.getKnowledgeBase();

		knowledgeAgent.monitorResourceChangeEvents(true);
		knowledgeAgent.addEventListener(new PkgKnowledgeAgentEventListener(this));

		log.info("Inicializando Drools en : " + (System.currentTimeMillis() - start));
	}

	public DecisionSessionInterface createKieSession() {
		if (this.knowledgeAgent == null) {
			log.warn("Drools is unitialized. If you are not using Spring, call postConstruct() manually");
			postConstruct();
		}
		if (this.knowledgeBase == null) {
			log.error("Failure initializing DecisionManager. Is the package in Guvnor available?");
			throw new IllegalStateException("KnowledgeBase not ready");
		}

		StatefulKnowledgeSession kieSession = knowledgeBase.newStatefulKnowledgeSession();

		return new DecisionSessionImpl(kieSession);
	}
}
