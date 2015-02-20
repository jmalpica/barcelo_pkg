package com.barcelo.businessrules.pkg_dinamico;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import com.barcelo.businessrules.model.api.dynamicpack.ComponentDistribution;
import com.barcelo.businessrules.model.api.dynamicpack.DynamicPackage;
import com.barcelo.businessrules.model.api.dynamicpack.HotelDistribution;
import com.barcelo.businessrules.model.api.dynamicpack.TransportDistribution;
import com.barcelo.businessrules.model.api.dynamicpack.Traveller;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dag-vsf
 */
@Slf4j
public class DtDetermineHotelMarkupTest {
	@Test
	public void test01() {
		// Creando la clase de prueba
		TestHelper testHelper = new TestHelper("PkgDinamicoBV");

		// Configurando los includes
		testHelper.addImport(DynamicPackage.class);
		testHelper.addImport(Traveller.class);
		testHelper.addImport(ComponentDistribution.class);
		testHelper.addImport(HotelDistribution.class);
		testHelper.addImport(TransportDistribution.class);

		// Creando los hechos
		DynamicPackage dynamicPackage = new DynamicPackage();
		dynamicPackage.setChannel("B2C");
		testHelper.addFact(dynamicPackage);

		Traveller traveller = new Traveller();
		traveller.setDynamicPackage(dynamicPackage);
		traveller.setAge(30);
		testHelper.addFact(traveller);

		HotelDistribution hotelDistribution = new HotelDistribution();
		hotelDistribution.setDynamicPackage(dynamicPackage);
		hotelDistribution.setSeason("BAJA");
		hotelDistribution.setDestinationGroup("ISLAS");
		hotelDistribution.setDaysInAdvance(10);
		testHelper.addFact(hotelDistribution);

		// Creando la referencia desde el DynamicPackage a los traveller y componentDistribution (opcional)
		ArrayList<Traveller> travellerList = new ArrayList<Traveller>();
		travellerList.add(traveller);
		dynamicPackage.setTravellerList(travellerList);
		ArrayList<ComponentDistribution> componentDistributionList = new ArrayList<ComponentDistribution>();
		componentDistributionList.add(hotelDistribution);
		dynamicPackage.setComponentDistributionList(componentDistributionList);

		// Configurando los AgendaGroup (en orden de ejecucion, no en pila)
		testHelper.addAgendaGroup("setup");
		testHelper.addAgendaGroup("commission_and_markup");

		// Configurando las reglas a testear
		testHelper.addGDST("DT/DT_DETERMINE_PAX_AGE_TYPE.gdst");
		testHelper.addGDST("DT/DT_DETERMINE_HOTEL_MARKUP.gdst");

		// Ejecutando test
		testHelper.run();

		// Verificando resultados
		Assert.assertNotNull(traveller.getAgeType());
		Assert.assertEquals("ADULT", traveller.getAgeType());
		Assert.assertNotNull(hotelDistribution.getProfitabilityRate());
	}
}
