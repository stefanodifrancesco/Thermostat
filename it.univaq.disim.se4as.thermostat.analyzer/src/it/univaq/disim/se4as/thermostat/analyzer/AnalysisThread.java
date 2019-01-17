package it.univaq.disim.se4as.thermostat.analyzer;

import java.util.ArrayList;
import java.util.List;

import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager;
import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager.Interval;
import it.univaq.disim.se4as.thermostat.SQLManager.model.SensedValue;
import it.univaq.disim.se4as.thermostat.planner.Planner;

public class AnalysisThread extends Thread {

	/*private Double living_area_temperature_threshold;
	private Double sleeping_area_temperature_threshold;
	private Double toilets_temperature_threshold;

	private Double air_quality_threshold = 1.0;*/

	private SQLManager sqlManager;
	private Planner planner;

	/*public AnalysisThread(SQLManager manager, Planner planner, Double living_area_temperature_threshold,
			Double sleeping_area_temperature_thershold, Double toiets_temperature_threshold) {
		this.living_area_temperature_threshold = living_area_temperature_threshold;
		this.sleeping_area_temperature_threshold = sleeping_area_temperature_thershold;
		this.toilets_temperature_threshold = toiets_temperature_threshold;
		this.sqlManager = manager;
		this.planner = planner;
	}*/
	
	public AnalysisThread(SQLManager manager, Planner planner) {
		this.sqlManager = manager;
		this.planner = planner;
	}

	/*private List<Alert> checkThresholds(List<String> rooms, List<String> sensor_types) {

		List<Alert> alerts = new ArrayList<Alert>();

		for (String room : rooms) {

			Alert alert = new Alert();
			alert.setRoom(room);
			
			try {
				List<SensedValue> qualityValues = sqlManager.getSensedData(room, "airQuality", Interval.LAST);
				if (qualityValues.get(0).getValue() < air_quality_threshold) {
					alert.setAirQualityAlert(true);
				}
			} catch (Exception e) {
				System.out.println("No air quality data");
			}
			
			try {
				List<SensedValue> presenceValues = sqlManager.getSensedData(room, "presence", Interval.LAST);
				if (presenceValues.get(0).getValue() > 0) {
					alert.setPresence(true);
				}
			} catch (Exception e) {
				System.out.println("No presence data");
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
	}*/

	private List<TemperatureTrend> calculateTrends(List<String> rooms) {

		List<TemperatureTrend> trends = new ArrayList<TemperatureTrend>();

		for (String room : rooms) {

			List<SensedValue> values = sqlManager.getSensedData(room, "temperature", Interval.ALL);

			TemperatureTrend trend = new TemperatureTrend();
			trend.setRoom(room);

			double slope = 0;
		
			int t4 = 0;
			int t1 = 10;
			double T4 = values.get(t4).getValue();
			double T1 = values.get(t1).getValue();
			int deltaTime = t1 - t4;
			
			slope = (T4 - T1) / deltaTime;
	        
	        trend.setSlope(slope);
	        System.out.println("slope" + slope);
			trends.add(trend);
		}
		
		return trends;
	}
	
	private List<PresencePrediction> predictPresence(List<String> rooms) {

		List<PresencePrediction> presencePredictions = new ArrayList<PresencePrediction>();

		for (String room : rooms) {

			List<SensedValue> values = sqlManager.getSensedData(room, "presence", Interval.ALL);

			PresencePrediction presencePrediction = new PresencePrediction();
			presencePrediction.setRoom(room);

			/* */
			
			
		}
		
		return presencePredictions;
	}
	
	@Override
	public void run() {

		try {

			while (!Thread.interrupted()) {
				while (true) {
					List<String> rooms = sqlManager.getRooms();
					/*List<String> sensor_types = sqlManager.getSensorTypes();*/

					// thresholds checking
					/*List<Alert> alerts = checkThresholds(rooms, sensor_types);*/
					
					// temperature trend
					List<TemperatureTrend> temperatureTrends = calculateTrends(rooms);
					//presence prediction
					List<PresencePrediction> presencePredictions = predictPresence(rooms)
					
					planner.receiveTrends(temperatureTrends);

					Thread.sleep(10000);
				}

			}
			return;

		} catch (InterruptedException e1) {

			System.out.println("Analyzer stopped");
		}
	}

}
