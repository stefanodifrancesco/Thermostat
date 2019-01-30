package it.univaq.disim.se4as.thermostat.planner;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.sql.Timestamp;

import javax.swing.tree.TreeCellRenderer;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import it.univaq.disim.se4as.thermostat.Models.PresencePrediction;
import it.univaq.disim.se4as.thermostat.Models.TemperatureTrend;
import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager;
import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager.DayOfWeek;
import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager.Interval;

public class Planner {

	BundleContext context;
	SQLManager sqlManager;
	List<TemperatureTrend> trends;

	public Planner(BundleContext context) {
		this.context = context;
		this.sqlManager = getSQLmanagerInstance();
	}

	public SQLManager getSQLmanagerInstance() {

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

		return sqlManager;
	}

	public void receiveTrends(List<TemperatureTrend> trends) {
		this.trends = trends;
	}

	public void startPlanning() {

		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		int day = calendar.get(Calendar.DAY_OF_WEEK); // TODO Adjust index
		Date currentDate = calendar.getTime();

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

			calculatePlan(currentTemperature, currentTrend, currentPresence, day, presences);
		}

	}

	private void calculatePlan(Double currentTemperature, TemperatureTrend currentTrend, Double currentPresence,
			int day, List<PresencePrediction> presences) {

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
			
			if (currentTrend.getSlope() >= 0 && currentTemperature >= targetTemperarure) {
				// TODO heater OFF
			} else {
				if (difference < 3600) {
					// TODO heater ON
				}
			}
			
			
		} else { // presence

			if (currentTemperature >= targetTemperarure) {

				if (currentTrend.getSlope() >= 0) {
					// TODO heater OFF
				} else {
					// TODO heater ON
				}
			} else {
				// TODO heater ON
			}
		}

	}

}
