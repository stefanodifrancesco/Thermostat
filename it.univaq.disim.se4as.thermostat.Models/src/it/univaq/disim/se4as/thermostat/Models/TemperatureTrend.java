package it.univaq.disim.se4as.thermostat.Models;

public class TemperatureTrend {
	
	private String room;
	private double slope;
	
	public TemperatureTrend() {	}
	
	public TemperatureTrend(String room, int slope) {
		this.room = room;
		this.slope = slope;
	}
	
	public String getRoom() {
		return room;
	}
	
	public void setRoom(String room) {
		this.room = room;
	}
	
	public double getSlope() {
		return slope;
	}
	
	public void setSlope(double slope2) {
		this.slope = slope2;
	}
	

}
