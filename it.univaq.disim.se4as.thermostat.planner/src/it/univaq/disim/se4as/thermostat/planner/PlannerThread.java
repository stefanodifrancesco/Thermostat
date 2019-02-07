package it.univaq.disim.se4as.thermostat.planner;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import it.univaq.disim.se4as.thermostat.Models.PresencePrediction;
import it.univaq.disim.se4as.thermostat.Models.TemperatureTrend;
import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager;
import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager.DayOfWeek;
import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager.Interval;
import it.univaq.disim.se4as.thermostat.analyzer.Analyzer;
import it.univaq.disim.se4as.thermostat.executor.Executor;
import it.univaq.disim.se4as.thermostat.executor.Executor.OnOff;

public class PlannerThread extends Thread {

	private SQLManager sqlManager;
	private Analyzer analyzer;
	private Executor executor;
	
	private Double living_area_temperature_threshold;
	private Double sleeping_area_temperature_threshold;
	private Double toilets_temperature_threshold;

	public PlannerThread(SQLManager manager, Analyzer analyzerInstance, Executor executor, Double liveThreshold,
			Double sleepThreshold, Double toiletThreshold) {
		this.sqlManager = manager;
		this.analyzer = analyzerInstance;
		this.executor = executor;
		this.living_area_temperature_threshold = liveThreshold;
		this.sleeping_area_temperature_threshold = sleepThreshold;
		this.toilets_temperature_threshold = toiletThreshold;
	}

	@Override
	public void run() {

		try {

			while (!Thread.interrupted()) {
				while (true) {

					plan();

					Thread.sleep(10000);
				}

			}
			return;

		} catch (InterruptedException e1) {

			System.out.println("Planner stopped");
		}
	}

	public void plan() {

		List<TemperatureTrend> trends = analyzer.calculateTrends(sqlManager.getRooms());

		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		int day = calendar.get(Calendar.DAY_OF_WEEK);

		for (String room : sqlManager.getRooms()) {

			List<PresencePrediction> presences = sqlManager.getPresenceHistory(room, DayOfWeek.values()[day]);

			TemperatureTrend currentTrend = null;
			for (TemperatureTrend trend : trends) {
				if (trend.getRoom().equals(room)) {
					currentTrend = trend;
				}
			}

			Double currentTemperature = sqlManager.getSensedData(room, "temperature", Interval.LAST).get(0).getValue();
			Double currentPresence = sqlManager.getSensedData(room, "presence", Interval.LAST).get(0).getValue();

			calculatePlan(currentTemperature, currentTrend, currentPresence, day, presences, room);
		}

	}

	private void calculatePlan(Double currentTemperature, TemperatureTrend currentTrend, Double currentPresence,
			int day, List<PresencePrediction> presences, String room) {

		// Get the area
		Double targetTemperature = 0D;

		if (room.substring(0, 7) == "bedroom") {
			targetTemperature = sleeping_area_temperature_threshold;
		}
		if (room.substring(0, 6) == "toilet") {
			targetTemperature = toilets_temperature_threshold;
		}
		if (room.substring(0, 10) == "livingroom" || room.substring(0, 7) == "kitchen") {
			targetTemperature = living_area_temperature_threshold;
		}

		if (currentPresence == 0) { // no presence

			Date currentDate = new Date();

			Long difference = 0L;

			for (int i = 0; i < presences.size(); i++) {

				if (currentDate.after(presences.get(i).getEndTime())) {

				} else {

					if (presences.get(i).getStartTime().before(currentDate)) { // inside prediction interval

					} else {
						difference = (presences.get(i).getStartTime().getTime() - currentDate.getTime()) / 1000;
					}
				}

			}

			if (currentTrend.getSlope() >= 0 && currentTemperature >= targetTemperature) {
				executor.setHeater(OnOff.OFF, room);
			} else {
				if (difference < 3600) {
					executor.setHeater(OnOff.ON, room);
				}
			}

		} else { // presence

			if (currentTemperature >= targetTemperature) {

				if (currentTrend.getSlope() >= 0) {
					executor.setHeater(OnOff.OFF, room);
				} else {
					executor.setHeater(OnOff.ON, room);
				}
			} else {
				executor.setHeater(OnOff.ON, room);
			}
		}

	}

}
