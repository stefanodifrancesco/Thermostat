package it.univaq.disim.se4as.thermostat.analyzer;

import java.util.List;

import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager;
import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager.Interval;
import it.univaq.disim.se4as.thermostat.SQLManager.model.SensedValue;

public class CheckingThread extends Thread {
	
	private Double living_area_temperature_threshold;
	private Double sleeping_area_temperature_threshold;
	private Double toilets_temperature_threshold;
	
	private SQLManager sqlManager;
 
	public CheckingThread(SQLManager manager, Double living_area_temperature_threshold, Double sleeping_area_temperature_thershold, Double toiets_temperature_threshold) {
		this.living_area_temperature_threshold = living_area_temperature_threshold;
		this.sleeping_area_temperature_threshold = sleeping_area_temperature_thershold;
		this.toilets_temperature_threshold = toiets_temperature_threshold;
		this.sqlManager = manager;
	}
	
    @Override
    public void run() {
    	
    	try {
    		
    		
    		while (!Thread.interrupted())
            {
    			while(true) {
    				List<String> rooms = sqlManager.getRooms();
	    			List<String> sensor_types = sqlManager.getSensorTypes();
	    			
	    			for (String room : rooms) {
	    				
	    				if (room.contains("sala")) {
		    				
	    					for (String type : sensor_types) {
		    					List<SensedValue> values = sqlManager.getSensedData(room, type, Interval.LAST);

		    					if (values.get(0).getValue() < living_area_temperature_threshold) {
				    				System.out.println("Analyzer - living_area_temperature_threshold excedeed");
				    			}
		    				}
	    					
	    					
		    			}
		    			if (room.contains("camera")) {
		    				for (String type : sensor_types) {
		    					List<SensedValue> values = sqlManager.getSensedData(room, type, Interval.LAST);

		    					if (values.get(0).getValue() < sleeping_area_temperature_threshold) {
				    				System.out.println("Analyzer - sleeping_area_temperature_threshold excedeed");
				    			}
		    				}	
		    			}
						if (room.contains("kitchen")) {
							
							for (String type : sensor_types) {
		    					List<SensedValue> values = sqlManager.getSensedData(room, type, Interval.LAST);

		    					if (values.get(0).getValue() < living_area_temperature_threshold) {
				    				System.out.println("Analyzer - living_area_temperature_threshold excedeed");
				    			}
		    				}		
							
						}
						if (room.contains("bathroom")) {
							for (String type : sensor_types) {
		    					List<SensedValue> values = sqlManager.getSensedData(room, type, Interval.LAST);

		    					if (values.get(0).getValue() < toilets_temperature_threshold) {
				    				System.out.println("Analyzer - toilets_temperature_threshold excedeed");
				    			}
		    				}	
						}
						if (room.contains("outside")) {
							List<SensedValue> values = sqlManager.getSensedData(room, "temperature", Interval.LAST);

	    					if (values.get(0).getValue() < living_area_temperature_threshold) {
			    				System.out.println("Analyzer - living_area_temperature_threshold excedeed");
			    			}
						}
	    				
	    			}
	    			
	    			Thread.sleep(10000);
    			}
    			
            }
    		return;		
    		
    	} catch (InterruptedException e1) {
    		
			System.out.println("Analyzer stopped");
		}
    }

}
