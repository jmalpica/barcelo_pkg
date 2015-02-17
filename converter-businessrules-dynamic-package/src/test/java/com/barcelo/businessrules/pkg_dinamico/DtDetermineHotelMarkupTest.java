package com.barcelo.businessrules.pkg_dinamico;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.barcelo.businessrules.dynamicpack.decision.impl.DecisionManager;
import com.barcelo.businessrules.model.api.dynamicpack.DynamicPackage;
import com.barcelo.businessrules.model.api.dynamicpack.HotelDistribution;
import com.barcelo.businessrules.model.api.dynamicpack.Traveller;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dag-vsf
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-config.xml")
@Slf4j
public class DtDetermineHotelMarkupTest {
	@Autowired
	private DecisionManager decisionManager;

	@Test
	public void test01() throws Exception {
		Assert.assertNotNull(this.decisionManager);

		DynamicPackage dynamicPackage = new DynamicPackage();
		dynamicPackage.setChannel("B2C");
		Traveller traveller = new Traveller();
		traveller.setDynamicPackage(dynamicPackage);
		traveller.setAge(30);
		HotelDistribution hotelDistribution = new HotelDistribution();
		hotelDistribution.setDynamicPackage(dynamicPackage);
		hotelDistribution.setSeason("BAJA");
		hotelDistribution.setDestinationGroup("ISLAS");
		hotelDistribution.setDaysInAdvance(10);

		log.info("Convirtiendo a lista de hechos");
		List<Object> factList = new ArrayList<Object>();
		factList.add(dynamicPackage);
		factList.add(traveller);
		factList.add(hotelDistribution);

		log.info("Creando la sesion");
		// KieSession kieSession = decisionManager.createKieSession();
		// kieSession.getAgenda().clear();
		KieSession kieSession = TestHelper.build(
				"com/barcelo/businessrules/pkg_dinamico/DT_DETERMINE_PAX_AGE_TYPE.gdst",
				"com/barcelo/businessrules/pkg_dinamico/DT_DETERMINE_HOTEL_MARKUP.gdst");

		log.info("Insertando hechos");
		for (Object fact : factList) {
			kieSession.insert(fact);
		}

		kieSession.getAgenda().getAgendaGroup("setup-mark2").setFocus();
		// kieSession.getAgenda().getAgendaGroup("setup").setFocus();

		log.info("Llamando a fireAllRules.");
		kieSession.fireAllRules();

		log.info("Eliminando la sesion.");
		kieSession.dispose();

		for (Object fact : factList) {
			log.info("Fact : {}", fact);
		}
	}
}
