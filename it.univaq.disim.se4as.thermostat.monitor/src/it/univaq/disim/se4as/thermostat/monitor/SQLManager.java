package it.univaq.disim.se4as.thermostat.monitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleContext;

public class SQLManager {

	private String server;
	private String database;
	private String user;
	private String password;
	
	public SQLManager(BundleContext context) {
		setConfiguration(context);
	}

	public void setConfiguration(BundleContext context) {

		System.out.println("SQLManager - Copy configuration files to " + context.getBundle().getDataFile("").getAbsolutePath());

		// Get type of sensor and room and server URL
		File configuration = context.getBundle().getDataFile("SQL_config.properties");

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
				this.database = properties.getProperty("database");
				this.user = properties.getProperty("user");
				this.password = properties.getProperty("password");
				
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

	public Connection connect() {

		Connection connection = null;

		try {
			//Class.forName("com.mysql.jdbc.Driver");

			connection = DriverManager.getConnection(
					"jdbc:mysql://" + server + "/" + database + "?" + "user=" + user + "&password=" + password + "");
		} catch (Exception e) {
			try {
				connection = DriverManager.getConnection(
						"jdbc:mysql://" + server + "/" + database + "?" + "user=" + user + "&password=" + password + "&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC");
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		return connection;

	}

	public void insertSensedValue(String room, String sensorType, Double sensedValue) {
		Connection connection = connect();

		// PreparedStatements can use variables and are more efficient
		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection.prepareStatement("insert into se4as.sensed_values(room, sensor_type, value) " +
																						" values (?, ?, ?)");

			// Parameters start with 1
			preparedStatement.setString(1, room);
			preparedStatement.setString(2, sensorType);
			preparedStatement.setDouble(3, sensedValue);

			preparedStatement.executeUpdate();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
