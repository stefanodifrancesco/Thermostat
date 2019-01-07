package it.univaq.disim.se4as.thermostat.monitor;

import java.util.ArrayList;
import java.util.List;

import javax.activation.UnsupportedDataTypeException;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import it.univaq.disim.se4as.thermostat.sensor.api.ISensor;
import it.univaq.disim.se4as.thermostat.sensor.api.SensorInfo;

public class Monitor extends Thread implements MqttCallback{
	private Boolean active = true;

	private MqttClient mqttClient;
	
	private List<SensorInfo> sensorInfoListOld = new ArrayList<SensorInfo>();
	private List<SensorInfo> sensorInfoListNew = new ArrayList<SensorInfo>();
	
	private BundleContext context;

	public Monitor(BundleContext context) {
		this.context = context;
	}
	
	public void run() {
		connect();
		
		while (active) {
		getTopics();
		
		//check difference between the 2 lists
		updateSubscriptions();
		}
		
	}

	public void getTopics(){
		

			ServiceReference<?>[] refs;
			try {
				refs = context.getAllServiceReferences(ISensor.class.getName(), null);
			

			if (refs != null) {
				for (int i = 0; i < refs.length; i++) {
					ISensor sensor = (ISensor) context.getService(refs[i]);
					if (refs[i] != null) {
						if (sensor != null) {
							/*System.out.println(sensor.getSensorInfo().getRoom().toString());
							System.out.println(sensor.getSensorInfo().getSensorType().toString());*/
							
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
			
		}
	
	public void connect() {
		
		try {
			
			mqttClient = new MqttClient("tcp://iot.eclipse.org:1883", "monitorSubscribe1");
			mqttClient.setCallback(this);
			mqttClient.connect();
			
		} catch (MqttException e) {
		      e.printStackTrace();
		}
	  }
	
	
	public void subscribe(SensorInfo info) {
		
		try {
		
			mqttClient.subscribe("home/"+ info.getRoom());
			
			
		} catch (MqttException e) {
		      e.printStackTrace();
		}
	  }
	
	public void unsubscribe(SensorInfo info) {
		
		try {
			
			
			mqttClient.unsubscribe("home/"+ info.getRoom());
			
			
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
		
		Double sensedValue = Double.parseDouble(message.toString());
		
		/*SQLManager sqlManager = new SQLManager();
		sqlManager.insertTemperature("pippobaudo", temperature);
		
		System.out.println("Interted in the DB");*/		
	}
	 
	public void updateSubscriptions() {
		
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
		
	}
	
	
}
