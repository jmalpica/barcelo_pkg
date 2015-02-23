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
import org.drools.event.rule.DebugAgendaEventListener;
import org.drools.event.rule.DebugWorkingMemoryEventListener;
import org.drools.ide.common.client.modeldriven.dt52.GuidedDecisionTable52;
import org.drools.ide.common.server.util.GuidedDTDRLPersistence;
import org.drools.ide.common.server.util.GuidedDTXMLPersistence;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;

import com.barcelo.businessrules.dynamicpack.decision.impl.PkgAgendaEventListener;

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
	private String header;
	private PkgAgendaEventListener agendaEventListener;

	public TestHelper(String packageName) {
		this.packageName = packageName;
		this.importList = new ArrayList<Class<?>>();
		this.factList = new ArrayList<Object>();
		this.agendaGroupList = new ArrayList<String>();
		kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
	}

	private static void printClasspath() {
		log.trace(" ====== Printing classpath START =========== ");
		//Get the System Classloader
		ClassLoader sysClassLoader = TestHelper.class.getClassLoader();

		//Get the URLs
		URL[] urls = ((URLClassLoader) sysClassLoader).getURLs();

		for (URL url : urls) {
			log.trace(url.getFile());
		}
		log.trace(" ====== Printing classpath END =========== ");
	}

	private static void printContextClassLoaderClasspath() {
		log.trace(" ====== Printing CCL classpath START =========== ");
		//Get the Thread Context Classloader
		ClassLoader sysClassLoader = Thread.currentThread().getContextClassLoader();

		//Get the URLs
		URL[] urls = ((URLClassLoader) sysClassLoader).getURLs();

		for (URL url : urls) {
			log.trace(url.getFile());
		}
		log.trace(" ====== Printing CCL classpath END =========== ");
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
			if (log.isTraceEnabled()) {
				printClasspath();
				printContextClassLoaderClasspath();
			}
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
		// This doesn't use the standard name sb because of a PMD rule
		StringBuilder stringBuilder = new StringBuilder();
		try {
			reader = new InputStreamReader(inputStream, "UTF-8");

			char[] chars = new char[32768];
			// This "spurious" duplication of the call to reader.read is because of a PMD rule
			int length = reader.read(chars);
			while (length > 0) {
				stringBuilder.append(chars, 0, length);
				length = reader.read(chars);
			}
		} catch (UnsupportedEncodingException e) {
			log.error("Failure reading the GDST: ", e);
			throw new IllegalStateException("Invalid encoding", e);
		} catch (IOException e) {
			log.error("Failure reading the GDST: ", e);
			throw new IllegalStateException("Read failure", e);
		}

		String xml = stringBuilder.toString();
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
		// This doesn't use the standard name sb because of a PMD rule
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("package ").append(this.packageName).append("\n\n");
		for (Class<?> importClass : this.importList) {
			stringBuilder.append("import ").append(importClass.getName()).append('\n');
		}
		stringBuilder.append('\n');
		this.header = stringBuilder.toString();
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

		agendaEventListener = new PkgAgendaEventListener(false, true);
		session.addEventListener(agendaEventListener);
		if (log.isDebugEnabled()) {
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

	public int getActivations(String ruleName) {
		return agendaEventListener.getActivations(ruleName);
	}
}
