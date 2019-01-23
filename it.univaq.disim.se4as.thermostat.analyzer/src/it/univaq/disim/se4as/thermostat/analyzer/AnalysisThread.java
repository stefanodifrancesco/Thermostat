package it.univaq.disim.se4as.thermostat.analyzer;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager;
import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager.DayOfWeek;
import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager.Interval;
import it.univaq.disim.se4as.thermostat.SQLManager.model.PresencePrediction;
import it.univaq.disim.se4as.thermostat.SQLManager.model.SensedValue;
import it.univaq.disim.se4as.thermostat.analyzer.models.TemperatureTrend;
import it.univaq.disim.se4as.thermostat.planner.Planner;

public class AnalysisThread extends Thread {

	private SQLManager sqlManager;
	private Planner planner;

	public AnalysisThread(SQLManager manager, Planner planner) {
		this.sqlManager = manager;
		this.planner = planner;
	}

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
			for (SQLManager.DayOfWeek day : DayOfWeek.values()) {
				
				List<SensedValue> values = sqlManager.getPresenceData(room, day);
				
				if (values.size() > 0) {
					presencePredictions.addAll(predictIntervals(values, room, day));
				}

			}
		}

		return presencePredictions;
	}

	private List<PresencePrediction> predictIntervals(List<SensedValue> values, String room, DayOfWeek day) {

		List<PresencePrediction> intervalsPredictions = new ArrayList<PresencePrediction>();

		Timestamp startTime = null;
		Timestamp endTime = null;

		Boolean presence = false;
		if (values.get(values.size() - 1).getValue() == 1) {
			presence = true;
			startTime = values.get(values.size()).getTimestamp();
		}

		for (int i = values.size() - 1; i >= 0; i--) {

			if (values.get(i).getValue() == 1 && presence == false) {
				presence = true;

				startTime = values.get(i).getTimestamp();
			}

			if (values.get(i).getValue() == 0 && presence == true) {
				presence = false;

				endTime = values.get(i).getTimestamp();

				PresencePrediction presencePrediction = new PresencePrediction();
				presencePrediction.setRoom(room);
				presencePrediction.setDay(day.toString());
				presencePrediction.setStartTime(startTime);
				presencePrediction.setEndTime(endTime);
				
				intervalsPredictions.add(presencePrediction);
			}
		}

		return intervalsPredictions;

	}

	@Override
	public void run() {

		try {

			while (!Thread.interrupted()) {
				while (true) {
					List<String> rooms = sqlManager.getRooms();

					// temperature trend
					List<TemperatureTrend> temperatureTrends = calculateTrends(rooms);

					// presence prediction
					sqlManager.clearPresenceHistory();
					for (PresencePrediction prediction : predictPresence(rooms)) {
						sqlManager.insertPresenceHistory(prediction);
					}

					Thread.sleep(10000);
				}

			}
			return;

		} catch (InterruptedException e1) {

			System.out.println("Analyzer stopped");
		}
	}

}
