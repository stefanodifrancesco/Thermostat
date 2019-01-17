package it.univaq.disim.se4as.thermostat.analyzer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import it.univaq.disim.se4as.thermostat.SQLManager.SQLManager;

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
		
		Analyzer analyzer = new Analyzer(bundleContext);
		analyzer.setConfiguration(bundleContext);
		analyzer.startCheckingThresholds();
		
		context.registerService(Analyzer.class.getName(), analyzer , null);
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
