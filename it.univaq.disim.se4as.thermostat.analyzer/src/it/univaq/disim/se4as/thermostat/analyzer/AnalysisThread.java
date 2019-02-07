package it.univaq.disim.se4as.thermostat.analyzer;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import it.univaq.disim.se4as.thermostat.Models.PresencePrediction;
import it.univaq.disim.se4as.thermostat.Models.SensedValue;
import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager;
import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager.DayOfWeek;

public class AnalysisThread extends Thread {

	private SQLManager sqlManager;

	public AnalysisThread(SQLManager manager) {
		this.sqlManager = manager;
	}
	
	@Override
	public void run() {

		try {

			while (!Thread.interrupted()) {
				while (true) {
					List<String> rooms = sqlManager.getRooms();

					
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

	private List<PresencePrediction> predictPresence(List<String> rooms) {

		List<PresencePrediction> presencePredictions = new ArrayList<PresencePrediction>();

		for (String room : rooms) {
			for (SQLManager.DayOfWeek day : DayOfWeek.values()) {
				
				List<SensedValue> values = sqlManager.getPresenceDataForPrediction(room, day);
				
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
