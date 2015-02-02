package com.barcelo.businessrules.dynamicpack.converter.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.barcelo.businessrules.dynamicpack.converter.FactModelConverterInterface;
import com.barcelo.businessrules.model.api.dynamicpack.ComponentDistribution;
import com.barcelo.businessrules.model.api.dynamicpack.DynamicPackage;
import com.barcelo.businessrules.model.api.dynamicpack.HotelDistribution;
import com.barcelo.businessrules.model.api.dynamicpack.TransportDistribution;
import com.barcelo.integration.engine.model.api.request.pack.TOProductAvailabilityRQ;
import com.barcelo.integration.engine.model.api.response.pack.TOProductAvailabilityRS;
import com.barcelo.integration.engine.model.api.shared.Price;
import com.barcelo.integration.engine.model.api.shared.auth.Retail;
import com.barcelo.integration.engine.model.api.shared.auth.Wholesaler;
import com.barcelo.integration.engine.model.api.shared.pack.*;
import com.barcelo.integration.engine.model.api.shared.traveller.Traveller;
import com.barcelo.integration.engine.model.api.shared.traveller.TravellerGroup;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dag-vsf
 */
@Service(FactModelConverterInterface.SERVICENAME)
@Scope("prototype")
@Slf4j
public class FactModelConverterImpl implements FactModelConverterInterface {
	private DynamicPackage dynamicPackage;
	private DateTime bookingDate;

	private static final String REENTRANCY_ERROR = new StringBuilder()
			.append("Class misuse: This class")
			.append(" FactModelConverterImpl")
			.append(" is not meant to be re-entrant, each invocation to")
			.append(" toModelInterface requires a new instance of the class. If you are using Spring")
			.append(" to provide you the instance you are calling, ensure it is configured properly")
			.append(" as a prototype, and not as a singleton. And ask Spring for a new instance for")
			.append(" each invocation.").toString();

