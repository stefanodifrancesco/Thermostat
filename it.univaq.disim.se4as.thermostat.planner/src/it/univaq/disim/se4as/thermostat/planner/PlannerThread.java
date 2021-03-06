package it.univaq.disim.se4as.thermostat.planner;

import it.univaq.disim.se4as.thermostat.AnalyzerAPI.AnalyzerAPI;
import it.univaq.disim.se4as.thermostat.DatabaseAPI.DatabaseAPI;
import it.univaq.disim.se4as.thermostat.DatabaseAPI.DatabaseAPI.DayOfWeek;
import it.univaq.disim.se4as.thermostat.DatabaseAPI.DatabaseAPI.Interval;
import it.univaq.disim.se4as.thermostat.Models.PresencePrediction;
import it.univaq.disim.se4as.thermostat.Models.TemperatureTrend;
import it.univaq.disim.se4as.thermostat.executorAPI.ExecutorAPI;
import it.univaq.disim.se4as.thermostat.executorAPI.ExecutorAPI.OnOff;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class PlannerThread extends Thread {

	private DatabaseAPI databaseAPI;
	private AnalyzerAPI analyzer;
	private ExecutorAPI executor;

	private Map<String, Double> thresholds = new HashMap<>();

	public PlannerThread(DatabaseAPI databaseAPI, AnalyzerAPI analyzerInstance, ExecutorAPI executor) {
		this.databaseAPI = databaseAPI;
		this.analyzer = analyzerInstance;
		this.executor = executor;
	}

	@Override
	public void run() {

		try {

			while (!Thread.interrupted()) {
				while (true) {

					plan();
					System.out.println("Planner plan called");

					Thread.sleep(10000);
				}

			}
			return;

		} catch (InterruptedException e1) {

			System.out.println("Planner stopped");
		}
	}

	public void plan() {

		List<TemperatureTrend> trends = analyzer.calculateTrends(databaseAPI.getRooms());

		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		int day = calendar.get(Calendar.DAY_OF_WEEK);

		for (String room : databaseAPI.getRooms()) {

			List<PresencePrediction> presences = databaseAPI.getPresenceHistory(room, DayOfWeek.values()[day - 1]);

			TemperatureTrend currentTrend = null;
			for (TemperatureTrend trend : trends) {
				if (trend.getRoom().equals(room)) {
					currentTrend = trend;
				}
			}

			Double currentTemperature = databaseAPI.getSensedData(room, "temperature", Interval.LAST).get(0).getValue();
			Double currentPresence = databaseAPI.getSensedData(room, "presence", Interval.LAST).get(0).getValue();

			calculatePlan(currentTemperature, currentTrend, currentPresence, presences, room);
		}

	}

	private void calculatePlan(Double currentTemperature, TemperatureTrend currentTrend, Double currentPresence,
			List<PresencePrediction> presences, String room) {

		// Get the target temperature of the room
		Double targetTemperature = 0D;

		if (room.contains("livingroom") || room.contains("kitchen")) {
			targetTemperature = thresholds.get("livingroomkitchen");
		} else if (room.contains("bedroom")) {
			targetTemperature = thresholds.get("bedroom");
		} else if (room.contains("toilet")) {
			targetTemperature = thresholds.get("toilet");
		}

		if (targetTemperature == null) {

			System.out.println("Threshold for room: " + room + " not found.");
			targetTemperature = 20D;
		}

		/*
		 * for (String entry : thresholds.keySet()) { if (entry.contains(room)) {
		 * targetTemperature = thresholds.get(entry); } } // if room not found if
		 * (targetTemperature == 0D) { targetTemperature = 20D;
		 * System.out.println("Threshold for room: " + room + " not found."); }
		 */
		if (currentPresence == 1) { // There are people in the room

			if (currentTemperature >= targetTemperature) {

				if (currentTrend.getSlope() >= 0) {
					executor.setHeater(OnOff.OFF, room);
				} else {

					if (getExitTimePrediction(presences) > 3600) {
						executor.setHeater(OnOff.ON, room);
					} else {
						executor.setHeater(OnOff.OFF, room);
					}

				}
			} else {
				executor.setHeater(OnOff.ON, room);
			}
		} else { // There are NO people in the room

			if (currentTrend.getSlope() >= 0 && currentTemperature >= targetTemperature) {
				executor.setHeater(OnOff.OFF, room);
			} else {

				if (getComebackTimePrediction(presences) < 3600) {
					executor.setHeater(OnOff.ON, room);
				} else {
					executor.setHeater(OnOff.OFF, room);
				}

			}
		}

	}

	public Long getComebackTimePrediction(List<PresencePrediction> presences) {

		Calendar currentTime = Calendar.getInstance();
		currentTime.setTime(new Date());

		Calendar intervalStartTime = Calendar.getInstance();
		Calendar intervalEndTime = Calendar.getInstance();

		long difference = 0L;

		// Iterates over the intervals of the table 'presences'
		for (int i = 0; i < presences.size(); i++) {

			// Sets the end time of the interval
			intervalEndTime.setTime(presences.get(i).getEndTime());
			intervalEndTime.set(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH),
					currentTime.get(Calendar.DAY_OF_MONTH));

			if (currentTime.after(intervalEndTime)) {
				// previous interval
			} else {

				// Sets the start time of the interval
				intervalStartTime.setTime(presences.get(i).getStartTime());
				intervalStartTime.set(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH),
						currentTime.get(Calendar.DAY_OF_MONTH));

				if (intervalStartTime.before(currentTime)) {
					// inside prediction interval
					// prediction wrong
					// set difference to 0 because people are already inside the room
					difference = 0L;
				} else {
					// We calculate the time remaining until this interval starts
					difference = (intervalStartTime.getTimeInMillis() - currentTime.getTimeInMillis()) / 1000;
				}

				break;
			}
		}

		return difference;
	}

	public Long getExitTimePrediction(List<PresencePrediction> presences) {

		Calendar currentTime = Calendar.getInstance();
		currentTime.setTime(new Date());

		Calendar intervalStartTime = Calendar.getInstance();
		Calendar intervalEndTime = Calendar.getInstance();

		long timeToExit = 0L;

		// Iterates over the intervals of the table 'presences'
		for (int i = 0; i < presences.size(); i++) {

			// Sets the end time of the interval
			intervalEndTime.setTime(presences.get(i).getEndTime());
			intervalEndTime.set(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH),
					currentTime.get(Calendar.DAY_OF_MONTH));

			if (currentTime.after(intervalEndTime)) {
				// previous interval
			} else {

				// Sets the start time of the interval
				intervalStartTime.setTime(presences.get(i).getStartTime());
				intervalStartTime.set(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH),
						currentTime.get(Calendar.DAY_OF_MONTH));

				if (intervalStartTime.before(currentTime)) {
					// inside prediction interval
					// we calculate the time remaining until this interval ends
					timeToExit = (intervalEndTime.getTimeInMillis() - currentTime.getTimeInMillis()) / 1000;
				} else {
					// Wrong presence prediction but we calculate the predicted time remaining until
					// people exit
					timeToExit = (intervalEndTime.getTimeInMillis() - currentTime.getTimeInMillis()) / 1000;
				}

				break;
			}
		}

		return timeToExit;
	}

	public Map<String, Double> getThresholds() {
		return thresholds;
	}

	public void setThresholds(Map<String, Double> thresholds) {
		this.thresholds = thresholds;
	}

}
