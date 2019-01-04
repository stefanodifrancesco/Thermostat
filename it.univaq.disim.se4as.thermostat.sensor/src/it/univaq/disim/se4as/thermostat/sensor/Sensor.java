package it.univaq.disim.se4as.thermostat.sensor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleContext;

public class Sensor {
	
	private String sensorType;
	private String room;
	private String server;
	private String[] sensedValues;
	private SenderThread send;
	
	
	public Sensor() throws IOException { }
	
	public void setConfiguration(BundleContext context) {
		
		System.out.println("Copy configuration files to " + context.getBundle().getDataFile("").getAbsolutePath());
		
		
		// Get type of sensor and room and server URL
		File configuration = context.getBundle().getDataFile("config.properties");
		
		while (!configuration.exists()) {}

		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			System.out.println("Sleep interrupted");
		}
		
		Properties properties = new Properties();
	    InputStream input = null;

	    try {
	    	if (configuration != null)
	    	{
	    		input = new FileInputStream(configuration);
	    		properties.load(input);
	    		this.sensorType = properties.getProperty("type");
	    		this.room = properties.getProperty("room");
	    		this.server = properties.getProperty("server");
	    	}
	    } catch (IOException ex) {
	        System.out.println("IOException reading configuration file");
	    } finally {
	        if (input != null) {
	            try {
	                input.close();
	            } catch (IOException e) {
	            	System.out.println("IOException closing configuration file");
	            }
	        }
	    }
	    
	    // Get list of sensed values
	    File valuesFile = context.getBundle().getDataFile("values");
		
		while (!valuesFile.exists()) {}
		
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			System.out.println("Sleep interrupted");
		}
		
		List<String> values = new ArrayList<String>();
		
	    BufferedReader abc;
		try {
			abc = new BufferedReader(new FileReader(valuesFile));			
			String line;
			while((line = abc.readLine()) != null) {
				values.add(line);
		    }	    
			abc.close();
		} catch (IOException e) {
			System.out.println("IOException reading values file");
		}
		sensedValues = values.toArray(new String[]{});
	}
	
	public void startSending() {		
		send = new SenderThread(sensorType, room, server, sensedValues);
		send.start();
	    System.out.println("Sensor started sending!");
	}
	
	public void stopSending() {
		send.interrupt();
	}

	public String getSensorType() {
		return sensorType;
	}

	public void setSensorType(String sensorType) {
		this.sensorType = sensorType;
	}
	
	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}
}
