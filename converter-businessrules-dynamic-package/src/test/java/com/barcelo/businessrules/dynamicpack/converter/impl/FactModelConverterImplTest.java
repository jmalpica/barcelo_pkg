package com.barcelo.businessrules.dynamicpack.converter.impl;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import static org.joda.time.Days.daysBetween;
import static org.junit.Assert.assertEquals;

/**
 * @author dag-vsf
 */
public class FactModelConverterImplTest {
	/**
	 * From http://stackoverflow.com/questions/3802893/number-of-days-between-two-dates-in-joda-time
	 * @throws Exception
	 */
	@org.junit.Test
	public void testExampleDayDifference() throws Exception {
		// 5am on the 20th to 1pm on the 21st, October 2013, Brazil
		DateTimeZone BRAZIL = DateTimeZone.forID("America/Sao_Paulo");
		DateTime start = new DateTime(2013, 10, 20, 5, 0, 0, BRAZIL);
		DateTime end = new DateTime(2013, 10, 21, 13, 0, 0, BRAZIL);

		assertEquals(0, daysBetween(start.withTimeAtStartOfDay(), end.withTimeAtStartOfDay()).getDays());

		assertEquals(1, daysBetween(start.toLocalDate(), end.toLocalDate()).getDays());
	}
}
