package it.univaq.disim.se4as.thermostat.sensor;

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

public class Sensor implements MqttCallback {

	private String sensor_type;
	private String room;
	private String server;

	private MqttClient mqttClientReceiver;
	private MqttClient mqttClientSender;

	private BundleContext context;

	public Sensor(BundleContext context) {
		this.context = context;
	}

	public void startSensor() {

		setConfiguration();
		System.out.println("Sensor " + room + " - " + sensor_type + " started sending!");

		connectReceiver();
		connectSender();
		
		subscribe();
	}

	public void connectReceiver() {

		MemoryPersistence persistence = new MemoryPersistence();

		try {

			mqttClientReceiver = new MqttClient("tcp://" + server, "Sensor_"+room+"_"+sensor_type+"_receiver", persistence);
			mqttClientReceiver.setCallback(this);
			mqttClientReceiver.connect();

		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	public void connectSender() {

		MemoryPersistence persistence = new MemoryPersistence();

		try {

			mqttClientSender = new MqttClient("tcp://" + server, "Sensor_"+room+"_"+sensor_type+"_sender", persistence);
			mqttClientSender.setCallback(this);
			mqttClientSender.connect();

		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	

	public void subscribe() {

		try {

			mqttClientReceiver.subscribe("home/" + room + "/" + sensor_type + "/values");

		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void connectionLost(Throwable arg0) {
		System.out.println("Sensor " + room + " - " + sensor_type + ": Connection with the MQTT Broker lost.");
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

		send(sensedValue);

	}

	private void send(Double sensedValue) {
		
		try {
			
			MqttMessage message = new MqttMessage();

			message.setPayload(sensedValue.toString().getBytes());
			mqttClientSender.publish("home/sensors/" + this.room + "/" + this.sensor_type, message);

			
		} catch (MqttException e) {
			System.out.println(sensor_type + "sensor of " + room + " cannot send data Value");
		}
	}

	public void setConfiguration() {

		System.out.println(
				"Sensor - Copy configuration files to " + context.getBundle().getDataFile("").getAbsolutePath());

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
				this.sensor_type = properties.getProperty("type");
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

	}
}
