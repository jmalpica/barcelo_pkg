package com.barcelo.businessrules.pkg_dinamico;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.event.DebugProcessEventListener;
import org.drools.event.rule.DebugAgendaEventListener;
import org.drools.event.rule.DebugWorkingMemoryEventListener;
import org.drools.ide.common.client.modeldriven.dt52.GuidedDecisionTable52;
import org.drools.ide.common.server.util.GuidedDTDRLPersistence;
import org.drools.ide.common.server.util.GuidedDTXMLPersistence;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dag-vsf
 */
@Slf4j
public class TestHelper {
	private final String packageName;
	private final List<Class<?>> importList;
	private final List<Object> factList;
	private final List<String> agendaGroupList;
	private final KnowledgeBuilder kbuilder;
	private String header = null;

	public TestHelper(String packageName) {
		this.packageName = packageName;
		this.importList = new ArrayList<Class<?>>();
		this.factList = new ArrayList<Object>();
		this.agendaGroupList = new ArrayList<String>();
		kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
	}

	private static void printClasspath() {
		log.info(" ====== Printing classpath START =========== ");
		//Get the System Classloader
		ClassLoader sysClassLoader = TestHelper.class.getClassLoader();

		//Get the URLs
		URL[] urls = ((URLClassLoader) sysClassLoader).getURLs();

		for (URL url : urls) {
			log.info(url.getFile());
		}
		log.info(" ====== Printing classpath END =========== ");
	}

	private static void printContextClassLoaderClasspath() {
		log.info(" ====== Printing CCL classpath START =========== ");
		//Get the Thread Context Classloader
		ClassLoader sysClassLoader = Thread.currentThread().getContextClassLoader();

		//Get the URLs
		URL[] urls = ((URLClassLoader) sysClassLoader).getURLs();

		for (URL url : urls) {
			log.info(url.getFile());
		}
		log.info(" ====== Printing CCL classpath END =========== ");
	}

	public void addImport(Class<?> clazz) {
		if (this.header != null) {
			log.error("Todos los imports han de añadirse antes de importar la primera tabla.");
			throw new IllegalStateException("Import after rule");
		}
		this.importList.add(clazz);
	}

	public void addGDST(String element) {
		if (header == null) {
			assembleHeader();
		}
		if (log.isTraceEnabled()) {
			log.trace("Resource URL : {}", Thread.currentThread().getContextClassLoader().getResource(element));
			log.trace("Resource URL : {}", DtDeterminePaxAgeTypeTest.class.getResource(element));
		}
		InputStream inputStream = null;
		if (element.startsWith("/")) {
			inputStream = TestHelper.class.getResourceAsStream(element);
		} else {
			inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(element);
		}
		Reader reader = null;
		StringBuilder sb = new StringBuilder();
		try {
			reader = new InputStreamReader(inputStream, "UTF-8");

			char[] chars = new char[32768];
			int length = 0;
			while ((length = reader.read(chars)) > 0) {
				sb.append(chars, 0, length);
			}
		} catch (UnsupportedEncodingException e) {
			log.error("Failure reading the GDST: ", e);
			throw new RuntimeException("Invalid encoding", e);
		} catch (IOException e) {
			log.error("Failure reading the GDST: ", e);
			throw new RuntimeException("Read failure", e);
		}

		String xml = sb.toString();
		log.trace("gdst : {}", xml);

		//convertimos el XML a un modelo de objetos
		GuidedDecisionTable52 model = GuidedDTXMLPersistence.getInstance().unmarshal(xml);
		//compilamos el modelo de objetos a lenguaje DRL
		String drl = this.header + GuidedDTDRLPersistence.getInstance().marshal(model);
		log.debug("Generated DRL: {}", drl);

		Resource drlResource = ResourceFactory.newReaderResource(new StringReader(drl));
		kbuilder.add(drlResource, ResourceType.DRL);
	}

	private void assembleHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("package ").append(this.packageName).append('\n');
		sb.append('\n');
		for (Class<?> importClass : this.importList) {
			sb.append("import ").append(importClass.getName()).append('\n');
		}
		sb.append('\n');
		this.header = sb.toString();
	}

	public void addAgendaGroup(String agendaGroup) {
		this.agendaGroupList.add(agendaGroup);
	}

	public void addFact(Object fact) {
		this.factList.add(fact);
	}

	public void run() {
		if (kbuilder.hasErrors()) {
			log.error("Building errors: {}", kbuilder.getErrors());
			throw new IllegalStateException("Errors in Builder, can't create session");
		}

		log.info("Creando la sesion.");
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();

		if (log.isDebugEnabled()) {
			session.addEventListener(new DebugAgendaEventListener());
			// session.addEventListener(new DebugProcessEventListener());
			session.addEventListener(new DebugWorkingMemoryEventListener());
		}

		session.getAgenda().clear();

		log.info("Insertando hechos.");
		for (Object fact : this.factList) {
			session.insert(fact);
		}
		log.info("Cargados {} hechos.", session.getFactCount());

		log.info("Configurando agendaGroups.");
		for (int ii = agendaGroupList.size() - 1; ii >= 0; ii--) {
			String agendaGroup = agendaGroupList.get(ii);
			session.getAgenda().getAgendaGroup(agendaGroup).setFocus();
		}

		log.info("Llamando a fireAllRules.");
		session.fireAllRules();

		log.info("Eliminando la sesion.");
		session.dispose();
	}
}
