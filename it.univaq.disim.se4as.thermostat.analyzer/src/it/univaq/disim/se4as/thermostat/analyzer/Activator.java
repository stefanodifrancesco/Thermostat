package it.univaq.disim.se4as.thermostat.analyzer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import it.univaq.disim.se4as.thermostat.AnalyzerAPI.AnalyzerAPI;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		
		Activator.context = bundleContext;		
		
		AnalyzerAPI analyzerAPI = new Analyzer(bundleContext);
		analyzerAPI.startAnalysis();
		
		context.registerService(Analyzer.class.getName(), analyzerAPI , null);
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
