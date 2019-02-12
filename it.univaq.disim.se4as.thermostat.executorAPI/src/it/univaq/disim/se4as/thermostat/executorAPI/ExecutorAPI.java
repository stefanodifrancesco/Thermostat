package it.univaq.disim.se4as.thermostat.executorAPI;

public interface ExecutorAPI {

	public enum OnOff {
		ON, OFF
	}

	public void setHeater(OnOff off, String room);
	
	
}
