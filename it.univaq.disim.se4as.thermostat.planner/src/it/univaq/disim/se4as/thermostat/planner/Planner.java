package it.univaq.disim.se4as.thermostat.planner;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager;
import it.univaq.disim.se4as.thermostat.analyzer.Analyzer;
import it.univaq.disim.se4as.thermostat.executor.Executor;

public class Planner implements MqttCallback{

	private BundleContext context;

	private Map<String, Double> thresholds = new HashMap<String, Double>();

	private PlannerThread plannerThread;

	private MqttClient mqttClient;
	
	private String hostMQTT;
	
	
	
	public Planner(BundleContext context) {
		this.context = context;
	}

	public void startPlanning() {
		
		connect();
		subscribe();

		plannerThread = new PlannerThread(getSQLmanagerInstance(), getAnalyzerInstance(), getExecutorInstance());
		plannerThread.start();
		System.out.println("Planner started!");

	}

	public void stopPlanning() {
		plannerThread.interrupt();
	}

	
	
	
	public void connect() {
		
		MemoryPersistence persistence = new MemoryPersistence();
		
		try {
			
			mqttClient = new MqttClient("tcp://"+ hostMQTT, "plannerSubscribe", persistence);
			mqttClient.setCallback(this);
			mqttClient.connect();
			
		} catch (MqttException e) {
		      e.printStackTrace();
		}
	  }
	
	
	public void subscribe() {
		
		try {
		
			
			mqttClient.subscribe("home/thresholds/#");
			
		} catch (MqttException e) {
		      e.printStackTrace();
		}
	  }

	@Override
	public void connectionLost(Throwable arg0) {
		System.out.println("Planner: Connection with the MQTT Broker lost.");
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		System.out.println("The delivery has been complete. The delivery token is " + arg0.toString());
	}

	@Override
	public void messageArrived(String arg0, MqttMessage message) throws Exception {
		System.out.println("A new message arrived from the topic: \"" + arg0 + "\". The payload of the message is "
				+ message.toString());
		
		Double threshold;
		String room;
		
		try {
			threshold = Double.parseDouble(message.toString());
		} catch (Exception e) {
			threshold = 20.0;
		}
		
		room = arg0.split("/")[2];
		
		setThresholds(room, threshold);
	
	}
	
	private void setThresholds(String room, Double threshold) {
		
		thresholds.put(room, threshold);	
		plannerThread.setThresholds(thresholds);
		
	}

	public void setConfiguration(BundleContext context) {

		System.out
				.println("Planner - Copy config file to " + context.getBundle().getDataFile("").getAbsolutePath());

		// Get type of sensor and room and server URL File configuration =
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
				
				this.hostMQTT = properties.getProperty("hostMQTT");
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

		ServiceReference<?>[] refs;

		try {
			refs = context.getAllServiceReferences(SQLManager.class.getName(), null);

			if (refs != null) {

				if (refs[0] != null) {
					SQLManager manager = (SQLManager) context.getService(refs[0]);
					return manager;
				}
			}

		} catch (InvalidSyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return null;
	}

	public Analyzer getAnalyzerInstance() {

		Analyzer analyzerInstance = null;

		ServiceReference<?>[] refs;

		try {
			refs = context.getAllServiceReferences(Analyzer.class.getName(), null);

			if (refs != null) {

				if (refs[0] != null) {
					Analyzer analyzer = (Analyzer) context.getService(refs[0]);
					if (analyzer != null) {
						analyzerInstance = analyzer;
					}

				}
			}

		} catch (InvalidSyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return analyzerInstance;
	}

	public Executor getExecutorInstance() {

		ServiceReference<?>[] refs;

		try {
			refs = context.getAllServiceReferences(Executor.class.getName(), null);

			if (refs != null) {

				if (refs[0] != null) {
					Executor executorInstance = (Executor) context.getService(refs[0]);
					return executorInstance;
				}
			}

		} catch (InvalidSyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return null;
	}
}
