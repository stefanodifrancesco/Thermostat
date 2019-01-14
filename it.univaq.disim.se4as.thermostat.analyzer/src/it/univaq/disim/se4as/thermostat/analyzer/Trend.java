package it.univaq.disim.se4as.thermostat.analyzer;

public class Trend {
	
	private String room;
	private int slope;
	
	public Trend() {
		super();
	}
	
	public Trend(String room, int slope) {
		super();
		this.room = room;
		this.slope = slope;
	}
	
	public String getRoom() {
		return room;
	}
	
	public void setRoom(String room) {
		this.room = room;
	}
	
	public int getSlope() {
		return slope;
	}
	
	public void setSlope(int slope) {
		this.slope = slope;
	}
	

}
