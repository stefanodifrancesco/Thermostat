package it.univaq.disim.se4as.thermostat.analyzer;

import java.sql.Timestamp;

public class PresencePrediction {
	private String room;
	private String day;
	private Timestamp startTime;
	private Timestamp endTime;
	
	
	
	public String getDay() {
		return day;
	}
	public void setDay(String day) {
		this.day = day;
	}
	public String getRoom() {
		return room;
	}
	public void setRoom(String room) {
		this.room = room;
	}
	public Timestamp getStartTime() {
		return startTime;
	}
	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}
	public Timestamp getEndTime() {
		return endTime;
	}
	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}
	
}
