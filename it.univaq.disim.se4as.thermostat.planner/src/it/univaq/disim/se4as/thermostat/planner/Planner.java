package it.univaq.disim.se4as.thermostat.planner;

import java.util.List;

public class Planner {


	public void receiveAlerts(List<Alert> alerts) {
		
		System.out.println(alerts.get(0).getRoom());
		
	}
}
