package it.univaq.disim.se4as.thermostat.planner;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import it.univaq.disim.se4as.thermostat.Models.PresencePrediction;
import it.univaq.disim.se4as.thermostat.Models.TemperatureTrend;
import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager;
import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager.DayOfWeek;
import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager.Interval;
import it.univaq.disim.se4as.thermostat.executor.Executor;
import it.univaq.disim.se4as.thermostat.executor.Executor.OnOff;

public class Planner {

	private BundleContext context;
	private SQLManager sqlManager;
	private Executor executor;
	private List<TemperatureTrend> trends;

	private Double living_area_temperature_threshold;
	private Double sleeping_area_temperature_threshold;
	private Double toilets_temperature_threshold;

	public Planner(BundleContext context) {
		this.context = context;
		this.sqlManager = getSQLmanagerInstance();
		this.executor = getExecutorInstance();

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

	public void receiveTrends(List<TemperatureTrend> trends) {
		this.trends = trends;
	}

	public void startPlanning() {

		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		int day = calendar.get(Calendar.DAY_OF_WEEK);

		for (String room : sqlManager.getRooms()) {

			List<PresencePrediction> presences = sqlManager.getPresenceHistory(room, DayOfWeek.values()[day]);

			TemperatureTrend currentTrend = null;
			for (TemperatureTrend trend : trends) {
				if (trend.getRoom().equals(room)) {
					currentTrend = trend;
				}
			}

			Double currentTemperature = sqlManager.getSensedData(room, "temperature", Interval.LAST).get(0).getValue();
			Double currentPresence = sqlManager.getSensedData(room, "presence", Interval.LAST).get(0).getValue();

			calculatePlan(currentTemperature, currentTrend, currentPresence, day, presences, room);
		}

	}

	private void calculatePlan(Double currentTemperature, TemperatureTrend currentTrend, Double currentPresence,
			int day, List<PresencePrediction> presences, String room) {

		// Get the area
		Double targetTemperature = 0D;

		if (room.substring(0, 7) == "bedroom") {
			targetTemperature = sleeping_area_temperature_threshold;
		}
		if (room.substring(0, 6) == "toilet") {
			targetTemperature = toilets_temperature_threshold;
		}
		if (room.substring(0, 10) == "livingroom" || room.substring(0, 7) == "kitchen") {
			targetTemperature = living_area_temperature_threshold;
		}

		if (currentPresence == 0) { // no presence

			Date currentDate = new Date();

			Long difference = 0L;

			for (int i = 0; i < presences.size(); i++) {

				if (currentDate.after(presences.get(i).getEndTime())) {

				} else {

					if (presences.get(i).getStartTime().before(currentDate)) { // inside prediction interval

					} else {
						difference = (presences.get(i).getStartTime().getTime() - currentDate.getTime()) / 1000;
					}
				}

			}

			if (currentTrend.getSlope() >= 0 && currentTemperature >= targetTemperature) {
				executor.setHeater(OnOff.OFF, room);
			} else {
				if (difference < 3600) {
					executor.setHeater(OnOff.ON, room);
				}
			}

		} else { // presence

			if (currentTemperature >= targetTemperature) {

				if (currentTrend.getSlope() >= 0) {
					executor.setHeater(OnOff.OFF, room);
				} else {
					executor.setHeater(OnOff.ON, room);
				}
			} else {
				executor.setHeater(OnOff.ON, room);
			}
		}

	}

	public void setConfiguration(BundleContext context) {

		System.out
				.println("Planner - Copy thresholds file to " + context.getBundle().getDataFile("").getAbsolutePath());

		// Get type of sensor and room and server URL File configuration =
		File configuration = context.getBundle().getDataFile("threshold.properties");

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

				this.living_area_temperature_threshold = Double
						.parseDouble(properties.getProperty("living_area_temperature_threshold"));
				this.sleeping_area_temperature_threshold = Double
						.parseDouble(properties.getProperty("sleeping_area_temperature_threshold"));
				this.toilets_temperature_threshold = Double
						.parseDouble(properties.getProperty("toilets_temperature_threshold"));
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
