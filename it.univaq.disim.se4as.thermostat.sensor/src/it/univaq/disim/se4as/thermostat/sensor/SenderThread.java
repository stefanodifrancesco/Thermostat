package it.univaq.disim.se4as.thermostat.sensor;

import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class SenderThread extends Thread {
	
	private MqttClient mqttClient;
	private String sensorType;
	private String room;
	private String server;
	private String[] sensedValues;
 
	public SenderThread(String sensorType, String room, String url, String[] values) {
		this.sensorType = sensorType;
		this.room = room;
		this.server = url;
		this.sensedValues = values;
	}
	
    @Override
    public void run() {
    	
    	try {
    		
    		MemoryPersistence persistence = new MemoryPersistence();
    		
    		try {
    			mqttClient = new MqttClient("tcp://" + server, sensorType + room, persistence);
				mqttClient.connect();
			} catch (MqttException e) {
				e.printStackTrace();
			}
    		
    		while (!Thread.interrupted())
            {
    			
    			try {
        			MqttMessage message = new MqttMessage();
        			
        			for (int i = 0; i < sensedValues.length; i++) {
        				message.setPayload(sensedValues[i].getBytes());
        				mqttClient.publish("home/" + this.room + "/" + this.sensorType, message);
        				TimeUnit.SECONDS.sleep(1);
        			}
        			
        		} catch (MqttException e) {
        			System.out.println("You are here");
        		}
    			
    			System.out.println("Not receiving data from " + sensorType + " of " + room);
	    		
            }
    		return;		
    		
    	} catch (InterruptedException e1) {
    		try {
    			mqttClient.disconnect();
    		} catch (MqttException m) {
    			m.printStackTrace();
    		}
			System.out.println("Sensor disconnected");
		}
    }
}
