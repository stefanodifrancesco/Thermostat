package it.univaq.disim.se4as.thermostat.analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager;
import it.univaq.disim.se4as.thermostat.planner.Planner;

public class Analyzer {
	
	private Double living_area_temperature_threshold;
	private Double sleeping_area_temperature_threshold;
	private Double toiets_temperature_threshold;
	private CheckingThread check;
	
	private BundleContext context;
	
	public Analyzer(BundleContext context) {
		this.context = context;
	}
	
	public void setConfiguration(BundleContext context) {

		System.out.println("Analyzer - Copy thresholds file to " + context.getBundle().getDataFile("").getAbsolutePath());

		// Get type of sensor and room and server URL
		File configuration = context.getBundle().getDataFile("threshold_config.properties");

		while (!configuration.exists()) { }

		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			System.out.println("Sleep interrupted");
		}

		Properties properties = new Properties();
		InputStream input = null;

		try {
			if (configuration != null) {
				input = new FileInputStream(configuration);
				properties.load(input);
				
				this.living_area_temperature_threshold = Double.parseDouble(properties.getProperty("living_area_temperature_threshold"));
				this.sleeping_area_temperature_threshold = Double.parseDouble(properties.getProperty("sleeping_area_temperature_threshold"));
				this.toiets_temperature_threshold = Double.parseDouble(properties.getProperty("toilets_temperature_threshold"));
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
	}

	public SQLManager getSQLmanagerInstance() {
		
		SQLManager sqlManager = null;
		
		ServiceReference<?>[] refs;
		
		try {
			refs = context.getAllServiceReferences(SQLManager.class.getName(), null);
		
			if (refs != null) {
				
					if (refs[0] != null) {
						SQLManager manager = (SQLManager) context.getService(refs[0]);
						if (manager != null) {
							sqlManager = manager;
						}
	
					}
			}
		
		} catch (InvalidSyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return sqlManager;
	}
	
	public Planner getPlannerInstance() {
		
		Planner plannerInstance = null;
		
		ServiceReference<?>[] refs;
		
		try {
			refs = context.getAllServiceReferences(Planner.class.getName(), null);
		
			if (refs != null) {
				
					if (refs[0] != null) {
						Planner planner = (Planner) context.getService(refs[0]);
						if (planner != null) {
							plannerInstance = planner;
						}
	
					}
			}
		
		} catch (InvalidSyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return plannerInstance;
	}

	public void startCheckingThresholds() {
		
		check = new CheckingThread(getSQLmanagerInstance(), getPlannerInstance(), this.living_area_temperature_threshold, this.sleeping_area_temperature_threshold, this.toiets_temperature_threshold);
		check.start();
	    System.out.println("Analyzer started analyzing!");
	}
}
