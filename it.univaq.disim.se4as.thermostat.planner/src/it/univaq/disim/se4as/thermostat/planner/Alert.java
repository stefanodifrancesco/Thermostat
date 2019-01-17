package it.univaq.disim.se4as.thermostat.planner;

public class Alert {
	
	private String room;
	
	private boolean temperatureAlert;
	private boolean airQualityAlert;
	private boolean presence;

	public Alert() {
		super();
	}

	public Alert(String room, boolean temperatureAlert, boolean airQualityAlert, boolean presence) {
		super();
		this.room = room;
		this.temperatureAlert = temperatureAlert;
		this.airQualityAlert = airQualityAlert;
		this.presence = presence;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public boolean isTemperatureAlert() {
		return temperatureAlert;
	}

	public void setTemperatureAlert(boolean temperatureAlert) {
		this.temperatureAlert = temperatureAlert;
	}

	public boolean isAirQualityAlert() {
		return airQualityAlert;
	}

	public void setAirQualityAlert(boolean airQualityAlert) {
		this.airQualityAlert = airQualityAlert;
	}

	public boolean isPresence() {
		return presence;
	}

	public void setPresence(boolean presence) {
		this.presence = presence;
	}
	
}
