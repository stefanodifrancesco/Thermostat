package it.univaq.disim.se4as.thermostat.analyzer;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import it.univaq.disim.se4as.thermostat.AnalyzerAPI.AnalyzerAPI;
import it.univaq.disim.se4as.thermostat.DatabaseAPI.DatabaseAPI;
import it.univaq.disim.se4as.thermostat.DatabaseAPI.DatabaseAPI.Interval;
import it.univaq.disim.se4as.thermostat.Models.SensedValue;
import it.univaq.disim.se4as.thermostat.Models.TemperatureTrend;

public class Analyzer implements AnalyzerAPI{

	private BundleContext context;
	
	private AnalysisThread analysisThread;
	private DatabaseAPI databaseAPI;

	public Analyzer(BundleContext context) {
		this.context = context;
	}
	
	public void startAnalysis() {

		databaseAPI = getDatabaseAPIInstance();
		analysisThread = new AnalysisThread(databaseAPI);
		analysisThread.start();
		System.out.println("Analyzer started!");

	}

	public List<TemperatureTrend> calculateTrends(List<String> rooms) {

		List<TemperatureTrend> trends = new ArrayList<TemperatureTrend>();

		for (String room : rooms) {

			List<SensedValue> values = databaseAPI.getSensedData(room, "temperature", Interval.ALL);

			TemperatureTrend trend = new TemperatureTrend();
			trend.setRoom(room);

			double slope = 0;

			int t4 = 0;
			int t1 = 5;
			double T4 = values.get(t4).getValue();
			double T1 = values.get(t1).getValue();
			int deltaTime = t1 - t4;

			slope = (T4 - T1) / deltaTime;

			trend.setSlope(slope);
			System.out.println("slope" + slope);
			trends.add(trend);
		}

		return trends;
	}
	
	public DatabaseAPI getDatabaseAPIInstance() {

		ServiceReference<?>[] refs;

		try {
			refs = context.getAllServiceReferences(DatabaseAPI.class.getName(), null);

			if (refs != null) {

				if (refs[0] != null) {
					DatabaseAPI dbAPI = (DatabaseAPI) context.getService(refs[0]);
					return dbAPI;
				}
			}

		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}

		return null;
	}

}
