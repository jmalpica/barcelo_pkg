package com.barcelo.businessrules.pkg_dinamico;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;

import org.drools.workbench.models.guided.dtable.backend.GuidedDTDRLPersistence;
import org.drools.workbench.models.guided.dtable.backend.GuidedDTXMLPersistence;
import org.drools.workbench.models.guided.dtable.shared.model.GuidedDecisionTable52;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dag-vsf
 */
@Slf4j
public class TestHelper {
	public static KieSession build(String... elements) {
		printClasspath();
		printContextClassLoaderClasspath();
/*
		try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {
			log.warn("While sleeping: ", e);
		}
*/

		KieServices kieServices = KieServices.Factory.get();

		KieModuleModel kieModuleModel = kieServices.newKieModuleModel();

/*
		KieBaseModel kieBaseModel1 = kieModuleModel.newKieBaseModel("KBase1 ")
				.setDefault(true)
				.setEqualsBehavior(EqualityBehaviorOption.EQUALITY)
				.setEventProcessingMode(EventProcessingOption.STREAM);

		KieSessionModel ksessionModel1 = kieBaseModel1.newKieSessionModel("KSession1")
				.setDefault(true)
				.setType(KieSessionModel.KieSessionType.STATEFUL)
				.setClockType(ClockTypeOption.get("realtime"));
*/

		KieFileSystem kfs = kieServices.newKieFileSystem();
		log.info(kieModuleModel.toXML());
		kfs.writeKModuleXML(kieModuleModel.toXML());

		for (String element : elements) {
			log.info("Resource URL : {}", Thread.currentThread().getContextClassLoader().getResource(element));
			log.info("Resource URL : {}", DtDeterminePaxAgeTypeTest.class.getResource(element));
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
				e.printStackTrace();  //This should not happen
			} catch (IOException e) {
				e.printStackTrace();  // TODO: Decide what to do here
			}

			String xml = sb.toString();
			log.trace("gdst : {}", xml);

			//convertimos el XML a un modelo de objetos
			GuidedDecisionTable52 model = GuidedDTXMLPersistence.getInstance().unmarshal(xml);
			//compilamos el modelo de objetos a lenguaje DRL
			String drl = GuidedDTDRLPersistence.getInstance().marshal(model);
			log.info("Generated DRL: {}", drl);

			Resource drlResource = kieServices.getResources().newReaderResource(new StringReader(drl));
			drlResource.setResourceType(ResourceType.DRL);

			// Resource drlResource = kieServices.getResources().newReaderResource(new StringReader(xml));
			String glue = element.startsWith("/") ? "" : "/";
			String path = "/src/main/resources" + glue + element.replace(".gdst", ".drl");
			log.info("DestinationPath: {}", path);
			drlResource.setTargetPath(path);
			kfs.write(drlResource);
		}

		kieServices.newKieBuilder(kfs).buildAll();

		KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
		return kieContainer.newKieSession();
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
}
