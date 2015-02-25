package com.barcelo.businessrules.dynamicpack.converter.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
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
import com.barcelo.integration.engine.model.api.request.BarMasterRQ;
import com.barcelo.integration.engine.model.api.request.pack.TOProductAvailabilityRQ;
import com.barcelo.integration.engine.model.api.request.pack.TOProductPreBookingRQ;
import com.barcelo.integration.engine.model.api.response.pack.TOProductAvailabilityRS;
import com.barcelo.integration.engine.model.api.response.pack.TOProductPreBookingRS;
import com.barcelo.integration.engine.model.api.shared.Price;
import com.barcelo.integration.engine.model.api.shared.Zone;
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
	/**
	 * Constant to make PMD happy. Notice that zero here is not a magic number,
	 * unless the frontier between positive and negative numbers can be configured in the universe somehow.
	 *
	 * Notice also, how replacing the 0 by a semantically meaningful constant here, the code becomes less
	 * legible instead of more.
	 */
	public static final int SMALLEST_POSITIVE = 0;
	private DynamicPackage dynamicPackage;
	private DateTime bookingDate;
	private String destinationGroup;

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
		extractBasicDynamicPackage(toProductAvailabilityRQ);
		extractDestination(toProductAvailabilityRQ.getDestinationZoneList());

		List<TravellerGroup> travellerGroupList = toProductAvailabilityRQ.getTravellerGroupList();
		extractTravellers(travellerGroupList);

		this.dynamicPackage.setComponentDistributionList(extractComponents(toProductAvailabilityRS.getProductList()));

		return this.dynamicPackage;
	}

	public DynamicPackage toModelInterface(TOProductPreBookingRQ toProductAvailabilityRQ,
										   TOProductPreBookingRS toProductAvailabilityRS) {
		extractBasicDynamicPackage(toProductAvailabilityRQ);

		List<TravellerGroup> travellerGroupList = toProductAvailabilityRQ.getBooking().getTravellerGroupList();
		extractTravellers(travellerGroupList);

		this.dynamicPackage.setComponentDistributionList(extractComponents(toProductAvailabilityRS.getBooking().getProductList()));

		return this.dynamicPackage;
	}

	private void extractBasicDynamicPackage(BarMasterRQ barMasterRQ) {
		if (this.dynamicPackage != null) {
			log.error(REENTRANCY_ERROR);
			throw new IllegalStateException("FactModelConverterImpl has been already used.");
		}

		bookingDate = DateTime.now();
		this.dynamicPackage = new DynamicPackage();
		List<String> brandList = barMasterRQ.getBrandList();
		if (brandList != null && brandList.size() > 0) {
			// TODO - dag-vsf - 29/01/2015 - Revisar modelo para poder guardar todas las marcas del paquete
			this.dynamicPackage.setBrand(brandList.get(0));
		}
		if (barMasterRQ.getPOS() != null && barMasterRQ.getPOS().getSource() != null) {
			Retail retail = barMasterRQ.getPOS().getSource().getRetail();
			if (retail != null) {
				this.dynamicPackage.setChannel(retail.getChannel());
				this.dynamicPackage.setSubChannel(retail.getSubChannel());
			}
			Wholesaler wholesaler = barMasterRQ.getPOS().getSource().getWholesaler();
			if (wholesaler != null) {
				this.dynamicPackage.setManagementGroup(wholesaler.getManagementGroup());
				this.dynamicPackage.setAgency(wholesaler.getAgency());
				this.dynamicPackage.setBranchOffice(wholesaler.getBranchOffice());
			}
		}
		this.dynamicPackage.setBookingDate(bookingDate.toDate());
		int bookingWeekday = calculateWeekday(this.dynamicPackage.getBookingDate());
		this.dynamicPackage.setBookingWeekday(bookingWeekday);
	}

	private void extractDestination(List<Zone> zones) {
		if (zones == null || zones.size() == 0) return;

		LinkedHashSet<String> rootZones = new LinkedHashSet<String>();

		rootZones.add(zones.get(0).getCode());

		for (Zone zone : zones) {
			if (rootZones.contains(zone.getCode())) {
				rootZones.remove(zone.getCode());
				for (Zone ancestor : zone.getAncestorList()) {
					rootZones.add(ancestor.getCode());
				}
			}
		}

		if (rootZones.size() > 0) {
			this.destinationGroup = rootZones.iterator().next();
		}
	}

	private void extractTravellers(List<TravellerGroup> travellerGroupList) {
		List<com.barcelo.businessrules.model.api.dynamicpack.Traveller> travellers =
				new ArrayList<com.barcelo.businessrules.model.api.dynamicpack.Traveller>();
		for (TravellerGroup travellerGroup : travellerGroupList) {
			List<Traveller> travellerList = travellerGroup.getTravellerList();
			for (Traveller travellerOrigin : travellerList) {
				com.barcelo.businessrules.model.api.dynamicpack.Traveller traveller =
						new com.barcelo.businessrules.model.api.dynamicpack.Traveller();
				traveller.setDynamicPackage(this.dynamicPackage);
				traveller.setAge(travellerOrigin.getAge());
				travellers.add(traveller);
			}
		}
		this.dynamicPackage.setTravellerList(travellers);
	}

	private List<ComponentDistribution> extractComponents(List<TOProduct> productList) {
		List<ComponentDistribution> result = new ArrayList<ComponentDistribution>();

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

		transportDistribution.setDestinationGroup(this.destinationGroup);

		/* Hay que acumular todas las compañias de todos los segmentos de todos los itinerarios,
		 * así que usaremos esta lista para hacerlo.
		 */
		transportDistribution.setCompanyList(new ArrayList<String>());

		// fijar el punto de partida para la acumulación de escalas
		transportDistribution.setSegmentCount(0);

		List<Itinerary> itineraryList = transport.getItineraryList();
		if (itineraryList != null) {
			for (Itinerary itinerary : itineraryList) {
				List<ItineraryOption> optionList = itinerary.getOptionList();
				if (optionList != null) {
					for (ItineraryOption itineraryOption : optionList) {
						processItineraryOption(result, transportDistribution, itineraryOption);
					}
				}
			}
		}
	}

	private static void processItineraryOption(List<ComponentDistribution> result, TransportDistribution transportDistribution, ItineraryOption itineraryOption) {
		TOPriceInformation priceInformation = itineraryOption.getPriceInformation();
		if (priceInformation != null) {
			if (transportDistribution.getPriceInformationRef() == null) {
				result.add(transportDistribution);
			}
			processPriceData(transportDistribution, priceInformation);

			if (itineraryOption.getProvider() != null) {
				// TODO - dag-vsf - 29/01/2015 - que hacer con los proveedores distintos por itinerario
				transportDistribution.setProvider(itineraryOption.getProvider().getProviderID());
			}
		}

		List<Segment> segmentList = itineraryOption.getSegmentList();
		if (segmentList != null) {
			// Acumulación de escalas
			int escalas = segmentList.size();
			if (escalas > SMALLEST_POSITIVE) {
				escalas--; // Las escalas son 1 menos que los segmentos
			}
			escalas += transportDistribution.getSegmentCount();
			transportDistribution.setSegmentCount(escalas);

			if (!segmentList.isEmpty()) {
				// Reune las compañias de este itinerario
				List<String> company = new ArrayList<String>();
				for (Segment segment : segmentList) {
					company.add(segment.getCompany());
				}
				// Y acumulalas al resto.
				transportDistribution.getCompanyList().addAll(company);
				// TODO - dag-vsf - 29/01/2015 - que hacer con las cabinas distintas por segmento?
				transportDistribution.setCabin(segmentList.get(0).getCabin());
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

							hotelDistribution.setDestinationGroup(this.destinationGroup);

							hotelDistribution.setChain(stay.getHotelChainID());
							hotelDistribution.setHotel(stay.getBhc());
							// TODO - dag-vsf - 30/01/2015 - Necesitamos confirmación de cual es el campo correspondiente del modelo
							hotelDistribution.setHotelType("PENDIENTE");
							hotelDistribution.setCategory(stay.getCategoryID());
							hotelDistribution.setMealPlan(stayOption.getOriginMealPlanID());
							hotelDistribution.setNightQuantity(stay.getNightQuantity());

							hotelDistribution.setRateType(stayOption.getContractID());

							processPriceData(hotelDistribution, priceInformation);

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

	private static void processPriceData(ComponentDistribution componentDistribution,
										 TOPriceInformation priceInformation) {
		if (componentDistribution.getPriceInformationRef() == null) {
			// Primer precio, lo tomamos como referencia a modificar
			componentDistribution.setPriceInformationRef(priceInformation);
			Price commissionableAmount = priceInformation.getCommissionableAmount();
			componentDistribution.setCommissionableAmount(commissionableAmount.getPrice());
			Price nonCommissionableAmount = priceInformation.getNonCommissionableAmount();
			componentDistribution.setNonCommissionableAmount(nonCommissionableAmount.getPrice());
			Price commissionAmount = priceInformation.getCommissionAmount();
			componentDistribution.setCommissionAmount(commissionAmount.getPrice());
			Price commissionTaxesAmount = priceInformation.getCommissionTaxesAmount();
			componentDistribution.setCommissionTaxesAmount(commissionTaxesAmount.getPrice());
			BigDecimal commissionTaxPercentage = priceInformation.getCommissionTaxPercentage();
			componentDistribution.setTaxRate(commissionTaxPercentage);

			// This should not be necessary. Some rule is broken.
			componentDistribution.setTotalAmount(BigDecimal.ZERO);
		} else {
			// Precios adicionales, acumularlos
			BigDecimal commissionableAmount = componentDistribution.getCommissionableAmount();
			commissionableAmount = commissionableAmount.add(
					priceInformation.getCommissionableAmount().getPrice());
			componentDistribution.setCommissionableAmount(commissionableAmount);
			BigDecimal nonCommissionableAmount = componentDistribution.getNonCommissionableAmount();
			nonCommissionableAmount = nonCommissionableAmount.add(
					priceInformation.getNonCommissionableAmount().getPrice());
			componentDistribution.setNonCommissionableAmount(nonCommissionableAmount);
			BigDecimal commissionAmount = componentDistribution.getCommissionAmount();
			commissionAmount = commissionAmount.add(
					priceInformation.getCommissionAmount().getPrice());
			componentDistribution.setCommissionAmount(commissionAmount);
			BigDecimal commissionTaxesAmount = componentDistribution.getCommissionTaxesAmount();
			commissionTaxesAmount = commissionTaxesAmount.add(
					priceInformation.getCommissionTaxesAmount().getPrice());
			componentDistribution.setCommissionTaxesAmount(commissionTaxesAmount);
		}
	}

	private final Calendar weekdayCalendar = Calendar.getInstance();
	private int calculateWeekday(Date date) {
		weekdayCalendar.setTime(date);
		return weekdayCalendar.get(Calendar.DAY_OF_WEEK);
	}

	private static int daysBetween(Date startDate, Date endDate) {
		LocalDate departureLocalDate = new LocalDate(startDate.getTime());
		LocalDate arrivalLocalDate = new LocalDate(endDate.getTime());
		return Days.daysBetween(departureLocalDate, arrivalLocalDate).getDays();
	}
}
