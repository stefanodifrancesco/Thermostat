package it.univaq.disim.se4as.thermostat.DatabaseAPI;

import java.util.List;
import it.univaq.disim.se4as.thermostat.Models.PresencePrediction;
import it.univaq.disim.se4as.thermostat.Models.SensedValue;


public interface DatabaseAPI {
	public enum Interval {
		LAST, LAST_WEEK, ALL
	}

	public enum DayOfWeek {
		sunday, monday, tuesday, wednesday, thursday, friday, saturday
	}

	public void insertSensedValue(SensedValue sensedValue);
	
	public List<String> getRooms();
	
	public List<String> getSensorTypes();
	
	public List<SensedValue> getSensedData(String room, String sensorType, Interval interval);
	
	public List<SensedValue> getPresenceDataForPrediction(String room, DayOfWeek dayOfWeek);
	
	public List<PresencePrediction> getPresenceHistory(String room, DayOfWeek dayOfWeek);
	
	public void clearPresenceHistory();
	
	public void insertPresenceHistory(PresencePrediction presencePrediction);
	
	public List<SensedValue> getTodayPresenceIntervals(String room);
	
	public void insertTodayPresence(PresencePrediction presencePrediction);
	
	public void clearTodayPresence();
	
}
