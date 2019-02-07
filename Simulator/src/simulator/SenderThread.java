package simulator;

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
	private Double value;
 
	public SenderThread(String sensorType, String room, String url, Double value) {
		this.sensorType = sensorType;
		this.room = room;
		this.server = url;
		this.value = value;
	}
	
    @Override
    public void run() {
    	
    	try {
    		
    		MemoryPersistence persistence = new MemoryPersistence();
    		
    		try {
    			mqttClient = new MqttClient("tcp://" + server, "home/" + sensorType + "/" + room + "/values", persistence);
				mqttClient.connect();
			} catch (MqttException e) {
				e.printStackTrace();
				return;
			}
    		
    		while (!Thread.interrupted())
            {
    			
    			try {
        			MqttMessage message = new MqttMessage();
        			
        			while (true) {
        				message.setPayload(value.toString().getBytes());
        				mqttClient.publish("home/" + this.room + "/" + this.sensorType + "/values", message);
        				TimeUnit.SECONDS.sleep(5);
        			}
        			
        		} catch (MqttException e) {
        			System.out.println(sensorType + "sensor of " + room + " cannot send data Value");
        			TimeUnit.SECONDS.sleep(5);
        		}
    			
    			
	    		
            }	
    		
    	} catch (InterruptedException e1) {
    		try {
    			mqttClient.disconnect();
    		} catch (MqttException m) {
    			m.printStackTrace();
    		}
			System.out.println("Disconnected");
		}
    }
}
