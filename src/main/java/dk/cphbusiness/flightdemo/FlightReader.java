package dk.cphbusiness.flightdemo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dk.cphbusiness.utils.Utils;
import lombok.*;

import javax.crypto.spec.PSource;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Purpose:
 *
 * @author: Thomas Hartmann
 */
public class FlightReader {

    public static void main(String[] args) {
        FlightReader flightReader = new FlightReader();
        try {
            List<DTOs.FlightDTO> flightList = flightReader.getFlightsFromFile("flights.json");
            List<DTOs.FlightInfo> flightInfoList = flightReader.getFlightInfoDetails(flightList);


            flightReader.flightsArrInFrankfurt(flightList);
            List<DTOs.AirportTime> airportTimes = flightReader.getAirportTimesFromFile("flights.json");
           // flightReader.totalFlightTimeForSpecificAirline2(flightList);
            flightReader.flightsSortedByTimezone(flightList,"Europe/Moscow");
           /* flightInfoList.forEach(f->{
                System.out.println("\n"+f);
            });

             */


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    public List<FlightDTO> jsonFromFile(String fileName) throws IOException {
//        List<FlightDTO> flights = getObjectMapper().readValue(Paths.get(fileName).toFile(), List.class);
//        return flights;
//    }

    //find alle flights der har arrival airport i Frankfurt International Airport

    public  List<DTOs.FlightDTO> flightsArrInFrankfurt(List<DTOs.FlightDTO> flightList) {
        List<DTOs.FlightDTO> flightsArrInFranfurt = flightList.stream()
                .filter(flightDTO -> flightDTO.getArrival() !=null && ("Frankfurt International Airport").equals((flightDTO.getArrival().getAirport())))
                .collect(Collectors.toList());
        //flightsArrInFranfurt.forEach(System.out::println);
        flightsArrInFranfurt.forEach(flightDTO -> System.out.println(flightDTO));
        return flightsArrInFranfurt;

                }



    public List<DTOs.FlightInfo> getFlightInfoDetails(List<DTOs.FlightDTO> flightList) {
        List<DTOs.FlightInfo> flightInfoList = flightList.stream()
                .map(flight -> {
            Duration duration = Duration.between(flight.getDeparture().getScheduled(), flight.getArrival().getScheduled());
            DTOs.FlightInfo flightInfo = DTOs.FlightInfo.builder()
                    .name(flight.getFlight().getNumber())
                    .iata(flight.getFlight().getIata())
                    .airline(flight.getAirline().getName())
                    .duration(duration)
                    .departure(flight.getDeparture().getScheduled().toLocalDateTime())
                    .arrival(flight.getArrival().getScheduled().toLocalDateTime())
                    .origin(flight.getDeparture().getAirport())
                    .destination(flight.getArrival().getAirport())
                    .build();

            return flightInfo;
        }).toList();
        return flightInfoList;
    }

    public List<DTOs.FlightDTO> getFlightsFromFile(String filename) throws IOException {
        DTOs.FlightDTO[] flights = new Utils().getObjectMapper().readValue(Paths.get(filename).toFile(), DTOs.FlightDTO[].class);

        List<DTOs.FlightDTO> flightList = Arrays.stream(flights).toList();
        return flightList;
    }
    // calculate the average flight time for a specific airline
    // calculate the average flight time for all flights operated by Lufthansa
    public Map<String, Double> averageDurationPerAirline(List<DTOs.FlightDTO> flightlist){

        Map<String, Double> averageCalcForFlight = flightlist.stream()
                .collect(Collectors.groupingBy(
                        flightDTO -> String.valueOf(flightDTO.getAirline()),
                        Collectors.averagingDouble(flight -> {
                            Duration duration = Duration.between(flight.getDeparture().getScheduled(), flight.getArrival().getScheduled());
                            return duration.toMinutes();
                        })
                ));

        averageCalcForFlight.forEach((airline,averagecalcTime) -> System.out.println("Airline: "+airline+ ",  Average time per flight: " +averagecalcTime));
        return averageCalcForFlight;
    }


public Map<DTOs.AirlineDTO, Double> totalFlightTimeForSpecificAirline2(List<DTOs.FlightDTO> flightList) {
    Map<DTOs.AirlineDTO, Double> totalAirtime = flightList.stream()
            .filter(flightDTO -> flightDTO.getAirline().equals("Lufthansa"))  // Filter for Lufthansa
            .collect(Collectors.groupingBy(
                    flightDTO -> flightDTO.getAirline(),  // Group by Airline name
                    Collectors.summingDouble(flight -> {
                        Duration duration = Duration.between(flight.getDeparture().getScheduled(), flight.getArrival().getScheduled());
                        return duration.toMinutes();  // Convert duration to minutes
                    })
            ));
    totalAirtime.forEach((airline,totalTime)-> System.out.println("Airline: "+airline+", total airtime: " +totalTime));
    return totalAirtime;
}

    public Map<DTOs.AirlineDTO, Double> totalFlightTimeForSpecificAirline2(List<DTOs.FlightDTO> flightList) {
        Map<DTOs.AirlineDTO, Double> totalAirtime = flightList.stream()
                .filter(flightDTO -> flightDTO.getAirline() != null && "Lufthansa".equals(flightDTO.getAirline().getName()))  // Filter for Lufthansa
                .collect(Collectors.groupingBy(
                        flightDTO -> flightDTO.getAirline(),  // Group by Airline name
                        Collectors.summingDouble(flight -> {
                            Duration duration = Duration.between(flight.getDeparture().getScheduled(), flight.getArrival().getScheduled());
                            return duration.toMinutes();  // Convert duration to minutes
                        })
                ));
        totalAirtime.forEach((airline,totalTime)-> System.out.println("Airline: "+airline+", total airtime: " +totalTime));
        return totalAirtime;
    }

    public void flightsSortedByTimezone(List<DTOs.FlightDTO> airportTimes,String timeZone){

        Map<String,List<DTOs.FlightDTO>> flightsByTimeZone = airportTimes.stream()
                .filter(airportTime -> airportTime.getDeparture().getTimezone() != null && timeZone.contentEquals(airportTime.getDeparture().getTimezone()))
                .collect(Collectors.groupingBy(airportTime -> airportTime.getDeparture().getAirport())
                );


        flightsByTimeZone.forEach((timezone,airport) -> System.out.println("\n" + "Timezone: " + timezone + ", airport: " + airport+"\n"));
    }
}