	public DynamicPackage toModelInterface(TOProductAvailabilityRQ toProductAvailabilityRQ,
										   TOProductAvailabilityRS toProductAvailabilityRS) {
		if (this.dynamicPackage != null) {
			log.error(REENTRANCY_ERROR);
			throw new IllegalStateException("FactModelConverterImpl has been already used.");
		}

		bookingDate = DateTime.now();
		this.dynamicPackage = new DynamicPackage();
		List<String> brandList = toProductAvailabilityRQ.getBrandList();
		if (brandList != null && brandList.size() > 0) {
			// TODO - dag-vsf - 29/01/2015 - Revisar modelo para poder guardar todas las marcas del paquete
			this.dynamicPackage.setBrand(brandList.get(0));
		}
		if (toProductAvailabilityRQ.getPOS() != null && toProductAvailabilityRQ.getPOS().getSource() != null) {
			Retail retail = toProductAvailabilityRQ.getPOS().getSource().getRetail();
			if (retail != null) {
				this.dynamicPackage.setChannel(retail.getChannel());
				this.dynamicPackage.setSubChannel(retail.getSubChannel());
			}
			Wholesaler wholesaler = toProductAvailabilityRQ.getPOS().getSource().getWholesaler();
			if (wholesaler != null) {
				this.dynamicPackage.setManagementGroup(wholesaler.getManagementGroup());
				this.dynamicPackage.setAgency(wholesaler.getAgency());
				this.dynamicPackage.setBranchOffice(wholesaler.getBranchOffice());
			}
		}
		this.dynamicPackage.setBookingDate(bookingDate.toDate());
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(this.dynamicPackage.getBookingDate());
		int bookingWeekday = calendar.get(Calendar.DAY_OF_WEEK);
		this.dynamicPackage.setBookingWeekday(bookingWeekday);

		List<com.barcelo.businessrules.model.api.dynamicpack.Traveller> travellers =
				new ArrayList<com.barcelo.businessrules.model.api.dynamicpack.Traveller> ();
		List<TravellerGroup> travellerGroupList = toProductAvailabilityRQ.getTravellerGroupList();
		for (TravellerGroup travellerGroup : travellerGroupList) {
			List<Traveller> travellerList = travellerGroup.getTravellerList();
			for (Traveller travellerOrigin : travellerList) {
				com.barcelo.businessrules.model.api.dynamicpack.Traveller traveller =
						new com.barcelo.businessrules.model.api.dynamicpack.Traveller();
				traveller.setAge(travellerOrigin.getAge());
				travellers.add(traveller);
			}
		}
		this.dynamicPackage.setTravellerList(travellers);

		this.dynamicPackage.setComponentDistributionList(extractComponents(toProductAvailabilityRS));

		return this.dynamicPackage;
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
				} else if (toProduct instanceof Stay) {
					flattenStay(result, (Stay) toProduct);
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
								} else if (toProduct1 instanceof Stay) {
									flattenStay(result, (Stay) toProduct1);
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

		transportDistribution.setDynamicPackage(this.dynamicPackage);

		Date departureDate = transport.getDepartureDateTime();
		transportDistribution.setStartDateTime(departureDate);
		Date arrivalDate = transport.getArrivalDateTime();
		transportDistribution.setEndDateTime(arrivalDate);

		transportDistribution.setStartWeekday(calculateWeekday(departureDate));
		transportDistribution.setEndWeekday(calculateWeekday(arrivalDate));

		/* Calculo de los días de estancia (aunque es un transporte). Usando JodaTime, no se usa withTimeAtStartOfDay
		 * porque falla en zonas horarias en las que la hora que se añade o elimina es la primera del día */
		transportDistribution.setStayQuantity(daysBetween(departureDate, arrivalDate));

		transportDistribution.setDaysInAdvance(daysBetween(bookingDate.toDate(), departureDate));

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
								Price commissionableAmount = priceInformation.getCommissionableAmount();
								transportDistribution.setCommissionableAmount(commissionableAmount.getPrice());
								Price nonCommissionableAmount = priceInformation.getNonCommissionableAmount();
								transportDistribution.setNonCommissionableAmount(nonCommissionableAmount.getPrice());
								Price commissionAmount = priceInformation.getCommissionAmount();
								transportDistribution.setCommissionAmount(commissionAmount.getPrice());
								Price commissionTaxesAmount = priceInformation.getCommissionTaxesAmount();
								transportDistribution.setTaxAmount(commissionTaxesAmount.getPrice());

								result.add(transportDistribution);
							} else {
								// Precios adicionales, acumularlos
								BigDecimal commissionableAmount = transportDistribution.getCommissionableAmount();
								commissionableAmount = commissionableAmount.add(
										priceInformation.getCommissionableAmount().getPrice());
								transportDistribution.setCommissionableAmount(commissionableAmount);
								BigDecimal nonCommissionableAmount = transportDistribution.getNonCommissionableAmount();
								nonCommissionableAmount = nonCommissionableAmount.add(
										priceInformation.getNonCommissionableAmount().getPrice());
								transportDistribution.setNonCommissionableAmount(nonCommissionableAmount);
								BigDecimal commissionAmount = transportDistribution.getCommissionAmount();
								commissionAmount = commissionAmount.add(
										priceInformation.getCommissionAmount().getPrice());
								transportDistribution.setCommissionAmount(commissionAmount);
								BigDecimal commissionTaxesAmount = transportDistribution.getTaxAmount();
								commissionTaxesAmount = commissionTaxesAmount.add(
										priceInformation.getCommissionTaxesAmount().getPrice());
								transportDistribution.setTaxAmount(commissionTaxesAmount);
							}

							if (itineraryOption.getProvider() != null) {
								// TODO - dag-vsf - 29/01/2015 - que hacer con los proveedores distintos por itinerario
								transportDistribution.setProvider(itineraryOption.getProvider().getProviderID());
							}
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

