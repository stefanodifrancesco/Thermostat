package it.univaq.disim.se4as.thermostat.executor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.osgi.framework.BundleContext;

public class Executor {
	private String hostMQTT;

	public enum OnOff {
		ON, OFF
	}

	public Executor(BundleContext context) {
		setConfiguration(context);
	}

	public void setHeater(OnOff OnOff, String room) {

		try {

			MemoryPersistence persistence = new MemoryPersistence();

			MqttClient mqttClient;

			mqttClient = new MqttClient("tcp://" + hostMQTT, "Executor_" + room, persistence);

			mqttClient.connect();

			MqttMessage message = new MqttMessage();

			message.setPayload(OnOff.toString().getBytes());
			mqttClient.publish("home/" + room + "/heater", message);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setConfiguration(BundleContext context) {

		System.out.println(
				"Executor - Copy configuration files to " + context.getBundle().getDataFile("").getAbsolutePath());

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

				this.hostMQTT = properties.getProperty("server");

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
