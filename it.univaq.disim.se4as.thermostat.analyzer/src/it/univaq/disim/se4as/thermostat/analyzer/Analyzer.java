package it.univaq.disim.se4as.thermostat.analyzer;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager;
import it.univaq.disim.se4as.thermostat.planner.Planner;

public class Analyzer {

	private AnalysisThread analysisThread;

	private BundleContext context;

	public Analyzer(BundleContext context) {
		this.context = context;
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

	public Planner getPlannerInstance() {

		Planner plannerInstance = null;

		ServiceReference<?>[] refs;

		try {
			refs = context.getAllServiceReferences(Planner.class.getName(), null);

			if (refs != null) {

				if (refs[0] != null) {
					Planner planner = (Planner) context.getService(refs[0]);
					if (planner != null) {
						plannerInstance = planner;
					}

				}
			}

		} catch (InvalidSyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return plannerInstance;
	}

	public void startAnalysis() {

		analysisThread = new AnalysisThread(getSQLmanagerInstance(), getPlannerInstance());
		analysisThread.start();
		System.out.println("Analyzer started analyzing!");

	}
}