	private void flattenStay(List<ComponentDistribution> result, Stay stay) {
		Date arrivalDate = stay.getArrivalDateTime();
		Date departureDate = stay.getDepartureDateTime();
		int startWeekday = calculateWeekday(arrivalDate);
		int endWeekday = calculateWeekday(departureDate);
		int daysInAdvance = daysBetween(bookingDate.toDate(), arrivalDate);

		List<StayMealPlan> stayMealPlanList = stay.getMealPlanList();
		if (stayMealPlanList != null) {
			for (StayMealPlan stayMealPlan : stayMealPlanList) {
				List<StayOption> optionList = stayMealPlan.getOptionList();
				if (optionList != null) {
					for (StayOption stayOption : optionList) {
						TOPriceInformation priceInformation = stayOption.getPriceInformation();
						if (priceInformation != null) {
							HotelDistribution hotelDistribution = new HotelDistribution();

							hotelDistribution.setDynamicPackage(this.dynamicPackage);

							hotelDistribution.setStartDateTime(arrivalDate);
							hotelDistribution.setEndDateTime(departureDate);

							hotelDistribution.setStartWeekday(startWeekday);
							hotelDistribution.setEndWeekday(endWeekday);

							hotelDistribution.setDaysInAdvance(daysInAdvance);

							hotelDistribution.setChain(stay.getHotelChainID());
							hotelDistribution.setHotel(stay.getBhc());
							// TODO - dag-vsf - 30/01/2015 - Necesitamos confirmación de cual es el campo correspondiente del modelo
							hotelDistribution.setHotelType("PENDIENTE");
							hotelDistribution.setCategory(stay.getCategoryID());
							hotelDistribution.setAccommodationType(stayOption.getOriginMealPlanID());
							hotelDistribution.setNightQuantity(stay.getNightQuantity());

							hotelDistribution.setRateType(stayOption.getContractID());

							if (hotelDistribution.getPriceInformationRef() == null) {
								// Primer precio, lo tomamos como referencia a modificar
								hotelDistribution.setPriceInformationRef(priceInformation);
								Price commissionableAmount = priceInformation.getCommissionableAmount();
								hotelDistribution.setCommissionableAmount(commissionableAmount.getPrice());
								Price nonCommissionableAmount = priceInformation.getNonCommissionableAmount();
								hotelDistribution.setNonCommissionableAmount(nonCommissionableAmount.getPrice());
								Price commissionAmount = priceInformation.getCommissionAmount();
								hotelDistribution.setCommissionAmount(commissionAmount.getPrice());
								Price commissionTaxesAmount = priceInformation.getCommissionTaxesAmount();
								hotelDistribution.setTaxAmount(commissionTaxesAmount.getPrice());
							} else {
								// Precios adicionales, acumularlos
								BigDecimal commissionableAmount = hotelDistribution.getCommissionableAmount();
								commissionableAmount = commissionableAmount.add(
										priceInformation.getCommissionableAmount().getPrice());
								hotelDistribution.setCommissionableAmount(commissionableAmount);
								BigDecimal nonCommissionableAmount = hotelDistribution.getNonCommissionableAmount();
								nonCommissionableAmount = nonCommissionableAmount.add(
										priceInformation.getNonCommissionableAmount().getPrice());
								hotelDistribution.setNonCommissionableAmount(nonCommissionableAmount);
								BigDecimal commissionAmount = hotelDistribution.getCommissionAmount();
								commissionAmount = commissionAmount.add(
										priceInformation.getCommissionAmount().getPrice());
								hotelDistribution.setCommissionAmount(commissionAmount);
								BigDecimal commissionTaxesAmount = hotelDistribution.getTaxAmount();
								commissionTaxesAmount = commissionTaxesAmount.add(
										priceInformation.getCommissionTaxesAmount().getPrice());
								hotelDistribution.setTaxAmount(commissionTaxesAmount);
							}

							if (stayOption.getProvider() != null) {
								// TODO - dag-vsf - 29/01/2015 - que hacer con los proveedores distintos por itinerario
								hotelDistribution.setProvider(stayOption.getProvider().getProviderID());
							}

							result.add(hotelDistribution);
						}
					}
				}
			}
		}
	}

	private final Calendar weekdayCalendar = Calendar.getInstance();
	private int calculateWeekday(Date date) {
		weekdayCalendar.setTime(date);
		return weekdayCalendar.get(Calendar.DAY_OF_WEEK);
	}

	private int daysBetween(Date startDate, Date endDate) {
		LocalDate departureLocalDate = new LocalDate(startDate.getTime());
		LocalDate arrivalLocalDate = new LocalDate(endDate.getTime());
		return Days.daysBetween(departureLocalDate, arrivalLocalDate).getDays();
	}
}
