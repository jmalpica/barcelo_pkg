package com.barcelo.businessrules.dynamicpack.converter.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.barcelo.businessrules.dynamicpack.converter.FactModelConverterInterface;
import com.barcelo.businessrules.model.api.dynamicpack.ComponentDistribution;
import com.barcelo.businessrules.model.api.dynamicpack.DynamicPackage;
import com.barcelo.businessrules.model.api.dynamicpack.TransportDistribution;
import com.barcelo.integration.engine.model.api.request.pack.TOProductAvailabilityRQ;
import com.barcelo.integration.engine.model.api.response.pack.TOProductAvailabilityRS;
import com.barcelo.integration.engine.model.api.shared.auth.Retail;
import com.barcelo.integration.engine.model.api.shared.auth.Wholesaler;
import com.barcelo.integration.engine.model.api.shared.pack.*;

/**
 * @author dag-vsf
 */
@Service(FactModelConverterInterface.SERVICENAME)
@Scope("prototype")
public class FactModelConverterImpl implements FactModelConverterInterface {
	public DynamicPackage toModelInterface(TOProductAvailabilityRQ toProductAvailabilityRQ,
										   TOProductAvailabilityRS toProductAvailabilityRS) {
		DynamicPackage result = new DynamicPackage();
		List<String> brandList = toProductAvailabilityRQ.getBrandList();
		if (brandList != null && brandList.size() > 0) {
			// TODO - dag-vsf - 29/01/2015 - Revisar modelo para poder guardar todas las marcas del paquete
			result.setBrand(brandList.get(0));
		}
		if (toProductAvailabilityRQ.getPOS() != null && toProductAvailabilityRQ.getPOS().getSource() != null) {
			Retail retail = toProductAvailabilityRQ.getPOS().getSource().getRetail();
			if (retail != null) {
				result.setChannel(retail.getChannel());
				result.setSubChannel(retail.getSubChannel());
			}
			Wholesaler wholesaler = toProductAvailabilityRQ.getPOS().getSource().getWholesaler();
			if (wholesaler != null) {
				result.setManagementGroup(wholesaler.getManagementGroup());
				result.setAgency(wholesaler.getAgency());
				result.setBranchOffice(wholesaler.getBranchOffice());
			}
		}
		result.setBookingDate(toProductAvailabilityRQ.getFromDate());
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(result.getBookingDate());
		int bookingWeekday = calendar.get(Calendar.DAY_OF_WEEK);
		result.setBookingWeekday(bookingWeekday);

		result.setComponentDistributionList(extractComponents(toProductAvailabilityRS));

		return result;
	}

	private List<ComponentDistribution> extractComponents(TOProductAvailabilityRS toProductAvailabilityRS) {
		List<ComponentDistribution> result = new ArrayList<ComponentDistribution>();

		List<TOProduct> productList = toProductAvailabilityRS.getProductList();
		if (productList != null) {
			for (TOProduct toProduct : productList) {
				if (toProduct instanceof TOPackage) {
					TOPackage toPackage = (TOPackage) toProduct;
					flattenTOPackage(result, toPackage);
				} else if (toProduct instanceof Transport) {
					flattenTransport(result, (Transport) toProduct);
				}

			}
		}

		return result;
	}
	private void flattenTOPackage(List<ComponentDistribution> result, TOPackage toPackage) {
		List<PackageOption> optionList = toPackage.getOptionList();
		if (optionList != null) {
			for (PackageOption packageOption : optionList) {
				List<Component> componentList = packageOption.getComponentList();
				if (componentList != null) {
					for (Component component : componentList) {
						List<TOProduct> toProductList = component.getToProductList();
						if (toProductList != null) {
							for (TOProduct toProduct1 : toProductList) {
								if (toProduct1 instanceof Transport) {
									flattenTransport(result, (Transport) toProduct1);
								}
							}
						}
					}
				}
			}
		}
	}

	private void flattenTransport(List<ComponentDistribution> result, Transport transport) {
		TransportDistribution transportDistribution = new TransportDistribution();

		Date departureDate = transport.getDepartureDateTime();
		transportDistribution.setStartDateTime(departureDate);
		Date arrivalDate = transport.getArrivalDateTime();
		transportDistribution.setEndDateTime(arrivalDate);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(departureDate);
		transportDistribution.setStartWeekday(calendar.get(Calendar.DAY_OF_WEEK));
		calendar.setTime(arrivalDate);
		transportDistribution.setEndWeekday(calendar.get(Calendar.DAY_OF_WEEK));

		/* Calculo de los días de estancia (aunque es un transporte). Usando JodaTime, no se usa withTimeAtStartOfDay
		 * porque falla en zonas horarias en las que la hora que se añade o elimina es la primera del día */
		LocalDate departureLocalDate = new LocalDate(departureDate.getTime());
		LocalDate arrivalLocalDate = new LocalDate(arrivalDate.getTime());
		transportDistribution.setStayQuantity(Days.daysBetween(departureLocalDate, arrivalLocalDate).getDays());

		/* Hay que acumular todas las compañias de todos los segmentos de todos los itinerarios,
		 * así que usaremos esta lista para hacerlo.
		 */
		transportDistribution.setCompany(new ArrayList<String>());

		// fijar el punto de partida para la acumulación de escalas
		transportDistribution.setSegmentCount(0);

		List<Itinerary> itineraryList = transport.getItineraryList();
		if (itineraryList != null) {
			for (Itinerary itinerary : itineraryList) {
				List<ItineraryOption> optionList = itinerary.getOptionList();
				if (optionList != null) {
					for (ItineraryOption itineraryOption : optionList) {
						TOPriceInformation priceInformation = itineraryOption.getPriceInformation();
						if (priceInformation != null) {
							if (transportDistribution.getPriceInformationRef() == null) {
								// Primer precio, lo tomamos como referencia a modificar
								transportDistribution.setPriceInformationRef(priceInformation);
							} else {
								// precios adicionales, aún pendiente de definir que hacer con ellos
							}

							if (itineraryOption.getProvider() != null) {
								// TODO - dag-vsf - 29/01/2015 - que hacer con los proveedores distintos por itinerario
								transportDistribution.setProvider(itineraryOption.getProvider().getProviderID());
							}

							result.add(transportDistribution);
						}

						List<Segment> segmentList = itineraryOption.getSegmentList();
						if (segmentList != null) {
							// Acumulación de escalas
							int escalas = segmentList.size();
							if (escalas > 0) escalas--; // Las escalas son 1 menos que los segmentos
							escalas += transportDistribution.getSegmentCount();
							transportDistribution.setSegmentCount(escalas);

							if (!segmentList.isEmpty()) {
								// Reune las compañias de este itinerario
								List<String> company = new ArrayList<String>();
								for (Segment segment : segmentList) {
									company.add(segment.getCompany());
								}
								// Y acumulalas al resto.
								transportDistribution.getCompany().addAll(company);
								// TODO - dag-vsf - 29/01/2015 - que hacer con las cabinas distintas por segmento?
								transportDistribution.setCabin(segmentList.get(0).getCabin());
							}
						}
					}
				}
			}
		}
	}
}
