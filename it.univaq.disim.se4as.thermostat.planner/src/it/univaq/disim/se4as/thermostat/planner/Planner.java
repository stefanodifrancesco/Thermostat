package it.univaq.disim.se4as.thermostat.planner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore.Entry;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager;
import it.univaq.disim.se4as.thermostat.analyzer.Analyzer;
import it.univaq.disim.se4as.thermostat.executor.Executor;

public class Planner {

	private BundleContext context;

	private Map<String, Double> thresholds = new HashMap<>();

	private PlannerThread plannerThread;

	public Planner(BundleContext context) {
		this.context = context;
	}

	public void startPlanning() {

		plannerThread = new PlannerThread(getSQLmanagerInstance(), getAnalyzerInstance(), getExecutorInstance(), thresholds);
		plannerThread.start();
		System.out.println("Planner started!");

	}

	public void stopPlanning() {
		plannerThread.interrupt();
	}

	public void setConfiguration(BundleContext context) {

		System.out
				.println("Planner - Copy thresholds file to " + context.getBundle().getDataFile("").getAbsolutePath());

		// Get type of sensor and room and server URL File configuration =
		File configuration = context.getBundle().getDataFile("thresholds.properties");

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
				
				for (Object prop : properties.keySet()) {
					String room = (String) prop;
					thresholds.put(room, Double.parseDouble(properties.getProperty(room)));
				}
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

	public Analyzer getAnalyzerInstance() {

		Analyzer analyzerInstance = null;

		ServiceReference<?>[] refs;

		try {
			refs = context.getAllServiceReferences(Analyzer.class.getName(), null);

			if (refs != null) {

				if (refs[0] != null) {
					Analyzer analyzer = (Analyzer) context.getService(refs[0]);
					if (analyzer != null) {
						analyzerInstance = analyzer;
					}

				}
			}

		} catch (InvalidSyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return analyzerInstance;
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
}
