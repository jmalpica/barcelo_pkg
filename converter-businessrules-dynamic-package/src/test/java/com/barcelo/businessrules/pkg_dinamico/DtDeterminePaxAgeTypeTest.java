package com.barcelo.businessrules.pkg_dinamico;

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
public class DtDeterminePaxAgeTypeTest {
	@Test
	public void test01() throws Exception {
		// Creando la clase de prueba
		TestHelper testHelper = new TestHelper("PkgDinamicoBV");

		// Configurando los includes
		testHelper.addImport(DynamicPackage.class);
		testHelper.addImport(Traveller.class);
		testHelper.addImport(ComponentDistribution.class);
		testHelper.addImport(HotelDistribution.class);
		testHelper.addImport(TransportDistribution.class);

		// Creando los hechos
		Traveller traveller1 = new Traveller();
		traveller1.setAge(0);
		testHelper.addFact(traveller1);

		// Configurando los AgendaGroup (en orden de ejecucion, no en pila)
		testHelper.addAgendaGroup("setup");

		// Configurando las reglas a testear
		testHelper.addGDST("DT/DT_DETERMINE_PAX_AGE_TYPE.gdst");

		// Ejecutando test
		testHelper.run();

		// Verificando resultados
		Assert.assertNotNull(traveller1.getAgeType());
		Assert.assertEquals("BABY", traveller1.getAgeType());
	}
}
