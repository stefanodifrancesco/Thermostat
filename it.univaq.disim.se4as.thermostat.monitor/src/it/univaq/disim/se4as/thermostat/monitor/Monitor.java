package it.univaq.disim.se4as.thermostat.monitor;

import it.univaq.disim.se4as.thermostat.Models.SensedValue;
import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager;
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
	//private Boolean active = true;

	private MqttClient mqttClient;
	
	private String server;
	
	/*private List<SensorInfo> sensorInfoListOld = new ArrayList<SensorInfo>();
	private List<SensorInfo> sensorInfoListNew = new ArrayList<SensorInfo>();*/
	
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
	
	/*@Override
	public void run() {
		connect();
		
		while (active) {
		getTopics();
		
		//check difference between the 2 lists
		updateSubscriptions();
		}
		
	}*/

	/*public void getTopics(){

			ServiceReference<?>[] refs;
			try {
				refs = context.getAllServiceReferences(ISensor.class.getName(), null);
			

			if (refs != null) {
				for (int i = 0; i < refs.length; i++) {
					ISensor sensor = (ISensor) context.getService(refs[i]);
					if (refs[i] != null) {
						if (sensor != null) {
							System.out.println(sensor.getSensorInfo().getRoom().toString());
							System.out.println(sensor.getSensorInfo().getSensorType().toString());
							
							sensorInfoListNew.add(sensor.getSensorInfo());
						}

					}
				}

			}
			
			} catch (InvalidSyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}*/
	
	
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
		
			//mqttClient.subscribe("home/"+ info.getRoom());
			mqttClient.subscribe("home/#");
			
		} catch (MqttException e) {
		      e.printStackTrace();
		}
	  }
	
	/*public void unsubscribe(SensorInfo info) {
		
		try {
			
			
			mqttClient.unsubscribe("home/"+ info.getRoom());
			
			
		} catch (MqttException e) {
		      e.printStackTrace();
		}
	  }*/

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
		
		
		insertDataIntoDB(arg0, sensedValue);
		
		/*SQLManager sqlManager = new SQLManager(context);
		sqlManager.insertTemperature(arg0, sensedValue);
		
		System.out.println("Interted in the DB");*/
	}
	 
	/*public void updateSubscriptions() {
		
		//loop for new sensors
		for (SensorInfo sensorInfo : sensorInfoListNew) {
			if (!sensorInfoListOld.contains(sensorInfo)) {
				subscribe(sensorInfo);
				sensorInfoListOld.add(sensorInfo);
			}
		} 
		
		//loop for removed sensors
		for (SensorInfo sensorInfo : sensorInfoListOld) {
			if (!sensorInfoListNew.contains(sensorInfo)) {
				unsubscribe(sensorInfo);
				sensorInfoListOld.remove(sensorInfo);
			}
		} 
		
	}*/
	
	public void insertDataIntoDB(String topic, Double value) {
		String[] topicArray;
		
		SensedValue sensedValue= new SensedValue();
		
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
		
		
		try {
			String delimiter= "/";
			
			topicArray = topic.split(delimiter);
			
			sensedValue.setRoom(topicArray[1]);
			sensedValue.setSensorType(topicArray[2]);
			sensedValue.setValue(value);
			
			if (sqlManager != null) {
				sqlManager.insertSensedValue(sensedValue);
			}			
			
			//System.out.println("Interted in the DB");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
