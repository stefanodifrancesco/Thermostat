package it.univaq.disim.se4as.thermostat.analyzer;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import it.univaq.disim.se4as.thermostat.DatabaseAPI.DatabaseAPI;
import it.univaq.disim.se4as.thermostat.DatabaseAPI.DatabaseAPI.DayOfWeek;
import it.univaq.disim.se4as.thermostat.Models.PresencePrediction;
import it.univaq.disim.se4as.thermostat.Models.SensedValue;

public class AnalysisThread extends Thread {

	private DatabaseAPI databaseAPI;

	public AnalysisThread(DatabaseAPI databaseAPI) {
		this.databaseAPI = databaseAPI;
	}
	
	@Override
	public void run() {

		try {

			while (!Thread.interrupted()) {
				while (true) {
					List<String> rooms = databaseAPI.getRooms();

					
					// presence prediction
					databaseAPI.clearPresenceHistory();
					for (PresencePrediction prediction : predictPresence(rooms)) {
						databaseAPI.insertPresenceHistory(prediction);
					}
					
					Thread.sleep(10000);
				}

			}
			return;

		} catch (InterruptedException e1) {

			System.out.println("Analyzer stopped");
		}
	}

	private List<PresencePrediction> predictPresence(List<String> rooms) {

		List<PresencePrediction> presencePredictions = new ArrayList<PresencePrediction>();

		for (String room : rooms) {
			for (DayOfWeek day : DayOfWeek.values()) {
				
				List<SensedValue> values = databaseAPI.getPresenceDataForPrediction(room, day);
				
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

	

}
