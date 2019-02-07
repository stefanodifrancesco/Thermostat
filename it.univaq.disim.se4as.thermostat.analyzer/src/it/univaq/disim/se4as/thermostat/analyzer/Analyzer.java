package it.univaq.disim.se4as.thermostat.analyzer;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import it.univaq.disim.se4as.thermostat.Models.SensedValue;
import it.univaq.disim.se4as.thermostat.Models.TemperatureTrend;
import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager;
import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager.Interval;

public class Analyzer {

	private BundleContext context;
	
	private AnalysisThread analysisThread;
	private SQLManager sqlManager;

	public Analyzer(BundleContext context) {
		this.context = context;
	}
	
	public void startAnalysis() {

		sqlManager = getSQLmanagerInstance();
		analysisThread = new AnalysisThread(sqlManager);
		analysisThread.start();
		System.out.println("Analyzer started!");

	}

	public List<TemperatureTrend> calculateTrends(List<String> rooms) {

		List<TemperatureTrend> trends = new ArrayList<TemperatureTrend>();

		for (String room : rooms) {

			List<SensedValue> values = sqlManager.getSensedData(room, "temperature", Interval.ALL);

			TemperatureTrend trend = new TemperatureTrend();
			trend.setRoom(room);

			double slope = 0;

			int t4 = 0;
			int t1 = 10;
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

}
