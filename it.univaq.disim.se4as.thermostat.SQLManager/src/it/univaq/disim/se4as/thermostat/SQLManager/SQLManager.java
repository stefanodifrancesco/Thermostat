package it.univaq.disim.se4as.thermostat.SQLManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleContext;

import it.univaq.disim.se4as.thermostat.SQLManager.model.SensedValue;

public class SQLManager {

	private String server;
	private String database;
	private String user;
	private String password;
	
	public enum Interval{
		LAST,
		LAST_WEEK,
		ALL
	}
	
	public enum DayOfWeek{
		monday,
		tuesday,
		wednesday,
		thursday,
		friday,
		saturday,
		sunday
	}
	
	public SQLManager(BundleContext context) {
		setConfiguration(context);
	}

	public void setConfiguration(BundleContext context) {

		System.out.println("SQLManager - Copy configuration files to " + context.getBundle().getDataFile("").getAbsolutePath());

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

	public void insertSensedValue(SensedValue sensedValue) {
		Connection connection = connect();

		// PreparedStatements can use variables and are more efficient
		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection.prepareStatement("insert into se4as.sensed_values(room, sensor_type, value, timestamp) " +
																						" values (?, ?, ?, NOW())");

			// Parameters start with 1
			preparedStatement.setString(1, sensedValue.getRoom());
			preparedStatement.setString(2, sensedValue.getSensorType());
			preparedStatement.setDouble(3, sensedValue.getValue());

			preparedStatement.executeUpdate();
			
			connection.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<String> getRooms() {
		List<String> rooms = new ArrayList<String>();
		
		Connection connection = connect();

		// PreparedStatements can use variables and are more efficient
		PreparedStatement preparedStatement;
		
		try {
			preparedStatement = connection.
					prepareStatement("select distinct room " + 
									 " from sensed_values");

			ResultSet rs;
			
			 rs = preparedStatement.executeQuery();
	            while ( rs.next() ) {
	                String room = rs.getString("room");
	                
	                rooms.add(room);
	            }
	            connection.close();
	            
	            
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rooms;
		
	}
	
	public List<String> getSensorTypes() {
		List<String> sensorTypes = new ArrayList<String>();
		
		Connection connection = connect();

		// PreparedStatements can use variables and are more efficient
		PreparedStatement preparedStatement;
		
		try {
			preparedStatement = connection.
					prepareStatement("select distinct sensor_type " + 
									 " from sensed_values");

			ResultSet rs;
			
			 rs = preparedStatement.executeQuery();
	            while ( rs.next() ) {
	                String sensorType = rs.getString("sensor_type");
	                
	                sensorTypes.add(sensorType);
	            }
	            connection.close();
	            
	            
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sensorTypes;
		
	}

	
	public List<SensedValue> getSensedData(String room, String sensorType, Interval internval) {
		List<SensedValue> sensedValues = new ArrayList<SensedValue>();
		
		Connection connection = connect();
		
		String query;

		// PreparedStatements can use variables and are more efficient
		PreparedStatement preparedStatement;
		try {
			
			
			query = "SELECT timestamp, value, sensor_type, room " + 
					" FROM se4as.sensed_values " +
					"WHERE room = ? AND sensor_type = ? "
					+ " ORDER BY timestamp DESC ";
			
			
			switch (internval) {
			case ALL:
				
				
				
				break;
				
			case LAST:
				
				query += " limit 0,1";	
				
				break;
				
			case LAST_WEEK:
				
				query += " limit 0,10";
				
				break;

			default:
				break;
			}			
			
			preparedStatement = connection.
					prepareStatement(query);
			
			// Parameters start with 1
			preparedStatement.setString(1, room);
			preparedStatement.setString(2, sensorType);
			
			ResultSet rs;
			
			 rs = preparedStatement.executeQuery();
	            while ( rs.next() ) {
	            	SensedValue sensedValue = new SensedValue();
	                sensedValue.setTimestamp(rs.getTimestamp("timestamp"));
	                sensedValue.setSensorType(rs.getString("sensor_type"));
	                sensedValue.setValue(rs.getDouble("value"));
	                sensedValue.setRoom(rs.getString("room"));
	                
	                sensedValues.add(sensedValue);
	                
	            }
	         
			connection.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sensedValues;
		
	}

	public List<SensedValue> getPresenceData(String room, DayOfWeek dayOfWeek) {
		List<SensedValue> sensedValues = new ArrayList<SensedValue>();
		
		Connection connection = connect();
		
		String query;

		// PreparedStatements can use variables and are more efficient
		PreparedStatement preparedStatement;
		try {
			
			
			query = "SELECT timestamp, value, sensor_type, room " + 
					" FROM se4as.sensed_values " +
					"WHERE sensor_type = 'presence' " +
					" AND room = ? " +
					" AND DAYNAME(timestamp) = ? " +
					" AND timestamp BETWEEN DATE_SUB(NOW(), INTERVAL 7 DAY) AND NOW();" +
					" ORDER BY timestamp DESC ";
			
			
			preparedStatement = connection.
					prepareStatement(query);
			
			// Parameters start with 1
			preparedStatement.setString(1, room);
			preparedStatement.setString(2, dayOfWeek.toString());
			
			ResultSet rs;
			
			 rs = preparedStatement.executeQuery();
	            while ( rs.next() ) {
	            	SensedValue sensedValue = new SensedValue();
	                sensedValue.setTimestamp(rs.getTimestamp("timestamp"));
	                sensedValue.setSensorType(rs.getString("sensor_type"));
	                sensedValue.setValue(rs.getDouble("value"));
	                sensedValue.setRoom(rs.getString("room"));
	                
	                sensedValues.add(sensedValue);
	                
	            }
	         
			connection.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sensedValues;
		
	}
	
}
