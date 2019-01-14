package it.univaq.disim.se4as.thermostat.analyzer;

import java.util.ArrayList;
import java.util.List;

import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager;
import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager.Interval;
import it.univaq.disim.se4as.thermostat.SQLManager.model.SensedValue;

public class CheckingThread extends Thread {

	private Double living_area_temperature_threshold;
	private Double sleeping_area_temperature_threshold;
	private Double toilets_temperature_threshold;

	private Double air_quality_threshold = 1.0;

	private SQLManager sqlManager;

	public CheckingThread(SQLManager manager, Double living_area_temperature_threshold,
			Double sleeping_area_temperature_thershold, Double toiets_temperature_threshold) {
		this.living_area_temperature_threshold = living_area_temperature_threshold;
		this.sleeping_area_temperature_threshold = sleeping_area_temperature_thershold;
		this.toilets_temperature_threshold = toiets_temperature_threshold;
		this.sqlManager = manager;
	}

	private List<Alert> checkThresholds(List<String> rooms, List<String> sensor_types) {

		List<Alert> alerts = new ArrayList<Alert>();

		for (String room : rooms) {

			Alert alert = new Alert();

			List<SensedValue> qualityValues = sqlManager.getSensedData(room, "airQuality", Interval.LAST);
			if (qualityValues.get(0).getValue() < air_quality_threshold) {
				alert.setAirQualityAlert(true);
			}
			
			List<SensedValue> presenceValues = sqlManager.getSensedData(room, "presence", Interval.LAST);
			if (presenceValues.get(0).getValue() > 0) {
				alert.setPresence(true);
			}

			if (room.contains("livingRoom")) {

				List<SensedValue> values = sqlManager.getSensedData(room, "temperature", Interval.LAST);

				if (values.get(0).getValue() < living_area_temperature_threshold) {
					alert.setTemperatureAlert(true);
					System.out.println("Analyzer - living_area_temperature_threshold excedeed");
				}
			}

			if (room.contains("camera")) {
				List<SensedValue> values = sqlManager.getSensedData(room, "temperature", Interval.LAST);

				if (values.get(0).getValue() < sleeping_area_temperature_threshold) {
					alert.setTemperatureAlert(true);
					System.out.println("Analyzer - living_area_temperature_threshold excedeed");
				}
			}

			if (room.contains("kitchen")) {
				List<SensedValue> values = sqlManager.getSensedData(room, "temperature", Interval.LAST);

				if (values.get(0).getValue() < living_area_temperature_threshold) {
					alert.setTemperatureAlert(true);
					System.out.println("Analyzer - living_area_temperature_threshold excedeed");
				}
			}
			
			if (room.contains("bathroom")) {
				List<SensedValue> values = sqlManager.getSensedData(room, "temperature", Interval.LAST);

				if (values.get(0).getValue() < toilets_temperature_threshold) {
					alert.setTemperatureAlert(true);
					System.out.println("Analyzer - living_area_temperature_threshold excedeed");
				}
			}

			alerts.add(alert);
		}
		
		return alerts;
	}

	private List<Trend> checkTrends(List<String> rooms, List<String> sensor_types) {

		List<Trend> trends = new ArrayList<Trend>();

		for (String room : rooms) {

			List<SensedValue> values = sqlManager.getSensedData(room, "temperature", Interval.ALL);

			Trend trend = new Trend();
			trend.setRoom(room);

			double slope = 0;
	      
			int number_of_sample = 10;
			
			double c = 0;
			int a = 0;
			double b = 0;
			double d = 0;
			for (int i = 0; i < number_of_sample; i++) {
				
				c += i* values.get(i).getValue();
				a++;
				b += values.get(i).getValue();
				d += values.get(i).getValue()*values.get(i).getValue();
			}
			
			double result = ((number_of_sample * c) - (a * b)) / ((number_of_sample * d) - Math.sqrt(a));
	        
	        //trend.setSlope(slope);
	        System.out.println("slope" + result);
			trends.add(trend);
		}
		
		return trends;
	}
	
	@Override
	public void run() {

		try {

			while (!Thread.interrupted()) {
				while (true) {
					List<String> rooms = sqlManager.getRooms();
					List<String> sensor_types = sqlManager.getSensorTypes();

					// thresholds checking
					//List<Alert> alerts = checkThresholds(rooms, sensor_types);
					
					// temperature trend
					List<Trend> trends = checkTrends(rooms, sensor_types);

					Thread.sleep(10000);
				}

			}
			return;

		} catch (InterruptedException e1) {

			System.out.println("Analyzer stopped");
		}
	}

}
