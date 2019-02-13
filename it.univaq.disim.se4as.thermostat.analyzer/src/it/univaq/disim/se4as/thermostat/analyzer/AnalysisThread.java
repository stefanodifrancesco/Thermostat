package it.univaq.disim.se4as.thermostat.analyzer;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

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
					
					//today presence
					databaseAPI.clearTodayPresence();
					for (PresencePrediction prediction : aggregateTodayPresence(rooms)) {
						databaseAPI.insertTodayPresence(prediction);
					}
					

					Thread.sleep(12000);
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
		Boolean first = false;
		int firstDay = 0;
		
		PresencePrediction presencePredictiontemp = null;
		
		for (int i = values.size() - 1; i >= 0; i--) {

			if (values.get(i).getValue() == 1 && presence == false) {
				presence = true;

				startTime = values.get(i).getTimestamp();
			}

			if (values.get(i).getValue() == 0 && presence == true && first == false) {
				presence = false;
				Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
				calendar.setTime(values.get(i).getTimestamp());
				firstDay = calendar.get(Calendar.DAY_OF_YEAR);
				first = true;
				endTime = values.get(i).getTimestamp();

				presencePredictiontemp = new PresencePrediction();
				presencePredictiontemp.setRoom(room);
				presencePredictiontemp.setDay(day.toString());
				presencePredictiontemp.setStartTime(startTime);
				presencePredictiontemp.setEndTime(endTime);

				intervalsPredictions.add(presencePredictiontemp);
			}

			if (values.get(i).getValue() == 0 && first == true) {
				Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
				calendar.setTime(values.get(i).getTimestamp());
				int secondDay = calendar.get(Calendar.DAY_OF_YEAR);
				
				if (secondDay != firstDay) {
					presence = false;
					first = false;
					endTime = values.get(i).getTimestamp();

					presencePredictiontemp.setRoom(room);
					presencePredictiontemp.setDay(day.toString());
					presencePredictiontemp.setStartTime(startTime);
					presencePredictiontemp.setEndTime(endTime);

				}
			}

			if (values.get(i).getValue() == 1 && presence == true && i == 0) {

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

	private List<PresencePrediction> todayIntervals(List<SensedValue> values, String room, DayOfWeek day) {

		List<PresencePrediction> intervalsPredictions = new ArrayList<PresencePrediction>();

		Timestamp startTime = null;
		Timestamp endTime = null;

		Boolean presence = false;

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
			

			if (values.get(i).getValue() == 1 && presence == true && i == 0) {

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

	
	private List<PresencePrediction> aggregateTodayPresence(List<String> rooms) {

		List<PresencePrediction> todayPresence = new ArrayList<PresencePrediction>();

		for (String room : rooms) {
			
			Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
			int numDay = calendar.get(Calendar.DAY_OF_WEEK);
			
			//DayOfWeek.values()[numDay - 1]
			
				List<SensedValue> values = databaseAPI.getTodayPresenceIntervals(room);

				if (values.size() > 0) {
					todayPresence.addAll(todayIntervals(values, room, DayOfWeek.values()[numDay - 1]));
				}

			
		}

		return todayPresence;
	}
	
	
}
