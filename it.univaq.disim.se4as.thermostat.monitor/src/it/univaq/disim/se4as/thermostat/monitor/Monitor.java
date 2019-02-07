package it.univaq.disim.se4as.thermostat.monitor;

import it.univaq.disim.se4as.thermostat.DatabaseAPI.DatabaseAPI;
import it.univaq.disim.se4as.thermostat.Models.SensedValue;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class Monitor implements MqttCallback{

	private MqttClient mqttClient;	
	private String server;
	private BundleContext context;

	public Monitor(BundleContext context) {
		this.context = context;
		
		setConfiguration(context);
		
		connect();
		subscribe();
	}
	
	public void setConfiguration(BundleContext context) {

		System.out.println("Monitor - Copy configuration files to " + context.getBundle().getDataFile("").getAbsolutePath());

		// Get type of sensor and room and server URL
		File configuration = context.getBundle().getDataFile("config.properties");

		while (!configuration.exists()) {
		}

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
	}
	
	public void connect() {
		
		MemoryPersistence persistence = new MemoryPersistence();
		
		try {
			
			mqttClient = new MqttClient("tcp://"+ server, "monitorSubscribe", persistence);
			mqttClient.setCallback(this);
			mqttClient.connect();
			
		} catch (MqttException e) {
		      e.printStackTrace();
		}
	  }
	
	
	public void subscribe() {
		
		try {
			
			mqttClient.subscribe("home/sensors/#");
			
		} catch (MqttException e) {
		      e.printStackTrace();
		}
	  }

	@Override
	public void connectionLost(Throwable arg0) {
		System.out.println("Monitor: Connection with the MQTT Broker lost.");
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		System.out.println("The delivery has been complete. The delivery token is " + arg0.toString());
	}

	@Override
	public void messageArrived(String arg0, MqttMessage message) throws Exception {
		System.out.println("A new message arrived from the topic: \"" + arg0 + "\". The payload of the message is "
				+ message.toString());
		
		Double sensedValue;
		try {
			 sensedValue = Double.parseDouble(message.toString());
		} catch (Exception e) {
			sensedValue = 100.0;
		}
		
		if (sensedValue> -50d && sensedValue<50d) {
			insertDataIntoDB(arg0, sensedValue);
		}else {
			System.out.println("Monitor - Discarded fault value");
		}

	}
	
	public void insertDataIntoDB(String topic, Double value) {
		String[] topicArray;
		
		SensedValue sensedValue= new SensedValue();
		
		DatabaseAPI databaseAPI = null;
		
		ServiceReference<?>[] refs;
		
		try {
			refs = context.getAllServiceReferences(DatabaseAPI.class.getName(), null);
		
			if (refs != null) {
				
					if (refs[0] != null) {
						DatabaseAPI dbAPI = (DatabaseAPI) context.getService(refs[0]);
						if (dbAPI != null) {
							databaseAPI = dbAPI;
						}
	
					}
			}
		
		} catch (InvalidSyntaxException e1) {
			e1.printStackTrace();
		}
		
		
		try {
			String delimiter= "/";
			
			topicArray = topic.split(delimiter);
			
			sensedValue.setRoom(topicArray[2]);
			sensedValue.setSensorType(topicArray[3]);
			sensedValue.setValue(value);
			
			if (databaseAPI != null) {
				databaseAPI.insertSensedValue(sensedValue);
			}			
			
			//System.out.println("Interted in the DB");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
